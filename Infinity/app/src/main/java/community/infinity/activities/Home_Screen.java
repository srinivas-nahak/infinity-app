package community.infinity.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.florent37.tutoshowcase.TutoShowcase;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinsBeard;
import com.novoda.merlin.registerable.connection.Connectable;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import community.infinity.BottomFrags.BottomFrag;
import community.infinity.BottomFrags.FloatingSettings;
import community.infinity.ExceptionHandler.MyExceptionHandler;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.adapters.AccountSettingsAdapter;
import community.infinity.adapters.ThemeAdapter;
import community.infinity.network_related.SocketAddress;
import community.infinity.adapters.Pager;
import community.infinity.R;
import community.infinity.writing.RememberTextStyle;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.NewCordiLay;
import custom_views_and_styles.ReverseInterpolator;
import custom_views_and_styles.SimpleTouchListener;
import custom_views_and_styles.ToggleButton;
import de.hdodenhof.circleimageview.CircleImageView;
import fcm.Constants;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import layout.Society_Show;
import needle.Needle;

/**
 * Created by Srinu on 04-08-2017.
 */

public class Home_Screen extends AppCompatActivity implements TabLayout.OnTabSelectedListener,AAH_FabulousFragment.Callbacks, AAH_FabulousFragment.AnimationListener{

      {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private AlertDialog dialog;
    private TabLayout tabLayout;
    public static TabLayout dummyTabLay;
    private ViewPager viewPager;
    private NewCordiLay cordiLay;
    private float downX,downY;
    private FloatingActionButton fab2,add_msg_btn,theme_btn;
    public    FloatingActionButton plus;
    private FloatingSettings dialogFrag;
    private FrameLayout soc_rViewCont,loadingCont,revealItemCont;
    private RecyclerView soc_rView,soc_rViewInfinite;
    public static   int current_tab_index=0;
    private RelativeLayout welcome_msg_container,tabLayCont;

    private boolean bool=false;
    private Merlin merlin;
    public static boolean homeActive;
    private BroadcastReceiver mMessageReceiver = null;

    private ImageButton search_btn,reveal_btn;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Hawk.init(this).build();

        setContentView(R.layout.home_screen);


        mMessageReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        // Do something
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        showSnack(intent.getStringExtra("body"),intent.getStringExtra("action"),intent.getExtras());

                    }
                });
            }
        };
        //Setting Static Context

        //Internet Checking
        merlin = new Merlin.Builder().withConnectableCallbacks().build(this);

        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
                // Do something!
            }
        });


        //Socket Related
        socket.on("my_profile_info",handleUserData);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String username = sharedPref.getString("username", null);


        //Initialising Emoji Lib


        JSONObject obji=new JSONObject();
        try {
            if(username!=null) {
                obji.put("my_profile_info", username);
                socket.emit("data", obji);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //Society RView Cont
        soc_rViewCont=findViewById(R.id.selected_society_layout);
        tabLayCont=findViewById(R.id.tabLayCont);
        soc_rView=findViewById(R.id.selected_society_home);
        soc_rViewInfinite=findViewById(R.id.selected_society_infinite_timeline);
        loadingCont=findViewById(R.id.loadingView);
        revealItemCont=findViewById(R.id.revealItemCont);
        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);
        dummyTabLay=findViewById(R.id.dummyTabLayout);
        plus=findViewById(R.id.plus);
        fab2 = findViewById(R.id.fab2);
        theme_btn=findViewById(R.id.themeChngBtn);
        add_msg_btn=findViewById(R.id.add_msg_btn);
        search_btn=findViewById(R.id.searchIcon);
        reveal_btn=findViewById(R.id.revealBtn);
        welcome_msg_container=findViewById(R.id.welcomeMsgCont);
        cordiLay=findViewById(R.id.mainHomeLay);

        theme_btn.setBackgroundResource(R.drawable.theme1);

        if(RememberTextStyle.themeResource==0){
            if(sharedPref.contains("theme")&&sharedPref.getInt("theme",0)!=0)
                RememberTextStyle.themeResource=sharedPref.getInt("theme",0);
            else if(Hawk.contains("theme"))
                RememberTextStyle.themeResource=Hawk.get("theme");
            cordiLay.setBackgroundResource(RememberTextStyle.themeResource);
        }
        else{
            cordiLay.setBackgroundResource(RememberTextStyle.themeResource);
        }


        //Adding Exception Catcher
       // Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));


        loadingCont.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingCont.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                if(soc_rViewCont.getVisibility()==View.VISIBLE ){
                    SharedPreferences sh =PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if(!sh.contains("tutorial")) {
                        TutoShowcase.from(Home_Screen.this)
                                .setContentView(R.layout.tutorial_screen)

                                .on(soc_rViewCont.getId()) //a view in actionbar
                                .addRoundRect()
                                .onClick(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //custom action
                                    }
                                })

                                .on(R.id.infoTxtTutorial)
                                .displaySwipableLeft()
                                .show();
                        Needle.onBackgroundThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                Hawk.put("tutorial", "yes");
                            }
                        });
                        SharedPreferences.Editor editor = sh.edit();
                        editor.putString("tutorial", "yes");
                        editor.apply();
                    }
                }
            }
        },2500);





        //Tint to Btn
        ButtonTint tint=new ButtonTint("white");
        tint.setTint(search_btn);
        tint.setTint(reveal_btn);
        //Opening search
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomFrag f=new BottomFrag();
                Bundle b=new Bundle();
                b.putString("switch","search");
                f.setArguments(b);
                f.show(getFragmentManager(),"fra");
            }
        });

        //Revealing Menu
        reveal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(revealItemCont.getVisibility()==View.VISIBLE){
                    revealItemCont.setVisibility(View.GONE);
                    reveal_btn.setRotation(90);
                   CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                           CoordinatorLayout.LayoutParams.MATCH_PARENT,
                           CoordinatorLayout.LayoutParams.MATCH_PARENT
                   );
                   params.setMargins(0,getDp(45),0,0);
                   viewPager.setLayoutParams(params);
                }
                else {
                    revealItemCont.setVisibility(View.VISIBLE);
                   reveal_btn.setRotation(-90);
                   CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                           CoordinatorLayout.LayoutParams.MATCH_PARENT,
                           CoordinatorLayout.LayoutParams.MATCH_PARENT
                   );
                   params.setMargins(0,getDp(125),0,0);
                   viewPager.setLayoutParams(params);
                }
            }
        });

       /* reveal_btn.setOnTouchListener(new SimpleTouchListener() {

            @Override
            public void onDownTouchAction() {
                // do something when the View is touched down
                revealItemCont.setVisibility(View.VISIBLE);
                reveal_btn.setScaleY(-1);
            }


            @Override
            public void onUpTouchAction() {
                // do something when the down touch is released on the View

            }

            @Override
            public void onCancelTouchAction() {

                // do something when the down touch is canceled
                // (e.g. because the down touch moved outside the bounds of the View
            }
        });*/


        dialogFrag = FloatingSettings.newInstance();
        if(dialogFrag.isAdded())
        {
            return; //or return false/true, based on where you are calling from
        }
        dialogFrag.setParentFab(fab2);

        theme_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(fab2.getContext());
                final View vi = LayoutInflater.from(fab2.getContext()).inflate(R.layout.theme_choosing_lay, null);
                final ImageButton back_btn=vi.findViewById(R.id.back_of_theme);
                final DragToClose dragToClose=vi.findViewById(R.id.dragViewTheme);
                final RecyclerView themeRview=vi.findViewById(R.id.themeRView);

                ThemeAdapter adapter=new ThemeAdapter();
                GridLayoutManager manager=new GridLayoutManager(fab2.getContext(),3);
                themeRview.setHasFixedSize(true);
                themeRview.setNestedScrollingEnabled(false);
                themeRview.setLayoutManager(manager);
                themeRview.setAdapter(adapter);

                themeRview.addOnItemTouchListener(new RecyclerItemClickListener(fab2.getContext(), themeRview, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        ImageView tick=view.findViewById(R.id.tickTheme);
                        for(ImageView tick_img:ThemeAdapter.imageViewList){
                          tick_img.setVisibility(View.GONE);
                        }
                        tick.setVisibility(View.VISIBLE);

                        Animation  an= AnimationUtils.loadAnimation(fab2.getContext(),R.anim.fade_buttons);
                        view.startAnimation(an);

                        String s="theme"+(position+1);
                        int i=getResources().getIdentifier(s, "drawable", getPackageName());

                        dialog.getWindow().setBackgroundDrawableResource(i);
                        cordiLay.setBackgroundResource(i);
                        //Storing Themes in memory

                        Needle.onBackgroundThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                RememberTextStyle.themeResource=i;
                                SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor=sh.edit();
                                editor.putInt("theme",i);
                                editor.apply();
                                Hawk.put("theme",i);
                            }
                        });

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));


                //Loading Click Animation


                //Drag to Close view
                dragToClose.setDragListener(new DragListener() {
                    @Override
                    public void onStartDraggingView() {}

                    @Override
                    public void onViewCosed() {
                        dialog.dismiss();
                    }
                });




                back_btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            v.performClick();
                        }
                    }
                });
                back_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });


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
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.getWindow().setBackgroundDrawableResource(RememberTextStyle.themeResource);

                /////////////////////////
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFrag.show(getSupportFragmentManager(), dialogFrag.getTag());
            }
        });




        //Adding the tabs using addTab() method
        checking();
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        //Creating our pager adapter
        Pager adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount(),getApplicationContext());

        //Adding adapter to pager
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if(position!=0||position!=1) soc_rViewCont.setVisibility(View.GONE);
                if(position==0||position==1) soc_rViewCont.setVisibility(View.VISIBLE);

                if(position==0) {
                    soc_rView.setVisibility(View.VISIBLE);
                    soc_rViewInfinite.setVisibility(View.GONE);
                    plus.setVisibility(View.VISIBLE);
                }
                else {
                    welcome_msg_container.setVisibility(View.GONE);
                    soc_rView.setVisibility(View.GONE);
                    soc_rViewInfinite.setVisibility(View.VISIBLE);
                    plus.setVisibility(View.GONE);
                }
                if(tabLayout.getTabCount()>3&&position==2){
                    CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                            CoordinatorLayout.LayoutParams.MATCH_PARENT,
                            CoordinatorLayout.LayoutParams.MATCH_PARENT
                    );
                    params.setMargins(0,getDp(100),0,0);
                    viewPager.setLayoutParams(params);

                    add_msg_btn.setVisibility(View.VISIBLE);
                }
                else if(tabLayout.getTabCount()==3&&position==1){
                    add_msg_btn.setVisibility(View.VISIBLE);
                }
                else{
                    CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                            CoordinatorLayout.LayoutParams.MATCH_PARENT,
                            CoordinatorLayout.LayoutParams.MATCH_PARENT
                    );
                    params.setMargins(0,getDp(100),0,0);
                    viewPager.setLayoutParams(params);

                    add_msg_btn.setVisibility(View.GONE);
                }
                current_tab_index=position;

                loadingCont.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.INVISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      loadingCont.setVisibility(View.GONE);
                      viewPager.setVisibility(View.VISIBLE);
                    }
                },1000);

                viewPager.getAdapter().notifyDataSetChanged();

                viewPager.setCurrentItem(position);
               tabLayout.setScrollPosition(position,0f,true);
               //tabLayout.getTabAt(position).getIcon().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
                tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });


        //Fcm Notifications
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, importance);
            mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        //Opening msgFrag from pushNotif
        Bundle b=getIntent().getExtras();
        if(b!=null&&b.containsKey("Open")){
            if(b.getString("Open").equals("messaging")){
                tabLayout.getTabAt(2).select();
                BottomFrag f=new BottomFrag();
                Bundle dialogBind=new Bundle();
                dialogBind.putString("switch","messaging");
                dialogBind.putString("searchUsername",b.getString("msg_username"));
                f.setArguments(dialogBind);
                f.show(getFragmentManager(),"frag");
            }
            else if(b.getString("Open").equals("notifications")){
                tabLayout.getTabAt(3).select();
            }
        }



       SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sh.edit();

                if(sh.getString("notifType",null)!=null&&sh.getString("notifType",null).equals("msg")){

                    BottomFrag f=new BottomFrag();
                    Bundle dialogBind=new Bundle();
                    dialogBind.putString("switch","messaging");
                    dialogBind.putString("searchUsername",sh.getString("notifMsgUsername",null));
                    f.setArguments(dialogBind);
                    f.show(getFragmentManager(),"frag");

                    editor.remove("notifMsgUsername");
                    editor.putString("notifType","nothing");
                    editor.apply();

                }

        //MyNotificationManager.getInstance(this).displayNotification("Greetings", "Hello how are you?");
    }
        private void disallowTouch(ViewParent parent, boolean isDisallow) {
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(isDisallow);
            }
        }

    public void checking(){


        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.go_home));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.infinite_timeline_icon));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.msg_vector));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.notifications));
        tabLayout.getTabAt(0).getIcon().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(Color.parseColor("#8c8c8c"), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(Color.parseColor("#8c8c8c"), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(3).getIcon().setColorFilter(Color.parseColor("#8c8c8c"), PorterDuff.Mode.SRC_IN);

        dummyTabLay.addTab(dummyTabLay.newTab().setIcon(new ColorDrawable(Color.parseColor("#00ffffff"))));
        dummyTabLay.addTab(dummyTabLay.newTab().setIcon(new ColorDrawable(Color.parseColor("#00ffffff"))));
        dummyTabLay.addTab(dummyTabLay.newTab().setIcon(new ColorDrawable(Color.parseColor("#00ffffff"))));
        dummyTabLay.addTab(dummyTabLay.newTab().setIcon(new ColorDrawable(Color.parseColor("#00ffffff"))));

        LinearLayout tabStrip = ((LinearLayout)dummyTabLay.getChildAt(0));
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }


    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position=tab.getPosition();
        viewPager.getAdapter().notifyDataSetChanged();
        tab.getIcon().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        if(tab.getPosition()==3) {
            dummyTabLay.getTabAt(tab.getPosition()).setIcon(new ColorDrawable(Color.parseColor("#00ffffff")));
            if(tab.getPosition()==3) {
                Hawk.delete("normal_notif");
                SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor=sh.edit();
                editor.remove("normal_notif");
                editor.apply();

            }
        }
        viewPager.setCurrentItem(position);

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        tab.getIcon().setColorFilter(Color.parseColor("#8c8c8c"), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    //Showing Notification Snanck
    public void showSnack(String content,String action,Bundle b){
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sh.edit();
            if (content.contains(":")) {
                if (current_tab_index == 2) return;
                else {
                    Needle.onBackgroundThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            Hawk.put("msg_notif", "yes");
                            editor.putString("msg_notif", "yes");
                            editor.apply();
                        }
                    });
                    dummyTabLay.getTabAt(2).setIcon(R.drawable.indicator);
                }
            } else {
                if (current_tab_index == 3) return;
                else {
                    Needle.onBackgroundThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            Hawk.put("normal_notif", "yes");
                            editor.putString("normal_notif", "yes");
                            editor.apply();
                        }
                    });
                    dummyTabLay.getTabAt(3).setIcon(R.drawable.indicator);
                }
            }
            Snackbar snackbar = Snackbar.make(search_btn, content, Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            sbView.setClickable(true);
            sbView.setFocusable(true);
            sbView.setBackgroundColor(Color.parseColor("#ffffff"));
            TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.parseColor("#001919"));
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) sbView.getLayoutParams();
            params.gravity = Gravity.TOP;
            sbView.setLayoutParams(params);
            snackbar.show();

            sbView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent();
                    i.setAction(action);
                    i.putExtras(b);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            });

    }


    //Deleting Cache
    public   void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public   boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();
        homeActive=true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sh.contains("upload_time")){
            JSONObject ob=new JSONObject();
            try {
                ob.put("check_upload_time_username",sh.getString("username",null));
                ob.put("check_upload_time",sh.getString("upload_time",null));
                socket.emit("data",ob);
                socket.emit("check_upload_status", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                      socket.disconnect();
                      SharedPreferences.Editor editor=sh.edit();
                      editor.remove("upload_time");
                      editor.apply();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(Hawk.contains("msg_notif")&&Hawk.get("msg_notif").toString().equals("yes")) {
            if(AccountSettingsAdapter.seen_checker.size()==0){
                Hawk.delete("msg_notif");
            }
            else
            dummyTabLay.getTabAt(2).setIcon(R.drawable.indicator);
            //msgNotif=false;
        }
        else if(sh.contains("msg_notif")&&sh.getString("msg_notif",null).equals("yes")){
            if(AccountSettingsAdapter.seen_checker.size()==0){
                SharedPreferences.Editor editor=sh.edit();
                editor.remove("msg_notif");
                editor.apply();
            }
            else
            dummyTabLay.getTabAt(2).setIcon(R.drawable.indicator);
        }
        if(Hawk.contains("normal_notif")&&Hawk.get("normal_notif").toString().equals("yes")) {
            dummyTabLay.getTabAt(3).setIcon(R.drawable.indicator);
            //normalNotif=false;
        }
        else if(sh.contains("normal_notif")&&sh.getString("normal_notif",null).equals("yes")){
            dummyTabLay.getTabAt(3).setIcon(R.drawable.indicator);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        homeActive=false;
        deleteCache(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        merlin.bind();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_SNACKBAR"));

        MerlinsBeard merlinsBeard=MerlinsBeard.from(this);
        if(!merlinsBeard.isConnected()){
            Snackbar snackbar = Snackbar.make(search_btn, "You're not connected to internet .", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.parseColor("#43cea2"));
            View sbView = snackbar.getView();
            sbView.setBackgroundColor(ContextCompat.getColor(this , R.color.textColor));
            TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.parseColor("#001919"));
            snackbar.show();
        }
    }

    @Override
    protected void onPause() {
        merlin.unbind();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteCache(this);
    }
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        if(Society_Show.swipe!=null)Society_Show.swipe.dispatchTouchEvent(event);
//        if(InfiniteTimeline.swipe!=null) InfiniteTimeline.swipe.dispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }
    @Override
    public void onBackPressed() {
        viewPager.setCurrentItem(0);
        if (bool) {
            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        this.bool = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                bool=false;
            }
        }, 2000);
    }
    private  Emitter.Listener handleUserData = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            //
                    JSONArray dati=(JSONArray) args[0];

                    try {
                        String bio=null;String prof_pic=null,wall_pic=null,society_list=null;
                        JSONObject ob=dati.getJSONObject(0);
                        final  String st=ob.getString("_id");
                        final  String tt=ob.getString("fullname");
                        if(ob.has("profile_pic"))  prof_pic=ob.getString("profile_pic");
                        if(ob.has("wall_pic"))  wall_pic=ob.getString("wall_pic");
                        if(ob.has("bio")) bio=ob.getString("bio");
                        if(ob.has("society_list")) society_list=ob.getString("society_list");
                        final  String uu=ob.getString("email");


                        //Saving in sharedPref for backup

                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor edit=sh.edit();

                        Hawk.delete("myUserName");
                        Hawk.put("myUserName",st);
                        edit.putString("username",st);

                        Hawk.put("myEmail",uu);
                        edit.putString("myEmail",uu);

                        Hawk.put("myFullName",tt);
                        edit.putString("myFullName",tt);

                        if(bio!=null) {
                            Hawk.put("myBio", bio);
                            edit.putString("myBio",bio);
                        }

                        if(society_list!=null) {
                            Hawk.put("mySocietyList", society_list);
                            edit.putString("mySocietyList",society_list);
                        }

                        if(wall_pic!=null) {
                            Hawk.put("myWallPic", wall_pic);
                            edit.putString("myWallPic",bio);
                        }

                        if(prof_pic!=null) {
                            Hawk.put("myProfilePic", prof_pic);
                            edit.putString("myProfilePic",prof_pic);
                        }
                        else{
                            Hawk.put("myProfilePic", "");
                            edit.putString("myProfilePic","");
                        }

                        edit.apply();

                        socket.disconnect();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


        }
    };


    //Fabulous Filter Methods
    @Override
    public void onOpenAnimationStart() {

    }

    @Override
    public void onOpenAnimationEnd() {

    }

    @Override
    public void onCloseAnimationStart() {

    }

    @Override
    public void onCloseAnimationEnd() {

    }

    @Override
    public void onResult(Object result) {

    }
    private int getDp(int px){
        float d = getResources().getDisplayMetrics().density;
        int dp= (int) (px * d);
        return dp;
    }

}
