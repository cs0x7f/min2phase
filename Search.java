/**
    Copyright (C) 2012  Shuang Chen

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

    static final boolean USE_TWIST_FLIP_PRUN = true;

    //Options for research purpose.
    static final boolean TRY_PRE_MOVE = true;
    static final boolean TRY_INVERSE = true;
    static final boolean TRY_THREE_AXES = true;

    private static final int PRE_IDX_MAX = TRY_PRE_MOVE ? 9 : 1;

    static boolean inited = false;

    private int[] move = new int[31];

    private int[] corn = new int[20];
    private int[] mid4 = new int[20];
    private int[] ud8e = new int[20];

    private int[][] twist = new int[6][9];
    private int[][] flip = new int[6][9];
    private int[][] slice = new int[6][9];

    private int[][] corn0 = new int[6][9];
    private int[][] ud8e0 = new int[6][9];
    private int[][] prun = new int[6][9];


    private byte[] f = new byte[54];

    private int conjMask;
    private int urfIdx;
    private int preIdx;
    private int length1;
    private int depth1;
    private int maxDep2;
    private int sol;
    private int valid1;
    private int valid2;
    private String solution;
    private long probe;
    private long probeMax;
    private long probeMin;
    private int verbose;
    private CubieCube cc = new CubieCube();

    private boolean isRecovery = false;

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
     * B-color, in position U3 we have the L color etc. according to the order U1, U2, U3, U4, U5, U6, U7, U8, U9, R1, R2,
     * R3, R4, R5, R6, R7, R8, R9, F1, F2, F3, F4, F5, F6, F7, F8, F9, D1, D2, D3, D4, D5, D6, D7, D8, D9, L1, L2, L3, L4,
     * L5, L6, L7, L8, L9, B1, B2, B3, B4, B5, B6, B7, B8, B9 of the enum constants.
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
        this.isRecovery = false;

        init();

        conjMask = (TRY_INVERSE ? 0 : 0x38) | (TRY_THREE_AXES ? 0 : 0x36);
        CubieCube pc = new CubieCube();
        CubieCube[] preList = new CubieCube[] {
            new CubieCube(), CubieCube.moveCube[3], CubieCube.moveCube[5],
            CubieCube.moveCube[6], CubieCube.moveCube[8], CubieCube.moveCube[12],
            CubieCube.moveCube[14], CubieCube.moveCube[15], CubieCube.moveCube[17]
        };
        for (int i = 0; i < 6; i++) {

            for (int j = 0; j < PRE_IDX_MAX; j++) {
                CubieCube.CornMult(preList[j], cc, pc);
                CubieCube.EdgeMult(preList[j], cc, pc);
                twist[i][j] = pc.getTwistSym();
                flip[i][j] = pc.getFlipSym();
                slice[i][j] = pc.getUDSlice();
                corn0[i][j] = pc.getCPermSym();
                ud8e0[i][j] = pc.getU4Comb() << 16 | pc.getD4Comb();
            }

            cc.URFConjugate();
            if (i % 3 == 2) {
                cc.invCubieCube();
            }
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < i; j++) { //If S_i^-1 * C * S_i == C, It's unnecessary to compute it again.
                if (twist[i][0] == twist[j][0] && flip[i][0] == flip[j][0] && slice[i][0] == slice[j][0]
                        && corn0[i][0] == corn0[j][0] && ud8e0[i][0] == ud8e0[j][0]) {
                    conjMask |= 1 << i;
                    break;
                }
            }
            if ((conjMask & (1 << i)) != 0) {
                continue;
            }
            for (int j = 0; j < PRE_IDX_MAX; j++) {
                prun[i][j] = Math.max(Math.max(
                                          CoordCube.getPruning(CoordCube.UDSliceTwistPrun,
                                                  (twist[i][j] >>> 3) * 495 + CoordCube.UDSliceConj[slice[i][j] & 0x1ff][twist[i][j] & 7]),
                                          CoordCube.getPruning(CoordCube.UDSliceFlipPrun,
                                                  (flip[i][j] >>> 3) * 495 + CoordCube.UDSliceConj[slice[i][j] & 0x1ff][flip[i][j] & 7])),
                                      USE_TWIST_FLIP_PRUN ? CoordCube.getPruning(CoordCube.TwistFlipPrun,
                                              (twist[i][j] >>> 3) * 2688 + (flip[i][j] & 0xfff8 | CubieCube.Sym8MultInv[flip[i][j] & 7][twist[i][j] & 7])) : 0);
            }
        }
        return (verbose & OPTIMAL_SOLUTION) == 0 ? search() : searchopt();
    }

    public synchronized String next(long probeMax, long probeMin, int verbose) {
        this.probe = 0;
        this.probeMax = probeMax;
        this.probeMin = Math.min(probeMin, probeMax);
        this.solution = null;
        this.isRecovery = (this.verbose & OPTIMAL_SOLUTION) == (verbose & OPTIMAL_SOLUTION);
        this.verbose = verbose;
        return (verbose & OPTIMAL_SOLUTION) == 0 ? search() : searchopt();
    }

    public static boolean isInited() {
        return inited;
    }

    public long numberOfProbes() {
        return probe;
    }

    public synchronized static void init() {
        if (inited) {
            return;
        }
        CubieCube.initMove();
        CubieCube.initSym();
        CubieCube.initFlipSym2Raw();
        CubieCube.initTwistSym2Raw();
        CubieCube.initPermSym2Raw();

        CoordCube.initFlipMove();
        CoordCube.initTwistMove();
        CoordCube.initUDSliceMoveConj();

        CoordCube.initCPermMove();
        CoordCube.initEPermMove();
        CoordCube.initMPermMoveConj();

        if (USE_TWIST_FLIP_PRUN) {
            CoordCube.initTwistFlipPrun();
        }
        CoordCube.initSliceTwistPrun();
        CoordCube.initSliceFlipPrun();
        CoordCube.initMEPermPrun();
        CoordCube.initMCPermPrun();
        inited = true;
    }

    int verify(String facelets) {
        int count = 0x000000;
        try {
            String center = new String(
                new char[] {
                    facelets.charAt(4),
                    facelets.charAt(13),
                    facelets.charAt(22),
                    facelets.charAt(31),
                    facelets.charAt(40),
                    facelets.charAt(49)
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

    private String search() {
        for (length1 = isRecovery ? length1 : 0; length1 < sol; length1++) {
            maxDep2 = Math.min(12, sol - length1);
            for (urfIdx = isRecovery ? urfIdx : 0; urfIdx < 6; urfIdx++) {
                if ((conjMask & (1 << urfIdx)) != 0) {
                    continue;
                }
                for (preIdx = isRecovery ? preIdx : 0; preIdx < PRE_IDX_MAX; preIdx++) {
                    corn[0] = corn0[urfIdx][preIdx];
                    mid4[0] = slice[urfIdx][preIdx];
                    ud8e[0] = ud8e0[urfIdx][preIdx];
                    valid1 = 0;
                    depth1 = length1 - (preIdx == 0 ? 0 : 1);
                    if ((prun[urfIdx][preIdx] <= depth1) &&
                            phase1(twist[urfIdx][preIdx] >>> 3, twist[urfIdx][preIdx] & 7,
                                   flip[urfIdx][preIdx] >>> 3, flip[urfIdx][preIdx] & 7,
                                   slice[urfIdx][preIdx] & 0x1ff, depth1, -1) == 0) {
                        return solution == null ? "Error 8" : solution;
                    }
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
    private int phase1(int twist, int tsym, int flip, int fsym, int slice, int maxl, int lm) {
        if (twist == 0 && flip == 0 && slice == 0 && maxl < 5) {
            return maxl == 0 ? initPhase2() : 1;
        }

        for (int axis = 0; axis < 18; axis += 3) {
            if (axis == lm || axis == lm - 9 || (isRecovery && axis < move[depth1 - maxl] - 2)) {
                continue;
            }
            for (int power = 0; power < 3; power++) {
                int m = axis + power;

                if (isRecovery && m != move[depth1 - maxl]) {
                    continue;
                }

                int slicex = CoordCube.UDSliceMove[slice][m] & 0x1ff;
                int twistx = CoordCube.TwistMove[twist][CubieCube.Sym8Move[tsym][m]];
                int tsymx = CubieCube.Sym8Mult[twistx & 7][tsym];
                twistx >>>= 3;
                int prun = CoordCube.getPruning(CoordCube.UDSliceTwistPrun,
                                                twistx * 495 + CoordCube.UDSliceConj[slicex][tsymx]);
                if (prun > maxl) {
                    break;
                } else if (prun == maxl) {
                    continue;
                }
                int flipx = CoordCube.FlipMove[flip][CubieCube.Sym8Move[fsym][m]];
                int fsymx = CubieCube.Sym8Mult[flipx & 7][fsym];
                flipx >>>= 3;
                if (USE_TWIST_FLIP_PRUN) {
                    prun = CoordCube.getPruning(CoordCube.TwistFlipPrun,
                                                (twistx * 336 + flipx) << 3 | CubieCube.Sym8MultInv[fsymx][tsymx]);
                    if (prun > maxl) {
                        break;
                    } else if (prun == maxl) {
                        continue;
                    }
                }
                prun = CoordCube.getPruning(CoordCube.UDSliceFlipPrun,
                                            flipx * 495 + CoordCube.UDSliceConj[slicex][fsymx]);
                if (prun > maxl) {
                    break;
                } else if (prun == maxl) {
                    continue;
                }
                move[depth1 - maxl] = m;
                valid1 = Math.min(valid1, depth1 - maxl);
                int ret = phase1(twistx, tsymx, flipx, fsymx, slicex, maxl - 1, axis);
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
        for (length1 = isRecovery ? length1 : 0; length1 < sol; length1++) {
            urfIdx = 0;
            preIdx = 0;
            corn[0] = corn0[0][0];
            mid4[0] = slice[0][0];
            ud8e[0] = ud8e0[0][0];
            valid1 = 0;
            if ((prun[0][0] <= length1) &&
                    phase1opt(twist[0][0] >>> 3, twist[0][0] & 7, flip[0][0] >>> 3, flip[0][0] & 7, slice[0][0] & 0x1ff,
                              twist[1][0] >>> 3, twist[1][0] & 7, flip[1][0] >>> 3, flip[1][0] & 7, slice[1][0] & 0x1ff,
                              twist[2][0] >>> 3, twist[2][0] & 7, flip[2][0] >>> 3, flip[2][0] & 7, slice[2][0] & 0x1ff,
                              length1, -1) == 0) {
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
    private int phase1opt(
        int ud_twist, int ud_tsym, int ud_flip, int ud_fsym, int ud_slice,
        int rl_twist, int rl_tsym, int rl_flip, int rl_fsym, int rl_slice,
        int fb_twist, int fb_tsym, int fb_flip, int fb_fsym, int fb_slice,
        int maxl, int lm) {

        if (ud_twist == 0 && ud_flip == 0 && ud_slice == 0 && maxl < 5) {
            maxDep2 = maxl + 1;
            depth1 = length1 - maxl;
            return initPhase2();
        }

        for (int axis = 0; axis < 18; axis += 3) {
            if (axis == lm || axis == lm - 9 || (isRecovery && axis < move[length1 - maxl] - 2)) {
                continue;
            }
            for (int power = 0; power < 3; power++) {
                int m = axis + power;

                if (isRecovery && m != move[length1 - maxl]) {
                    continue;
                }

                // UD Axis
                int ud_slicex = CoordCube.UDSliceMove[ud_slice][m] & 0x1ff;
                int ud_twistx = CoordCube.TwistMove[ud_twist][CubieCube.Sym8Move[ud_tsym][m]];
                int ud_tsymx = CubieCube.Sym8Mult[ud_twistx & 7][ud_tsym];
                ud_twistx >>>= 3;
                int ud_prun = CoordCube.getPruning(CoordCube.UDSliceTwistPrun,
                                                   ud_twistx * 495 + CoordCube.UDSliceConj[ud_slicex][ud_tsymx]);
                if (ud_prun > maxl) {
                    break;
                } else if (ud_prun == maxl) {
                    continue;
                }
                int ud_flipx = CoordCube.FlipMove[ud_flip][CubieCube.Sym8Move[ud_fsym][m]];
                int ud_fsymx = CubieCube.Sym8Mult[ud_flipx & 7][ud_fsym];
                ud_flipx >>>= 3;
                if (USE_TWIST_FLIP_PRUN) {
                    ud_prun = Math.max(ud_prun, CoordCube.getPruning(CoordCube.TwistFlipPrun,
                                       (ud_twistx * 336 + ud_flipx) << 3 | CubieCube.Sym8MultInv[ud_fsymx][ud_tsymx]));
                    if (ud_prun > maxl) {
                        break;
                    } else if (ud_prun == maxl) {
                        continue;
                    }
                }
                ud_prun = Math.max(ud_prun, CoordCube.getPruning(CoordCube.UDSliceFlipPrun,
                                   ud_flipx * 495 + CoordCube.UDSliceConj[ud_slicex][ud_fsymx]));
                if (ud_prun > maxl) {
                    break;
                } else if (ud_prun == maxl) {
                    continue;
                }

                // RL Axis
                m = CubieCube.urfMove[2][m];
                int rl_slicex = CoordCube.UDSliceMove[rl_slice][m] & 0x1ff;
                int rl_twistx = CoordCube.TwistMove[rl_twist][CubieCube.Sym8Move[rl_tsym][m]];
                int rl_tsymx = CubieCube.Sym8Mult[rl_twistx & 7][rl_tsym];
                rl_twistx >>>= 3;
                int rl_prun = CoordCube.getPruning(CoordCube.UDSliceTwistPrun,
                                                   rl_twistx * 495 + CoordCube.UDSliceConj[rl_slicex][rl_tsymx]);
                if (rl_prun > maxl) {
                    break;
                } else if (rl_prun == maxl) {
                    continue;
                }
                int rl_flipx = CoordCube.FlipMove[rl_flip][CubieCube.Sym8Move[rl_fsym][m]];
                int rl_fsymx = CubieCube.Sym8Mult[rl_flipx & 7][rl_fsym];
                rl_flipx >>>= 3;
                if (USE_TWIST_FLIP_PRUN) {
                    rl_prun = Math.max(rl_prun, CoordCube.getPruning(CoordCube.TwistFlipPrun,
                                       (rl_twistx * 336 + rl_flipx) << 3 | CubieCube.Sym8MultInv[rl_fsymx][rl_tsymx]));
                    if (rl_prun > maxl) {
                        break;
                    } else if (rl_prun == maxl) {
                        continue;
                    }
                }
                rl_prun = Math.max(rl_prun, CoordCube.getPruning(CoordCube.UDSliceFlipPrun,
                                   rl_flipx * 495 + CoordCube.UDSliceConj[rl_slicex][rl_fsymx]));
                if (rl_prun > maxl) {
                    break;
                } else if (rl_prun == maxl) {
                    continue;
                }

                // FB Axis
                m = CubieCube.urfMove[2][m];
                int fb_slicex = CoordCube.UDSliceMove[fb_slice][m] & 0x1ff;
                int fb_twistx = CoordCube.TwistMove[fb_twist][CubieCube.Sym8Move[fb_tsym][m]];
                int fb_tsymx = CubieCube.Sym8Mult[fb_twistx & 7][fb_tsym];
                fb_twistx >>>= 3;
                int fb_prun = CoordCube.getPruning(CoordCube.UDSliceTwistPrun,
                                                   fb_twistx * 495 + CoordCube.UDSliceConj[fb_slicex][fb_tsymx]);
                if (fb_prun > maxl) {
                    break;
                } else if (fb_prun == maxl) {
                    continue;
                }
                int fb_flipx = CoordCube.FlipMove[fb_flip][CubieCube.Sym8Move[fb_fsym][m]];
                int fb_fsymx = CubieCube.Sym8Mult[fb_flipx & 7][fb_fsym];
                fb_flipx >>>= 3;
                if (USE_TWIST_FLIP_PRUN) {
                    fb_prun = Math.max(fb_prun, CoordCube.getPruning(CoordCube.TwistFlipPrun,
                                       (fb_twistx * 336 + fb_flipx) << 3 | CubieCube.Sym8MultInv[fb_fsymx][fb_tsymx]));
                    if (fb_prun > maxl) {
                        break;
                    } else if (fb_prun == maxl) {
                        continue;
                    }
                }
                fb_prun = Math.max(fb_prun, CoordCube.getPruning(CoordCube.UDSliceFlipPrun,
                                   fb_flipx * 495 + CoordCube.UDSliceConj[fb_slicex][fb_fsymx]));

                if (ud_prun == rl_prun && rl_prun == fb_prun && fb_prun != 0) {
                    fb_prun++;
                }
                if (fb_prun > maxl) {
                    break;
                } else if (fb_prun == maxl) {
                    continue;
                }


                m = CubieCube.urfMove[2][m];

                move[length1 - maxl] = m;
                valid1 = Math.min(valid1, length1 - maxl);
                int ret = phase1opt(
                              ud_twistx, ud_tsymx, ud_flipx, ud_fsymx, ud_slicex,
                              rl_twistx, rl_tsymx, rl_flipx, rl_fsymx, rl_slicex,
                              fb_twistx, fb_tsymx, fb_flipx, fb_fsymx, fb_slicex,
                              maxl - 1, axis);
                if (ret == 0) {
                    return 0;
                } else if (ret == 2) {
                    break;
                }
            }
        }
        return 1;
    }

    /**
     * @return
     *      0: Found or Probe limit exceeded
     *      1: Try Next Power
     *      2: Try Next Axis
     */
    private int initPhase2() {
        isRecovery = false;
        if (probe >= (solution == null ? probeMax : probeMin)) {
            return 0;
        }
        ++probe;
        valid2 = Math.min(valid2, valid1);
        int cidx = corn[valid1] >>> 4;
        int csym = corn[valid1] & 0xf;
        for (int i = valid1; i < depth1; i++) {
            int m = move[i];
            cidx = CoordCube.CPermMove[cidx][CubieCube.SymMove[csym][m]];
            csym = CubieCube.SymMult[cidx & 0xf][csym];
            cidx >>>= 4;
            corn[i + 1] = cidx << 4 | csym;

            int cx = CoordCube.UDSliceMove[mid4[i] & 0x1ff][m];
            mid4[i + 1] = Util.permMult[mid4[i] >>> 9][cx >>> 9] << 9 | cx & 0x1ff;
        }
        valid1 = depth1;
        int mid = mid4[depth1] >>> 9;
        int prun = CoordCube.getPruning(CoordCube.MCPermPrun, cidx * 24 + CoordCube.MPermConj[mid][csym]);
        if (prun >= maxDep2) {
            return prun > maxDep2 ? 2 : 1;
        }

        int u4e = ud8e[valid2] >>> 16;
        int d4e = ud8e[valid2] & 0xffff;
        for (int i = valid2; i < depth1; i++) {
            int m = move[i];

            int cx = CoordCube.UDSliceMove[u4e & 0x1ff][m];
            u4e = Util.permMult[u4e >>> 9][cx >>> 9] << 9 | cx & 0x1ff;

            cx = CoordCube.UDSliceMove[d4e & 0x1ff][m];
            d4e = Util.permMult[d4e >>> 9][cx >>> 9] << 9 | cx & 0x1ff;

            ud8e[i + 1] = u4e << 16 | d4e;
        }
        valid2 = depth1;

        int edge = CubieCube.MtoEPerm[494 - (u4e & 0x1ff) + (u4e >>> 9) * 70 + (d4e >>> 9) * 1680];
        int esym = edge & 15;
        edge >>>= 4;

        prun = Math.max(CoordCube.getPruning(CoordCube.MEPermPrun, edge * 24 + CoordCube.MPermConj[mid][esym]), prun);
        if (prun >= maxDep2) {
            return prun > maxDep2 ? 2 : 1;
        }

        int lm = depth1 == 0 ? 10 : Util.std2ud[move[depth1 - 1] / 3 * 3 + 1];
        for (int depth2 = prun; depth2 < maxDep2; depth2++) {
            if (phase2(edge, esym, cidx, csym, mid, depth2, depth1, lm)) {
                sol = depth1 + depth2;
                if (preIdx != 0) {
                    move[sol++] = Util.preMove[preIdx];
                }
                maxDep2 = Math.min(12, sol - length1);
                solution = solutionToString();
                return probe >= probeMin ? 0 : 1;
            }
        }
        return 1;
    }

    private boolean phase2(int eidx, int esym, int cidx, int csym, int mid, int maxl, int depth, int lm) {
        if (eidx == 0 && cidx == 0 && mid == 0) {
            return true;
        }
        for (int m = 0; m < 10; m++) {
            if (Util.ckmv2[lm][m]) {
                continue;
            }
            int midx = CoordCube.MPermMove[mid][m];
            int cidxx = CoordCube.CPermMove[cidx][CubieCube.SymMove[csym][Util.ud2std[m]]];
            int csymx = CubieCube.SymMult[cidxx & 15][csym];
            cidxx >>>= 4;
            if (CoordCube.getPruning(CoordCube.MCPermPrun,
                                     cidxx * 24 + CoordCube.MPermConj[midx][csymx]) >= maxl) {
                continue;
            }
            int eidxx = CoordCube.EPermMove[eidx][CubieCube.SymMoveUD[esym][m]];
            int esymx = CubieCube.SymMult[eidxx & 15][esym];
            eidxx >>>= 4;
            if (CoordCube.getPruning(CoordCube.MEPermPrun,
                                     eidxx * 24 + CoordCube.MPermConj[midx][esymx]) >= maxl) {
                continue;
            }
            if (phase2(eidxx, esymx, cidxx, csymx, midx, maxl - 1, depth + 1, m)) {
                move[depth] = Util.ud2std[m];
                return true;
            }
        }
        return false;
    }

    private String solutionToString() {
        StringBuffer sb = new StringBuffer();
        int urf = (verbose & INVERSE_SOLUTION) != 0 ? (urfIdx + 3) % 6 : urfIdx;
        if (urf < 3) {
            for (int s = 0; s < depth1; s++) {
                sb.append(Util.move2str[CubieCube.urfMove[urf][move[s]]]).append(' ');
            }
            if ((verbose & USE_SEPARATOR) != 0) {
                sb.append(".  ");
            }
            for (int s = depth1; s < sol; s++) {
                sb.append(Util.move2str[CubieCube.urfMove[urf][move[s]]]).append(' ');
            }
        } else {
            for (int s = sol - 1; s >= depth1; s--) {
                sb.append(Util.move2str[CubieCube.urfMove[urf][move[s]]]).append(' ');
            }
            if ((verbose & USE_SEPARATOR) != 0) {
                sb.append(".  ");
            }
            for (int s = depth1 - 1; s >= 0; s--) {
                sb.append(Util.move2str[CubieCube.urfMove[urf][move[s]]]).append(' ');
            }
        }
        if ((verbose & APPEND_LENGTH) != 0) {
            sb.append("(").append(sol).append("f)");
        }
        return sb.toString();
    }
}
