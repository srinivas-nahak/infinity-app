package community.infinity.profile;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import community.infinity.BottomFrags.BottomFrag;
import community.infinity.ListItem;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.RecyclerViewItems.SpeedyLinearLayoutManager;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.Home_Screen;
import community.infinity.activities.ProfileHolder;
import community.infinity.adapters.AccountSettingsAdapter;
import community.infinity.adapters.MyPreloadModelProvider;
import community.infinity.adapters.StarredAdapter;
import community.infinity.adapters.TimeLineSocietyAdapter;
import community.infinity.adapters.TimelineData;
import community.infinity.writing.RememberTextStyle;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.CustomRecyclerView;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import multipleimageselect.helpers.Constants;
import multipleimageselect.models.Image;
import needle.Needle;
import ooo.oxo.library.widget.TouchImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Srinu on 12-12-2017.
 */

public class SearchProfile extends Fragment {

    private Button follow,followed,message,block_btn;
    private CustomRecyclerView society_rView,picRView;
    private TextView fullname_txt,bio,following_num,followers_num,post_txt,post_counter;
    private CircleImageView profile_pic;
    private Animation an;
    private  View v;
    private LinearLayout following_lay,followers_lay;
    private FrameLayout privacyIndicator,loading_view;
    private ListItem item;//it's used
    private Bundle dialogBind=new Bundle();
    private AlertDialog dialog;
    private String search_username,my_username;
    private ImageButton editWall,back;
    private KenBurnsView kenBurnsView;
    private List<TimelineData> timelineDataList=new ArrayList<>();
    private StarredAdapter gridadp;
    private GridLayoutManager gridLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AppCompatImageButton triple_dot;
    private String society_name="home",post_username,post_fullname,postProfPic,post_time,post_link,post_caption,private_post_stat,private_account_status,
            blocked_status,comment_disabled,share_disabled,download_disabled,comments_counter,likes_counter,short_book_content,society_name_adp;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("timeline_data",handlePosts);
    }

    public SearchProfile() {
    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.profile_search,container,false);

        //TypeCasting
        follow=v.findViewById(R.id.follow_btn);
        followed=v.findViewById(R.id.followed_btn);
        followers_lay=v.findViewById(R.id.followers_laySearch);
        following_lay=v.findViewById(R.id.following_laySearch);
        following_num=v.findViewById(R.id.following_numSearch);
        followers_num=v.findViewById(R.id.followers_numSearch);
        message=v.findViewById(R.id.message_btnSearch);
        picRView=v.findViewById(R.id.prof_pic_view);
        society_rView=v.findViewById(R.id.selected_societyProfile);
        fullname_txt=v.findViewById(R.id.full_name_profile_search);
        bio=v.findViewById(R.id.bioProfile);
        kenBurnsView=v.findViewById(R.id.movingViewSearch);
        profile_pic=v.findViewById(R.id.circleImageProfile);
        editWall=v.findViewById(R.id.edit_wall);
        privacyIndicator=v.findViewById(R.id.privateAccIndicator);
        triple_dot=v.findViewById(R.id.triple_dot_profile);
        block_btn=v.findViewById(R.id.block_btn);
        swipeRefreshLayout=v.findViewById(R.id.swipe_containerProfile);
        loading_view=v.findViewById(R.id.loadingViewProfile);
        post_txt=v.findViewById(R.id.postTxt);
        post_counter=v.findViewById(R.id.postCounterProfile);
        back=v.findViewById(R.id.back_button_ProfileSearch);
        final ScrollView parentLay=v.findViewById(R.id.scrollViewProfile);

        //Setting Bg
        loading_view.setBackgroundResource(RememberTextStyle.themeResource);
        parentLay.setBackgroundResource(RememberTextStyle.themeResource);




        ButtonTint white_tint=new ButtonTint("white");
        white_tint.setTint(back);
        white_tint.setTint(triple_dot);
        

        //Building Hawk
        Hawk.init(v.getContext()).build();


        //Making LoadingView visible first then RView
        loading_view.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.INVISIBLE);

        //Aligning textview
        bio.setGravity(Gravity.START);
        bio.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loading_view.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
            }
        },1000);


        //Deleting this key to avoid notif_indicator in home
        if(getArguments().containsKey("go_home")) Hawk.delete("normal_notif");

        //Making links of bio clickable
        bio.setMovementMethod(LinkMovementMethod.getInstance());




        //Animation
        an= AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons);

        //Getting usernames
        //Getting usernames
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        if (Hawk.get("myUserName") != null) my_username = Hawk.get("myUserName");
        else my_username = sharedPref.getString("username", null);

        if(getArguments().containsKey("searchUsername")) {
            search_username = getArguments().getString("searchUsername");
        }

        //Setting Typeface and Gradient Color of TxtView
        final Typeface font = Typeface.createFromAsset((fullname_txt.getContext()).getAssets(), "fonts/"+"Lato-Bold.ttf");
        fullname_txt.setTypeface(font);




        //Loading data with swipeRefreshLay
       /* swipeRefreshLayout.setColorSchemeColors(0,0,0,0);
        try {
            Field f =swipeRefreshLayout.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView img = (ImageView)f.get(swipeRefreshLayout);
            img.setBackgroundColor(Color.parseColor("#001919"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#ffffff"));*/



        //RecyclerView
        picRView.setNestedScrollingEnabled(false);
        picRView.setItemViewCacheSize(10);
        picRView.setDrawingCacheEnabled(true);
        picRView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


        //Setting Adapter
        gridadp=new StarredAdapter(timelineDataList);

        //Grid Propertis
        gridLayoutManager = new GridLayoutManager(v.getContext(),3);
        gridLayoutManager.setItemPrefetchEnabled(true);
        gridLayoutManager.setInitialPrefetchItemCount(2);
        picRView.setLayoutManager(gridLayoutManager);
        picRView.setAdapter(gridadp);

        //Society Rview
        society_rView.setHasFixedSize(true);
        society_rView.setNestedScrollingEnabled(false);
        SpeedyLinearLayoutManager.MILLISECONDS_PER_INCH=60f;
        SpeedyLinearLayoutManager layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.HORIZONTAL,false);
        society_rView.setLayoutManager(layoutManager);


        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                //Downloading Images in advance
                ListPreloader.PreloadSizeProvider sizeProvider =
                        new FixedPreloadSizeProvider(600 , 600);
                ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(timelineDataList,v.getContext());
                RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                        new RecyclerViewPreloader<>(
                                Glide.with(v.getContext()), modelProvider, sizeProvider, 20 );
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        picRView.addOnScrollListener(preloader);
                    }
                });

            }
        });




        
        //Onclick Methods

        //Society RView
        society_rView.addOnItemTouchListener(
                new RecyclerItemClickListener(v.getContext(), society_rView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                        //Setting autoscroll on click
                        int avg =
                                (layoutManager.findFirstCompletelyVisibleItemPosition()+(layoutManager.findFirstCompletelyVisibleItemPosition()+1)+
                                        layoutManager.findLastCompletelyVisibleItemPosition())/3;


                        if(position>avg) society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, position+1);
                        else if(position!=0) society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, position-1);
                        else society_rView.getLayoutManager().smoothScrollToPosition(society_rView, null, 0);



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

                        society_name=soc_names.getText().toString();



                        //Getting Data from server
                        JSONObject obj=new JSONObject();
                        try {

                            obj.put("timeline_username",search_username);
                            obj.put("timeline_posts","individual");
                            obj.put("timeline_society_name",society_name.toLowerCase());
                            socket.emit("data",obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                }));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getArguments().containsKey("go_home")){
                    Intent i=new Intent(v.getContext(), Home_Screen.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                else getActivity().finish();
            }
        });
        message.setOnClickListener(v1 -> {
            BottomFrag f=new BottomFrag();

            dialogBind.putString("switch","messaging");
            dialogBind.putString("searchUsername",search_username);
            f.setArguments(dialogBind);
            f.show(getFragmentManager(),"frag");
        });

        followers_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomFrag f=new BottomFrag();
                dialogBind.putString("switch","followers_list");
                dialogBind.putString("searchUsername",search_username);
                f.setArguments(dialogBind);
                f.show(getFragmentManager(),"frag");
            }
        });
        following_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomFrag f=new BottomFrag();

                dialogBind.putString("switch","following_list");
                dialogBind.putString("searchUsername",search_username);
                f.setArguments(dialogBind);
                f.show(getFragmentManager(),"frag");
            }
        });
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    follow.startAnimation(an);
                    follow.setVisibility(View.INVISIBLE);
                    followed.setVisibility(View.VISIBLE);
                    JSONObject obj = new JSONObject();
                    try {
                        if (private_account_status.equals("yes")) {
                            obj.put("follower_username", my_username);

                            if(Hawk.get("myFullName")!=null)obj.put("follower_fullname",Hawk.get("myFullName"));
                            else obj.put("follower_fullname",sharedPref.getString("myFullName",null));

                            if(Hawk.get("myProfilePic")!=null)obj.put("follower_profPic",Hawk.get("myProfilePic"));
                            else obj.put("follower_profPic",sharedPref.getString("myProfilePic",null));

                            obj.put("following_username", search_username);
                            obj.put("follow_private_account_status", "yes");
                        } else {
                            obj.put("follower_username", my_username);

                            if(Hawk.get("myFullName")!=null)obj.put("follower_fullname",Hawk.get("myFullName"));
                            else obj.put("follower_fullname",sharedPref.getString("myFullName",null));

                            if(Hawk.get("myProfilePic")!=null)obj.put("follower_profPic",Hawk.get("myProfilePic"));
                            else obj.put("follower_profPic",sharedPref.getString("myProfilePic",null));

                            obj.put("following_username", search_username);
                        }
                        socket.emit("data", obj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                //Increasing the follower counter
                if(Integer.valueOf(followers_num.getText().toString())!=0){
                    followers_num.setText(String.valueOf(Integer.valueOf(followers_num.getText().toString())+1));
                }
                else{
                    followers_num.setText("1");
                }


            }
        });
        followed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followed.startAnimation(an);
                followed.setVisibility(View.INVISIBLE);
                follow.setVisibility(View.VISIBLE);
                JSONObject obj=new JSONObject();
                try {
                    if(private_account_status.equals("yes")) {
                        obj.put("unfollower_username", my_username);
                        obj.put("unfollowing_username", search_username);
                        obj.put("unfollow_private_account_status","yes");
                        post_txt.setVisibility(View.GONE);
                        picRView.setVisibility(View.GONE);
                        privacyIndicator.setVisibility(View.VISIBLE);
                    }
                    else{
                        obj.put("unfollower_username", my_username);
                        obj.put("unfollowing_username", search_username);
                    }
                    socket.emit("data",obj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(Integer.valueOf(followers_num.getText().toString())!=0){
                followers_num.setText(String.valueOf(Integer.valueOf(followers_num.getText().toString())-1));
                }
            }
        });
        block_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject ob=new JSONObject();
                try {
                    ob.put("unblock_blocked_user",search_username);
                    ob.put("unblock_blocker",my_username);
                    socket.emit("data",ob);
                    socket.on("unblock_status", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            JSONObject ob=(JSONObject)args[0];
                            try {
                                if(ob.getString("status").equals("yes")){
                                    Needle.onMainThread().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            blocked_status="no";
                                            block_btn.setVisibility(View.GONE);
                                            follow.setVisibility(View.VISIBLE);
                                            message.setVisibility(View.VISIBLE);
                                            Toast.makeText(v.getContext(),"User is unblocked successfully.",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        triple_dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                final View vi = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_image, null);
                RelativeLayout mainCont=vi.findViewById(R.id.dialogImgRelLay);
                RecyclerView postSettings=vi.findViewById(R.id.fontRecycler);
                TouchImageView img=vi.findViewById(R.id.dialog_image);
                img.setVisibility(View.GONE);
                postSettings.setVisibility(View.VISIBLE);

                //Setting Wrap Content
                RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                mainCont.setLayoutParams(params);
                mainCont.setBackgroundResource(R.drawable.bg_gradient);



                final ArrayList<String> name=new ArrayList<>();
                //name.add("Report this");
                if(blocked_status.equals("no"))name.add("Block");
                else name.add("Unblock");
                name.add("Report");

                AccountSettingsAdapter adapter=new AccountSettingsAdapter(name,null,null,"triple_dot",null);
                postSettings.setHasFixedSize(true);
                postSettings.setLayoutManager(new LinearLayoutManager(v.getContext()));
                postSettings.setAdapter(adapter);
                postSettings.addOnItemTouchListener(
                        new RecyclerItemClickListener(v.getContext(), postSettings ,new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                              if(name.get(position).equals("Block")){
                                  JSONObject ob=new JSONObject();
                                  try {
                                      ob.put("block_blocked_user",search_username);
                                      ob.put("block_blocker",my_username);
                                      socket.emit("data",ob);
                                      socket.on("block_status", new Emitter.Listener() {
                                          @Override
                                          public void call(Object... args) {
                                              JSONObject ob=(JSONObject)args[0];
                                              try {
                                                  if(ob.getString("status").equals("yes")){
                                                      Needle.onMainThread().execute(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              blocked_status="yes";
                                                              if(followed.getVisibility()==View.VISIBLE) {
                                                                  //Decreasing number of follower
                                                                  if (Integer.valueOf(followers_num.getText().toString()) != 0) {
                                                                      followers_num.setText(String.valueOf(Integer.valueOf(followers_num.getText().toString()) - 1));
                                                                  }
                                                              }
                                                              block_btn.setVisibility(View.VISIBLE);
                                                              follow.setVisibility(View.INVISIBLE);
                                                              followed.setVisibility(View.INVISIBLE);
                                                              message.setVisibility(View.INVISIBLE);

                                                              Toast.makeText(v.getContext(),"User is blocked successfully.",Toast.LENGTH_SHORT).show();
                                                          }
                                                      });
                                                  }
                                              } catch (JSONException e) {
                                                  e.printStackTrace();
                                              }
                                          }
                                      });
                                      dialog.dismiss();
                                  } catch (JSONException e) {
                                      e.printStackTrace();
                                  }
                              }
                               else if(name.get(position).equals("Unblock")){
                                    JSONObject ob=new JSONObject();
                                    try {

                                        ob.put("unblock_blocked_user",search_username);
                                        ob.put("unblock_blocker",my_username);
                                        socket.emit("data",ob);
                                        socket.on("unblock_status", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob=(JSONObject)args[0];
                                                try {
                                                    if(ob.getString("status").equals("yes")){
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                blocked_status="no";
                                                                follow.setVisibility(View.VISIBLE);
                                                                message.setVisibility(View.VISIBLE);
                                                                block_btn.setVisibility(View.GONE);
                                                                Toast.makeText(v.getContext(),"User is unblocked successfully.",Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                        dialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                              else{//Report
                                  Needle.onBackgroundThread().execute(new Runnable() {
                                      @Override
                                      public void run() {
                                          socket.disconnect();
                                          socket.connect();

                                          JSONObject ob=new JSONObject();
                                          try {
                                              ob.put("report_reporter_name",my_username);
                                              ob.put("report_profile_owner",search_username);
                                              ob.put("report_type","profile");
                                              socket.emit("data",ob);
                                              socket.on("report_stat", new Emitter.Listener() {
                                                  @Override
                                                  public void call(Object... args) {
                                                      JSONObject ob=(JSONObject)args[0];
                                                      try {
                                                          if(ob.getString("status").equals("yes")){
                                                              socket.disconnect();
                                                          }
                                                      } catch (JSONException e) {
                                                          e.printStackTrace();
                                                      }
                                                  }
                                              });
                                          } catch (JSONException e) {
                                              e.printStackTrace();
                                          }
                                      }
                                  });
                                  Needle.onMainThread().execute(new Runnable() {
                                      @Override
                                      public void run() {
                                          Toast.makeText(v.getContext(),"We would check your report very soon .",Toast.LENGTH_SHORT).show();
                                          dialog.dismiss();
                                      }
                                  });
                              }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                        }));
                //Dialog Properties////////
                builder.setView(vi);
                dialog = builder.create();
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setDimAmount(0.3f);
                /////////////////////////
            }

        });

        if(my_username.equals(search_username)||search_username.equals("infinity")||search_username.equals("srinu@1919")||
                search_username.equals("infinity.admin")) {
            triple_dot.setVisibility(View.GONE);
        }


        if(my_username.equals(search_username)||my_username.equals("infinity")) {
            profile_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    final View vi = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_image, null);
                    RelativeLayout mainCont=vi.findViewById(R.id.dialogImgRelLay);
                    RecyclerView postSettings=vi.findViewById(R.id.fontRecycler);
                    TouchImageView img=vi.findViewById(R.id.dialog_image);
                    img.setVisibility(View.GONE);
                    postSettings.setVisibility(View.VISIBLE);

                    //Setting Wrap Content
                    RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                    mainCont.setLayoutParams(params);
                    mainCont.setBackgroundResource(R.drawable.bg_gradient);



                    final ArrayList<String> name=new ArrayList<>();
                    //name.add("Report this");
                    name.add("Change Profile Picture");
                    name.add("Remove Profile Picture");

                    AccountSettingsAdapter adapter=new AccountSettingsAdapter(name,null,null,"triple_dot",null);
                    postSettings.setHasFixedSize(true);
                    postSettings.setLayoutManager(new LinearLayoutManager(v.getContext()));
                    postSettings.setAdapter(adapter);
                    postSettings.addOnItemTouchListener(
                            new RecyclerItemClickListener(v.getContext(), postSettings ,new RecyclerItemClickListener.OnItemClickListener() {
                                @Override public void onItemClick(View view, int position) {
                                    if(name.get(position).equals("Change Profile Picture")){
                                        Needle.onBackgroundThread().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Hawk.put("what","timeline");
                                                Hawk.put("bookTitle", "no");
                                            }
                                        });
                                        Intent i = new Intent(v.getContext(), ProfileHolder.class);
                                        Bundle b = new Bundle();
                                        b.putString("what", "profile_pic");
                                        i.putExtras(b);
                                        i.putExtra("Open", "chooser");
                                        startActivity(i);
                                        dialog.dismiss();
                                    }
                                    else if(name.get(position).equals("Remove Profile Picture")){
                                      profile_pic.setImageBitmap(null);
                                      JSONObject ob=new JSONObject();
                                        try {
                                            ob.put("remove_profile_pic_username",my_username);
                                            Toast.makeText(v.getContext(),"Profile Pic Removed",Toast.LENGTH_SHORT).show();
                                            socket.emit("data",ob);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        profile_pic.setImageResource(R.drawable.profile);
                                      dialog.dismiss();
                                    }

                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                            }));
                    //Dialog Properties////////
                    builder.setView(vi);
                    dialog = builder.create();
                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setDimAmount(0.3f);
                    /////////////////////////
                }
            });

            editWall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    final View vi = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_image, null);
                    RelativeLayout mainCont=vi.findViewById(R.id.dialogImgRelLay);
                    RecyclerView postSettings=vi.findViewById(R.id.fontRecycler);
                    TouchImageView img=vi.findViewById(R.id.dialog_image);
                    img.setVisibility(View.GONE);
                    postSettings.setVisibility(View.VISIBLE);

                    //Setting Wrap Content
                    RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                    mainCont.setLayoutParams(params);
                    mainCont.setBackgroundResource(R.drawable.bg_gradient);



                    final ArrayList<String> name=new ArrayList<>();
                    //name.add("Report this");
                    name.add("Change Wall Picture");
                    name.add("Remove Wall Picture");

                    AccountSettingsAdapter adapter=new AccountSettingsAdapter(name,null,null,"triple_dot",null);
                    postSettings.setHasFixedSize(true);
                    postSettings.setLayoutManager(new LinearLayoutManager(v.getContext()));
                    postSettings.setAdapter(adapter);
                    postSettings.addOnItemTouchListener(
                            new RecyclerItemClickListener(v.getContext(), postSettings ,new RecyclerItemClickListener.OnItemClickListener() {
                                @Override public void onItemClick(View view, int position) {
                                    if(name.get(position).equals("Change Wall Picture")){

                                        Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                Hawk.put("bookTitle", "no");
                                            }
                                        });
                                        Intent i = new Intent(v.getContext(), ProfileHolder.class);
                                        Bundle b = new Bundle();
                                        b.putString("what", "wall_pic");
                                        i.putExtras(b);
                                        i.putExtra("Open", "chooser");
                                        startActivity(i);



                                        dialog.dismiss();
                                    }
                                    else if(name.get(position).equals("Remove Wall Picture")){
                                        kenBurnsView.setImageBitmap(null);
                                        JSONObject ob=new JSONObject();
                                        try {
                                            ob.put("remove_wall_pic_username",my_username);
                                            Toast.makeText(v.getContext(),"Wall Pic Removed",Toast.LENGTH_SHORT).show();
                                            socket.emit("data",ob);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        dialog.dismiss();
                                    }

                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                            }));
                    //Dialog Properties////////
                    builder.setView(vi);
                    dialog = builder.create();
                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setDimAmount(0.3f);
                    /////////////////////////
                }
            });
        }
        kenBurnsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(kenBurnsView.getDrawable()!=null){
                Bitmap bitmap = ((BitmapDrawable) kenBurnsView.getDrawable()).getBitmap();

                    // Opening Dialog
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(v.getContext());
                    final View dialog_view = LayoutInflater.from(back.getContext()).inflate(R.layout.dialog_image, null);
                    builder.setView(dialog_view);
                    dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.getWindow().setDimAmount(0.9f);
                    final TouchImageView img = dialog_view.findViewById(R.id.dialog_image);
                    final DragToClose dragToClose = dialog_view.findViewById(R.id.dragViewDialogImg);

                    //Setting Bitmap
                    img.setImageBitmap(bitmap);

                    dialog.show();
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);

                    dragToClose.setVisibility(View.VISIBLE);
                    dragToClose.openDraggableContainer();
                    //Drag to Close view
                    dragToClose.setDragListener(new DragListener() {
                        @Override
                        public void onStartDraggingView() {

                        }

                        @Override
                        public void onViewCosed() {
                            dialog.dismiss();
                        }
                    });
                    dialog.setOnDismissListener(
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ViewGroup v = (ViewGroup) dialog_view.getParent();
                                    v.removeAllViews();
                                }
                            }
                    );
                }
            }
        });



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
    void showData(List<TimelineData> timelineDataList){
        gridadp = new StarredAdapter(timelineDataList);
        picRView.setLayoutManager(null);
        picRView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        picRView.setAdapter(null);
        picRView.setLayoutManager(gridLayoutManager);
        picRView.setAdapter(gridadp);
        //assumed you attached your layout manager earlier
    }
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {

       if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
           ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
           //Getting Path
           String path=images.get(0).path;

           Glide.with(v.getContext()).asBitmap().load(path).into(new SimpleTarget<Bitmap>(700, 700) {

               @Override
               public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                   Toast.makeText(v.getContext(),"Wall Pic Uploaded .",Toast.LENGTH_SHORT).show();
                   kenBurnsView.setImageBitmap(null);
                   kenBurnsView.setImageBitmap(resource);
                   Needle.onBackgroundThread().execute(new Runnable() {
                       @Override
                       public void run() {
                           JSONObject ob=new JSONObject();
                           try {
                               ob.put("wall_pic_username",my_username);
                               ob.put("wall_pic",getStringImage(resource));
                               socket.emit("data",ob);
                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                       }
                   });

               }
           });


           //  Glide.with(v.getContext()).load(path).apply(new RequestOptions().override(700,700)).into(kenBurnsView);
       }
   }
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    public static SearchProfile create(){
        return new SearchProfile();
    }



    @Override
    public void onStart() {
        super.onStart();
        //Getting Posts
        socket.disconnect();
        socket.connect();

        refreshData();
    }

    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);

        if (!my_username.equals(search_username)) {
            JSONObject ob=new JSONObject();

            try {//Checking Account Private or not
                ob.put("private_acc_requester_username",my_username);
                ob.put("private_acc_accepter_username",search_username);
                socket.emit("data",ob);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject ov=new JSONObject();
                        try {
                            ov.put("check_block_blocked_user",search_username);
                            ov.put("check_block_blocker",my_username);
                            socket.emit("data",ov);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },200);
                socket.on("check_block_status", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JSONObject ob=(JSONObject)args[0];
                        try {
                            if(ob.getString("status").equals("yes")){
                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        blocked_status="yes";
                                        follow.setVisibility(View.INVISIBLE);
                                        block_btn.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                            else{
                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        blocked_status="no";
                                        message.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                socket.on("private_acc_check_stat", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JSONObject obj=(JSONObject)args[0];
                        try {
                            if(obj.getString("accepted_status").equals("yes")){
                                try {
                                    if(obj.getString("acc_status").equals("private"))
                                        private_account_status="yes";
                                    else private_account_status="no";

                                    JSONObject obju = new JSONObject();
                                    obju.put("timeline_username", search_username);
                                    obju.put("timeline_posts", "individual");
                                    obju.put("timeline_society_name",society_name.toLowerCase());
                                    socket.emit("data", obju);
                                    Needle.onMainThread().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            post_txt.setVisibility(View.VISIBLE);
                                            picRView.setVisibility(View.VISIBLE);
                                            privacyIndicator.setVisibility(View.GONE);
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(false);
                                        private_account_status="yes";
                                        // Toast.makeText(v.getContext(),"Sorry This Account is Private",Toast.LENGTH_SHORT).show();
                                        post_txt.setVisibility(View.VISIBLE);
                                        picRView.setVisibility(View.GONE);
                                        privacyIndicator.setVisibility(View.VISIBLE);
                                        society_rView.setVisibility(View.GONE);
                                        followed.setText("Requested");
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            JSONObject obju = new JSONObject();
            try {
                obju.put("timeline_username", search_username);
                obju.put("timeline_posts", "individual");
                obju.put("timeline_society_name",society_name.toLowerCase());
                socket.emit("data", obju);
                post_txt.setVisibility(View.VISIBLE);
                picRView.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //Invisible of follow btns for own account
        if(my_username.equals(search_username)){
            follow.setVisibility(View.INVISIBLE);
            message.setVisibility(View.INVISIBLE);
        }
        else {
            editWall.setVisibility(View.GONE);

            //Checking Followers for changing color of btns
            JSONObject obj=new JSONObject();
            try {
                obj.put("check_follower_username",my_username);
                obj.put("check_following_username",search_username);
                socket.emit("data",obj);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.on("check_follow", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    final JSONObject data = (JSONObject)args[0];
                    try {
                        final String st = data.getString("follow_status");
                        final int i=Integer.valueOf(st);
                        Handler handler = new Handler(Looper.getMainLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                // Any UI task, example
                                try {
                                    if(i==1){
                                        followed.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        if(block_btn.getVisibility()==View.VISIBLE) follow.setVisibility(View.INVISIBLE);
                                        else follow.setVisibility(View.VISIBLE);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        handler.sendEmptyMessage(1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }


        //Setting ProfileData
        if(search_username.equals(my_username)){
            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());

            if(Hawk.get("myFullName")!=null)fullname_txt.setText(Hawk.get("myFullName"));
            else fullname_txt.setText(sh.getString("myFullName",null));

            if(Hawk.get("myBio")!=null&&!Hawk.get("myBio").toString().equals("")) bio.setText(Hawk.get("myBio"));
            else if(sh.getString("myBio",null)!=null&&!sh.getString("myBio",null).equals("")) bio.setText(sh.getString("myBio",null));

            if(Hawk.get("myProfilePic")!=null) {
                if(Hawk.get("myProfilePic").equals("")) {
                    profile_pic.setImageResource(R.drawable.profile);
                }
                else {
                    ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));

                    Glide
                            .with(v.getContext())
                            .asBitmap()
                            .load(Hawk.get("myProfilePic").toString())
                            .apply(new RequestOptions().placeholder(cd).diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true).override(250,250))
                            .thumbnail(0.1f)
                            .into(profile_pic);
                }
            }
            else{
                if(sh.getString("myProfilePic",null).equals("")) {
                    profile_pic.setImageResource(R.drawable.profile);
                }
                else {
                    ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));

                    Glide
                            .with(v.getContext())
                            .asBitmap()
                            .load(sh.getString("myProfilePic",null))
                            .apply(new RequestOptions().placeholder(cd).diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true).override(250,250))
                            .thumbnail(0.1f)
                            .into(profile_pic);
                }
            }

            if(Hawk.get("myWallPic")!=null) {
                Glide.with(kenBurnsView.getContext())
                        .load(Hawk.get("myWallPic").toString())
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .override(600, 600))
                        .into(kenBurnsView);
            }
            else if(sh.getString("myWallPic",null)!=null){
                Glide.with(kenBurnsView.getContext())
                        .load(sh.getString("myWallPic",null))
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .override(600, 600))
                        .into(kenBurnsView);
            }

            if(Hawk.get("mySocietyList")!=null&&!Hawk.get("mySocietyList").toString().equals("")) {
                String finalSociety_list=Hawk.get("mySocietyList").toString();
                String[] splits = finalSociety_list.replace("[", "").replace("]", "").split(",");
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(0,"Home");
                for (int i = 0; i < splits.length; i++) {
                    arrayList.add(splits[i].replace("\"", ""));
                }
                if(arrayList.contains("null")) society_rView.setVisibility(View.GONE);
                else {
                    TimeLineSocietyAdapter adapter = new TimeLineSocietyAdapter(arrayList,society_name, false);
                    society_rView.setAdapter(adapter);
                }
            }
            else if(sh.getString("mySocietyList",null)!=null&&!sh.getString("mySocietyList",null).equals("")){
                String finalSociety_list=sh.getString("mySocietyList",null);
                String[] splits = finalSociety_list.replace("[", "").replace("]", "").split(",");
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(0,"Home");
                for (int i = 0; i < splits.length; i++) {
                    arrayList.add(splits[i].replace("\"", ""));
                }
                if(arrayList.contains("null")) society_rView.setVisibility(View.GONE);
                else {
                    TimeLineSocietyAdapter adapter = new TimeLineSocietyAdapter(arrayList,society_name, false);
                    society_rView.setAdapter(adapter);
                }
            }
            else{
                society_rView.setVisibility(View.GONE);
            }
        }
        else if(getArguments().containsKey("searchFullname")&&getArguments().containsKey("searchSocietyList")){
            if(getArguments().containsKey("searchFullname")) fullname_txt.setText(getArguments().getString("searchFullname"));

            if(getArguments().containsKey("searchBio"))  bio.setText(getArguments().getString("searchBio"));

            if(getArguments().containsKey("searchWallPic")){
                Glide.with(kenBurnsView.getContext())
                        .load(getArguments().getString("searchWallPic"))
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .override(600, 600))
                        .into(kenBurnsView);
            }

            if(getArguments().containsKey("searchProfilePic")){
                if(getArguments().getString("searchProfilePic").equals("")) {
                    profile_pic.setImageResource(R.drawable.profile);
                }
                else {
                    ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));

                    Glide
                            .with(v.getContext())
                            .asBitmap()
                            .load(getArguments().getString("searchProfilePic"))
                            .apply(new RequestOptions().placeholder(cd).diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true).override(250,250))
                            .thumbnail(0.1f)
                            .into(profile_pic);
                }
            }

            if(getArguments().getString("searchSocietyList")!=null||!getArguments().getString("searchSocietyList").equals("")){
                String finalSociety_list=getArguments().getString("searchSocietyList");
                String[] splits = finalSociety_list.replace("[", "").replace("]", "").split(",");
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(0,"Home");
                for (int i = 0; i < splits.length; i++) {
                    arrayList.add(splits[i].replace("\"", ""));
                }
                if(arrayList.contains("null")) society_rView.setVisibility(View.GONE);
                else {
                    TimeLineSocietyAdapter adapter = new TimeLineSocietyAdapter(arrayList,society_name, false);
                    society_rView.setAdapter(adapter);
                }
            }

        }
        else{
            //Getting Profile Info
            item=new ListItem(society_name,search_username,fullname_txt,bio,profile_pic,kenBurnsView,society_rView,"profile_info");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                        //Setting Number of Follow
                        item=new ListItem(search_username,null,following_num,"following_counter",null,null,null,v.getContext());
                        item=new ListItem(search_username,null,followers_num,"followers_counter",null,null,null,v.getContext());

                        //Getting Posts Counter
                        item=new ListItem(search_username,null,post_counter,"posts_counter",null,null,null,v.getContext());
            }
        },250);


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

                }
            }
        },20000);
    }

    void addTimelineData(String username,String fullname,String postProfPic, String time, String img_link, String caption, String private_post_stat,
                         String comment_disabled, String share_disabled,String download_disabled,String likes_counter,
                         String comments_counter,String short_book_content,String society_name_adp){
       // timelineDataList.add(new TimelineData(username,time,img_link,caption,private_post_stat,comment_disabled,share_disabled));
        boolean isRepeated = false;
        for(TimelineData data:timelineDataList){
            if(data.getTime().equals(time)){
                isRepeated = true;
            }
        }
        if(!isRepeated){
            timelineDataList.add(new TimelineData(username,fullname,postProfPic, time, img_link, caption,private_post_stat, comment_disabled,
                    share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name_adp));
            //gridadp.notifyDataSetChanged();

        }


            //gridadp.notifyItemInserted(timelineDataList.size()-1);
        }


    private  Emitter.Listener handlePosts = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            try {
                JSONArray jsonArray=(JSONArray)args[0];
                Needle.onMainThread().execute(() -> {
                    timelineDataList.clear();//Clearing whole data in order to show new data in a proper sequence
                    swipeRefreshLayout.setRefreshing(false);
                    for(int i=0;i<jsonArray.length();i++){
                        try {
                            JSONObject ob=jsonArray.getJSONObject(i);
                            post_username=ob.getString("_pid");
                            post_fullname=ob.getString("owner_fullname");
                            if(ob.has("owner_profPic"))postProfPic=ob.getString("owner_profPic");
                            else postProfPic="";

                            //Skipping Private Posts
                            if(ob.getString("private_post_stat").equals("yes")&& !post_username.equals(my_username) ) continue;
                            else private_post_stat=ob.getString("private_post_stat");

                            post_time=ob.getString("time");

                            post_link=ob.getString("img_link");

                            likes_counter=ob.getString("likes_counter");
                            comments_counter=ob.getString("comments_counter");

                            if(ob.has("caption")) post_caption=ob.getString("caption");
                            else post_caption=null;
                            if(ob.has("short_book_content")) short_book_content=ob.getString("short_book_content");
                            else short_book_content=null;
                            comment_disabled=ob.getString("comment_disabled");

                            share_disabled=ob.getString("share_disabled");
                            download_disabled=ob.getString("download_disabled");
                            society_name_adp=ob.getString("society");

                            addTimelineData(post_username,post_fullname,postProfPic,post_time,post_link,post_caption,private_post_stat,
                                    comment_disabled,share_disabled,download_disabled,
                                    likes_counter,comments_counter,short_book_content,society_name_adp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                   /* RecyclerView.Adapter adapter=picRView.getAdapter();
                    picRView.setAdapter(null);
                    picRView.setAdapter(adapter);*/
                    gridadp.notifyDataSetChanged();
                    picRView.scrollToPosition(gridadp.getItemCount()-1);
                    picRView.scrollToPosition(0);
                });

            } catch (Exception e) {
                Log.e("error",e.toString());
            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();

    }
}
