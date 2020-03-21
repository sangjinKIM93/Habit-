package com.sangjin.habit.Test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
    private String urlStr;
    private String title;
    //private ImageView imageView;
    //private ProgressBar progressBar;

    // 요청 url과 비트맵 객체 매핑, 메모리 관리 위한 것
    private static HashMap<String, Bitmap> bitmapHash = new HashMap<>();

    public ImageLoadTask(String urlStr, String title) {
        this.urlStr = urlStr;
        this.title = title;
        //this.imageView = imageView;
        //this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        /*
         * 해당 url 에 접속하여 스트림을 받음
         * 이미지일 경우 이미지 스트림 그대로 넘어오고
         * decodeStream 은 이미지 스트림을 비트맵으로 바꿔줌
         * (주소에 해당하는 이미지를 스트림(byte array)으로 가져옴!)
         */

        Bitmap bitmap = null;
        try {
            if(bitmapHash.containsKey(urlStr)){ // 요청 주소가 들어있다면
                Bitmap oldBitmap = bitmapHash.remove(urlStr);
                if(oldBitmap != null){
                    oldBitmap.recycle();
                    oldBitmap = null;
                }
            }
            URL url = new URL(urlStr);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            /*
             * 라이브러리들은 한번 받아놓은 이미지가 있으면 단말에 캐싱해놓고
             * 같은 URL로 요청할 경우 캐싱된 로컬 이미지를 그대로 사용한다 */

            bitmapHash.put(urlStr, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        //progressBar.setVisibility(View.GONE);
        saveBitmaptoJpeg(bitmap, "habit", title);
    }

    public static void saveBitmaptoJpeg(Bitmap bitmap,String folder, String name){

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/downloads");
        dir.mkdirs();
        String file_name = name+".jpg";
        File file = new File(dir, file_name);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dir+"/"+file_name);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}