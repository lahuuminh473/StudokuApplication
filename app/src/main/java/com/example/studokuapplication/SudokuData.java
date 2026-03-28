package com.example.studokuapplication;

import java.util.*;

public class SudokuData {

    static Random rand = new Random();

    // ===== PUBLIC METHOD =====
    public static int[][] generate(String level){

        int[][] board = new int[9][9];

        fillBoard(board);

        int clues;
        if(level.equals("dễ")) clues = 40;
        else if(level.equals("trung bình")) clues = 32;
        else clues = 25;

        removeCells(board, clues);

        return board;
    }

    // ===== STEP 1: FILL FULL BOARD =====
    static boolean fillBoard(int[][] board){

        for(int row=0;row<9;row++){
            for(int col=0;col<9;col++){

                if(board[row][col]==0){

                    List<Integer> nums = new ArrayList<>();
                    for(int i=1;i<=9;i++) nums.add(i);

                    Collections.shuffle(nums);

                    for(int num: nums){

                        if(isSafe(board,row,col,num)){

                            board[row][col]=num;

                            if(fillBoard(board)) return true;

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

        int remove = 81 - clues;

        while(remove > 0){

            int row = rand.nextInt(9);
            int col = rand.nextInt(9);

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
    static int[][] copyBoard(int[][] board){

        int[][] copy = new int[9][9];

        for(int i=0;i<9;i++){
            System.arraycopy(board[i],0,copy[i],0,9);
        }

        return copy;
    }
}