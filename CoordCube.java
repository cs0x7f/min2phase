package cs.min2phase;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;

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
    static final int N_UDSLICEFLIP_SYM = 64430;

    static final long N_HUGE = N_UDSLICEFLIP_SYM * N_TWIST * 70L;// 9,863,588,700
    static final int N_FULL_5 = N_UDSLICEFLIP_SYM * N_TWIST / 5;
    static final int N_HUGE_16 = (int) ((N_HUGE + 15) / 16);
    static final int N_HUGE_5 = (int) (N_HUGE / 5);// 1,972,717,740

    //XMove = Move Table
    //XPrun = Pruning Table
    //XConj = Conjugate Table

    //full phase1
    static int[][] UDSliceFlipMove = Search.USE_FULL_PRUN ? new int[N_UDSLICEFLIP_SYM][N_MOVES] : null;
    static char[][] TwistMoveF = Search.USE_FULL_PRUN ? new char[N_TWIST][N_MOVES] : null;
    static char[][] TwistConj = Search.USE_FULL_PRUN ? new char[N_TWIST][16] : null;
    static byte[] UDSliceFlipTwistPrunP = null; //Search.USE_FULL_PRUN ? new byte[N_UDSLICEFLIP_SYM * N_TWIST / 5] : null;
    static byte[] HugePrunP = null; //Search.USE_HUGE_PRUN ? new byte[N_HUGE_5] : null;

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

    static void setPruning(int[] table, int index, int value) {
        table[index >> 3] ^= (0xf ^ value) << ((index & 7) << 2);
    }

    static int getPruning(int[] table, int index) {
        return table[index >> 3] >> ((index & 7) << 2) & 0xf;
    }

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

    static int getPruningP(byte[] table, long index, final long THRESHOLD) {
        if (index < THRESHOLD) {
            return tri2bin[table[(int) (index >> 2)] & 0xff] >> ((index & 3) << 1) & 3;
        } else {
            return tri2bin[table[(int) (index - THRESHOLD)] & 0xff] >> 8 & 3;
        }
    }

    static void packPrunTable(int[] PrunTable, ByteBuffer buf, final long PACKED_SIZE) {
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
                flip >>= 3;
                for (int m = 0; m < N_MOVES; m++) {
                    int twistx = TwistMove[twist][m];
                    int tsymx = twistx & 7;
                    twistx >>= 3;
                    int flipx = FlipMove[flip][CubieCube.Sym8Move[m << 3 | fsym]];
                    int fsymx = CubieCube.Sym8MultInv[CubieCube.Sym8Mult[flipx & 7 | fsym << 3] << 3 | tsymx];
                    flipx >>= 3;
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
                        if ((sym & 1 << k) == 0) {
                            continue;
                        }
                        int idxx = twistx << 11 | CubieCube.FlipS2RF[flipx << 3 | CubieCube.Sym8MultInv[fsymx << 3 | k]];
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
                               final int PrunFlag) {

        final int SYM_SHIFT = PrunFlag & 0xf;
        final boolean SymSwitch = ((PrunFlag >> 4) & 1) == 1;
        final boolean MoveMapSym = ((PrunFlag >> 5) & 1) == 1;
        final boolean MoveMapRaw = ((PrunFlag >> 6) & 1) == 1;

        final int SYM_MASK = (1 << SYM_SHIFT) - 1;
        final int N_RAW = RawMove.length;
        final int N_SYM = SymMove.length;
        final int N_SIZE = N_RAW * N_SYM;
        final int N_MOVES = MoveMapRaw ? 10 : RawMove[0].length;

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
                    int symx = SymMove[sym][MoveMapSym ? Util.ud2std[m] : m];
                    int rawx = RawConj[RawMove[raw][MoveMapRaw ? Util.ud2std[m] : m] & 0x1ff][symx & SYM_MASK];
                    symx >>= SYM_SHIFT;
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
                        int idxx = symx * N_RAW + RawConj[rawx][j ^ (SymSwitch ? CubieCube.e2c[j] : 0)];
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

    static boolean loadPrunPTable(byte[] table, String fileName) {
        final int length = table.length;
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "r");
            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
            for (int i = 0; i < length; i++) {
                table[i] = buffer.get();
            }
            raf.close();
            return true;
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    static void packAndSavePrunPTable(int[] table, String fileName, int FILE_SIZE) {
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            raf.setLength(FILE_SIZE);
            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
            packPrunTable(table, buffer, FILE_SIZE);
            raf.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static final int MAXDEPTH = 15;

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

    CoordCube() { }

    CoordCube(CoordCube cc) {
        set(cc);
    }

    void set(CoordCube node) {
        this.twist = node.twist;
        this.tsym = node.tsym;
        this.flip = node.flip;
        this.fsym = node.fsym;
        this.slice = node.slice;
        this.prun = node.prun;
    }

    int getPackedPruning(boolean isPhase1) {
        int prunm3 = 0;
        if (Search.USE_HUGE_PRUN && !isPhase1) {
            prunm3 = getPruningP(HugePrunP, flip * ((long) N_TWIST) * N_COMB + TwistConj[twist][fsym] * N_COMB + CCombConj[tsym][fsym], N_HUGE_5 * 4L);
        } else {
            prunm3 = getPruningP(UDSliceFlipTwistPrunP, flip * N_TWIST + TwistConj[twist][fsym], N_UDSLICEFLIP_SYM * N_TWIST / 5 * 4);
        }
        prun = 0;
        CoordCube tmp1 = new CoordCube();
        CoordCube tmp2 = new CoordCube();
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
        return prun;
    }

    void calcPruning(boolean isPhase1) {
        if (Search.USE_FULL_PRUN || Search.USE_HUGE_PRUN) {
            getPackedPruning(isPhase1);
        } else {
            prun = Math.max(
                       Math.max(
                           getPruning(UDSliceTwistPrun,
                                      twist * N_SLICE + UDSliceConj[slice & 0x1ff][tsym]),
                           getPruning(UDSliceFlipPrun,
                                      flip * N_SLICE + UDSliceConj[slice & 0x1ff][fsym])),
                       Search.USE_TWIST_FLIP_PRUN ? getPruning(TwistFlipPrun,
                               twist << 11 | CubieCube.FlipS2RF[flip << 3 | CubieCube.Sym8MultInv[fsym << 3 | tsym]]) : 0);
        }
    }

    void set(CubieCube cc) {
        if (Search.USE_FULL_PRUN || Search.USE_HUGE_PRUN) {
            twist = cc.getTwist();
            flip = cc.getUDSliceFlipSym();
            slice = cc.getUDSlice();
            fsym = flip & 0xf;
            flip >>= 4;
            if (Search.USE_HUGE_PRUN) {
                tsym = cc.getCComb(); //tsym -> CComb
            }
        } else {
            twist = cc.getTwistSym();
            flip = cc.getFlipSym();
            slice = cc.getUDSlice();
            tsym = twist & 7;
            twist = twist >> 3;
            fsym = flip & 7;
            flip = flip >> 3;
        }
    }

    /**
     * @return
     *      0: Success
     *      1: Try Next Power
     *      2: Try Next Axis
     */
    int doMovePrun(CoordCube cc, int m, boolean isPhase1) {
        if (Search.USE_FULL_PRUN) {
            twist = TwistMoveF[cc.twist][m];
            flip = UDSliceFlipMove[cc.flip][CubieCube.SymMove[cc.fsym][m]];
            fsym = CubieCube.SymMult[flip & 0xf][cc.fsym];
            flip >>= 4;

            int prunm3;
            if (Search.USE_HUGE_PRUN && !isPhase1) {
                tsym = CCombMove[cc.tsym][m];
                prunm3 = getPruningP(HugePrunP,
                                     flip * ((long) N_TWIST) * N_COMB + TwistConj[twist][fsym] * N_COMB + CCombConj[tsym][fsym], N_HUGE_5 * 4L);
            } else {
                prunm3 = getPruningP(UDSliceFlipTwistPrunP,
                                     flip * N_TWIST + TwistConj[twist][fsym], N_FULL_5 * 4);
            }
            prun = ((0x49249249 << prunm3 >> cc.prun) & 3) + cc.prun - 1;
        } else {
            slice = UDSliceMove[cc.slice & 0x1ff][m] & 0x1ff;

            flip = FlipMove[cc.flip][CubieCube.Sym8Move[m << 3 | cc.fsym]];
            fsym = CubieCube.Sym8Mult[flip & 7 | cc.fsym << 3];
            flip >>= 3;

            twist = TwistMove[cc.twist][CubieCube.Sym8Move[m << 3 | cc.tsym]];
            tsym = CubieCube.Sym8Mult[twist & 7 | cc.tsym << 3];
            twist >>= 3;

            prun = Math.max(
                       Math.max(
                           getPruning(UDSliceTwistPrun,
                                      twist * N_SLICE + UDSliceConj[slice][tsym]),
                           getPruning(UDSliceFlipPrun,
                                      flip * N_SLICE + UDSliceConj[slice][fsym])),
                       Search.USE_TWIST_FLIP_PRUN ? getPruning(TwistFlipPrun,
                               twist << 11 | CubieCube.FlipS2RF[flip << 3 | CubieCube.Sym8MultInv[fsym << 3 | tsym]]) : 0);
        }
        return prun;
    }
}
