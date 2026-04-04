package com.example.studokuapplication;

import java.util.Locale;

public class GameHistory {

    public String level;
    public String result;
    public String playedAt;
    public long durationSeconds;

    public GameHistory(String level, String result, String playedAt, long durationSeconds){
        this.level = level;
        this.result = result;
        this.playedAt = playedAt;
        this.durationSeconds = durationSeconds;
    }

    public String formatDuration() {
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

}