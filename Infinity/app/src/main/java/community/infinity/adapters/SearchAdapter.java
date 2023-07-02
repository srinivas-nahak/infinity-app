package community.infinity.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import community.infinity.ListItem;
import community.infinity.R;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.ProfileHolder;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.ReverseInterpolator;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

/**
 * Created by Srinu on 12-03-2018.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private ViewGroup parent;
    private ArrayList<String> fullname,profile_pic,email;
    private List<NotificationData> notification;
    private ListItem item;
    private String tagString;
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public SearchAdapter(List<NotificationData> notification){
       this.notification=notification;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent=parent;
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.design_of_acc_settings,null);

        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder hold, int position) {
        try {
            if (fullname != null) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);
                if (fullname.size() > 0) {
                    hold.fullname.setText(fullname.get(position));
                }

                Glide
                        .with(parent.getContext())
                        .asBitmap()
                        .load(profile_pic.get(hold.getAdapterPosition()))
                        .apply(new RequestOptions().placeholder(new ColorDrawable(Color.parseColor("#20ffffff"))).error(R.drawable
                                .profile))
                        .thumbnail(0.1f)
                        .into(new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                hold.profile_pic.setImageBitmap(resource);
                            }
                        });

                Toast.makeText(parent.getContext(), tagString, Toast.LENGTH_SHORT).show();


            } else {
                //Getting NotificationData
                NotificationData notif = notification.get(hold.getAdapterPosition());


                //Making Necessary Components Visible
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.VISIBLE);


                //Setting NotificationData
                hold.fullname.setText(notif.getNotification_data());

                //Setting Box and Text Components

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);
                hold.fullname.setTextSize(15);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting NotificationTxt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);

                //Setting line
                RelativeLayout.LayoutParams margin_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp((int) 0.1));
                margin_params.setMargins(getDp(30), 0, getDp(30), 0);
                margin_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                hold.margin.setLayoutParams(margin_params);


                if (notif.getType().equals("follow_request")) {
                    hold.followApprovalCont.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    param.addRule(RelativeLayout.CENTER_VERTICAL);
                    param.addRule(RelativeLayout.LEFT_OF, hold.followApprovalCont.getId());
                    param.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                    param.setMargins(getDp(5), 0, 0, 0);
                    hold.fullname.setLayoutParams(param);

                    ButtonTint tint = new ButtonTint("white");
                    tint.setTint(hold.accept_btn);
                    tint.setTint(hold.reject_btn);


                    hold.accept_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            socket.disconnect();
                            socket.connect();
                            JSONObject ob = new JSONObject();
                            try {
                                ob.put("accept_requester_username", notif.getFollower_username());
                                ob.put("accept_accepter_username", Hawk.get("myUserName"));
                                ob.put("accept_accepter_fullname", Hawk.get("myFullName"));
                                socket.emit("data", ob);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.on("accept_follow_request", new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    JSONObject ob = (JSONObject) args[0];
                                    try {
                                        if (ob.getString("status").equals("yes")) {
                                            final Animation rev_an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                                            rev_an.setInterpolator(new ReverseInterpolator());
                                            Needle.onMainThread().execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    hold.itemView.startAnimation(rev_an);
                                                    if (hold.getAdapterPosition() != RecyclerView.NO_POSITION) {
                                                        notification.remove(hold.getAdapterPosition());
                                                        notifyItemRemoved(hold.getAdapterPosition());
                                                        notifyItemRangeChanged(hold.getAdapterPosition(), notification.size());
                                                    }
                                                    // Toast.makeText( parent.getContext(),notif.getFollower_username()+" has started following you.",Toast.LENGTH_LONG).show();
                                                }
                                            });

                                            socket.disconnect();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    hold.reject_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            socket.disconnect();
                            socket.connect();
                            JSONObject ob = new JSONObject();
                            try {
                                ob.put("reject_requester_username", notif.getFollower_username());
                                ob.put("reject_accepter_username", Hawk.get("myUserName"));
                                socket.emit("data", ob);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.on("reject_follow_request", new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    JSONObject ob = (JSONObject) args[0];
                                    try {
                                        if (ob.getString("status").equals("yes")) {
                                            final Animation rev_an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                                            rev_an.setInterpolator(new ReverseInterpolator());
                                            Needle.onMainThread().execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    hold.itemView.startAnimation(rev_an);
                                                    if (hold.getAdapterPosition() != RecyclerView.NO_POSITION) {
                                                        notification.remove(hold.getAdapterPosition());
                                                        notifyItemRemoved(hold.getAdapterPosition());
                                                        notifyItemRangeChanged(hold.getAdapterPosition(), notification.size());
                                                    }
                                                    Toast.makeText(parent.getContext(), "Follow request removed successfully .", Toast.LENGTH_LONG).show();
                                                }
                                            });

                                            socket.disconnect();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });

                }

                //Setting ProfilePic of liker or commenter or follower
                ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));

                if (notif.getNotif_sender_profPic().equals("")) {
                    hold.profile_pic.setImageResource(R.drawable.profile);
                } else {
                    Glide.with(parent.getContext()).load(notif.getNotif_sender_profPic()).
                            apply(new RequestOptions().override(50, 50).error(R.drawable.profile).placeholder(cd).diskCacheStrategy
                                    (DiskCacheStrategy.NONE).
                                    skipMemoryCache(true)).thumbnail(0.1f).into(hold.profile_pic);
                }


                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (notif.getType().equals("likes")) {
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle b = new Bundle();
                            b.putString("post_owner_username", notif.getPost_owner_username());
                            b.putString("post_owner_time", notif.getPost_owner_time());
                            b.putString("what", "show_notification_post");
                            i.putExtras(b);
                            i.putExtra("Open", "starred");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);
                        } else if (notif.getType().equals("comments")) {
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle b = new Bundle();
                            b.putString("post_owner_username", notif.getPost_owner_username());
                            b.putString("post_owner_time", notif.getPost_owner_time());
                            b.putString("notif_commenter_username", notif.getCommenter_username());
                            b.putString("notif_comment_time", notif.getComment_time());
                            b.putString("what", "show_notification_post");
                            i.putExtras(b);
                            i.putExtra("Open", "starred");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);
                        } else if (notif.getType().equals("comment_likes")) {
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle b = new Bundle();
                            b.putString("post_owner_username", notif.getPost_owner_username());
                            b.putString("post_owner_time", notif.getPost_owner_time());
                            b.putString("notif_commenter_username", notif.getComment_owner_username());
                            b.putString("notif_comment_time", notif.getComment_owner_time());
                            b.putString("what", "show_notification_post");
                            i.putExtras(b);
                            i.putExtra("Open", "starred");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);
                        } else if (notif.getType().equals("follow") || notif.getType().equals("follow_request")) {
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle b = new Bundle();
                            b.putString("searchUsername", notif.getFollower_username());
                            i.putExtras(b);
                            i.putExtra("Open", "search_profile");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);
                        }
                    }
                });
            }
        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        if(notification!=null)
        return notification.size();
        else return fullname.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder{
        private TextView contents,fullname,like_counter,reply;
        private AppCompatImageButton like_outline,liked,accept_btn,reject_btn;
        private CircleImageView profile_pic;
        private RelativeLayout rl;
        private FrameLayout followApprovalCont;
        private View margin;
        public SearchViewHolder(View v) {
            super(v);
            contents=v.findViewById(R.id.accountSettingsName);
            fullname=v.findViewById(R.id.fullnameComm);
            like_counter=v.findViewById(R.id.likeComm);
            reply=v.findViewById(R.id.replyComm);
            like_outline=v.findViewById(R.id.like_out_Comm);
            liked=v.findViewById(R.id.liked_Comm);
            profile_pic=v.findViewById(R.id.userImageComm);
            margin=v.findViewById(R.id.headAccSett);
            rl=v.findViewById(R.id.rl_Acc);
            followApprovalCont=v.findViewById(R.id.followApprovalCont);
            accept_btn=v.findViewById(R.id.acceptFollow);
            reject_btn=v.findViewById(R.id.rejectFollow);

            final Typeface reg_font = Typeface.createFromAsset((fullname.getContext()).getAssets(), "fonts/"+"Lato-Regular.ttf");
            contents.setTypeface(reg_font);
            fullname.setTypeface(reg_font);


            ButtonTint tint=new ButtonTint("white");
            tint.setTint(rl);
        }
    }
   private int getDp(int px){
        float d =  parent.getContext().getResources().getDisplayMetrics().density;
       int dp= (int) (px * d);
       return dp;
    }
}
