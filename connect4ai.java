package connect4ai;

import java.util.ArrayList;

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
}

class Position{
    //the players colour, if analysePosition returns this the position is lost
    public static int lossColour;
    //whether the position is won or lost: -1 = loss, 0 = even, 1 = won
    public int state;
    //represents the board as a multidimensional array, 0 = red checker 1 = blue checker 2 = empty
    public int[][] board = new int[6][7];
    //references to parent and children positions, useful for the minimax algorithm
    public Position parent;
    public ArrayList<Position> children = new ArrayList<>();
    
    //explicitly define a position (only used for startingPosition or sample positions)
    public Position(int[][] array, int result){
        for(int i = 0; i < 6; i++){
            System.arraycopy(array[i], 0, board[i], 0, array[i].length);
        }
        if (result == 2){state = 0;}
        else if (result == lossColour){state = -1;}
        else{state = 1;}
    }
    
    //create a position by placing a checker in the parent position
    public Position(Position Parent, int[] move, int colour){
        parent = Parent;
        for(int i = 0; i < 6; i++){
            System.arraycopy(Parent.board[i], 0, board[i], 0, Parent.board[i].length);
        }
        board[move[0]][move[1]] = colour;
        
        int result = Analysis.analysePosition(board, move);
        if (result == 2){state = 0;}
        else if (result == lossColour){state = -1;}
        else{state = 1;}
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
        System.out.println();
    }
}

class gameTree{
    //A multidimensional Arraylist containing positions in the tree organised by levels
    private ArrayList<ArrayList<Position>> positions = new ArrayList<>();
    //the current position of the rootNode in the form Level, Levelposition
    private int[] rootNode = {0,0};
    //the largest size a level can be without taking too long to analyse
    private final int levelLimit = 1000000;
    
    //Generates the positions possible in the next move from the input position
    private ArrayList<Position> genNextPositions(Position parent, int colour){
        ArrayList<Position> nextPositions = new ArrayList<>();
        
        for(int i = 0; i < 7; i++){
            if (parent.board[0][i] != 2){ continue;} //don't add a checker in a full column
            
            //find lowestEmptySpace
            int lowestEmptySpace = 5;
            while (parent.board[lowestEmptySpace][i] != 2){
                lowestEmptySpace--;
            }
            
            //Create new position
            int[] move = {lowestEmptySpace, i};
            Position newPos = new Position(parent, move, colour);
            
            //Append new position to arrayLists
            nextPositions.add(newPos);
            parent.children.add(newPos);
        }
        return nextPositions;
    }
    
    //Adds a new level to the tree by calling genNextPositions() on the current level
    private void genNewLevel(int colour){
        ArrayList<Position> newLevel = new ArrayList<>();
        int Levels = positions.size() - 1;
        int prevLevelSize = positions.get(Levels).size();
        
        for(int i = 0; i < prevLevelSize; i++){
            Position parent = positions.get(Levels).get(i);
            
            if (parent.state == 0){ //if the position isn't won or lost
                ArrayList<Position> children = genNextPositions(parent, colour);
                
                //add children positions to newLevel
                for(int j = 0; j < children.size(); j++){
                    newLevel.add(children.get(j));
                }
            }
        }
        positions.add(newLevel);
    }
    
    //initialises the gameTree by repeatedly calling genNewLevel()
    public gameTree(int depth, Position startPosition, int colour){
        //define the first level
        ArrayList<Position> startLevel = new ArrayList<>();
        startLevel.add(startPosition);
        positions.add(startLevel);
        
        //call genNewLevel() until depth is reached
        for(int i = 0; i < depth; i++) {
            genNewLevel(colour);
            if (colour == 1){colour = 0;} //update the colour
            else {colour = 1;}
        }
    }
    
    //replaces positions with a new ArrayList containing the descendants of the new rootNode
    private void narrowTree(){
        //define the newPositions ArrayList
        ArrayList<ArrayList<Position>> newPositions = new ArrayList<>();
        ArrayList<Position> startLevel = new ArrayList<>();
        startLevel.add(positions.get(rootNode[0]).get(rootNode[1]));
        newPositions.add(startLevel);
        
        //create each level based on the last one
        for(int i = 0; i < positions.size() - 2; i++){
            ArrayList<Position> newLevel = new ArrayList<>();
            
            //loop through each position in the previous level
            for(int j = 0; j < newPositions.get(i).size(); j++){
                //loop through each child
                for(int k = 0; k < newPositions.get(i).get(j).children.size(); k++){
                    newLevel.add(newPositions.get(i).get(j).children.get(k));
                }
            }
            newPositions.add(newLevel);
        }
        
        positions = newPositions;
    }
}

class Connect4ai {
    public static void main(String[] args) {
        Position.lossColour = 0;
        int[][] startPosArray = {{2,2,2,2,2,2,2}, 
                                 {2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2},
                                 {2,2,2,2,2,2,2}};
              
        int[][] samplePosArray = {{1,2,2,2,2,2,2},
                                  {0,2,2,2,2,2,1},
                                  {1,1,2,2,2,1,1},
                                  {0,0,1,2,1,0,1},
                                  {0,1,1,0,0,1,0},
                                  {0,1,0,0,0,1,1}};
                
        Position startPos = new Position(startPosArray, 2);
        Position samplePos = new Position(samplePosArray, 2);
        gameTree tree = new gameTree(7, startPos, 1);
        
        
    }
}
