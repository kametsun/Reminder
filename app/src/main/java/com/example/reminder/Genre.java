package com.example.reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class Genre {
    private int id;
    private String title;

    // パラメータなしのコンストラクタ
    public Genre() {
    }

    // パラメータ付きのコンストラクタ
    public Genre(int id, String title) {
        this.id = id;
        this.title = title;
    }

    // idのためのゲッターおよびセッターメソッド
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // titleのためのゲッターおよびセッターメソッド
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // デバッグ目的でtoString()をオーバーライドすることもできます
    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }


    public static class GenreDBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "genres_database";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "genres_table";
        private static final String ID = "id";
        private static final String TITLE = "title";

        public GenreDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // テーブルの作成
            String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TITLE + " TEXT)";
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // データベースのアップグレード時の処理
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        // 新しいジャンルを挿入
        public int insertNewGenre(Genre genre) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TITLE, genre.getTitle());
            long id = db.insert(TABLE_NAME, null, values);
            db.close();
            return (int)id;
        }

        // ジャンルを更新
        public void updateGenre(Genre genre) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TITLE, genre.getTitle());
            db.update(TABLE_NAME, values, ID + " = ?", new String[]{String.valueOf(genre.getId())});
            db.close();
        }
        public void deleteGenre(int genreId) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, ID + " = ?", new String[]{String.valueOf(genreId)});
            db.close();
        }

        // 全てのジャンルを取得
        public List<Genre> getAllGenres() {
            List<Genre> genreList = new ArrayList<>();
            String selectQuery = "SELECT * FROM " + TABLE_NAME;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Genre genre = new Genre();
                    genre.setId(cursor.getInt(cursor.getColumnIndex(ID)));
                    genre.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
                    genreList.add(genre);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return genreList;
        }
    }

}
