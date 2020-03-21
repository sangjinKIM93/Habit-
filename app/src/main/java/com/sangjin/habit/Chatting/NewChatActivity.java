package com.sangjin.habit.Chatting;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;
import com.sangjin.habit.Test.ImageLoadTask;
import com.sangjin.habit.TogetherPlus.ChatData;
import com.sangjin.habit.TogetherPlus.TogetherTabActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NewChatActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST = 11;
    private RecyclerView tv_message;
    private EditText et_message;
    private Button btn_sendMessage, btn_startChat, btn_sendImage;
    private SenderThread thread1;
    private Socket socket;
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

    int notiOff = 0;
    private static final int GALLERY_CODE_CHAT = 30;
    String imagePathUpload;
    private String TAG = "NewChatActivity : ";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_chat, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exitChat:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("정말로 채팅방을 나가시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                exitChat();
                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(NewChatActivity.this, "취소", Toast.LENGTH_LONG).show();
                            }
                        });
                builder.show();
                return true;

            case R.id.action_notiOff:
                Toast.makeText(this, "채팅 알람이 꺼졌습니다.", Toast.LENGTH_SHORT).show();
                notiOff = 1;
                stopNoti();
                return true;

            case R.id.action_notiOn:
                Toast.makeText(this, "채팅 알람이 켜졌습니다.", Toast.LENGTH_SHORT).show();
                notiOff = 0;
                stopNoti();
                return true;

            default:
                return false;
        }
    }

    //menuItem 보이기,숨기기 설정
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT > 11) {
            invalidateOptionsMenu();
            if (notiOff == 1) {
                menu.findItem(R.id.action_notiOff).setVisible(false);
                menu.findItem(R.id.action_notiOn).setVisible(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //core : "onCreate"
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        /*갤러리 접근 권한*/
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
//        }

        //레트로핏 객체 및 레트로핏 서비스 객체 선언
        retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();
        retrofitService = retrofit.create(RetrofitService.class);

        //로그인한 아이디 가져오기
        SharedPreferences pref = getSharedPreferences("loginState", MODE_PRIVATE);
        uid = pref.getString("email", "null");
        name = pref.getString("name", "null");
        imagePath = pref.getString("imagePath", "null");


        Intent intent = getIntent();
        togetherIdx = intent.getIntExtra("IDX", -1);

        Bundle bundle = getIntent().getExtras();
        String togetherIdxString = bundle.getString("togetherIdx");
        if (togetherIdxString != null) {
            togetherIdx = Integer.parseInt(togetherIdxString);
        }

        //제목 받기
        getChatTitle();

        //FCMtoken 받기
        fcmToken = FirebaseInstanceId.getInstance().getToken();

        //리사이클러뷰 셋팅
        recyclerViewSetting();

        tv_message = findViewById(R.id.tv_message);
        et_message = findViewById(R.id.et_message);
        btn_sendMessage = findViewById(R.id.btn_sendMessage);
        btn_startChat = findViewById(R.id.btn_startChat);
        relative_chat = findViewById(R.id.relative_chat);
        btn_sendImage = findViewById(R.id.btn_sendImage);

        //
        getChatBefore();

        //채팅 시작 여부 체크. 해당idx에 해당 id가 있으면 button없애고 relative visible로 바꾸기.
        checkChatLog();

        //버튼 클릭
        btnClickList();


    }

    private void getChatTitle() {
        Call<ResponseBody> call = retrofitService.getChatTitle(togetherIdx);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = null;
                    result = response.body().string();
                    if (response.isSuccessful()) {

                        Log.d("ChatTitle Suceessful:", result);
                        ActionBar actionBar = getSupportActionBar();
                        actionBar.setTitle(result);

                    } else {
                        Log.d("ChatTitle isn't Susful:", result);
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


    private void btnClickList() {
        btn_sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //서버에 이미지를 저장하고 이미지 주소를 반환한다.
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE_CHAT);

                //반환한 주소로 tcp 통신을 한다.

                //adapter는 text가 주소형식일때 해당 text를 glide에 넣는다.
            }
        });

        btn_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = et_message.getText().toString();
                //소켓에서 message보내기
                thread1.sendMessage(content);
                //서버에 데이터 넣어주기
                updateChatData(content);
                //fcmMessage 보내기
                sendFCM();

                et_message.setText(""); //초기화
            }
        });

        btn_startChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateChatLog();       //db에 채팅시작 여부 남기기
                createSocket();         //소켓 열기.
            }
        });

        //삭제 버튼 클릭시
        mAdapter.setOnItemClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewChatActivity.this);
                builder.setMessage("이미지를 다운받겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                String url = mArrayList.get(position).getContent();
                                String filename = url.substring(url.length()-15, url.length()-4);
                                ImageLoadTask imageLoadTask = new ImageLoadTask(url, filename);
                                imageLoadTask.execute();
                                Log.d("URL 값 : ", url);
                                Toast.makeText(NewChatActivity.this, "Filename : "+filename, Toast.LENGTH_SHORT).show();

                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(NewChatActivity.this,"삭제 취소",Toast.LENGTH_LONG).show();
                            }
                        });
                builder.show();

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_CODE_CHAT) {
            if(data != null){   //선택 안 하고 그냥 뒤로가기 눌렀을때 nullpointerror 잡기
                //imageview에 해당 사진 넣어주기
                imagePathUpload = getPath(data.getData());
                //File f = new File(imagePath);
                //iv_cardImage.setImageURI(Uri.fromFile(f));
                uploadImageChat();
            }
        }
    }

    private void uploadImageChat() {
        Log.d(TAG, "uploadImageToServer");
        Log.d(TAG, "Path: "+imagePath);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverURL)
                .build();

        ArrayList<MultipartBody.Part> imageList = new ArrayList<>();

        File file = new File(imagePathUpload);        //파일을 가져와서
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);     //requestBody라는 객체에 담고
        MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("files[]", imagePath, requestBody);    //multipartbody라는 객체에 마지막으로 넣어서
        imageList.add(uploadFile);

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        Call<ResponseBody> call = retrofitService.uploadImageChat(imageList);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "imageUpload onResponse: "+response);

                try {
                    String result = response.body().string();

                    if(response.isSuccessful()){
                        Log.d(TAG, "imageUpload isSuceessful: "+result);
                        //주소가 아주 잘 나왔어. 이제 이 자료를 바탕으로 서버chatData에 저장하고 tcp 통신해주는거야.
                        thread1.sendMessage(result);
                        updateChatData(result);

                    }else{
                        Log.d(TAG, "imageUpload isn't Suceessful: "+result);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "imageUpload onFailure: "+t);
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


    private void sendFCM() {
        Call<ResponseBody> call = retrofitService.sendFCM(togetherIdx, et_message.getText().toString());  //call을 보내는 것
        //call에 대한 응답받기
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "sendFCM : " + response);
                String result = null;
                try {
                    result = response.body().string();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "sendFCM Suceessful: " + result);
                    } else {
                        Log.d(TAG, "sendFCM isn't Suceessful: " + result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "sendFCM Failed : " + t);
            }
        });
    }


    private void exitChat() {
        Call<ResponseBody> call = retrofitService.exitChat(togetherIdx, uid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(NewChatActivity.this, "채팅을 나갑니다.", Toast.LENGTH_SHORT).show();
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
                    if (response.isSuccessful()) {
                        if (result.equals("ok")) {
                            Log.d(TAG, "checkChatLog : " + result);
                            relative_chat.setVisibility(View.VISIBLE);

                            createSocket();
                        } else {
                            Log.d(TAG, "checkChatLog : " + result);
                            btn_startChat.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.d(TAG, "checkChatLog isn't Suceessful: " + result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "checkChatLog Failed : " + t);
            }
        });
    }


    private void createSocket() {

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
                    if (response.isSuccessful()) {
                        Log.d("ChatLog success : ", result);

                        //ui변경.
                        btn_startChat.setVisibility(View.GONE);
                        relative_chat.setVisibility(View.VISIBLE);

                    } else {
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

    }


    private void getChatBefore() {
        mArrayList.clear();

        Call<ResponseBody> call = retrofitService.getChatData(togetherIdx, uid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = null;
                    result = response.body().string();
                    if (response.isSuccessful()) {

                        Log.d("getchat Suceessful: ", result);
                        josnToRecycler(result);

                    } else {
                        Log.d("chat isn't Suceessful: ", result);
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
    private void josnToRecycler(String result) {
        mArrayList.clear();

        //JSONarray로 선언해주고 array에서 JSONobject를 하나씩 받아서 풀어줌.
        JSONArray jsonArray = null;
        try {

            String dateBefore = "null";

            jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String contentChat = jsonObject.getString("contentChat");
                String nameChat = jsonObject.getString("nameChat");
                String imageChat = jsonObject.getString("imageChat");
                String uidChat = jsonObject.getString("uidChat");
                String created = jsonObject.getString("created");

                //날짜가 다르다면 날짜 표시 데이터 추가 생성
                String date = created.substring(0, 10);
                if (!dateBefore.equals(date)) {
                    //Log.d(TAG, "둘이 다르다 : "+String.valueOf(!dateBefore.equals(date)));
                    ChatData chatData2 = new ChatData();
                    chatData2.setContent(date);
                    chatData2.setViewType(1);

                    mArrayList.add(chatData2);
                }
                dateBefore = date;

                //채팅 레이아웃 구분하기
                if (nameChat.equals(name)) {

                    if(contentChat.contains("http://52.79.252.51/chatting/uploads/")){
                        viewType = 4;
                    }else{
                        viewType = 2;
                    }
                } else {

                    if(contentChat.contains("http://52.79.252.51/chatting/uploads/")){
                        viewType = 3;
                    }else{
                        viewType = 0;
                    }
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
        mRecyclerView = findViewById(R.id.tv_message);
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ChatAdapter(NewChatActivity.this, mArrayList);
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
                        //putData(str);
                        Message message = handler.obtainMessage();
                        handler.sendMessage(message);
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

//    private void putData(String msg) {
//
//        new Thread() {
//            public void run() {
//                Message message = handler.obtainMessage();
//                handler.sendMessage(message);
//            }
//        }.start();
//
//    }

    int date_before = 0;
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //뿌려주기
            final ChatData chatData = new ChatData();
            chatData.setName(nameChat);
            chatData.setContent(contentChat);
            Log.d("ContentChat : ", contentChat);
            chatData.setImagePath(imageChat);       //이미지는 쉐어드에 저장된 주소로 가져오기.


            Date currentTime = Calendar.getInstance().getTime();
            String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
            int date = Integer.parseInt(date_text);



            //채팅 레이아웃 구분하기
            if (nameChat.equals(name)) {
                //이미지일 경우도 레이아웃 바꿔줘야해.
                if(contentChat.contains("http://52.79.252.51/chatting/uploads/")){
                    viewType = 4;
                }else{
                    viewType = 2;
                }
            } else {

                if(contentChat.contains("http://52.79.252.51/chatting/uploads/")){
                    viewType = 3;
                }else{
                    viewType = 0;
                }
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

    private void updateChatData(String content) {
        HashMap hashmap = new HashMap();
        hashmap.put("nameChat", name);
        hashmap.put("contentChat", content);
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
                    if (response.isSuccessful()) {
                        Log.d("ChatData success : ", result);

                    } else {
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
        public void sendMessage(final String message) {

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
                    mWriter.println(message);
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

    //채팅 알람 끄고,켜기
    private void stopNoti() {
        Call<ResponseBody> call = retrofitService.updateChatNoti(togetherIdx, notiOff, uid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "sendFCM : " + response);
                String result = null;
                try {
                    result = response.body().string();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "sendFCM Suceessful: " + result);
                    } else {
                        Log.d(TAG, "sendFCM isn't Suceessful: " + result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "sendFCM Failed : " + t);
            }
        });
    }


    //뒤로가기


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, TogetherTabActivity.class);
        intent.putExtra("IDX", togetherIdx);
        startActivity(intent);
        finish();
    }

    //destroy되면 소켓 통신 종료
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


