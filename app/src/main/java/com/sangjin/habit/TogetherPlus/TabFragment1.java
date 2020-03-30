package com.sangjin.habit.TogetherPlus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.sangjin.habit.Adapter.ApplicationListAdapter;
import com.sangjin.habit.Adapter.PagerAdapter;
import com.sangjin.habit.Chatting.NewChatActivity;
import com.sangjin.habit.DataType.ApplicationData;
import com.sangjin.habit.HabitAuth.TabFragment2;
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

public class TabFragment1 extends Fragment {

    private TextView tv_togetherViewTitle, tv_togetherViewContent, tv_applyCompleted;
    private ImageView iv_togetherViewMain;
    private Button btn_apply, btn_togetherEdit, btn_togetherDelete, btn_withdraw;

    private ArrayList<ApplicationData> mArrayList;
    private ApplicationListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    RetrofitService retrofitService;
    Retrofit retrofit;

    private String serverURL = "http://52.79.252.51/";

    private static String IP_ADDRESS = "52.79.252.51";
    private static String TAG = "Register";

    int togetherIdx;
    private String mJsonString;
    String uid, name, title, content, imagePath;
    String uidCaptain;

    private Context mContext;
    ViewGroup rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.together_view, container, false);
        mContext = getActivity();

        setHasOptionsMenu(true);

        //레트로핏 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        //레트로핏 서비스 객체 선언
        retrofitService = retrofit.create(RetrofitService.class);

        btn_togetherEdit = rootView.findViewById(R.id.btn_togetherEdit);
        btn_togetherDelete = rootView.findViewById(R.id.btn_togetherDelete);
        btn_withdraw = rootView.findViewById(R.id.btn_withdraw);

        //제목과 내용 채우기
        tv_togetherViewTitle = rootView.findViewById(R.id.tv_togetherViewTitle);
        tv_togetherViewContent = rootView.findViewById(R.id.tv_togetherViewContent);
        tv_applyCompleted = rootView.findViewById(R.id.tv_applyCompleted);
        iv_togetherViewMain = rootView.findViewById(R.id.iv_togetherViewMain);

        //해당 모임 idx
        Intent intent = getActivity().getIntent();
        togetherIdx = intent.getIntExtra("IDX", -1);

        //로그인한 아이디 가져오기
        SharedPreferences pref = mContext.getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");
        name = pref.getString("name", "null");

        //버튼 클릭 모음
        clickBtn();

        //신청인원 리사이클러뷰
        mArrayList = new ArrayList<>();//먼저선언해야추가할수있어
        mRecyclerView = rootView.findViewById(R.id.application_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ApplicationListAdapter((Activity) mContext, mArrayList);
        mRecyclerView.setAdapter(mAdapter);

//        getApplyList();       //getData() 다음 실행되는 것으로 수정

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //db에서 해당 together에 신청한 목록 가져오기
        getData();

    }

    //클릭 버튼 모음
    private void clickBtn() {
        //삭제 버튼
        btn_togetherDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });

        btn_togetherEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit();
            }
        });

        //신청하기 버튼
        btn_apply = rootView.findViewById(R.id.btn_apply);
        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

        //모임 나가기 버튼
        btn_withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                witdrawApply();
            }
        });
    }

    private void witdrawApply() {
        //레트로핏 서비스로 php와 연결하기(서버를 call하기)
        Call<ResponseBody> call = retrofitService.withdrawApply(uid);  //call을 보내는 것
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Delete Success : " + response);
                Toast.makeText(mContext, "모임을 나갑니다.", Toast.LENGTH_SHORT).show();
                //fragment가 담긴 tab layout액티비티 종료
                getActivity().finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Delete Failed : " + t);
            }
        });
    }



    //모임 정보 받아오기
    private void getData() {
        Call<ResponseBody> call = retrofitService.getTogetherData(togetherIdx);  //call을 보내는 것
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "getTogetherData : " + response);
                String result = null;
                try {
                    result = response.body().string();
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = new JSONObject(result);
                        title = jsonObject.getString("title");
                        content = jsonObject.getString("content");
                        imagePath = jsonObject.getString("imagePath");
                        uidCaptain = jsonObject.getString("uid");

                        //intent로 데이터 받아서 넣어주기. text&image
                        tv_togetherViewTitle.setText(title);
                        tv_togetherViewContent.setText(content);

                        if (imagePath.equals("null")) {
                            iv_togetherViewMain.setImageResource(R.drawable.training);
                        } else {
                            //Glide.with(mContext).load(imagePath).placeholder(R.drawable.loading).override(200,200).into(iv_togetherViewMain);
                            //글라이드 로딩중 처리
                            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(mContext);
                            circularProgressDrawable.setStrokeWidth(5f);
                            circularProgressDrawable.setCenterRadius(30f);
                            circularProgressDrawable.start();

                            Glide.with(mContext)
                                    .load(imagePath)
                                    .placeholder(circularProgressDrawable)
                                    .override(600, 400).into(iv_togetherViewMain);
                        }

                        //글 작성자일 경우 수정, 삭제 버튼 보이게 하기
                        if (uid.equals(uidCaptain)) {
                            btn_togetherEdit.setVisibility(View.VISIBLE);
                            btn_togetherDelete.setVisibility(View.VISIBLE);
                            tv_applyCompleted.setVisibility(View.VISIBLE);
                        } else {
                            btn_togetherEdit.setVisibility(View.GONE);
                            btn_togetherDelete.setVisibility(View.GONE);
                            btn_apply.setVisibility(View.GONE);
                        }
                        //db에서 해당 together에 신청한 목록 가져오기
                        getApplyList();


                    } else {
                        Log.d(TAG, "getTogetherData isn't Suceessful: " + result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Delete Failed : " + t);
            }
        });

    }


    private void delete() {
        //레트로핏 서비스로 php와 연결하기(서버를 call하기)
        Call<ResponseBody> call = retrofitService.deleteGathering(togetherIdx);  //call을 보내는 것
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Delete Success : " + response);
                //fragment가 담긴 tab layout액티비티 종료
                getActivity().finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Delete Failed : " + t);
            }
        });
    }


    private void edit() {

        //1. 수정화면을 띄운다.(이때 기존 자료를 받아온다.) -> intent받아온걸로
        Intent intent = new Intent(mContext, TogetherEditActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("imagePath", imagePath);
        intent.putExtra("togetherIdx", togetherIdx);
        startActivity(intent);
        //2. 수정된 내용을 다시 저장한다.

        //3. 저장된 내용을 새로고침해서 보여준다.
    }


    private void show() {
        final EditText edittext = new EditText(mContext);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("신청");
        builder.setMessage("각오를 적어주세요.");
        builder.setView(edittext);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //shared로 아이디 받아오고, intent로 togetherIdx받아오기
                        String resolution = edittext.getText().toString();

                        InsertData task = new InsertData();
                        task.execute("http://" + IP_ADDRESS + "/apply_add.php", String.valueOf(togetherIdx), name, uid, resolution);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    private void getApplyList() {
        //기존 데이터 삭제하고
        mArrayList.clear();
        mAdapter.notifyDataSetChanged();

        //db에서 자료 받아서 넣어주기
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/apply_list.php", String.valueOf(togetherIdx), uidCaptain);

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

            if (result == null) {
                Log.d(TAG, "response - " + errorString);

            } else {

                mJsonString = result;
                Log.d("받아온 JSON : ", mJsonString);
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String togetherIdx = params[1];
            String uidCaptain = params[2];

            String postParameters = "togetherIdx=" + togetherIdx + "&uidCaptain=" + uidCaptain;

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
        String TAG_JSON = "applyList";
        String TAG_NAME = "name";
        String TAG_RESOLUTION = "resolution";
        String TAG_CREATED = "created";
        String TAG_UID = "uid";
        String TAG_IMAGE = "image";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            Boolean applied = false;
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject item = jsonArray.getJSONObject(i);

                String nameAll = item.getString(TAG_NAME);
                String resolution = item.getString(TAG_RESOLUTION);
                String created = item.getString(TAG_CREATED);
                String uidAll = item.getString(TAG_UID);
                String image = item.getString(TAG_IMAGE);
                image = image.replace("images/", "images%2F");

                //신청목록으로 그려줄 list 넣어주기
                ApplicationData applicationData = new ApplicationData();
                applicationData.setName(nameAll);
                applicationData.setResloution(resolution);
                applicationData.setImage(image);

                mArrayList.add(applicationData);
                mAdapter.notifyDataSetChanged();

                //이미 신청한 경우 버튼 모양 변경
                if (uidAll.equals(uid)) {
                    applied = true;
                }

            }

            //신청을 한 경우 -> '신청되었습니다' _ 그렇지 않은 경우 -> '신청하기'
            if (applied) {
                btn_apply.setVisibility(View.INVISIBLE);
                tv_applyCompleted.setVisibility(View.VISIBLE);

                if (!uid.equals(uidCaptain)) {
                    btn_withdraw.setVisibility(View.VISIBLE);
                }
            } else {
                btn_apply.setVisibility(View.VISIBLE);
                tv_applyCompleted.setVisibility(View.INVISIBLE);
            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

        //여기서 로딩처리한다.
        ProgressBar progressBar = rootView.findViewById(R.id.loading_togetherView);
        RelativeLayout relativeLayout = rootView.findViewById(R.id.layout_togetherView);

        progressBar.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.VISIBLE);

    }


    class InsertData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(TAG, "POST response  - " + result);
            //추가 후 새로고침
            getApplyList();

//            //TabActivity 새로고침
//            Intent intent = new Intent(mContext, TogetherTabActivity.class);
//            intent.putExtra("IDX", togetherIdx);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);

        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = (String) params[0];
            String togetherIdx = (String) params[1];
            String name = (String) params[2];
            String uid = (String) params[3];
            String resolution = (String) params[4];

            String postParameters = "togetherIdx=" + togetherIdx + "&name=" + name + "&uid=" + uid + "&resolution=" + resolution;

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

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }
}
