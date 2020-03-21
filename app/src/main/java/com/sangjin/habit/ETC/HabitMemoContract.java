package com.sangjin.habit.ETC;

import android.provider.BaseColumns;

/**
 * 메모 계약 클래스
 */
public final class HabitMemoContract {

    // 인스턴스화 금지
    private HabitMemoContract() {
    }

    // 테이블 정보를 내부 클래스로 정의
    public static class HabitMemoEntry implements BaseColumns {
        public static final String TABLE_NAME = "habitMemo";
        public static final String COLUMN_NAME_HABIT = "habit";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_CREATED = "created";
    }

}