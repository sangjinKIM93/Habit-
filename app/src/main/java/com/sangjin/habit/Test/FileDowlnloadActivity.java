package com.sangjin.habit.Test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sangjin.habit.R;

import java.io.File;

public class FileDowlnloadActivity extends AppCompatActivity {

    Button btn_fileDownload;
    private String FileName=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_download);

        btn_fileDownload = findViewById(R.id.btn_fileDownload);
        btn_fileDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://movie-phinf.pstatic.net/20180530_170/1527645793223uhWgz_JPEG/movie_image.jpg";
                ImageLoadTask imageLoadTask = new ImageLoadTask(url, "filename");
                imageLoadTask.execute();

            }
        });
    }
}
