package com.sangjin.habit.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sangjin.habit.DataType.ApplicationData;
import com.sangjin.habit.DataType.TogetherData;
import com.sangjin.habit.R;

import java.util.ArrayList;

public class ApplicationListAdapter extends RecyclerView.Adapter<ApplicationListAdapter.CustomViewHolder> {

    private ArrayList<ApplicationData> mList = null;
    private Context context = null;


    public ApplicationListAdapter(Activity context, ArrayList<ApplicationData> list) {
        this.context = context;
        this.mList = list;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_resolution, tv_applyPerson;
        protected ImageView iv_applyProfile;

        public CustomViewHolder(View view) {
            super(view);
            tv_resolution = view.findViewById(R.id.tv_resolution);
            tv_applyPerson = view.findViewById(R.id.tv_applyPerson);
            iv_applyProfile = view.findViewById(R.id.iv_applyProfile);

        }
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_apply, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        context = viewGroup.getContext();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        String image = mList.get(position).getImage();

        viewholder.tv_applyPerson.setText(mList.get(position).getName());
        viewholder.tv_resolution.setText(mList.get(position).getResloution());

        if(image.equals("null")){
            viewholder.iv_applyProfile.setImageResource(R.drawable.profile);
        }else{
            Glide.with(context).load(image).placeholder(R.drawable.loading).override(200,200).into(viewholder.iv_applyProfile);
        }

    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
