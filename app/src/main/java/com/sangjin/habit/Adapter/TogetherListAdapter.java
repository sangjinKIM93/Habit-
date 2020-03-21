package com.sangjin.habit.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sangjin.habit.DataType.TogetherData;
import com.sangjin.habit.R;

import java.util.ArrayList;

public class TogetherListAdapter extends RecyclerView.Adapter<TogetherListAdapter.CustomViewHolder> {

    private ArrayList<TogetherData> mList = null;
    private Context context = null;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListenerOnClick = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListenerOnClick = listener;
    }

    public TogetherListAdapter(Activity context, ArrayList<TogetherData> list) {
        this.context = context;
        this.mList = list;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_togetherTitle;
        protected ImageView iv_cardImageShow;

        public CustomViewHolder(View view) {
            super(view);
            tv_togetherTitle = view.findViewById(R.id.tv_togetherTitle);
            iv_cardImageShow = view.findViewById(R.id.iv_cardImageShow);

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
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_together, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        context = viewGroup.getContext();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder viewholder, int position) {

        String image = mList.get(position).getImage();

        viewholder.tv_togetherTitle.setText(mList.get(position).getTitle());

        if(image.equals("null")){
            viewholder.iv_cardImageShow.setImageResource(R.drawable.training);
        }else{
            Glide.with(context)
                    .load(image)
                    .override(800,500).into(viewholder.iv_cardImageShow);
        }


    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
