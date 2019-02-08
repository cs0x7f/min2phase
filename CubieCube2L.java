package cs.min2phase;

import java.util.Arrays;

class CubieCube2L extends CubieCube {
    // static String[] move2str = new String[] {
    //     "U ",  // 0
    //     "U2",  // 1
    //     "U'",  // 2
    //     "R ",  // 3
    //     "R2",  // 4
    //     "R'",  // 5
    //     "y ",  // 6
    //     "y2",  // 7
    //     "y'",  // 8
    //     "x ",  // 9
    //     "x2",  // 10
    //     "x'",  // 11
    //     "",  // 12
    //     "",  // 13
    //     "y ",  // 14
    //     "y2",  // 15
    //     "y'",  // 16
    //     "x ",  // 17
    //     "x2",  // 18
    //     "x'"   // 19
    // };

    static String[] move2str = new String[] {
        "(z1z0) U ",  // 0
        "(z2z0) U2",  // 1
        "(z3z0) U'",  // 2
        "(z0z1) R ",  // 3
        "(z0z2) R2",  // 4
        "(z0z3) R'",  // 5
        "(z1s0) y ",  // 6
        "(z2s0) y2",  // 7
        "(z3s0) y'",  // 8
        "(s0z1) x ",  // 9
        "(s0z2) x2",  // 10
        "(s0z3) x'",  // 11
        "(z0s1)   ",  // 12
        "(s1z0)   ",  // 13
        "(z1s1) y ",  // 14
        "(z2s1) y2",  // 15
        "(z3s1) y'",  // 16
        "(s1z1) x ",  // 17
        "(s1z2) x2",  // 18
        "(s1z3) x'"   // 19
    };

    int ct = 0x543210;

    CubieCube2L() {}

    CubieCube2L(CubieCube c) {
        copy(c);
        this.ct = 0x543210;
    }

    CubieCube2L(int cperm, int twist, int eperm, int flip, int ct) {
        super(cperm, twist, eperm, flip);
        this.ct = ct;
    }

    void copy(CubieCube2L c) {
        super.copy(c);
        this.ct = c.ct;
    }

    @Override
    void copy(CubieCube c) {
        super.copy(c);
        this.ct = 0x543210;
    }

    int getSliceF4() {
        int slice = getUDSlice();
        var flip4 = 0;
        for (int i = 0; i < 12; i++) {
            if ((ea[i] >> 1) >= 8) {
                flip4 = flip4 << 1 | ea[i] & 1;
            }
        }
        return slice << 4 | flip4;
    }

    void setSliceF4(int idx) {
        setUDSlice(idx >> 4);
        for (int i = 11; i >= 0; i--) {
            if ((ea[i] >> 1) >= 8) {
                ea[i] = (byte) (ea[i] & 0xfffe | (idx & 1));
                idx >>= 1;
            }
        }
    }

    int getSliceF8() {
        int slice = getUDSlice();
        var flip8 = 0;
        for (int i = 0; i < 12; i++) {
            if ((ea[i] >> 1) < 8) {
                flip8 = flip8 << 1 | ea[i] & 1;
            }
        }
        return slice << 8 | flip8;
    }

    void setSliceF8(int idx) {
        setUDSlice(idx >> 8);
        for (int i = 11; i >= 0; i--) {
            if ((ea[i] >> 1) < 8) {
                ea[i] = (byte) (ea[i] & 0xfffe | (idx & 1));
                idx >>= 1;
            }
        }
    }

    void setCComb(int idx) {
        Util.setComb(this.ca, idx, 0, false);
    }

    int getCComb() {
        return Util.getComb(this.ca, 0, false);
    }

    int getCtIdx() {
        // System.out.println(Integer.toHexString(ct) + "\t" + java.util.Arrays.asList(ctIdx2Val).indexOf(ct));
        for (int i = 0; i < 24; i++) {
            if (ctIdx2Val[i] == ct) {
                return i;
            }
        }
        return -1;
        // return java.util.Arrays.asList(ctIdx2Val).indexOf(ct);
    }

    void setCtIdx(int idx) {
        this.ct = ctIdx2Val[idx];
    }

    void doMoveTo(int m, CubieCube2L cc) {
        int axis = ~~(m / 3);
        int pow = m % 3;
        if (m >= Util.Ux1 && m <= Util.Bx3) {
            axis = this.ct >> (axis << 2) & 0xf;
            m = axis * 3 + pow;
            EdgeMult(this, moveCube[m], cc);
            CornMult(this, moveCube[m], cc);
            cc.ct = this.ct;
        } else {
            cc.copy(this);
            int ct = this.ct;
            for (int i = 0; i <= pow; i++) {
                switch (axis) {
                case 6: // y
                    ct = ((ct & 0x00000f) << 0) | ((ct & 0x0000f0) << 4) | ((ct & 0x000f00) << 8) |
                         ((ct & 0x00f000) << 0) | ((ct & 0x0f0000) << 4) | ((ct & 0xf00000) >> 16);
                    break;
                case 7: // x
                    ct = ((ct & 0x00000f) << 20) | ((ct & 0x0000f0) << 0) | ((ct & 0x000f00) >> 8) |
                         ((ct & 0x00f000) >> 4) | ((ct & 0x0f0000) << 0) | ((ct & 0xf00000) >> 8);
                    break;
                case 8: // z
                    ct = ((ct & 0x00000f) << 4) | ((ct & 0x0000f0) << 8) | ((ct & 0x000f00) << 0) |
                         ((ct & 0x00f000) << 4) | ((ct & 0x0f0000) >> 16) | ((ct & 0xf00000) >> 0);
                    break;
                }
            }
            cc.ct = ct;
        }
    }

    static int[] ctIdx2Val = new int[24];

    static void initCenter() {
        for (int i = 0, ct = 0x543210; i < 24; i++) {
            ctIdx2Val[i] = ct;

            //URF
            if (i % 1 == 0) {
                ct = ((ct & 0x00000f) << 4) | ((ct & 0x0000f0) << 4) | ((ct & 0x000f00) >> 8) |
                     ((ct & 0x00f000) << 4) | ((ct & 0x0f0000) << 4) | ((ct & 0xf00000) >> 8);
            }
            //U4
            if (i % 3 == 2) {
                ct = ((ct & 0x00000f) << 0) | ((ct & 0x0000f0) << 4) | ((ct & 0x000f00) << 8) |
                     ((ct & 0x00f000) << 0) | ((ct & 0x0f0000) << 4) | ((ct & 0xf00000) >> 16);
            }
            //R2
            if (i % 12 == 11) {
                ct = ((ct & 0x00000f) << 12) | ((ct & 0x0000f0) << 0) | ((ct & 0x000f00) << 12) |
                     ((ct & 0x00f000) >> 12) | ((ct & 0x0f0000) << 0) | ((ct & 0xf00000) >> 12);
            }
        }
    }

    public static void main(String[] args) {
        initCenter();
    }
}
