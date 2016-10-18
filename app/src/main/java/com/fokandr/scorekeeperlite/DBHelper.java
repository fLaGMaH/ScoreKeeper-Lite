package com.fokandr.scorekeeperlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSIONS = 1;
    public static final String DATABASE_NAME = "ScoreKeeperLite";

    /*Таблица игроков*/
    public static final String TABLE_PLAYER = "PLAYERS";
    public static final String KEY_ID_PlAYER = "id";
    public static final String KEY_NAME_PlAYER = "name";
    /*Таблица раундов*/
    public static final String TABLE_ROUND = "ROUNDS";
    public static final String KEY_ID_ROUND = "id";
    public static final String KEY_ROUND_NUM = "id_round";
    public static final String KEY_ID_PLAYER_IN_ROUND = "id_player";
    public static final String KEY_SCORE = "score";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSIONS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLE_PLAYER+" ("
                                    +KEY_ID_PlAYER+" integer primary key, "
                                    +KEY_NAME_PlAYER+" text)");
        db.execSQL("create table "+TABLE_ROUND+" ("+KEY_ID_ROUND+" integer primary key, "
                                                   +KEY_ROUND_NUM+" integer, "
                                                   +KEY_ID_PLAYER_IN_ROUND+" integer, "
                                                    +KEY_SCORE+","
                                                    +" FOREIGN KEY ("+KEY_ID_PLAYER_IN_ROUND+") REFERENCES "+TABLE_PLAYER+"("+KEY_ID_PlAYER+") )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table "+TABLE_PLAYER);
        db.execSQL("drop table "+TABLE_ROUND);
        onCreate(db);

    }

}
