package com.sangjin.habit.Activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sangjin.habit.DataType.PersonalData;
import com.sangjin.habit.ETC.MemoDbHelper;
import com.sangjin.habit.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    //context 설정하기
    public Context context = null;
    public ArrayList<PersonalData> arrayList;
    MemoDbHelper dbHelper;
    SQLiteDatabase db;

    public MyRemoteViewsFactory(Context context) {
        this.context = context;
    }

    private void getHabitList() {
        arrayList = new ArrayList<>();
        //db내용 가져오기
        //mArrayList.clear(); // 데이터 초기화
        dbHelper = MemoDbHelper.getInstance(context);
        db= dbHelper.getReadableDatabase();
        db.beginTransaction();

        Cursor cursor = dbHelper.LoadSQLiteDBCursor();
        try {
            cursor.moveToFirst();
            System.out.println("SQLiteDB 개수 = " + cursor.getCount());

            while (!cursor.isAfterLast()) {
                System.out.println("SQLiteDB 컬럼 0 = " +cursor.getString(0));
                System.out.println("SQLiteDB 컬럼 1 = " +cursor.getString(1));
                System.out.println("SQLiteDB 컬럼 2 = " +cursor.getString(2));
                System.out.println("SQLiteDB 컬럼 3 = " +cursor.getString(3));
                System.out.println("SQLiteDB 컬럼 4 = " +cursor.getString(4));
                System.out.println("SQLiteDB 컬럼 5 = " +cursor.getString(5));
                System.out.println("SQLiteDB 컬럼 6 = " +cursor.getString(6));
                System.out.println("SQLiteDB 컬럼 7 = " +cursor.getString(7));
                System.out.println("SQLiteDB 컬럼 8 = " +cursor.getString(8));

                if (cursor.getInt(6) > cursor.getInt(7)) {          //도전중인 목록만 뜨도록
                    addGroupItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7), cursor.getInt(8));
                }
                cursor.moveToNext();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                db.endTransaction();
            }
        }
    }

    public void addGroupItem(int idx, String title, String date, int daybefore, int yesterday, String today, int goal, int achieved, int continuity){
        PersonalData personalData = new PersonalData();
        personalData.setIdx(idx);
        personalData.setHabitName(title);   //title을 받아서 넣었다.
        personalData.setCreated(date);
        personalData.setDayBefore(daybefore);
        personalData.setYesterday(yesterday);
        personalData.setToday(today);
        personalData.setGoalNum(goal);
        personalData.setDayAchieved(achieved);
        personalData.setContinuity(continuity);
        arrayList.add(personalData);
    }

    //DB를 대신하여 arrayList에 데이터를 추가하는 함수ㅋㅋ
//    public void setData() {
//        arrayList = new ArrayList<>();
//        arrayList.add(new WidgetItem(1, "First"));
//        arrayList.add(new WidgetItem(2, "Second"));
//        arrayList.add(new WidgetItem(3, "Third"));
//        arrayList.add(new WidgetItem(4, "Fourth"));
//        arrayList.add(new WidgetItem(5, "Fifth"));
//    }

    //이 모든게 필수 오버라이드 메소드

    //실행 최초로 호출되는 함수
    @Override
    public void onCreate() {
        //setData();
        getHabitList();
    }

    //항목 추가 및 제거 등 데이터 변경이 발생했을 때 호출되는 함수
    //브로드캐스트 리시버에서 notifyAppWidgetViewDataChanged()가 호출 될 때 자동 호출
    @Override
    public void onDataSetChanged() {
        //setData();
        getHabitList();

    }

    //마지막에 호출되는 함수
    @Override
    public void onDestroy() {

    }

    // 항목 개수를 반환하는 함수
    @Override
    public int getCount() {
        return arrayList.size();
    }

    //각 항목을 구현하기 위해 호출, 매개변수 값을 참조하여 각 항목을 구성하기위한 로직이 담긴다.
    // 항목 선택 이벤트 발생 시 인텐트에 담겨야 할 항목 데이터를 추가해주어야 하는 함수
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews listviewWidget = new RemoteViews(context.getPackageName(), R.layout.item_collection);
        //습관명 설정
        listviewWidget.setTextViewText(R.id.widget_habitName, arrayList.get(position).getHabitName());

        //check버튼 설정
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(currentTime);
        if (arrayList.get(position).getToday().equals(date_text)) {
            listviewWidget.setImageViewResource(R.id.widget_check, R.drawable.check3);
        } else {
            listviewWidget.setImageViewResource(R.id.widget_check, R.drawable.check2);
        }

        //progressBar 셋팅
        int dayAchieved = arrayList.get(position).getDayAchieved();
        int goal = arrayList.get(position).getGoalNum();
        int percent_achieve = (dayAchieved*100/goal);   //100을 먼저 곱해줘야해. 나중에 곱하면 그 전의 값이 0이 되어서 100을 곱하는 것이 의미가 없어져
        listviewWidget.setProgressBar(R.id.widget_progress, goal, dayAchieved, false);


        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(WidgetListView.EXTRA_ITEM, position);
        listviewWidget.setOnClickFillInIntent(R.id.listView_parent, fillInIntent);      //pendingIntent와 같이 쓰이는 녀석. 클릭시 설정된 extraData를 추가적으로 전달할 수 있어.



        return listviewWidget;
    }

    //로딩 뷰를 표현하기 위해 호출, 없으면 null
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    //항목의 타입 갯수를 판단하기 위해 호출, 모든 항목이 같은 뷰 타입이라면 1을 반환하면 된다.
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    //각 항목의 식별자 값을 얻기 위해 호출
    @Override
    public long getItemId(int position) {
        return 0;
    }

    // 같은 ID가 항상 같은 개체를 참조하면 true 반환하는 함수
    @Override
    public boolean hasStableIds() {
        return false;
    }

}
