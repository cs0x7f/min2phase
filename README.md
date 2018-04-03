# min2phase
- Rubik's Cube solver or scrambler. [![Build Status](https://travis-ci.org/cs0x7f/min2phase.svg?branch=dev-min)](https://travis-ci.org/cs0x7f/min2phase)

# Usage

```java
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
```

# Two-phase algorithm
- See Kociemba's [page](http://kociemba.org/cube.htm)

## Improvements compared with conventional two-phase algorithm
Conventional two-phase algorithm only find (sub-)optimal solutions to &lt;U,R2,F2,D,L2,B2&gt;. However, If we are able to find more phase1 solutions within a limited depth, the probability of finding a short solution will be increased. 
- Try different axes: The target of phase1 can be either &lt;U,R2,F2,D,L2,B2&gt;, &lt;U2,R,F2,D2,L,B2&gt;, or &lt;U2,R2,F,D2,L2,B&gt;.
- Try the inverse of the state: We will try to solve the inverse state simultaneously to find more phase1 solutions. 
- Try pre-scramble: We can also use pre-scramble technique (which is widely used in fewest-move challenge) to find more phase1 solutions. If PreMoves * Scramble * Phase1 * Phase2 = Solved, then Scramble * (Phase1 * Phase2 * PreMoves) = Solved, Solution = Phase1 * Phase2 * PreMoves.

## Compilation options
There are several compilation options that can be modified for different purposes, e.g. faster solving speed, or less resource usage. 
- USE_TWIST_FLIP_PRUN [Search.java line 28](https://github.com/cs0x7f/min2phase/blob/dev-min/Search.java#L28): To determine whether twist-flip-pruning table will be generated and used. The size of the table is about 300KB (2,048 * 324 entries / 2 entries per byte = 331,776 bytes).
- PARTIAL_INIT_PRUN [Search.java line 42](https://github.com/cs0x7f/min2phase/blob/dev-min/Search.java#L38): For some situations that initialization speed is bottleneck, e.g. initialization cannot be pre-executed before solving, this option can be set to reduce initialization time by about 50%. However, the solving speed is about 2x slower due to ineffective pruning. However, the initialization will continue during the search, in other word, the solving speed will be increased after solving several cubes.
- MAX_PRE_MOVES, TRY_INVERSE, TRY_THREE_AXES [Search.java line 49](https://github.com/cs0x7f/min2phase/blob/dev-min/Search.java#L41): Three improvements of min2phase compared with conventional two-phase algorithm. Can be disabled for research purpose. 

# Benchmark
- Memory: ~1M with twist-flip-pruning (TFP) table, ~0.7M without TFP table, ~40M with Full table.
- Average solving time (CPU: Intel Core i7-6700HQ. Flag: F=Full P1 table, R=Partial_init, T=TFP table, A=three axes, I=inverse, P=pre-scramble. **Default: -TAIP**. Kociemba: Kociemba's [twophase.jar](http://kociemba.org/downloads/twophase.jar)):

    |   Flag   | Unlimited |  21 moves |  20 moves | Init Time |
    |:--------:|:---------:|:---------:|:---------:|:---------:|
    | Kociemba |  28.5 ms  |  53.5 ms  |     ? ms  |    -  ms  |
    |  -T---   |  .638 ms  |  6.47 ms  |  266. ms  |    -  ms  |
    |  -TA--   |  .584 ms  |  1.83 ms  |  54.7 ms  |    -  ms  |
    |  -TAI-   |  .598 ms  |  1.10 ms  |  20.3 ms  |    -  ms  |
    |**-TAIP** |**.633 ms**|**.735 ms**|**4.13 ms**|**197. ms**|
    |  R-AIP   |  2.59 ms  |  2.75 ms  |  11.6 ms  |  65.5 ms  |
    |  RTAIP   |  2.83 ms  |  3.22 ms  |  16.5 ms  |  77.6 ms  |


# File description
- Tools.java Many useful functions, can be excluded.
- Util.java  Definitions and some math tools.
- CubieCube.java  CubieCube, see kociemba's [page](http://kociemba.org/math/cubielevel.htm).
- CoordCube.java  Only for generating tables.
- Search.java  Main.
- MainProgram.java  GUI version.
- pruningValue.txt  For checking whether the pruning table is generated correctly.

# License GPLv3

    Copyright (C) 2017  Shuang Chen

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

