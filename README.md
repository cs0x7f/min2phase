# min2phase
- Rubik's Cube solver or scrambler. [![Build Status](https://travis-ci.org/cs0x7f/min2phase.svg?branch=master)](https://travis-ci.org/cs0x7f/min2phase)

# Two-phase algorithm
- See Kociemba's [page](http://kociemba.org/cube.htm)

## Improvements compared with conventional two-phase algorithm
Conventional two-phase algorithm only find (sub-)optimal solutions to &lt;U,R2,F2,D,L2,B2&gt;. However, If we are able to find more phase1 solutions within a limited depth, the probability of finding a short solution will be increased. 
- Try different axes: The target of phase1 can be either &lt;U,R2,F2,D,L2,B2&gt;, &lt;U2,R,F2,D2,L,B2&gt;, or &lt;U2,R2,F,D2,L2,B&gt;.
- Try the inverse of the state: We will try to solve the inverse state simultaneously to find more phase1 solutions. 
- Try pre-scramble: We can also use pre-scramble technique (which is widely used in fewest-move challenge) to find more phase1 solutions.

## Compilation options
There are several compilation options that can be modified for different purposes, e.g. faster solving speed, or less resource usage. 
- USE_TWIST_FLIP_PRUN [Search.java line 28](https://github.com/cs0x7f/min2phase/blob/dev/Search.java#L28): To determine whether twist-flip-pruning table will be generated and used. The size of the table is about 300KB (2,048 * 324 entries / 2 entries per byte = 331,776 bytes).
- PARTIAL_INIT_PRUN [Search.java line 42](https://github.com/cs0x7f/min2phase/blob/dev/Search.java#L42): For some situations that initialization speed is bottleneck, e.g. initialization cannot be pre-executed before solving, this option can be set to reduce initialization time by about 50%. However, the solving speed is about 2x slower due to ineffective pruning. However, the initialization will continue during the search, in other word, the solving speed will be increased after solving several cubes.
- TRY_PRE_MOVE, TRY_INVERSE, TRY_THREE_AXES [Search.java line 49](https://github.com/cs0x7f/min2phase/blob/dev/Search.java#L49): Three improvements of min2phase compared with conventional two-phase algorithm. Can be disabled for research purpose. 

# Benchmark
- Memory: ~1M with twist-flip-pruning (TFP) table, ~0.7M without TFP table, ~40M with Full table. See [Search.java line 28](https://github.com/cs0x7f/min2phase/blob/dev/Search.java#L28).
- Average solving time (CPU: Intel Core i7-6700HQ. Flag: F=Full P1 table, R=Partial_init, T=TFP table, A=three axes, I=inverse, P=pre-scramble. **Default: -TAIP**. Kociemba: Kociemba's [twophase.jar](http://kociemba.org/downloads/twophase.jar)):

    |   Flag   | Unlimited |  21 moves |  20 moves | Init Time |
    |:--------:|:---------:|:---------:|:---------:|:---------:|
    | Kociemba |  28.5 ms  |  53.5 ms  |     ? ms  |    -  ms  |
    |  -T---   |  1.02 ms  |  7.05 ms  |  283. ms  |    -  ms  |
    |  -TA--   |  .905 ms  |  2.29 ms  |  56.9 ms  |    -  ms  |
    |  -TAI-   |  .918 ms  |  1.49 ms  |  21.8 ms  |    -  ms  |
    |**-TAIP** |**.998 ms**|**1.06 ms**|**5.61 ms**|**184. ms**|
    |  R-AIP   |  2.89 ms  |  3.12 ms  |  17.7 ms  |  71.1 ms  |
    |  RTAIP   |  3.07 ms  |  3.63 ms  |  23.3 ms  |  78.2 ms  |


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

