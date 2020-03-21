package com.sangjin.habit.ETC;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.sangjin.habit.ETC.MemoContract.MemoEntry.TABLE_NAME;

/**
 * Created by junsuk on 2017. 7. 13..
 */

public class MemoDbHelper extends SQLiteOpenHelper {
    private static MemoDbHelper sInstance;

    // DB의 버전으로 1부터 시작하고 스키마가 변경될 때 숫자를 올린다
    private static final int DB_VERSION = 1;
    // DB 파일명
    private static final String DB_NAME = "Memo.db";

    // 테이블 생성 SQL문
    private static final String SQL_CREATE_ENTRIES =
            String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s NUMERIC, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER)",
                    TABLE_NAME,     //import해서 다른 매개변수와는 다른 형태임. import하지 않는다면 똑같은 형식으로 작성하면 됨.
                    MemoContract.MemoEntry._ID,
                    MemoContract.MemoEntry.COLUMN_NAME_TITLE,
                    MemoContract.MemoEntry.COLUMN_NAME_CREATED,
                    MemoContract.MemoEntry.COLUMN_NAME_DAYBEFORE,
                    MemoContract.MemoEntry.COLUMN_NAME_YESTERDAY,
                    MemoContract.MemoEntry.COLUMN_NAME_TODAY,
                    MemoContract.MemoEntry.COLUMN_NAME_GOAL,
                    MemoContract.MemoEntry.COLUMN_NAME_ACHIEVED,
                    MemoContract.MemoEntry.COLUMN_NAME_CONTINUITY);
    // 테이블 삭제 SQL문
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    // 팩토리 메서드
    public static synchronized MemoDbHelper getInstance(Context context) {
        // 액티비티의 context가 메모리 릭(leak)을 발생할 수 있으므로
        // application context를 사용하는 것이 좋다
        if (sInstance == null) {
            sInstance = new MemoDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // 생성자를 private로 직접 인스턴스화를 방지하고
    // getInstance()를 통해 인스턴스를 얻어야 함
    private MemoDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DB 스키마가 변경될 때 여기서 데이터를 백업하고
        // 테이블을 삭제 후 재생성 및 데이터 복원 등을 한다
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public Cursor LoadSQLiteDBCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " order by _id desc";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return cursor;
    }


}