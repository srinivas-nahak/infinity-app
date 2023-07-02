package community.infinity.SettingsMenu;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.github.pwittchen.swipe.library.Swipe;
import com.github.pwittchen.swipe.library.SwipeListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import community.infinity.CurrentSociety;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.RecyclerViewItems.SpeedyLinearLayoutManager;
import community.infinity.adapters.TimeLineSocietyAdapter;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.Home_Screen;
import community.infinity.adapters.CustomRecyclerViewAdapter;
import community.infinity.adapters.MyPreloadModelProvider;
import community.infinity.adapters.StarredAdapter;
import community.infinity.adapters.TimelineData;
import community.infinity.writing.RememberTextStyle;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.CustomRecyclerView;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

/**
 * Created by Srinu on 29-10-1517.
 */

public class Starred extends Fragment {


    private CustomRecyclerView rView,soc_rView;
    private ImageButton back;
    private TextView heading,noResult;
    private SpeedyLinearLayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<String> society_namArr=new ArrayList<>();
    private CustomRecyclerViewAdapter adapteru;
    private List<TimelineData> timelineDataList=new ArrayList<>();
    private String post_username,post_fullname,postProfPic,post_time,post_link,post_caption,private_post_stat,comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp="home";
    private StarredAdapter gridadp;
    private FrameLayout loading_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static Swipe swipe;
    private String society_name="home";
    private View v;
    private String what,my_username;
    //private Bundle bundle;
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public Starred() {
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("tag_posts",handlePosts);
        socket.on("tips_posts",handleTipsPosts);
        socket.on("opportunity_posts",handleTipsPosts);
        socket.on("notification_post",handlePosts);
        socket.on("total_society_list",handleSocietyList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.starred_showing, container, false);
        rView = v.findViewById(R.id.starred_rView);
        back=v.findViewById(R.id.back_of_starred);
        heading=v.findViewById(R.id.headingStarred);
        noResult=v.findViewById(R.id.noResultTxtStarred);
        swipeRefreshLayout=v.findViewById(R.id.swipe_containerStarred);
        loading_view=v.findViewById(R.id.loadingViewStarred);
        soc_rView=v.findViewById(R.id.selected_society_starred);
        final ConstraintLayout parentLay=v.findViewById(R.id.starredMainCont);
        DragToClose dragToClose=v.findViewById(R.id.dragViewStarred);

        //Setting Bg
        parentLay.setBackgroundResource(RememberTextStyle.themeResource);


        //Building Hawk
        Hawk.init(v.getContext()).build();

        //Adding Tint
        ButtonTint tint=new ButtonTint("white");
        tint.setTint(back);



        //Getting Username
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if(sharedPrefs.contains("username"))my_username=sharedPrefs.getString("username",null);
        else my_username=Hawk.get("myUserName");


        //Making LoadingView visible first then RView
        loading_view.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loading_view.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
            }
        },1000);


        //Society RecyclerView
        soc_rView.setHasFixedSize(true);
        soc_rView.setNestedScrollingEnabled(false);
        SpeedyLinearLayoutManager.MILLISECONDS_PER_INCH=60f;
        SpeedyLinearLayoutManager soc_layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.HORIZONTAL,false);
        soc_rView.setLayoutManager(soc_layoutManager);

        //Deleting this key to avoid notif_indicator in home
        if(getArguments().containsKey("go_home")) {
            Hawk.delete("normal_notif");
            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
            SharedPreferences.Editor editor=sh.edit();
            editor.remove("normal_notif");
            editor.apply();
        }

        //Drag to Close view
        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {}

            @Override
            public void onViewCosed() {
                if(getArguments().containsKey("go_home")){
                    Intent i=new Intent(v.getContext(), Home_Screen.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                else getActivity().finish();
            }
        });

        //Loading data with swipeRefreshLay
        swipeRefreshLayout.setColorSchemeColors(0,0,0,0);
        try {
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
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#ffffff"));



        //Enhancing performance of RView
        RecyclerView.ItemAnimator animator = rView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            animator.setChangeDuration(0);
        }
        rView.setNestedScrollingEnabled(false);


        //Getting Bundle String
        what=getArguments().getString("what");

        if (what.equals("tips") ) {
            heading.setText("Tips");

            layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);
            //Getting Device Height
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            //layoutManager.setExtraLayoutSpace(height);
            //rView.setHasFixedSize(true);
           // layoutManager.setItemPrefetchEnabled(true);
           // layoutManager.setInitialPrefetchItemCount(3);
            layoutManager.setItemPrefetchEnabled(true);
            layoutManager.setInitialPrefetchItemCount(2);
            rView.setItemViewCacheSize(10);
            rView.setDrawingCacheEnabled(true);
            rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            rView.setLayoutManager(layoutManager);

            Needle.onBackgroundThread().execute(new Runnable() {
                @Override
                public void run() {
                    //Downloading Images in advance
                    ListPreloader.PreloadSizeProvider sizeProvider =
                            new FixedPreloadSizeProvider(600, 600);
                    ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(timelineDataList,v.getContext());
                    RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                            new RecyclerViewPreloader<>(
                                    Glide.with(v.getContext()), modelProvider, sizeProvider,3);
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            rView.addOnScrollListener(preloader);
                        }
                    });

                }
            });



            //Setting List Adapter
            adapteru = new CustomRecyclerViewAdapter(timelineDataList);

            rView.setAdapter(adapteru);


        }
        else if( what.equals("opportunities")){
            heading.setText("Opportunities");

            layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);
            //Getting Device Height
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            //layoutManager.setExtraLayoutSpace(height);
            //rView.setHasFixedSize(true);
           // layoutManager.setItemPrefetchEnabled(true);
          //  layoutManager.setInitialPrefetchItemCount(3);
            layoutManager.setItemPrefetchEnabled(true);
            layoutManager.setInitialPrefetchItemCount(2);
            rView.setItemViewCacheSize(10);
            rView.setDrawingCacheEnabled(true);
            rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            rView.setLayoutManager(layoutManager);

            Needle.onBackgroundThread().execute(new Runnable() {
                @Override
                public void run() {
                    //Downloading Images in advance
                    ListPreloader.PreloadSizeProvider sizeProvider =
                            new FixedPreloadSizeProvider(600, 600);
                    ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(timelineDataList,v.getContext());
                    RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                            new RecyclerViewPreloader<>(
                                    Glide.with(v.getContext()), modelProvider, sizeProvider,3);
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            rView.addOnScrollListener(preloader);
                        }
                    });

                }
            });


            //Setting List Adapter
            adapteru = new CustomRecyclerViewAdapter(timelineDataList);

            rView.setAdapter(adapteru);
        }

        else if(what.equals("tags")) {

            String tag_searched= getArguments().getString("tag_searched");

            heading.setText(tag_searched);


            SpeedyLinearLayoutManager layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);
            //Getting Device Height
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            //layoutManager.setExtraLayoutSpace(height);
            //rView.setHasFixedSize(true);
           // layoutManager.setItemPrefetchEnabled(true);
          //  layoutManager.setInitialPrefetchItemCount(3);
            layoutManager.setItemPrefetchEnabled(true);
            layoutManager.setInitialPrefetchItemCount(2);
            rView.setItemViewCacheSize(10);
            rView.setDrawingCacheEnabled(true);
            rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            rView.setLayoutManager(layoutManager);



            //Setting List Adapter
                adapteru = new CustomRecyclerViewAdapter(timelineDataList);

            rView.setAdapter(adapteru);

           Needle.onBackgroundThread().execute(new Runnable() {
                @Override
                public void run() {
                    //Downloading Images in advance
                    ListPreloader.PreloadSizeProvider sizeProvider =
                            new FixedPreloadSizeProvider(600, 600);
                    ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(timelineDataList,v.getContext());
                    RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                            new RecyclerViewPreloader<>(
                                    Glide.with(v.getContext()), modelProvider, sizeProvider,3);
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            rView.addOnScrollListener(preloader);
                        }
                    });

                }
            });

        }

       else if(what.equals("show_post")){
             swipeRefreshLayout.setEnabled(false);
             heading.setText("Posts");
            //Getting Post Data
           Needle.onBackgroundThread().execute(new Runnable() {
               @Override
               public void run() {
                   Gson gson=new Gson();
                   Type type = new TypeToken<List<TimelineData>>() {}.getType();
                   int pos=getArguments().getInt("position");
                   timelineDataList = gson.fromJson(getArguments().getString("timelineData"), type);

                   getArguments().remove("timelineData");


                   //Downloading Images in advance
                   /*ListPreloader.PreloadSizeProvider sizeProvider =
                           new FixedPreloadSizeProvider(600, 600);
                   ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(timelineDataList,v.getContext());
                   RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                           new RecyclerViewPreloader<>(
                                   Glide.with(v.getContext()), modelProvider, sizeProvider,15);
                   rView.addOnScrollListener(preloader);*/

                   Needle.onMainThread().execute(new Runnable() {
                       @Override
                       public void run() {
                           SpeedyLinearLayoutManager layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);

                           DisplayMetrics displayMetrics = new DisplayMetrics();
                           getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                           int height = displayMetrics.heightPixels;
                           layoutManager.setExtraLayoutSpace(height);
                          // layoutManager.setItemPrefetchEnabled(true);
                          // layoutManager.setInitialPrefetchItemCount(3);
                          // layoutManager.setItemPrefetchEnabled(true);
                           //layoutManager.setInitialPrefetchItemCount(2);
                           rView.setItemViewCacheSize(10);
                           rView.setDrawingCacheEnabled(true);
                           rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                           rView.setLayoutManager(layoutManager);

                           //Downloading Images in advance
                   ListPreloader.PreloadSizeProvider sizeProvider =
                           new FixedPreloadSizeProvider(600, 600);
                   ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(timelineDataList,v.getContext());
                   RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                           new RecyclerViewPreloader<>(
                                   Glide.with(v.getContext()), modelProvider, sizeProvider,3);
                   rView.addOnScrollListener(preloader);


                           //Setting List Adapter
                           adapteru=new CustomRecyclerViewAdapter(timelineDataList);
                           rView.setAdapter(adapteru);
                           rView.scrollToPosition(pos);
                       }
                   });


               }
           });


        }
        else if(what.equals("show_notification_post")){
            heading.setText("Posts");

            SpeedyLinearLayoutManager layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);
            //Getting Device Height
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;


            layoutManager.setItemPrefetchEnabled(true);
            rView.setItemViewCacheSize(1);
            rView.setDrawingCacheEnabled(true);
            rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            rView.setLayoutManager(layoutManager);

            //Setting Adapter
            if(getArguments().containsKey("notif_commenter_username")) {
                if(getArguments().containsKey("fromComntReport"))
                    adapteru = new CustomRecyclerViewAdapter(timelineDataList, getArguments().getString("notif_commenter_username"),
                            getArguments().getString("notif_comment_time"),"yes");
                else
                adapteru = new CustomRecyclerViewAdapter(timelineDataList, getArguments().getString("notif_commenter_username"),
                        getArguments().getString("notif_comment_time"),null);
            }
            else if(getArguments().containsKey("fromPostReport")){
                adapteru=new CustomRecyclerViewAdapter(timelineDataList,"yes");
            }
            else  adapteru=new CustomRecyclerViewAdapter(timelineDataList);

            rView.setAdapter(adapteru);
        }
        else if(what.equals("starred")) {
            heading.setText("Starred");

            //Grid Properties
            gridadp=new StarredAdapter(timelineDataList);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(v.getContext(),3);

            rView.setLayoutManager(gridLayoutManager);
            rView.setAdapter(gridadp);

          Needle.onBackgroundThread().execute(new Runnable() {
               @Override
               public void run() {
                   //Downloading Images in advance
                   ListPreloader.PreloadSizeProvider sizeProvider =
                           new FixedPreloadSizeProvider(200, 200);
                   ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(timelineDataList,v.getContext());
                   RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                           new RecyclerViewPreloader<>(
                                   Glide.with(v.getContext()), modelProvider, sizeProvider,3);
                   Needle.onMainThread().execute(new Runnable() {
                       @Override
                       public void run() {
                           rView.addOnScrollListener(preloader);
                       }
                   });

               }
           });

        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getArguments().containsKey("go_home")){
                    Intent i=new Intent(v.getContext(), Home_Screen.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
               else getActivity().finish();
            }
        });
        
        //Society Rview
        soc_rView.addOnItemTouchListener(
                new RecyclerItemClickListener(v.getContext(), soc_rView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                        //Setting autoscroll on click
                        LinearLayoutManager layoutManager=((LinearLayoutManager)soc_rView.getLayoutManager());
                        int avg =
                                (layoutManager.findFirstCompletelyVisibleItemPosition()+(layoutManager.findFirstCompletelyVisibleItemPosition()+1)+
                                        layoutManager.findLastCompletelyVisibleItemPosition())/3;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(position>avg) soc_rView.getLayoutManager().smoothScrollToPosition(soc_rView, null, position+1);
                                else if(position!=0) soc_rView.getLayoutManager().smoothScrollToPosition(soc_rView, null, position-1);
                                else soc_rView.getLayoutManager().smoothScrollToPosition(soc_rView, null, 0);
                            }
                        },200);


                       //Clearing Data of Rview for adding new Data
                        timelineDataList.clear();
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

                            obj.put(what+"_timeline_society",society_name.toLowerCase());
                            socket.emit("data",obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
                        swipeRefreshLayout.post(new Runnable() {
                            @Override public void run() {
                                swipeRefreshLayout.setRefreshing(true);
                                // directly call onRefresh() method
                                swipeRefreshListner.onRefresh();

                                if(!swipeRefreshLayout.isRefreshing()) {
                                    JSONObject obj=new JSONObject();
                                    try {

                                        obj.put(what+"_timeline_society",society_name.toLowerCase());
                                        socket.emit("data",obj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        //Showing Loading
                        loading_view.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loading_view.setVisibility(View.GONE);
                            }
                        },750);

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                }));

        //Swipe Detection
       /* swipe = new Swipe();
        swipe.setListener(new SwipeListener() {
            @Override public void onSwipingLeft(final MotionEvent event) {
            }

            @Override public void onSwipedLeft(final MotionEvent event) {
            }

            @Override public void onSwipingRight(final MotionEvent event) {
            }

            @Override public void onSwipedRight(final MotionEvent event) {
            }

            @Override public void onSwipingUp(final MotionEvent event) {
                headingView.setVisibility(View.GONE);
            }

            @Override public void onSwipedUp(final MotionEvent event) {

            }


            @Override public void onSwipingDown(final MotionEvent event) {
               headingView.setVisibility(View.VISIBLE);

            }

            @Override public void onSwipedDown(final MotionEvent event) {

            }
        });*/



        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                    if(getArguments().containsKey("go_home")){
                        Intent i=new Intent(v.getContext(), Home_Screen.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }

                    else{
                        getActivity().finish();
                    }
                    return true;
                }
                return false;
            }
        } );









        return v;
    }
    private void addTimelineData(String username,String fullname,String profPic,String time,String img_link,String caption,
                                 String private_post_stat,String comment_disabled,String share_disabled,String download_disabled,String likes_counter,
                                 String comments_counter,String short_book_content,String society_name_adp){
        boolean isRepeated = false;
        for(TimelineData data:timelineDataList){
            if(data.getTime().equals(time)){
                isRepeated = true;
            }
        }
        if(!isRepeated){
            timelineDataList.add(new TimelineData(username,fullname,profPic,time,img_link,caption,private_post_stat,comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp));
            if(what.equals("tags")||what.equals("show_notification_post")||what.equals("tips")||what.equals("opportunities")) {
                adapteru.notifyDataSetChanged();
                rView.scrollToPosition(adapteru.getItemCount()-1);
                rView.scrollToPosition(0);
            }

        }

       swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
           public void onRefresh() {
               updateData();
           }
       });

    }
    void showData(List<TimelineData> timelineDataList){
        adapteru = new CustomRecyclerViewAdapter(timelineDataList);
        rView.setLayoutManager(null);
        rView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        rView.setAdapter(null);
        rView.setLayoutManager(layoutManager);
        rView.setAdapter(adapteru);
        //assumed you attached your layout manager earlier
    }
    @Override
    public void onStart() {
        super.onStart();
                socket.disconnect();
                socket.connect();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (what.equals("tips") || what.equals("opportunities")) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("get_total_society_list", "yes");
                        socket.emit("data", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        },150);

                if(what.equals("tags")){

                    String tag_searched= getArguments().getString("tag_searched");
                    JSONObject ob=new JSONObject();
                    try {
                        ob.put("tag_searched",tag_searched);
                        ob.put("tag_search_username", my_username);
                        socket.emit("data",ob);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(what.equals("show_notification_post")){

                    String post_owner_username= getArguments().getString("post_owner_username");
                    String post_owner_time= getArguments().getString("post_owner_time");
                    JSONObject ob=new JSONObject();
                    try {
                        ob.put("notification_post_owner_username",post_owner_username);
                        ob.put("notification_post_owner_time",post_owner_time);
                        socket.emit("data",ob);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(what.equals("starred")){
                    JSONObject ob=new JSONObject();
                    try {
                        ob.put("starred_post_username",my_username);
                        socket.emit("data",ob);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

    }
    private void updateData(){
        swipeRefreshLayout.setRefreshing(true);
        if (what.equals("tips") || what.equals("opportunities")) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("get_total_society_list", "yes");
                socket.emit("data", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Getting Data from server
                    JSONObject obj=new JSONObject();
                    try {

                        obj.put(what+"_timeline_society",society_name.toLowerCase());
                        socket.emit("data",obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },150);
        }
        if(what.equals("tags")){

            String tag_searched= getArguments().getString("tag_searched");
            JSONObject ob=new JSONObject();
            try {
                ob.put("tag_searched",tag_searched);
                ob.put("tag_search_username", my_username);
                socket.emit("data",ob);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(what.equals("show_notification_post")){

            String post_owner_username= getArguments().getString("post_owner_username");
            String post_owner_time= getArguments().getString("post_owner_time");
            JSONObject ob=new JSONObject();
            try {
                ob.put("notification_post_owner_username",post_owner_username);
                ob.put("notification_post_owner_time",post_owner_time);
                socket.emit("data",ob);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(what.equals("starred")){
            //((SimpleItemAnimator) rView.getItemAnimator()).setSupportsChangeAnimations(false);
            JSONObject ob=new JSONObject();
            try {
                ob.put("starred_post_username",my_username);
                socket.emit("data",ob);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                    //   Toast.makeText(v.getContext(),"Sorry couldn't fetch data . Please try again",Toast.LENGTH_LONG).show();
                }
            }
        },15000);
    }


    SwipeRefreshLayout.OnRefreshListener swipeRefreshListner = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            updateData();
        }
    };

    private void checking(){

        if(society_namArr.size()>0){
            if(!what.equals("tips")||!what.equals("opportunities"))society_namArr.add(0,"Home");
            adapter=new TimeLineSocietyAdapter(society_namArr,society_name,false);
            soc_rView.setAdapter(adapter);
        }

    }

    private  Emitter.Listener handlePosts = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            try {
                JSONArray jsonArray=(JSONArray)args[0];
                Needle.onMainThread().execute(() -> {
                    timelineDataList.clear();
                    swipeRefreshLayout.setRefreshing(false);
                    if(jsonArray.length()>0) noResult.setVisibility(View.GONE);
                    else noResult.setVisibility(View.VISIBLE);
                    for(int i=0;i<jsonArray.length();i++){
                        try {
                            JSONArray arr=jsonArray.getJSONArray(i);
                            JSONObject ob=arr.getJSONObject(0);

                            post_username=ob.getString("_pid");
                            post_fullname=ob.getString("owner_fullname");
                            if(ob.has("owner_profPic"))postProfPic=ob.getString("owner_profPic");
                            else postProfPic="";

                            post_time=ob.getString("time");

                            post_link=ob.getString("img_link");
                            likes_counter=ob.getString("likes_counter");
                            comments_counter=ob.getString("comments_counter");
                            if(ob.has("caption")) post_caption=ob.getString("caption");
                            else post_caption=null;

                            //Skipping Private Posts
                            if(ob.getString("private_post_stat").equals("yes")&&!post_username.equals(my_username)) {
                                if(what.equals("show_notification_post")) {
                                    noResult.setText("Sorry you can't see this post because this is private now.");
                                    noResult.setVisibility(View.VISIBLE);
                                }
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

                            addTimelineData(post_username,post_fullname,postProfPic,post_time,post_link,post_caption,
                                    private_post_stat,comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(what.equals("starred")) {
                        gridadp.notifyDataSetChanged();
                        rView.scrollToPosition(gridadp.getItemCount()-1);
                        rView.scrollToPosition(0);
                    }


                });

            } catch (Exception e) {
                Log.e("error",e.toString());
            }
        }
    };
    private  Emitter.Listener handleTipsPosts = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            try {
                JSONArray jsonArray=(JSONArray)args[0];
                Needle.onMainThread().execute(() -> {
                    //Toast.makeText(v.getContext(),jsonArray.toString(),Toast.LENGTH_LONG).show();
                    timelineDataList.clear();
                    rView.removeAllViewsInLayout();
                    swipeRefreshLayout.setRefreshing(false);
                    if(jsonArray.length()>0) noResult.setVisibility(View.GONE);
                    else noResult.setVisibility(View.VISIBLE);
                    for(int i=0;i<jsonArray.length();i++){
                        try {
                           // JSONArray arr=jsonArray.getJSONArray(i);
                            JSONObject ob=jsonArray.getJSONObject(i);

                            post_username=ob.getString("_pid");
                            post_fullname=ob.getString("owner_fullname");
                            if(ob.has("owner_profPic"))postProfPic=ob.getString("owner_profPic");
                            else postProfPic="";

                            post_time=ob.getString("time");

                            post_link=ob.getString("img_link");
                            likes_counter=ob.getString("likes_counter");
                            comments_counter=ob.getString("comments_counter");
                            if(ob.has("caption")) post_caption=ob.getString("caption");
                            else post_caption=null;

                            //Skipping Private Posts
                            if(ob.getString("private_post_stat").equals("yes")&&!post_username.equals(my_username)) {
                                if(what.equals("show_notification_post")) {
                                    noResult.setText("Sorry you can't see this post because this is private now.");
                                    noResult.setVisibility(View.VISIBLE);
                                }
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

                            addTimelineData(post_username,post_fullname,postProfPic,post_time,post_link,post_caption,
                                    private_post_stat,comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                   /* RecyclerView.Adapter adapter=rView.getAdapter();
                    rView.setAdapter(null);
                    rView.setAdapter(adapter);*/



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
                            if(jsonArray!=null&&jsonArray.length()==0) soc_rView.setVisibility(View.GONE);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socket.disconnect();
    }
}
