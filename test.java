import java.util.Random;
import java.io.*;
import cs.min2phase.Tools;
import cs.min2phase.Search;

public class test {

    static int[] fact = {1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800, 479001600};

    static void set8Perm(byte[] arr, int idx) {
        int val = 0x76543210;
        for (int i = 0; i < 7; i++) {
            int p = fact[7 - i];
            int v = idx / p;
            idx -= v * p;
            v <<= 2;
            arr[i] = (byte) ((val >> v) & 0xf);
            int m = (1 << v) - 1;
            val = (val & m) + ((val >> 4) & ~m);
        }
        arr[7] = (byte)val;
    }


    static int get8Perm(byte[] arr) {
        int idx = arr[0];
        int val = 0x76543210 - (0x11111110 << (idx << 2));
        for (int i = 1; i < 7; i++) {
            int v = arr[i] << 2;
            val -= 0x11111110 << v;
            idx = (8 - i) * idx + ((val >> v) & 0xf);
        }
        return idx;
    }

    static void set8PermX(byte[] arr, int idx) {
        int val = 0x76543210;
        for (int i = 8; i > 1; i--) {
            int v = (idx % i) << 2;
            idx /= i;
            arr[8 - i] = (byte) ((val >> v) & 0xf);
            int m = (1 << v) - 1;
            val = (val & m) + ((val >> 4) & ~m);
        }
        arr[7] = (byte)val;
    }

    static int get8PermX(byte[] arr) {
        int idx = 0;
        int val = 0x11111110 << (arr[7] << 2);
        for (int i = 6; i >= 0; i--) {
            int v = arr[i] << 2;
            idx = (8 - i) * idx + ((val >> v) & 0xf);
            val += 0x11111110 << v;
        }
        return idx;
    }

    static void other() {
        for (int ix = 0; ix < 100; ix++) {
            byte[] cp = {0, 1, 2, 3, 4, 5, 6, 7};
            byte[] co = {0, 0, 0, 0, 0, 0, 0, 0};
            byte[] ep = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
            byte[] eo = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            //          Tools.randomState(cp, co, ep, eo);

            for (int i = 0; i < 8; ++i) {
                System.out.print(String.format("%2s", cp[i]));
            }
            System.out.print("\t");
            for (int i = 0; i < 8; ++i) {
                System.out.print(String.format("%2s", co[i]));
            }
            System.out.print("\t");
            for (int i = 0; i < 12; ++i) {
                System.out.print(String.format("%3s", ep[i]));
            }
            System.out.print("\t");
            for (int i = 0; i < 12; ++i) {
                System.out.print(String.format("%2s", eo[i]));
            }
            System.out.println();
        }
    }

    static int testOptimal(int maxl, int length, int[] arr, int lm, Search search) {
        if (maxl == 0) {
            String sol = search.solution(Tools.fromScramble(arr), 10, 100000, 0, 0);
            while (sol.length() > length * 3) {
                sol = search.next(100000, 0, 0);
                if (sol.startsWith("Error")) {
                    throw new RuntimeException("Cannot find the optimal solution.");
                }
            }
            return 1;
        }
        int ret = 0;
        for (int axis = 0; axis < 18; axis += 3) {
            if (axis == lm || axis == lm - 9) {
                continue;
            }
            for (int pow = 0; pow < 3; pow++) {
                arr[length] = axis + pow;
                ret += testOptimal(maxl - 1, length + 1, arr, axis, search);
            }
        }
        return ret;
    }

    static void testRandomOptimal(int length, Search search, Random gen) {
        int[] arr = new int[length];
        for (int i = 0; i < length; i++) {
            int axis = 0;
            do {
                axis = gen.nextInt(6) * 3;
            } while (i != 0 && (axis == arr[i - 1] || axis == arr[i - 1] - 9));
            int pow = gen.nextInt(3);
            arr[i] = axis + pow;
        }
        String sol = search.solution(Tools.fromScramble(arr), 20, 100000, 0, 0);
        while (sol.length() > length * 3 || sol.startsWith("Error")) {
            if (sol.startsWith("Error")) {
                System.out.println(sol);
                throw new RuntimeException("Cannot find the optimal solution.");
            }
            sol = search.next(100000, 0, 0);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("java -client test testValue [nSolves maxLength maxTime minTime verbose]");
            return;
        }
        int testValue = args.length < 1 ? 0 : Integer.parseInt(args[0]);
        int nSolves = args.length < 2 ? 1000 : Integer.parseInt(args[1]);
        int maxLength = args.length < 3 ? 21 : Integer.parseInt(args[2]);
        int maxTime = args.length < 4 ? 100000 : Integer.parseInt(args[3]);
        int minTime = args.length < 5 ? 0 : Integer.parseInt(args[4]);
        int verbose = args.length < 6 ? 0 : Integer.parseInt(args[5]);

        long tm;
        if ((testValue & 0x01) != 0) {
            tm = System.nanoTime();
            other();
            System.out.println(System.nanoTime() - tm);
        }

        DataInputStream dis = null;
        if ((testValue & 0x02) != 0) {
            tm = System.nanoTime();
            try {
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream("data")));
                Tools.initFrom(dis);
            } catch (Exception e) {
                dis = null;
                e.printStackTrace();
            }
            System.out.println(System.nanoTime() - tm);
        }
        if ((testValue & 0x04) != 0) {
            tm = System.nanoTime();
            if (dis == null) {
                DataOutputStream dos = null;
                try {
                    dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("data")));
                    Tools.saveTo(dos);
                    dos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println(System.nanoTime() - tm);
        }

        tm = System.nanoTime();
        Search.init();
        if ((testValue & 0x08) != 0) {
            System.out.println(String.format("Initialization Time: %d ms\r", System.nanoTime() - tm));
        }


        Search search = new Search();
        String sol;
        if ((testValue & 0x10) != 0) {
            System.out.println("========== Selected Scramble Solving Test ==========");
            System.out.print("IdCube Test: \"");
            String scr = Tools.fromScramble(new int[0]);
            System.out.print(search.solution(scr, 21, 100000, 0, 0));
            System.out.println("\"");

            int n_test = 0;
            long curTime;
            for (int length = 1; length < 5; length++) {
                System.out.print(String.format("%d-Move: ", length));
                curTime = System.nanoTime();
                n_test = testOptimal(length, 0, new int[length], -1, search);
                System.out.println(String.format("OK, All %d Cube(s) Solved Optimally. AvgTime: %1.3f ms.", n_test, (System.nanoTime() - curTime) / 1000000d / n_test));
            }

            for (int length = 5; length < 13; length++) {
                System.out.print(String.format("%d-Move: ", length));
                Random gen = new Random(42L);
                curTime = System.nanoTime();
                n_test = 1000;
                for (int i = 0; i < n_test; i++) {
                    testRandomOptimal(length, search, gen);
                }
                System.out.println(String.format("OK, %d Cube(s) Solved Optimally. AvgTime: %1.3f ms.", n_test, (System.nanoTime() - curTime) / 1000000d / n_test));
            }
            System.out.print("SuperFlip: ");
            curTime = System.nanoTime();
            sol = search.solution(Tools.superFlip(), 20, 100000, 0, 0);
            System.out.println(String.format("OK. Time: %1.3f ms.", (System.nanoTime() - curTime) / 1000000d));
            System.out.print("20-Depth: ");
            String[] depth20 = new String[] {
                "B2 L  B2 R' F' U' B' L  D' F' L  U  L2 B2 L' D2 B2 D2 R2 B2",
                "R  U2 R  D2 R2 B2 L' D' B' F  U  B' R' U2 L' D  R2 F' U2 L2",
                "D2 R2 F2 D2 F2 D2 R' F2 D' L2 R  B  L' F  U  R' B  F2 R2 F'",
                "D' F' U  B2 R2 F  R' U2 B' L  D  F  R  D2 R2 L2 D' R2 F2 D'",
                "U2 R2 F2 D' U  F2 U2 B  U  B' R  U' F  L  B  R' F  L2 D' B ",
                "D  B2 D' B2 R2 D' R2 U  L  R' D  B' D  R  F' D2 R2 U' F' R ",
                "B  D' L' F' L  F  B  U' D2 F' R2 B' U  F2 R' L  U2 R2 F2 B2",
                "U2 L' U2 F2 L' R  D2 L2 B' D2 L  F' R' U' L  U2 F' D' R  B ",
                "F' L  B2 R  U' B' L  U2 D' F  L' R2 U2 D2 B2 R2 D  R2 L2 F2",
                "U2 R2 D2 B  U2 B' F  D' B' R' D  U2 B2 F2 R' D' B  U' F' R2"
            };
            n_test = 10;
            curTime = System.nanoTime();
            for (int i = 0; i < depth20.length; i++) {
                sol = search.solution(Tools.fromScramble(depth20[0]), 20, 100000, 0, 0);
            }
            System.out.println(String.format("OK, Random %d Cube(s) Solved. AvgTime: %1.3f ms.", n_test, (System.nanoTime() - curTime) / 1000000d / n_test));
        }

        if ((testValue & 0x20) != 0) {
            System.out.println("========== Random Cube Solving Test ==========");
            System.out.println(String.format("Solve Random %d Cubes:", nSolves));
            System.out.println(
                "MaxLength: " + maxLength + "\n" +
                "MaxTimeLimited: " + maxTime + "\n" +
                "MinTimeLimited: " + minTime + "\n" +
                "verbose: " + verbose);
            tm = System.nanoTime();
            int total = 0;
            int x = 0;
            //          System.out.print("Average Solving Time: - nanoSecond(s)\r");
            long minT = 1L << 62;
            long maxT = 0L;
            long totalTime = 0;
            Tools.setRandomSource(new Random(42L));
            while (System.nanoTime() - tm < 60000000000L && x < nSolves) {
                long curTime = System.nanoTime();
                String cube = Tools.randomCube();
                String s = search.solution(cube, maxLength, maxTime, minTime, verbose);
                if (s.length() > 63) {
                    s = search.next(maxTime, 0, verbose);
                }
                curTime = System.nanoTime() - curTime;
                totalTime += curTime;
                maxT = Math.max(maxT, curTime);
                minT = Math.min(minT, curTime);
                x++;
                System.out.print(String.format("AvgTime: %8.3f ms, MaxTime: %8.3f ms, MinTime: %8.3f ms\r",
                                               (totalTime / 1000000d) / x, maxT / 1000000d, minT / 1000000d));
            }
            System.out.println();
            System.out.println(x + " Random Cube(s) Solved");
        }
    }
}
