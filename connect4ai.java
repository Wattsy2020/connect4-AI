package connect4ai;

import java.util.ArrayList;
import java.util.Scanner;

class Analysis{
    //contains all the analysis methods used by the program
    
    //checks if a line (maximum 7 checkers) contains 4 checkers in a row, returns 0 if red wins, 1 if blue wins, 2 if none
    public static int connected(int[] line){        
        int j = 0;
        int maxTests = line.length - 3;
        while (j < maxTests){
            if (line[j+1] == line[j]){
                if (line[j+2] == line[j]){
                    if (line[j+3] == line[j]){
                        //note if there are 4 connected empty spots it is impossible for there to be another
                        //4 checkers in a row, so returning 2 here is fine
                        return line[j];
                    }
                    j++;
                }
                j++;
            }
            j++;
        }
        return 2;
    }
    
    //Analyse a position created from an even parent (only have to check the modified row, column and diagonals)
    public static int analysePosition(int[][] board, int[] move){
        //check row
        int result = connected(board[move[0]]);
        if (result != 2){ return result;}
        
        //check column
        int[] column = new int[6];
        for(int i = 0; i < 6; i++){
            column[i] = board[i][move[1]];
        }
        result = connected(column);
        if (result != 2){ return result;}
        
        //check top left to bottom right diagonal
        int[] topLeftStart = new int[2]; //coordinates of the start of the diagonal
        int length;
        if (move[0] >= move[1]){
            topLeftStart[0] = move[0] - move[1];
            topLeftStart[1] = 0;
            length = 6 - topLeftStart[0];
        }
        else { 
            topLeftStart[0] = 0;
            topLeftStart[1] = move[1] - move[0];
            length = 7 - topLeftStart[1];
        }
        
        //construct diagonal
        int[] diagonal = new int[length];
        int i = 0;
        while (i < length){
            diagonal[i] = board[topLeftStart[0] + i][topLeftStart[1] + i];
            i++;
        }
        
        result = connected(diagonal);
        if (result != 2){ return result;}
        
        //check top right to bottom left diagonal
        int[] topRightStart = new int[2]; //coordinates of the start of the diagonal
        if (move[0] >= 6 - move[1]){
            topRightStart[0] = move[0] - (6 - move[1]);
            topRightStart[1] = 6;
            length = 6 - topRightStart[0];
        }
        else{
            topRightStart[0] = 0;
            topRightStart[1] = move[1] + move[0];
            length = topRightStart[1] + 1;
        }
        
        //construct diagonal array
        i = 0;
        int[] diagonal2 = new int[length];
        while (i < length){
            diagonal2[i] = board[topRightStart[0] + i][topRightStart[1] - i];
            i++;
        }
        return connected(diagonal2);
    }
    
    //Heuristic to analyse even positions by number of adjacent pieces 
    public static int leafNodeValue(Position pos, int opponentColour){
        int callerScore = 0;
        int opponentScore = 0;
        int sum;
        
        for(int i = 5; i > 0; i--){ //loop through rows from bottom to top
            //stop searching if the row is empty
            sum = 0;
            for (int j = 0; j < 7; j++){
                sum += pos.board[i][j];
            }
            if (sum == 14){return callerScore - opponentScore/2;}
            
            for(int k = 0; k < 6; k++){ //search all columns except the last, so that their is always a column to the right
                //compare to pieces directly above
                if (pos.board[i][k] == pos.board[i-1][k]){
                    if (pos.board[i][k] == opponentColour){opponentScore++;}
                    else if (pos.board[i][k] != 2){callerScore++;}
                }
                
                //compare to pieces diagonally above and to the right
                if (pos.board[i][k] == pos.board[i-1][k+1]){
                    if (pos.board[i][k] == opponentColour){opponentScore++;}
                    else if (pos.board[i][k] != 2){callerScore++;}
                }
                
                //compare to pieces diagonally above and to the left
                if (pos.board[i][k+1] == pos.board[i-1][k]){
                    if (pos.board[i][k+1] == opponentColour){opponentScore++;}
                    else if (pos.board[i][k+1] != 2){callerScore++;}
                }
                
                //compare to pieces to the right
                if (pos.board[i][k] == pos.board[i][k+1]){
                    if (pos.board[i][k] == opponentColour){opponentScore++;}
                    else if (pos.board[i][k] != 2){callerScore++;}
                }
            }
            
            //check the last column
            if (pos.board[i][6] == pos.board[i-1][6]){
                    if (pos.board[i][6] == opponentColour){opponentScore++;}
                    else if (pos.board[i][6] != 2){callerScore++;}
            }
        }
        
        return callerScore - opponentScore/2; //favours gaining adjacent pieces over stopping your opponent
    }
}

class Position{
    //whether the position is won or lost: -100 = loss, 0 = even, 100 = won
    public int state;
    //represents the board as a multidimensional array, 0 = red checker 1 = blue checker 2 = empty
    public int[][] board = new int[6][7];
    //the players colour
    public static int playerColour;
    //references to children positions, useful for the minimax algorithm
    public ArrayList<Position> children = new ArrayList<>();
    public int minChildColumn = 0; //the smallest column a checker could be placed in to create a new child
    private int numChildrenStore = -1; //storage for the number of children, only calculated through numChildren() method
    //useful for minimax algorithm, between -100 and 100
    public int nodeValue;
    //level of the position, needed for iterative deepening
    public int level;
    
    
    //explicitly define a position (only used for startingPosition or sample positions)
    public Position(int[][] array, int result){
        for(int i = 0; i < 6; i++){
            System.arraycopy(array[i], 0, board[i], 0, array[i].length);
        }
        if (result == 2){state = 0;}
        else if (result == playerColour){state = -100;}
        else{state = 100;}
        level = 0;
    }
    
    //create a position by placing a checker in the parent position
    public Position(Position Parent, int[] move, int colour){
        for(int i = 0; i < 6; i++){
            System.arraycopy(Parent.board[i], 0, board[i], 0, Parent.board[i].length);
        }
        board[move[0]][move[1]] = colour;
        level  = Parent.level;
        level++;
        
        int result = Analysis.analysePosition(board, move);
        if (result == 2){state = 0;}
        else if (result == playerColour){state = -100;}
        else{state = 100;}
    }
    
    public void displayPosition(){
        for (int[] board1 : board) {
            for (int j = 0; j < board1.length; j++) {
                switch (board1[j]) {
                    case 0: System.out.printf("[O]");
                    break;
                    case 1: System.out.printf("[X]");
                    break;
                    case 2: System.out.printf("[ ]");
                    break;
                }
            }
            System.out.println();
        }
        System.out.println(" 0  1  2  3  4  5  6");
    }
    
    public int numChildren(){
        if (numChildrenStore != -1){return numChildrenStore;}
        
        int numChildren = 7;
        //check if there are any full columns
        for(int i = 0; i < 7; i++){
            if (board[0][i] != 2){numChildren--;}
        }
        
        numChildrenStore = numChildren; //stores numChildren for future reference
        return numChildren;
    }
}

class gameTree{
    //the root of the gameTree
    public Position root;
    //the largest size a level can be without taking too long to analyse
    private final int levelLimit = 3000000;
    //keeps track of how many levels findBestMove has analysed
    private int currentLevelSize;
    private int depth;
    
    private int updateColour(int colour){
        if (colour == 0){return 1;}
        return 0;
    }
    
    //adds a child to the parent position by placing a checker in minColumn or the next free column after that
    public void addChild(Position parent, int colour){
        //find column to place checker in
        while (parent.board[0][parent.minChildColumn] != 2){ //while column is full
            parent.minChildColumn++;
        }
        
        //create new position
        int lowestEmptySpace = 5;
        while (parent.board[lowestEmptySpace][parent.minChildColumn] != 2){
            lowestEmptySpace--;
        }
        int[] move = {lowestEmptySpace, parent.minChildColumn};
        Position newPos = new Position(parent, move, colour);
        
        parent.children.add(newPos);
        parent.minChildColumn++;
        currentLevelSize++;
    }
    
    public gameTree(Position startPosition){
        root = startPosition;
    }
    
    private int maximiserValue(Position pos, int alpha, int beta){
        //stop recursion if it is won or lost
        if (pos.state != 0){return pos.state;}
        //stop recursion if it is a leaf node
        if (pos.level == depth){return Analysis.leafNodeValue(pos, updateColour(Position.playerColour));}
        
        pos.nodeValue = -100; //initialise nodeValue to worst case scenario
        int numChildren = pos.numChildren();
        for(int i = 0; i < numChildren; i++){
            //generate child if needed
            if (pos.children.size() == i){
                addChild(pos, updateColour(Position.playerColour));
            }
            
            int childValue = minimiserValue(pos.children.get(i), alpha, beta); //find the nodeValue of a child
            
            //if childValue is better than beta the minimiser would never go here
            if (childValue > beta){return childValue;}
            //update nodeValue and alpha if a better option is available
            if (childValue > pos.nodeValue){
                pos.nodeValue = childValue;
                if (childValue > alpha){alpha = childValue;}
            }
        }
        return pos.nodeValue;
    }
    
    private int minimiserValue(Position pos, int alpha, int beta){
        //stop recursion if it is won or lost
        if (pos.state != 0){return pos.state;}
        //stop recursion if it is a leaf node
        if (pos.level == depth){return Analysis.leafNodeValue(pos, Position.playerColour);}
        
        pos.nodeValue = 100; //initialise nodeValue to worst case scenario
        int numChildren = pos.numChildren();
        for(int i = 0; i < numChildren; i++){
            //generate child if needed
            if (pos.children.size() == i){
                addChild(pos, Position.playerColour);
            }
            
            int childValue = maximiserValue(pos.children.get(i), alpha, beta); //find the nodeValue of a child
            
            //if childValue is worse than alpha the maximiser would never go here
            if (childValue < alpha){return childValue;}
            //update nodeValue and beta if a better option is available
            if (childValue < pos.nodeValue){
                pos.nodeValue = childValue;
                if (childValue < beta) {beta = childValue;}
            }
        }
        return pos.nodeValue;
    }
    
    public int findBestMove(){
        root.nodeValue = -100; //initialise nodeValue to worst case scenario
        int bestMove = 0;
        int numChildren = root.numChildren();
        
        //note alpha = best already explored option along the path to the root for the maximiser and beta is the same for the minimiser
        for(int i = 0; i < numChildren; i++){
            //generate child if needed
            if (root.children.size() == i){
                addChild(root, updateColour(Position.playerColour));
            }
            
            int childValue = minimiserValue(root.children.get(i), root.nodeValue, 100);
            
            //update nodeValue if a better option is found
            if (childValue > root.nodeValue) {
                root.nodeValue = childValue;
                bestMove = i;
            }
            if (root.nodeValue == 100) {break;}
        }
        return bestMove;
    }
    
    //iterative deepening algorithm that uses findBestMove
    public Position decideMove(){
        int bestMove = 0;
        int newMove;
        depth = 1;
        
        //keep searching until the next level would have a larger size than the levelLimit
        while (currentLevelSize*5 < levelLimit){
            currentLevelSize = 0;
            
            //update bestMove and depth
            newMove = findBestMove();
            System.out.println("Best Move: " + newMove + " Evaluation: " + root.nodeValue + " Depth: " + depth + " Level size: " + currentLevelSize);
            
            if (root.nodeValue == -100){break;} //if the algorithm thinks the position is lost it won't bother finding a solution, so return the previous bestMove
            bestMove = newMove;
            depth++;
            
            //if there are no more positions to generate we have reached max depth and are done
            if (currentLevelSize == 0){break;}
        }
        return root.children.get(bestMove);
    }
}

class Connect4ai {
    private static Scanner sc = new Scanner(System.in);
    
    public static void main(String[] args) {
        Position.playerColour = 0;
        int[][] startPosArray = {{2,2,2,2,2,2,2}, 
                                 {2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2},
                                 {2,2,2,1,2,2,2}};
              
        int[][] samplePosArray = {{1,2,2,2,2,2,2},
                                  {0,1,2,2,2,2,0},
                                  {1,1,2,2,2,1,1},
                                  {0,0,1,2,1,0,1},
                                  {0,1,1,0,0,1,0},
                                  {0,1,0,0,0,1,1}};
        
        int[][] testPosArray = {{2,2,2,2,2,2,2}, 
                                {2,2,2,2,2,2,2},
                                {2,2,2,2,2,2,2},
                                {2,2,2,0,0,0,2},
                                {2,2,2,2,0,1,1},
                                {2,2,2,2,0,1,1}};
                
        Position startPos = new Position(startPosArray, 2);
        Position samplePos = new Position(samplePosArray, 2);
        Position testPos = new Position(testPosArray, 2);
        
        String Continue;
        do{
            gameLoop(startPos);
            System.out.printf("Continue playing? [Y/N] ");
            Continue = sc.next();
            System.out.println();
        } while (!"N".equals(Continue));
        
    }
    
    private static void gameLoop(Position startPos){
        gameTree tree = new gameTree(startPos);
        Position currentPos = startPos;
        
        System.out.println("Welcome to connect4! Enter your moves by typing a column number between 0 and 6");
        System.out.println("The system evaluates positions from -100 to 100 with 100 being best for the computer");
        currentPos.displayPosition();
        while (true){
            currentPos = getPlayerMove(currentPos);
            currentPos.displayPosition();
            if (currentPos.state == -100){
                System.out.println("You win!");
                break;
            }
            
            tree.root = currentPos;
            currentPos = tree.decideMove();
            currentPos.displayPosition();
            if (currentPos.state == 100){
                System.out.println("You lose");
                break;
            }
        }
    }
    
    private static Position getPlayerMove(Position pos){
        System.out.printf("Enter move: ");
        int column = sc.nextInt();
        System.out.println();
        
        int lowestEmptySpace = 5;
        while (pos.board[lowestEmptySpace][column] != 2){
            lowestEmptySpace--;
        }
        
        int[] move = {lowestEmptySpace, column};
        Position playerPos = new Position(pos, move, Position.playerColour);
        playerPos.level = 0;
        return playerPos;
    }
}
