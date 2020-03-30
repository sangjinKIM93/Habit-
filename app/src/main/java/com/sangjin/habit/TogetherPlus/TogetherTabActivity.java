package com.sangjin.habit.TogetherPlus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sangjin.habit.Activity.MainBottomActivity;
import com.sangjin.habit.Adapter.PagerAdapter;
import com.sangjin.habit.Chatting.NewChatActivity;
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

        Toolbar tool_bar = findViewById(R.id.tool_bar);
        setSupportActionBar(tool_bar);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exitChat:
                Intent intent = new Intent(this, NewChatActivity.class);
                intent.putExtra("IDX", togetherIdx);
                startActivity(intent);
                return true;

            default:
                return false;
        }
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
