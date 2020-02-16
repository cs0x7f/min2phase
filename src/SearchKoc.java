package cs.min2phase;

public class SearchKoc extends Search {

    static {
        MIN_P1LENGTH_PRE = 9;
        MAX_DEPTH2 = 20;
    }

    public SearchKoc() {
        for (int i = 0; i < 21; i++) {
            nodeUD[i] = new CoordCubeKoc();
            nodeRL[i] = new CoordCubeKoc();
            nodeFB[i] = new CoordCubeKoc();
        }
        for (int i = 0; i < 6; i++) {
            urfCoordCube[i] = new CoordCubeKoc();
        }
    }

    public synchronized String solution(String facelets, int maxDepth, long probeMax, long probeMin, int verbose) {
        init();
        return super.solution(facelets, maxDepth, probeMax, probeMin, verbose);
    }

    public synchronized static void init() {
        if (!inited) {
            CubieCube.initMove();
            CubieCube.initSym();
        }
        CoordCubeKoc.init(true);
        inited = true;
    }
}
