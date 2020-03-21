package com.sangjin.habit.Activity;

import android.content.ContentValues;
import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HabitAddActivity extends AppCompatActivity {
    private EditText mTitleEditText;
    private EditText mContentsEditText;
    private Button btn_habbitAdd;

    private long mMemoId = -1;

    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_add);

        mTitleEditText = findViewById(R.id.et_habitName);
        mContentsEditText = findViewById(R.id.et_habitGoal);

//        Intent intent = getIntent();
//        if (intent != null) {
//            mMemoId = intent.getLongExtra("id", -1);
//            String title = intent.getStringExtra("title");
//            String contents = intent.getStringExtra("contents");
//            mTitleEditText.setText(title);
//            mContentsEditText.setText(contents);
//        }

        btn_habbitAdd = findViewById(R.id.btn_habitAdd);
        btn_habbitAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //editText 중 빈칸이 있는지 확인. 있을 경우 진행 x
                if (mTitleEditText.getText().toString().replace(" ", "").equals("") || mContentsEditText.getText().toString().replace(" ", "").equals("")) {
                    Toast.makeText(HabitAddActivity.this, "빈 항목을 모두 작성해주세요.", Toast.LENGTH_SHORT).show();

                } else {

                    // DB 에 저장하는 처리
                    String title = mTitleEditText.getText().toString();
                    int goal = Integer.parseInt(String.valueOf(mContentsEditText.getText()));
                    Date currentTime = Calendar.getInstance().getTime();
                    String date_text = new SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(currentTime);

                    //SQLite에 저장하는 기본 방법은 contentValues라는 객체에 담아서 저장.
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_TITLE, title);
                    contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_CREATED, date_text);
                    contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_DAYBEFORE, 0);
                    contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_YESTERDAY, 0);
                    contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_TODAY, 0);
                    contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_GOAL, goal);
                    contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_ACHIEVED, 0);
                    contentValues.put(MemoContract.MemoEntry.COLUMN_NAME_CONTINUITY, 0);

                    mContext = getApplicationContext();

                    SQLiteDatabase db = MemoDbHelper.getInstance(mContext).getWritableDatabase();
                    // DB 에 저장하는 처리
                    long newRowId = db.insert(MemoContract.MemoEntry.TABLE_NAME, null, contentValues);

                    if (newRowId == -1) {
                        Toast.makeText(mContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "추가되었습니다..", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(mContext, HabitListActivity.class));
                        finish();
                    }
                }
            }
        });


    }


}