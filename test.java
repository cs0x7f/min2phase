import java.util.Random;
import java.io.*;
import cs.min2phase.Tools;
import cs.min2phase.Search;

public class test {

    static void other() {
    }

    static int testOptimal(int maxl, int length, int[] arr, int lm, Search search, int verbose) {
        if (maxl == 0) {
            String sol = search.solution(Tools.fromScramble(arr), 100, 0, 0, verbose | Search.INVERSE_SOLUTION);
            while (sol.startsWith("Error") || search.length() > length) {
                if (sol.startsWith("Error") && !sol.startsWith("Error 8")) {
                    throw new RuntimeException(String.format("Cannot find the optimal solution: %s", sol));
                }
                sol = search.next(100, 0, verbose | Search.INVERSE_SOLUTION);
            }
            assert Tools.fromScramble(sol).equals(Tools.fromScramble(arr));
            return 1;
        }
        int ret = 0;
        for (int axis = 0; axis < 18; axis += 3) {
            if (axis == lm || axis == lm - 9) {
                continue;
            }
            for (int pow = 0; pow < 3; pow++) {
                arr[length] = axis + pow;
                ret += testOptimal(maxl - 1, length + 1, arr, axis, search, verbose);
            }
        }
        return ret;
    }

    static void testRandomOptimal(int length, Search search, Random gen, int verbose) {
        int[] arr = new int[length];
        for (int i = 0; i < length; i++) {
            int axis = 0;
            do {
                axis = gen.nextInt(6) * 3;
            } while (i != 0 && (axis == arr[i - 1] / 3 * 3 || axis == arr[i - 1] / 3 * 3 - 9));
            int pow = gen.nextInt(3);
            arr[i] = axis + pow;
        }
        String sol = search.solution(Tools.fromScramble(arr), 100, 0, 0, verbose | Search.INVERSE_SOLUTION);
        while (sol.startsWith("Error") || search.length() > length) {
            if (sol.startsWith("Error") && !sol.startsWith("Error 8")) {
                throw new RuntimeException(String.format("Cannot find the optimal solution: %s", sol));
            }
            sol = search.next(100, 0, verbose | Search.INVERSE_SOLUTION);
        }
        assert Tools.fromScramble(sol).equals(Tools.fromScramble(arr));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("java -client test testValue [nSolves maxLength maxProbe minProbe verbose]");
            return;
        }
        int testValue = args.length < 1 ? 0 : Integer.parseInt(args[0]);
        int nSolves = args.length < 2 ? 1000 : Integer.parseInt(args[1]);
        int maxLength = args.length < 3 ? 21 : Integer.parseInt(args[2]);
        int probeMax = args.length < 4 ? 100000 : Integer.parseInt(args[3]);
        int probeMin = args.length < 5 ? 0 : Integer.parseInt(args[4]);
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
            System.out.println("========== Selected Scramble Solving Test (Optimal Solver) ==========");
            String scr = Tools.fromScramble(new int[0]);
            System.out.println(String.format("IdCube Test: \"%s\"", search.solution(scr, 21, 100000, 0, Search.OPTIMAL_SOLUTION)));

            int n_test = 0;
            long curTime;
            final int MAX_OPT_ALL = 3;
            for (int length = 1; length <= MAX_OPT_ALL; length++) {
                System.out.print(String.format("%d-Move: ", length));
                curTime = System.nanoTime();
                n_test = testOptimal(length, 0, new int[length], -1, search, Search.OPTIMAL_SOLUTION);
                System.out.println(String.format("OK, All %d Cube(s) Solved Optimally. AvgTime: %1.3f ms.", n_test, (System.nanoTime() - curTime) / 1000000d / n_test));
            }

            final long MAX_TEST_TIME = 100000000;
            for (int length = MAX_OPT_ALL + 1; length < 15; length++) {
                System.out.print(String.format("%d-Move: ", length));
                Random gen0 = new Random(42L);
                Random gen = new Random();
                curTime = System.nanoTime();
                n_test = 0;
                while (System.nanoTime() - curTime < MAX_TEST_TIME) {
                    gen.setSeed(gen0.nextLong());
                    testRandomOptimal(length, search, gen, Search.OPTIMAL_SOLUTION);
                    ++n_test;
                }
                System.out.println(String.format("OK, %d Cube(s) Solved Optimally. AvgTime: %1.3f ms.", n_test, (System.nanoTime() - curTime) / 1000000d / n_test));
            }

            System.out.println("========== Selected Scramble Solving Test (Two-phase Solver) ==========");
            System.out.print("IdCube Test: \"");
            scr = Tools.fromScramble(new int[0]);
            System.out.print(search.solution(scr, 21, 100000, 0, Search.OPTIMAL_SOLUTION));
            System.out.println("\"");

            for (int length = 1; length <= MAX_OPT_ALL; length++) {
                System.out.print(String.format("%d-Move: ", length));
                curTime = System.nanoTime();
                n_test = testOptimal(length, 0, new int[length], -1, search, 0);
                System.out.println(String.format("OK, All %d Cube(s) Solved Optimally. AvgTime: %1.3f ms.", n_test, (System.nanoTime() - curTime) / 1000000d / n_test));
            }

            for (int length = MAX_OPT_ALL + 1; length < 15; length++) {
                System.out.print(String.format("%d-Move: ", length));
                Random gen0 = new Random(42L);
                Random gen = new Random();
                curTime = System.nanoTime();
                n_test = 0;
                while (System.nanoTime() - curTime < MAX_TEST_TIME) {
                    gen.setSeed(gen0.nextLong());
                    testRandomOptimal(length, search, gen, 0);
                    ++n_test;
                }
                System.out.println(String.format("OK, %d Cube(s) Solved Optimally. AvgTime: %1.3f ms.", n_test, (System.nanoTime() - curTime) / 1000000d / n_test));
            }

            System.out.print("SuperFlip: ");
            curTime = System.nanoTime();
            sol = search.solution(Tools.superFlip(), 20, 1000000000, 0, 0);
            while (sol.startsWith("Error") || search.length() > 20) {
                if (sol.startsWith("Error") && !sol.startsWith("Error 8")) {
                    throw new RuntimeException(String.format("Cannot find the optimal solution: %s", sol));
                }
                sol = search.next(100000, 0, 0);
            }
            System.out.println(String.format("OK:\n%s\nTime: %1.3f ms.", sol, (System.nanoTime() - curTime) / 1000000d));
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
                sol = search.solution(Tools.fromScramble(depth20[i]), 20, 100000, 0, Search.INVERSE_SOLUTION);
                assert Tools.fromScramble(depth20[i]).equals(Tools.fromScramble(sol));
            }
            System.out.println(String.format("OK, Random %d Cube(s) Solved. AvgTime: %1.3f ms.", n_test, (System.nanoTime() - curTime) / 1000000d / n_test));
        }

        if ((testValue & 0x20) != 0) {
            System.out.println("========== Random Scramble Solving Test (Two-phase Solver) ==========");
            System.out.println(String.format("Solve Random %d Cubes:", nSolves));
            System.out.println(
                "MaxLength: " + maxLength + "\n" +
                "ProbeMax: " + probeMax + "\n" +
                "ProbeMin: " + probeMin + "\n" +
                "verbose: " + verbose);
            tm = System.nanoTime();
            int total = 0;
            int x = 0;
            //          System.out.print("Average Solving Time: - nanoSecond(s)\r");
            long minT = 1L << 62;
            long maxT = 0L;
            long totalTime = 0;
            Tools.setRandomSource(new Random(42L));
            int totalLength = 0;
            int[] lengthDis = new int[30];
            while (System.nanoTime() - tm < 6000000000000L && x < nSolves) {
                long curTime = System.nanoTime();
                String cube = Tools.randomCube();
                String s = search.solution(cube, maxLength, probeMax, probeMin, verbose);
                // if (s.length() > 63) {
                //     s = search.next(probeMax, 0, verbose);
                // }
                curTime = System.nanoTime() - curTime;
                totalTime += curTime;
                maxT = Math.max(maxT, curTime);
                minT = Math.min(minT, curTime);
                totalLength += search.length();
                lengthDis[search.length()]++;
                x++;
                System.out.print(String.format("%6d AvgT: %6.3f ms, MaxT: %8.3f ms, MinT: %6.3f ms, AvgL: %6.3f\r", x,
                                               (totalTime / 1000000d) / x, maxT / 1000000d, minT / 1000000d, totalLength / 1.0d / x));
            }
            System.out.println();
            System.out.println(x + " Random Cube(s) Solved");
            System.out.println("Length Distribution: ");
            for (int i = 0; i < 30; i++) {
                if (lengthDis[i] != 0) {
                    System.out.println(String.format("%2d: %d", i, lengthDis[i]));
                }
            }
        }
    }
}
