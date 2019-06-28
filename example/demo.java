import cs.min2phase.Search;

public class demo {
    public static void main(String[] args) {
        //Basic usage
        String scrambledCube = "DUUBULDBFRBFRRULLLBRDFFFBLURDBFDFDRFRULBLUFDURRBLBDUDL";
        String result = new Search().solution(scrambledCube, 21, 100000000, 0, 0);
        System.out.println(result);// R2 U2 B2 L2 F2 U' L2 R2 B2 R2 D  B2 F  L' F  U2 F' R' D' L2 R'

        //Output Control
        result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.APPEND_LENGTH);
        System.out.println(result);// R2 U2 B2 L2 F2 U' L2 R2 B2 R2 D  B2 F  L' F  U2 F' R' D' L2 R' (21f)
        result = new Search().solution(scrambledCube, 21, 100000000, 0, Search.USE_SEPARATOR | Search.INVERSE_SOLUTION);
        System.out.println(result);// R  L2 D  R  F  U2 F' L  F' .  B2 D' R2 B2 R2 L2 U  F2 L2 B2 U2 R2

        //Find shorter solutions (try more probes even a solution has already been found)
        //In this example, we try AT LEAST 10000 phase2 probes to find shorter solutions.
        result = new Search().solution(scrambledCube, 21, 100000000, 10000, 0);
        System.out.println(result);// L2 U  D2 R' B  U2 L  F  U  R2 D2 F2 U' L2 U  B  D  R'

        //Continue to find shorter solutions
        Search searchObj = new Search();
        result = searchObj.solution(scrambledCube, 21, 500, 0, 0);
        System.out.println(result);// R2 U2 B2 L2 F2 U' L2 R2 B2 R2 D  B2 F  L' F  U2 F' R' D' L2 R'
        result = searchObj.next(500, 0, 0);
        System.out.println(result);// D2 L' D' L2 U  R2 F  B  L  B  D' B2 R2 U' R2 U' F2 R2 U' L2
        result = searchObj.next(500, 0, 0);
        System.out.println(result);// L' U  B  R2 F' L  F' U2 L  U' B' U2 B  L2 F  U2 R2 L2 B2
        result = searchObj.next(500, 0, 0);
        System.out.println(result);// Error 8, no solution is found after 500 phase2 probes. Let's try more probes.
        result = searchObj.next(500, 0, 0);
        System.out.println(result);// L2 U  D2 R' B  U2 L  F  U  R2 D2 F2 U' L2 U  B  D  R'
    }
}