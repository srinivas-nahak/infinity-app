package community.infinity.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import community.infinity.R;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.RoundedImageView;

/**
 * Created by Srinu on 29-10-2017.
 */

public class StarredAdapter extends RecyclerView.Adapter<StarredAdapter.StarViewHolder> {
    private ViewGroup parent;
    private List<TimelineData> totalList;
    private ArrayList<String> bg_links;
    private ArrayList<Bitmap> bit;
    private boolean all_bg;
    private int CLICK_ACTION_THRESHOLD = 150;
    private float startX;
    private float startY;
    private Activity activity;

    public StarredAdapter(List<TimelineData> totalList) {
      this.totalList=totalList;
    }
    public StarredAdapter(ArrayList<String> bg_links,boolean all_bg) {
        this.bg_links=bg_links;
        this.all_bg=all_bg;
    }
    public StarredAdapter(ArrayList<Bitmap> bit) {
       this.bit=bit;
    }
    @Override
    public StarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent=parent;
        activity=(Activity)parent.getContext();
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.design_starred,parent,false);
        return new StarViewHolder(v);
    }

    public class StarViewHolder extends RecyclerView.ViewHolder{
        private RoundedImageView img;
        private ImageView indicator;
        private FrameLayout card;
        private View tintLay;
        public StarViewHolder(View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.starred_img_holder);
            indicator=itemView.findViewById(R.id.multiPicIndicator);
            card=itemView.findViewById(R.id.album_frame_lay);
            tintLay=itemView.findViewById(R.id.tintLay);

            img.setCornerRadius(7,"inf");
            ButtonTint black_tint=new ButtonTint("white");
            black_tint.setTint(tintLay);

        }
    }
    @Override
    public void onBindViewHolder(StarViewHolder holder, int position) {
        try {
            if (totalList != null && totalList.size() > 0) {
                TimelineData timelineData = totalList.get(holder.getAdapterPosition());


                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                ArrayList<String> arrayList = gson.fromJson(timelineData.getImg_link(), type);

                //Making the multiImg indicator visible
                if (arrayList.size() > 1) {
                    holder.indicator.setVisibility(View.VISIBLE);
                }

                //Making the shortBook indicator visible
                if (timelineData.getShort_book_content() != null) {
                    holder.indicator.setImageResource(R.drawable.short_book_vector);
                    holder.indicator.setVisibility(View.VISIBLE);
                } else if (arrayList.size() < 1) {
                    holder.indicator.setVisibility(View.GONE);
                }

                //loading img
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!activity.isFinishing())
                            Glide.with(parent.getContext()).asBitmap().load(arrayList.get(0)).apply(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .override(150, 150).dontAnimate()
                                    .placeholder(new ColorDrawable(Color.parseColor("#20ffffff"))))
                                    .thumbnail(0.1f).into(holder.img);
                    }
                }, 200);


                holder.tintLay.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            v.performClick();
                        }
                    }
                });

                //opening img on click
                holder.tintLay.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        holder.tintLay.callOnClick();


                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = event.getX();
                                startY = event.getY();

                                v.setBackgroundColor(Color.parseColor("#20ffffff"));

                                break;
                            case MotionEvent.ACTION_MOVE:

                                v.setBackgroundColor(Color.parseColor("#20ffffff"));

                                break;
                            case MotionEvent.ACTION_CANCEL:
                                v.setBackgroundColor(Color.TRANSPARENT);
                                break;
                            case MotionEvent.ACTION_UP:
                                v.setBackgroundColor(Color.TRANSPARENT);

                                float endX = event.getX();
                                float endY = event.getY();
                                if (isAClick(startX, endX, startY, endY)) {
                                    Bundle b = new Bundle();
                                    b.putString("what", "show_post");
                                    b.putString("timelineData", gson.toJson(totalList));
                                    b.putInt("position", holder.getAdapterPosition());
                                    Intent i = new Intent("profile");
                                    i.putExtras(b);
                                    i.putExtra("Open", "starred");
                                    // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    //AppCompatActivity activity =(AppCompatActivity) parent.getContext();
                                    parent.getContext().startActivity(i);
                                }
                                break;
                        }

                        return true;
                    }
                });
          /*  holder.tintLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle b = new Bundle();
                    b.putString("what", "show_post");
                    b.putString("timelineData", gson.toJson(totalList));
                    b.putInt("position",holder.getAdapterPosition());
                    Intent i = new Intent("profile");
                    i.putExtras(b);
                    i.putExtra("Open", "starred");
                   // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //AppCompatActivity activity =(AppCompatActivity) parent.getContext();
                     parent.getContext().startActivity(i);
                }
            });*/
            } else if (bg_links != null && bg_links.size() > 0) {
                //loading img
                if (!activity.isFinishing())
                    Glide.with(parent.getContext()).load(bg_links.get(position)).apply(new RequestOptions()
                            .override(200, 200).priority(Priority.HIGH).placeholder(new ColorDrawable(Color.parseColor
                                    ("#20ffffff"))).diskCacheStrategy(DiskCacheStrategy.ALL))
                            .thumbnail(0.1f).into(holder.img);
                if (!all_bg) {
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getDp(100), getDp(100));
                    layoutParams.setMargins(getDp(2), getDp(2), getDp(2), getDp(2));
                    holder.card.setLayoutParams(layoutParams);
                }
            } else if (bit != null && bit.size() > 0) {

                if (!activity.isFinishing())
                    Glide.with(parent.getContext()).asBitmap().load(bit.get(holder.getAdapterPosition())).apply(new RequestOptions().override(200, 200).priority(Priority.HIGH)).into(holder.img);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getDp(100), getDp(100));
                layoutParams.setMargins(getDp(2), getDp(2), getDp(2), getDp(2));
                holder.card.setLayoutParams(layoutParams);
            }
        }catch (Exception e){

        }
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }
    @Override
    public int getItemCount() {
        if(totalList!=null)
        return totalList.size();
        else if(all_bg) return bg_links.size();
        else if(!all_bg&& bit==null) return 20;
        else return bit.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }


    private int getDp(int px){
        float d =  parent.getContext().getResources().getDisplayMetrics().density;
        int dp= (int) (px * d);
        return dp;
    }
}
