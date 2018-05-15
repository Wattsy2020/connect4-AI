# connect4-AI
Artificial intelligence that plays connect4

How it works:
1. The MiniMax algorithm - an algorithm that finds the best path for the maximising player assuming the minimising player takes the best path available to them
2. Alpha-beta pruning - a tree pruning method that greatly increases the efficiency of the MiniMax algorithm
3. Iterative deepening - the algorithm analyses a tree of depth 1, then depth 2 etc... until analysing the next depth is too costly. This is a cost efficient way to manage the time the algorithm takes
4. It evaluates positions that aren't won or lost by tallying the number of adjacent checkers each side has

How to use it:
1. Download the connect4ai.jar file from the releases tab
2. Open the command line and type
>java -jar "C:\path to connect4ai.jar"
