package com.sangjin.habit.Crawling;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.sangjin.habit.R;

import java.util.ArrayList;

public class CrawlListAdapter extends RecyclerView.Adapter<CrawlListAdapter.CustomViewHolder> {

    private ArrayList<CrawlData> mList = null;
    private Context context = null;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListenerOnClick = null;
    private OnItemClickListener mListenerLongClick = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListenerOnClick = listener;
    }


    public CrawlListAdapter(Activity context, ArrayList<CrawlData> list) {
        this.context = context;
        this.mList = list;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_titleCrawling;
        protected ImageView iv_crawling;

        public CustomViewHolder(View view) {
            super(view);
            tv_titleCrawling = view.findViewById(R.id.tv_titleCrawling);
            iv_crawling = view.findViewById(R.id.iv_crawling);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListenerOnClick != null) {
                            mListenerOnClick.onItemClick(v, pos);
                        }
                    }
                }
            });

        }
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cardview, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        context = viewGroup.getContext();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        viewholder.tv_titleCrawling.setText(mList.get(position).getTitle());
        //viewholder.iv_crawling.setText(mList.get(position).getThumbnail());

        Glide.with(context).load(mList.get(position).getThumbnail()).placeholder(circularProgressDrawable).override(800, 400).into(viewholder.iv_crawling);

    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
