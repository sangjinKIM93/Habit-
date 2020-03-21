package com.sangjin.habit.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sangjin.habit.TogetherPlus.TogetherFragment;
import com.sangjin.habit.R;

public class MainBottomActivity extends AppCompatActivity {

    // FrameLayout에 각 메뉴의 Fragment를 바꿔 줌
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HabitPlusFragment habitPlusFragment = new HabitPlusFragment();
    private TogetherFragment togetherFragment = new TogetherFragment();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_navigation);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        Intent intent = getIntent();
        int fromTabActivity = intent.getIntExtra("fromTabActivity", -1);

        if(fromTabActivity == 1){
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, togetherFragment).commitAllowingStateLoss();
            bottomNavigationView.setSelectedItemId(R.id.navigation_menu2);
        }else{
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, habitPlusFragment).commitAllowingStateLoss();
            bottomNavigationView.setSelectedItemId(R.id.navigation_menu1);
        }


        // bottomNavigationView의 아이템이 선택될 때 호출될 리스너 등록
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigation_menu1: {
                        transaction.replace(R.id.frame_layout, habitPlusFragment).commitAllowingStateLoss();
                        break;
                    }
                    case R.id.navigation_menu2: {
                        transaction.replace(R.id.frame_layout, togetherFragment).commitAllowingStateLoss();
                        break;
                    }

                }

                return true;
            }
        });
    }
}
