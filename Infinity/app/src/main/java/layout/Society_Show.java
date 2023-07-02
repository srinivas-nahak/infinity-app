package layout;



import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
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
import com.github.pwittchen.swipe.library.Swipe;
import com.github.pwittchen.swipe.library.SwipeListener;
import com.orhanobut.hawk.Hawk;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import community.infinity.CurrentSociety;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.Home_Screen;
import community.infinity.adapters.AccountSettingsAdapter;
import community.infinity.adapters.CustomRecyclerViewAdapter;
import community.infinity.activities.ProfileHolder;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.RecyclerViewItems.SpeedyLinearLayoutManager;
import community.infinity.adapters.MyPreloadModelProvider;
import community.infinity.adapters.TimeLineSocietyAdapter;
import community.infinity.adapters.TimelineData;
import custom_views_and_styles.CustomRecyclerView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;
import ooo.oxo.library.widget.TouchImageView;


public class Society_Show extends Fragment implements View.OnClickListener {


    private CustomRecyclerView posts_rView,society_rView;
    private RecyclerView.Adapter adapter;
    private FloatingActionButton plus;
    private Animation an;
    public static Swipe swipe;
    private ArrayList<String> society_namArr=new ArrayList<>();
    private RelativeLayout welcome_msg_container;
    private FrameLayout selected_society;
    private int selected_soc_pos;
    private CustomRecyclerViewAdapter adapteru;
    private SpeedyLinearLayoutManager layoutManager;
    private String my_username;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog dialog;
    private View v;
    private List<TimelineData> timelineDataList=new ArrayList<>() ;


    private String post_username,post_fullname,postProfPic,post_time,post_link,post_caption,private_post_stat,
            comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp;

    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public Society_Show() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("timeline_data",handlePosts);
        socket.on("society_list",handleSocietyList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.society_show,null, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        this.v=v;
        plus=getActivity().findViewById(R.id.plus);
        selected_society=getActivity().findViewById(R.id.selected_society_layout);
        society_rView=getActivity().findViewById(R.id.selected_society_home);
        posts_rView =  v.findViewById(R.id.featured_recycler_view_society);
        final ViewPager parentViewPg=getActivity().findViewById(R.id.pager);
        final FrameLayout loadingView=getActivity().findViewById(R.id.loadingView);
        welcome_msg_container=getActivity().findViewById(R.id.welcomeMsgCont);


        swipeRefreshLayout=v.findViewById(R.id.swipe_container);
       // final FrameLayout parentFrameLay=getActivity().findViewById(R.id.toolBoxHomeScreen);
        


        //Building Hawk
        Hawk.init(v.getContext()).build();


        //Getting Username
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if(sharedPrefs.contains("username"))my_username=sharedPrefs.getString("username",null);
        else my_username=Hawk.get("myUserName");







        /////

        //Animation
        an= AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons);

        //Featured RecyclerView
        //selected=new ArrayList<>();
        layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);
        ((SimpleItemAnimator) posts_rView.getItemAnimator()).setSupportsChangeAnimations(false);
        //Getting Device Height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        //layoutManager.setExtraLayoutSpace(height);
        //posts_rView.setHasFixedSize(true);
        layoutManager.setItemPrefetchEnabled(true);
        layoutManager.setInitialPrefetchItemCount(2);
        posts_rView.setItemViewCacheSize(10);
        posts_rView.setDrawingCacheEnabled(true);
        posts_rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        posts_rView.setLayoutManager(layoutManager);
        //posts_rView.setNestedScrollingEnabled(false);

      //  posts_rView.scrollToPosition(0);

        //posts_rView.setLayoutManager(new GridLayoutManager(v.getContext(), 3));
        //Setting Adapter
        adapteru=new CustomRecyclerViewAdapter(timelineDataList);
        posts_rView.setAdapter(adapteru);

        //Downloading Images in advance
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                ListPreloader.PreloadSizeProvider sizeProvider =
                        new FixedPreloadSizeProvider(600, 600);
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





        //Society RecyclerView
        society_rView.setHasFixedSize(true);
        society_rView.setNestedScrollingEnabled(false);
        SpeedyLinearLayoutManager horz_layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.HORIZONTAL,false);
        society_rView.setLayoutManager(horz_layoutManager);



        ///Setting SwipeRefreshLay
       // swipeRefreshLayout.setOnRefreshListener(this);
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
                        int avg =
                                (horz_layoutManager.findFirstCompletelyVisibleItemPosition()+(horz_layoutManager.findFirstCompletelyVisibleItemPosition()+1)+
                                        horz_layoutManager.findLastCompletelyVisibleItemPosition())/3;


                        if(position>avg) society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, position+1);
                        else if(position!=0) society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, position-1);
                        else society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, 0);



                        //Clearing Data for adding new data
                        timelineDataList.clear();
                        //posts_rView.getRecycledViewPool().clear();
                        adapteru.notifyDataSetChanged();
                       // posts_rView.removeAllViewsInLayout();
                        //showData(timelineDataList);


                        //Capsule Color Change and Animation
                        view.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons));

                        FrameLayout msgContainer=view.findViewById(R.id.msgContainer);
                        TextView soc_names=view.findViewById(R.id.timeline_soc_names);
                        for(FrameLayout msgContain: TimeLineSocietyAdapter.msgContainerList){
                            msgContain.setBackgroundResource(R.drawable.unselected_soc);
                        }

                        msgContainer.setBackgroundResource(R.drawable.textinputborder);
                        soc_names.setTextColor(Color.parseColor("#001919"));

                        CurrentSociety.home_society = society_namArr.get(position);




                        //Getting Data from server
                        /*JSONObject obj=new JSONObject();
                        try {

                            obj.put("timeline_username",my_username);
                            obj.put("timeline_posts","all");
                            obj.put("timeline_society_name",CurrentSociety.home_society.toLowerCase());
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
                                    JSONObject obj = new JSONObject();
                                    try {

                                        obj.put("timeline_username", my_username);
                                        obj.put("timeline_posts", "all");
                                        obj.put("timeline_society_name", CurrentSociety.home_society.toLowerCase());
                                        socket.emit("data", obj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
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
                        },750);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                }));

        //Add Pics & Video Button
        plus.setOnClickListener(this);

        // Fetching data from server
        Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
            @Override
            public void run() {
                socket.disconnect();
                socket.connect();
                //Getting Data from server
                JSONObject obj=new JSONObject();
                try {
                    obj.put("timeline_username",my_username);
                    obj.put("timeline_posts","all");
                    if(CurrentSociety.home_society==null) obj.put("timeline_society_name","home");
                    else obj.put("timeline_society_name",CurrentSociety.home_society.toLowerCase());
                    socket.emit("data",obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               updateData();
            }
        });

        //Swipe Detection
        swipe = new Swipe();
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

            }

            @Override public void onSwipedUp(final MotionEvent event) {


                 new Handler().postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         if(Home_Screen.current_tab_index==0)plus.setVisibility(View.GONE);

                     }
                 },500);
            }


            @Override public void onSwipingDown(final MotionEvent event) {
                if(Home_Screen.current_tab_index==0) plus.setVisibility(View.VISIBLE);
            }

            @Override public void onSwipedDown(final MotionEvent event) {

            }
        });
    }

    void checking(){
        //Toast.makeText(v.getContext(),society_namArr.size(),Toast.LENGTH_SHORT).show();
        if(society_namArr.size()>0&!society_namArr.contains("null")){
            society_namArr.add(0,"Home");
            adapter=new TimeLineSocietyAdapter(society_namArr,CurrentSociety.home_society,false);
            society_rView.setAdapter(adapter);
            society_rView.scrollToPosition(selected_soc_pos);
        }
        else{
            Needle.onBackgroundThread().execute(new Runnable() {
                @Override
                public void run() {
                    Hawk.put("society_name","home");
                }
            });

          //  society_rView.setVisibility(View.GONE);
            selected_society.setVisibility(View.GONE);
        }
    }

    void addTimelineData(String username,String fullname,String postProfPic,String time,String img_link,String caption,String private_stat,
                         String comment_disabled,String share_disabled,String download_disabled,String likes_counter,String comments_counter,String short_book_content,String society_name_adp){


            boolean isRepeated = false;
            for (TimelineData data:timelineDataList) {

              //  TimelineData data=timelineDataList.get(i);
                if (data.getTime().equals(time)) {
                    isRepeated = true;
                }
            }
            if (!isRepeated) {
                timelineDataList.add(new TimelineData(username,fullname,postProfPic, time, img_link, caption, private_stat, comment_disabled, share_disabled,download_disabled, likes_counter, comments_counter,short_book_content,society_name_adp));
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        adapteru.notifyDataSetChanged();
                        //posts_rView.scrollToPosition(adapteru.getItemCount()/2);
                        //posts_rView.scrollToPosition(0);
                    }
                });
            }

        }


    @Override
    public void onClick(View v) {
        an= AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons);
        switch (v.getId()){
            case R.id.plus:
                plus.startAnimation(an);
                //Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                final View vi = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_image, null);
                RelativeLayout mainCont=vi.findViewById(R.id.dialogImgRelLay);
                RecyclerView fontList=vi.findViewById(R.id.fontRecycler);
                TouchImageView img=vi.findViewById(R.id.dialog_image);
                img.setVisibility(View.GONE);
                fontList.setVisibility(View.VISIBLE);

                //Setting Wrap Content
                RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                mainCont.setLayoutParams(params);
                mainCont.setBackgroundResource(R.drawable.bg_gradient);


                final ArrayList<String> name=new ArrayList<>();
                //name.add("Report this");
                //name.add("Add Pictures");

                if(CurrentSociety.home_society.equals("Writing")){
                    name.add("Add Writings");
                    name.add("Add Short Book");
                    AccountSettingsAdapter adapter=new AccountSettingsAdapter(name,null,null,"triple_dot",null);
                    fontList.setHasFixedSize(true);
                    fontList.setLayoutManager(new LinearLayoutManager(v.getContext()));
                    fontList.setItemViewCacheSize(30);
                    fontList.setDrawingCacheEnabled(true);
                    fontList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    fontList.setAdapter(adapter);
                    fontList.addOnItemTouchListener(
                            new RecyclerItemClickListener(v.getContext(), fontList ,new RecyclerItemClickListener.OnItemClickListener() {
                                @Override public void onItemClick(View view, int position) {
                                    if(name.get(position).equals("Add Writings")){
                                        Intent i=new Intent(v.getContext(),ProfileHolder.class);
                                        i.putExtra("Open","compose");
                                        Needle.onBackgroundThread().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                Hawk.put("bookTitle","no");
                                            }
                                        });
                                        startActivity(i);
                                    }
                                    if(name.get(position).equals("Add Short Book")){
                                        Intent i=new Intent(v.getContext(),ProfileHolder.class);
                                        i.putExtra("Open","compose");
                                        Needle.onBackgroundThread().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                Hawk.put("bookTitle","yes");
                                            }
                                        });

                                        startActivity(i);
                                    }

                                    dialog.dismiss();

                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                            }));
                    //Dialog Properties////////
                    builder.setView(vi);
                    dialog = builder.create();
                    dialog.show();
                    dialog.setOnDismissListener(
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ViewGroup v = (ViewGroup) vi.getParent();
                                    v.removeAllViews();
                                }
                            }
                    );
                    dialog.getWindow().setDimAmount(0.3f);
                    // dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    /////////////////////////
                    v.performLongClick();
                }
                else if(my_username.equals("infinity")){
                    name.add("Add Writings");
                    name.add("Add Short Book");
                    name.add("Add Pictures");
                    AccountSettingsAdapter adapter=new AccountSettingsAdapter(name,null,null,"triple_dot",null);
                    fontList.setHasFixedSize(true);
                    fontList.setLayoutManager(new LinearLayoutManager(v.getContext()));
                    fontList.setItemViewCacheSize(30);
                    fontList.setDrawingCacheEnabled(true);
                    fontList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    fontList.setAdapter(adapter);
                    fontList.addOnItemTouchListener(
                            new RecyclerItemClickListener(v.getContext(), fontList ,new RecyclerItemClickListener.OnItemClickListener() {
                                @Override public void onItemClick(View view, int position) {
                                    if(name.get(position).equals("Add Writings")){
                                        Intent i=new Intent(v.getContext(),ProfileHolder.class);
                                        i.putExtra("Open","compose");
                                        Needle.onBackgroundThread().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                Hawk.put("bookTitle","no");
                                            }
                                        });
                                        startActivity(i);
                                    }
                                    else if(name.get(position).equals("Add Short Book")){
                                        Intent i=new Intent(v.getContext(),ProfileHolder.class);
                                        i.putExtra("Open","compose");
                                        Needle.onBackgroundThread().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                Hawk.put("bookTitle","yes");
                                            }
                                        });

                                        startActivity(i);
                                    }
                                    else{
                                        Needle.onBackgroundThread().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Hawk.put("what","timeline");
                                                Hawk.put("bookTitle","no");
                                            }
                                        });
                                        Intent i=new Intent(v.getContext(),ProfileHolder.class);
                                        Bundle b=new Bundle();
                                        b.putString("what","timeline");
                                        i.putExtras(b);
                                        i.putExtra("Open","chooser");
                                        startActivity(i);
                                    }

                                    dialog.dismiss();

                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                            }));
                    //Dialog Properties////////
                    builder.setView(vi);
                    dialog = builder.create();
                    dialog.show();
                    dialog.setOnDismissListener(
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ViewGroup v = (ViewGroup) vi.getParent();
                                    v.removeAllViews();
                                }
                            }
                    );
                    dialog.getWindow().setDimAmount(0.3f);
                    // dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    /////////////////////////
                    v.performLongClick();
                }
                else{
                    Needle.onBackgroundThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            // Hawk.put("what","timeline");
                            Hawk.put("bookTitle","no");
                        }
                    });
                    Intent i=new Intent(v.getContext(),ProfileHolder.class);
                    Bundle b=new Bundle();
                    b.putString("what","timeline");
                    i.putExtras(b);
                    i.putExtra("Open","chooser");
                    startActivity(i);
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //Making Plus Visible
        plus.setVisibility(View.VISIBLE);



        new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Needle.onBackgroundThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj=new JSONObject();
                                try {
                                    if(my_username!=null) {
                                        obj.put("get_society_list_username", my_username);
                                        socket.emit("data", obj);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                },200);

    }
    /*@Override
    public void onRefresh() {
        // Fetching data from server


                //Getting Data from server According to Society
                JSONObject obj=new JSONObject();
                try {
                    obj.put("timeline_username",my_username);
                    obj.put("timeline_posts","all");
                    obj.put("timeline_society_name",CurrentSociety.home_society.toLowerCase());
                    socket.emit("data",obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject obj=new JSONObject();
                try {
                    obj.put("get_society_list_username",my_username);
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
        },15000);

    }*/
    void showData(List<TimelineData> timelineDataList){
        adapteru = new CustomRecyclerViewAdapter(timelineDataList);
        posts_rView.setLayoutManager(null);
        posts_rView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        posts_rView.setAdapter(null);
        posts_rView.setLayoutManager(layoutManager);
        posts_rView.setAdapter(adapteru);
        //assumed you attached your layout manager earlier
    }
    private void updateData(){
        // Fetching data from server


        //Getting Data from server According to Society
        JSONObject obj=new JSONObject();
        try {
            obj.put("timeline_username",my_username);
            obj.put("timeline_posts","all");
            obj.put("timeline_society_name",CurrentSociety.home_society.toLowerCase());
            socket.emit("data",obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject obj=new JSONObject();
                try {
                    obj.put("get_society_list_username",my_username);
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
        },15000);
    }
    SwipeRefreshLayout.OnRefreshListener swipeRefreshListner = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
           updateData();
        }
    };
    private  Emitter.Listener handlePosts = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            try {
                JSONArray jsonArray=(JSONArray)args[0];


                  //Clearing whole data in order to show new data in a proper sequence
                   //timelineDataList.clear();

                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        if(jsonArray==null||jsonArray.length()==0) welcome_msg_container.setVisibility(View.VISIBLE);
                        else welcome_msg_container.setVisibility(View.GONE);

                   timelineDataList.clear();


                   for(int i=0;i<jsonArray.length();i++){
                       try {
                           JSONObject ob=jsonArray.getJSONObject(i);


                           post_username=ob.getString("_pid");
                           post_fullname=ob.getString("owner_fullname");
                           if(ob.has("owner_profPic"))postProfPic=ob.getString("owner_profPic");
                           else postProfPic="";

                           //Skipping Private Posts
                               if(ob.getString("private_post_stat").equals("yes")&& !post_username.equals(my_username)) continue;
                              else
                               private_post_stat=ob.getString("private_post_stat");

                           likes_counter=ob.getString("likes_counter");
                           comments_counter=ob.getString("comments_counter");
                           post_time=ob.getString("time");

                           post_link=ob.getString("img_link");

                           if(ob.has("caption")) post_caption=ob.getString("caption");
                           else post_caption=null;

                           comment_disabled=ob.getString("comment_disabled");


                           share_disabled=ob.getString("share_disabled");
                           download_disabled=ob.getString("download_disabled");
                           society_name_adp=ob.getString("society");

                           if(ob.has("short_book_content")) short_book_content=ob.getString("short_book_content");
                           else short_book_content=null;

                          addTimelineData(post_username,post_fullname,postProfPic,post_time,post_link,post_caption,private_post_stat,
                                  comment_disabled,share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp);

                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   }
                      /* Needle.onMainThread().execute(new Runnable() {
                           @Override
                           public void run() {
                               RecyclerView.Adapter adapter=posts_rView.getAdapter();
                               posts_rView.setAdapter(null);
                               posts_rView.setAdapter(adapter);
                           }
                       });*/

                   //adapteru.notifyDataSetChanged();


                    }
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
                            if(jsonArray==null||jsonArray.length()==0) selected_society.setVisibility(View.GONE);

                            //Getting Societylist
                            JSONObject ob= (JSONObject) jsonArray.get(0);
                            if(ob.has("society_list")) {
                                String s = ob.getString("society_list");
                                String[] splits = s.replace("[", "").replace("]", "").split(",");
                                ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(splits));

                                society_namArr = new ArrayList<>();
                                //society_namArr.clear();

                                for (int i = 0; i < arrayList.size(); i++) {
                                    society_namArr.add(arrayList.get(i).replace("\"", ""));
                                }
                                if (society_namArr.contains("null") || society_namArr.contains("") || society_namArr.size() == 0) {
                                    selected_society.setVisibility(View.GONE);
                                    society_namArr.clear();
                                } else checking();
                            }
                            else{
                                selected_society.setVisibility(View.GONE);
                            }




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