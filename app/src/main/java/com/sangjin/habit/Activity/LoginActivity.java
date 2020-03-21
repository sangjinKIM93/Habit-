package com.sangjin.habit.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sangjin.habit.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private static String IP_ADDRESS = "52.79.252.51";
    private static String TAG = "logintest";

    private EditText et_loginEmail, et_loginPwd;
    private Button btn_login, btn_toRegister;
    String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        et_loginEmail= findViewById(R.id.et_loginEmail);
        et_loginPwd = findViewById(R.id.et_loginPwd);

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = et_loginEmail.getText().toString();
                String password = et_loginPwd.getText().toString();

                GetData task = new GetData();
                task.execute( "http://" + IP_ADDRESS + "/checkLogin.php", email, password);

            }
        });

        btn_toRegister = findViewById(R.id.btn_toRegister);
        btn_toRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
               startActivity(i);
            }
        });
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response - " + result);

            if (result == null){
                Toast.makeText(LoginActivity.this, errorString, Toast.LENGTH_SHORT).show();
            }
            else {
                String strSplit[] = result.split(">");
                String resultParsing = strSplit[0];
                String name = strSplit[1];
                String imagePath = strSplit[2];

                if(resultParsing.equals("success")) {
                    //shared에 이메일 저장.(로그인 상태 체크를 위함)
                    SharedPreferences pref = getSharedPreferences("loginState", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("email", email);
                    editor.putString("name", name);
                    editor.putString("imagePath", imagePath);
                    editor.commit();

                    Toast.makeText(LoginActivity.this, "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show();
                    finish();

                }else{
                    Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
                }
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String email = (String)params[1];
            String password = (String)params[2];

            String serverURL = (String)params[0];
            String postParameters = "email=" + email + "&password=" + password;     //전송할 데이터 형식이 "key:value" 형식

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
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

//    //json데이터를 받아서 recyclerview에 추가.
//    private void showResult(){
//
//        String TAG_JSON="webnautes";
//        String TAG_ID = "id";
//        String TAG_NAME = "name";
//        String TAG_COUNTRY ="country";
//
//
//        try {
//            JSONObject jsonObject = new JSONObject(mJsonString);
//            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
//
//            for(int i=0;i<jsonArray.length();i++){
//
//                JSONObject item = jsonArray.getJSONObject(i);
//
//                String id = item.getString(TAG_ID);
//                String name = item.getString(TAG_NAME);
//                String country = item.getString(TAG_COUNTRY);
//
//                PersonalData personalData = new PersonalData();
//
//                personalData.setMember_id(id);
//                personalData.setMember_name(name);
//                personalData.setMember_country(country);
//
//                mArrayList.add(personalData);
//                mAdapter.notifyDataSetChanged();
//            }
//
//
//
//        } catch (JSONException e) {
//
//            Log.d(TAG, "showResult : ", e);
//        }
//
//    }
}
