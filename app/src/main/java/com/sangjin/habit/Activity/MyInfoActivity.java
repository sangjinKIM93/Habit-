package com.sangjin.habit.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

public class MyInfoActivity extends AppCompatActivity {

    RetrofitService retrofitService;
    Retrofit retrofit;

    private FirebaseStorage storage;
    private static final int GALLERY_CODE = 20;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    ImageView iv_profile;
    private String imagePath;

    private String serverURL = "http://52.79.252.51/";
    private static String IP_ADDRESS = "52.79.252.51";
    private static String TAG = "profile_update";
    private String mJsonString;

    String uid;
    String image;
    TextView tv_email;

    //동적인 권한 획득. 갤러리 접근을 위해서 추가적으로 해줘야해.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    startStorage();
                } else {
                    // Permission Denied
                    Toast.makeText(MyInfoActivity.this, "READ_STORAGE Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startStorageWrapper() {
        int permission = ActivityCompat.checkSelfPermission(MyInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            return;
        }
        startStorage();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_info);

        //FireStorage 객체 선언
        storage = FirebaseStorage.getInstance();

        //레트로핏 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        //레트로핏 서비스 객체 선언
        retrofitService = retrofit.create(RetrofitService.class);

        //shared에 사진 주소 추가.
        SharedPreferences pref = getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");

//        /*권한*/
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
//        }

        //아이디, 이름 담는 뷰 선언
        tv_email = findViewById(R.id.tv_email);
        tv_email.setText(uid);

        //db에서 사진 받아서 넣어주기
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/get_profile.php", uid);

        //로그아웃
        Button btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 값(ALL Data) 삭제하기
                SharedPreferences pref = getSharedPreferences("loginState", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();
                Toast.makeText(MyInfoActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

                finish();

            }
        });

        //이미지 업로드
        iv_profile = findViewById(R.id.iv_profile);
        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStorageWrapper();
            }
        });

    }

    private void startStorage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

        startActivityForResult(intent, GALLERY_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_CODE) {

            if (data != null) {   //선택 안 하고 그냥 뒤로가기 눌렀을때 nullpointerror 잡기
                //imageview에 해당 사진 넣어주기
                imagePath = getPath(data.getData());
                File f = new File(imagePath);
                iv_profile.setImageURI(Uri.fromFile(f));

                upload(imagePath);
            }

        }
    }

    StorageReference riversRef;

    private void upload(String uri) {
        /*레트로핏을 활용한 서버 업로드*/
        Log.d(TAG, "uploadImageToServer");
        Log.d(TAG, "Path: " + imagePath);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        ArrayList<MultipartBody.Part> imageList = new ArrayList<>();

        File file = new File(imagePath);        //파일을 가져와서
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);     //requestBody라는 객체에 담고
        MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("files[]", imagePath, requestBody);    //multipartbody라는 객체에 마지막으로 넣어서
        imageList.add(uploadFile);

        //email정보 같이 보내기
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), uid);

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<ResponseBody> call = retrofitService.uploadImage(imageList, email);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: " + response);

                try {
                    String result = response.body().string();

                    if (response.isSuccessful()) {
                        Log.d(TAG, "isSuceessful: " + result);

                        SharedPreferences pref = getSharedPreferences("loginState", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("imagePath", result);
                        editor.commit();

                        //글라이드 로딩중 처리
                        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(MyInfoActivity.this);
                        circularProgressDrawable.setStrokeWidth(5f);
                        circularProgressDrawable.setCenterRadius(30f);
                        circularProgressDrawable.start();

                        //저장이 완료되면 imageview로 띄워주기
                        Glide.with(MyInfoActivity.this).load(result).into(iv_profile);

                        //이미지 경로를 db에 저장해주기.
                        //아 근데 이거는 그냥 서버단에서 해주면 되잖아?

                    } else {
                        Log.d(TAG, "isn't Suceessful: " + result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });




        /*Fire Strage를 통한 업로드*/
//        StorageReference storageRef = storage.getReferenceFromUrl("gs://lebit-a1fe5.appspot.com");
//
//        Uri file = Uri.fromFile(new File(uri));
//        riversRef = storageRef.child("images/" + file.getLastPathSegment());
//        UploadTask uploadTask = riversRef.putFile(file);
//
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
//
//                        SharedPreferences pref = getSharedPreferences("loginState", MODE_PRIVATE);
//                        SharedPreferences.Editor editor = pref.edit();
//                        editor.putString("imagePath", String.valueOf(uri));
//                        editor.commit();
//
//                        InsertData task = new InsertData();
//                        task.execute("http://" + IP_ADDRESS + "/profile_update.php", uid, String.valueOf(uri));
//                    }
//                });
//
//            }
//        });

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


    class InsertData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d("ImageUpdate", "POST response  - " + result);

        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = (String) params[0];
            String uid = (String) params[1];
            String image = (String) params[2];
            Log.d("php 전 background", image);
            String postParameters = "&uid=" + uid + "&image=" + image;

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
                Log.d("ImageUpdate", "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString();

            } catch (Exception e) {

                Log.d("ImageUpdate", "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }


    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MyInfoActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response - " + result);

            if (result == null) {
                Log.d(TAG, "response - " + errorString);
            } else {

                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String uid = params[1];

            String postParameters = "uid=" + uid;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString().trim();

            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void showResult() {

        String TAG_JSON = "webnautes";
        String TAG_IMAGE = "image";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                image = item.getString(TAG_IMAGE);

                //글라이드 로딩중 처리
                CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(this);
                circularProgressDrawable.setStrokeWidth(5f);
                circularProgressDrawable.setCenterRadius(30f);
                circularProgressDrawable.start();

                Log.d("ImagePath : ", image);
                Glide.with(this)
                        .load(image)
                        .placeholder(R.drawable.profile)
                        .override(550, 550).into(iv_profile);


            }

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }

    }
}
