package com.sangjin.habit.TogetherPlus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sangjin.habit.Chatting.ChatAdapter;
import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class TabFragment3 extends Fragment {

    private RecyclerView tv_message;
    private EditText et_message;
    private Button btn_sendMessage, btn_startChat;
    private SenderThread thread1;
    private Socket socket;
    private ViewGroup rootView;
    private Context mContext;
    private ReceiverThread thread2;

    private String uid, name, imagePath;

    private ArrayList<ChatData> mArrayList;
    private ChatAdapter mAdapter;
    private RecyclerView mRecyclerView;
    LinearLayoutManager linearLayoutManager;

    int togetherIdx;

    RetrofitService retrofitService;
    Retrofit retrofit;
    private String serverURL = "http://52.79.252.51/";
    //private Realm mRealm;

    String str, nameChat, contentChat, idxChat, imageChat;
    int viewType;

    RelativeLayout relative_chat;
    String fcmToken;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //채팅이 열렸을때만 메뉴 아이템 생성
        if(relative_chat.getVisibility() == View.VISIBLE){
            inflater.inflate(R.menu.actionbar_actions, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exitChat :

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("정말로 나가시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                exitChat();
                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mContext,"취소",Toast.LENGTH_LONG).show();
                            }
                        });
                builder.show();

                return true ;

            default :
                return false ;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.activity_chat, container, false);

        mContext = getActivity();
        setHasOptionsMenu(true);    //이게 있어야 actionBar에 옵션 메뉴를 띄울 수 있어.

        //레트로핏 객체 및 레트로핏 서비스 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();
        retrofitService = retrofit.create(RetrofitService.class);

        //로그인한 아이디 가져오기
        SharedPreferences pref = mContext.getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");
        name = pref.getString("name", "null");
        imagePath = pref.getString("imagePath", "null");

        Intent intent = getActivity().getIntent();
        togetherIdx = intent.getIntExtra("IDX", -1);

        //FCMtoken 받기
        fcmToken = FirebaseInstanceId.getInstance().getToken();

        //리사이클러뷰 셋팅
        recyclerViewSetting();

        tv_message = rootView.findViewById(R.id.tv_message);
        et_message = rootView.findViewById(R.id.et_message);
        btn_sendMessage = rootView.findViewById(R.id.btn_sendMessage);
        btn_startChat = rootView.findViewById(R.id.btn_startChat);
        relative_chat = rootView.findViewById(R.id.relative_chat);

        //버튼 클릭
        btnClickList();

        //채팅 시작 여부 체크. 해당idx에 해당 id가 있으면 button없애고 relative visible로 바꾸기.
        checkChatLog();


        return rootView;
    }


    private void btnClickList(){
        btn_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //소켓에서 message보내기
                ((SenderThread) thread1).sendMessage(et_message.getText().toString());
                //서버에 데이터 넣어주기
                updateChatData();
                //fcmMessage 보내기
                sendFCM();
            }
        });

        btn_startChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateChatLog();       //db에 채팅시작 여부 남기기
                createSocket();         //소켓 열기.
            }
        });
    }


    private void sendFCM() {
        Call<ResponseBody> call = retrofitService.sendFCM(togetherIdx, et_message.getText().toString());  //call을 보내는 것
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "sendFCM : "+response);
                String result = null;
                try {
                    result = response.body().string();
                    if(response.isSuccessful()){
                        Log.d(TAG, "sendFCM Suceessful: "+result);
                    }else{
                        Log.d(TAG, "sendFCM isn't Suceessful: "+result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "sendFCM Failed : "+t);
            }
        });
    }


    private void exitChat() {
        Call<ResponseBody> call = retrofitService.exitChat(togetherIdx, uid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(mContext, "채팅을 나갑니다.", Toast.LENGTH_SHORT).show();
                relative_chat.setVisibility(View.INVISIBLE);
                btn_startChat.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Delete Failed : " + t);
            }
        });
    }


    private void checkChatLog() {
        Call<ResponseBody> call = retrofitService.checkChatLog(togetherIdx, uid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = null;
                    result = response.body().string();
                    if(response.isSuccessful()){
                        if(result.equals("ok")){
                            Log.d(TAG, "checkChatLog : "+result);
                            relative_chat.setVisibility(View.VISIBLE);
                            btn_startChat.setVisibility(View.GONE);
                            createSocket();
                        }else{
                            Log.d(TAG, "checkChatLog : "+result);
                        }
                    }else{
                        Log.d(TAG, "checkChatLog isn't Suceessful: "+result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "checkChatLog Failed : "+t);
            }
        });
    }


    private void createSocket(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 일단은 테스트 용으로 본인의 아이피를 입력해서 진행하겠습니다.
                    socket = new Socket("52.79.252.51", 8000);
                    // 두번째 파라메터 로는 본인의 닉네임을 적어줍니다.

                    Log.d("쉐어드 imagePath : ", imagePath);

                    thread1 = new SenderThread(socket, name, togetherIdx, imagePath);
                    thread2 = new ReceiverThread(socket);

                    thread1.start();
                    thread2.start();

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }


    private void updateChatLog() {
        HashMap hashmap = new HashMap();
        hashmap.put("name", name);
        hashmap.put("uid", uid);
        hashmap.put("togetherIdx", togetherIdx);
        hashmap.put("fcmToken", fcmToken);

        Call<ResponseBody> call = retrofitService.updateChatLog(hashmap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = null;
                    result = response.body().string();
                    if(response.isSuccessful()){
                        Log.d("ChatLog success : ", result);

                        //ui변경.
                        btn_startChat.setVisibility(View.GONE);
                        relative_chat.setVisibility(View.VISIBLE);

                    }else{
                        Log.d("ChatLog isn't success", result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ChatLog Failed : ", String.valueOf(t));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //과거데이터 가져오기
        getChatBefore();
    }


    private void getChatBefore(){
        mArrayList.clear();

        Call<ResponseBody> call = retrofitService.getChatData(togetherIdx, uid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = null;
                    result = response.body().string();
                    if(response.isSuccessful()){

                        Log.d("getchat Suceessful: ",result);
                        josnToRecycler(result);

                    }else{
                        Log.d("chat isn't Suceessful: ",result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("getChat Failed : ", String.valueOf(t));
            }
        });

    }

    //받은 json데이터 리사이클러뷰에 넣어주기
    private void josnToRecycler(String result){
        mArrayList.clear();

        //JSONarray로 선언해주고 array에서 JSONobject를 하나씩 받아서 풀어줌.
        JSONArray jsonArray = null;
        try {

            String dateBefore = "null";

            jsonArray = new JSONArray(result);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String contentChat = jsonObject.getString("contentChat");
                String nameChat = jsonObject.getString("nameChat");
                String imageChat = jsonObject.getString("imageChat");
                String uidChat = jsonObject.getString("uidChat");
                String created = jsonObject.getString("created");

                //날짜가 다르다면 날짜 표시 데이터 추가 생성
                String date = created.substring(0,10);
                if(!dateBefore.equals(date)){
                    //Log.d(TAG, "둘이 다르다 : "+String.valueOf(!dateBefore.equals(date)));
                    ChatData chatData2 = new ChatData();
                    chatData2.setContent(date);
                    chatData2.setViewType(1);

                    mArrayList.add(chatData2);
                }
                dateBefore = date;

                //viewType 결정
                int viewType;
                if(uidChat.equals(uid)){
                    viewType=2;
                }else{
                    viewType=0;
                }

                ChatData chatData = new ChatData();
                chatData.setName(nameChat);
                chatData.setContent(contentChat);
                chatData.setImagePath(imageChat);
                chatData.setViewType(viewType);
                chatData.setCreated(created);

                mArrayList.add(chatData);


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


//    private void insertChatData() {
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                ChatData chatData = mRealm.createObject(ChatData.class);
//                chatData.setContent("test");
//                showResult();
//            }
//        });
//    }
//
//    private void deleteChatData() {
//        final RealmResults<ChatData> result = mRealm.where(ChatData.class).findAll();     //realmresult라는 객체로 생성되고 findAll은 다 가져오는거,
//
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                result.deleteAllFromRealm();
//            }
//        });
//    }

//    private void showResult() {
//        //저장된 데이터 가져와서 출력하기
//        RealmResults<ChatData> result = mRealm.where(ChatData.class).findAll();     //realmresult라는 객체로 생성되고 findAll은 다 가져오는거,
//
//        Log.d("Realm DB : ", result.toString());
//    }

    //리사이클러뷰 셋팅
    private void recyclerViewSetting() {
        mArrayList = new ArrayList<>();
        mRecyclerView = rootView.findViewById(R.id.tv_message);
        linearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ChatAdapter(mContext, mArrayList);
        mRecyclerView.setAdapter(mAdapter);
    }


    class ReceiverThread extends Thread {


        Socket socket;

        public ReceiverThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            //스트림을 읽고 출력
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                while (true) {

                    str = reader.readLine();
                    if (str == null) {
                        break;
                    }

                    Log.d("Receiver : ", str);

                    String strSplit[] = str.split(">");
                    nameChat = strSplit[0];

                    if (nameChat.equals("access")) {
                        //대화 내역이 아닌 입장, 퇴장 문구 출력.
                        contentChat = strSplit[1];
                        idxChat = strSplit[2];
                        imageChat = "";
                    } else {
                        contentChat = strSplit[1];
                        idxChat = strSplit[2];
                        imageChat = strSplit[3];
                    }


                    Log.d("name : ", nameChat);
                    Log.d("content : ", contentChat);
//                    Log.d("idx : ", idxChat);

                    //handler.sendMessage(str)
                    String togetherIdxString = String.valueOf(togetherIdx);
                    if (idxChat.equals(togetherIdxString)) {
                        putData(str);
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private void putData(String msg) {

        new Thread() {
            public void run() {
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
            }
        }.start();

    }

    int date_before = 0;
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //뿌려주기
            final ChatData chatData = new ChatData();
            chatData.setName(nameChat);
            chatData.setContent(contentChat);
            chatData.setImagePath(imageChat);       //이미지는 쉐어드에 저장된 주소로 가져오기.

            Date currentTime = Calendar.getInstance().getTime();
            String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
            int date = Integer.parseInt(date_text);

            //채팅 레이아웃 구분하기
            if (nameChat.equals(name)) {
                viewType = 2;
            } else if (nameChat.equals("access")) {         //여기 부분을 날짜로 바구기.
                viewType = 1;
            } else {
                viewType = 0;
            }
            chatData.setViewType(viewType);

            mArrayList.add(chatData);
            mAdapter.notifyDataSetChanged();


            //보낼때도 아래로 포커싱
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                }
            });


        }
    };

    private void updateChatData() {
        HashMap hashmap = new HashMap();
        hashmap.put("nameChat", name);
        hashmap.put("contentChat", et_message.getText().toString());
        hashmap.put("imageChat", imagePath);
        hashmap.put("uid", uid);
        hashmap.put("togetherIdx", togetherIdx);

        Call<ResponseBody> call = retrofitService.updateChatData(hashmap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = null;
                    result = response.body().string();
                    if(response.isSuccessful()){
                        Log.d("ChatData success : ", result);

                    }else{
                        Log.d("ChatData isn't success", result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ChatData Failed : ", String.valueOf(t));
            }
        });
    }


    class SenderThread extends Thread {
        Socket socket;
        String name;
        int togetherIdx;
        String imagePath;

        public SenderThread(Socket socket, String name, int togetherIdx, String imagePath) {
            this.socket = socket;
            this.name = name;
            this.togetherIdx = togetherIdx;
            this.imagePath = imagePath;
        }

        String messageSend;

        public void sendMessage(String message) {

            Log.d("SendMessage : ", "실행됨.");

            messageSend = message;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PrintWriter mWriter = null;
                    try {
                        mWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    imageChat = imagePath;
                    mWriter.println(messageSend);
                    mWriter.flush();


                }
            }).start();
        }

        @Override
        public void run() {

            try {
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                // 제일 먼저 서버로 대화명을 송신합니다.
                writer.println(name);
                writer.println(togetherIdx);
                writer.println(imagePath);
                writer.flush();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }


    //destroy되면 소켓 통신 종료
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


