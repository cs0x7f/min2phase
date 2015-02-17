package cs.min2phase;

class CoordCube {
    static final int N_MOVES = 18;
    static final int N_MOVES2 = 10;

    static final int N_SLICE = 495;
    static final int N_TWIST_SYM = 324;
    static final int N_FLIP = 2048;
    static final int N_FLIP_SYM = 336;
    static final int N_PERM_SYM = 2768;
    static final int N_MPERM = 24;

    static final int N_UDSLICEFLIP_SYM = 64430;
    static final int N_TWIST = 2187;

    //XMove = Move Table
    //XPrun = Pruning Table
    //XConj = Conjugate Table

    static int[][] UDSliceFlipMove = Search.USE_FULL_PRUN ? new int[N_UDSLICEFLIP_SYM][N_MOVES] : null;
    static char[][] TwistMoveF = Search.USE_FULL_PRUN ? new char[N_TWIST][N_MOVES] : null;
    static char[][] TwistConj = Search.USE_FULL_PRUN ? new char[N_TWIST][16] : null;
    static int[] UDSliceFlipTwistPrun = Search.USE_FULL_PRUN ? new int[N_UDSLICEFLIP_SYM * N_TWIST / 16 + 1] : null;

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
    static int[] MCPermPrun = new int[N_MPERM * N_PERM_SYM / 8];
    static int[] MEPermPrun = new int[N_MPERM * N_PERM_SYM / 8];

    static void setPruning(int[] table, int index, int value) {
        table[index >> 3] ^= (0xf ^ value) << ((index & 7) << 2);
    }

    static int getPruning(int[] table, int index) {
        return (table[index >> 3] >> ((index & 7) << 2)) & 0xf;
    }

    static void setPruning2(int[] table, int index, int value) {
        table[index >> 4] ^= (0x3 ^ value) << ((index & 0xf) << 1);
    }

    static int getPruning2(int[] table, int index) {
        return (table[index >> 4] >> ((index & 0xf) << 1)) & 0x3;
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
                UDSliceConj[i][j >>> 1] = (char) (d.getUDSlice() & 0x1ff);
            }
        }
        for (int i = 0; i < N_SLICE; i++) {
            for (int j = 0; j < N_MOVES; j += 3) {
                int udslice = UDSliceMove[i][j];
                for (int k = 1; k < 3; k++) {
                    int cx = UDSliceMove[udslice & 0x1ff][j];
                    udslice = Util.permMult[udslice >>> 9][cx >>> 9] << 9 | cx & 0x1ff;
                    UDSliceMove[i][j + k] = (char)(udslice);
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

    static void initUDSliceFlipMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_UDSLICEFLIP_SYM; i++) {
            c.setUDSliceFlip(CubieCube.UDSliceFlipS2R[i]);
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[j], d);
                // UDSliceFlipMove[i][j] = d.getUDSliceFlipSym();

                int flip = d.getFlipSym();
                int fsym = flip & 0x7;
                flip >>= 3;
                int udsliceflip = fs2sf[flip * 495 + UDSliceConj[d.getUDSlice() & 0x1ff][fsym]];
                UDSliceFlipMove[i][j] = udsliceflip & 0xfffffff0 | CubieCube.SymMult[udsliceflip & 0xf][fsym << 1];
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
                CubieCube.CornConjugate(c, CubieCube.SymInv[j], d);
                TwistConj[i][j] = (char) d.getTwist();
            }
        }
        for (int i = 0; i < N_TWIST; i++) {
            for (int j = 0; j < N_MOVES; j += 3) {
                int twist = TwistMoveF[i][j];
                for (int k = 1; k < 3; k++) {
                    twist = TwistMoveF[twist][j];
                    TwistMoveF[i][j + k] = (char)(twist);
                }
            }
        }
    }

    static void initTwistFlipPrun() {
        int depth = 0;
        int done = 1;
        boolean inv;
        int select;
        int check;
        final int N_SIZE = N_FLIP * N_TWIST_SYM;
        for (int i = 0; i < N_SIZE / 8; i++) {
            TwistFlipPrun[i] = -1;
        }
        setPruning(TwistFlipPrun, 0, 0);

        while (done < N_SIZE) {
            inv = depth > 6;
            select = inv ? 0xf : depth;
            check = inv ? depth : 0xf;
            depth++;
            int val = 0;
            for (int i = 0; i < N_SIZE; i++, val >>= 4) {
                if ((i & 7) == 0) {
                    val = TwistFlipPrun[i >> 3];
                    if (!inv && val == -1) {
                        i += 7;
                        continue;
                    }
                }
                if ((val & 0xf) != select) {
                    continue;
                }
                int twist = i >> 11;
                int flip = CubieCube.FlipR2S[i & 0x7ff];
                int fsym = flip & 7;
                flip >>>= 3;
                for (int m = 0; m < N_MOVES; m++) {
                    int twistx = TwistMove[twist][m];
                    int tsymx = twistx & 7;
                    twistx >>>= 3;
                    int flipx = FlipMove[flip][CubieCube.Sym8Move[fsym][m]];
                    int fsymx = CubieCube.Sym8MultInv[CubieCube.Sym8Mult[flipx & 7][fsym]][tsymx];
                    flipx >>>= 3;
                    int idx = twistx << 11 | CubieCube.FlipS2RF[flipx << 3 | fsymx];
                    if (getPruning(TwistFlipPrun, idx) != check) {
                        continue;
                    }
                    done++;
                    if (inv) {
                        setPruning(TwistFlipPrun, i, depth);
                        break;
                    }
                    setPruning(TwistFlipPrun, idx, depth);
                    char sym = CubieCube.SymStateTwist[twistx];
                    if (sym == 1) {
                        continue;
                    }
                    for (int k = 0; k < 8; k++) {
                        if ((sym & (1 << k)) == 0) {
                            continue;
                        }
                        int idxx = twistx << 11 | CubieCube.FlipS2RF[flipx << 3 | CubieCube.Sym8MultInv[fsymx][k]];
                        if (getPruning(TwistFlipPrun, idxx) == 0xf) {
                            setPruning(TwistFlipPrun, idxx, depth);
                            done++;
                        }
                    }
                }
            }
            // System.out.println(String.format("%2d%10d", depth, done));
        }
    }

    static void initRawSymPrun(int[] PrunTable, final int INV_DEPTH,
                               final char[][] RawMove, final char[][] RawConj,
                               final char[][] SymMove, final char[] SymState,
                               final byte[] SymSwitch, final int[] moveMap, final int SYM_SHIFT) {

        final int SYM_MASK = (1 << SYM_SHIFT) - 1;
        final int N_RAW = RawMove.length;
        final int N_SYM = SymMove.length;
        final int N_SIZE = N_RAW * N_SYM;
        final int N_MOVES = RawMove[0].length;

        for (int i = 0; i < (N_RAW * N_SYM + 7) / 8; i++) {
            PrunTable[i] = -1;
        }
        setPruning(PrunTable, 0, 0);

        int depth = 0;
        int done = 1;

        while (done < N_SIZE) {
            boolean inv = depth > INV_DEPTH;
            int select = inv ? 0xf : depth;
            int check = inv ? depth : 0xf;
            depth++;
            int val = 0;
            for (int i = 0; i < N_SIZE; i++, val >>= 4) {
                if ((i & 7) == 0) {
                    val = PrunTable[i >> 3];
                    if (!inv && val == -1) {
                        i += 7;
                        continue;
                    }
                }
                if ((val & 0xf) != select) {
                    continue;
                }
                int raw = i % N_RAW;
                int sym = i / N_RAW;
                for (int m = 0; m < N_MOVES; m++) {
                    int symx = SymMove[sym][moveMap == null ? m : moveMap[m]];
                    int rawx = RawConj[RawMove[raw][m] & 0x1ff][symx & SYM_MASK];
                    symx >>>= SYM_SHIFT;
                    int idx = symx * N_RAW + rawx;
                    if (getPruning(PrunTable, idx) != check) {
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
                        int idxx = symx * N_RAW + RawConj[rawx][j ^ (SymSwitch == null ? 0 : SymSwitch[j])];
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

    static int[] fs2sf = Search.USE_FULL_PRUN ? new int[N_FLIP_SYM * N_SLICE] : null;
    static char[] twist2raw = Search.USE_FULL_PRUN ? new char[N_TWIST_SYM * 8] : null;

    static void flipSlice2UDSliceFlip() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int flip = 0; flip < N_FLIP_SYM; flip++) {
            c.setFlip(CubieCube.FlipS2R[flip]);
            for (int slice = 0; slice < N_SLICE; slice++) {
                c.setUDSlice(slice);
                fs2sf[flip * N_SLICE + slice] = c.getUDSliceFlipSym();
            }
        }
        for (int twist = 0; twist < N_TWIST_SYM; twist++) {
            c.setTwist(CubieCube.TwistS2R[twist]);
            for (int tsym = 0; tsym < 8; tsym++) {
                CubieCube.CornConjugate(c, tsym << 1, d);
                twist2raw[twist << 3 | tsym] = (char) d.getTwist();
            }
        }
    }

    private static final int MAXDEPTH = 16;

    static int getUDSliceFlipSymPrun(int twist, int tsym, int flip, int fsym, int slice) {
        int nextpm3 = getUDSliceFlipSymPrunMod3(twist, tsym, flip, fsym, slice);
        if (nextpm3 == 0x3) {
            return MAXDEPTH;
        }
        int prun = 0;
        while (twist != 0 || flip != 0 || slice != 0) {
            ++prun;
            nextpm3 = (nextpm3 + 2) % 3;
            for (int m = 0; m < 18; m++) {
                int slicex = CoordCube.UDSliceMove[slice][m] & 0x1ff;

                int twistx = CoordCube.TwistMove[twist][CubieCube.Sym8Move[tsym][m]];
                int tsymx = CubieCube.Sym8Mult[twistx & 7][tsym];
                twistx >>>= 3;

                int flipx = CoordCube.FlipMove[flip][CubieCube.Sym8Move[fsym][m]];
                int fsymx = CubieCube.Sym8Mult[flipx & 7][fsym];
                flipx >>>= 3;

                if (nextpm3 == getUDSliceFlipSymPrunMod3(twistx, tsymx, flipx, fsymx, slicex)) {
                    twist = twistx;
                    tsym = tsymx;
                    flip = flipx;
                    fsym = fsymx;
                    slice = slicex;
                    break;
                }
            }
        }
        return prun;
    }

    static int getUDSliceFlipSymPrunMod3(int twist, int tsym, int flip, int fsym, int slice) {
        int udsliceflip = fs2sf[flip * 495 + UDSliceConj[slice][fsym]];
        int twistr = twist2raw[twist << 3 | CubieCube.Sym8MultInv[fsym][tsym]];
        int udsfsym = udsliceflip & 0xf;
        udsliceflip >>= 4;
        return getPruning2(UDSliceFlipTwistPrun, udsliceflip * 2187 + TwistConj[twistr][udsfsym]);
    }

    static int getUDSliceFlipSymPrun(int twist, int tsym, int flip, int fsym, int slice, int prun) {
        int prunm3 = getUDSliceFlipSymPrunMod3(twist, tsym, flip, fsym, slice);
        // prun = (prunm3 - prun + 16) % 3 + prun - 1;
        return prunm3 == 0x3 ? MAXDEPTH : ((0x24924924 >> prun - prunm3 + 2) & 3) + prun - 1;
    }

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
                        symx >>>= 4;
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
                        for (int j = 1, symState = CubieCube.SymStateUDSliceFlip[symx]; (symState >>= 1) != 0; j++) {
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

    static void initSliceTwistPrun() {
        initRawSymPrun(
            UDSliceTwistPrun, 6,
            UDSliceMove, UDSliceConj,
            TwistMove, CubieCube.SymStateTwist,
            null, null, 3
        );
    }

    static void initSliceFlipPrun() {
        initRawSymPrun(
            UDSliceFlipPrun, 6,
            UDSliceMove, UDSliceConj,
            FlipMove, CubieCube.SymStateFlip,
            null, null, 3
        );
    }

    static void initMEPermPrun() {
        initRawSymPrun(
            MEPermPrun, 7,
            MPermMove, MPermConj,
            EPermMove, CubieCube.SymStatePerm,
            null, null, 4
        );
    }

    static void initMCPermPrun() {
        initRawSymPrun(
            MCPermPrun, 10,
            MPermMove, MPermConj,
            CPermMove, CubieCube.SymStatePerm,
            CubieCube.e2c, Util.ud2std, 4
        );
    }
}
