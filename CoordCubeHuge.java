package cs.min2phase;

class CoordCubeHuge extends CoordCube {

    static final int N_UDSLICEFLIP_SYM = 64430;

    static final long N_HUGE = N_UDSLICEFLIP_SYM * N_TWIST * 70L;// 9,863,588,700
    static final int N_FULL_5 = N_UDSLICEFLIP_SYM * N_TWIST / 5;
    static final int N_HUGE_16 = (int) ((N_HUGE + 15) / 16);
    static final int N_HUGE_5 = (int) (N_HUGE / 5);// 1,972,717,740

    //XMove = Move Table
    //XPrun = Pruning Table
    //XConj = Conjugate Table

    //full phase1
    static int[][] UDSliceFlipMove = Search.EXTRA_PRUN_LEVEL > 0 ? new int[N_UDSLICEFLIP_SYM][N_MOVES] : null;
    static char[][] TwistMoveF = Search.EXTRA_PRUN_LEVEL > 0 ? new char[N_TWIST][N_MOVES] : null;
    static char[][] TwistConj = Search.EXTRA_PRUN_LEVEL > 0 ? new char[N_TWIST][16] : null;
    static byte[] UDSliceFlipTwistPrunP = null; //Search.EXTRA_PRUN_LEVEL > 0 ? new byte[N_UDSLICEFLIP_SYM * N_TWIST / 5] : null;
    static byte[] HugePrunP = null; //Search.EXTRA_PRUN_LEVEL > 1 ? new byte[N_HUGE_5] : null;

    static void setPruning2(int[] table, long index, int value) {
        table[(int) (index >> 4)] ^= (0x3 ^ value) << ((index & 0xf) << 1);
    }

    static int getPruning2(int[] table, long index) {
        return table[(int) (index >> 4)] >> ((index & 0xf) << 1) & 0x3;
    }

    static char[] tri2bin = new char[243];

    static {
        for (int i = 0; i < 243; i++) {
            int val = 0;
            int l = i;
            for (int j = 0; j < 5; j++) {
                val |= (l % 3) << (j << 1);
                l /= 3;
            }
            tri2bin[i] = (char) val;
        }
    }

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
        initFlipMove();
        initUDSliceMoveConj();

        CubieCube.initUDSliceFlipSym2Raw();
        initUDSliceFlipMove();
        initTwistMoveConj();
        initUDSliceFlipTwistPrun();
        if (Search.EXTRA_PRUN_LEVEL > 1) {
            initHugePrun();
        }
    }

    static int getPruningP(byte[] table, long index, final long THRESHOLD) {
        if (index < THRESHOLD) {
            return tri2bin[table[(int) (index >> 2)] & 0xff] >> ((index & 3) << 1) & 3;
        } else {
            return tri2bin[table[(int) (index - THRESHOLD)] & 0xff] >> 8 & 3;
        }
    }

    static void packPrunTable(int[] PrunTable, java.nio.ByteBuffer buf, final long PACKED_SIZE) {
        for (long i = 0; i < PACKED_SIZE; i++) {
            int n = 1;
            int value = 0;
            for (int j = 0; j < 4; j++) {
                value += n * getPruning2(PrunTable, i << 2 | j);
                n *= 3;
            }
            value += n * getPruning2(PrunTable, (PACKED_SIZE << 2) + i);
            buf.put((byte) value);
        }
    }

    static boolean loadPrunPTable(byte[] table, String fileName) {
        final int length = table.length;
        try {
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "r");
            java.nio.channels.FileChannel channel = raf.getChannel();
            java.nio.MappedByteBuffer buffer = channel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, length);
            for (int i = 0; i < length; i++) {
                table[i] = buffer.get();
            }
            raf.close();
            return true;
        } catch (java.io.FileNotFoundException e) {
            // e.printStackTrace();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    static void packAndSavePrunPTable(int[] table, String fileName, int FILE_SIZE) {
        try {
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
            raf.setLength(FILE_SIZE);
            java.nio.channels.FileChannel channel = raf.getChannel();
            java.nio.MappedByteBuffer buffer = channel.map(java.nio.channels.FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
            packPrunTable(table, buffer, FILE_SIZE);
            raf.close();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static final int MAXDEPTH = 15;

    static void initUDSliceFlipMove() {
        CubieCube c = new CubieCube();
        CubieCube d = new CubieCube();
        for (int i = 0; i < N_UDSLICEFLIP_SYM; i++) {
            c.setUDSliceFlip(CubieCube.UDSliceFlipS2R[i]);
            int udslice = CubieCube.UDSliceFlipS2R[i] >> 11;
            for (int j = 0; j < N_MOVES; j++) {
                CubieCube.EdgeMult(c, CubieCube.moveCube[j], d);
                // UDSliceFlipMove[i][j] = d.getUDSliceFlipSym();

                int flip = d.getFlipSym();
                int fsym = flip & 0x7;
                flip >>= 3;
                int udsliceflip = CubieCube.FlipSlice2UDSliceFlip[flip * N_SLICE + UDSliceConj[UDSliceMove[udslice][j] & 0x1ff][fsym]];
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
                    TwistMoveF[i][j + k] = (char) twist;
                }
            }
        }
    }

    static void initUDSliceFlipTwistPrun() {
        UDSliceFlipTwistPrunP = new byte[N_FULL_5];
        if (loadPrunPTable(UDSliceFlipTwistPrunP, "FullTable.prunP")) {
            return;
        }
        UDSliceFlipTwistPrunP = null;
        int[] UDSliceFlipTwistPrun = new int[N_UDSLICEFLIP_SYM * N_TWIST / 16 + 1];

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

        packAndSavePrunPTable(UDSliceFlipTwistPrun, "FullTable.prunP", N_FULL_5);
        UDSliceFlipTwistPrun = null;
        UDSliceFlipTwistPrunP = new byte[N_FULL_5];
        if (!loadPrunPTable(UDSliceFlipTwistPrunP, "FullTable.prunP")) {
            System.out.println("Error Loading FullTable.prunP");
            throw new RuntimeException("Error Loading FullTable.prunP");
        }
    }

    static void initHugePrun() {
        HugePrunP = new byte[N_HUGE_5];
        if (loadPrunPTable(HugePrunP, "HugeTable.prunP")) {
            return;
        }
        HugePrunP = null;

        final long N_SIZE = N_HUGE;
        final long N_RAW = N_TWIST * N_COMB;

        int[] HugePrun = new int[N_HUGE_16];

        for (int i = 0; i < N_HUGE_16; i++) {
            HugePrun[i] = -1;
        }
        setPruning2(HugePrun, 0, 0);

        int depth = 0;
        long done = 1;

        while (done < N_SIZE) {
            boolean inv = depth > 9;
            int select = inv ? 0x3 : depth % 3;
            int check = inv ? depth % 3 : 0x3;
            depth++;
            int depm3 = depth % 3;
            for (long i = 0; i < N_SIZE;) {
                int val = HugePrun[(int) (i >> 4)];
                if (!inv && val == -1) {
                    i += 16;
                    continue;
                }
                for (long end = Math.min(i + 16, N_SIZE); i < end; i++, val >>= 2) {
                    if ((val & 0x3) != select) {
                        continue;
                    }
                    int raw = (int) (i % N_RAW);
                    int sym = (int) (i / N_RAW);
                    for (int m = 0; m < N_MOVES; m++) {
                        int symx = UDSliceFlipMove[sym][m];
                        int rawx = TwistConj[TwistMoveF[raw / N_COMB][m]][symx & 0xf] * N_COMB + CCombConj[CCombMove[raw % N_COMB][m]][symx & 0xf];
                        symx >>= 4;
                        long idx = symx * N_RAW + rawx;
                        if (getPruning2(HugePrun, idx) != check) {
                            continue;
                        }
                        done++;
                        if ((done & 0x1fffff) == 0) {
                            System.out.print(done + "\r");
                        }
                        if (inv) {
                            setPruning2(HugePrun, i, depm3);
                            break;
                        }
                        setPruning2(HugePrun, idx, depm3);
                        for (int j = 1, symState = CubieCube.SymStateUDSliceFlip[symx]; (symState >>= 1) != 0; j++) {
                            if ((symState & 1) != 1) {
                                continue;
                            }
                            long idxx = symx * N_RAW + TwistConj[rawx / N_COMB][j] * N_COMB + CCombConj[rawx % N_COMB][j];
                            if (getPruning2(HugePrun, idxx) == 0x3) {
                                setPruning2(HugePrun, idxx, depm3);
                                done++;
                            }
                        }
                    }
                }
            }
            System.out.println(String.format("%2d%12d", depth, done));
        }

        packAndSavePrunPTable(HugePrun, "HugeTable.prunP", N_HUGE_5);
        HugePrun = null;
        HugePrunP = new byte[N_HUGE_5];
        if (!loadPrunPTable(HugePrunP, "HugeTable.prunP")) {
            System.out.println("Error Loading HugeTable.prunP");
            throw new RuntimeException("Error Loading HugeTable.prunP");
        }
    }

    CoordCubeHuge() { }

    @Override
    void calcPruning(boolean isPhase1) {
        int prunm3 = 0;
        if (Search.EXTRA_PRUN_LEVEL > 1 && !isPhase1) {
            prunm3 = getPruningP(HugePrunP, flip * ((long) N_TWIST) * N_COMB + TwistConj[twist][fsym] * N_COMB + CCombConj[tsym][fsym], N_HUGE_5 * 4L);
        } else {
            prunm3 = getPruningP(UDSliceFlipTwistPrunP, flip * N_TWIST + TwistConj[twist][fsym], N_UDSLICEFLIP_SYM * N_TWIST / 5 * 4);
        }
        prun = 0;
        CoordCubeHuge tmp1 = new CoordCubeHuge();
        CoordCubeHuge tmp2 = new CoordCubeHuge();
        tmp1.set(this);
        tmp1.prun = prunm3;
        while (tmp1.twist != 0 || tmp1.flip != 0 || tmp1.tsym != 0 && !isPhase1) {
            ++prun;
            if (tmp1.prun == 0) {
                tmp1.prun = 3;
            }
            for (int m = 0; m < 18; m++) {
                int gap = tmp2.doMovePrun(tmp1, m, isPhase1);
                if (gap < tmp1.prun) {
                    tmp1.set(tmp2);
                    break;
                }
            }
        }
    }

    @Override
    void set(CubieCube cc) {
        twist = cc.getTwist();
        flip = cc.getUDSliceFlipSym();
        slice = cc.getUDSlice();
        fsym = flip & 0xf;
        flip >>= 4;
        if (Search.EXTRA_PRUN_LEVEL > 1) {
            tsym = cc.getCComb(); //tsym -> CComb
        }
    }

    /**
     * @return
     *      0: Success
     *      1: Try Next Power
     *      2: Try Next Axis
     */
    @Override
    int doMovePrun(CoordCube cc, int m, boolean isPhase1) {

        twist = TwistMoveF[cc.twist][m];
        flip = UDSliceFlipMove[cc.flip][CubieCube.SymMove[cc.fsym][m]];
        fsym = CubieCube.SymMult[flip & 0xf][cc.fsym];
        flip >>= 4;

        int prunm3;
        if (Search.EXTRA_PRUN_LEVEL > 1 && !isPhase1) {
            tsym = CCombMove[cc.tsym][m];
            prunm3 = getPruningP(HugePrunP,
                                 flip * ((long) N_TWIST) * N_COMB + TwistConj[twist][fsym] * N_COMB + CCombConj[tsym][fsym], N_HUGE_5 * 4L);
        } else {
            prunm3 = getPruningP(UDSliceFlipTwistPrunP,
                                 flip * N_TWIST + TwistConj[twist][fsym], N_FULL_5 * 4);
        }
        prun = ((0x49249249 << prunm3 >> cc.prun) & 3) + cc.prun - 1;

        return prun;
    }
}
