package com.sangjin.habit.Adapter;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sangjin.habit.DataType.MemoData;
import com.sangjin.habit.R;

import java.util.ArrayList;


public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.CustomViewHolder> {

    private ArrayList<MemoData> mList = null;
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
    public void setOnItemClickListenerLong(OnItemClickListener listener) {
        this.mListenerLongClick = listener;
    }

    public MemoAdapter(Activity context, ArrayList<MemoData> list) {
        this.context = context;
        this.mList = list;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_num, tv_date, tv_content;

        public CustomViewHolder(View view) {
            super(view);
            tv_num = view.findViewById(R.id.tv_num);
            tv_date = view.findViewById(R.id.tv_date);
            tv_content = view.findViewById(R.id.tv_content);

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

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListenerLongClick != null) {
                            mListenerLongClick.onItemClick(v, pos);
                        }
                    }
                    return false;
                }

            });

        }
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_memo, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        context = viewGroup.getContext();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        viewholder.tv_num.setText(mList.get(position).getNum());
        viewholder.tv_date.setText(mList.get(position).getDate());
        if(mList.get(position).getContent().equals("")){
            viewholder.tv_content.setText("내용 없음");
            viewholder.tv_content.setTextColor(Color.parseColor("#FFD3D3D3"));
        }else{
            viewholder.tv_content.setText(mList.get(position).getContent());
        }


    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
