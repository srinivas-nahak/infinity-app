package community.infinity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import community.infinity.adapters.TimeLineSocietyAdapter;
import community.infinity.network_related.SocketAddress;
import custom_views_and_styles.HeightWrappingViewPager;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

/**
 * Created by Srinu on 07-12-2017.
 */

public class ListItem{
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    public ListItem(final String username, final String time, final TextView tv, final String event, final ImageView outline, final ImageView like, final FragmentManager manager,Context ctx){
        Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
            @Override
            public void run() {
                socket.disconnect();
                socket.connect();
                SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(ctx);
                final String my_username=sh.getString("username",null);
                JSONObject ov=new JSONObject();
                try {
                    if(event.equals("likes")){
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                         String  currentTime = simpleDateFormat.format(new Date());
                         Hawk.init(ctx).build();
                        ov.put("like_liker_username", my_username);
                        if(Hawk.get("myFullName")!=null)ov.put("like_liker_fullname",Hawk.get("myFullName"));
                        else ov.put("like_liker_fullname",sh.getString("myFullName",null));
                        if(Hawk.get("myProfilePic")!=null) ov.put("like_liker_profPic",Hawk.get("myProfilePic"));
                        else ov.put("like_liker_profPic",sh.getString("myProfilePic",null));
                        ov.put("like_post_owner_username",username);
                        ov.put("like_post_owner_time",time);
                        ov.put("like_time",currentTime);
                    }
                    else if(event.equals("unlike")){
                        ov.put("unlike_liker_username", my_username);
                        ov.put("unlike_post_owner_username",username);
                        ov.put("unlike_post_owner_time",time);
                    }
                    else if(event.equals("check_like")){
                        ov.put("check_like_liker_username", my_username);
                        ov.put("check_like_post_owner_username",username);
                        ov.put("check_like_time",time);
                    }
                    else if(event.equals("like_counter")){
                        ov.put("counter_like_post_owner_username",username);
                        ov.put("counter_like_time",time);
                    }
                    else if(event.equals("star")){
                        //SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(tv.getContext());
                        ov.put("star_starrer_username",my_username);
                        ov.put("star_post_owner_username",username);
                        ov.put("star_time",time);
                    }
                    else if(event.equals("unstar")){
                        //SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(tv.getContext());
                        ov.put("unstar_starrer_username", my_username);
                        ov.put("unstar_post_owner_username",username);
                        ov.put("unstar_time",time);
                    }
                    else if(event.equals("check_star")){
                        ov.put("check_star_starrer_username", my_username);
                        ov.put("check_star_post_owner_username",username);
                        ov.put("check_star_time",time);
                    }
                    else if(event.equals("comm_counter")){
                        ov.put("count_comment_post_owner_username",username);
                        ov.put("count_comment_time",time);
                    }
                    else if(event.equals("following_counter")){
                        ov.put("count_following_username",username);
                    }
                    else if(event.equals("followers_counter")){
                        ov.put("count_followers_username",username);
                    }
                    else if(event.equals("posts_counter")){
                        ov.put("count_posts_username",username);
                    }
                    else if(event.equals("notification_comment")){
                        ov.put("notification_commenter_username",username);
                        ov.put("notification_comment_time",time);
                    }
                    else if(event.equals("get_full_name")){
                        ov.put("get_fullname_username",username);
                    }
                    socket.emit("data",ov);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


        socket.on(event, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                        try {
                            if(event.equals("notification_comment")){
                                JSONArray dati=(JSONArray) args[0];

                                JSONObject ob=dati.getJSONObject(0);
                                final  String st=ob.getString("content");
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        try {

                                            //Setting Comment Content

                                            tv.setText(URLDecoder.decode(st, "UTF-8"));
                                            socket.disconnect();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                handler.sendEmptyMessage(1);
                                //adapter.notifyDataSetChanged();
                            }
                            else if(event.equals("get_full_name")) {

                                final JSONObject data = (JSONObject)args[0];
                                JSONArray ar=data.getJSONArray("fullname");
                                JSONObject ob=ar.getJSONObject(0);

                                final String st = ob.getString("fullname");

                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (st!=null&&st.length()>0&&!st.equals("null")) {
                                                tv.setText(st);
                                                socket.disconnect();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                            else if(event.equals("comm_counter")){
                                final JSONObject data = (JSONObject)args[0];
                                final String st = data.getString("comm_counter");

                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        try {
                                            // setNo_of_comments(st);
                                            if(Integer.valueOf(st)==0){
                                                tv.setVisibility(View.GONE);
                                            }
                                            else{
                                                tv.setVisibility(View.VISIBLE);
                                                if(Integer.valueOf(st)==1) tv.setText(st+" "+"Comment");
                                                else tv.setText(st+" "+"Comments");
                                                tv.invalidate();

                                            }
                                            socket.disconnect();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                handler.sendEmptyMessage(1);
                            }
                            else if(event.equals("following_counter")){
                                final JSONObject data = (JSONObject)args[0];
                                final String st = data.getString("following_counter");

                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        try {
                                            tv.setText(st);
                                            tv.invalidate();
                                            socket.disconnect();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                handler.sendEmptyMessage(1);
                            }
                            else if(event.equals("followers_counter")){
                                final JSONObject data = (JSONObject)args[0];
                                final String st = data.getString("followers_counter");

                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        try {
                                            tv.invalidate();
                                            tv.setText(st);
                                            socket.disconnect();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                handler.sendEmptyMessage(1);
                            }
                            else if(event.equals("posts_counter")){
                                final JSONObject data = (JSONObject)args[0];
                                final String st = data.getString("posts_counter");
                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            tv.invalidate();
                                            tv.setText("- "+st);
                                            socket.disconnect();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            else if(event.equals("likes")){
                                final JSONObject data = (JSONObject)args[0];
                                if(data.getString("status").equals("yes")) socket.disconnect();
                            }
                            else if(event.equals("unlike")){
                                final JSONObject data = (JSONObject)args[0];
                                if(data.getString("status").equals("yes")) socket.disconnect();
                            }
                            else if(event.equals("check_like")){
                                final JSONObject data = (JSONObject)args[0];
                                final String st = data.getString("like_status");
                                final int i=Integer.valueOf(st);
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        try {

                                            if(i==1){
                                                outline.setVisibility(View.INVISIBLE);
                                                like.setVisibility(View.VISIBLE);
                                                socket.disconnect();
                                            }
                                            // socket.disconnect();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                handler.sendEmptyMessage(1);
                            }
                            else if(event.equals("like_counter")){
                                final JSONObject data = (JSONObject)args[0];
                                final String st = data.getString("no_of_likes");
                                final int i=Integer.valueOf(st);
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {

                                            if(i!=0){
                                                tv.setVisibility(View.VISIBLE);
                                                if(i==1) tv.setText(i+" like");
                                                else tv.setText(i+" likes");
                                                tv.invalidate();
                                                socket.disconnect();
                                            }
                                            else{
                                                tv.setVisibility(View.GONE);
                                            }
                                            // socket.disconnect();
                                    }
                                };
                                handler.sendEmptyMessage(1);
                            }
                            else if(event.equals("star")){
                                final JSONObject data = (JSONObject)args[0];
                                if(data.getString("status").equals("yes")) socket.disconnect();
                            }
                            else if(event.equals("unstar")){
                                final JSONObject data = (JSONObject)args[0];
                                if(data.getString("status").equals("yes")) socket.disconnect();
                            }
                            else if(event.equals("check_star")){
                                final JSONObject data = (JSONObject)args[0];
                                final String st = data.getString("starred_status");
                                final int i=Integer.valueOf(st);
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {

                                            if(i==1){
                                                outline.setVisibility(View.GONE);
                                                like.setVisibility(View.VISIBLE);
                                                socket.disconnect();
                                            }
                                            // socket.disconnect();

                                    }
                                };
                                handler.sendEmptyMessage(1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        });

    }

   //Comment Like
    public ListItem(final String post_owner_username,final String post_owner_time,final String comment_owner_username,final String comment_time,
    final ImageView like_outline,final ImageView like,final TextView comm_like_count,final String myUsername,final String event){
        Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
            @Override
            public void run() {
                socket.disconnect();
                socket.connect();
                JSONObject ov=new JSONObject();
                try {
                    if(event.equals("comment_likes")){
                        //SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(tv.getContext());
                        Hawk.init(like_outline.getContext()).build();
                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(like_outline.getContext());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                        String  currentTime = simpleDateFormat.format(new Date());

                        ov.put("comment_like_post_owner_username",post_owner_username);
                        ov.put("comment_like_post_owner_time",post_owner_time);
                        ov.put("comment_like_comment_owner_username",comment_owner_username);
                        ov.put("comment_like_comment_owner_time",comment_time);
                        if(Hawk.get("myUserName")!=null)ov.put("comment_liker_username", Hawk.get("myUserName"));
                        else ov.put("comment_liker_username", sh.getString("username",null));
                        if(Hawk.get("myFullName")!=null)ov.put("comment_liker_fullname",Hawk.get("myFullName"));
                        else ov.put("comment_liker_fullname",sh.getString("myFullName",null));
                        if(Hawk.get("myProfilePic")!=null) ov.put("comment_liker_profPic",Hawk.get("myProfilePic"));
                        else ov.put("comment_liker_profPic",sh.getString("myProfilePic",null));
                        ov.put("comment_like_time",currentTime);

                    }
                    if(event.equals("comment_unlike")){
                        ov.put("comment_unlike_post_owner_username",post_owner_username);
                        ov.put("comment_unlike_post_owner_time",post_owner_time);
                        ov.put("comment_unlike_comment_owner_username",comment_owner_username);
                        ov.put("comment_unlike_comment_owner_time",comment_time);
                        ov.put("comment_unliker_username", myUsername);
                    }
                    if(event.equals("comment_like_count")){
                        ov.put("comment_like_count_post_owner_username",post_owner_username);
                        ov.put("comment_like_count_post_owner_time",post_owner_time);
                        ov.put("comment_like_count_comment_owner_username",comment_owner_username);
                        ov.put("comment_like_count_comment_owner_time",comment_time);
                    }
                    if(event.equals("comment_check_like")){
                        //SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(tv.getContext());
                        ov.put("comment_check_like_post_owner_username",post_owner_username);
                        ov.put("comment_check_like_post_owner_time",post_owner_time);
                        ov.put("comment_check_like_comment_owner_username",comment_owner_username);
                        ov.put("comment_check_like_comment_owner_time",comment_time);
                        ov.put("comment_check_like_liker_username", myUsername);

                    }
                  socket.emit("data",ov);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                socket.on(event, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            final JSONObject data = (JSONObject)args[0];
                            if(event.equals("comment_like")){
                                if(data.getString("status").equals("yes")) socket.disconnect();
                            }
                            else if(event.equals("comment_unlike")){
                                if(data.getString("status").equals("yes")) socket.disconnect();
                            }
                            else if(event.equals("comment_like_count")){
                                final String st = data.getString("no_of_comment_like");
                                final int i=Integer.valueOf(st);
                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Toast.makeText(like_outline.getContext(),data.toString(),Toast.LENGTH_SHORT).show();
                                        if(i>0){
                                            comm_like_count.setVisibility(View.VISIBLE);
                                            if(i==1) comm_like_count.setText(i+" like");
                                            else comm_like_count.setText(i+" likes");
                                            comm_like_count.invalidate();
                                            socket.disconnect();
                                        }
                                    }
                                });
                            }
                            else{//Checking Comment is liked or not
                                final String st = data.getString("comment_like_status");
                                final int i=Integer.valueOf(st);
                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(i==1){
                                            like_outline.setVisibility(View.GONE);
                                            like.setVisibility(View.VISIBLE);
                                            socket.disconnect();
                                        }
                                    }
                                });
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

    }
    //ProfilePic
    public ListItem(final String username, final CircleImageView iv){
        Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                JSONObject obj=new JSONObject();
                try {
                    obj.put("prof_username",username);
                    socket.emit("data",obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.on("profile", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        final JSONObject data = (JSONObject)args[0];
                        try {
                            JSONArray ar=data.getJSONArray("profile_pic");
                            JSONObject ob=ar.getJSONObject(0);

                            Needle.onMainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String st;
                                        if(ob.has("profile_pic"))st = ob.getString("profile_pic");
                                        else st="";
                                        if(iv.getContext()!=null) {
                                            if(st.equals("")) iv.setImageResource(R.drawable.profile);
                                            else {

                                                ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));
                                                Glide.with(iv.getContext()).load(st).
                                                        apply(new RequestOptions().override(50, 50).placeholder(cd).diskCacheStrategy(DiskCacheStrategy.NONE).error(R.drawable.profile).
                                                                skipMemoryCache(true)).thumbnail(0.1f).into(iv);
                                            }
                                        }
                                        socket.disconnect();


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }

    public ListItem(final String selected_soc,final String username, final TextView tv_one, final TextView tv_two, final CircleImageView circleImg,
                    final KenBurnsView kenBurnsView,final RecyclerView societyRView, final String event) {
        Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
            @Override
            public void run() {
                socket.disconnect();
                socket.connect();
                JSONObject ov=new JSONObject();
                try {
                    if(event.equals("profile_info")){
                        ov.put("profile_info",username);
                        socket.emit("data",ov);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.on(event, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {

                        try {
                            if(event.equals("profile_info")) {
                                String society_list=null, bio=null,ss;
                                JSONArray dati = (JSONArray) args[0];
                                JSONObject ob = dati.getJSONObject(0);
                                final String st = ob.getString("fullname");
                                if(ob.has("profile_pic")) ss = ob.getString("profile_pic");
                                else ss="";
                                if(ob.has("bio")) bio = ob.getString("bio");

                                if(ob.has("society_list"))   society_list=ob.getString("society_list");
                                String wall=null;
                                if(ob.has("wall_pic")) wall=ob.getString("wall_pic");
                                // final  String uu=ob.getString("username");
                                String finalWall = wall;
                                String finalSociety_list = society_list;
                                String finalBio = bio;
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @SuppressLint("NewApi")
                                    @Override
                                    public void handleMessage(Message msg) {
                                        try {
                                            tv_one.setText(st);
                                            tv_one.setGravity(Gravity.START);
                                            tv_one.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);

                                            ///Setting Only person icon if profile pid is null
                                            if(ss.equals("")) {
                                                circleImg.setImageResource(R.drawable.profile);
                                            }
                                            else {
                                                ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));

                                                Glide
                                                        .with(circleImg.getContext())
                                                        .asBitmap()
                                                        .load(ss)
                                                        .apply(new RequestOptions().placeholder(cd).diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                .error(R.drawable.profile) .skipMemoryCache(true).override(250,250))
                                                        .thumbnail(0.1f)
                                                        .into(circleImg);
                                            }
                                            if(finalWall!=null) {
                                                Glide.with(kenBurnsView.getContext()).clear(kenBurnsView);
                                                Glide.with(kenBurnsView.getContext())
                                                        .asBitmap()
                                                        .load(finalWall)
                                                        .apply(new RequestOptions()
                                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                        .priority(Priority.HIGH)
                                                        .skipMemoryCache(true))
                                                        .into(new SimpleTarget<Bitmap>() {
                                                            @Override
                                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super
                                                                    Bitmap> transition) {
                                                                CustomPagerAdapter c=new CustomPagerAdapter();
                                                                kenBurnsView.setImageBitmap(c.compressImage(resource,"wall"));
                                                            }
                                                        });
                                            }

                                            if(finalBio!=null&&finalBio.length()>0) {
                                                tv_two.setText(URLDecoder.decode(URLDecoder.decode(finalBio, "UTF-8"),"UTF-8"));
                                            }



                                            //Setting Society List
                                            if(finalSociety_list!=null) {
                                                String[] splits = finalSociety_list.replace("[", "").replace("]",
                                                        "").split(",");
                                                ArrayList<String> arrayList = new ArrayList<>();
                                                arrayList.add(0,"Home");
                                                for (int i = 0; i < splits.length; i++) {
                                                    arrayList.add(splits[i].replace("\"", ""));
                                                }
                                                if(arrayList.contains("null")) societyRView.setVisibility(View.GONE);
                                                else {
                                                    TimeLineSocietyAdapter adapter = new TimeLineSocietyAdapter(arrayList, selected_soc,
                                                            false);
                                                    societyRView.setAdapter(adapter);
                                                }
                                            }


                                            socket.disconnect();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                handler.sendEmptyMessage(1);

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

    }

    public ListItem(final Activity activity, final HeightWrappingViewPager pager, final ArrayList<String> arrayList, final int position,
                    final ImageView like_outline,final ImageView like_btn,final ImageView middle_heart){
        //Setting ViewPager
        CustomPagerAdapter adp=new CustomPagerAdapter(activity,arrayList,like_outline,like_btn,middle_heart);
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return false;
            }
        });
          pager.setCurrentItem(position, false);
          pager.clearAnimation();
        adp.notifyDataSetChanged();
        pager.setAdapter(adp);
        pager.setOffscreenPageLimit(1);
    }

    private int getDp(int px,Context context){
        float d = context.getResources().getDisplayMetrics().density;
        return (int) (px * d);
    }
}


