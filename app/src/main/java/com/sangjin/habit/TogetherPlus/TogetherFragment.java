package com.sangjin.habit.TogetherPlus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sangjin.habit.Activity.LoginActivity;
import com.sangjin.habit.Activity.MyInfoActivity;
import com.sangjin.habit.Adapter.TogetherListAdapter;
import com.sangjin.habit.DataType.TogetherData;
import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

public class TogetherFragment extends Fragment {

    private Button btn_toLogin, btn_myInfo;
    private ArrayList<TogetherData> cArrayList;
    private TogetherListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private Context mContext;

    FloatingActionButton fab_together;

    private static String IP_ADDRESS = "52.79.252.51";
    private static String TAG = "Register";
    private String mJsonString;

    String email;

    RetrofitService retrofitService;
    Retrofit retrofit;

    private String serverURL = "http://52.79.252.51/";

    ViewGroup rootView;

    LinearLayoutManager linearLayoutManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.together_list, container, false);
        mContext = getActivity();

        setHasOptionsMenu(true);

        //레트로핏 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        //레트로핏 서비스 객체 선언
        retrofitService = retrofit.create(RetrofitService.class);

        cArrayList=new ArrayList<>();//먼저선언해야추가할수있어
        mRecyclerView=(RecyclerView)rootView.findViewById(R.id.recycle_together);
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter= new TogetherListAdapter((Activity) mContext, cArrayList);
        mRecyclerView.setAdapter(mAdapter);

        //로그인 버튼 설정
        btn_toLogin = rootView.findViewById(R.id.btn_toLogin);
        btn_toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, LoginActivity.class));
            }
        });

        //내정보 버튼 설정
        btn_myInfo = rootView.findViewById(R.id.btn_myInfo);
        btn_myInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, MyInfoActivity.class));
            }
        });

        //플로팅바 등록
        fab_together = rootView.findViewById(R.id.fab_together);
        fab_together.setOnClickListener(new FABClickListener());

        //스크롤 아래로 내릴시 플로팅바 사라지도록
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    // 아래로 스크롤
                    fab_together.hide();
                } else if (dy < 0) {
                    // 위로 스크롤
                    fab_together.show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().invalidateOptionsMenu();

        cArrayList.clear();
        getTogetherList();

        //로그인 상태 체크 후 버튼 상태 변화
        SharedPreferences pref = mContext.getSharedPreferences("loginState", MODE_PRIVATE);
        email = pref.getString("email", "null");

        if(email.equals("null")){
            btn_toLogin.setVisibility(View.VISIBLE);
            btn_myInfo.setVisibility(View.INVISIBLE);
        }else{
            btn_toLogin.setVisibility(View.INVISIBLE);
            btn_myInfo.setVisibility(View.VISIBLE);
        }
    }

    private void clickEvent(){
        //단순 클릭 리스너
        mAdapter.setOnItemClickListener(new TogetherListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

                //로그인이 된 경우만 클릭 가능하도록 설정
                if(email.equals("null")){
                    Toast.makeText(mContext, "로그인이 필요한 서비스입니다.", Toast.LENGTH_SHORT).show();

                }else{
                    String title = cArrayList.get(position).getTitle();
                    String content = cArrayList.get(position).getContent();
                    int idx = cArrayList.get(position).getId();
                    String imagePath = cArrayList.get(position).getImage();   //image경로.
                    String uid = cArrayList.get(position).getUid();

                    Intent intent = new Intent(mContext, TogetherTabActivity.class);
                    intent.putExtra("TITLE", title);
                    //intent.putExtra("CONTENT", content);
                    intent.putExtra("IDX", idx);
                    //intent.putExtra("IMAGEPATH", imagePath);
                    //intent.putExtra("UID", uid);
                    startActivity(intent);
                }

            }
        });
    }

    private void getTogetherList(){
        Call<ResponseBody> call = retrofitService.getTogetherList();  //call을 보내는 것
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "getTogetherData : "+response);
                String result = null;
                try {
                    result = response.body().string();
                    if(response.isSuccessful()){

                        mJsonString = result;
                        showResult();

                    }else{
                        Log.d(TAG, "getTogetherData isn't Suceessful: "+result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Delete Failed : "+t);
            }
        });
    }

    //urlconnect를 이용한 처리
    private void getList(){
        //기존 데이터 삭제하고
        cArrayList.clear();
        mAdapter.notifyDataSetChanged();

        //db에서 자료 받아서 넣어주기
        GetData task = new GetData();
        task.execute( "http://" + IP_ADDRESS + "/together_list.php", "");

        //view에 대한 클릭리스너 달아주기.
        clickEvent();
    }

    //플로팅바 액션 처리
    private class FABClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            // FAB Click 이벤트 처리 구간
            startActivity(new Intent(mContext, TogetherAddActivity.class));
        }
    }

    private class GetData extends AsyncTask<String, Void, String> {


        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(TAG, "response - " + result);

            if (result == null){
                Log.d(TAG, "response - " + errorString);
            }
            else {

                mJsonString = result;
                showResult();
                //progressDialog.dismiss();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];


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

    private void showResult(){

        String TAG_JSON="webnautes";
        String TAG_ID = "id";
        String TAG_TITLE = "title";
        String TAG_CONTENT="content";
        String TAG_UID="uid";
        String TAG_CREATED="created";
        String TAG_Image="image";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String id = item.getString(TAG_ID);
                String title = item.getString(TAG_TITLE);
                String content = item.getString(TAG_CONTENT);
                String created = item.getString(TAG_CREATED);
                String uid = item.getString(TAG_UID);
                String image;
                image = item.getString(TAG_Image);
                //image = image.replace("images/", "images%2F");

                TogetherData togetherData = new TogetherData();
                togetherData.setId(Integer.parseInt(id));
                togetherData.setTitle(title);
                togetherData.setContent(content);
                togetherData.setUid(uid);
                togetherData.setCreated(created);
                togetherData.setImage(image);

                cArrayList.add(togetherData);
                mAdapter.notifyDataSetChanged();
            }


        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

        //view에 대한 클릭리스너 달아주기.
        clickEvent();

        ProgressBar progressBar = rootView.findViewById(R.id.loading_togetherList);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycle_together);

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

    }
}
