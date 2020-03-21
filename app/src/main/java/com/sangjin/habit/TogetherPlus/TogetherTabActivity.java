package com.sangjin.habit.TogetherPlus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sangjin.habit.Activity.MainBottomActivity;
import com.sangjin.habit.Adapter.PagerAdapter;
import com.sangjin.habit.R;

public class TogetherTabActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    int togetherIdx;
    PagerAdapter adapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_together);

        Intent intent = getIntent();
        togetherIdx = intent.getIntExtra("IDX", -1);

        //tablayout 상단 메뉴 설정.
        mTabLayout = (TabLayout) findViewById(R.id.layout_tab);
        mTabLayout.addTab(mTabLayout.newTab().setText("정보"));
        mTabLayout.addTab(mTabLayout.newTab().setText("인증게시판"));
        mTabLayout.addTab(mTabLayout.newTab().setText("관련 영상"));

        viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new PagerAdapter
                (getSupportFragmentManager(), mTabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainBottomActivity.class);
        intent.putExtra("fromTabActivity", 1);
        startActivity(intent);
        finish();
    }


}
