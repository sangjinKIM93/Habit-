package com.sangjin.habit.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sangjin.habit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static String IP_ADDRESS = "52.79.252.51";
    private static String TAG = "Register";

    private EditText et_email, et_password, et_name, et_passwordAgain;
    private TextView tv_error;
    private Button btn_register, btn_idCheck;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_PASSWOLD_REGEX_ALPHA_NUM = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    private String mJsonString;
    public Boolean idChecked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_passwordAgain = findViewById(R.id.et_passwordAgain);
        et_name = findViewById(R.id.et_name);
        tv_error = findViewById(R.id.tv_error);

        btn_idCheck = findViewById(R.id.btn_idCheck);
        btn_idCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetDataID taskID = new GetDataID();
                taskID.execute( "http://" + IP_ADDRESS + "/checkID.php", "");
            }
        });

        btn_register =findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(idChecked) {  //id 중복 확인이 되어야 등록할 수 있도록
                    Boolean validation = validate();    //유효성 검사

                    if (validation) {
                        String email = et_email.getText().toString();
                        String password = et_password.getText().toString();
                        String name = et_name.getText().toString();

                        InsertData task = new InsertData();
                        task.execute("http://" + IP_ADDRESS + "/register.php", email, password, name);
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, "이메일 중복을 확인하세요.", Toast.LENGTH_SHORT).show();
                    et_email.requestFocus();
                }

            }

            //유효성 검사
            private boolean validate() {

                Util util = new Util(); //정규식을 위한 클래스 호출

                if ( et_email.getText().toString().length() == 0 ) {
                    Toast.makeText(RegisterActivity.this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                    et_email.requestFocus();
                    return false;     //끝내라는 뜻. 뭘 끝내라는건데? 함수를 마친다. 여기서는 onclick이라는 함수. 그러면 굳이 뒤 명령을 if문으로 덮을 필요가 없어.
                }

                if(!util.validateEmail(et_email.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "이메일 형식에 맞춰 입력하세요", Toast.LENGTH_SHORT).show();
                    et_email.requestFocus();
                    return false;
                }

                if ( et_password.getText().toString().length() == 0 ) {
                    Toast.makeText(RegisterActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                    et_password.requestFocus();
                    return false;
                }

                if(!util.validatePassword(et_password.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "비밀번호는 4~16자리 입니다.", Toast.LENGTH_SHORT).show();
                    et_password.requestFocus();
                    return false;
                }

                if ( et_passwordAgain.getText().toString().length() == 0 ) {
                    Toast.makeText(RegisterActivity.this, "비밀번호 확인을 입력하세요", Toast.LENGTH_SHORT).show();
                    et_passwordAgain.requestFocus();
                    return false;
                }

                if ( et_name.getText().toString().length() == 0 ) {
                    Toast.makeText(RegisterActivity.this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();
                    et_name.requestFocus();
                    return false;
                }

                if ( !et_password.getText().toString().equals(et_passwordAgain.getText().toString()) ) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    et_password.setText("");
                    et_passwordAgain.setText("");
                    et_password.requestFocus();
                    return false;
                }

                return true;
            }
        });

    }


    class InsertData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(RegisterActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {   //doInBackground가 완료되면 호출. 즉, result는 doinbackground return값.
            super.onPostExecute(result);

            Log.d(TAG, "POST response  - " + result);

            progressDialog.dismiss();
            if(result.equals("success")){
                Toast.makeText(RegisterActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }else{
                Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected String doInBackground(String... params) {     //background 작업 공간  ,   params가 task실행시 execute로 입력한 3가지 변수

            String email = (String)params[1];
            String password = (String)params[2];
            String name = (String)params[3];

            String serverURL = (String)params[0];
            String postParameters = "email=" + email + "&password=" + password + "&name=" + name;     //전송할 데이터 형식이 "key:value" 형식

            try {
                //POST 방식으로 데이터 전송
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));   //전송할 데이터 입력
                outputStream.flush();
                outputStream.close();

                //응답 읽기
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    //정상적인 응답 데이터
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                //수신되는 데이터를 저장(stringBuilder 사용)
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();     //왜? append를 사용하기 위함. 아마 아니여서 돌아가긴 할거야.
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

    public static class AeSimpleSHA1 {
        private static String convertToHex(byte[] data) {
            StringBuilder buf = new StringBuilder();
            for (byte b : data) {
                int halfbyte = (b >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                    halfbyte = b & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        }

        public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] textBytes = text.getBytes("iso-8859-1");
            md.update(textBytes, 0, textBytes.length);
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        }
    }

    //이메일, 비밀번호 정규식 검사를 위한 클래스
    public static class Util{
        public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

        public static boolean validateEmail(String emailStr) {
            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
            return matcher.find();
        }

        public static final Pattern VALID_PASSWOLD_REGEX_ALPHA_NUM = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

        public static boolean validatePassword(String pwStr) {
            Matcher matcher = VALID_PASSWOLD_REGEX_ALPHA_NUM.matcher(pwStr);
            return matcher.matches();
        }
    }

    private class GetDataID extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(RegisterActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            tv_error.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){

                tv_error.setText(errorString);
            }
            else {
                mJsonString = result;
                Boolean resultID = showResult();
                if(resultID){
                    tv_error.setText("사용가능한 아이디입니다.");
                    idChecked = true;       //아이디 중복체크 해야만 등록 버튼 누를 수 있도록
                }else{
                    tv_error.setText("다른 아이디를 입력해주세요");
                    et_email.setText("");   //아이디가 있을 경우 text 지우기.
                }
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];      //json 받아오는거라 key:value 형식은 아닌듯.


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

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    //json데이터를 받아서 recyclerview에 추가.
    private boolean showResult(){

        String TAG_JSON="emailCheck";
        String TAG_EMAIL = "email";

        String emailInput = et_email.getText().toString();

        Util util = new Util(); //정규식을 위한 클래스 호출

        if(!util.validateEmail(emailInput)){
            Toast.makeText(RegisterActivity.this, "이메일 형식에 맞춰 입력하세요", Toast.LENGTH_SHORT).show();
            et_email.requestFocus();
            return false;
        }

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String email = item.getString(TAG_EMAIL);

                Log.d(TAG, email);
                Log.d(TAG, emailInput);
                if(emailInput.equals(email)){
                    return false;
                }

            }



        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

        return true;
    }

}
