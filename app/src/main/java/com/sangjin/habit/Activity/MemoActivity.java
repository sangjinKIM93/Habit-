package com.sangjin.habit.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sangjin.habit.Adapter.MemoAdapter;
import com.sangjin.habit.DataType.MemoData;
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

public class MemoActivity extends AppCompatActivity {

    private ArrayList<MemoData> mArrayList;
    private MemoAdapter mAdapter;
    private RecyclerView mRecyclerView;

    HabitMemoDbHelper dbHelper;
    SQLiteDatabase db;

    String today;
    String habitName;
    int idxHabit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_list);

        mArrayList = new ArrayList<>(); //먼저 선언해야 추가할 수 있어

        //리사이클러뷰 설정부터 어댑터 연결까지 -> 이후 데이터(mArrayList)를 새로 넣어주고 새로고침 해줘야해(notifyDataSetChanged)
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_memo);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); //Manager만 grid로 바꿔주고 열에 들어갈 갯수만 설정해주면 됨.
        mAdapter = new MemoAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);

        getHabitList();     //DB에서 리스트 가져오기
        mAdapter.notifyDataSetChanged();

        //onclick 인터페이스
        mAdapter.setOnItemClickListener(new MemoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //memo를 확인할 수 있는 뷰 출력

            }
        });

        //클릭 리스너(어답터 연결)
        clickListen();

    }

    private void getHabitList() {
        //db내용 가져오기
        dbHelper = HabitMemoDbHelper.getInstance(this);
        db= dbHelper.getReadableDatabase();
        db.beginTransaction();

        Intent intent = getIntent();
        habitName = intent.getExtras().getString("habitName");
        idxHabit = intent.getExtras().getInt("idxHabit");

        TextView tv_memoListTitle = findViewById(R.id.tv_memoListTitle);

        Cursor cursor = dbHelper.LoadSQLiteDBCursor(habitName);
        try {
            cursor.moveToFirst();
            System.out.println("SQLiteDB 개수 = " + cursor.getCount());
            int numMemo = 1;
            while (!cursor.isAfterLast()) {
                //자료받아서 arrayList에 넣어주기
                addMemo(cursor.getInt(0), String.valueOf(numMemo), cursor.getString(3), cursor.getString(2));
                numMemo = numMemo+1;

                tv_memoListTitle.setText(cursor.getString(1));      //제목에 해당 습관이름 넣기

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


    private void addMemo(int idx, String num, String date, String content){
        MemoData memoData = new MemoData();
        memoData.setIdx(idx);
        memoData.setNum(num);
        memoData.setDate(date);
        memoData.setContent(content);
        mArrayList.add(memoData);
    }

    private void clickListen(){
        //onLongClick 인터페이스
        mAdapter.setOnItemClickListenerLong(new MemoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //custom다이얼로그로 띄우기
                CustomDialog oDialog = new CustomDialog(MemoActivity.this ,position);
                oDialog.setCancelable(false);
                oDialog.show();
            }
        });

        //단순 클릭 리스너
        mAdapter.setOnItemClickListener(new MemoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

                String content = mArrayList.get(position).getContent();
                String date = mArrayList.get(position).getDate();
                //또다른 커스텀 다이얼로그 띄우기.
                CustomDialogMemoView memoDialog = new CustomDialogMemoView(MemoActivity.this ,position, content, date, 0);
                memoDialog.setCancelable(false);
                memoDialog.show();
            }
        });
    }

    //CustomDialog 출력하는 예제.
    public class CustomDialog extends Dialog
    {
        CustomDialog m_oDialog;
        int positionGet;
        public CustomDialog(Context context, int position)
        {
            super(context, android.R.style.Theme_Translucent_NoTitleBar);
            positionGet = position;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
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

                    //커스텀 하는데 edit으로 띄우는거야. et는... 새로 만들어야하나? 레이아웃은 새로 만들되 변수값을 줘서 다르게 구분하자.
                    String content = mArrayList.get(positionGet).getContent();
                    String date = mArrayList.get(positionGet).getDate();
                    //또다른 커스텀 다이얼로그 띄우기.
                    CustomDialogMemoView memoDialog = new CustomDialogMemoView(MemoActivity.this ,positionGet, content, date, 1);
                    memoDialog.setCancelable(false);
                    memoDialog.show();

                    //팝업창 종료
                    onClickBtn(v);
                }
            });

            Button btn_delete = this.findViewById(R.id.btn_delete);
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //삭제 경고 문구 만들기 ("삭제시 내용은 복구할 수 없으며, 달성일은 1일 차감됩니다. 정말로 삭제하시겠습니까?")
                    AlertDialog.Builder builder = new AlertDialog.Builder(MemoActivity.this);
                    builder.setTitle("Delete");
                    builder.setMessage("한번 삭제된 내용은 복구할 수 없습니다. 삭제하시겠습니까?");
                    builder.setPositiveButton("네",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //id 받아오기.
                                    int idx = mArrayList.get(positionGet).getIdx();

                                    //해당 메모 삭제
                                    SQLiteDatabase db = HabitMemoDbHelper.getInstance(MemoActivity.this).getWritableDatabase();
                                    db.delete(HabitMemoContract.HabitMemoEntry.TABLE_NAME, HabitMemoContract.HabitMemoEntry._ID + "=" + idx, null);

                                    //HabitList의 달성일(acieved) -1
                                    SQLiteDatabase dbUpdate = MemoDbHelper.getInstance(MemoActivity.this).getWritableDatabase();
                                    String sql = "update habit set achieved=achieved-1 where _id="+idxHabit;
                                    dbUpdate.execSQL(sql);
                                    Toast.makeText(MemoActivity.this, "deleted.", Toast.LENGTH_SHORT).show();

                                    //만약 오늘 메모가 삭제되면 버튼도 다시 바꿔줘야해.
                                    //->today를 0으로. *조건 = date가 오늘일때
                                    String date = mArrayList.get(positionGet).getDate();
                                    Date currentTime = Calendar.getInstance().getTime();
                                    today = new SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(currentTime);
                                    //Boolean eql = today.equals(date);

                                    if(today.equals(date)){     //날짜가 오늘날짜인 메모를 삭제할 경우. 디비도 habitMemo가 아니라 memoDb

                                        String sqlToday = "update habit set today=0 where _id="+idxHabit;
                                        dbUpdate.execSQL(sqlToday);
//                                        ContentValues todayValues = new ContentValues();
//                                        todayValues.put(MemoContract.MemoEntry.COLUMN_NAME_TODAY, "null");
//                                        dbUpdate.update(MemoContract.MemoEntry.TABLE_NAME, todayValues, MemoContract.MemoEntry.COLUMN_NAME_TITLE + "=" + habitName, null);
                                        Log.d("Today 0으로 최신화:", "실행됨.");
                                        //업데이트 이후에 버튼을 새로고침 해줘야해. 지금 메모 리스트는 최신화가 되는데 그전 액티비티의 리스트는 최신화가 안 된다.
                                        //activity가 가리면 pause상태니까. onResume에서 최신화를 해주자.

                                    }

                                    //리스트 새로고침
                                    refresh();


                                }
                            });
                    builder.setNegativeButton("아니요",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    builder.show();

                    //수정,삭제 팝업창 종료
                    onClickBtn(v);

                }
            });

            Button btn_exit = (Button)this.findViewById(R.id.btn_exit);
            btn_exit.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onClickBtn(v);
                }
            });
        }

        public void onClickBtn(View _oView)
        {
            this.dismiss();
        }
    }

    private void refresh(){
        mArrayList.clear();
        getHabitList();
        mAdapter = new MemoAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        clickListen();

    }

    //memoView를 위한 커스텀 다이얼로그.
    public class CustomDialogMemoView extends Dialog
    {
        CustomDialogMemoView memoDialog;
        int positionGet, editcheck;
        String memoContent, memoDate;
        EditText et_content_memoEdit;

        public CustomDialogMemoView(Context context, int position, String content, String date, int check)
        {
            super(context, android.R.style.Theme_Translucent_NoTitleBar);
            positionGet = position;
            memoContent = content;
            memoDate = date;
            editcheck = check;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWindow.dimAmount = 0.5f;
            getWindow().setAttributes(lpWindow);

            memoDialog = this;

            //메모view를 위한 호출과 수정을 위한 호출을 구분
            if(editcheck == 0){

                setContentView(R.layout.memo_dialog);

                TextView tv_content_memoView = this.findViewById(R.id.tv_content_memoView);
                tv_content_memoView.setText(memoContent);
                tv_content_memoView.setMovementMethod(new ScrollingMovementMethod());       //스크롤뷰 적용.

                TextView tv_date_memoView = this.findViewById(R.id.tv_date_memoView);
                tv_date_memoView.setText(memoDate);

                Button btn_exit_memoView = this.findViewById(R.id.btn_exit_memoView);
                btn_exit_memoView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        onClickBtn(v);
                    }
                });

            } else {
                setContentView(R.layout.memoedit_dialog);

                et_content_memoEdit = this.findViewById(R.id.et_content_memoEdit);
                et_content_memoEdit.setText(memoContent);
                et_content_memoEdit.setMovementMethod(new ScrollingMovementMethod());       //스크롤뷰 적용.

                TextView tv_date_memoEdit = this.findViewById(R.id.tv_date_memoEdit);
                tv_date_memoEdit.setText(memoDate);

                //완료 버튼 클릭시 수정된 내용 업데이트
                Button btn_complete_memoEdit = this.findViewById(R.id.btn_complete_memoEdit);
                btn_complete_memoEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(),"edited.",Toast.LENGTH_LONG).show();
                        String contentEdited = et_content_memoEdit.getText().toString();
                        int idx = mArrayList.get(positionGet).getIdx();

                        //DB에 업데이트
                        SQLiteDatabase dbUpdate = HabitMemoDbHelper.getInstance(MemoActivity.this).getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(HabitMemoContract.HabitMemoEntry.COLUMN_NAME_CONTENT, contentEdited);
                        dbUpdate.update(HabitMemoContract.HabitMemoEntry.TABLE_NAME, contentValues, MemoContract.MemoEntry._ID + "=" + idx, null);

                        //새로고침하고 닫아주기
                        refresh();
                        onClickBtn(v);
                    }
                });

                Button btn_exit_memoEdit = this.findViewById(R.id.btn_exit_memoEdit);
                btn_exit_memoEdit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        onClickBtn(v);
                    }
                });
            }

        }

        public void onClickBtn(View _oView)
        {
            this.dismiss();
        }
    }

}
