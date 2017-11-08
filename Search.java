/**
    Copyright (C) 2015  Shuang Chen

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cs.min2phase;

/**
 * Rubik's Cube Solver.<br>
 * A much faster and smaller implemention of Two-Phase Algorithm.<br>
 * Symmetry is used to reduce memory used.<br>
 * Total Memory used is about 1MB.<br>
 * @author Shuang Chen
 */
public class Search {

    public static final boolean USE_TWIST_FLIP_PRUN = true;

    /**
     * If this variable is set, only a few entries of the pruning table will be initialized.
     * Hence, the initialization time will be decreased by about 50%, however, the speed
     * of the solver is affected.
     * 0: without partial initialization
     * 1: enable partial initialization, and the initialization will continue during solving
     * 2: enable partial initialization, and the initialization will not continue
     */
    public static final int PARTIAL_INIT_LEVEL = 0;

    //Options for research purpose.
    static final int PRE_MOVE_LEVEL = 2; //0: disable pre-scramble, 1: 1-move pre-scramble, 2: 2-move pre-scramble
    static final boolean TRY_INVERSE = true;
    static final boolean TRY_THREE_AXES = true;
    static final boolean USE_CONJ_PRUN = USE_TWIST_FLIP_PRUN;

    static final int MAX_DEPTH2 = 13;

    static final int PRE_IDX_MAX = PRE_MOVE_LEVEL == 2 ? 113 :
                                   PRE_MOVE_LEVEL == 1 ? 9 : 1;
    static final int PRE_IDX_VALID_MAX = PRE_IDX_MAX / 2 + 1;
    static final int PRE_IDX_MIN = 9;
    static final int PRE_IDX_VALID_MIN = PRE_IDX_MIN / 2 + 1;

    static boolean inited = false;

    private int[] move = new int[31];

    private int[][] corn0 = new int[6][PRE_IDX_VALID_MAX];
    private int[][] ud8e0 = new int[6][PRE_IDX_VALID_MAX];

    private CoordCube[] nodeUD = new CoordCube[21];
    private CoordCube[] nodeRL = new CoordCube[21];
    private CoordCube[] nodeFB = new CoordCube[21];

    private CoordCube[][] node0 = new CoordCube[6][PRE_IDX_VALID_MAX];

    private int p2corn;
    private int p2csym;
    private int p2edge;
    private int p2esym;
    private int p2mid;

    private long selfSym;
    private int preIdxMax;
    private int preIdxMin;
    private int conjMask;
    private int urfIdx;
    private int preIdx;
    private int length1;
    private int depth1;
    private int maxDep2;
    private int sol;
    private String solution;
    private long probe;
    private long probeMax;
    private long probeMin;
    private int verbose;
    private CubieCube cc = new CubieCube();
    private int urfPreInitStatus = 0x000000;
    char[] searchTasks = new char[21];

    private boolean isRec = false;

    /**
     *     Verbose_Mask determines if a " . " separates the phase1 and phase2 parts of the solver string like in F' R B R L2 F .
     *     U2 U D for example.<br>
     */
    public static final int USE_SEPARATOR = 0x1;

    /**
     *     Verbose_Mask determines if the solution will be inversed to a scramble/state generator.
     */
    public static final int INVERSE_SOLUTION = 0x2;

    /**
     *     Verbose_Mask determines if a tag such as "(21f)" will be appended to the solution.
     */
    public static final int APPEND_LENGTH = 0x4;

    /**
     *     Verbose_Mask determines if guaranteeing the solution to be optimal.
     */
    public static final int OPTIMAL_SOLUTION = 0x8;


    public Search() {
        for (int i = 0; i < 21; i++) {
            nodeUD[i] = new CoordCube();
            nodeRL[i] = new CoordCube();
            nodeFB[i] = new CoordCube();
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < PRE_IDX_VALID_MAX; j++) {
                node0[i][j] = new CoordCube();
            }
        }
    }

    /**
     * Computes the solver string for a given cube.
     *
     * @param facelets
     *      is the cube definition string format.<br>
     * The names of the facelet positions of the cube:
     * <pre>
     *             |************|
     *             |*U1**U2**U3*|
     *             |************|
     *             |*U4**U5**U6*|
     *             |************|
     *             |*U7**U8**U9*|
     *             |************|
     * ************|************|************|************|
     * *L1**L2**L3*|*F1**F2**F3*|*R1**R2**F3*|*B1**B2**B3*|
     * ************|************|************|************|
     * *L4**L5**L6*|*F4**F5**F6*|*R4**R5**R6*|*B4**B5**B6*|
     * ************|************|************|************|
     * *L7**L8**L9*|*F7**F8**F9*|*R7**R8**R9*|*B7**B8**B9*|
     * ************|************|************|************|
     *             |************|
     *             |*D1**D2**D3*|
     *             |************|
     *             |*D4**D5**D6*|
     *             |************|
     *             |*D7**D8**D9*|
     *             |************|
     * </pre>
     * A cube definition string "UBL..." means for example: In position U1 we have the U-color, in position U2 we have the
     * B-color, in position U3 we have the L color etc. For example, the "super flip" state is represented as <br>
     * <pre>UBULURUFURURFRBRDRFUFLFRFDFDFDLDRDBDLULBLFLDLBUBRBLBDB</pre>
     * and the state generated by "F U' F2 D' B U R' F' L D' R' U' L U B' D2 R' F U2 D2" can be represented as <br>
     * <pre>FBLLURRFBUUFBRFDDFUULLFRDDLRFBLDRFBLUUBFLBDDBUURRBLDDR</pre>
     * You can also use {@link cs.min2phase.Tools#fromScramble(java.lang.String s)} to convert the scramble string to the
     * cube definition string.
     *
     * @param maxDepth
     *      defines the maximal allowed maneuver length. For random cubes, a maxDepth of 21 usually will return a
     *      solution in less than 0.02 seconds on average. With a maxDepth of 20 it takes about 0.1 seconds on average to find a
     *      solution, but it may take much longer for specific cubes.
     *
     * @param probeMax
     *      defines the maximum number of the probes of phase 2. If it does not return with a solution, it returns with
     *      an error code.
     *
     * @param probeMin
     *      defines the minimum number of the probes of phase 2. So, if a solution is found within given probes, the
     *      computing will continue to find shorter solution(s). Btw, if probeMin > probeMax, probeMin will be set to probeMax.
     *
     * @param verbose
     *      determins the format of the solution(s). see USE_SEPARATOR, INVERSE_SOLUTION, APPEND_LENGTH, OPTIMAL_SOLUTION
     *
     * @return The solution string or an error code:<br>
     *      Error 1: There is not exactly one facelet of each colour<br>
     *      Error 2: Not all 12 edges exist exactly once<br>
     *      Error 3: Flip error: One edge has to be flipped<br>
     *      Error 4: Not all corners exist exactly once<br>
     *      Error 5: Twist error: One corner has to be twisted<br>
     *      Error 6: Parity error: Two corners or two edges have to be exchanged<br>
     *      Error 7: No solution exists for the given maxDepth<br>
     *      Error 8: Probe limit exceeded, no solution within given probMax
     */
    public synchronized String solution(String facelets, int maxDepth, long probeMax, long probeMin, int verbose) {
        int check = verify(facelets);
        if (check != 0) {
            return "Error " + Math.abs(check);
        }
        this.sol = maxDepth + 1;
        this.probe = 0;
        this.probeMax = probeMax;
        this.probeMin = Math.min(probeMin, probeMax);
        this.verbose = verbose;
        this.solution = null;
        this.isRec = false;

        init();

        initSearch();

        return (verbose & OPTIMAL_SOLUTION) == 0 ? search() : searchopt();
    }

    private void initSearch() {
        conjMask = (TRY_INVERSE ? 0 : 0x38) | (TRY_THREE_AXES ? 0 : 0x36);
        CubieCube pc = new CubieCube();
        CubieCube pc2 = new CubieCube();
        selfSym = cc.selfSymmetry();
        conjMask |= (selfSym >> 16 & 0xffff) != 0 ? 0x12 : 0;
        conjMask |= (selfSym >> 32 & 0xffff) != 0 ? 0x24 : 0;
        conjMask |= (selfSym >> 48 & 0xffff) != 0 ? 0x38 : 0;
        selfSym &= 0xffffffffffffL;

        preIdxMax = conjMask > 7 ? 1 : PRE_IDX_MAX;
        preIdxMin = Math.min(preIdxMax, PRE_IDX_MIN);
        for (int i = 0; i < 6; i++) {
            int preIdxValidMaxCur = (conjMask & 1 << i) == 0 ? (preIdxMin + 1) / 2 : 1;
            initConjPreIdxRange(i, 0, preIdxValidMaxCur, false);
        }
        urfPreInitStatus = 0x000000;
    }

    public synchronized String next(long probeMax, long probeMin, int verbose) {
        this.probe = 0;
        this.probeMax = probeMax;
        this.probeMin = Math.min(probeMin, probeMax);
        this.solution = null;
        this.isRec = (this.verbose & OPTIMAL_SOLUTION) == (verbose & OPTIMAL_SOLUTION);
        this.verbose = verbose;
        return (verbose & OPTIMAL_SOLUTION) == 0 ? search() : searchopt();
    }

    public static boolean isInited() {
        return inited;
    }

    public long numberOfProbes() {
        return probe;
    }

    public int length() {
        return sol;
    }

    public synchronized static void init() {
        if (!inited) {
            CubieCube.initMove();
            CubieCube.initSym();
        }

        CoordCube.init();

        inited = true;
    }

    int verify(String facelets) {
        int count = 0x000000;
        byte[] f = new byte[54];
        try {
            String center = new String(
                new char[] {
                    facelets.charAt(Util.U5),
                    facelets.charAt(Util.R5),
                    facelets.charAt(Util.F5),
                    facelets.charAt(Util.D5),
                    facelets.charAt(Util.L5),
                    facelets.charAt(Util.B5)
                }
            );
            for (int i = 0; i < 54; i++) {
                f[i] = (byte) center.indexOf(facelets.charAt(i));
                if (f[i] == -1) {
                    return -1;
                }
                count += 1 << (f[i] << 2);
            }
        } catch (Exception e) {
            return -1;
        }
        if (count != 0x999999) {
            return -1;
        }
        Util.toCubieCube(f, cc);
        return cc.verify();
    }

    void initConjPreIdxRange(int urfIdx, int preIdxAdjStart, int preIdxAdjEnd, boolean initPhase2) {
        if (preIdxAdjStart >= preIdxAdjEnd) {
            return;
        }
        CubieCube pc = new CubieCube();
        CubieCube pc2 = new CubieCube();
        CubieCube ccc = new CubieCube(cc);
        if (urfIdx >= 3) {
            ccc.invCubieCube();
        }
        for (int urf = urfIdx % 3; urf > 0; urf--) {
            ccc.URFConjugate();
        }
        for (int j = preIdxAdjStart; j < preIdxAdjEnd; j++) {
            pc.copy(ccc);
            for (int m : CubieCube.preMoveList[j << 1]) {
                CubieCube.CornMult(CubieCube.moveCube[m], pc, pc2);
                CubieCube.EdgeMult(CubieCube.moveCube[m], pc, pc2);
                pc.copy(pc2);
            }
            if (initPhase2) {
                corn0[urfIdx][j] = pc.getCPermSym();
                ud8e0[urfIdx][j] = pc.getU4Comb() << 16 | pc.getD4Comb();
            } else {
                node0[urfIdx][j].set(pc);
                corn0[urfIdx][j] = -1;
            }
        }
    }

    private String search() {
        if (!isRec) {
            for (int i = 0; i <= 20; i++) {
                searchTasks[i] = 0; // all tasks not finished
            }
        }

        for (length1 = isRec ? length1 : 0; length1 < sol; length1++) {
            maxDep2 = Math.min(MAX_DEPTH2, sol - length1);
            for (urfIdx = isRec ? urfIdx : 0; urfIdx < 6; urfIdx++) {
                if ((conjMask & 1 << urfIdx) != 0) {
                    continue;
                }
                int preIdxStart = 0;
                int status = searchTasks[length1] >> (urfIdx << 1) & 3;
                if (status == 1) {
                    preIdxStart = preIdxMin + 1;
                } else if (status == 3) {
                    preIdxStart = preIdxMax + 1;
                }
                int preIdxEnd = (urfPreInitStatus >> (urfIdx << 1) & 3) != 3 ? preIdxMin : preIdxMax;
                for (preIdx = isRec ? preIdx : preIdxStart; preIdx < preIdxEnd; preIdx += 2) {
                    int preIdxValid = (preIdx + 1) >> 1;
                    node0[urfIdx][preIdxValid].calcPruning(true);
                    depth1 = length1 - CubieCube.preMoveList[preIdx].length;
                    if (node0[urfIdx][preIdxValid].prun <= depth1
                            && phase1(node0[urfIdx][preIdxValid],
                                      (int) selfSym & CubieCube.preMoveSym[preIdx],
                                      depth1, -1) == 0) {
                        return solution == null ? "Error 8" : solution;
                    }
                }
                searchTasks[length1] |= (preIdx >= preIdxMin ? 1 : 0) << (urfIdx << 1);
                searchTasks[length1] |= (preIdx >= preIdxMax ? 3 : 0) << (urfIdx << 1);
                if ((urfPreInitStatus >> (urfIdx << 1) & 3) == 1) {
                    urfPreInitStatus |= 2 << (urfIdx << 1);
                    initConjPreIdxRange(urfIdx, preIdxMin / 2 + 1, preIdxMax / 2 + 1, false);
                    length1 = -1;
                    break;
                }
            }

        }
        return solution == null ? "Error 7" : solution;
    }

    /**
     * @return
     *      0: Found or Probe limit exceeded
     *      1: Try Next Power
     *      2: Try Next Axis
     */
    private int phase1(CoordCube node, int ssym, int maxl, int lm) {
        if (node.prun == 0 && maxl < 5) {
            if (maxl == 0) {
                int ret = initPhase2();
                if (ret == 0 || preIdx == 0) {
                    return ret;
                }
                preIdx--;
                ret = Math.min(initPhase2(), ret);
                preIdx++;
                return ret;
            } else {
                return 1;
            }
        }

        int skipMoves = 0;
        int i = 1;
        for (int s = ssym; (s >>= 1) != 0; i++) {
            if ((s & 1) == 1) {
                skipMoves |= CubieCube.firstMoveSym[i];
            }
        }

        for (int axis = 0; axis < 18; axis += 3) {
            if (axis == lm || axis == lm - 9) {
                continue;
            }
            for (int power = 0; power < 3; power++) {
                int m = axis + power;

                if (isRec && m != move[depth1 - maxl]
                        || skipMoves != 0 && (skipMoves & 1 << m) != 0) {
                    continue;
                }

                int prun = nodeUD[maxl].doMovePrun(node, m, true);
                if (prun > maxl) {
                    break;
                } else if (prun == maxl) {
                    continue;
                }

                if (USE_CONJ_PRUN) {
                    prun = nodeUD[maxl].doMovePrunConj(node, m);
                    if (prun > maxl) {
                        break;
                    } else if (prun == maxl) {
                        continue;
                    }
                }

                move[depth1 - maxl] = m;
                int ret = phase1(nodeUD[maxl], ssym & (int) CubieCube.moveCubeSym[m], maxl - 1, axis);
                if (ret == 0) {
                    return 0;
                } else if (ret == 2) {
                    break;
                }
            }
        }
        return 1;
    }

    private String searchopt() {
        int maxprun1 = 0;
        int maxprun2 = 0;
        for (int i = 0; i < 6; i++) {
            node0[i][0].calcPruning(false);
            if (i < 3) {
                maxprun1 = Math.max(maxprun1, node0[i][0].prun);
            } else {
                maxprun2 = Math.max(maxprun2, node0[i][0].prun);
            }
        }
        urfIdx = maxprun2 > maxprun1 ? 3 : 0;
        preIdx = 0;
        for (length1 = isRec ? length1 : 0; length1 < sol; length1++) {
            CoordCube ud = node0[0 + urfIdx][0];
            CoordCube rl = node0[1 + urfIdx][0];
            CoordCube fb = node0[2 + urfIdx][0];

            if (ud.prun <= length1 && rl.prun <= length1 && fb.prun <= length1
                    && phase1opt(ud, rl, fb, selfSym, length1, -1) == 0) {
                return solution == null ? "Error 8" : solution;
            }
        }
        return solution == null ? "Error 7" : solution;
    }

    /**
     * @return
     *      0: Found or Probe limit exceeded
     *      1: Try Next Power
     *      2: Try Next Axis
     */
    private int phase1opt(CoordCube ud, CoordCube rl, CoordCube fb, long ssym, int maxl, int lm) {
        if (ud.prun == 0 && rl.prun == 0 && fb.prun == 0 && maxl < 5) {
            maxDep2 = maxl + 1;
            depth1 = length1 - maxl;
            return initPhase2() == 0 ? 0 : 1;
        }

        int skipMoves = 0;
        int i = 1;
        for (long s = ssym; (s >>= 1) != 0; i++) {
            if ((s & 1) == 1) {
                skipMoves |= CubieCube.firstMoveSym[i];
            }
        }

        for (int axis = 0; axis < 18; axis += 3) {
            if (axis == lm || axis == lm - 9) {
                continue;
            }
            for (int power = 0; power < 3; power++) {
                int m = axis + power;

                if (isRec && m != move[length1 - maxl]
                        || skipMoves != 0 && (skipMoves & 1 << m) != 0) {
                    continue;
                }

                // UD Axis
                int prun_ud = Math.max(
                                  nodeUD[maxl].doMovePrun(ud, m, false),
                                  USE_CONJ_PRUN ? nodeUD[maxl].doMovePrunConj(ud, m) : 0
                              );
                if (prun_ud > maxl) {
                    break;
                } else if (prun_ud == maxl) {
                    continue;
                }

                // RL Axis
                m = CubieCube.urfMove[2][m];

                int prun_rl = Math.max(
                                  nodeRL[maxl].doMovePrun(rl, m, false),
                                  USE_CONJ_PRUN ? nodeRL[maxl].doMovePrunConj(rl, m) : 0
                              );
                if (prun_rl > maxl) {
                    break;
                } else if (prun_rl == maxl) {
                    continue;
                }

                // FB Axis
                m = CubieCube.urfMove[2][m];

                int prun_fb = Math.max(
                                  nodeFB[maxl].doMovePrun(fb, m, false),
                                  USE_CONJ_PRUN ? nodeFB[maxl].doMovePrunConj(fb, m) : 0
                              );
                if (prun_ud == prun_rl && prun_rl == prun_fb && prun_fb != 0) {
                    prun_fb++;
                }

                if (prun_fb > maxl) {
                    break;
                } else if (prun_fb == maxl) {
                    continue;
                }

                m = CubieCube.urfMove[2][m];

                move[length1 - maxl] = m;
                int ret = phase1opt(nodeUD[maxl], nodeRL[maxl], nodeFB[maxl], ssym & CubieCube.moveCubeSym[m], maxl - 1, axis);
                if (ret == 0) {
                    return 0;
                }
            }
        }
        return 1;
    }

    void initPhase2State() {
        int preIdxValid = (preIdx + 1) >> 1;

        if (preIdx % 2 == 0) {
            urfPreInitStatus |= 1 << (urfIdx << 1);
            if (corn0[urfIdx][preIdxValid] == -1) {
                initConjPreIdxRange(urfIdx, preIdxValid, preIdxValid + 1, true);
            }
            p2corn = corn0[urfIdx][preIdxValid] >> 4;
            p2csym = corn0[urfIdx][preIdxValid] & 0xf;
            p2mid = node0[urfIdx][preIdxValid].slice;
            for (int i = 0; i < depth1; i++) {
                int m = move[i];
                p2corn = CoordCube.CPermMove[p2corn][Util.std2ud[CubieCube.SymMove[p2csym][m]]];
                p2csym = CubieCube.SymMult[p2corn & 0xf][p2csym];
                p2corn >>= 4;

                int cx = CoordCube.UDSliceMove[p2mid & 0x1ff][m];
                p2mid = Util.permMult[p2mid >> 9][cx >> 9] << 9 | cx & 0x1ff;
            }
            p2mid >>= 9;

            int u4e = ud8e0[urfIdx][preIdxValid] >> 16;
            int d4e = ud8e0[urfIdx][preIdxValid] & 0xffff;
            for (int i = 0; i < depth1; i++) {
                int m = move[i];

                int cx = CoordCube.UDSliceMove[u4e & 0x1ff][m];
                u4e = Util.permMult[u4e >> 9][cx >> 9] << 9 | cx & 0x1ff;

                cx = CoordCube.UDSliceMove[d4e & 0x1ff][m];
                d4e = Util.permMult[d4e >> 9][cx >> 9] << 9 | cx & 0x1ff;
            }

            p2edge = CubieCube.MtoEPerm[494 - (u4e & 0x1ff) + (u4e >> 9) * 70 + (d4e >> 9) * 1680];
            p2esym = p2edge & 0xf;
            p2edge >>= 4;
        } else {
            int m = Util.std2ud[CubieCube.preMoveList[preIdx][CubieCube.preMoveList[preIdx].length - 1] / 3 * 3 + 1];

            int p2edgeI = CubieCube.getPermSymInv(p2edge, p2esym, false);
            int p2cornI = CubieCube.getPermSymInv(p2corn, p2csym, true);
            int p2midI = Util.permInv[p2mid];

            int p2cornIx = CoordCube.CPermMove[p2cornI >> 4][CubieCube.SymMoveUD[p2cornI & 0xf][m]];
            int p2csymIx = CubieCube.SymMult[p2cornIx & 0xf][p2cornI & 0xf];
            int p2edgeIx = CoordCube.EPermMove[p2edgeI >> 4][CubieCube.SymMoveUD[p2edgeI & 0xf][m]];
            int p2esymIx = CubieCube.SymMult[p2edgeIx & 0xf][p2edgeI & 0xf];
            int p2midIx = CoordCube.MPermMove[p2midI][m];

            p2edge = CubieCube.getPermSymInv(p2edgeIx >> 4, p2esymIx, false);
            p2esym = p2edge & 0xf;
            p2edge >>= 4;
            p2corn = CubieCube.getPermSymInv(p2cornIx >> 4, p2csymIx, true);
            p2csym = p2corn & 0xf;
            p2corn >>= 4;
            p2mid = Util.permInv[p2midIx];
        }
    }

    /**
     * @return
     *      0: Found or Probe limit exceeded
     *      1: Try Next Power
     *      2: Try Next Axis
     */
    private int initPhase2() {
        isRec = false;
        if (probe >= (solution == null ? probeMax : probeMin)) {
            return 0;
        }
        ++probe;
        if (preIdx % 2 == 1) {
            --probe;
        }
        initPhase2State();
        int prun = Math.max(
                       CoordCube.getPruning(CoordCube.EPermCCombPrun,
                                            p2edge * 70 + CoordCube.CCombConj[CubieCube.Perm2Comb[p2corn]][CubieCube.SymMultInv[p2esym][p2csym]]),
                       Math.max(
                           CoordCube.getPruning(CoordCube.MEPermPrun,
                                                p2edge * 24 + CoordCube.MPermConj[p2mid][p2esym]),
                           CoordCube.getPruning(CoordCube.MCPermPrun,
                                                p2corn * 24 + CoordCube.MPermConj[p2mid][p2csym])));

        if (prun >= maxDep2) {
            return prun > maxDep2 ? 2 : 1;
        }

        int lm = 10;
        if (depth1 >= 2 && move[depth1 - 1] / 3 % 3 == move[depth1 - 2] / 3 % 3) {
            lm = Util.std2ud[Math.max(move[depth1 - 1], move[depth1 - 2]) / 3 * 3 + 1];
        } else if (depth1 >= 1) {
            lm = Util.std2ud[move[depth1 - 1] / 3 * 3 + 1];
            if (move[depth1 - 1] > Util.Fx3) {
                lm = -lm;
            }
        }

        int depth2;
        for (depth2 = maxDep2 - 1; depth2 >= prun; depth2--) {
            int ret = phase2(p2edge, p2esym, p2corn, p2csym, p2mid, depth2, depth1, lm);
            if (ret < 0) {
                break;
            }
            depth2 = depth2 - ret;
            sol = depth1 + depth2;
            if (preIdx != 0) {
                assert depth2 > 0; //If depth2 == 0, the solution is optimal. In this case, we won't try preScramble to find shorter solutions.
                for (int i = CubieCube.preMoveList[preIdx].length - 1; i >= 0; i--) {
                    appendPreMove(CubieCube.preMoveList[preIdx][i], depth2);
                }
            }
            solution = solutionToString();
        }

        if (depth2 != maxDep2 - 1) { //At least one solution has been found.
            maxDep2 = Math.min(MAX_DEPTH2, sol - length1);
            return probe >= probeMin ? 0 : 1;
        } else {
            return 1;
        }
    }

    void appendPreMove(int preMove, int depth2) {
        assert depth2 > 0; //If depth2 == 0, the solution is optimal. In this case, we won't try preScramble to find shorter solutions.
        int axisPre = preMove / 3;
        int axisLast = move[sol - 1] / 3;
        if (axisPre == axisLast) {
            int pow = (preMove % 3 + move[sol - 1] % 3 + 1) % 4;
            move[sol - 1] = axisPre * 3 + pow;
        } else if (depth2 > 1
                   && axisPre % 3 == axisLast % 3
                   && move[sol - 2] / 3 == axisPre) {
            int pow = (preMove % 3 + move[sol - 2] % 3 + 1) % 4;
            move[sol - 2] = axisPre * 3 + pow;
        } else {
            move[sol++] = preMove;
        }
    }

    //-1: no solution found
    // X: solution with X moves shorter than expectation. Hence, the length of the solution is  depth - X
    private int phase2(int edge, int esym, int corn, int csym, int mid, int maxl, int depth, int lm) {
        if (edge == 0 && corn == 0 && mid == 0) {
            return maxl;
        }
        int moveMask = lm < 0 ? (1 << (-lm)) : Util.ckmv2bit[lm];
        for (int m = 0; m < 10; m++) {
            if ((moveMask >> m & 1) != 0) {
                m += 0x42 >> m & 3;
                continue;
            }
            int midx = CoordCube.MPermMove[mid][m];
            int cornx = CoordCube.CPermMove[corn][CubieCube.SymMoveUD[csym][m]];
            int csymx = CubieCube.SymMult[cornx & 0xf][csym];
            cornx >>= 4;
            if (CoordCube.getPruning(CoordCube.MCPermPrun,
                                     cornx * 24 + CoordCube.MPermConj[midx][csymx]) >= maxl) {
                continue;
            }
            int edgex = CoordCube.EPermMove[edge][CubieCube.SymMoveUD[esym][m]];
            int esymx = CubieCube.SymMult[edgex & 0xf][esym];
            edgex >>= 4;
            if (CoordCube.getPruning(CoordCube.EPermCCombPrun,
                                     edgex * 70 + CoordCube.CCombConj[CubieCube.Perm2Comb[cornx]][CubieCube.SymMultInv[esymx][csymx]]) >= maxl) {
                continue;
            }
            if (CoordCube.getPruning(CoordCube.MEPermPrun,
                                     edgex * 24 + CoordCube.MPermConj[midx][esymx]) >= maxl) {
                continue;
            }
            int edgei = CubieCube.getPermSymInv(edgex, esymx, false);
            int corni = CubieCube.getPermSymInv(cornx, csymx, true);
            if (CoordCube.getPruning(CoordCube.EPermCCombPrun,
                                     (edgei >> 4) * 70 + CoordCube.CCombConj[CubieCube.Perm2Comb[corni >> 4]][CubieCube.SymMultInv[edgei & 0xf][corni & 0xf]]) >= maxl) {
                continue;
            }

            int ret = phase2(edgex, esymx, cornx, csymx, midx, maxl - 1, depth + 1, (lm < 0 && m + lm == -5) ? -lm : m);
            if (ret >= 0) {
                move[depth] = Util.ud2std[m];
                return ret;
            }
        }
        return -1;
    }

    private String solutionToString() {
        StringBuffer sb = new StringBuffer();
        int urf = (verbose & INVERSE_SOLUTION) != 0 ? (urfIdx + 3) % 6 : urfIdx;
        if (urf < 3) {
            for (int s = 0; s < sol; s++) {
                if ((verbose & USE_SEPARATOR) != 0 && s == depth1) {
                    sb.append(".  ");
                }
                sb.append(Util.move2str[CubieCube.urfMove[urf][move[s]]]).append(' ');
            }
        } else {
            for (int s = sol - 1; s >= 0; s--) {
                sb.append(Util.move2str[CubieCube.urfMove[urf][move[s]]]).append(' ');
                if ((verbose & USE_SEPARATOR) != 0 && s == depth1) {
                    sb.append(".  ");
                }
            }
        }
        if ((verbose & APPEND_LENGTH) != 0) {
            sb.append("(").append(sol).append("f)");
        }
        return sb.toString();
    }
}
