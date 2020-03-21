package com.sangjin.habit.Crawling;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class CrawlingFragment extends Fragment {

    private ViewGroup rootView;
    private Context mContext;

    RetrofitService retrofitService;
    Retrofit retrofit;
    private String serverURL = "http://52.79.252.51/";

    private ArrayList<CrawlData> mArrayList;
    private CrawlListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    String uid, name, keywordGET;
    int togetherIdx;

    private ProgressBar loading_crawling;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_infomation, container, false);
        mContext = getActivity();

        setHasOptionsMenu(true);

        loading_crawling = rootView.findViewById(R.id.loading_crawling);

        //쉐어드로 로그인한 아이디 가져오기
        SharedPreferences pref = mContext.getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");
        name = pref.getString("name", "null");

        //모임 번호 받아오기
        Intent intent = getActivity().getIntent();
        togetherIdx = intent.getIntExtra("IDX", -1);

        //레트로핏 객체 및 레트로핏 서비스 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();
        retrofitService = retrofit.create(RetrofitService.class);

        //리사이클러뷰 셋팅
        mArrayList = new ArrayList<>();
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_crawling);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new CrawlListAdapter((Activity) mContext, mArrayList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        //크롤링 데이터 가져오기
        getInformation(togetherIdx);

        //삭제 버튼 클릭시
        mAdapter.setOnItemClickListener(new CrawlListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra("link", mArrayList.get(position).getLink());
                startActivity(intent);
            }
        });

        return rootView;
    }


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



    private void getInformation(int keywordGET){

        Call<ResponseBody> call = retrofitService.getCrawlingData(keywordGET);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "getCrawling : "+response);
                String result = null;
                try {
                    result = response.body().string();
                    if(response.isSuccessful()){
                        Log.d(TAG, "getCrawling Suceessful: "+result);

                        //데이터 넣어주기
                        josnToRecycler(result);

                    }else{
                        Log.d(TAG, "getCrawling isn't Suceessful: "+result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "getCrawling Failed : " + t);
            }
        });
    }


    private void josnToRecycler(String result) {
        //mArrayList.clear();

        //JSONarray로 선언해주고 array에서 JSONobject를 하나씩 받아서 풀어줌.
        JSONArray jsonArray = null;
        try {

            String dateBefore = "null";

            jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String title = jsonObject.getString("title");
                String link = jsonObject.getString("link");
                String thumbnail = jsonObject.getString("thumbnail");

                //특수문자(', &) 못 잡는 것 잡아주기.
                title = title.replaceAll("&#39;", "'");
                title = title.replaceAll("&amp;", "&");

                CrawlData crawlData = new CrawlData();
                crawlData.setLink(link);
                crawlData.setTitle(title);
                crawlData.setThumbnail(thumbnail);

                Log.d("title : ", title);
                Log.d("link : ", link);
                Log.d("thumbnail : ", thumbnail);

                mArrayList.add(crawlData);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mAdapter.notifyDataSetChanged();
        loading_crawling.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

    }

}
