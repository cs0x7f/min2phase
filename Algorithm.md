# Two-phase algorithm
- See Kociemba's [page](http://kociemba.org/cube.htm)

## Improvements compared with conventional two-phase algorithm
Conventional two-phase algorithm only find (sub-)optimal solutions to &lt;U,R2,F2,D,L2,B2&gt;. However, If we are able to find more phase1 solutions within a limited depth, the probability of finding a short solution will be increased. 
- Try different axes: The target of phase1 can be either &lt;U,R2,F2,D,L2,B2&gt;, &lt;U2,R,F2,D2,L,B2&gt;, or &lt;U2,R2,F,D2,L2,B&gt;.
- Try the inverse of the state: We will try to solve the inverse state simultaneously to find more phase1 solutions. 
- Try pre-scramble: We can also use pre-scramble technique (which is widely used in fewest-move challenge) to find more phase1 solutions. If PreMoves * Scramble * Phase1 * Phase2 = Solved, then Scramble * (Phase1 * Phase2 * PreMoves) = Solved, Solution = Phase1 * Phase2 * PreMoves.

## Pruning table

	|  Pruning Table   |   Coord1  |Coord1 Size|  Coord2  | Coord2 Size  |   Phase   | Average |
	|:----------------:|:---------:|:---------:|:--------:|:------------:|:---------:|:-------:|
	| UDSliceTwistPrun |  UDSlice  |     495   | TwistSym |  2187 / 324  |     1     |   6.76  |
	|  UDSliceFlipPrun |  UDSlice  |     495   |  FlipSym |  2048 / 336  |     1     |   6.85  |
	|  TwistFlipPrun   |   Flip    |    2048   | TwistSym |  2187 / 324  |     1     |   7.18  |
	|    MCPermPrun    |   MPerm   |      24   | CPermSym | 40320 / 2768 |     2     |   9.69  |
	| EPermCCombPPrun  |   CComb   |     140   | EPermSym | 40320 / 2768 |     2     |   9.31  |

## Coordinates

- UDSlice: Position of 4 edges (FL FR BL BR) without permutation among them.
- Flip: Orientation of all 12 edges.
- Twist: Orientation of all 8 corners.
- CPerm: Permutation of 8 corners.
- EPerm: Permutation of 8 edges in U and D layers.
- CComb: Parity of all edges (or corners), and position of 4 corners (URF UFL ULB UBR) without permutation among them.
