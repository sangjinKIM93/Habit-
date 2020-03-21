package com.sangjin.habit;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitService {

    //인증 데이터 가져오기.
    @GET("habitAuth/getAuthData.php")
    Call<ResponseBody> getAuthData(
            @Query("togetherIdx") int togetherIdx
    );

    //인증 데이터 저장하고 데이터 가져오기까지
    @FormUrlEncoded
    @POST("habitAuth/pushAuthData.php")
    Call<ResponseBody> pushAuthData(
            @FieldMap HashMap<String, String> hashMap
    );

    //모임 데이터 단순 수정(파일x)
    @FormUrlEncoded
    @POST("retrofit/editData.php")
    Call<ResponseBody> editData(
            @FieldMap HashMap<String, String> hashMap
    );

    // 사진을 보내기 위해 POST 방식에서 FormUrlEncoded 가 아닌 Multipart 방식을 사용. post가 여러가지 파트로 이루어져있다는 뜻.
    @Multipart
    @POST("retrofit/uploadImage.php")
    Call<ResponseBody> uploadImage(
            @Part ArrayList<MultipartBody.Part> files, @Part("email") RequestBody email);

    // 사진을 보내기 위해 POST 방식에서 FormUrlEncoded 가 아닌 Multipart 방식을 사용. post가 여러가지 파트로 이루어져있다는 뜻.
    @Multipart
    @POST("retrofit/uploadTogetherImage.php")
    Call<ResponseBody> uploadTogetherImage(
            @Part ArrayList<MultipartBody.Part> files, @Part("title") RequestBody title, @Part("content") RequestBody content, @Part("email") RequestBody email);

    //모임 수정
    @Multipart
    @POST("retrofit/editGathering.php")
    Call<ResponseBody> editGathering(
            @Part ArrayList<MultipartBody.Part> files, @Part("title") RequestBody title, @Part("content") RequestBody content, @Part("idx") RequestBody idx, @Part("imageName") RequestBody imageName);

    //모임 삭제
    @FormUrlEncoded
    @POST("deleteGathering.php")
    Call<ResponseBody> deleteGathering(
            @Field("idx") int idx
    );

    //해당 모임 정보 가져오기
    @GET("getTogetherData.php")
    Call<ResponseBody> getTogetherData(
            @Query("idx") int idx
    );

    //모임 리스트 가져오기
    @GET("together_list.php")
    Call<ResponseBody> getTogetherList(
    );

    //해당 모임 정보 가져오기
    @GET("checkApply.php")
    Call<ResponseBody> checkApply(
            @Query("togetherIdx") int togetherIdx, @Query("uid") String uid
    );

    //모임 나가기
    @FormUrlEncoded
    @POST("withdrawApply.php")
    Call<ResponseBody> withdrawApply(
            @Field("uid") String uid
    );

    //습관 인증 내역 삭제
    @FormUrlEncoded
    @POST("habitAuth/deleteHabitAuth.php")
    Call<ResponseBody> deleteHabitAuth(
            @Field("idx") int idx
    );

    //채팅 데이터 저장
    @FormUrlEncoded
    @POST("chatting/updateChatData.php")
    Call<ResponseBody> updateChatData(
            @FieldMap HashMap<String, String> hashMap
    );

    //채팅 데이터 가져오기
    @GET("chatting/getChatData.php")
    Call<ResponseBody> getChatData(
            @Query("togetherIdx") int togetherIdx, @Query("uid") String uid
    );

    //채팅 데이터 저장
    @FormUrlEncoded
    @POST("chatting/updateChatLog.php")
    Call<ResponseBody> updateChatLog(
            @FieldMap HashMap<String, String> hashMap
    );

    //채팅 로그 체크 후 리턴
    @GET("chatting/checkChatLog.php")
    Call<ResponseBody> checkChatLog(
            @Query("togetherIdx") int togetherIdx, @Query("uid") String uid
    );

    //채팅 로그 삭제 하기
    @FormUrlEncoded
    @POST("chatting/exitChat.php")
    Call<ResponseBody> exitChat(
            @Field("togetherIdx") int togetherIdx,  @Field("uid") String uid
    );


    //FCM 보내기
    @GET("chatting/sendFCM.php")
    Call<ResponseBody> sendFCM(
            @Query("togetherIdx") int togetherIdx, @Query("message") String message
    );

    //FCM 보내기
    @GET("chatting/updateChatNoti.php")
    Call<ResponseBody> updateChatNoti(
            @Query("togetherIdx") int togetherIdx, @Query("noti") int noti, @Query("uid") String uid
    );

    //채팅 이미지 업로드
    @Multipart
    @POST("chatting/uploadImageChat.php")
    Call<ResponseBody> uploadImageChat(
            @Part ArrayList<MultipartBody.Part> files
    );

    //채팅방 제복 가져오기
    @GET("chatting/getChatTitle.php")
    Call<ResponseBody> getChatTitle(
            @Query("togetherIdx") int togetherIdx
    );

    //인증 사진 업로드
    @Multipart
    @POST("habitAuth/uploadImageAuth.php")
    Call<ResponseBody> uploadImageAuth(
            @Part ArrayList<MultipartBody.Part> files
    );

    //크롤링 데이터 받기
    @GET("crawling/youtube.php")
    Call<ResponseBody> getCrawlingData(
            @Query("togetherIdx") int togetherIdx
    );
}
