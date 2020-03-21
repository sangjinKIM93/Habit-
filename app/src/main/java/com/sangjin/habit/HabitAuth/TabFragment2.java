package com.sangjin.habit.HabitAuth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sangjin.habit.Chatting.NewChatActivity;
import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

public class TabFragment2 extends Fragment {

    private Context mContext;
    ViewGroup rootView;

    private ArrayList<HabitAuthData> mArrayList;
    private HabitAuthAdapter mAdapter;
    private RecyclerView mRecyclerView;
    LinearLayoutManager linearLayoutManager;

    RetrofitService retrofitService;
    Retrofit retrofit;
    private String serverURL = "http://52.79.252.51/";
    private String TAG = "HabitAuthFragment";

    EditText et_habitAuth;
    Button btn_habitAuth;
    String uid, name;
    int togetherIdx;
    String uidCaptain;
    TextView tv_protectFragment2;
    RelativeLayout relative_fragment2;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exitChat:
                Intent intent = new Intent(mContext, NewChatActivity.class);
                intent.putExtra("IDX", togetherIdx);
                startActivity(intent);
                return true;

            default:
                return false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.tab_fragment_2, container, false);
        mContext = getActivity();

        setHasOptionsMenu(true);

        //쉐어드로 로그인한 아이디 가져오기
        SharedPreferences pref = mContext.getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");
        name = pref.getString("name", "null");

        //et_habitAuth = rootView.findViewById(R.id.et_habitAuth);
        btn_habitAuth = rootView.findViewById(R.id.btn_habitAuth);
        tv_protectFragment2 = rootView.findViewById(R.id.tv_protectFragment2);
        relative_fragment2 = rootView.findViewById(R.id.relative_fragment2);

        //모임 번호 받아오기
        Intent intent = getActivity().getIntent();
        togetherIdx = intent.getIntExtra("IDX", -1);
        //uidCaptain = intent.getStringExtra("UID");

        //레트로핏 객체 및 레트로핏 서비스 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();
        retrofitService = retrofit.create(RetrofitService.class);

        //신청한 인원인지 확인하기. 1.방장 아이디와 같은 경우 2.신청자 목록에 있는 경우
//        if(uidCaptain.equals(uid)){
//            relative_fragment2.setVisibility(View.VISIBLE);
//            tv_protectFragment2.setVisibility(View.GONE);
//        }else{
//            checkApply();
//        }


        //리사이클러뷰 셋팅
        recyclerViewSetting();



        //클릭 리스너 모음
        clickList();

        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();
        checkApply();
        //초기 데이터 받아오기
        getAuthData();
    }

    private void deleteAlert(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("해당 인증을 삭제하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        deleteHabitAuth(mArrayList.get(position).getIdx());

                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(mContext, "삭제 취소", Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }

    //클릭리스너 모음
    private void clickList() {
        //인증버튼 클릭시
        btn_habitAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, HabitAuthAddActivity.class);
                intent.putExtra("togetherIdx", togetherIdx);
                startActivity(intent);
                //pushAuthData();

            }
        });

        //text 인증 삭제
        mAdapter.setOnItemClickListener4(new HabitAuthAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                deleteAlert(position);
            }
        });

        //image 인증 삭제
        mAdapter.setOnItemClickListener3(new HabitAuthAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                deleteAlert(position);
            }
        });

        //video 인증 삭제
        mAdapter.setOnItemClickListener2(new HabitAuthAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                deleteAlert(position);
            }
        });

        //영상 클릭시
        mAdapter.setOnItemClickListener(new HabitAuthAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

                String url = mArrayList.get(position).getImagePath();

                CustomDialogVideo oDialog = new CustomDialogVideo(mContext, position, url);
                oDialog.setCancelable(false);
                oDialog.show();

            }
        });
    }

    private void deleteHabitAuth(int idx) {
        //레트로핏 서비스로 php와 연결하기(서버를 call하기)
        Call<ResponseBody> call = retrofitService.deleteHabitAuth(idx);  //call을 보내는 것
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Delete Success : " + response);
                getAuthData();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Delete Failed : " + t);
            }
        });
    }


    public void checkApply() {
        //레트로핏 서비스로 php와 연결하기(서버를 call하기)
        Call<ResponseBody> call = retrofitService.checkApply(togetherIdx, uid);  //call을 보내는 것
        Log.d(TAG, "콜이 갔는지?");
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    String result = null;
                    result = response.body().string();
                    if (response.isSuccessful()) {
                        if (result.equals("ok")) {
                            Log.d(TAG, "checkApply : " + result);
                            relative_fragment2.setVisibility(View.VISIBLE);

                        } else {
                            Log.d(TAG, "checkApply : " + result);
                            tv_protectFragment2.setVisibility(View.VISIBLE);
                        }

                    } else {
                        Log.d(TAG, "checkApply isn't Suceessful: " + result);
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


    //리사이클러뷰 셋팅
    private void recyclerViewSetting() {
        mArrayList = new ArrayList<>();
        mRecyclerView = rootView.findViewById(R.id.recycle_habitAuth);
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new HabitAuthAdapter((Activity) mContext, mArrayList);
        mRecyclerView.setAdapter(mAdapter);
    }


    //초기 데이터 가져오기
    private void getAuthData() {
        mArrayList.clear();
        Call<ResponseBody> call = retrofitService.getAuthData(togetherIdx);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = null;
                    result = response.body().string();
                    if (response.isSuccessful()) {

                        //recyclerView에 넣어주기
                        josnToRecycler(result);

                    } else {
                        Log.d(TAG, "Push isn't Suceessful: " + result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

    }


    //데이터 입력 및 출력
    private void pushAuthData() {
        String content = et_habitAuth.getText().toString();

        HashMap hashmap = new HashMap();
        hashmap.put("togetherIdx", togetherIdx);        //intent로 받기
        hashmap.put("content", content);
        hashmap.put("uid", uid);                    //shared로 받기
        hashmap.put("imagePath", "null");        //일단 null로 해두고 나중에 이미지 첨부 기능 추가하면 수정하기.

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

                        //recyclerView에 넣어주기
                        josnToRecycler(result);


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


    //받은 json데이터 리사이클러뷰에 넣어주기
    private void josnToRecycler(String result) {
        mArrayList.clear();

        //JSONarray로 선언해주고 array에서 JSONobject를 하나씩 받아서 풀어줌.
        JSONArray jsonArray = null;
        try {

            String dateBefore = "null";

            jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String idx = jsonObject.getString("idx");
                String content = jsonObject.getString("content");
                String name = jsonObject.getString("name");
                String userImage = jsonObject.getString("userImage");
                String created = jsonObject.getString("created");
                String imagePath =  jsonObject.getString("imagePath");

                String date = created.substring(0, 10);

                //날짜가 다르다면 날짜 표시 데이터 추가 생성
                if (!dateBefore.equals(date)) {
                    //Log.d(TAG, "둘이 다르다 : "+String.valueOf(!dateBefore.equals(date)));
                    HabitAuthData habitAuthData = new HabitAuthData();
                    habitAuthData.setIdx(Integer.parseInt(idx));
                    habitAuthData.setUid(name);
                    habitAuthData.setContent(date);
                    habitAuthData.setUserImage("showDate");
                    habitAuthData.setViewType(1);
                    mArrayList.add(habitAuthData);
                }
                dateBefore = date;

                //그냥 가져올때는 잘 된다.
                //근데 데이터를 넣고 다시 가져오면 에러가 뜬다.
                //또 이상한건 로그에서는 이상이 없다는 거다.
                int viewType;

                Log.d("HabitAuth ImagePath : ", imagePath);
                if(imagePath.contains("http://52.79.252.51/habitAuth/uploads/")){
                    if(imagePath.contains(".mp4")){
                        viewType = 3;
                    }else{
                        viewType = 2;
                    }
                    Log.d("ViewType: ", String.valueOf(viewType));
                }else{
                    viewType = 0;
                }

                HabitAuthData habitAuthData = new HabitAuthData();
                habitAuthData.setIdx(Integer.parseInt(idx));
                habitAuthData.setUid(name);
                habitAuthData.setContent(content);
                habitAuthData.setUserImage(userImage);
                habitAuthData.setViewType(viewType);
                habitAuthData.setImagePath(imagePath);
                mArrayList.add(habitAuthData);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mAdapter.notifyDataSetChanged();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        });

    }


}
