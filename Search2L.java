/**
    Copyright (C) 2019  Shuang Chen

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

public class Search2L extends Search {
    static int MAX_LENGTH2 = 36;

    static int N_FILTER_MOVES = 5;
    static int FILTER_MASK = (1 << ((N_FILTER_MOVES - 1) * 5)) - 1;
    static long FILTER_MASKL = (1L << (N_FILTER_MOVES * 5)) - 1;


    static java.util.TreeMap<String, Long> set = new java.util.TreeMap<String, Long>();
    static int node_cnt = 0;
    static java.util.Hashtable<Long, Long> allowedMoves = new java.util.Hashtable<Long, Long>();
    static boolean inited = false;
    java.util.HashSet<Long> solvedPhase2States = new java.util.HashSet<Long>();

    static public void init() {
        if (inited) {
            return;
        }
        inited = true;
        Search.init();
        TRY_INVERSE = false;
        TRY_THREE_AXES = true;
        MAX_PRE_MOVES = 0;
        CoordCube2L.init();
        for (int maxl = 0; maxl <= N_FILTER_MOVES; maxl++) {
            initAllowedMoves(new CubieCube2L(new CubieCube(0, 0, 0, 0)), 0, 0, 0, -1, 0, maxl);
            initAllowedMoves(new CubieCube2L(new CubieCube(0, 0, 0, 0)), 1, 1, 0, -1, 0, maxl);
            initAllowedMoves(new CubieCube2L(new CubieCube(0, 0, 0, 0)), 2, 2, 0, -1, 0, maxl);
        }
        System.out.println(set.size() + "\t" + node_cnt + "\t" + allowedMoves.size());
    }

    CubieCube2L[] phase1Cubie2L = new CubieCube2L[80];

    public Search2L() {
        super();
        nodeUD = new CoordCube[80];
        nodeRL = new CoordCube[80];
        nodeFB = new CoordCube[80];
        move = new int[80];
        for (int i = 0; i < 80; i++) {
            nodeUD[i] = new CoordCube2L();
            nodeRL[i] = new CoordCube2L();
            nodeFB[i] = new CoordCube2L();
            phase1Cubie2L[i] = new CubieCube2L();
        }
        for (int i = 0; i < 6; i++) {
            urfCoordCube[i] = new CoordCube2L();
        }
        cc = new CubieCube2L();
    }

    @Override
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
        this.solvedPhase2States.clear();

        Search.init();
        init();
        initSearch();

        selfSym = 0;
        maxLen2 = MAX_LENGTH2;

        return (verbose & OPTIMAL_SOLUTION) == 0 ? search() : searchopt();
    }

    int[] depth2maxl = new int[100];
    int depth2;
    int maxLen2;

    @Override
    protected int initPhase2Pre() {
        isRec = false;
        if (probe >= (solution == null ? probeMax : probeMin)) {
            return 0;
        }
        ++probe;

        long start = System.nanoTime();

        if (valid1 == 0) {
            phase1Cubie2L[0].copy(phase1Cubie[0]);
            phase1Cubie2L[0].setCtIdx(4 >> urfIdx & 3);
            depth2maxl[0] = length1 + 1;
        }
        for (int i = valid1; i <= depth1; i++) {
            int m = move[i];
            int cubeMove = CoordCube2L.mOnCube[m];
            int ctMove = CoordCube2L.mOnCt[m];
            if (cubeMove != -1) {
                phase1Cubie2L[i].doMoveTo(cubeMove, phase1Cubie2L[i + 1]);
            } else if (ctMove != -1) {
                phase1Cubie2L[i].doMoveTo(ctMove + 18, phase1Cubie2L[i + 1]);
            } else {
                phase1Cubie2L[i + 1].copy(phase1Cubie2L[i]);
            }
        }

        valid1 = depth1;

        //check
        // if (phase1Cubie2L[depth1].getTwist() != 0 ||
        //         phase1Cubie2L[depth1].getFlip() != 0 ||
        //         phase1Cubie2L[depth1].getUDSlice() != 0) {
        //     System.out.println("Phase1 Error");
        //     return 1;
        // }

        int eperm = phase1Cubie2L[depth1].getEPerm();
        int cperm = phase1Cubie2L[depth1].getCPerm();
        int mperm = phase1Cubie2L[depth1].getMPerm();
        int ct = phase1Cubie2L[depth1].getCtIdx();
        int leg = nodeUD[depth2maxl[depth1]].tsym;
        if (ct >= 3) {
            int conj = CoordCube2L.ctStdConj[ct];
            eperm = CoordCube2L.EPermConj[eperm][conj];
            cperm = CoordCube2L.CPermConj[cperm][conj];
            mperm = CoordCube2L.MPermConj[mperm][conj];
            ct %= 3;
        }

        long key = (((eperm * 40320L + cperm) * 24L + mperm) * 3L + ct) * 3L + leg;
        if (solvedPhase2States.contains(key)) {
            phase2Duration += System.nanoTime() - start;
            return 1;
        }
        solvedPhase2States.add(key);

        // StringBuffer sbx = new StringBuffer();
        // for (int i = 0; i < depth1; i++) {
        //     sbx.append(CubieCube2L.move2str[move[i]] + "|");
        // }
        // sbx.append(eperm + "," + cperm + "," + mperm + "," + ct + "," + leg);
        // System.out.println(sbx.toString());

        int prun = Math.max(
                       // CoordCube2L.EPermCCombPrun[eperm][CoordCube2L.CPerm2CComb[cperm] * 9 + ct * 3 + leg],
                       CoordCube2L.EPermMPCCbPrun[eperm][(CoordCube2L.CPerm2CComb[cperm] * 24 + mperm) * 9 + ct * 3 + leg],
                       CoordCube2L.CPermMPermPrun[cperm][mperm * 9 + ct * 3 + leg]
                   );
        ;
        long startS = System.nanoTime();
        int length2;
        for (length2 = maxLen2 - 1; length2 >= prun; length2--) {
            // System.out.println(length2);
            int ret = phase2(eperm, cperm, mperm, ct, leg, length2, depth1, 20);
            if (ret < 0) {
                break;
            }
            length2 -= ret;
            sol = length1 + length2;
            estimated = 0;
            // System.out.println(urfIdx + "\t" + depth2 + "\t" + (length1 + length2) + " = " + length1 + "+" + length2);
            StringBuffer sb = new StringBuffer();
            for (int i = 0, leg2 = 0; i < depth2; i++) {
                sb.append(CubieCube2L.move2str[move[i]] + " ");
                estimated += CoordCube2L.moveCost[leg2][move[i]];
                leg2 = CoordCube2L.NextState[leg2][move[i]];
            }
            solution = sb.toString();
        }
        phase2Duration += System.nanoTime() - start;
        phase2SDuration += System.nanoTime() - startS;
        if (length2 != maxLen2 - 1) { //At least one solution has been found.
            maxLen2 = Math.min(MAX_LENGTH2, sol - length1);
            return probe >= probeMin ? 0 : 1;
        } else {
            return 1;
        }
    }

    int depth1 = 0;
    long estimated = 0;
    long phase2Duration = 0;
    long phase2SDuration = 0;

    @Override
    protected int phase1(CoordCube node, int depth, int maxl, int lm) {
        if (depth == 0) {
            node.fsym = 4 >> urfIdx & 3;
        }
        if (node.twist == 0 && node.flip == 0 && node.slice == 0 && maxl < 5) {
            depth1 = depth;
            return maxl == 0 ? 1 : initPhase2Pre();
        }
        int[] nextState = CoordCube2L.NextState[node.tsym];
        int lmMask = CoordCube2L.releasedLegs[lm == -1 ? 20 : (lm & 0x1f)] | CoordCube2L.parallelMoves[lm == -1 ? 20 : (lm & 0x1f)];

        Long val = allowedMoves.get(Long.valueOf(((lm & FILTER_MASK) * 3 + node.tsym)));
        int lmMask2 = val == null ? (lm == -1 ? 0xfffff : 0) : val.intValue();

        for (int m = 0; m < CoordCube2L.N_LEG_MOVES; m++) {
            if (nextState[m] == -1
                    || (lmMask2 >> m & 1) == 0
                    || (CoordCube2L.releasedLegs[m] & lmMask) != 0
                    || (CoordCube2L.parallelMoves[m] & lmMask) != 0 && (m > (lm == -1 ? 20 : (lm & 0x1f)))) {
                continue;
            }

            if (isRec && m != move[depth1 - maxl]) {
                continue;
            }
            int prun = nodeUD[maxl].doMovePrun(node, m, true);
            int maxl_ = maxl - CoordCube2L.mCost[node.tsym][m];
            if (prun > maxl_) {
                continue;
            }
            move[depth] = m;
            depth2maxl[depth + 1] = maxl;
            valid1 = Math.min(valid1, depth);
            // int ret = phase1(nodeUD[maxl], depth + 1, maxl_, m);
            int ret = phase1(nodeUD[maxl], depth + 1, maxl_, ((lm + 1) << 5 | m) & FILTER_MASK);
            if (ret == 0) {
                return 0;
            }
        }
        return 1;
    }

    @Override
    protected int phase2(int eperm, int cperm, int mperm, int ct, int leg, int maxl, int depth, int lm) {
        if (eperm == 0 && cperm == 0 && mperm == 0) {
            depth2 = depth;
            return maxl;
        }
        int[] nextState = CoordCube2L.NextState[leg];
        int lmMask = CoordCube2L.releasedLegs[lm == -1 ? 20 : lm] | CoordCube2L.parallelMoves[lm == -1 ? 20 : lm];

        for (int m = 0; m < CoordCube2L.N_LEG_MOVES; m++) {
            if (nextState[m] == -1
                    || (CoordCube2L.releasedLegs[m] & lmMask) != 0
                    || (CoordCube2L.parallelMoves[m] & lmMask) != 0 && (m > lm)) {
                continue;
            }
            if (isRec && m != move[depth1 - maxl]) {
                continue;
            }
            int legx = nextState[m];
            int epermx = eperm;
            int cpermx = cperm;
            int mpermx = mperm;
            int ctx = ct;

            var cubeMove = CoordCube2L.mOnCube[m];
            if (cubeMove != -1) {
                cubeMove = CoordCube2L.MoveConj[ct][cubeMove];
                cubeMove = Util.std2ud[cubeMove];
                if (cubeMove >= 10) {
                    continue;
                }
                epermx = CoordCube2L.EPermMove[epermx][cubeMove];
                cpermx = CoordCube2L.CPermMove[cpermx][cubeMove];
                mpermx = CoordCube2L.MPermMove[mpermx][cubeMove];
            }
            int ctMove = CoordCube2L.mOnCt[m];
            if (ctMove != -1) {
                ctx = CoordCube2L.CtMove[ct][ctMove];
            }
            if (ctx >= 3) {
                int conj = CoordCube2L.ctStdConj[ctx];
                epermx = CoordCube2L.EPermConj[epermx][conj];
                cpermx = CoordCube2L.CPermConj[cpermx][conj];
                mpermx = CoordCube2L.MPermConj[mpermx][conj];
                ctx %= 3;
            }

            int prun = Math.max(
                           // CoordCube2L.EPermCCombPrun[epermx][CoordCube2L.CPerm2CComb[cpermx] * 9 + ctx * 3 + legx],
                           CoordCube2L.EPermMPCCbPrun[epermx][(CoordCube2L.CPerm2CComb[cpermx] * 24 + mpermx) * 9 + ctx * 3 + legx],
                           CoordCube2L.CPermMPermPrun[cpermx][mpermx * 9 + ctx * 3 + legx]
                       );
            int maxl_ = maxl - CoordCube2L.mCost[leg][m];
            if (prun > maxl_) {
                continue;
            }
            int ret = phase2(epermx, cpermx, mpermx, ctx, legx, maxl_, depth + 1, m);
            if (ret >= 0) {
                move[depth] = m;
                return ret;
            }
        }
        return -1;
    }

    static String lm2str(int lm, int leg) {
        StringBuffer sb = new StringBuffer();
        lm++;
        while (lm != 0) {
            sb.insert(0, (lm & 0x1f) - 1 + "\t");
            lm >>= 5;
        }
        return (sb.toString() + leg);
    }

    static void initAllowedMoves(CubieCube2L cc, int leg, int leg0, int prevLeg, long lm, int depth, int maxl) {

        if (maxl == 0) {
            String key = cc.toString().replace("\n", "") + "|" + leg0 + "|" + leg + "|" + cc.ct + "|";
            if (!set.containsKey(key)) {
                set.put(key, lm * 3 + leg);

                Long key2 = Long.valueOf(lm == -1 ? -1 : (((lm >> 5) - 1) * 3 + prevLeg));
                Long val = allowedMoves.get(key2);
                int bitmap = val == null ? 0 : val.intValue();
                allowedMoves.put(key2, Long.valueOf(bitmap | (1 << (lm & 0x1f))));

            }
            node_cnt++;
            return;
        }
        int[] nextState = CoordCube2L.NextState[leg];
        int lmMask = CoordCube2L.releasedLegs[(int) (lm == -1 ? 20 : (lm & 0x1f))] | CoordCube2L.parallelMoves[(int) (lm == -1 ? 20 : (lm & 0x1f))];

        for (int m = 0; m < CoordCube2L.N_LEG_MOVES; m++) {
            if (nextState[m] == -1
                    || (CoordCube2L.releasedLegs[m] & lmMask) != 0
                    || (CoordCube2L.parallelMoves[m] & lmMask) != 0 && (m > (lm == -1 ? 20 : (lm & 0x1f)))
               ) {
                continue;
            }
            CubieCube2L cc2 = new CubieCube2L();
            if (CoordCube2L.mOnCt[m] != -1) {
                cc.doMoveTo(CoordCube2L.mOnCt[m] + 18, cc2);
            }
            if (CoordCube2L.mOnCube[m] != -1) {
                cc.doMoveTo(CoordCube2L.mOnCube[m], cc2);
            }
            initAllowedMoves(cc2, nextState[m], leg0, leg, ((lm + 1) << 5 | m) & FILTER_MASKL, depth + 1, maxl - 1);
        }
    }

    public static void main(String[] args) {
        Search2L.init();

        Search2L search = new Search2L();
        Search search0 = new Search();
        long totalLength = 0;
        long totalDuration = 0;
        long totalPhase2 = 0;
        long totalPhase2S = 0;
        long totalProbe = 0;
        long totalEstimated = 0;
        Tools.setRandomSource(new java.util.Random(42L));
        for (int i = 0; i < 100; i++) {
            String scramble = Tools.randomCube(); //"R' U R2 B2 F2 U' B2 D' F2 R2 U L F U' L2 F L F' D2 F";
            String generator = search0.solution(scramble, 200, 10000000, 100, INVERSE_SOLUTION);
            System.out.println(generator);

            long start = System.nanoTime();
            String solution = search.solution(scramble, 70, 10000000, 500, 0);
            totalDuration += System.nanoTime() - start;
            totalPhase2 += search.phase2Duration;
            totalPhase2S += search.phase2SDuration;
            totalLength += search.sol;
            totalProbe += search.probe;
            totalEstimated += search.estimated;

            System.out.println(solution);
            System.out.println(String.format("%.1f\t%.1f\t%.1f\t%.1f\t%.1f\t%.2f",
                                             totalDuration / 1000000.0 / (i + 1),
                                             totalPhase2 / 1000000.0 / (i + 1),
                                             totalPhase2S / 1000000.0 / (i + 1),
                                             totalProbe / 1.0 / ( i + 1),
                                             totalEstimated / 1.0 / ( i + 1),
                                             totalLength / 1.0 / (i + 1)));
            search.phase2Duration = 0;
            search.phase2SDuration = 0;
        }
    }
}
