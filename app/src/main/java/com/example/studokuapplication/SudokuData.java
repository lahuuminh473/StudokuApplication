package com.example.studokuapplication;

import java.util.*;

public class SudokuData {

    public static class PuzzleData {
        public final int[][] puzzle;
        public final int[][] solution;

        public PuzzleData(int[][] puzzle, int[][] solution) {
            this.puzzle = puzzle;
            this.solution = solution;
        }
    }

    static Random rand = new Random();

    // ===== PUBLIC METHOD =====
    public static int[][] generate(String level){

        return generateWithSolution(level).puzzle;
    }

    public static PuzzleData generateWithSolution(String level) {

        int[][] board = new int[9][9];

        fillBoard(board);

        int[][] solution = copyBoard(board);

        int clues;
        if(level.equals("dễ")) clues = 40;
        else if(level.equals("trung bình")) clues = 32;
        else clues = 25;

        removeCells(board, clues);

        return new PuzzleData(board, solution);
    }

    public static PuzzleData generateDailyWithSolution(String dailyKey) {
        long seed = dailyKey == null ? System.currentTimeMillis() : dailyKey.hashCode();
        Random dailyRandom = new Random(seed);

        int[][] board = new int[9][9];
        fillBoard(board, dailyRandom);

        int[][] solution = copyBoard(board);
        int clues = 32;
        removeCells(board, clues, dailyRandom);

        return new PuzzleData(board, solution);
    }

    public static PuzzleData fromCustom(int[][] puzzle) {
        if (puzzle == null || !isBoardShapeValid(puzzle) || !isInitialBoardValid(puzzle)) {
            return null;
        }

        int[][] solved = copyBoard(puzzle);
        if (!solveSingle(solved)) {
            return null;
        }


        return new PuzzleData(copyBoard(puzzle), solved);
    }

    public static String serializeBoard(int[][] board) {
        if (!isBoardShapeValid(board)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(81);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int value = board[i][j];
                if (value < 0 || value > 9) {
                    return null;
                }
                sb.append(value);
            }
        }
        return sb.toString();
    }

    public static int[][] deserializeBoard(String data) {
        if (data == null || data.length() != 81) {
            return null;
        }

        int[][] board = new int[9][9];
        for (int i = 0; i < 81; i++) {
            char c = data.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
            board[i / 9][i % 9] = c - '0';
        }
        return board;
    }

    // ===== STEP 1: FILL FULL BOARD =====
    static boolean fillBoard(int[][] board){
        return fillBoard(board, rand);
    }

    static boolean fillBoard(int[][] board, Random random){

        for(int row=0;row<9;row++){
            for(int col=0;col<9;col++){

                if(board[row][col]==0){

                    List<Integer> nums = new ArrayList<>();
                    for(int i=1;i<=9;i++) nums.add(i);

                    Collections.shuffle(nums, random);

                    for(int num: nums){

                        if(isSafe(board,row,col,num)){

                            board[row][col]=num;

                            if(fillBoard(board, random)) return true;

                            board[row][col]=0;
                        }
                    }

                    return false;
                }
            }
        }

        return true;
    }

    // ===== STEP 2: REMOVE CELLS =====
    static void removeCells(int[][] board, int clues){
        removeCells(board, clues, rand);
    }

    static void removeCells(int[][] board, int clues, Random random){

        int remove = 81 - clues;

        while(remove > 0){

            int row = random.nextInt(9);
            int col = random.nextInt(9);

            if(board[row][col]==0) continue;

            int backup = board[row][col];
            board[row][col] = 0;

            int[][] copy = copyBoard(board);

            solutionCount = 0;
            solve(copy);

            if(solutionCount != 1){
                board[row][col] = backup; // restore
            } else {
                remove--;
            }
        }
    }

    // ===== STEP 3: COUNT SOLUTIONS =====
    static int solutionCount = 0;

    static void solve(int[][] board){

        if(solutionCount > 1) return;

        for(int row=0;row<9;row++){
            for(int col=0;col<9;col++){

                if(board[row][col]==0){

                    for(int num=1;num<=9;num++){

                        if(isSafe(board,row,col,num)){

                            board[row][col]=num;

                            solve(board);

                            board[row][col]=0;
                        }
                    }

                    return;
                }
            }
        }

        solutionCount++;
    }

    public static boolean solveSingle(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            if (solveSingle(board)) {
                                return true;
                            }
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    // ===== CHECK VALID =====
    static boolean isSafe(int[][] board,int row,int col,int num){

        for(int i=0;i<9;i++){
            if(board[row][i]==num || board[i][col]==num)
                return false;
        }

        int sr = row - row%3;
        int sc = col - col%3;

        for(int i=sr;i<sr+3;i++){
            for(int j=sc;j<sc+3;j++){
                if(board[i][j]==num)
                    return false;
            }
        }

        return true;
    }

    // ===== COPY =====
    public static int[][] copyBoard(int[][] board){

        int[][] copy = new int[9][9];

        for(int i=0;i<9;i++){
            System.arraycopy(board[i],0,copy[i],0,9);
        }

        return copy;
    }

    static boolean isBoardShapeValid(int[][] board) {
        if (board.length != 9) {
            return false;
        }
        for (int i = 0; i < 9; i++) {
            if (board[i] == null || board[i].length != 9) {
                return false;
            }
        }
        return true;
    }

    static boolean isInitialBoardValid(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = board[row][col];
                if (value < 0 || value > 9) {
                    return false;
                }
                if (value == 0) {
                    continue;
                }
                board[row][col] = 0;
                boolean valid = isSafe(board, row, col, value);
                board[row][col] = value;
                if (!valid) {
                    return false;
                }
            }
        }
        return true;
    }
}