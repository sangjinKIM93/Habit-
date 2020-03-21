package com.sangjin.habit.Activity;

import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sangjin.habit.DataType.PersonalData;
import com.sangjin.habit.Adapter.HabitListAdapter;
import com.sangjin.habit.ETC.HabitMemoContract;
import com.sangjin.habit.ETC.HabitMemoDbHelper;
import com.sangjin.habit.ETC.MemoContract;
import com.sangjin.habit.ETC.MemoDbHelper;
import com.sangjin.habit.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HabitListActivity extends AppCompatActivity {

    private TextView mTextViewResult;
    private ArrayList<PersonalData> mArrayList;
    private HabitListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button btn_add;

    MemoDbHelper dbHelper;
    SQLiteDatabase db;

    String date_text;
    String habit;
    int idx;

    private Spinner spinner2;
    ArrayList<String> arrayListSpinner;
    ArrayAdapter<String> arrayAdapterSpinner;
    private int lineUp = 0;     //default를 "1.도전중인 습관" 으로

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_list);

        mArrayList = new ArrayList<>(); //먼저 선언해야 추가할 수 있어

        //DB에서 habitList 가져오기
        getHabitList();

        //리사이클러뷰 설정부터 어댑터 연결까지 -> 이후 데이터(mArrayList)를 새로 넣어주고 새로고침 해줘야해(notifyDataSetChanged)
        mRecyclerView = (RecyclerView) findViewById(R.id.listView_main_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new HabitListAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        //버튼 클릭에 대한 리스너 (함수로 만듬)
        clickListen();

        //정렬을 위한 스피너 등록
        arrayListSpinner = new ArrayList<>();
        arrayListSpinner.add("Challenging");
        arrayListSpinner.add("Acheived");
        arrayListSpinner.add("All");
        arrayAdapterSpinner = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                arrayListSpinner);

        spinner2 = (Spinner)findViewById(R.id.spinner_lineUp);
        spinner2.setAdapter(arrayAdapterSpinner);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        lineUp = 0;
                        refresh();
                        clickListen();
                        break;
                    case 1:
                        lineUp = 1;
                        refresh();
                        clickListen();
                        break;
                    case 2:
                        lineUp = 2;
                        refresh();
                        clickListen();
                        break;
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    private void refresh() {

        mArrayList.clear();
        getHabitList();
        mAdapter = new HabitListAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, WidgetListView.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);

    }

    private void clickListen() {
        mAdapter.setOnItemClickListener(new HabitListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int position) {

                //custom다이얼로그로 띄우기
                CustomDialog oDialog = new CustomDialog(HabitListActivity.this, position);
                oDialog.setCancelable(false);
                oDialog.show();

//                final AlertDialog.Builder builder = new AlertDialog.Builder(HabitListActivity.this);
//                builder.setTitle("삭제");
//                builder.setMessage("해당 항목을 삭제하시겠습니까?");
//                builder.setPositiveButton("예",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                //id 받아오기.
//                                int idx = mArrayList.get(position).getIdx();
//                                //삭제 로직
//                                SQLiteDatabase db = MemoDbHelper.getInstance(HabitListActivity.this).getWritableDatabase();
//                                db.delete(MemoContract.MemoEntry.TABLE_NAME, MemoContract.MemoEntry._ID+"="+idx, null);
//                                Toast.makeText(HabitListActivity.this, idx+"가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
//
//                                //리스트 새로고침
//                                refresh();
//
//                                //재귀로 다시 넣어주기
//                                clickListen();
//                            }
//                        });
//                builder.setNegativeButton("아니오",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        });
//                builder.show();
            }
        });

        mAdapter.setOnItemClickListener2(new HabitListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int position) {

                //날짜 변수
                String today = mArrayList.get(position).getToday();
                Date currentTime = Calendar.getInstance().getTime();
                date_text = new SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(currentTime);
                Boolean eql = today.equals(date_text);

                //기본 변수
                habit = mArrayList.get(position).getHabitName();
                int goalNum = mArrayList.get(position).getGoalNum();
                int dayAcheived = mArrayList.get(position).getDayAchieved();
                Boolean acheived = goalNum <= dayAcheived;
                Log.d("BOOLEAN Aceived:", String.valueOf(acheived));

                //이미 선택이 완료된 경우는 mewmoList로 넘어가도록 처리
                if (today.equals(date_text) || acheived) {
                    Intent intent = new Intent(HabitListActivity.this, MemoActivity.class);
                    intent.putExtra("habitName", habit);
                    intent.putExtra("idxHabit", idx);
                    startActivity(intent);

                } else {
                    //alertDialog 띄우기. 함수로 만들어서 쓰고 싶었는데 그러면 버튼 통제가 안 되어서 그냥 여기 안에 넣었어.
                    final EditText edittext = new EditText(HabitListActivity.this);
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(HabitListActivity.this);
                    builder.setTitle("Write your feeling");
                    //builder.setMessage("AlertDialog Content");
                    builder.setView(edittext);
                    builder.setPositiveButton("done",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    //btn_today.setBackgroundResource(R.drawable.check);      //성공시 버튼 색 바꾸기.

                                    // DB 에 저장하는 처리
                                    String content = edittext.getText().toString();

                                    //SQLite에 저장하는 기본 방법은 contentValues라는 객체에 담아서 저장.
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(HabitMemoContract.HabitMemoEntry.COLUMN_NAME_HABIT, habit);
                                    contentValues.put(HabitMemoContract.HabitMemoEntry.COLUMN_NAME_CONTENT, content);
                                    contentValues.put(HabitMemoContract.HabitMemoEntry.COLUMN_NAME_CREATED, date_text);
                                    Log.d("내용이 없는 콘텐츠", content);

                                    SQLiteDatabase db = HabitMemoDbHelper.getInstance(HabitListActivity.this).getWritableDatabase();
                                    // DB 에 저장하는 처리
                                    long newRowId = db.insert(HabitMemoContract.HabitMemoEntry.TABLE_NAME, null, contentValues);

                                    if (newRowId == -1) {
                                        Toast.makeText(HabitListActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(HabitListActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                                    }

                                    //update (habitList의 오늘 수행 여부)
                                    SQLiteDatabase dbUpdate = MemoDbHelper.getInstance(HabitListActivity.this).getWritableDatabase();

                                    idx = mArrayList.get(position).getIdx();

                                    ContentValues values = new ContentValues();
                                    values.put(MemoContract.MemoEntry.COLUMN_NAME_TODAY, date_text);
                                    dbUpdate.update(MemoContract.MemoEntry.TABLE_NAME, values, MemoContract.MemoEntry._ID + "=" + idx, null);

                                    //habit에도 달성일 +1
                                    String sql = "update habit set achieved=achieved+1 where _id=" + idx;
                                    dbUpdate.execSQL(sql);

                                    //리스트 새로고침
                                    refresh();

                                    //재귀로 다시 넣어주기
                                    clickListen();
                                }
                            });
                    builder.setNegativeButton("cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                }
            }
        });
    }

    private void getHabitList() {
        //db내용 가져오기
        //mArrayList.clear(); // 데이터 초기화
        dbHelper = MemoDbHelper.getInstance(this);
        db = dbHelper.getReadableDatabase();
        db.beginTransaction();

        Cursor cursor = dbHelper.LoadSQLiteDBCursor();
        try {
            cursor.moveToFirst();
            System.out.println("SQLiteDB 개수 = " + cursor.getCount());

            while (!cursor.isAfterLast()) {
                System.out.println("SQLiteDB 컬럼 0 = " + cursor.getString(0));
                System.out.println("SQLiteDB 컬럼 1 = " + cursor.getString(1));
                System.out.println("SQLiteDB 컬럼 2 = " + cursor.getString(2));
                System.out.println("SQLiteDB 컬럼 3 = " + cursor.getString(3));
                System.out.println("SQLiteDB 컬럼 4 = " + cursor.getString(4));
                System.out.println("SQLiteDB 컬럼 5 = " + cursor.getString(5));
                System.out.println("SQLiteDB 컬럼 6 = " + cursor.getString(6));
                System.out.println("SQLiteDB 컬럼 7 = " + cursor.getString(7));
                System.out.println("SQLiteDB 컬럼 8 = " + cursor.getString(8));

                //정렬에 따라 가져오는 데이터를 다르게
                switch (lineUp){

                    case 0:
                        //도전
                        if (cursor.getInt(6) > cursor.getInt(7)) {
                            addGroupItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7), cursor.getInt(8));
                        }
                        break;
                    case 1:
                        //달성
                        if (cursor.getInt(6) <= cursor.getInt(7)) {
                            addGroupItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7), cursor.getInt(8));
                        }
                        break;
                    case 2:
                        //전체
                        addGroupItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7), cursor.getInt(8));
                        break;
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

    public void addGroupItem(int idx, String title, String date, int daybefore, int yesterday, String today, int goal, int achieved, int continuity) {
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
        mArrayList.add(personalData);
    }

    //CustomDialog 출력하는 예제.
    public class CustomDialog extends Dialog {
        CustomDialog m_oDialog;
        int positionGet;

        public CustomDialog(Context context, int position) {
            super(context, android.R.style.Theme_Translucent_NoTitleBar);
            positionGet = position;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWindow.dimAmount = 0.5f;
            getWindow().setAttributes(lpWindow);

            setContentView(R.layout.custom_dialog);

            m_oDialog = this;

            Button btn_edit = this.findViewById(R.id.btn_edit);
            btn_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //HabitEditActivity로 인텐트를 넘겨준다.
                    //습관 이름과 목표일수를 같이 넘겨준다
                    String habitName = mArrayList.get(positionGet).getHabitName();
                    int goalNum = mArrayList.get(positionGet).getGoalNum();
                    int idx = mArrayList.get(positionGet).getIdx();

                    Intent intent = new Intent(HabitListActivity.this, HabitEditActivity.class);
                    intent.putExtra("habitName", habitName);
                    intent.putExtra("goalNum", goalNum);
                    intent.putExtra("idx", idx);
                    startActivity(intent);

                    //팝업창 종료
                    onClickBtn(v);
                }
            });

            Button btn_delete = this.findViewById(R.id.btn_delete);
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //id 받아오기.
                    int idx = mArrayList.get(positionGet).getIdx();
                    //삭제 로직
                    SQLiteDatabase db = MemoDbHelper.getInstance(HabitListActivity.this).getWritableDatabase();
                    db.delete(MemoContract.MemoEntry.TABLE_NAME, MemoContract.MemoEntry._ID + "=" + idx, null);
                    Toast.makeText(HabitListActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();

                    //리스트 새로고침
                    refresh();

                    //재귀로 다시 넣어주기
                    clickListen();

                    //팝업창 종료
                    onClickBtn(v);
                }
            });

            Button btn_exit = (Button) this.findViewById(R.id.btn_exit);
            btn_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickBtn(v);
                }
            });
        }

        public void onClickBtn(View _oView) {
            this.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //목록 최신화
        refresh();
        //버튼 클릭에 대한 리스너 등록
        clickListen();
    }
}
