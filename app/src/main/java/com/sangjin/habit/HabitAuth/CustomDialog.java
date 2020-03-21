package com.sangjin.habit.HabitAuth;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

public class CustomDialog extends Dialog
{
    CustomDialog m_oDialog;

    private Context context;
    private int togetherIdx;

    RetrofitService retrofitService;
    Retrofit retrofit;
    private String serverURL = "http://52.79.252.51/";
    private String TAG = "CustomDialog";

    private String uid, name;

    public CustomDialog(Context context, int togetherIdx)
    {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.context = context;
        this.togetherIdx = togetherIdx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.activity_habit_auth_add);

        //레트로핏 객체 및 레트로핏 서비스 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();
        retrofitService = retrofit.create(RetrofitService.class);

        //쉐어드로 로그인한 아이디 가져오기
        SharedPreferences pref = context.getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");
        name = pref.getString("name", "null");

        m_oDialog = this;

//        TextView oView = (TextView) this.findViewById(R.id.textView);
//        oView.setText("Custom Dialog\n테스트입니다.");

//        Button oBtn = (Button)this.findViewById(R.id.btnOK);
//        oBtn.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                onClickBtn(v);
//            }
//        });
    }

    private void pushAuthData() {
        //String content = et_habitAuth.getText().toString();

        HashMap hashmap = new HashMap();
        hashmap.put("togetherIdx", togetherIdx);        //intent로 받기
        //hashmap.put("content", content);
        hashmap.put("uid", uid);                    //shared로 받기
        hashmap.put("imagePath", "null");        //일단 null로 해두고 나중에 이미지 첨부 기능 추가하면 수정하기.

        //레트로핏 서비스로 php와 연결하기(서버를 call하기)
        Call<ResponseBody> call = retrofitService.pushAuthData(hashmap);  //call을 보내는 것
        Log.d(TAG, "콜이 갔는지?");
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    String result = null;
                    result = response.body().string();
                    if (response.isSuccessful()) {

                        //recyclerView에 넣어주기
//                        josnToRecycler(result);
//                        et_habitAuth.setText("");

                    } else {
                        Log.d(TAG, "Push isn't Suceessful: " + result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Push Failed : " + t);
            }
        });
    }

    public void onClickBtn(View _oView)
    {
        this.dismiss();
    }
}

