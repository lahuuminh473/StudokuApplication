package com.example.studokuapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SudokuActivity extends AppCompatActivity {

    private static final String BOX_CELEBRATION_ASSET = "Success animation.json";

    GridLayout grid;
    int mistakeCount = 0;
    int maxMistake = 10;
    EditText[][] cells = new EditText[9][9];
    boolean[][] fixedCells = new boolean[9][9];
    boolean[][] celebratedBoxes = new boolean[3][3];
    boolean isAutoCandidateEnabled = false;
    int[][] puzzle;
    int[][] solution;

    boolean[][] errorCells = new boolean[9][9];

    int selectedRow = -1;
    int selectedCol = -1;
    TextView txtMistake;
    TextView txtLevel;
    TextView txtTimer;
    TextView txtBestTime;
    Button btnHint;
    LottieAnimationView boxCelebrateAnimation;

    String level;
    String gameMode;
    String dailyKey;

    long startElapsedRealtime;
    long elapsedSeconds;
    boolean gameFinished = false;
    int hintsUsed = 0;
    boolean isCelebrationPlaying = false;

    final Runnable hideCelebrationRunnable = () -> stopCelebrationAnimation();

    final Handler timerHandler = new Handler(Looper.getMainLooper());
    final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedSeconds = (SystemClock.elapsedRealtime() - startElapsedRealtime) / 1000;
            txtTimer.setText("Timer: " + formatDuration(elapsedSeconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemePrefs.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);

        txtLevel = findViewById(R.id.txtLevel);
        grid = findViewById(R.id.gridSudoku);
        txtMistake = findViewById(R.id.txtMistake);
        txtTimer = findViewById(R.id.txtTimer);
        txtBestTime = findViewById(R.id.txtBestTime);
        btnHint = findViewById(R.id.btnHint);
        boxCelebrateAnimation = findViewById(R.id.boxCelebrateAnimation);
        if (boxCelebrateAnimation != null) {
            boxCelebrateAnimation.setFailureListener(
                    throwable -> Toast.makeText(this, "Khong tai duoc animation", Toast.LENGTH_SHORT).show()
            );
        }

        level = getIntent().getStringExtra("level");
        if (level == null) {
            level = "dễ";
        }
        gameMode = getIntent().getStringExtra("gameMode");

        Statistics.load(this);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);

        String customPuzzleData = getIntent().getStringExtra("customPuzzle");
        SudokuData.PuzzleData gameData;

        if (customPuzzleData != null) {
            gameMode = "custom";
            int[][] customPuzzle = SudokuData.deserializeBoard(customPuzzleData);
            gameData = SudokuData.fromCustom(customPuzzle);
        } else if ("daily".equals(gameMode) || "daily".equals(level)) {
            gameMode = "daily";
            level = "daily";
            dailyKey = getTodayKey();
            gameData = SudokuData.generateDailyWithSolution(dailyKey);
        } else {
            gameMode = "normal";
            gameData = SudokuData.generateWithSolution(level);
        }

        if ("daily".equals(gameMode)) {
            txtLevel.setText("Daily Challenge - " + getTodayDisplay());
        } else {
            txtLevel.setText("Level: " + level);
        }

        if (gameData == null) {
            Toast.makeText(this, "Đề không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        puzzle = gameData.puzzle;
        solution = gameData.solution;

        startElapsedRealtime = SystemClock.elapsedRealtime();
        updateBestTime();

        btnHint.setOnClickListener(v -> useHint());
        Button btnAutoCandidate = findViewById(R.id.btnAutoCandidate);

        btnAutoCandidate.setOnClickListener(v -> {
            isAutoCandidateEnabled = !isAutoCandidateEnabled;

            btnAutoCandidate.setText(isAutoCandidateEnabled ? "Auto-Candidate: ON" : "Auto-Candidate: OFF");

            updateAutoCandidates();
        });
        createBoard();
    }



    void createBoard(){

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){

                EditText cell = new EditText(this);

                cell.setWidth(110);
                cell.setHeight(110);
                cell.setTextSize(22);
                cell.setTextColor(ContextCompat.getColor(this, R.color.sudoku_text));
                cell.setTypeface(null, Typeface.BOLD);
                cell.setGravity(Gravity.CENTER);

                cell.setInputType(InputType.TYPE_CLASS_NUMBER);
                cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                cell.setKeyListener(DigitsKeyListener.getInstance("123456789"));
                cell.setHintTextColor(ContextCompat.getColor(this, R.color.sudoku_sub_text));

                // dùng drawable border
                cell.setBackgroundResource(R.drawable.cell_border);

                int row = i;
                int col = j;

                if(puzzle[i][j] != 0){
                    cell.setText(String.valueOf(puzzle[i][j]));
                    cell.setEnabled(false);
                    fixedCells[i][j] = true;
                }

                cell.setOnFocusChangeListener((v,hasFocus)->{

                    if(hasFocus){
                        selectedRow = row;
                        selectedCol = col;
                        refreshBoard();
                        updateAutoCandidates();
                    }

                });

                cell.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s,int start,int count,int after){}

                    @Override
                    public void onTextChanged(CharSequence s,int start,int before,int count){}

                    @Override
                    public void afterTextChanged(Editable s){
                        if (gameFinished) {
                            return;
                        }

                        String value = s.toString();

                        if (!value.equals("")) {
                            int num = Integer.parseInt(value);
                            if (num < 1 || num > 9) {
                                s.clear();
                                return;
                            }

                            CheckResult result = checkValid(row,col,num);

                            if(!result.valid){

                                mistakeCount++;
                                playWrongSound();

                                txtMistake.setText("Số lỗi: "+mistakeCount+"/10");

                                if(mistakeCount >= maxMistake){
                                    long duration = getCurrentDuration();
                                    String playedAt = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                                            .format(new Date());
                                    Statistics.addHistory(
                                            thisActivity(),
                                            new GameHistory(level,"thua",playedAt,duration)
                                    );
                                    gameFinished = true;
                                    stopTimer();
                                    showGameOver();
                                    return;
                                }
                            } else {
                                playCorrectSound();
                            }
                        }


                        updateErrors();

                        refreshBoard();
                        updateAutoCandidates();

                        checkAndCelebrateCompletedBox(row, col);

                        if(checkWin()){
                            long duration = getCurrentDuration();
                            String playedAt = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                                    .format(new Date());
                            Statistics.addHistory(
                                    thisActivity(),
                                    new GameHistory(level,"thắng",playedAt,duration)
                            );
                            Statistics.saveBestTimeIfBetter(thisActivity(), level, duration);
                            boolean unlockedNoHint = false;
                            boolean unlockedFast = false;
                            if (hintsUsed == 0) {
                                unlockedNoHint = Statistics.unlockNoHintAchievement(thisActivity());
                            }
                            if (duration <= 300) {
                                unlockedFast = Statistics.unlockUnderFiveAchievement(thisActivity());
                            }

                            if (unlockedNoHint || unlockedFast) {
                                StringBuilder text = new StringBuilder("Mo khoa Achievement:\n");
                                if (unlockedNoHint) {
                                    text.append("- Hoan thanh khong dung hint\n");
                                }
                                if (unlockedFast) {
                                    text.append("- Giai trong 5 phut");
                                }
                                Toast.makeText(thisActivity(), text.toString().trim(), Toast.LENGTH_LONG).show();
                            }

                            updateBestTime();
                            gameFinished = true;
                            stopTimer();
                            showWinGame();
                        }

                    }
                });

                cells[i][j] = cell;
                grid.addView(cell);

            }
        }

        refreshBoard();
        initCelebratedBoxesState();
        updateAutoCandidates();
        startTimer();
    }

    void updateAutoCandidates() {

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {

                EditText cell = cells[row][col];
                if (cell == null) continue;

                //  Nếu tắt → xóa hết hint
                if (!isAutoCandidateEnabled) {
                    cell.setHint("");
                    continue;
                }

                //  Nếu không phải ô đang chọn → không hiện
                if (row != selectedRow || col != selectedCol) {
                    cell.setHint("");
                    continue;
                }

                // Nếu ô đã có số → không hiện
                if (!cell.getText().toString().isEmpty()) {
                    cell.setHint("");
                    continue;
                }

                //  Chỉ còn đúng ô đang focus và trống
                String candidates = buildCandidates(row, col);
                cell.setHint(candidates);
            }
        }
    }

    String buildCandidates(int row, int col) {
        boolean[] used = new boolean[10];

        for (int i = 0; i < 9; i++) {
            String rowValue = cells[row][i].getText().toString();
            if (!rowValue.isEmpty()) {
                int value = Integer.parseInt(rowValue);
                if (value >= 1 && value <= 9) {
                    used[value] = true;
                }
            }

            String colValue = cells[i][col].getText().toString();
            if (!colValue.isEmpty()) {
                int value = Integer.parseInt(colValue);
                if (value >= 1 && value <= 9) {
                    used[value] = true;
                }
            }
        }

        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                String boxValue = cells[i][j].getText().toString();
                if (!boxValue.isEmpty()) {
                    int value = Integer.parseInt(boxValue);
                    if (value >= 1 && value <= 9) {
                        used[value] = true;
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int num = 1; num <= 9; num++) {
            if (!used[num]) {
                sb.append(num);
            }
        }

        return sb.toString();
    }

    void initCelebratedBoxesState() {
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                celebratedBoxes[boxRow][boxCol] = isBoxSolved(boxRow, boxCol);
            }
        }
    }

    void checkAndCelebrateCompletedBox(int row, int col) {
        int boxRow = row / 3;
        int boxCol = col / 3;

        if (celebratedBoxes[boxRow][boxCol]) {
            return;
        }

        if (isBoxSolved(boxRow, boxCol)) {
            celebratedBoxes[boxRow][boxCol] = true;
            showBoxCelebration();
        }
    }

    boolean isBoxSolved(int boxRow, int boxCol) {
        int startRow = boxRow * 3;
        int startCol = boxCol * 3;

        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                String value = cells[i][j].getText().toString();
                if (value.isEmpty()) {
                    return false;
                }

                int num = Integer.parseInt(value);
                if (solution != null && num != solution[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    void showBoxCelebration() {
        if (isFinishing() || boxCelebrateAnimation == null || gameFinished) {
            return;
        }

        if (isCelebrationPlaying) {
            return;
        }

        try {
            isCelebrationPlaying = true;
            boxCelebrateAnimation.setVisibility(View.VISIBLE);
            boxCelebrateAnimation.cancelAnimation();
            boxCelebrateAnimation.setProgress(0f);
            boxCelebrateAnimation.setAnimation(BOX_CELEBRATION_ASSET);
            boxCelebrateAnimation.setRepeatCount(0);
            boxCelebrateAnimation.setSpeed(1f);
            boxCelebrateAnimation.playAnimation();

            // Keep animation visible long enough to feel celebratory.
            timerHandler.removeCallbacks(hideCelebrationRunnable);
            timerHandler.postDelayed(hideCelebrationRunnable, 1600);
        } catch (Exception e) {
            isCelebrationPlaying = false;
            Toast.makeText(this, "Hoan thanh o 3x3!", Toast.LENGTH_SHORT).show();
        }
    }

    void stopCelebrationAnimation() {
        if (boxCelebrateAnimation != null) {
            boxCelebrateAnimation.cancelAnimation();
            boxCelebrateAnimation.setVisibility(View.GONE);
        }
        isCelebrationPlaying = false;
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
                .setMessage("❌ Bạn đã thua! Sai quá 10 lần\nThời gian: " + formatDuration(getCurrentDuration()))
                .setPositiveButton("OK", (d,w)->{
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .show();
    }
    void showWinGame(){
        new AlertDialog.Builder(this)
                .setTitle("Win")
                .setMessage("🎉 Bạn đã thắng\nThời gian: "
                        + formatDuration(getCurrentDuration())
                        + "\nHint đã dùng: " + hintsUsed)
                .setCancelable(false) // 👈 QUAN TRỌNG
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
                    setCellColor(cells[i][j], ContextCompat.getColor(this, R.color.cell_error));
                }
                else{
                    setCellColor(cells[i][j], ContextCompat.getColor(this, R.color.cell_default));
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
                setCellColor(cells[row][i], ContextCompat.getColor(this, R.color.cell_row_col));

            if(!errorCells[i][col])
                setCellColor(cells[i][col], ContextCompat.getColor(this, R.color.cell_row_col));
        }

        int startRow = row - row%3;
        int startCol = col - col%3;

        for(int i=startRow;i<startRow+3;i++){
            for(int j=startCol;j<startCol+3;j++){

                if(!errorCells[i][j])
                    setCellColor(cells[i][j], ContextCompat.getColor(this, R.color.cell_box));
            }
        }
    }



    void highlightSameNumber(int num){

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){

                String value = cells[i][j].getText().toString();

                if(!value.equals("") && Integer.parseInt(value)==num){

                    if(!errorCells[i][j])
                        setCellColor(cells[i][j], ContextCompat.getColor(this, R.color.cell_same_number));
                }
            }
        }
    }



    void setCellColor(EditText cell,int color){

        GradientDrawable drawable =
                (GradientDrawable) cell.getBackground();

        drawable.setColor(color);
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

                String value = cells[i][j].getText().toString();
                if(value.equals("")){
                    return false;
                }

                int num = Integer.parseInt(value);
                if (!checkValid(i, j, num).valid) {
                    return false;
                }
            }
        }

        return true;
    }

    void useHint() {
        if (gameFinished) {
            return;
        }

        int row = selectedRow;
        int col = selectedCol;

        boolean canUseSelected = row >= 0 && col >= 0 && !fixedCells[row][col]
                && cells[row][col].getText().toString().isEmpty();

        if (!canUseSelected) {
            row = -1;
            col = -1;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (!fixedCells[i][j] && cells[i][j].getText().toString().isEmpty()) {
                        row = i;
                        col = j;
                        break;
                    }
                }
                if (row != -1) {
                    break;
                }
            }
        }

        if (row == -1) {
            Toast.makeText(this, "Không còn ô trống để gợi ý", Toast.LENGTH_SHORT).show();
            return;
        }

        cells[row][col].setText(String.valueOf(solution[row][col]));
        selectedRow = row;
        selectedCol = col;
        hintsUsed++;
        playCorrectSound();
        refreshBoard();
        updateAutoCandidates();
        checkAndCelebrateCompletedBox(row, col);
    }

    String getTodayKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
    }

    String getTodayDisplay() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    void updateBestTime() {
        long best = Statistics.getBestTime(this, level);
        if (best < 0) {
            txtBestTime.setText("Best: --:--");
        } else {
            txtBestTime.setText("Best: " + formatDuration(best));
        }
    }

    long getCurrentDuration() {
        return (SystemClock.elapsedRealtime() - startElapsedRealtime) / 1000;
    }

    String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long remainSeconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainSeconds);
    }

    void startTimer() {
        stopTimer();
        timerHandler.post(timerRunnable);
    }

    void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    void playCorrectSound() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 70);
        }
    }

    void playWrongSound() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 120);
        }
    }

    SudokuActivity thisActivity() {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gameFinished) {
            startTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        timerHandler.removeCallbacks(hideCelebrationRunnable);
        stopCelebrationAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(hideCelebrationRunnable);
        stopCelebrationAnimation();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }

}