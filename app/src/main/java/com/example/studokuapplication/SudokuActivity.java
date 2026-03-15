package com.example.studokuapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class SudokuActivity extends AppCompatActivity {

    GridLayout grid;
    int mistakeCount = 0;
    int maxMistake = 10;
    EditText[][] cells = new EditText[9][9];

    int[][] puzzle;

    boolean[][] errorCells = new boolean[9][9];

    int selectedRow = -1;
    int selectedCol = -1;
    TextView txtMistake;
    TextView txtLevel;
    String level;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);

        txtLevel = findViewById(R.id.txtLevel);
        grid = findViewById(R.id.gridSudoku);
        txtMistake = findViewById(R.id.txtMistake);
        level = getIntent().getStringExtra("level");
        txtLevel.setText("Level: " + level);

        Random random = new Random();
        int index = random.nextInt(5);

        if(level.equals("dễ")){
            puzzle = SudokuData.easy[index];
        }

        if(level.equals("trung bình")){
            puzzle = SudokuData.medium[index];
        }

        if(level.equals("khó")){
            puzzle = SudokuData.hard[index];
        }

        createBoard();
    }



    void createBoard(){

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){

                EditText cell = new EditText(this);

                cell.setWidth(110);
                cell.setHeight(110);
                cell.setTextSize(22); // chữ lớn hơn
                cell.setTextColor(Color.BLACK); // chữ đậm
                cell.setTypeface(null, android.graphics.Typeface.BOLD); // chữ đậm
                cell.setGravity(Gravity.CENTER);

                cell.setInputType(InputType.TYPE_CLASS_NUMBER);
                cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

                // dùng drawable border
                cell.setBackgroundResource(R.drawable.cell_border);

                int row = i;
                int col = j;

                if(puzzle[i][j] != 0){
                    cell.setText(String.valueOf(puzzle[i][j]));
                    cell.setEnabled(false);
                }

                cell.setOnFocusChangeListener((v,hasFocus)->{

                    if(hasFocus){
                        selectedRow = row;
                        selectedCol = col;
                        refreshBoard();
                    }

                });

                cell.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s,int start,int count,int after){}

                    @Override
                    public void onTextChanged(CharSequence s,int start,int before,int count){}

                    @Override
                    public void afterTextChanged(Editable s){
                        String value = s.toString();

                        // nếu có nhập số thì kiểm tra lỗi để cộng mistake
                        if(!value.equals("")){

                            int num = Integer.parseInt(value);

                            CheckResult result = checkValid(row,col,num);

                            if(!result.valid){

                                mistakeCount++;

                                txtMistake.setText("Số lỗi: "+mistakeCount+"/10");

                                if(mistakeCount >= 10){
                                    String time = new java.text.SimpleDateFormat("dd/MM HH:mm")
                                            .format(new java.util.Date());

                                    Statistics.historyList.add(
                                            new GameHistory(level,"thua",time)
                                    );
                                    showGameOver();
                                }
                            }
                        }


                        updateErrors();

                        refreshBoard();

                        if(checkWin()){
                            String time = new java.text.SimpleDateFormat("dd/MM HH:mm")
                                    .format(new java.util.Date());

                            Statistics.historyList.add(
                                    new GameHistory(level,"thắng",time)
                            );
                           showWinGame();
                        }

                    }
                });

                cells[i][j] = cell;
                grid.addView(cell);

            }
        }
    }



    void updateErrors(){

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){
                errorCells[i][j] = false;
            }
        }

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){

                String value = cells[i][j].getText().toString();

                if(value.equals("")) continue;

                int num = Integer.parseInt(value);

                CheckResult result = checkValid(i,j,num);

                if(!result.valid){
                    errorCells[i][j] = true;
                    errorCells[result.row][result.col] = true;

                }
            }
        }
    }


    void showGameOver(){
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("❌ Bạn đã thua! Sai quá 10 lần")
                .setPositiveButton("OK", (d,w)->{
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .show();
    }
    void showWinGame(){
        new AlertDialog.Builder(this)
                .setTitle("Win")
                .setMessage("🎉 Bạn đã thắng")
                .setPositiveButton("OK", (d,w)->{
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .show();
    }
    void refreshBoard(){

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){

                if(errorCells[i][j]){
                    setCellColor(cells[i][j], "#EF5350");
                }
                else{
                    setCellColor(cells[i][j], "#FFFFFF");
                }

            }
        }

        if(selectedRow==-1) return;

        highlightArea(selectedRow,selectedCol);

        String value = cells[selectedRow][selectedCol].getText().toString();

        if(!value.equals("")){
            highlightSameNumber(Integer.parseInt(value));
        }
    }




    void highlightArea(int row,int col){

        for(int i=0;i<9;i++){

            if(!errorCells[row][i])
                setCellColor(cells[row][i], "#E3F2FD");

            if(!errorCells[i][col])
                setCellColor(cells[i][col], "#E3F2FD");
        }

        int startRow = row - row%3;
        int startCol = col - col%3;

        for(int i=startRow;i<startRow+3;i++){
            for(int j=startCol;j<startCol+3;j++){

                if(!errorCells[i][j])
                    setCellColor(cells[i][j], "#BBDEFB");
            }
        }
    }



    void highlightSameNumber(int num){

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){

                String value = cells[i][j].getText().toString();

                if(!value.equals("") && Integer.parseInt(value)==num){

                    if(!errorCells[i][j])
                        setCellColor(cells[i][j], "#FFF59D");
                }
            }
        }
    }



    void setCellColor(EditText cell,String color){

        GradientDrawable drawable =
                (GradientDrawable) cell.getBackground();

        drawable.setColor(Color.parseColor(color));
    }



    CheckResult checkValid(int row,int col,int num){

        for(int i=0;i<9;i++){

            if(i!=col){

                String value = cells[row][i].getText().toString();

                if(!value.equals("") && Integer.parseInt(value)==num){
                    return new CheckResult(false,row,i);
                }
            }
        }

        for(int i=0;i<9;i++){

            if(i!=row){

                String value = cells[i][col].getText().toString();

                if(!value.equals("") && Integer.parseInt(value)==num){
                    return new CheckResult(false,i,col);
                }
            }
        }

        int startRow = row - row%3;
        int startCol = col - col%3;

        for(int i=startRow;i<startRow+3;i++){
            for(int j=startCol;j<startCol+3;j++){

                if(i!=row||j!=col){

                    String value = cells[i][j].getText().toString();

                    if(!value.equals("") && Integer.parseInt(value)==num){
                        return new CheckResult(false,i,j);
                    }
                }
            }
        }

        return new CheckResult(true,-1,-1);
    }



    boolean checkWin(){

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){

                if(cells[i][j].getText().toString().equals("")){
                    return false;
                }
            }
        }

        return true;
    }

}