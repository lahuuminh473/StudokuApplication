package com.example.studokuapplication;

public class CheckResult {
    boolean valid;
    int row;
    int col;

    CheckResult(boolean valid,int row,int col){
        this.valid = valid;
        this.row = row;
        this.col = col;
    }
}
