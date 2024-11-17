package org.example.demo;

public class Game {

    // row length
    int row;

    // col length
    int col;

    // board content
    int[][] board;

    public Game(int[][] board) {
        this.board = board;
        this.row = board.length;
        this.col = board[0].length;
    }

    // judge the validity of an operation
    public boolean judge(int row1, int col1, int row2, int col2){

        // Check if either cell is empty
        if (board[row1][col1] == 0 || board[row2][col2] == 0) {
            return false;
        }

        if ((board[row1][col1] != board[row2][col2]) || (row1 == row2 && col1 == col2)) {
            return false;
        }

        // one line
        if (isDirectlyConnected(row1, col1, row2, col2, board)) {
            return true;
        }

        // two lines
        if((row1 != row2) && (col1 != col2)){
            if(board[row1][col2] == 0 && isDirectlyConnected(row1, col1, row1, col2, board)
            && isDirectlyConnected(row1, col2, row2, col2, board))
                return true;
            if(board[row2][col1] == 0 && isDirectlyConnected(row2, col2, row2, col1, board)
            && isDirectlyConnected(row2, col1, row1, col1, board))
                return true;
        }

        // three lines
        if(row1 != row2)
            for (int i = 0; i < board[0].length; i++) {
                if (board[row1][i] == 0 && board[row2][i] == 0 &&
                        isDirectlyConnected(row1, col1, row1, i, board) && isDirectlyConnected(row1, i, row2, i, board)
                        && isDirectlyConnected(row2, col2, row2, i, board)){
                    return true;
                }
            }
        if(col1 != col2)
            for (int j = 0; j < board.length; j++){
                if (board[j][col1] == 0 && board[j][col2] == 0 &&
                        isDirectlyConnected(row1, col1, j, col1, board) && isDirectlyConnected(j, col1, j, col2, board)
                        && isDirectlyConnected(row2, col2, j, col2, board)){
                    return true;
                }
            }

        return false;
    }

    // judge whether
    private boolean isDirectlyConnected(int row1, int col1, int row2, int col2, int[][] board) {
        if (row1 == row2) {
            int minCol = Math.min(col1, col2);
            int maxCol = Math.max(col1, col2);
            for (int col = minCol + 1; col < maxCol; col++) {
                if (board[row1][col] != 0) {
                    return false;
                }
            }
            return true;
        } else if (col1 == col2) {
            int minRow = Math.min(row1, row2);
            int maxRow = Math.max(row1, row2);
            for (int row = minRow + 1; row < maxRow; row++) {
                if (board[row][col1] != 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Method to check if there are any valid moves left
    public boolean hasAvailableMoves() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                // Skip empty cells
                if (board[i][j] == 0) {
                    continue;}

                //if only one element left, game over
                if(countElement() <=1){
                    return false;
                }
                // Check each piece for a possible match with any other piece
                for (int x = 0; x < row; x++) {
                    for (int y = 0; y < col; y++) {
                        // Skip if same cell or empty
                        if ((i == x && j == y) || board[x][y] == 0) continue;

                        // Check if there is a valid match
                        if (judge(i, j, x, y)) {
                            return true; // Move available
                        }
                    }
                }

            }
        }
        return false; // No moves available
    }

    //count number of non-zero element in the game
    public int countElement(){
        int count = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (board[i][j] != 0) {
                    count++;
                }
            }
        }
        return count;
    }

}
