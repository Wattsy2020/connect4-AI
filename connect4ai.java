package connect4ai;

import java.util.ArrayList;

class Analysis{
    //contains all the analysis methods used by the program
    
    public static int connected(int[] line){
        //checks if a line (maximum 7 checkers) contains 4 checkers in a row, returns 0 if red wins, 1 if blue wins, 2 if none
        
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
    
    public static int analysePosition(Position pos){
        //Determines whether a positions is won, lost or even
        
        //check rows
        int fullRows = 6;
        for (int i = 0; i < 6; i++){
            //check if row is empty
            int sum = 0;
            for (int j = 0; j < 7; j++){
                sum += pos.board[i][j];
            }           
            if (sum == 14){ //i.e. there are 7 twos so the row is empty
                fullRows -= 1;
            }
            
            int result = connected(pos.board[i]);
            if (result != 2){
                return result;
            }
        }
        
        if (fullRows < 4){ //can't have 4 in a row in a column/diagonal if less than 4 rows
            return 2;
        }
        
        //check columns
        int[] column = new int[fullRows];
        for (int j = 0; j < 7; j++){
            //construct the column array from bottom row to top
            for (int k = 5; k > 5 - fullRows; k--){
                column[5 - k] = pos.board[k][j];
            }
            
            int result = connected(column);
            if (result != 2){
                return result;
            }
        }
        
        //check diagonals from [0,0] to [6,6] then [1,0] to [6,5] then [2,0] to [6,4]
        for (int m = 0; m < 3; m++){
            //construct diagonal array
            int[] diagonal = new int [6 - m];
            int i = 0;
            for (int n = 6 - fullRows; n < 6; n++){
                if ((n - m) < 0){
                    continue;
                }
                diagonal[i] = pos.board[n][n-m];
                i++;
            }
            
            int result = connected(diagonal);
            if (result != 2){
                return result;
            }           
        }
        
        //check diagonals from [0, 1] to [6,7] than [0,2] to [5,7] then [0,3] to [4,7]
        for (int q = 1; q < 4; q++){
            int[] diagonal = new int[1 + fullRows - q];
            for (int r = 6 - fullRows; r < (7 - q); r++){
                diagonal[r + fullRows - 6] = pos.board[r][q + r];
            }
            
            int result = connected(diagonal);
            if (result != 2){
                return result;
            }
        }
        
        //check diagonals from [0, 7] to [6,1] than [1, 7] to [6,2] ...
        for (int o = 0; o < 3; o++){
            int[] diagonal = new int [6 - o];
            int i = 0;
            for (int p = 6 - fullRows; p < 6; p++){
                if ((p - o) < 0){
                    continue;
                }
                diagonal[i] = pos.board[p][6 - (p - o)];
                i++;
            }
            
            int result = connected(diagonal);
            if (result != 2){
                return result;
            }           
        }
        
        //check diagonals from [0, 6] to [6,0] than [0,5] to [5,0] then [0,3] to [4,7]
        for (int s = 1; s < 4; s++){
            int[] diagonal = new int[1 + fullRows - s];
            for (int t = 6 - fullRows; t < (7 - s); t++){
                diagonal[t + fullRows - 6] = pos.board[t][6 - (s + t)];
            }
            
            int result = connected(diagonal);
            if (result != 2){
                return result;
            }
        }
        
        return 2;
    }
}

class Position{
    //the players colour, if analysePosition returns this the position is lost
    public static int lossColour;
    //whether the position is won or lost: -1 = loss, 0 = even, 1 = won
    public int State;
    //represents the board as a multidimensional array, 0 = red checker 1 = blue checker 2 = empty
    public int[][] board = new int[6][7];
    
    public Position(int[][] array){
        for(int i = 0; i < array.length; i++){
            System.arraycopy(array[i], 0, board[i], 0, array[i].length);
        }
        int result = Analysis.analysePosition(this);
        if (result == 2){State = 0;}
        else if (result == lossColour){State = -1;}
        else{State = 1;}
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
    //stores the index of the current root node of the tree
    private int[] rootNode = {0,0};
    
    //Generates the positions possible in the next move from the input position
    public ArrayList<Position> genNextPositions(Position pos, int colour){
        ArrayList<Position> nextPositions = new ArrayList<>();
        for(int i = 0; i < 7; i++){
            if (pos.board[0][i] != 2){continue;} //don't add a checker in a full column
            
            int lowestEmptySpace = 5;
            while (pos.board[lowestEmptySpace][i] != 2){
                lowestEmptySpace--;
            }
            pos.board[lowestEmptySpace][i] = colour;
            
            Position newPos = new Position(pos.board);
            nextPositions.add(newPos);
            pos.board[lowestEmptySpace][i] = 2; //reset position for the next iteration
        }
        return nextPositions;
    }
    
    //Adds a new level to the tree by calling genNextPositions() on the current level
    public void genNewLevel(){
        //need to think of a way to associate each position with it's parent
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
                                  {0,2,2,2,2,2,2},
                                  {1,1,2,2,2,1,1},
                                  {0,0,1,2,1,0,1},
                                  {0,1,1,0,0,1,0},
                                  {0,1,0,0,0,1,1}};
                
        Position startPos = new Position(startPosArray);
        Position samplePos = new Position(samplePosArray);
        gameTree tree = new gameTree();
        
        
    }
}
