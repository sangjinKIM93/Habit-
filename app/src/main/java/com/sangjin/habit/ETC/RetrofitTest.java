package com.sangjin.habit.ETC;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.sangjin.habit.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RetrofitTest extends AppCompatActivity {


    private static final int GALLERY_CODE = 20;
    String imagePath;
    File tempSelectFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_retrofit);

        /*갤러리 접근 권한*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        Button btn_retrofit = findViewById(R.id.btn_retrofit);
        btn_retrofit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_CODE) {
            if (data != null) {   //선택 안 하고 그냥 뒤로가기 눌렀을때 nullpointerror 잡기
                //imageview에 해당 사진 넣어주기
                imagePath = getPath(data.getData());
                File f = new File(imagePath);
                //iv_cardImage.setImageURI(Uri.fromFile(f));


                //선택한 이미지 임시 저장
//                try {
//                    InputStream in = getContentResolver().openInputStream(data.getData());
//                    Bitmap image = BitmapFactory.decodeStream(in);
//                    String date = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
//                    tempSelectFile = new File(imagePath);
//                    Log.d("이미지 경로 : ", imagePath);
//                    Log.d("파일 : ", String.valueOf(tempSelectFile));
//                    OutputStream out = new FileOutputStream(tempSelectFile);
//                    image.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                } catch (IOException ioe) {
//                    ioe.printStackTrace();
//                }

                //okHttp방식 업로드
                //okHttpUpload();


                imageUpload(imagePath);
            }
        }
    }



    private void okHttpUpload() {
        FileUploadUtils.send2Server(tempSelectFile);

    }

    private void imageUpload(String imagePath) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(RetrofitTest.this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        //Create a file object using file path
        File file = new File(imagePath);
        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        // Create MultipartBody.Part using file request-body,file name and part name
        MultipartBody.Part part = MultipartBody.Part.createFormData("upload", file.getName(), fileReqBody);
        //Create request body with text description and text media type
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
        //
        Call call = uploadAPIs.uploadImage(part, description);
        Log.d("명령어 끝:", "끝!");
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d("콜백 :", String.valueOf(response));
            }

            @Override
            public void onFailure(Call call, Throwable t) {
            }
        });
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
}
