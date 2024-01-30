package com.example.reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Reminder {
    private int id;
    private int genreId;
    private String title;

    public Reminder(int genreId, String title) {
        this.genreId = genreId;
        this.title = title;
    }

    public Reminder(int id, int genreId, String title) {
        this.id = id;
        this.genreId = genreId;
        this.title = title;
    }

    public Reminder() {

    }

    public Reminder(int genreId) {
        this.genreId = genreId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static class ReminderDBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "reminder.db";    // DBファイル
        private static final int DB_VERSION = 1;    // バージョン

        public ReminderDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE reminders (")
                    .append("id INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append("genre_id INTEGER,")
                    .append("title TEXT")
                    .append(");");
            String sql = sb.toString();

            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        public List<Reminder> getAllReminders() {
            List<Reminder> reminders = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            String sql = "SELECT * FROM reminders";
            Cursor cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex("id");
                int genreIdIndex = cursor.getColumnIndex("genre_id");
                int titleIndex = cursor.getColumnIndex("title");
                Reminder reminder = new Reminder(
                        cursor.getInt(idIndex),
                        cursor.getInt(genreIdIndex),
                        cursor.getString(titleIndex)
                );
                reminders.add(reminder);
            }
            return reminders;
        }

        public List<Reminder> getRemindersByGenreId(int genreId) {
            Log.d("getRemindersByGenreIdです", "ジャンルから探します.");
            List<Reminder> reminders = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String[] args = {String.valueOf(genreId)};

            String sql = "SELECT * FROM reminders WHERE genre_id = ?";
            Cursor cursor = db.rawQuery(sql, args);

            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex("id");
                int genreIdIndex = cursor.getColumnIndex("genre_id");
                int titleIndex = cursor.getColumnIndex("title");
                Reminder reminder = new Reminder(
                        cursor.getInt(idIndex),
                        cursor.getInt(genreIdIndex),
                        cursor.getString(titleIndex)
                );
                Log.d("ジャンルから取得", reminder.title);
                reminders.add(reminder);
            }
            return reminders;
        }

        public int insertNewReminder(Reminder reminder) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("genre_id", reminder.getGenreId());
            values.put("title", reminder.getTitle());

            long newRowId = db.insert("reminders", null, values);
            Log.d("保存成功", "IDは" + newRowId);
            return (int) newRowId;
        }

        public void updateReminder(Reminder reminder) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("genre_id", reminder.getGenreId());
            values.put("title", reminder.getTitle());

            String selection = "id = ?";
            String[] selectionArgs = {String.valueOf(reminder.genreId)};

            db.update("reminders", values, selection, selectionArgs);
        }

        public void deleteReminder(int id) {
            Log.d("削除します", "DBHelper -> 128");
            SQLiteDatabase db = this.getWritableDatabase();
            String selection = "id = ?";
            String[] selectionArgs = {String.valueOf(id)};
            db.delete("reminders", selection, selectionArgs);
        }
    }
}
