package com.sangjin.habit.Chatting;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.sangjin.habit.Constant;
import com.sangjin.habit.R;
import com.sangjin.habit.TogetherPlus.ChatData;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ChatData> myDataList = null;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public ChatAdapter(Context mContext, ArrayList<ChatData> dataList) {
        context = mContext;
        myDataList = dataList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (viewType == Constant.ViewType.CENTER_CONTENT) {
            view = inflater.inflate(R.layout.item_chat_center, parent, false);
            return new CenterViewHolder(view);
        } else if (viewType == Constant.ViewType.LEFT_CONTENT) {
            view = inflater.inflate(R.layout.item_chat_other, parent, false);
            return new LeftViewHolder(view);
        } else if (viewType == Constant.ViewType.RIGHT_CONTENT) {
            view = inflater.inflate(R.layout.item_chat, parent, false);
            return new RightViewHolder(view);
        } else if (viewType == 3) {
            view = inflater.inflate(R.layout.item_chat_other_image, parent, false);
            return new LeftImageViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_chat_image, parent, false);
            return new RightImageViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        if (viewHolder instanceof CenterViewHolder) {
            ((CenterViewHolder) viewHolder).content.setText(myDataList.get(position).getContent());
        } else if (viewHolder instanceof LeftViewHolder) {

            ((LeftViewHolder) viewHolder).name.setText(myDataList.get(position).getName());

            String content = myDataList.get(position).getContent();
            ((LeftViewHolder) viewHolder).content.setText(content);

            String image = myDataList.get(position).getImagePath();
            if (image.equals("null")) {
                ((LeftViewHolder) viewHolder).image.setImageResource(R.drawable.profile);
            } else {
                Glide.with(context).load(image).placeholder(circularProgressDrawable).override(200, 200).into(((LeftViewHolder) viewHolder).image);
            }

        } else if (viewHolder instanceof RightViewHolder) {

            String content = myDataList.get(position).getContent();
            ((RightViewHolder) viewHolder).content.setText(content);

            String image = myDataList.get(position).getImagePath();
            if (image.equals("null")) {
                ((RightViewHolder) viewHolder).image.setImageResource(R.drawable.profile);
            } else {
                Glide.with(context).load(image).placeholder(circularProgressDrawable).override(200, 200).into(((RightViewHolder) viewHolder).image);
            }

        } else if (viewHolder instanceof LeftImageViewHolder) {

            ((LeftImageViewHolder) viewHolder).name.setText(myDataList.get(position).getName());

            String content = myDataList.get(position).getContent();
            Glide.with(context).load(content).placeholder(circularProgressDrawable).override(200, 200).into(((LeftImageViewHolder) viewHolder).iv_imageUpload);

            String image = myDataList.get(position).getImagePath();
            if (image.equals("null")) {
                ((LeftImageViewHolder) viewHolder).image.setImageResource(R.drawable.profile);
            } else {
                Glide.with(context).load(image).placeholder(circularProgressDrawable).override(200, 200).into(((LeftImageViewHolder) viewHolder).image);
            }


        } else if (viewHolder instanceof RightImageViewHolder) {

            String content = myDataList.get(position).getContent();
            Glide.with(context).load(content).placeholder(circularProgressDrawable).override(200, 200).into(((RightImageViewHolder) viewHolder).iv_imageUpload);

            String image = myDataList.get(position).getImagePath();
            if (image.equals("null")) {
                ((RightImageViewHolder) viewHolder).image.setImageResource(R.drawable.profile);
            } else {
                Glide.with(context).load(image).placeholder(circularProgressDrawable).override(200, 200).into(((RightImageViewHolder) viewHolder).image);
            }
        }

    }

    @Override
    public int getItemCount() {
        return myDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return myDataList.get(position).getViewType();
    }

    public class CenterViewHolder extends RecyclerView.ViewHolder {
        TextView content;

        CenterViewHolder(View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.tv_chatContent);
        }
    }

    public class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        TextView name;
        ImageView image;

        LeftViewHolder(View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.tv_chatContent);
            name = itemView.findViewById(R.id.tv_chatName);
            image = itemView.findViewById(R.id.iv_chatProfile);
        }
    }

    public class RightViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        ImageView image;

        RightViewHolder(View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.tv_chatContent);
            image = itemView.findViewById(R.id.iv_chatProfile);
        }
    }

    private class LeftImageViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image, iv_imageUpload;

        LeftImageViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_chatName);
            image = itemView.findViewById(R.id.iv_chatProfile);
            iv_imageUpload = itemView.findViewById(R.id.iv_imageUpload);

            //이미지 다운 버튼 인터페이스
            iv_imageUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener != null) {
                            mListener.onItemClick(v, pos);
                        }
                    }
                }
            });
        }
    }

    private class RightImageViewHolder extends RecyclerView.ViewHolder {
        ImageView image, iv_imageUpload;

        RightImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_chatProfile);
            iv_imageUpload = itemView.findViewById(R.id.iv_imageUpload);

            //이미지 다운 버튼 인터페이스
            iv_imageUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener != null) {
                            mListener.onItemClick(v, pos);
                        }
                    }
                }
            });
        }
    }
}
