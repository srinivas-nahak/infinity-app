package layout;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.RecyclerViewItems.SpeedyLinearLayoutManager;
import community.infinity.adapters.CustomRecyclerViewAdapter;
import community.infinity.network_related.SocketAddress;
import community.infinity.adapters.MyPreloadModelProvider;
import community.infinity.adapters.StarredAdapter;
import community.infinity.adapters.TimeLineSocietyAdapter;
import community.infinity.adapters.TimelineData;
import custom_views_and_styles.CustomRecyclerView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

public class InfiniteTimeline extends Fragment {


    private CustomRecyclerView posts_rView,society_rView;
    private StarredAdapter gridPostAdapter;
    private RecyclerView.Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String society_name="home",my_username;
    private ArrayList<String> society_namArr=new ArrayList<>();
    private RelativeLayout welcome_cont;
    private FrameLayout selected_society;
    private int selected_soc_pos=0;
    private GridLayoutManager gridLayoutManager;
    private List<TimelineData> timelineDataList=new ArrayList<>() ;
    private String post_username,post_fullname,post_profPic,post_time,post_link,post_caption,private_post_stat,comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp="home";
    private View v;
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        my_username=sharedPrefs.getString("username",null);
        socket.on("infinite_posts",handlePosts);
        socket.on("total_society_list",handleSocietyList);
    }


    public InfiniteTimeline() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.society_show,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
       // selected_society=v.findViewById(R.id.selected_society_layout);
        this.v=v;
        posts_rView =  v.findViewById(R.id.featured_recycler_view_society);
        society_rView=getActivity().findViewById(R.id.selected_society_infinite_timeline);
        swipeRefreshLayout=v.findViewById(R.id.swipe_container);
        selected_society=getActivity().findViewById(R.id.selected_society_layout);
        welcome_cont=getActivity().findViewById(R.id.welcomeMsgCont);
        final ViewPager parentViewPg=getActivity().findViewById(R.id.pager);
        final FrameLayout loadingView=getActivity().findViewById(R.id.loadingView);
       // final FrameLayout parentFrameLay=getActivity().findViewById(R.id.toolBoxHomeScreen);

        


        //Building Hawk
        Hawk.init(v.getContext()).build();

        gridLayoutManager = new GridLayoutManager(v.getContext(),3);
            //gridLayoutManager.setItemPrefetchEnabled(true);
            //gridLayoutManager.setInitialPrefetchItemCount(10);

        gridLayoutManager.setItemPrefetchEnabled(true);
        gridLayoutManager.setInitialPrefetchItemCount(2);

        posts_rView.setLayoutManager(gridLayoutManager);
        posts_rView.setItemAnimator(null);
        posts_rView.setHasFixedSize(true);
        posts_rView.setItemViewCacheSize(10);
        posts_rView.setDrawingCacheEnabled(true);
        posts_rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);



        gridPostAdapter=new StarredAdapter(timelineDataList);
        posts_rView.setAdapter(gridPostAdapter);

        //Downloading Images in advance
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                ListPreloader.PreloadSizeProvider sizeProvider =
                        new FixedPreloadSizeProvider(200, 200);
                ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(timelineDataList,v.getContext());
                RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                        new RecyclerViewPreloader<>(
                                Glide.with(v.getContext()), modelProvider, sizeProvider,5);
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        posts_rView.addOnScrollListener(preloader);
                    }
                });

            }
        });

        //Getting Data from server
        JSONObject obj=new JSONObject();
        try {
            obj.put("infinite_timeline_username",my_username);
            obj.put("infinite_timeline_society",society_name.toLowerCase());
            socket.emit("data",obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Society RecyclerView
        society_rView.setHasFixedSize(true);
        society_rView.setNestedScrollingEnabled(false);
        SpeedyLinearLayoutManager.MILLISECONDS_PER_INCH=60f;
        SpeedyLinearLayoutManager layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.HORIZONTAL,false);
        society_rView.setLayoutManager(layoutManager);


        ///Setting SwipeRefreshLay
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


        society_rView.addOnItemTouchListener(
                new RecyclerItemClickListener(v.getContext(), society_rView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {


                        selected_soc_pos=position;


                        //Setting autoscroll on click
                        LinearLayoutManager layoutManager=((LinearLayoutManager)society_rView.getLayoutManager());
                        int avg =
                                (layoutManager.findFirstCompletelyVisibleItemPosition()+(layoutManager.findFirstCompletelyVisibleItemPosition()+1)+
                                        layoutManager.findLastCompletelyVisibleItemPosition())/3;


                        if(position>avg) society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, position+1);
                        else if(position!=0) society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, position-1);
                        else society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, 0);
                        
                        //Clearing Data of Rview for adding new Data
                        timelineDataList.clear();
                        /*posts_rView.getRecycledViewPool().clear();
                        // posts_rView.invalidate();
                        gridPostAdapter.notifyDataSetChanged();
                        posts_rView.removeAllViewsInLayout();*/
                        showData(timelineDataList);


                        //Capsule Color Change and Animation
                        view.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons));

                        FrameLayout msgContainer=view.findViewById(R.id.msgContainer);
                        TextView soc_names=view.findViewById(R.id.timeline_soc_names);
                        for(FrameLayout msgContain: TimeLineSocietyAdapter.msgContainerList){
                            msgContain.setBackgroundResource(R.drawable.unselected_soc);
                        }

                        msgContainer.setBackgroundResource(R.drawable.textinputborder);
                        soc_names.setTextColor(Color.parseColor("#001919"));

                        society_name = society_namArr.get(position);


                        //Getting Data from server
                        /*JSONObject obj=new JSONObject();
                        try {

                            obj.put("infinite_timeline_username",my_username);
                            obj.put("infinite_timeline_society",society_name.toLowerCase());
                            socket.emit("data",obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
                        swipeRefreshLayout.post(new Runnable() {
                            @Override public void run() {
                                swipeRefreshLayout.setRefreshing(true);
                                // directly call onRefresh() method
                                swipeRefreshListner.onRefresh();
                            }
                        });

                        //Showing Loading
                        loadingView.setVisibility(View.VISIBLE);
                        parentViewPg.setVisibility(View.INVISIBLE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingView.setVisibility(View.GONE);
                                parentViewPg.setVisibility(View.VISIBLE);
                            }
                        },2500);

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                }));

         swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 updateData();
             }
         });





    }

    private void checking(){

        if(society_namArr.size()>0){
            society_namArr.add(0,"Home");
            adapter=new TimeLineSocietyAdapter(society_namArr,society_name,false);
            society_rView.setAdapter(adapter);

            society_rView.scrollToPosition(selected_soc_pos);

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Fetching data from server
        welcome_cont.setVisibility(View.GONE);
        socket.disconnect();
        socket.connect();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject obj=new JSONObject();
                try {
                    obj.put("get_total_society_list","yes");
                    socket.emit("data",obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },200);

    }


    void showData(List<TimelineData> timelineDataList){
        gridPostAdapter = new StarredAdapter(timelineDataList);
        posts_rView.setLayoutManager(null);
        posts_rView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        posts_rView.setAdapter(null);
        posts_rView.setLayoutManager(gridLayoutManager);
        posts_rView.setAdapter(gridPostAdapter);
        //assumed you attached your layout manager earlier
    }

    private void updateData(){
        swipeRefreshLayout.setRefreshing(true);
        welcome_cont.setVisibility(View.GONE);

        //Getting Data from server
        JSONObject obj=new JSONObject();
        try {
            obj.put("infinite_timeline_username",my_username);
            obj.put("infinite_timeline_society",society_name.toLowerCase());
            socket.emit("data",obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject obj=new JSONObject();
                try {
                    obj.put("get_total_society_list","yes");
                    socket.emit("data",obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },200);
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

    SwipeRefreshLayout.OnRefreshListener swipeRefreshListner = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            updateData();
        }
    };

    private void addTimelineData(String username,String fullname,String post_profPic,String time,String img_link,String caption,
                                 String private_post_stat,String comment_disabled,String share_disabled,String download_disabled,String likes_counter,
                                 String comments_counter,String short_book_content,String society_name_adp){
        boolean isRepeated = false;
        for(TimelineData data:timelineDataList){
            if (data.getTime().equals(time)) {
                isRepeated = true;
            }
        }
        if(!isRepeated){
            timelineDataList.add(new TimelineData(username,fullname,post_profPic,time,img_link,caption,private_post_stat,comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp));
            // posts_rView.scrollToPosition(gridPostAdapter.getItemCount()-1);
           // posts_rView.scrollToPosition(0);
        }


         //gridPostAdapter.notifyItemInserted(timelineDataList.size()-1);

    }



  //Emitter Listeners

    private  Emitter.Listener handlePosts = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            try {
                JSONArray jsonArray=(JSONArray)args[0];
                Needle.onMainThread().execute(() -> {
                    timelineDataList.clear();
                    swipeRefreshLayout.setRefreshing(false);

                    for(int i=0;i<jsonArray.length();i++){
                        try {
                            //JSONArray arr=jsonArray.getJSONArray(i);
                            JSONObject ob=jsonArray.getJSONObject(i);

                            post_username=ob.getString("_pid");
                            post_fullname=ob.getString("owner_fullname");

                            if(ob.has("owner_profPic"))post_profPic=ob.getString("owner_profPic");
                            else post_profPic="";

                            post_time=ob.getString("time");

                            post_link=ob.getString("img_link");
                            likes_counter=ob.getString("likes_counter");
                            comments_counter=ob.getString("comments_counter");
                            if(ob.has("caption")) post_caption=ob.getString("caption");
                            else post_caption=null;

                            //Skipping Private Posts
                            if(ob.getString("private_post_stat").equals("yes")&&!post_username.equals(my_username)) {
                                continue;
                            }
                            else
                                private_post_stat = ob.getString("private_post_stat");

                            comment_disabled=ob.getString("comment_disabled");

                            share_disabled=ob.getString("share_disabled");

                            download_disabled=ob.getString("download_disabled");

                            if(ob.has("short_book_content")) short_book_content=ob.getString("short_book_content");
                            else short_book_content=null;
                            society_name_adp=ob.getString("society");

                            addTimelineData(post_username,post_fullname,post_profPic,post_time,post_link,post_caption,
                                    private_post_stat,comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    gridPostAdapter.notifyDataSetChanged();



                });

            } catch (Exception e) {
                Log.e("error",e.toString());
            }
        }
    };

    private  Emitter.Listener handleSocietyList = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            try {
                JSONArray jsonArray=(JSONArray)args[0];


                //Clearing whole data in order to show new data in a proper sequence
                //timelineDataList.clear();
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Making SocietyContainer invisible if no society is selected
                            if(jsonArray!=null&&jsonArray.length()==0) selected_society.setVisibility(View.GONE);

                            //Getting Societylist
                            JSONObject ob= (JSONObject) jsonArray.get(0);
                            String s=ob.getString("total_society_list");
                            String[] splits =  s.replace("[","").replace("]","").split(",");
                            ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(splits));

                            society_namArr.clear();

                            for(int i=0;i<arrayList.size();i++){
                                society_namArr.add(arrayList.get(i).replace("\"", ""));
                            }
                            checking();
                            //Toast.makeText(v.getContext(),arrayList.get(0),Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                });


            } catch (Exception e) {
                Log.e("error",e.toString());
            }
        }
    };


}
