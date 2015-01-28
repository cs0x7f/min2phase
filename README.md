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
- Memory: ~1M with twist-flip-pruning (TFP) table, ~0.5M without TFP table. See [Search.java line 28](https://github.com/cs0x7f/min2phase/blob/master/Search.java#L28).
- Average solving time (CPU: Intel Core i7-2670QM. Flag: F=Full P1 table, T=TFP table, A=three axes, I=inverse, P=pre-scramble. Kociemba: Kociemba's [twophase.jar](http://kociemba.org/twophase.jar)): 

    |   Flag   | Unlimited | 21 moves | 20 moves |
    |:--------:|:---------:|:--------:|:--------:|
    | Kociemba |    45 ms  |   70 ms  | >1000 ms |
    |   TAIP   |   5.6 ms  |  5.7 ms  |   19 ms  |
    |   -AIP   |   7.4 ms  |  7.7 ms  |   37 ms  |
    |   T-IP   |   5.6 ms  |  6.9 ms  |   66 ms  |
    |   TA-P   |   5.6 ms  |  6.3 ms  |   43 ms  |
    |   TA--   |   5.6 ms  |  7.8 ms  |  113 ms  |
    |   -A--   |   7.7 ms  |   13 ms  |  240 ms  |
    |   TAI-   |   5.5 ms  |  6.2 ms  |   44 ms  |
    |   -AI-   |   7.6 ms  |  9.0 ms  |   89 ms  |
    |   T---   |   5.6 ms  |   17 ms  |  700 ms  |
    |   ----   |   7.7 ms  |   30 ms  | >1000 ms |
    |   FAIP   |   4.0 ms  |  3.9 ms  |  4.5 ms  |
    |   FAI-   |   3.8 ms  |  3.7 ms  |  5.4 ms  |
    |   FA--   |   3.7 ms  |  3.8 ms  |  9.1 ms  |
    |   F---   |   3.7 ms  |  3.9 ms  |   32 ms  |

- Initialization Time: 160 ms without TFP table, 240 ms with TFP table.

# File Description
- Tools.java Many useful functions, can be excluded.
- Util.java  Definitions and some math tools.
- CubieCube.java  CubieCube, see kociemba's [page](http://kociemba.org/math/cubielevel.htm).
- CoordCube.java  Only for generating tables.
- Search.java  Main.
- MainProgram.java  GUI version.
- pruningValue.txt  For checking whether the pruning table is generated correctly.

# License GPLv3

    Copyright (C) 2015  Shuang Chen

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

