package com.sangjin.habit.Test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sangjin.habit.R;
import com.sangjin.habit.User;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmTEST extends AppCompatActivity {

    private Realm mRealm;
    private TextView tv_result;
    private EditText et_realmEmail, et_realmPwd;
    private Button btn_realmInsert, btn_realmExport;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_realm);

        mRealm = Realm.getDefaultInstance();

        tv_result = findViewById(R.id.tv_result);
        et_realmEmail = findViewById(R.id.et_realmEmail);
        et_realmPwd = findViewById(R.id.et_realmPwd);
        btn_realmInsert = findViewById(R.id.btn_realmInsert);
        btn_realmExport = findViewById(R.id.btn_realmExport);

        btn_realmInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    private void insertData(){
        mRealm.executeTransaction(new Realm.Transaction() {     //trasction이란 언제 쓰는가? 대향의 insert, delete할때 중간에 뻑나면 데이터를 되돌려야 하잖아. 그래서 쓰는거야.
            @Override
            public void execute(Realm realm) {
                User user = mRealm.createObject(User.class);
                user.setEmail(et_realmEmail.getText().toString());
                user.setPassword(et_realmPwd.getText().toString());
                showResult();
            }
        });

    }

    private void showResult(){
        //저장된 데이터 가져와서 출력하기
        RealmResults<User> result = mRealm.where(User.class).findAll();     //realmresult라는 객체로 생성되고 findAll은 다 가져오는거,

//        String thing="";
//        for(int i=0; i<result.size(); i++){
//            String parsed = result.get(i).getEmail();
//            thing += parsed;
//        }

        tv_result.setText(result.toString());

    }
}
