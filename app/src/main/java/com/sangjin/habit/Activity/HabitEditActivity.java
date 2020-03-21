package com.sangjin.habit.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sangjin.habit.ETC.MemoContract;
import com.sangjin.habit.ETC.MemoDbHelper;
import com.sangjin.habit.R;

public class HabitEditActivity extends AppCompatActivity {
    private EditText mTitleEditText;
    private EditText mContentsEditText;
    private Button btn_habbitAdd;

    public Context mContext;
    int idx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_add);

        mTitleEditText = findViewById(R.id.et_habitName);
        mContentsEditText = findViewById(R.id.et_habitGoal);

        Intent intent = getIntent();
        final String habitName = intent.getExtras().getString("habitName");
        final int goalNum = intent.getExtras().getInt("goalNum");
        idx = intent.getExtras().getInt("idx");

        mTitleEditText.setText(habitName);
        mContentsEditText.setText(String.valueOf(goalNum));

        btn_habbitAdd = findViewById(R.id.btn_habitAdd);
        btn_habbitAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTitleEditText.getText().toString().replace(" ", "").equals("") || mContentsEditText.getText().toString().replace(" ", "").equals("")) {
                    Toast.makeText(HabitEditActivity.this, "fill all content.", Toast.LENGTH_SHORT).show();

                } else {
                    //update (habitList의 오늘 수행 여부)
                    SQLiteDatabase dbUpdate = MemoDbHelper.getInstance(HabitEditActivity.this).getWritableDatabase();

                    String habitNameEdited = mTitleEditText.getText().toString();
                    int goalEdited = Integer.parseInt(String.valueOf(mContentsEditText.getText()));

                    ContentValues values = new ContentValues();
                    values.put(MemoContract.MemoEntry.COLUMN_NAME_TITLE, habitNameEdited);
                    values.put(MemoContract.MemoEntry.COLUMN_NAME_GOAL, goalEdited);
                    dbUpdate.update(MemoContract.MemoEntry.TABLE_NAME, values, MemoContract.MemoEntry._ID + "=" + idx, null);

                    Intent intent = new Intent(HabitEditActivity.this, MainBottomActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

//                // DB 에 저장하는 처리
//                String title = mTitleEditText.getText().toString();
//                int goal = Integer.parseInt(String.valueOf(mContentsEditText.getText()));
//                Date currentTime = Calendar.getInstance().getTime();
//                String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일", Locale.getDefault()).format(currentTime);
//
//                //SQLite에 저장하는 기본 방법은 contentValues라는 객체에 담아서 저장.
//                ContentValues contentValues = new ContentValues();
//                contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_TITLE, title);
//                contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_CREATED, date_text);
//                contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_DAYBEFORE, 0);
//                contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_YESTERDAY, 0);
//                contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_TODAY, 0);
//                contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_GOAL, goal);
//                contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_ACHIEVED, 0);
//                contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_CONTINUITY, 0);
//
//                mContext = getApplicationContext();
//
//                SQLiteDatabase db = MemoDbHelper.getInstance(mContext).getWritableDatabase();
//                // DB 에 저장하는 처리
//                long newRowId = db.insert(MemoContract.MemoEntry.TABLE_NAME, null, contentValues);
//
//                if (newRowId == -1) {
//                    Toast.makeText(mContext, "저장에 문제가 발생하였습니다", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(mContext, "저장되었습니다.", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(mContext, HabitListActivity.class));
//                    finish();
//                }
                }
            }
        });
    }


}