package cs.min2phase;

class CoordCube2L extends CoordCube {
    static int N_CUBE_MOVE = 18;
    static int N_CUBE_MOVE2 = 10;
    static int N_EFF_MOVE = 6;

    static int N_INIT_THREAD = 6;

    static int[][] MoveConj = new int[24][N_EFF_MOVE];
    static int[][] CtMove = new int[24][N_EFF_MOVE];

    static int[][] FlipMove = new int[2048][N_CUBE_MOVE];
    static int[][] TwistMove = new int[2187][N_CUBE_MOVE];
    static int[][] UDSliceMove = new int[495][N_CUBE_MOVE];
    static int[][] FlipConj = new int[2048][16];
    static int[][] TwistConj = new int[2187][16];
    static int[][] UDSliceConj = new int[495][16];
    static int[][] SliceF4Move = new int[495 * 16][N_CUBE_MOVE];
    static int[][] SliceF4Conj = new int[495 * 16][16];
    static int[] FlipUDS2SliceF4 = new int[495 * 2048];
    static int[][] SliceF8Move = new int[495 * 256][N_CUBE_MOVE];
    static int[][] SliceF8Conj = new int[495 * 256][16];
    static int[] FlipUDS2SliceF8 = new int[495 * 2048];
    static int[] FlipConjXor = new int[495];
    static int[][] FlipUDSMove = new int[495 * 2048][N_CUBE_MOVE];
    static int[][] FlipUDSConj = new int[495 * 2048][16];

    static byte[][] FlipUDSPrun;
    static byte[][] TwistUDSlicePrun;
    static byte[][] TwistSliceF4Prun;
    static byte[][] TwistSliceF8Prun;
    static byte[][] TwistFlipUDSPrun;

    static int[][] EPermMove = new int[40320][N_CUBE_MOVE2];
    static int[][] CPermMove = new int[40320][N_CUBE_MOVE2];
    static int[][] MPermMove = new int[24][N_CUBE_MOVE2];
    static int[][] CCombMove = new int[70][N_CUBE_MOVE2];
    static int[][] MPCCbMove = new int[1680][N_CUBE_MOVE2];
    static int[] CPerm2CComb = new int[40320];
    static int[][] EPermConj = new int[40320][16];
    static int[][] CPermConj = new int[40320][16];
    static int[][] MPermConj = new int[24][16];
    static int[][] CCombConj = new int[70][16];
    static int[][] MPCCbConj = new int[1680][16];

    static int[][] EmptyMove = new int[][] {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    static int[][] EmptyConj = new int[][] {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};


    static byte[][] EPermCCombPrun;
    static byte[][] CPermMPermPrun;
    static byte[][] EPermMPCCbPrun;

    static int N_LEG_MOVES = 20;
    static int[] mOnCube = new int[] {0, 1, 2, 3, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    static int[] mOnCt = new int[] { -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, -1, -1, 0, 1, 2, 3, 4, 5};
    static int[][] NextState = new int[][] {
        {  1,  0,  1,  2,  0,  2,  1,  0,  1,  2,  0,  2,  2,  1, -1,  2, -1, -1,  1, -1},
        {  0,  1,  0, -1, -1, -1,  0,  1,  0, -1,  1, -1, -1,  0,  2, -1,  2,  2,  0,  2},
        { -1, -1, -1,  0,  2,  0, -1,  2, -1,  0,  2,  0,  0, -1,  1,  0,  1,  1, -1,  1}
    };
    static int[][] mCost = new int[][] { //机械步骤
        {1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4},
        {1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 5, 3, 3, 3, 4, 4, 4, 4, 4, 4},
        {1, 1, 1, 1, 1, 1, 3, 5, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4}
    };
    // static int[][] mCost = new int[][] {    //操作时间归一化
    //     {1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 2, 2, 3, 4, 3, 3, 4, 3},
    //     {1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 5, 3, 2, 2, 3, 4, 3, 3, 4, 3},
    //     {1, 1, 1, 1, 1, 1, 3, 5, 3, 3, 3, 3, 2, 2, 3, 4, 3, 3, 4, 3}
    // };
    // static int[][] mCost = new int[][] {    //步数
    //     {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
    //     {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
    //     {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    // };
    static int[][] moveCost = new int[][] {
        {51, 87, 51, 51, 87, 51, 211, 284, 211, 211, 284, 211, 138, 138, 249, 322, 249, 249, 322, 249},
        {51, 87, 51, 51, 87, 51, 211, 284, 211, 211, 360, 211, 138, 138, 249, 322, 249, 249, 322, 249},
        {51, 87, 51, 51, 87, 51, 211, 360, 211, 211, 284, 211, 138, 138, 249, 322, 249, 249, 322, 249}
    };

// 10|14 <=> 10|16
// 11|15 <=> 09|15
// 07|11 <=> 07|09
// 07|19 <=> 07|17
// 08|10 <=> 06|10
// 08|18 <=> 06|18

    static int[] releasedLegs = {
        0x0004, // z1z0 U   0
        0x0004, // z2z0 U2  1
        0x0004, // z3z0 U'  2
        0x0008, // z0z1 R   3
        0x0008, // z0z2 R2  4
        0x0008, // z0z3 R'  5
        0x0001, // z1s0 y   6
        0x00c1, // z2s0 y2  7
        0x0301, // z3s0 y'  8
        0x0002, // s0z1 x   9
        0x0112, // s0z2 x2  10
        0x0062, // s0z3 x'  11
        0x0001, // z0s1 --  12
        0x0002, // s1z0 --  13
        0x0011, // z1s1 y   14
        0x0021, // z2s1 y2  15
        0x0001, // z3s1 y'  16
        0x0002, // s1z1 x   17
        0x0202, // s1z2 x2  18
        0x0082, // s1z3 x'  19
        0x0000 //
    };

    static int[] parallelMoves = {
        0x010000, // z1z0 U   0
        0x010000, // z2z0 U2  1
        0x010000, // z3z0 U'  2
        0x020000, // z0z1 R   3
        0x020000, // z0z2 R2  4
        0x020000, // z0z3 R'  5
        0x010000, // z1s0 y   6
        0x310000, // z2s0 y2  7
        0x010000, // z3s0 y'  8
        0x020000, // s0z1 x   9
        0x320000, // s0z2 x2  10
        0x020000, // s0z3 x'  11
        0x020000, // z0s1 --  12
        0x010000, // s1z0 --  13
        0x000000, // z1s1 y   14
        0x100000, // z2s1 y2  15
        0x000000, // z3s1 y'  16
        0x000000, // s1z1 x   17
        0x200000, // s1z2 x2  18
        0x000000, // s1z3 x'  19
        0x000000 //
    };

    static int[] ctStdConj = {
        0, 0, 0, 3, 3, 3, 2, 2, 2, 1, 1, 1, 10, 10, 10, 11, 11, 11, 8, 8, 8, 9, 9, 9
    };

    static void initCenterMove() {
        CubieCube2L cc = new CubieCube2L();
        CubieCube2L cc2 = new CubieCube2L();
        for (int i = 0; i < 24; i++) {
            cc.setCtIdx(i);
            for (int m = 0; m < N_EFF_MOVE; m++) {
                cc.doMoveTo(m + 18, cc2);
                CtMove[i][m] = cc2.getCtIdx();
            }
        }

        for (int idx = 0; idx < 24; idx++) {
            int ct = CubieCube2L.ctIdx2Val[idx];
            for (int m = 0; m < 6; m++) {
                int axis = (m / 3);
                int pow = m % 3;
                axis = ct >> (axis << 2) & 0xf;
                MoveConj[idx][m] = axis * 3 + pow;
            }
        }
    }

    static void initPhase1Move() {
        CubieCube2L cc = new CubieCube2L();
        CubieCube2L cc2 = new CubieCube2L();
        for (int i = 0; i < 2048; i++) {
            cc.setFlip(i);
            for (int m = 0; m < N_CUBE_MOVE; m++) {
                cc.doMoveTo(m, cc2);
                FlipMove[i][m] = cc2.getFlip();
            }
        }

        for (int i = 0; i < 2187; i++) {
            cc.setTwist(i);
            for (int m = 0; m < N_CUBE_MOVE; m++) {
                cc.doMoveTo(m, cc2);
                TwistMove[i][m] = cc2.getTwist();
            }
        }

        for (int i = 0; i < 495; i++) {
            cc.setUDSlice(i);
            for (int m = 0; m < N_CUBE_MOVE; m++) {
                cc.doMoveTo(m, cc2);
                UDSliceMove[i][m] = cc2.getUDSlice();
            }
        }

        cc.setUDSlice(0);
        for (var i = 0; i < 2048; i++) {
            cc.setFlip(i);
            for (var j = 0; j < 16; j++) {
                CubieCube.EdgeConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                FlipConj[i][j] = cc2.getFlip();
            }
        }

        for (var i = 0; i < 2187; i++) {
            cc.setTwist(i);
            for (var j = 0; j < 16; j++) {
                CubieCube.CornConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                TwistConj[i][j] = cc2.getTwist();
            }
        }

        for (var i = 0; i < 495; i++) {
            cc.setUDSlice(i);
            for (var j = 0; j < 16; j++) {
                CubieCube.EdgeConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                UDSliceConj[i][j] = cc2.getUDSlice();
            }
        }

        for (int i = 0; i < 495; i++) {
            cc.setUDSlice(i);
            cc.setFlip(0);
            for (int j = 0; j < 12; j++) {
                if ((cc.ea[j] >> 1) >= 8 ) {
                    cc.ea[j] |= 1;
                }
            }
            FlipConjXor[i] = cc.getFlip() ^ 7;
        }

        for (int i = 0; i < 495; i++) {
            for (int j = 0; j < 2048; j++) {
                for (int m = 0; m < N_CUBE_MOVE; m++) {
                    FlipUDSMove[i << 11 | j][m] = UDSliceMove[i][m] << 11 | FlipMove[j][m];
                }
                for (int k = 0; k < 16; k++) {
                    FlipUDSConj[i << 11 | j][k] = UDSliceConj[i][k] << 11 | FlipConj[j][k] ^ (k % 2 == 1 ? FlipConjXor[UDSliceConj[i][k]] : 0);
                }
            }
        }

        for (int i = 0; i < 495; i++) {
            for (int j = 0; j < 2048; j++) {
                cc.setUDSlice(i);
                cc.setFlip(j);
                FlipUDS2SliceF4[i << 11 | j] = cc.getSliceF4();
            }
        }

        for (int i = 0; i < 7920; i++) {
            cc.setSliceF4(i);
            for (int m = 0; m < N_CUBE_MOVE; m++) {
                cc.doMoveTo(m, cc2);
                SliceF4Move[i][m] = cc2.getSliceF4();
            }
        }

        for (var i = 0; i < 7920; i++) {
            cc.setSliceF4(i);
            for (var j = 0; j < 16; j++) {
                CubieCube.EdgeConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                SliceF4Conj[i][j] = cc2.getSliceF4();
            }
        }


        for (int i = 0; i < 495; i++) {
            for (int j = 0; j < 2048; j++) {
                cc.setUDSlice(i);
                cc.setFlip(j);
                FlipUDS2SliceF8[i << 11 | j] = cc.getSliceF8();
            }
        }

        for (int i = 0; i < 126720; i++) {
            cc.setSliceF8(i);
            for (int m = 0; m < N_CUBE_MOVE; m++) {
                cc.doMoveTo(m, cc2);
                SliceF8Move[i][m] = cc2.getSliceF8();
            }
        }

        for (var i = 0; i < 126720; i++) {
            cc.setSliceF8(i);
            for (var j = 0; j < 16; j++) {
                CubieCube.EdgeConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                SliceF8Conj[i][j] = cc2.getSliceF8();
            }
        }
    }

    static void initPhase2Move() {
        CubieCube2L cc = new CubieCube2L();
        CubieCube2L cc2 = new CubieCube2L();
        for (int i = 0; i < 40320; i++) {
            cc.setEPerm(i);
            for (int m = 0; m < N_CUBE_MOVE2; m++) {
                cc.doMoveTo(Util.ud2std[m], cc2);
                EPermMove[i][m] = cc2.getEPerm();
            }
        }

        for (int i = 0; i < 40320; i++) {
            cc.setCPerm(i);
            CPerm2CComb[i] = cc.getCComb();
            for (int m = 0; m < N_CUBE_MOVE2; m++) {
                cc.doMoveTo(Util.ud2std[m], cc2);
                CPermMove[i][m] = cc2.getCPerm();
            }
        }

        for (int i = 0; i < 24; i++) {
            cc.setMPerm(i);
            for (int m = 0; m < N_CUBE_MOVE2; m++) {
                cc.doMoveTo(Util.ud2std[m], cc2);
                MPermMove[i][m] = cc2.getMPerm();
            }
        }

        for (int i = 0; i < 70; i++) {
            cc.setCComb(i);
            for (int m = 0; m < N_CUBE_MOVE2; m++) {
                cc.doMoveTo(Util.ud2std[m], cc2);
                CCombMove[i][m] = cc2.getCComb();
            }
        }

        for (int i = 0; i < 40320; i++) {
            cc.setEPerm(i);
            for (int j = 0; j < 16; j++) {
                CubieCube.EdgeConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                EPermConj[i][j] = cc2.getEPerm();
            }
        }

        for (int i = 0; i < 40320; i++) {
            cc.setCPerm(i);
            for (int j = 0; j < 16; j++) {
                CubieCube.CornConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                CPermConj[i][j] = cc2.getCPerm();
            }
        }

        for (int i = 0; i < 24; i++) {
            cc.setMPerm(i);
            for (int j = 0; j < 16; j++) {
                CubieCube.EdgeConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                MPermConj[i][j] = cc2.getMPerm();
            }
        }

        for (int i = 0; i < 70; i++) {
            cc.setCComb(i);
            for (int j = 0; j < 16; j++) {
                CubieCube.CornConjugate(cc, CubieCube.SymMultInv[0][j], cc2);
                CCombConj[i][j] = cc2.getCComb();
            }
        }

        for (int i = 0; i < 1680; i++) {
            for (int j = 0; j < N_CUBE_MOVE2; j++) {
                MPCCbMove[i][j] = CCombMove[i / 24][j] * 24 + MPermMove[i % 24][j];
            }
        }

        for (int i = 0; i < 1680; i++) {
            for (int j = 0; j < 16; j++) {
                MPCCbConj[i][j] = CCombConj[i / 24][j] * 24 + MPermConj[i % 24][j];
            }
        }
    }



    static byte[][] initPrunTableMultiThread(int[][] Cord1Move, int[][] Cord2Move, int[][] Cord1Conj, int[][] Cord2Conj, boolean isPhase2) {
        int CORD1_SIZE = Cord1Move.length;
        int CORD2_SIZE = Cord2Move.length;

        byte[][] PrunTable = new byte[CORD1_SIZE][CORD2_SIZE * 3 * 3];
        for (int i = 0; i < CORD1_SIZE; i++) {
            for (int j = 0; j < CORD2_SIZE * 3 * 3; j++) {
                PrunTable[i][j] = 127;
            }
        }
        for (int i = 0; i < 3 * 3; i++) {
            PrunTable[0][i] = 0;
        }

        class InitPrunThread extends Thread {
            int curId = 0;
            int numId = 0;
            volatile int depth = -1;
            volatile boolean finished = false;
            volatile long[] doneCnt;

            InitPrunThread(int curId, int numId, long[] doneCnt) {
                this.curId = curId;
                this.numId = numId;
                this.doneCnt = doneCnt;
            }

            @Override
            public void run() {
                while (depth != -2) {
                    if (depth == -1) {
                        synchronized (PrunTable) {
                            try {
                                PrunTable.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        continue;
                    }
                    long done = 0;
                    for (int cord1 = curId; cord1 < CORD1_SIZE; cord1 += numId) {
                        for (int i = 0; i < CORD2_SIZE * 3 * 3; i++) {
                            if (PrunTable[cord1][i] != depth) {
                                continue;
                            }
                            ++done;
                            int cord2 = (i / 3 / 3);
                            int ct = (i / 3) % 3;
                            int leg = i % 3;
                            int[] nextState = NextState[leg];
                            for (int m = 0; m < N_LEG_MOVES; m++) {
                                int leg_ = nextState[m];
                                if (leg_ == -1) {
                                    continue;
                                }
                                int cord1_ = cord1;
                                int cord2_ = cord2;
                                int ct_ = ct;

                                int cubeMove = mOnCube[m];
                                if (cubeMove != -1) {
                                    cubeMove = MoveConj[ct][cubeMove];
                                    if (isPhase2) {
                                        cubeMove = Util.std2ud[cubeMove];
                                        if (cubeMove >= 10) {
                                            continue;
                                        }
                                    }
                                    cord1_ = Cord1Move[cord1][cubeMove];
                                    cord2_ = Cord2Move[cord2][cubeMove];
                                }
                                int ctMove = mOnCt[m];
                                if (ctMove != -1) {
                                    ct_ = CtMove[ct][ctMove];
                                }
                                if (ct_ >= 3) {
                                    int conj = ctStdConj[ct_];
                                    cord1_ = Cord1Conj[cord1_][conj];
                                    cord2_ = Cord2Conj[cord2_][conj];
                                    ct_ %= 3;
                                }
                                int idx = (cord2_ * 3 + ct_) * 3 + leg_;
                                int minCost = depth + mCost[leg][m];
                                synchronized (PrunTable[cord1_]) {
                                    if (minCost < PrunTable[cord1_][idx]) {
                                        PrunTable[cord1_][idx] = (byte) minCost;
                                    }
                                }
                            }
                        }
                    }
                    depth = -1;
                    synchronized (doneCnt) {
                        doneCnt[curId] = done;
                        doneCnt.notify();
                    }
                }
            }
        }
        int n_thread = N_INIT_THREAD;
        InitPrunThread[] initThread = new InitPrunThread[n_thread];
        long[] doneCnt = new long[n_thread];
        long doneTotal = 0;
        for (int i = 0; i < n_thread; i++) {
            initThread[i] = new InitPrunThread(i, n_thread, doneCnt);
            initThread[i].start();
        }
        for (int depth = 0; depth < 0xffff; depth++) {
            long done = 0;
            for (int i = 0; i < n_thread; i++) {
                initThread[i].depth = depth;
                doneCnt[i] = -1;
            }
            synchronized (PrunTable) {
                PrunTable.notifyAll();
            }
            synchronized (doneCnt) {
                for (int i = 0; i < n_thread; i++) {
                    while (doneCnt[i] == -1) {
                        try {
                            doneCnt.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    done += doneCnt[i];
                }
            }
            doneTotal += done;
            System.out.println(depth + "\t" + done + "\t" + doneTotal);
            if (doneTotal / CORD1_SIZE == CORD2_SIZE * 3 * 3) {
                break;
            }
        }
        for (int i = 0; i < n_thread; i++) {
            initThread[i].depth = -2;
        }
        synchronized (PrunTable) {
            PrunTable.notifyAll();
        }
        return PrunTable;
    }


    static byte[][] initPrunTable(int[][] Cord1Move, int[][] Cord2Move, int[][] Cord1Conj, int[][] Cord2Conj, boolean isPhase2) {
        int CORD1_SIZE = Cord1Move.length;
        int CORD2_SIZE = Cord2Move.length;

        byte[][] PrunTable = new byte[CORD1_SIZE][CORD2_SIZE * 3 * 3];
        for (int i = 0; i < CORD1_SIZE; i++) {
            for (int j = 0; j < CORD2_SIZE * 3 * 3; j++) {
                PrunTable[i][j] = 127;
            }
        }
        for (int i = 0; i < 3 * 3; i++) {
            PrunTable[0][i] = 0;
        }
        // int[][] possibleTransfer = new int[24][16];
        long doneTotal = 0;
        for (int depth = 0; depth < 0xffff; depth++) {
            long done = 0;
            boolean updated = false;
            for (int cord1 = 0; cord1 < CORD1_SIZE; cord1++) {
                for (int i = 0; i < CORD2_SIZE * 3 * 3; i++) {
                    if (PrunTable[cord1][i] != depth) {
                        continue;
                    }
                    ++done;
                    int cord2 = (i / 3 / 3);
                    int ct = (i / 3) % 3;
                    int leg = i % 3;
                    int[] nextState = NextState[leg];
                    for (int m = 0; m < N_LEG_MOVES; m++) {
                        int leg_ = nextState[m];
                        if (leg_ == -1) {
                            continue;
                        }
                        int cord1_ = cord1;
                        int cord2_ = cord2;
                        int ct_ = ct;

                        int cubeMove = mOnCube[m];
                        if (cubeMove != -1) {
                            cubeMove = MoveConj[ct][cubeMove];
                            if (isPhase2) {
                                cubeMove = Util.std2ud[cubeMove];
                                if (cubeMove >= 10) {
                                    continue;
                                }
                            }
                            cord1_ = Cord1Move[cord1][cubeMove];
                            cord2_ = Cord2Move[cord2][cubeMove];
                        }
                        int ctMove = mOnCt[m];
                        if (ctMove != -1) {
                            ct_ = CtMove[ct][ctMove];
                        }
                        if (ct_ >= 3) {
                            int conj = ctStdConj[ct_];
                            cord1_ = Cord1Conj[cord1_][conj];
                            cord2_ = Cord2Conj[cord2_][conj];
                            ct_ %= 3;
                        }
                        int idx = (cord2_ * 3 + ct_) * 3 + leg_;
                        int minCost = depth + mCost[leg][m];
                        if (minCost < PrunTable[cord1_][idx]) {
                            PrunTable[cord1_][idx] = (byte) minCost;
                            updated = true;
                        }
                    }
                }
            }
            // for (var i = 0; i < CORD1_SIZE * CORD2_SIZE * 24 * 3; i++) {
            //     if (PrunTable[i] != depth) {
            //         continue;
            //     }
            //     int cord1 = (i / 3 / 24 / CORD2_SIZE);
            //     int cord2 = (i / 3 / 24) % CORD2_SIZE;
            //     int ct = (i / 3) % 24;
            //     int leg = i % 3;

            //     int ct_ = ct % 3;
            //     // var conj = RotConjTarget[rot];
            //     // var flip_ = FlipConjTable[flip][conj];
            //     // var twist_ = TwistConjTable[twist][conj];
            //     for (int conj = 0; conj < 16; conj++) {
            //         int idx = ((Cord1Conj[cord1][conj] * CORD2_SIZE + Cord2Conj[cord2][conj]) * 24 + ct_) * 3 + leg;
            //         if (PrunTable[idx] != PrunTable[i]) {
            //             possibleTransfer[ct][conj] = 1;
            //         }
            //     }
            // }
            // for (int i = 0; i < 24; i++) {
            //     for (int j = 0; j < 16; j++) {
            //         if (possibleTransfer[i][j] != 1 && ((j & 0x4) == 0)) {
            //             System.out.println(i + "\t" + j);
            //         }
            //     }
            // }
            // console.log(depth, done);
            // console.log(rotConjMap);
            doneTotal += done;
            System.out.println(depth + "\t" + done + "\t" + doneTotal);
            if (doneTotal / CORD1_SIZE == CORD2_SIZE * 3 * 3) {
                break;
            }
        }
        return PrunTable;
    }

    @Override
    void calcPruning(boolean isPhase1) {
        if (fsym >= 3) {
            int conj = ctStdConj[fsym];
            slice = UDSliceConj[slice][conj];
            flip = FlipConj[flip][conj];
            flip ^= (conj % 2 == 1 ? FlipConjXor[slice] : 0);
            fsym %= 3;
            twist = TwistConj[twist][conj];
        }
        // prun = TwistFlipUDSPrun[twist][(slice << 11 | flip) * 9 + fsym * 3 + tsym];
        prun = Math.max(
                   TwistSliceF4Prun[twist][FlipUDS2SliceF4[slice << 11 | flip] * 9 + fsym * 3 + tsym],
                   Math.max(
                       TwistSliceF8Prun[twist][FlipUDS2SliceF8[slice << 11 | flip] * 9 + fsym * 3 + tsym],
                       // TwistUDSlicePrun[twist][slice * 9 + fsym * 3 + tsym],
                       FlipUDSPrun[0][(slice << 11 | flip) * 9 + fsym * 3 + tsym]
                   )
               );
    }

    @Override
    boolean setWithPrun(CubieCube cc, int depth) {
        twist = cc.getTwist();
        flip = cc.getFlip();
        slice = cc.getUDSlice();
        fsym = 0; //center
        tsym = 0; //leg

        calcPruning(true);
        return prun <= depth;
    }

    @Override
    int doMovePrun(CoordCube cc, int m, boolean isPhase1) {
        flip = cc.flip;
        twist = cc.twist;
        slice = cc.slice;
        fsym = cc.fsym;
        tsym = NextState[cc.tsym][m];

        var cubeMove = mOnCube[m];
        if (cubeMove != -1) {
            cubeMove = MoveConj[fsym][cubeMove];
            flip = FlipMove[flip][cubeMove];
            twist = TwistMove[twist][cubeMove];
            slice = UDSliceMove[slice][cubeMove];
        }
        var ctMove = mOnCt[m];
        if (ctMove != -1) {
            fsym = CtMove[cc.fsym][ctMove];
        }

        calcPruning(true);
        return prun;
    }

    static boolean isInited = false;

    static void saveToFile(String fileName, byte[][] PrunTable) {
        try {
            long FILE_SIZE = PrunTable.length * 1L * PrunTable[0].length;
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "rw");
            raf.setLength(FILE_SIZE);
            java.nio.channels.FileChannel channel = raf.getChannel();
            for (int i = 0; i < PrunTable.length; i++) {
                java.nio.MappedByteBuffer buffer = channel.map(java.nio.channels.FileChannel.MapMode.READ_WRITE, ((long) i) * PrunTable[i].length, PrunTable[i].length);
                buffer.put(PrunTable[i]);
            }
            raf.close();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }
    static byte[][] loadFromFile(String fileName, int CORD1_SIZE, int CORD2_SIZE) {
        try {
            // long[] distCnt = new long[80];
            byte[][] PrunTable = new byte[CORD1_SIZE][CORD2_SIZE * 3 * 3];
            long FILE_SIZE = CORD1_SIZE * 1L * CORD2_SIZE * 9;
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(fileName, "r");
            java.nio.channels.FileChannel channel = raf.getChannel();

            for (int i = 0; i < PrunTable.length; i++) {
                java.nio.MappedByteBuffer buffer = channel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, ((long) i) * PrunTable[i].length, PrunTable[i].length);
                buffer.get(PrunTable[i]);
                // for (int j = 0; j < PrunTable[i].length; j++) {
                //     distCnt[PrunTable[i][j]]++;
                // }
            }
            raf.close();
            // for (int i = 0; i < 80; i++) {
            //     System.out.println(i + "\t" + distCnt[i]);
            // }
            return PrunTable;
        } catch (java.io.FileNotFoundException e) {
            // e.printStackTrace();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    static void init() {
        if (isInited) {
            return;
        }
        isInited = true;
        CubieCube.initMove();
        CubieCube.initSym();
        CubieCube2L.initCenter();
        initCenterMove();
        initPhase1Move();
        initPhase2Move();
        // initPrunTableMultiThread(TwistMove, FlipMove);
        // initPrunTableMultiThread(TwistMove, FlipUDSMove, TwistConj, FlipUDSConj, false);

        // TwistUDSlicePrun = initPrunTableMultiThread(TwistMove, UDSliceMove, TwistConj, UDSliceConj, false);
        // FlipUDSPrun = initPrunTableMultiThread(EmptyMove, FlipUDSMove, EmptyConj, FlipUDSConj, false);
        // EPermCCombPrun = initPrunTableMultiThread(EPermMove, CCombMove, EPermConj, CCombConj, true);
        // CPermMPermPrun = initPrunTableMultiThread(CPermMove, MPermMove, CPermConj, MPermConj, true);

        TwistSliceF4Prun = loadFromFile("TwistSliceF4Prun.data", TwistMove.length, SliceF4Move.length);
        if (TwistSliceF4Prun == null) {
            TwistSliceF4Prun = initPrunTableMultiThread(TwistMove, SliceF4Move, TwistConj, SliceF4Conj, false);
            saveToFile("TwistSliceF4Prun.data", TwistSliceF4Prun);
        }

        TwistSliceF8Prun = loadFromFile("TwistSliceF8Prun.data", TwistMove.length, SliceF8Move.length);
        if (TwistSliceF8Prun == null) {
            TwistSliceF8Prun = initPrunTableMultiThread(TwistMove, SliceF8Move, TwistConj, SliceF8Conj, false);
            saveToFile("TwistSliceF8Prun.data", TwistSliceF8Prun);
        }

        // TwistUDSlicePrun = loadFromFile("TwistUDSlicePrun.data", TwistMove.length, UDSliceMove.length);
        // if (TwistUDSlicePrun == null) {
        //     TwistUDSlicePrun = initPrunTableMultiThread(TwistMove, UDSliceMove, TwistConj, UDSliceConj, false);
        //     saveToFile("TwistUDSlicePrun.data", TwistUDSlicePrun);
        // }

        FlipUDSPrun = loadFromFile("FlipUDSPrun.data", EmptyMove.length, FlipUDSMove.length);
        if (FlipUDSPrun == null) {
            FlipUDSPrun = initPrunTableMultiThread(EmptyMove, FlipUDSMove, EmptyConj, FlipUDSConj, false);
            saveToFile("FlipUDSPrun.data", FlipUDSPrun);
        }

        // EPermCCombPrun = loadFromFile("EPermCCombPrun.data", EPermMove.length, CCombMove.length);
        // if (EPermCCombPrun == null) {
        //     EPermCCombPrun = initPrunTableMultiThread(EPermMove, CCombMove, EPermConj, CCombConj, true);
        //     saveToFile("EPermCCombPrun.data", EPermCCombPrun);
        // }

        EPermMPCCbPrun = loadFromFile("EPermMPCCbPrun.data", EPermMove.length, MPCCbMove.length);
        if (EPermMPCCbPrun == null) {
            EPermMPCCbPrun = initPrunTableMultiThread(EPermMove, MPCCbMove, EPermConj, MPCCbConj, true);
            saveToFile("EPermMPCCbPrun.data", EPermMPCCbPrun);
        }

        CPermMPermPrun = loadFromFile("CPermMPermPrun.data", CPermMove.length, MPermMove.length);
        if (CPermMPermPrun == null) {
            CPermMPermPrun = initPrunTableMultiThread(CPermMove, MPermMove, CPermConj, MPermConj, true);
            saveToFile("CPermMPermPrun.data", CPermMPermPrun);
        }

        // TwistFlipUDSPrun = loadFromFile("TwistFlipUDSPrun.data", TwistMove.length, FlipUDSMove.length);
        // if (TwistFlipUDSPrun == null) {
        //     TwistFlipUDSPrun = initPrunTableMultiThread(TwistMove, FlipUDSMove, TwistConj, FlipUDSConj, false);
        //     saveToFile("TwistFlipUDSPrun.data", TwistFlipUDSPrun);
        // }
    }

    public static void main(String[] args) {
        init();
    }
}
