package com.sangjin.habit.Adapter;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sangjin.habit.DataType.PersonalData;
import com.sangjin.habit.ETC.MemoDbHelper;
import com.sangjin.habit.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class HabitListAdapter extends RecyclerView.Adapter<HabitListAdapter.CustomViewHolder> {

    private ArrayList<PersonalData> mList = null;
    private Context context = null;

    MemoDbHelper dbHelperSelect;
    SQLiteDatabase dbSelect;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null;
    private OnItemClickListener mListener2 = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }
    public void setOnItemClickListener2(OnItemClickListener listener) {
        this.mListener2 = listener;
    }

    public HabitListAdapter(Activity context, ArrayList<PersonalData> list) {
        this.context = context;
        this.mList = list;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_habitName, tv_dayAchieved, tv_goal;
        protected Button btn_today;
        protected ProgressBar bar_progress;

        public CustomViewHolder(View view) {
            super(view);

            tv_habitName = view.findViewById(R.id.tv_habitName);
            tv_dayAchieved = view.findViewById(R.id.tv_dayAchieved);
            tv_goal = view.findViewById(R.id.tv_goal);
            bar_progress = view.findViewById(R.id.bar_progress);

            btn_today = view.findViewById(R.id.btn_today);

            btn_today.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // 리스너 객체의 메서드 호출.
                        if (mListener2 != null) {
                            mListener2.onItemClick(v, pos);
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
                        if (mListener != null) {
                            mListener.onItemClick(v, pos);
                        }
                    }
                    return false;
                }

            });
        }
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_habit, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        context = viewGroup.getContext();

        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        int dayAchieved = mList.get(position).getDayAchieved();
        int goal = mList.get(position).getGoalNum();

        viewholder.tv_habitName.setText(mList.get(position).getHabitName());
        viewholder.tv_dayAchieved.setText(Integer.toString(dayAchieved));  //setText는 string만 넣을 수 있어. 그래서 Integer.toString으로 형변환 시켜줌.
        viewholder.tv_goal.setText(Integer.toString(goal));

        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(currentTime);

        if (mList.get(position).getToday().equals(date_text)) {
            viewholder.btn_today.setBackgroundResource(R.drawable.check3);
        } else {
            viewholder.btn_today.setBackgroundResource(R.drawable.check2);
        }

        //달성한 습관은 계속 버튼이 유지되도록
        if(mList.get(position).getGoalNum() <= mList.get(position).getDayAchieved()){
            viewholder.btn_today.setBackgroundResource(R.drawable.check3);
        }

        Log.d("progress, dayAchieved:", String.valueOf(dayAchieved));
        Log.d("progress, goal:", String.valueOf(goal));
        //나중에 프로그래스까지 추가
        int percent_achieve = (dayAchieved*100/goal);   //100을 먼저 곱해줘야해. 나중에 곱하면 그 전의 값이 0이 되어서 100을 곱하는 것이 의미가 없어져
        Log.d("percent_achieve:", String.valueOf(percent_achieve));
        viewholder.bar_progress.setProgress(percent_achieve,true);
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
