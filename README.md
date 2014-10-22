# min2phase
- Rubik's Cube solver or scrambler.
[![Build Status](https://travis-ci.org/cs0x7f/min2phase.svg?branch=master)](https://travis-ci.org/cs0x7f/min2phase)

# two-phase algorithm
- See Kociemba's [page](http://kociemba.org/cube.htm)

# Feature
- Memory: ~1M with twist-flip-pruning table, ~0.5M without twist-flip-pruning table. See [Tools.java line 13](https://github.com/ChenShuang/min2phase/blob/master/Tools.java#L13)
- Average Solving Time @21 moves: ~10ms without T-F-P table, ~7ms with T-F-P table.
- Initialization Time: ~160ms without T-F-P table, ~240ms with T-F-P table.

# File Description
- Tools.java Many  useful functions
- Util.java  Definitions and some math tools.
- CubieCube.java  CubieCube, see kociemba's [page](http://kociemba.org/math/cubielevel.htm).
- CoordCube.java  Only for generating tables.
- Search.java  Main.
- MainProgram.java  GUI version.
- pruningValue.txt  For checking whether the pruning table is generated correctly.

# License GPLv3

    Copyright (C) 2012  Shuang Chen

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

# Some improvements
Conventional two-phase algorithm only find (sub-)optimal solutions to &lt;U,R2,F2,D,L2,B2&gt;. However, If we are able to find more phase1 solutions within a limited depth, the probability of a short solution will increased. 
## Try different axes
The target of phase1 can be either &lt;U,R2,F2,D,L2,B2&gt;, &lt;U2,R,F2,D2,L,B2&gt;, or &lt;U2,R2,F,D2,L2,B&gt;.
## Try the inverse of the state
We will try to solve the inverse state simultaneously to find more phase1 solutions. 
## Try pre-scramble
We can also use pre-scramble technique (which is widely used in fewest-move challenge) to find more phase1 solutions.
