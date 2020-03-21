package com.sangjin.habit.TogetherPlus;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import androidx.loader.content.CursorLoader;

import com.google.firebase.storage.FirebaseStorage;
import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TogetherAddActivity extends AppCompatActivity {

    RetrofitService retrofitService;
    Retrofit retrofit;
    private String serverURL = "http://52.79.252.51/";

    private EditText et_togetherTitle, et_togetherContent;
    private Button btn_togetherAdd;
    private ImageView iv_cardImage;

    private static String IP_ADDRESS = "52.79.252.51";
    private static String TAG = "Register";
    private static final int GALLERY_CODE = 20;

    private String imagePath = "null";
    private FirebaseStorage storage;
    private String uid;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.together_add);
        storage = FirebaseStorage.getInstance();

        //레트로핏 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        //레트로핏 서비스 객체 선언
        retrofitService = retrofit.create(RetrofitService.class);

        SharedPreferences pref = getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");

        /*갤러리 접근 권한*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        et_togetherTitle = findViewById(R.id.et_togetherTitle);
        et_togetherContent = findViewById(R.id.et_togetherContent);
        iv_cardImage = findViewById(R.id.iv_cardImage);
        iv_cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        btn_togetherAdd = findViewById(R.id.btn_togetherAdd);
        btn_togetherAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //firestorage에 등록 및 해당 주소로 db 저장
                //내 프로필은 update라 따로해줬는데 여기서는 insert니까 한방에 간다.
                if(imagePath.equals("null")){       //null일때는 fireStorage 거치지 않고 바로 db 등록

                    String title = et_togetherTitle.getText().toString();
                    String content = et_togetherContent.getText().toString();
                    InsertData task = new InsertData();
                    task.execute("http://" + IP_ADDRESS + "/together_add.php", title, content, uid, "null");

                } else {    //이미지를 선택한 경우 fireStorage에 등록하고 db저장.

                    uploadImageToServer();
                }


            }
        });
    }


    private void uploadImageToServer(){
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

        String title = et_togetherTitle.getText().toString();
        String content = et_togetherContent.getText().toString();

        RequestBody titleEdited = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody contentEdited = RequestBody.create(MediaType.parse("text/plain"), content);
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), uid);

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<ResponseBody> call = retrofitService.uploadTogetherImage(imageList, titleEdited, contentEdited, email);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: "+response);

                try {
                    String result = response.body().string();

                    if(response.isSuccessful()){
                        Log.d(TAG, "isSuceessful: "+result);
                        //result형식이 ["url"] 이라 괄호와 따옴표를 없애줘야함.

                        finish();
                        Toast.makeText(TogetherAddActivity.this, "모임이 등록되었습니다.", Toast.LENGTH_SHORT).show();

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


    //FireSTrage를 이용한 이미지 저장
//    StorageReference riversRef;
//    private void upload(String uri) {
//        StorageReference storageRef = storage.getReferenceFromUrl("gs://lebit-a1fe5.appspot.com");
//        Uri file = Uri.fromFile(new File(uri));
//        riversRef = storageRef.child("images/" + file.getLastPathSegment());
//        UploadTask uploadTask = riversRef.putFile(file);
//
//        progressDialog = ProgressDialog.show(TogetherAddActivity.this,
//                "Please Wait", null, true, true);
//        // Register observers to listen for when the download is done or if it fails
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle unsuccessful uploads
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Log.d("이미지 주소 : ", uri.toString());
//                        String title = et_togetherTitle.getText().toString();
//                        String content = et_togetherContent.getText().toString();
//
//                        InsertData task = new InsertData();
//                        task.execute("http://" + IP_ADDRESS + "/together_add.php", title, content, uid, String.valueOf(uri));
//                    }
//                });
//            }
//        });
//    }

    private class InsertData extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "POST response  - " + result);
            finish();
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = (String)params[0];
            String title = (String)params[1];
            String content = (String)params[2];
            String uid = (String)params[3];
            String image = (String)params[4];

            String postParameters = "title=" + title + "&content=" + content + "&uid=" + uid + "&image=" + image;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();
                return sb.toString();
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_CODE) {
            if(data != null){   //선택 안 하고 그냥 뒤로가기 눌렀을때 nullpointerror 잡기
                //imageview에 해당 사진 넣어주기
                imagePath = getPath(data.getData());
                File f = new File(imagePath);
                iv_cardImage.setImageURI(Uri.fromFile(f));

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
}
