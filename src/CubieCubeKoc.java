package cs.min2phase;

import java.util.Arrays;

class CubieCubeKoc extends CubieCube {

    static int[] UDSliceFlipS2R;
    static int[] FlipSlice2UDSliceFlip;
    static char[] SymStateUDSliceFlip;

    CubieCubeKoc() {
    	super();
    }

    CubieCubeKoc(CubieCube cc) {
    	super(cc);
    }

    void setUDSliceFlip(int idx) {
        setFlip(idx & 0x7ff);
        setUDSlice(idx >> 11);
    }

    int getUDSliceFlip() {
        return (getUDSlice() & 0x1ff) << 11 | getFlip();
    }

    int getUDSliceFlipSym() {
        int flip = getFlipSym();
        int fsym = flip & 0x7;
        flip >>= 3;
        int udslice = getUDSlice() & 0x1ff;
        int udsliceflip = FlipSlice2UDSliceFlip[flip * 495 + CoordCube.UDSliceConj[udslice][fsym]];
        return udsliceflip & 0xfffffff0 | SymMult[udsliceflip & 0xf][fsym << 1];
    }

    static void initUDSliceFlipSym2Raw() {
        UDSliceFlipS2R = new int[64430];
        SymStateUDSliceFlip = new char[64430];
        FlipSlice2UDSliceFlip = new int[CoordCube.N_FLIP_SYM * CoordCube.N_SLICE];

        CubieCubeKoc c = new CubieCubeKoc();
        CubieCubeKoc d = new CubieCubeKoc();
        int[] occ = new int[2048 * 495 >> 5];
        int count = 0;
        for (int i = 0; i < 2048 * 495; i++) {
            if ((occ[i >> 5] & 1 << (i & 0x1f)) != 0) {
                continue;
            }
            c.setUDSliceFlip(i);
            for (int s = 0; s < 16; s++) {
                EdgeConjugate(c, s, d);
                int idx = d.getUDSliceFlip();
                if (idx == i) {
                    SymStateUDSliceFlip[count] |= 1 << s;
                }
                occ[idx >> 5] |= 1 << (idx & 0x1f);
                int fidx = Arrays.binarySearch(FlipS2R, (char) (idx & 0x7ff));
                if (fidx >= 0) {
                    FlipSlice2UDSliceFlip[fidx * CoordCube.N_SLICE + (idx >> 11)] = count << 4 | s;
                }
            }
            UDSliceFlipS2R[count++] = i;
        }
        assert count == 64430;
    }
}