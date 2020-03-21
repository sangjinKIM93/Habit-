package com.sangjin.habit.HabitAuth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.sangjin.habit.Adapter.ApplicationListAdapter;
import com.sangjin.habit.Constant;
import com.sangjin.habit.DataType.ApplicationData;
import com.sangjin.habit.R;
import com.sangjin.habit.RetrofitService;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HabitAuthAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<HabitAuthData> mList = null;
    private Context context = null;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null;
    private OnItemClickListener mListener2 = null;
    private OnItemClickListener mListener3 = null;
    private OnItemClickListener mListener4 = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setOnItemClickListener2(OnItemClickListener listener) {
        this.mListener2 = listener;
    }

    public void setOnItemClickListener3(OnItemClickListener listener) {
        this.mListener3 = listener;
    }

    public void setOnItemClickListener4(OnItemClickListener listener) {
        this.mListener4 = listener;
    }

    public HabitAuthAdapter(Activity context, ArrayList<HabitAuthData> list) {
        this.context = context;
        this.mList = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = null;
        Context context = viewGroup.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (viewType == Constant.ViewType.CENTER_CONTENT) {
            view = inflater.inflate(R.layout.item_habitauth, viewGroup, false);
            return new CenterViewHolder(view);
        } else if (viewType == Constant.ViewType.LEFT_CONTENT) {
            view = inflater.inflate(R.layout.item_habitauth, viewGroup, false);
            return new LeftViewHolder(view);
        } else if (viewType == Constant.ViewType.IMAGE_CONTENT) {
            view = inflater.inflate(R.layout.item_habitauth_image, viewGroup, false);
            return new LeftImageViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_habitauth_video, viewGroup, false);
            return new LeftVideoViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        String image = mList.get(position).getUserImage();

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        SharedPreferences pref = context.getSharedPreferences("loginState", Context.MODE_PRIVATE);
        String name = pref.getString("name", "null");


        if (holder instanceof CenterViewHolder) {
            ((CenterViewHolder) holder).iv_habitAuthProfile.setVisibility(View.GONE);
            ((CenterViewHolder) holder).tv_habitAuthUid.setVisibility(View.GONE);
            ((CenterViewHolder) holder).tv_habitAuthContent.setText(mList.get(position).getContent());
            ((CenterViewHolder) holder).tv_habitAuthContent.setTextSize(20);
            ((CenterViewHolder) holder).tv_habitAuthContent.setTypeface(Typeface.DEFAULT_BOLD);
            ((CenterViewHolder) holder).tv_habitAuthContent.setGravity(Gravity.CENTER);
            ((CenterViewHolder) holder).item_habitAuth.setBackgroundResource(0);
            ((CenterViewHolder) holder).btn_deleteHabitAuth.setVisibility(View.GONE);

        } else if (holder instanceof LeftViewHolder) {
            ((LeftViewHolder) holder).tv_habitAuthUid.setText(mList.get(position).getUid());
            ((LeftViewHolder) holder).tv_habitAuthContent.setText(mList.get(position).getContent());


            if (name.equals(mList.get(position).getUid())) {
                ((LeftViewHolder) holder).btn_deleteHabitAuth.setVisibility(View.VISIBLE);
            }

            //이미지 주소 받아서 채워주기
            Log.d("IMAGE!!! : ", image);
            if (image.equals("null")) {
                ((LeftViewHolder) holder).iv_habitAuthProfile.setImageResource(R.drawable.profile);
            } else {
                Glide.with(context).load(image).placeholder(circularProgressDrawable).override(200, 200).into(((LeftViewHolder) holder).iv_habitAuthProfile);
            }

            //삭제 버튼 인터페이스
            if (name != null && name.equals(mList.get(position).getUid())) {
                ((LeftViewHolder) holder).tv_habitAuthContent.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = position;
                        if (pos != RecyclerView.NO_POSITION) {
                            // 리스너 객체의 메서드 호출.
                            if (mListener4 != null) {
                                mListener4.onItemClick(v, pos);
                            }
                        }
                        return false;
                    }
                });
            }

        } else if (holder instanceof LeftImageViewHolder) {
            ((LeftImageViewHolder) holder).tv_habitAuthUid.setText(mList.get(position).getUid());
            ((LeftImageViewHolder) holder).tv_habitAuthContent.setText(mList.get(position).getContent());


            //이미지 주소 받아서 채워주기
            if (image.equals("null")) {
                ((LeftImageViewHolder) holder).iv_habitAuthProfile.setImageResource(R.drawable.profile);
            } else {
                Glide.with(context).load(image).placeholder(circularProgressDrawable).override(200, 200).into(((LeftImageViewHolder) holder).iv_habitAuthProfile);
            }

            //content도 같이 넣어주기
            String imagePath = mList.get(position).getImagePath();
            Log.d("HAbitAUTH_IMAGE!!! : ", imagePath);
            Glide.with(context).load(imagePath).placeholder(circularProgressDrawable).override(500, 400).into(((LeftImageViewHolder) holder).iv_habitAuthImageView);

            //삭제 버튼 인터페이스
            if (name != null && name.equals(mList.get(position).getUid())) {
                ((LeftImageViewHolder) holder).iv_habitAuthImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = position;
                        if (pos != RecyclerView.NO_POSITION) {
                            // 리스너 객체의 메서드 호출.
                            if (mListener3 != null) {
                                mListener3.onItemClick(v, pos);
                            }
                        }
                        return false;
                    }
                });
            }

        } else if (holder instanceof LeftVideoViewHolder) {
            ((LeftVideoViewHolder) holder).tv_habitAuthUid.setText(mList.get(position).getUid());
            ((LeftVideoViewHolder) holder).tv_habitAuthContent.setText(mList.get(position).getContent());

            //이미지 주소 받아서 채워주기
            if (image.equals("null")) {
                ((LeftVideoViewHolder) holder).iv_habitAuthProfile.setImageResource(R.drawable.profile);
            } else {
                Glide.with(context).load(image).placeholder(circularProgressDrawable).override(200, 200).into(((LeftVideoViewHolder) holder).iv_habitAuthProfile);
            }

            //content도 같이 넣어주기
            String imagePath = mList.get(position).getImagePath();
            Log.d("HAbitAUTH_IMAGE!!! : ", imagePath);
            Glide.with(context).load(imagePath).placeholder(circularProgressDrawable).override(500, 400).into(((LeftVideoViewHolder) holder).iv_habitAuthImageView);

            //삭제 버튼 인터페이스
            if (name != null && name.equals(mList.get(position).getUid())) {
                ((LeftVideoViewHolder) holder).iv_habitAuthImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = position;
                        if (pos != RecyclerView.NO_POSITION) {
                            // 리스너 객체의 메서드 호출.
                            if (mListener2 != null) {
                                mListener2.onItemClick(v, pos);
                            }
                        }
                        return false;
                    }
                });
            }


            ((LeftVideoViewHolder) holder).iv_habitAuthImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = position;
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


    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getViewType();
    }

    public class CenterViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_habitAuthUid, tv_habitAuthContent;
        protected ImageView iv_habitAuthProfile;
        protected RelativeLayout item_habitAuth;
        protected Button btn_deleteHabitAuth;


        CenterViewHolder(View itemView) {
            super(itemView);

            tv_habitAuthUid = itemView.findViewById(R.id.tv_habitAuthUid);
            tv_habitAuthContent = itemView.findViewById(R.id.tv_habitAuthContent);
            iv_habitAuthProfile = itemView.findViewById(R.id.iv_habitAuthProfile);
            item_habitAuth = itemView.findViewById(R.id.item_habitAuth);
            btn_deleteHabitAuth = itemView.findViewById(R.id.btn_deleteHabitAuth);

        }
    }

    public class LeftViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_habitAuthUid, tv_habitAuthContent;
        protected ImageView iv_habitAuthProfile;
        protected RelativeLayout item_habitAuth;
        protected Button btn_deleteHabitAuth;

        LeftViewHolder(View itemView) {
            super(itemView);

            tv_habitAuthUid = itemView.findViewById(R.id.tv_habitAuthUid);
            tv_habitAuthContent = itemView.findViewById(R.id.tv_habitAuthContent);
            iv_habitAuthProfile = itemView.findViewById(R.id.iv_habitAuthProfile);
            item_habitAuth = itemView.findViewById(R.id.item_habitAuth);
            btn_deleteHabitAuth = itemView.findViewById(R.id.btn_deleteHabitAuth);

        }
    }

    public class LeftImageViewHolder extends RecyclerView.ViewHolder {
        protected TextView tv_habitAuthUid, tv_habitAuthContent;
        protected ImageView iv_habitAuthProfile, iv_habitAuthImageView;
        protected RelativeLayout item_habitAuth;
        protected Button btn_deleteHabitAuth;


        LeftImageViewHolder(View itemView) {
            super(itemView);

            tv_habitAuthUid = itemView.findViewById(R.id.tv_habitAuthUid);
            iv_habitAuthProfile = itemView.findViewById(R.id.iv_habitAuthProfile);
            tv_habitAuthContent = itemView.findViewById(R.id.tv_habitAuthContent);
            item_habitAuth = itemView.findViewById(R.id.item_habitAuth);
            btn_deleteHabitAuth = itemView.findViewById(R.id.btn_deleteHabitAuth);
            iv_habitAuthImageView = itemView.findViewById(R.id.iv_habitAuthImageView);

//
//            //삭제 버튼 인터페이스
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    int pos = getAdapterPosition();
//                    if (pos != RecyclerView.NO_POSITION) {
//                        // 리스너 객체의 메서드 호출.
//                        if (mListener2 != null) {
//                            mListener2.onItemClick(v, pos);
//                        }
//                    }
//                    return false;
//                }
//            });

        }
    }

    private class LeftVideoViewHolder extends RecyclerView.ViewHolder {

        protected TextView tv_habitAuthUid, tv_habitAuthContent;
        protected ImageView iv_habitAuthProfile, iv_habitAuthImageView, iv_habitAuthVideoBtn;
        protected RelativeLayout item_habitAuth;
        protected Button btn_deleteHabitAuth;

        public LeftVideoViewHolder(View itemView) {
            super(itemView);

            tv_habitAuthUid = itemView.findViewById(R.id.tv_habitAuthUid);
            iv_habitAuthProfile = itemView.findViewById(R.id.iv_habitAuthProfile);
            tv_habitAuthContent = itemView.findViewById(R.id.tv_habitAuthContent);
            item_habitAuth = itemView.findViewById(R.id.item_habitAuth);
            btn_deleteHabitAuth = itemView.findViewById(R.id.btn_deleteHabitAuth);
            iv_habitAuthImageView = itemView.findViewById(R.id.iv_habitAuthImageView);
            iv_habitAuthVideoBtn = itemView.findViewById(R.id.iv_habitAuthVideoBtn);


        }
    }


}
