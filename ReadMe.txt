/********
Nick Luo & Ray Zeng
All group members were present and contributing during all work on this project.
We have neither received nor given any unauthorized aid in this assignment.
********/
ReadMe:
1, Contributor: Nick Luo(Haiyu) Ray Zeng(Tianrui)

2, Name of files: SudokuPlayer.java

3, Explanation of the customSolver:
We have used Forward-checking algorithm for the premise of the customized solver. The forward-checking algorithm will check whether using one value will cause the domain of other values become empty. We have also tried two different heuristics: the least constraint value and the most constraint variables. We believe that an efficient implementation of the two heuristics will reduce the recursive steps a lot and also have much faster run-time. However, our implementation is not very efficient that we use a board called heuristic to store the heuristic value for the choosing next variable and it requires several long for loops to update and select the next variable, which although has less recursive steps, has longer runtime. 

The customized solver perform worse than the AC3 because we believe that forward-checking only check one steps instead of checking all the constraints like a chain in AC3. AC3 in some sense can be viewed a really powerful forward-checking that it check more steps than forward-checking, so AC3 will definitely has less recursive steps.

We have also thought about another heuristic during debugging that this heuristic will start from the most constraint variable and try its neighbor first. This is because of the overlapping property of the neighbors, and also trying neighbors only has 9 cells to check. Having 9 variable in a set is more efficient than viewing the entire board as a whole. However, due to the limited time of both of us : (, we could not implement this heuristic, but this customized solver is good enough to solve the puzzle without any potential bugs. We also have considered what will happen if we add heuristics to AC3? We would like to try implementing heuristics to AC3, but probably just for fun.


 
