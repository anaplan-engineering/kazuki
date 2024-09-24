# Game of Life

An example specification of Conway's Game of Life in Kazuki, based on a version written
in [VDM](https://www.overturetool.org/download/examples/VDMSL/ConwayGameLifeSL/).

Conway's Game of Life is a theoretical system embedded into a two-dimensional square grid.
Each square is either alive or dead (sometimes called "empty").
Each new generation is determined based only on the current generation's state.
Each square on the grid interacts with it's eight neighbours (horizontal, vertical and diagonal).
The rules for this are as follows:

- Any live square with less than two live neighbours dies
- Any live square with more than three live neighbours dies
- Any live square with two or three neighbours survives into the next generation
- Any empty square with three neighbours will become a live square

The system is given an intial state uses that to calculate the subsequent generations.
This calculation is repeated, with some systems eventually forming cycles and others eventually dying off.