package com.sangjin.habit.TogetherPlus;

import android.Manifest;
import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.sangjin.habit.R;
import com.sangjin.habit.RealPathFromURI;
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

public class TogetherEditActivity extends AppCompatActivity {

    EditText et_editTitle, et_editContent;
    ImageView iv_editImage;
    Button btn_editFinish, btn_toDefault;

    int REQUEST_CODE_FOR_IMAGES = 100;
    private String TAG = "Edit Together";
    String imagePath;
    int togetherIdx;


    RetrofitService retrofitService;
    Retrofit retrofit;
    private String serverURL = "http://52.79.252.51/";
    String imagePathSelected;
    Boolean goToGallery = false;

    String imageName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.together_edit);

        /*갤러리 접근 권한*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        //레트로핏 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        //레트로핏 서비스 객체 선언
        retrofitService = retrofit.create(RetrofitService.class);

        //수정 전 데이터 받아오기
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        imagePath = intent.getStringExtra("imagePath");
        togetherIdx = intent.getIntExtra("togetherIdx", -1);

        //삭제를 위해서 이미지 이름만 따로 잘라주기
        if(!imagePath.equals("null")){
            imageName = imagePath.substring(imagePath.length()-15, imagePath.length());
        }else{
            imageName = "null";
        }

        et_editTitle = findViewById(R.id.et_editTitle);
        et_editContent = findViewById(R.id.et_editContent);
        iv_editImage = findViewById(R.id.iv_editImage);
        btn_editFinish = findViewById(R.id.btn_editFinish);
        btn_toDefault = findViewById(R.id.btn_toDefault);

        //수정 전 데이터 셋팅
        et_editTitle.setText(title);
        et_editContent.setText(content);

        //이미지 가져오기
        getImage();

        //버튼 클릭 리스너
        btnClcikList();

    }


    private void btnClcikList() {

        //기본이미지로 변경 버튼
        btn_toDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePathSelected = "null";
                iv_editImage.setImageResource(R.drawable.training);

            }
        });

        //완료 버튼 클릭시
        btn_editFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!goToGallery){           //갤러리에 접근한 적이 없는 경우, 즉 사진을 변경하지 않은 경우, 제목, 콘텐츠만 수정
                    imagePathSelected = "notChanged";
                    editData();

                }else{
                    if(imagePathSelected.equals("null")){       //기존 이미지를 default로 변경할 경우, file전송이 아니라 null값만 전달해서 입력
                        editData();
                        Toast.makeText(TogetherEditActivity.this, "editData()", Toast.LENGTH_SHORT).show();
                    }else{
                        uploadImageToServer();
                    }

                }
            }

        });
    }


    private void getImage() {
        if(imagePath.equals("null")){
            iv_editImage.setImageResource(R.drawable.training);         //기본이미지일 경우
            //버튼 클릭
            iv_editImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectMultiImages();
                }
            });

        }else{
            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(this);
            circularProgressDrawable.setStrokeWidth(5f);
            circularProgressDrawable.setCenterRadius(30f);
            circularProgressDrawable.start();

            Glide.with(this)
                    .load(imagePath)
                    .placeholder(circularProgressDrawable)
                    .override(550, 550).into(iv_editImage);

            //버튼 클릭
            iv_editImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectMultiImages();
                }
            });
        }
    }


    private void editData() {
        String title = et_editTitle.getText().toString();
        String content = et_editContent.getText().toString();

        HashMap hashmap = new HashMap();
        hashmap.put("title", title);
        hashmap.put("content", content);
        hashmap.put("imagePath", imagePathSelected);
        hashmap.put("idx", togetherIdx);

        //레트로핏 서비스로 php와 연결하기(서버를 call하기)
        Call<ResponseBody> call = retrofitService.editData(hashmap);  //call을 보내는 것
        Log.d(TAG, "콜이 갔는지?");
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                Toast.makeText(TogetherEditActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "update Failed : "+t);
            }
        });
    }


    private void uploadImageToServer(){
        Log.d(TAG, "uploadImageToServer");
        Log.d(TAG, "Path: "+imagePathSelected);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        ArrayList<MultipartBody.Part> imageList = new ArrayList<>();

        File file = new File(imagePathSelected);        //파일을 가져와서
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);     //requestBody라는 객체에 담고
        MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("files[]", imagePathSelected, requestBody);    //multipartbody라는 객체에 마지막으로 넣어서
        imageList.add(uploadFile);

        String title = et_editTitle.getText().toString();
        String content = et_editContent.getText().toString();

        RequestBody titleEdited = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody contentEdited = RequestBody.create(MediaType.parse("text/plain"), content);
        RequestBody idx = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(togetherIdx));
        RequestBody imageNamePre = RequestBody.create(MediaType.parse("text/plain"), imageName);

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<ResponseBody> call = retrofitService.editGathering(imageList, titleEdited, contentEdited, idx, imageNamePre);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: "+response);

                try {
                    String result = response.body().string();

                    if(response.isSuccessful()){
                        Log.d(TAG, "isSuceessful: "+result);
                        //result형식이 ["url"] 이라 괄호와 따옴표를 없애줘야함.

                        Toast.makeText(TogetherEditActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();

                    }else{
                        Log.d(TAG, "isn't Suceessful: "+result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t);
            }
        });
    }



    private void selectMultiImages(){
        Log.d(TAG, "selectMultiImages");
        goToGallery = true;
        // 선택을 할 수 있는 인텐트를 만듦
        Intent intent = new Intent(Intent.ACTION_PICK);
        // 이미지 선택을 하겠다는 의미
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

        // 아래 줄로 인해서 한번에 여러장을 받아올 수 있다.
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // 선택된 이미지의 uri를 받아오겠다는 의미
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_FOR_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: "+data);
        Log.d(TAG, "requestCode: "+requestCode);
        Log.d(TAG, "resultCode: "+resultCode);

        if(requestCode == REQUEST_CODE_FOR_IMAGES && resultCode == RESULT_OK){

            if(data != null){   //선택 안 하고 그냥 뒤로가기 눌렀을때 nullpointerror 잡기
                //imageview에 해당 사진 넣어주기
                imagePathSelected = RealPathFromURI.getRealPathFromURI(this, data.getData());
                Log.d(TAG, "Path: "+imagePathSelected);

                File f = new File(imagePathSelected);
                iv_editImage.setImageURI(Uri.fromFile(f));

            }

        }
    }
}
