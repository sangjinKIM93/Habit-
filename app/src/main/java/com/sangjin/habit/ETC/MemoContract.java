package com.sangjin.habit.ETC;

import android.provider.BaseColumns;

/**
 * 메모 계약 클래스
 */
public final class MemoContract {

    // 인스턴스화 금지
    private MemoContract() {
    }

    // 테이블 정보를 내부 클래스로 정의
    public static class MemoEntry implements BaseColumns {
        public static final String TABLE_NAME = "habit";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_CREATED = "created";
        public static final String COLUMN_NAME_DAYBEFORE = "dayBefore";
        public static final String COLUMN_NAME_YESTERDAY = "yesterday";
        public static final String COLUMN_NAME_TODAY = "today";
        public static final String COLUMN_NAME_GOAL = "goal";
        public static final String COLUMN_NAME_ACHIEVED = "achieved";
        public static final String COLUMN_NAME_CONTINUITY = "continuity";
    }

}