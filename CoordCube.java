package cs.min2phase;

class CoordCube {
    static final int N_MOVES = 18;
    static final int N_MOVES2 = 10;

    static final int N_SLICE = 495;
    static final int N_TWIST = 2187;
    static final int N_TWIST_SYM = 324;
    static final int N_FLIP = 2048;
    static final int N_FLIP_SYM = 336;
    static final int N_PERM = 40320;
    static final int N_PERM_SYM = 2768;
    static final int N_MPERM = 24;
    static final int N_COMB = 70;

    //XMove = Move Table
    //XPrun = Pruning Table
    //XConj = Conjugate Table

    //phase1
    static char[][] UDSliceMove = new char[N_SLICE][N_MOVES];
    static char[][] TwistMove = new char[N_TWIST_SYM][N_MOVES];
    static char[][] FlipMove = new char[N_FLIP_SYM][N_MOVES];
    static char[][] UDSliceConj = new char[N_SLICE][8];
    static int[] UDSliceTwistPrun = new int[N_SLICE * N_TWIST_SYM / 8 + 1];
    static int[] UDSliceFlipPrun = new int[N_SLICE * N_FLIP_SYM / 8];
    static int[] TwistFlipPrun = Search.USE_TWIST_FLIP_PRUN ? new int[N_FLIP * N_TWIST_SYM / 8] : null;

    //phase2
    static char[][] CPermMove = new char[N_PERM_SYM][N_MOVES];
    static char[][] EPermMove = new char[N_PERM_SYM][N_MOVES2];
    static char[][] MPermMove = new char[N_MPERM][N_MOVES2];
    static char[][] MPermConj = new char[N_MPERM][16];
    static char[][] CCombMove = new char[N_COMB][N_MOVES];
    static char[][] CCombConj = new char[N_COMB][16];
    static int[] MCPermPrun = new int[N_MPERM * N_PERM_SYM / 8];
    static int[] MEPermPrun = new int[N_MPERM * N_PERM_SYM / 8];
    static int[] EPermCCombPrun = new int[N_COMB * N_PERM_SYM / 8];

    static void init() {
        CubieCube.initPermSym2Raw();

        initCPermMove();
        initEPermMove();
        initMPermMoveConj();
        initCombMoveConj();

        initMEPermPrun();
        initMCPermPrun();
        initPermCombPrun();

        CubieCube.initFlipSym2Raw();
        CubieCube.initTwistSym2Raw();
        initFlipMove();
        initTwistMove();
        initUDSliceMoveConj();

        if (Search.USE_TWIST_FLIP_PRUN) {
            initTwistFlipPrun();
        }
        initSliceTwistPrun();
        initSliceFlipPrun();
    }

    static void setPruning(int[] table, int index, int value) {
        table[index >> 3] ^= (0xf ^ value) << ((index & 7) << 2);
    }

    static int getPruning(int[] table, int index) {
        return table[index >> 3] >> ((index & 7) << 2) & 0xf;
    }

    static void initUDSliceMoveConj() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_SLICE; i++) {
            c.setUDSlice(i);
            for (int j = 0; j < N_MOVES; j += 3) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[j], d);
                UDSliceMove[i][j] = (char) d.getUDSlice();
            }
            for (int j = 0; j < 16; j += 2) {
                CubieCube.EdgeConjugate(c, CubieCube.SymInv[j], d);
                UDSliceConj[i][j >> 1] = (char) (d.getUDSlice() & 0x1ff);
            }
        }
        for (int i = 0; i < N_SLICE; i++) {
            for (int j = 0; j < N_MOVES; j += 3) {
                int udslice = UDSliceMove[i][j];
                for (int k = 1; k < 3; k++) {
                    int cx = UDSliceMove[udslice & 0x1ff][j];
                    udslice = Util.permMult[udslice >> 9][cx >> 9] << 9 | cx & 0x1ff;
                    UDSliceMove[i][j + k] = (char) udslice;
                }
            }
        }
    }

    static void initFlipMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_FLIP_SYM; i++) {
            c.setFlip(CubieCube.FlipS2R[i]);
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[j], d);
                FlipMove[i][j] = (char) d.getFlipSym();
            }
        }
    }

    static void initTwistMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_TWIST_SYM; i++) {
            c.setTwist(CubieCube.TwistS2R[i]);
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.CornMult(c, CubieCube.moveCube[j], d);
                TwistMove[i][j] = (char) d.getTwistSym();
            }
        }
    }

    static void initCPermMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_PERM_SYM; i++) {
            c.setCPerm(CubieCube.EPermS2R[i]);
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.CornMult(c, CubieCube.moveCube[j], d);
                CPermMove[i][j] = (char) d.getCPermSym();
            }
        }
    }

    static void initEPermMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_PERM_SYM; i++) {
            c.setEPerm(CubieCube.EPermS2R[i]);
            for (int j = 0; j < N_MOVES2; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[Util.ud2std[j]], d);
                EPermMove[i][j] = (char) d.getEPermSym();
            }
        }
    }

    static void initMPermMoveConj() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_MPERM; i++) {
            c.setMPerm(i);
            for (int j = 0; j < N_MOVES2; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[Util.ud2std[j]], d);
                MPermMove[i][j] = (char) d.getMPerm();
            }
            for (int j = 0; j < 16; j++) {
                CubieCube.EdgeConjugate(c, CubieCube.SymInv[j], d);
                MPermConj[i][j] = (char) d.getMPerm();
            }
        }
    }

    static void initCombMoveConj() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_COMB; i++) {
            c.setCComb(i);
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.CornMult(c, CubieCube.moveCube[j], d);
                CCombMove[i][j] = (char) d.getCComb();
            }
            for (int j = 0; j < 16; j++) {
                CubieCube.CornConjugate(c, CubieCube.SymInv[j], d);
                CCombConj[i][j] = (char) d.getCComb();
            }
        }
    }

    static boolean hasZero(int val) {
        return ((val - 0x11111111) & ~val & 0x88888888) != 0;
    }

    static void initRawSymPrun(int[] PrunTable, final int INV_DEPTH,
                               final char[][] RawMove, final char[][] RawConj,
                               final char[][] SymMove, final char[] SymState,
                               final int PrunFlag) {

        final int SYM_SHIFT = PrunFlag & 0xf;
        final int SYM_E2C_MAGIC = ((PrunFlag >> 4) & 1) == 1 ? CubieCube.SYM_E2C_MAGIC : 0x00000000;
        final boolean MoveMapSym = ((PrunFlag >> 5) & 1) == 1;
        final boolean MoveMapRaw = ((PrunFlag >> 6) & 1) == 1;

        final int SYM_MASK = (1 << SYM_SHIFT) - 1;
        final boolean ISTFP = RawMove == null;
        final int N_RAW = ISTFP ? N_FLIP : RawMove.length;
        final int N_SYM = SymMove.length;
        final int N_SIZE = N_RAW * N_SYM;
        final int N_MOVES = MoveMapRaw ? 10 : ISTFP ? 18 : RawMove[0].length;
        final int NEXT_AXIS_MAGIC = N_MOVES == 10 ? 0x42 : 0x92492;

        for (int i = 0; i < (N_SIZE + 7) / 8; i++) {
            PrunTable[i] = -1;
        }
        setPruning(PrunTable, 0, 0);

        int depth = 0;
        int done = 1;

        while (done < N_SIZE) {
            boolean inv = depth > INV_DEPTH;
            int select = inv ? 0xf : depth;
            int selArrMask = select * 0x11111111;
            int check = inv ? depth : 0xf;
            depth++;
            int val = 0;
            for (int i = 0; i < N_SIZE; i++, val >>= 4) {
                if ((i & 7) == 0) {
                    val = PrunTable[i >> 3];
                    if (!hasZero(val ^ selArrMask)) {
                        i += 7;
                        continue;
                    }
                }
                if ((val & 0xf) != select) {
                    continue;
                }
                int raw = i % N_RAW;
                int sym = i / N_RAW;
                int flip = 0, fsym = 0;
                if (ISTFP) {
                    flip = CubieCube.FlipR2S[raw];
                    fsym = flip & 7;
                    flip >>= 3;
                }

                for (int m = 0; m < N_MOVES; m++) {
                    int symx = SymMove[sym][MoveMapSym ? Util.ud2std[m] : m];
                    int rawx;
                    if (ISTFP) {
                        rawx = CubieCube.FlipS2RF[
                                   FlipMove[flip][CubieCube.Sym8Move[m << 3 | fsym]] ^
                                   fsym ^ (symx & SYM_MASK)];
                    } else {
                        rawx = RawConj[RawMove[raw][MoveMapRaw ? Util.ud2std[m] : m] & 0x1ff][symx & SYM_MASK];

                    }
                    symx >>= SYM_SHIFT;
                    int idx = symx * N_RAW + rawx;
                    int prun = getPruning(PrunTable, idx);
                    if (prun != check) {
                        if (prun < depth - 1) {
                            m += NEXT_AXIS_MAGIC >> m & 3;
                        }
                        continue;
                    }
                    done++;
                    if (inv) {
                        setPruning(PrunTable, i, depth);
                        break;
                    }
                    setPruning(PrunTable, idx, depth);
                    for (int j = 1, symState = SymState[symx]; (symState >>= 1) != 0; j++) {
                        if ((symState & 1) != 1) {
                            continue;
                        }
                        int idxx = symx * N_RAW;
                        if (ISTFP) {
                            idxx += CubieCube.FlipS2RF[CubieCube.FlipR2S[rawx] ^ j];
                        } else {
                            idxx += RawConj[rawx][j ^ (SYM_E2C_MAGIC >> (j << 1) & 3)];
                        }
                        if (getPruning(PrunTable, idxx) == 0xf) {
                            setPruning(PrunTable, idxx, depth);
                            done++;
                        }
                    }
                }
            }
            // System.out.println(String.format("%2d%10d", depth, done));
        }
    }

    static void initTwistFlipPrun() {
        initRawSymPrun(
            TwistFlipPrun, 6,
            null, null,
            TwistMove, CubieCube.SymStateTwist, 0x3
        );
    }

    static void initSliceTwistPrun() {
        initRawSymPrun(
            UDSliceTwistPrun, 6,
            UDSliceMove, UDSliceConj,
            TwistMove, CubieCube.SymStateTwist, 0x3
        );
    }

    static void initSliceFlipPrun() {
        initRawSymPrun(
            UDSliceFlipPrun, 6,
            UDSliceMove, UDSliceConj,
            FlipMove, CubieCube.SymStateFlip, 0x3
        );
    }

    static void initMEPermPrun() {
        initRawSymPrun(
            MEPermPrun, 7,
            MPermMove, MPermConj,
            EPermMove, CubieCube.SymStatePerm, 0x4
        );
    }

    static void initMCPermPrun() {
        initRawSymPrun(
            MCPermPrun, 10,
            MPermMove, MPermConj,
            CPermMove, CubieCube.SymStatePerm, 0x34
        );
    }

    static void initPermCombPrun() {
        initRawSymPrun(
            EPermCCombPrun, 8,
            CCombMove, CCombConj,
            EPermMove, CubieCube.SymStatePerm, 0x44
        );
    }


    int twist;
    int tsym;
    int flip;
    int fsym;
    int slice;
    int prun;

    int twistc;
    int flipc;

    CoordCube() { }

    void set(CoordCube node) {
        this.twist = node.twist;
        this.tsym = node.tsym;
        this.flip = node.flip;
        this.fsym = node.fsym;
        this.slice = node.slice;
        this.prun = node.prun;

        if (Search.USE_CONJ_PRUN) {
            this.twistc = node.twistc;
            this.flipc = node.flipc;
        }
    }

    void calcPruning(boolean isPhase1) {
        prun = Math.max(
                   Math.max(
                       getPruning(UDSliceTwistPrun,
                                  twist * N_SLICE + UDSliceConj[slice & 0x1ff][tsym]),
                       getPruning(UDSliceFlipPrun,
                                  flip * N_SLICE + UDSliceConj[slice & 0x1ff][fsym])),
                   Search.USE_TWIST_FLIP_PRUN ? getPruning(TwistFlipPrun,
                           twist << 11 | CubieCube.FlipS2RF[flip << 3 | (fsym ^ tsym)]) : 0);
    }

    void set(CubieCube cc) {
        twist = cc.getTwistSym();
        flip = cc.getFlipSym();
        slice = cc.getUDSlice();
        tsym = twist & 7;
        twist = twist >> 3;
        fsym = flip & 7;
        flip = flip >> 3;

        if (Search.USE_CONJ_PRUN) {
            CubieCube pc = new CubieCube();
            CubieCube.CornConjugate(cc, 1, pc);
            CubieCube.EdgeConjugate(cc, 1, pc);
            twistc = pc.getTwistSym();
            flipc = pc.getFlipSym();
        }
    }

    /**
     * @return
     *      0: Success
     *      1: Try Next Power
     *      2: Try Next Axis
     */
    int doMovePrun(CoordCube cc, int m, boolean isPhase1) {
        slice = UDSliceMove[cc.slice & 0x1ff][m] & 0x1ff;

        flip = FlipMove[cc.flip][CubieCube.Sym8Move[m << 3 | cc.fsym]];
        fsym = (flip & 7) ^ cc.fsym;
        flip >>= 3;

        twist = TwistMove[cc.twist][CubieCube.Sym8Move[m << 3 | cc.tsym]];
        tsym = (twist & 7) ^ cc.tsym;
        twist >>= 3;

        prun = Math.max(
                   Math.max(
                       getPruning(UDSliceTwistPrun,
                                  twist * N_SLICE + UDSliceConj[slice][tsym]),
                       getPruning(UDSliceFlipPrun,
                                  flip * N_SLICE + UDSliceConj[slice][fsym])),
                   Search.USE_TWIST_FLIP_PRUN ? getPruning(TwistFlipPrun,
                           twist << 11 | CubieCube.FlipS2RF[flip << 3 | (fsym ^ tsym)]) : 0);
        return prun;
    }

    int doMovePrunConj(CoordCube cc, int m) {
        m = CubieCube.SymMove[3][m];
        flipc = FlipMove[cc.flipc >> 3][CubieCube.Sym8Move[m << 3 | cc.flipc & 7]] ^ (cc.flipc & 7);
        twistc = TwistMove[cc.twistc >> 3][CubieCube.Sym8Move[m << 3 | cc.twistc & 7]] ^ (cc.twistc & 7);
        return getPruning(TwistFlipPrun,
                          (twistc >> 3) << 11 | CubieCube.FlipS2RF[flipc ^ (twistc & 7)]);
    }
}
