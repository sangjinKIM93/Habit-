package com.sangjin.habit.HabitAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HabitAuthAddActivity extends AppCompatActivity {

    private int togetherIdx;

    RetrofitService retrofitService;
    Retrofit retrofit;
    private String serverURL = "http://52.79.252.51/";
    private String TAG = "CustomDialog";

    private String uid;
    private String name;
    private String imagePath = "";

    private EditText et_habitAuthAdd;
    private Button btn_habitAuthAdd;
    private ImageView iv_habitAuthImage, iv_habitAuthVideo;

    private int GALLERY_CODE_AUTH = 44;
    private int VIDEO_CODE_AUTH = 55;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_auth_add);

        /*갤러리 접근 권한*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        //레트로핏 객체 및 레트로핏 서비스 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();
        retrofitService = retrofit.create(RetrofitService.class);

        //쉐어드로 로그인한 아이디 가져오기
        SharedPreferences pref = getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");
        name = pref.getString("name", "null");

        Intent intent = getIntent();
        togetherIdx = intent.getIntExtra("togetherIdx", -1);

        et_habitAuthAdd = findViewById(R.id.et_habitAuthAdd);
        btn_habitAuthAdd = findViewById(R.id.btn_habitAuthAdd);
        iv_habitAuthImage = findViewById(R.id.iv_habitAuthImage);
        iv_habitAuthVideo = findViewById(R.id.iv_habitAuthVideo);

        iv_habitAuthImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_CODE_AUTH);
            }
        });

        iv_habitAuthVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                startActivityForResult(intent, VIDEO_CODE_AUTH);
            }
        });

        btn_habitAuthAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(HabitAuthAddActivity.this,
                        "Please Wait", null, true, true);
                uploadImageChat();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_CODE_AUTH) {
            if(data != null){   //선택 안 하고 그냥 뒤로가기 눌렀을때 nullpointerror 잡기
                //imageview에 해당 사진 넣어주기
                imagePath = getPath(data.getData());
                File f = new File(imagePath);
                iv_habitAuthImage.setImageURI(Uri.fromFile(f));
                iv_habitAuthVideo.setVisibility(View.GONE);

            }
        }
        else if (requestCode == VIDEO_CODE_AUTH) {
            if(data != null){   //선택 안 하고 그냥 뒤로가기 눌렀을때 nullpointerror 잡기
                //imageview에 해당 사진 넣어주기
                imagePath = getPath(data.getData());
                File f = new File(imagePath);

                //Log.d("Video PATH : ", videoPath);
                Log.d("Video File : ", String.valueOf(f));
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(imagePath, MediaStore.Images.Thumbnails.MINI_KIND);
                iv_habitAuthVideo.setImageBitmap(bitmap);
                iv_habitAuthImage.setVisibility(View.GONE);

            }
        }
    }


    //임시 경로를 절대 경로로 바꿔주는 코드
    public String getPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(index);
    }


    //서버에 사진 업로드
    private void uploadImageChat() {
        Log.d(TAG, "uploadImageToServer");
        Log.d(TAG, "Path: "+imagePath);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        ArrayList<MultipartBody.Part> imageList = new ArrayList<>();

        File file = new File(imagePath);        //파일을 가져와서
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);     //requestBody라는 객체에 담고
        MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("files[]", imagePath, requestBody);    //multipartbody라는 객체에 마지막으로 넣어서
        imageList.add(uploadFile);

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<ResponseBody> call = retrofitService.uploadImageAuth(imageList);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "imageUpload onResponse: "+response);

                try {
                    String result = response.body().string();

                    if(response.isSuccessful()){
                        Log.d(TAG, "imageUpload isSuceessful: "+result);
                        //주소가 아주 잘 나왔어. 이제 이 자료를 바탕으로 서버chatData에 저장하고 tcp 통신해주는거야.
                        imagePath = result;
                        pushAuthData();

                    }else{
                        Log.d(TAG, "imageUpload isn't Suceessful: "+result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "imageUpload onFailure: "+t);
            }
        });
    }


    //서버에 인증 데이터 업로드
    private void pushAuthData() {
        String content = et_habitAuthAdd.getText().toString();

        HashMap hashmap = new HashMap();
        hashmap.put("togetherIdx", togetherIdx);        //intent로 받기
        hashmap.put("content", content);
        hashmap.put("uid", uid);                    //shared로 받기
        hashmap.put("imagePath", imagePath);        //일단 null로 해두고 나중에 이미지 첨부 기능 추가하면 수정하기.

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

                        progressDialog.dismiss();
                        //recyclerView에 넣어주기
                        finish();
                        Toast.makeText(HabitAuthAddActivity.this, "인증내역이 저장되었습니다.", Toast.LENGTH_SHORT).show();

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
}
