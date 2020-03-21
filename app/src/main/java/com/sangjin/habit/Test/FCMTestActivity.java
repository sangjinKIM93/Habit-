//package com.sangjin.habit;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//
//import com.google.firebase.iid.FirebaseInstanceId;
//
//import java.io.IOException;
//
//import okhttp3.ResponseBody;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//
//public class FCMTestActivity extends AppCompatActivity {
//
//    RetrofitService retrofitService;
//    Retrofit retrofit;
//    private String serverURL = "http://52.79.252.51/";
//
//    private String TAG = "FCMTestActivity";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_fcmtest);
//
//        //레트로핏 객체 및 레트로핏 서비스 객체 선언
//        retrofit = new Retrofit.Builder()
//                .baseUrl(serverURL)
//                .build();
//        retrofitService = retrofit.create(RetrofitService.class);
//
//        Button btn_getToken = findViewById(R.id.btn_getToken);
//        btn_getToken.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String savedToken = FirebaseInstanceId.getInstance().getToken();
//                Log.d("등록되어 있는 토큰 : ", savedToken);
//            }
//        });
//
//        Button btn_sendMessager = findViewById(R.id.btn_sendMessage);
//        btn_sendMessager.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendFCM();
//            }
//        });
//    }
//
//    private void sendFCM() {
//        Call<ResponseBody> call = retrofitService.sendFCM();  //call을 보내는 것
//        //call에 대한 응답받기
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.d(TAG, "sendFCM : "+response);
//                String result = null;
//                try {
//                    result = response.body().string();
//                    if(response.isSuccessful()){
//                        Log.d(TAG, "sendFCM Suceessful: "+result);
//                    }else{
//                        Log.d(TAG, "sendFCM isn't Suceessful: "+result);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.d(TAG, "sendFCM Failed : "+t);
//            }
//        });
//    }
//}
