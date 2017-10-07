# min2phase
- Rubik's Cube solver or scrambler. [![Build Status](https://travis-ci.org/cs0x7f/min2phase.svg?branch=master)](https://travis-ci.org/cs0x7f/min2phase)



# two-phase algorithm
- See Kociemba's [page](http://kociemba.org/cube.htm)

## Improvements comparing to conventional two-phase algorithm
Conventional two-phase algorithm only find (sub-)optimal solutions to &lt;U,R2,F2,D,L2,B2&gt;. However, If we are able to find more phase1 solutions within a limited depth, the probability of finding a short solution will be increased. 
- Try different axes: The target of phase1 can be either &lt;U,R2,F2,D,L2,B2&gt;, &lt;U2,R,F2,D2,L,B2&gt;, or &lt;U2,R2,F,D2,L2,B&gt;.
- Try the inverse of the state: We will try to solve the inverse state simultaneously to find more phase1 solutions. 
- Try pre-scramble: We can also use pre-scramble technique (which is widely used in fewest-move challenge) to find more phase1 solutions.

# Benchmark
- Memory: ~1M with twist-flip-pruning (TFP) table, ~0.7M without TFP table, ~40M with Full table. See [Search.java line 28](https://github.com/cs0x7f/min2phase/blob/master/Search.java#L28).
- Average solving time (CPU: Intel Core i7-6700HQ. Flag: F=Full P1 table, T=TFP table, A=three axes, I=inverse, P=pre-scramble. **Default: TAIP**. Kociemba: Kociemba's [twophase.jar](http://kociemba.org/downloads/twophase.jar)): 

    |   Flag   | Unlimited | 21 moves | 20 moves |
    |:--------:|:---------:|:--------:|:--------:|
    | Kociemba |  28.5 ms  | 53.5 ms  |    ? ms  |
    |   ----   |  1.41 ms  | 18.0 ms  |    ? ms  |
    |   -A--   |  1.35 ms  | 4.86 ms  | 137. ms  |
    |   -AI-   |  1.33 ms  | 2.77 ms  | 50.6 ms  |
    |   -AIP   |  1.31 ms  | 1.99 ms  | 20.7 ms  |
    |   T---   |  .973 ms  | 9.26 ms  | 337. ms  |
    |   TA--   |  .947 ms  | 2.75 ms  | 70.0 ms  |
    |   TAI-   |  .944 ms  | 1.65 ms  | 26.5 ms  |
    | **TAIP** |**.952 ms**|**1.27 ms**|**10.5 ms**|
    |   F---   |  .485 ms  | .973 ms  | 24.4 ms  |
    |   FA--   |  .509 ms  | .577 ms  | 5.40 ms  |
    |   FAI-   |  .542 ms  | .556 ms  | 2.35 ms  |
    |   FAIP   |  .913 ms  | .910 ms  | 1.65 ms  |

- Initialization Time: 150 ms without TFP table, 220 ms with TFP table, 12 s with Full table.

# File Description
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

