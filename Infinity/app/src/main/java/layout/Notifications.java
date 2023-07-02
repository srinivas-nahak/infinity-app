package layout;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import community.infinity.R;
import community.infinity.network_related.SocketAddress;
import community.infinity.adapters.NotificationData;
import community.infinity.adapters.SearchAdapter;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

public class Notifications extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String username;
    private View v;
    private TextView noResultTxt;
    private RecyclerView notifRecycler;
    private List<NotificationData> notification=new ArrayList<>();
    private SearchAdapter adapter;
    private RelativeLayout welcome_cont;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Socket socket;
    {
        try{
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public Notifications() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("notification_list",handleNotification);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_notifications, container, false);
        notifRecycler=v.findViewById(R.id.notificationRecycler);
        noResultTxt=v.findViewById(R.id.noNotifTxt);
        swipeRefreshLayout=v.findViewById(R.id.swipe_containerNotification);
        welcome_cont= getActivity().findViewById(R.id.welcomeMsgCont);


        //Building Hawk
        Hawk.init(v.getContext()).build();


        //Getting Username
        SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if(Hawk.get("myUserName")!=null)username=Hawk.get("myUserName");
        else username=sh.getString("username",null);




        //Loading data with swipeRefreshLay
        swipeRefreshLayout.setOnRefreshListener(this);
       /* try {
            Field f =swipeRefreshLayout.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView img = (ImageView)f.get(swipeRefreshLayout);
            img.setBackgroundColor(Color.parseColor("#001919"));
            // img.setImageResource(R.drawable.heart_icon);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#ffffff"));*/

        //Setting Recylerview
        adapter=new SearchAdapter(notification);
        notifRecycler.setHasFixedSize(true);
        notifRecycler.setLayoutManager(new LinearLayoutManager(v.getContext()));
        notifRecycler.setAdapter(adapter);


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();

        JSONObject ob=new JSONObject();
        try {
            ob.put("notification_list_username",username);
            socket.emit("data",ob);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        welcome_cont.setVisibility(View.GONE);


        // Fetching data from server
        JSONObject ob=new JSONObject();
        try {
            ob.put("notification_list_username",username);
            socket.emit("data",ob);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ConnectivityManager conMgr =  (ConnectivityManager)v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if (netInfo == null){
                    Toast.makeText(v.getContext(),"You're not connected to internet",Toast.LENGTH_LONG).show();
                }

                if(swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                   // Toast.makeText(v.getContext(),"Sorry couldn't fetch data . Please try again",Toast.LENGTH_LONG).show();
                }
            }
        },20000);
    }


    private  Emitter.Listener handleNotification = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            try {
                JSONArray jsonArray=(JSONArray)args[0];
                Needle.onMainThread().execute(() -> {
                    try {
                         if(jsonArray==null||jsonArray.length()==0) {
                             new Handler().postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     noResultTxt.setVisibility(View.VISIBLE);
                                 }
                             },500);
                         }
                         else noResultTxt.setVisibility(View.GONE);
                        String type,notif_sender_profPic,post_owner_time=null,like_time=null,comment_time=null,liker_username=null,commenter_username=null,notification_data,
                        post_owner_username=null,follower_username=null,comment_owner_username=null,comment_owner_time=null,comment_liker_username=null,comment_like_time=null;

                        notification.clear();
                    for(int i=0;i<jsonArray.length();i++){

                            JSONObject ob=jsonArray.getJSONObject(i);
                            notification_data=ob.getString("notification_data");
                            type=ob.getString("type");
                            if(ob.has("notif_sender_profPic"))notif_sender_profPic=ob.getString("notif_sender_profPic");
                            else notif_sender_profPic="";


                            if(type.equals("likes")||type.equals("comments")||type.equals("comment_likes")) {
                                post_owner_username = ob.getString("post_owner_username");
                                post_owner_time = ob.getString("owner_post_time");
                            }
                            if(type.equals("comment_likes")){
                                comment_owner_username=ob.getString("comment_owner_username");
                                comment_owner_time=ob.getString("comment_owner_time");
                                comment_liker_username=ob.getString("comment_liker_username");
                                comment_like_time=ob.getString("comment_like_time");
                            }
                            if(type.equals("follow")||type.equals("follow_request")) {
                                follower_username = ob.getString("follower_username");
                            }
                            //if(type.equals("comment_likes")) Toast.makeText(v.getContext(),ob.getString("comment_liker_username"),Toast.LENGTH_LONG).show();

                            if(ob.has("comment_time"))comment_time=ob.getString("comment_time");
                            if(ob.has("commenter_username")) commenter_username=ob.getString("commenter_username");
                            if(ob.has("like_time"))like_time=ob.getString("like_time");
                            if(ob.has("liker_username")) liker_username=ob.getString("liker_username");
                        NotificationData n=new NotificationData(type,notif_sender_profPic,post_owner_username,post_owner_time,like_time,comment_time,liker_username,commenter_username,follower_username,notification_data,
                                comment_owner_username,comment_owner_time,comment_liker_username,comment_like_time);
                        notification.add(n);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                Log.e("error",e.toString());
            }
        }
    };


}
