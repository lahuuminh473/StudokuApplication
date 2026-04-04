package com.example.studokuapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Statistics {

    private static final String PREF_NAME = "sudoku_stats";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_BEST_PREFIX = "best_";
    private static final String KEY_ACH_NO_HINT = "ach_no_hint";
    private static final String KEY_ACH_UNDER_5 = "ach_under_5";

    public static ArrayList<GameHistory> historyList = new ArrayList<>();

    public static void load(Context context) {
        if (!historyList.isEmpty()) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                historyList.add(new GameHistory(
                        obj.optString("level", ""),
                        obj.optString("result", ""),
                        obj.optString("playedAt", ""),
                        obj.optLong("durationSeconds", 0)
                ));
            }
        } catch (Exception ignored) {
            historyList.clear();
        }
    }

    public static void addHistory(Context context, GameHistory history) {
        load(context);
        historyList.add(0, history);
        save(context);
    }

    public static void saveBestTimeIfBetter(Context context, String level, long durationSeconds) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = KEY_BEST_PREFIX + level;
        long old = prefs.getLong(key, -1);
        if (old == -1 || durationSeconds < old) {
            prefs.edit().putLong(key, durationSeconds).apply();
        }
    }

    public static long getBestTime(Context context, String level) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_BEST_PREFIX + level, -1);
    }

    public static List<GameHistory> getLeaderboard(Context context, String level) {
        load(context);
        List<GameHistory> wins = new ArrayList<>();
        for (GameHistory h : historyList) {
            if ("thắng".equals(h.result) && level.equals(h.level)) {
                wins.add(h);
            }
        }
        Collections.sort(wins, Comparator.comparingLong(h -> h.durationSeconds));
        return wins;
    }

    public static boolean unlockNoHintAchievement(Context context) {
        return unlockBoolean(context, KEY_ACH_NO_HINT);
    }

    public static boolean unlockUnderFiveAchievement(Context context) {
        return unlockBoolean(context, KEY_ACH_UNDER_5);
    }

    public static boolean hasNoHintAchievement(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ACH_NO_HINT, false);
    }

    public static boolean hasUnderFiveAchievement(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ACH_UNDER_5, false);
    }

    public static String getAchievementSummary(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Achievement\n");
        sb.append(hasNoHintAchievement(context) ? "[x] " : "[ ] ")
                .append("Hoan thanh khong dung hint\n");
        sb.append(hasUnderFiveAchievement(context) ? "[x] " : "[ ] ")
                .append("Giai trong 5 phut");
        return sb.toString();
    }

    private static boolean unlockBoolean(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(key, false)) {
            return false;
        }
        prefs.edit().putBoolean(key, true).apply();
        return true;
    }

    private static void save(Context context) {
        JSONArray arr = new JSONArray();
        for (GameHistory h : historyList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("level", h.level);
                obj.put("result", h.result);
                obj.put("playedAt", h.playedAt);
                obj.put("durationSeconds", h.durationSeconds);
                arr.put(obj);
            } catch (Exception ignored) {
            }
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_HISTORY, arr.toString())
                .apply();
    }

}