package cs.min2phase;

class CoordCubeKoc extends CoordCube {

    static final int N_UDSLICEFLIP_SYM = 64430;

    //full phase1
    static int[][] UDSliceFlipMove = new int[N_UDSLICEFLIP_SYM][N_MOVES];
    static char[][] TwistMoveF = new char[N_TWIST][N_MOVES];
    static char[][] TwistConj = new char[N_TWIST][16];
    static int[] UDSliceFlipTwistPrun = new int[N_UDSLICEFLIP_SYM * N_TWIST / 16 + 1];

    static void setPruning2(int[] table, long index, int value) {
        table[(int) (index >> 4)] ^= (0x3 ^ value) << ((index & 0xf) << 1);
    }

    static int getPruning2(int[] table, long index) {
        return table[(int) (index >> 4)] >> ((index & 0xf) << 1) & 0x3;
    }

    static void init(boolean fullInit) {
        if (initLevel == 2) {
            return;
        }
        CoordCube.init(true);

        long tt = System.nanoTime();

        CubieCubeKoc.initUDSliceFlipSym2Raw();
        System.out.println(System.nanoTime() - tt); tt = System.nanoTime();
        initUDSliceFlipMove();
        System.out.println(System.nanoTime() - tt); tt = System.nanoTime();
        initTwistMoveConj();
        System.out.println(System.nanoTime() - tt); tt = System.nanoTime();
        initUDSliceFlipTwistPrun();
        System.out.println(System.nanoTime() - tt); tt = System.nanoTime();
        initLevel = 2;
    }

    private static final int MAXDEPTH = 13;

    static void initUDSliceFlipMove() {
        CubieCubeKoc c = new CubieCubeKoc();
        CubieCubeKoc d = new CubieCubeKoc();
        for (int i = 0; i < N_UDSLICEFLIP_SYM; i++) {
            c.setUDSliceFlip(CubieCubeKoc.UDSliceFlipS2R[i]);
            int udslice = CubieCubeKoc.UDSliceFlipS2R[i] >> 11;
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[j], d);
                int flip = d.getFlipSym();
                int fsym = flip & 0x7;
                flip >>= 3;
                int udsliceflip = CubieCubeKoc.FlipSlice2UDSliceFlip[flip * N_SLICE + UDSliceConj[UDSliceMove[udslice][j] & 0x1ff][fsym]];
                UDSliceFlipMove[i][j] = udsliceflip & ~0xf | CubieCube.SymMult[udsliceflip & 0xf][fsym << 1];
            }
        }
    }

    static void initTwistMoveConj() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_TWIST; i++) {
            c.setTwist(i);
            for (int j = 0; j < N_MOVES; j += 3) {
                CubieCube.CornMult(c, CubieCube.moveCube[j], d);
                TwistMoveF[i][j] = (char) d.getTwist();
            }
            for (int j = 0; j < 16; j++) {
                CubieCube.CornConjugate(c, CubieCube.SymMultInv[0][j], d);
                TwistConj[i][j] = (char) d.getTwist();
            }
        }
        for (int i = 0; i < N_TWIST; i++) {
            for (int j = 0; j < N_MOVES; j += 3) {
                int twist = TwistMoveF[i][j];
                for (int k = 1; k < 3; k++) {
                    twist = TwistMoveF[twist][j];
                    TwistMoveF[i][j + k] = (char) twist;
                }
            }
        }
    }

    // static final int BASE_DEPTH = 8;

    static void initUDSliceFlipTwistPrun() {
        final int N_SIZE = N_TWIST * N_UDSLICEFLIP_SYM;

        for (int i = 0; i < (N_SIZE + 15) / 16; i++) {
            UDSliceFlipTwistPrun[i] = -1;
        }
        setPruning2(UDSliceFlipTwistPrun, 0, 0);

        int depth = 0;
        int done = 1;

        while (done < N_SIZE) {
            boolean inv = depth > 8;
            int select = inv ? 0x3 : depth % 3;
            int check = inv ? depth % 3 : 0x3;
            depth++;
            int depm3 = depth % 3;
            if (depth >= MAXDEPTH) {
                break;
            }
            for (int i = 0; i < N_SIZE;) {
                int val = UDSliceFlipTwistPrun[i >> 4];
                if (!inv && val == -1) {
                    i += 16;
                    continue;
                }
                for (int end = Math.min(i + 16, N_SIZE); i < end; i++, val >>= 2) {
                    if ((val & 0x3) != select) {
                        continue;
                    }
                    int raw = i % N_TWIST;
                    int sym = i / N_TWIST;
                    for (int m = 0; m < N_MOVES; m++) {
                        int symx = UDSliceFlipMove[sym][m];
                        int rawx = TwistConj[TwistMoveF[raw][m]][symx & 0xf];
                        symx >>= 4;
                        int idx = symx * N_TWIST + rawx;
                        if (getPruning2(UDSliceFlipTwistPrun, idx) != check) {
                            continue;
                        }
                        done++;
                        if (inv) {
                            setPruning2(UDSliceFlipTwistPrun, i, depm3);
                            break;
                        }
                        setPruning2(UDSliceFlipTwistPrun, idx, depm3);
                        for (int j = 1, symState = CubieCubeKoc.SymStateUDSliceFlip[symx]; (symState >>= 1) != 0; j++) {
                            if ((symState & 1) != 1) {
                                continue;
                            }
                            int idxx = symx * N_TWIST + TwistConj[rawx][j];
                            if (getPruning2(UDSliceFlipTwistPrun, idxx) == 0x3) {
                                setPruning2(UDSliceFlipTwistPrun, idxx, depm3);
                                done++;
                            }
                        }
                    }
                }
            }
            System.out.println(String.format("%2d%10d", depth, done));
        }
    }

    CoordCubeKoc() { }

    @Override
    void calcPruning(boolean isPhase1) {
        int prunm3 = getPruning2(UDSliceFlipTwistPrun, flip * N_TWIST + TwistConj[twist][fsym]);
        if (prunm3 == 3) {
            prun = MAXDEPTH;
            return;
        }
        int curTwist = TwistConj[twist][fsym];
        int curFlip = flip;
        prun = 0;
        while (curFlip != 0 || curTwist != 0) {
            ++prun;
            prunm3 = (prunm3 + 2) % 3;
            for (int m = 0; m < N_MOVES; m++) {
                int newFlip = UDSliceFlipMove[curFlip][m];
                int newTwist = TwistConj[TwistMoveF[curTwist][m]][newFlip & 0xf];
                newFlip >>= 4;
                int prunm3x = getPruning2(UDSliceFlipTwistPrun, newFlip * N_TWIST + newTwist);
                if (prunm3x == prunm3) {
                    curFlip = newFlip;
                    curTwist = newTwist;
                    break;
                }
            }
        }
    }

    @Override
    boolean setWithPrun(CubieCube cc, int depth) {
        twist = cc.getTwist();
        flip = new CubieCubeKoc(cc).getUDSliceFlipSym();
        fsym = flip & 0xf;
        flip >>= 4;
        calcPruning(true);
        return prun <= depth;
    }

    @Override
    int doMovePrun(CoordCube cc, int m, boolean isPhase1) {
        twist = TwistMoveF[cc.twist][m];
        flip = UDSliceFlipMove[cc.flip][CubieCube.SymMove[cc.fsym][m]];
        fsym = CubieCube.SymMult[flip & 0xf][cc.fsym];
        flip >>= 4;

        int prunm3 = getPruning2(UDSliceFlipTwistPrun, flip * N_TWIST + TwistConj[twist][fsym]);
        if (prunm3 == 3) {
            prun = MAXDEPTH;
        } else {
            prun = ((0x49249249 << prunm3 >> cc.prun) & 3) + cc.prun - 1;
        }
        return prun;
    }

    @Override // do nothing
    int doMovePrunConj(CoordCube cc, int m) {
        return 0;
    }
}