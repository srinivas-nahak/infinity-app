package community.infinity.BottomFrags;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import community.infinity.ListItem;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.RecyclerViewItems.SpeedyLinearLayoutManager;
import community.infinity.activities.Home_Screen;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.ProfileHolder;
import community.infinity.adapters.AccountSettingsAdapter;
import community.infinity.adapters.SearchAdapter;
import community.infinity.adapters.TimeLineSocietyAdapter;
import community.infinity.image_related.ImageChooser;
import community.infinity.message.Message;
import community.infinity.message.MessageAdapter;
import community.infinity.writing.RememberTextStyle;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.HashTagHelper;
import custom_views_and_styles.ReverseInterpolator;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import layout.Messaging;
import needle.Needle;


/**
 * Created by Srinu on 27-11-2017.
 */

public class BottomFrag extends DialogFragment implements TabLayout.OnTabSelectedListener {

    private AppCompatImageButton back,tick_btn,choose_img_btn,refresh_btn;
    private String s,my_username,friend_username,tagString,msg,from,to,
            msg_time,img_link,msg_type,msg_post_owner_username,msg_post_owner_time,msg_post_img_link,msg_post_type,msg_post_multi;
    private TextView heading,seenText,clear_btn;
    private TabLayout tabLayout;
    private RelativeLayout rl,search_cont;
    private BottomSheetBehavior behavior;
    private RecyclerView rView,tagRView;
    private SearchAdapter comment_search_adapter;
    private AccountSettingsAdapter adapter;
    private EditText comment,searchBox;
    private ProgressDialog pDialog;
    public static String roomKey,msgImgLink;
    private TextView noResultTxt,typingTxt;
    private int msg_counter=0;//helpful for refreshing msg list if came from new_msg_search



    private ArrayList<String> selecTags=new ArrayList<>();
    private ArrayList<String> settings=new ArrayList<>();
    private ArrayList<String> username_list=new ArrayList<>();
    private ArrayList<String> fullname_list=new ArrayList<>();
    private ArrayList<String> time=new ArrayList<>();
    private ArrayList<String> profile_pic_list=new ArrayList<>();
    private ArrayList<String> bio_list=new ArrayList<>();
    private ArrayList<String> society_list=new ArrayList<>();
    private ArrayList<String> wall_pic_list=new ArrayList<>();



    private FrameLayout commentBox,loading_view;
    private RelativeLayout heading_view;
    private LinearLayout pencil;
    private MessageAdapter msgAdp;
    private List<Message> messages = new ArrayList<>();
    private ImageView commentPencilImg;
    private CircleImageView userImgMsgRoom;
    private TextView mention_btn;
    private TextView comment_count;
    public static EditText tagSelector;
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
    public BottomFrag() {
    }
    @SuppressLint("ValidFragment")
    public BottomFrag(TextView comment_count){
      this.comment_count=comment_count;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Hawk.init(getActivity().getApplicationContext()).build();


        socket.on("comments",handleIncomingComments);
        socket.on("followers_list",handleFollowers);
        socket.on("following_list",handleFollowing);
        socket.on("search",handleSearch);
        socket.on("previous_msg", handlePreviousMessages);
        socket.on("current_msg",handleCurrentMsg);
        socket.on("seen_stat",handleSeen);
        socket.on("typing_stat",handleTyping);
        socket.on("like_list",handleLikeList);
        socket.on("comment_like_list",handleCommentLikeList);
       // socket.on(Socket.EVENT_CONNECT,onConnect);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bot_frag, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        try {
            this.v = v;
            heading = v.findViewById(R.id.headingBot);
            back = v.findViewById(R.id.back_of_bot);
            rl = v.findViewById(R.id.relBot);
            rView = v.findViewById(R.id.botRView);
            pencil = v.findViewById(R.id.commPencil);
            comment = v.findViewById(R.id.commText);
            search_cont = v.findViewById(R.id.searchItemCont);
            heading_view = v.findViewById(R.id.hBot);
            commentBox = v.findViewById(R.id.commentBoxContainer);
            seenText = v.findViewById(R.id.seenText);
            searchBox = v.findViewById(R.id.searchBox);
            clear_btn = v.findViewById(R.id.clear_of_search_card);
            tabLayout = v.findViewById(R.id.searchTab);
            loading_view = v.findViewById(R.id.loadingViewBotFrag);
            noResultTxt = v.findViewById(R.id.noResultTxt);
            mention_btn = v.findViewById(R.id.mentionTxt);
            tagRView = v.findViewById(R.id.tagRView);
            tick_btn = v.findViewById(R.id.doneTags);
            typingTxt = v.findViewById(R.id.typingTxt);
            refresh_btn = v.findViewById(R.id.refresh_of_bot);
            userImgMsgRoom = v.findViewById(R.id.userImageMsgRoom);
            commentPencilImg = v.findViewById(R.id.commentPencilImg);
            choose_img_btn = v.findViewById(R.id.chooseMsg);
            DragToClose dragToClose = v.findViewById(R.id.dragViewBotFrag);

            //Setting Bg
            rl.setBackgroundResource(RememberTextStyle.themeResource);


            //Building Hawk
            Hawk.init(v.getContext()).build();

            //Making LoadingView visible first then RView
            loading_view.setVisibility(View.VISIBLE);
            rView.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loading_view.setVisibility(View.GONE);
                    rView.setVisibility(View.VISIBLE);
                }
            }, 1000);


            //Setting Tint
            final ButtonTint black_tint = new ButtonTint("white");
            black_tint.setTint(back);
            black_tint.setTint(refresh_btn);
            black_tint.setTint(choose_img_btn);

            comment.setMaxHeight(getDp(1050));
            final SpeedyLinearLayoutManager recycle_lay_manager = new SpeedyLinearLayoutManager(v.getContext(), RecyclerView.VERTICAL, false);
            rView.setLayoutManager(recycle_lay_manager);
            rView.setItemViewCacheSize(20);
            rView.setDrawingCacheEnabled(true);
            rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            rView.clearAnimation();
            ((SimpleItemAnimator) rView.getItemAnimator()).setSupportsChangeAnimations(false);
            //Getting usernames
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());
            if (Hawk.get("myUserName") != null) my_username = Hawk.get("myUserName");
            else my_username = sharedPref.getString("username", null);

            if (getArguments().containsKey("searchUsername")) {
                if (getArguments().getString("searchUsername") != null)
                    friend_username = getArguments().getString("searchUsername");
            }///////////////Error


            //Drag to Close view
            dragToClose.setDragListener(new DragListener() {
                @Override
                public void onStartDraggingView() {
                }

                @Override
                public void onViewCosed() {
                    dismiss();
                }
            });


            //Setting No Title
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

            //Getting Bundle Value
            s = getArguments().getString("switch");


            if (s.equals("like_list")) {
                heading.setText("Likes");
                JSONObject obj = new JSONObject();
                try {
                    obj.put("like_list_username", getArguments().getString("like_list_post_owner_username"));
                    obj.put("like_list_time", getArguments().getString("like_list_time"));
                    obj.put("like_list_like_viewer", my_username);
                    socket.emit("data", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter = new AccountSettingsAdapter(profile_pic_list, username_list, fullname_list, "like_list", null);
                rView.setAdapter(adapter);
                commentBox.setVisibility(View.GONE);
            } else if (s.equals("comment_like_list")) {
                heading.setText("Likes");
                JSONObject obj = new JSONObject();
                try {
                    obj.put("comment_like_list_post_owner_username", getArguments().getString("comment_like_list_post_owner_username"));
                    obj.put("comment_like_list_post_owner_time", getArguments().getString("comment_like_list_post_owner_time"));
                    obj.put("comment_like_list_comment_owner_username", getArguments().getString("comment_like_list_comment_owner_username"));
                    obj.put("comment_like_list_comment_owner_time", getArguments().getString("comment_like_list_comment_owner_time"));
                    obj.put("comment_like_list_viewer", my_username);
                    socket.emit("data", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter = new AccountSettingsAdapter(profile_pic_list, username_list, fullname_list, "like_list", null);
                rView.setAdapter(adapter);
                commentBox.setVisibility(View.GONE);
            } else if (s.equals("search")) {
                dragToClose.setDraggableContainerId(R.id.relBot);
                dragToClose.setDraggableViewId(tabLayout.getId());
                search_cont.setVisibility(View.VISIBLE);
                heading_view.setVisibility(View.GONE);
                heading.setVisibility(View.GONE);
                commentBox.setVisibility(View.GONE);

                if (getArguments().containsKey("tagSelect")) {
                    tagRView.setVisibility(View.VISIBLE);
                    tick_btn.setVisibility(View.VISIBLE);
                    black_tint.setTint(tick_btn);//its used in tag selection


                    //Showing Keyboard
                    new Handler().postDelayed(new Runnable() {

                        public void run() {
                            searchBox.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                            searchBox.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                        }
                    }, 200);


                    //Arranging Views
                    //Getting Same Pixels for everyDevice
                    float d = v.getContext().getResources().getDisplayMetrics().density;
                    int margin = (int) (6 * d); // margin in pixels

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);

                    params.setMargins(0, margin, 0, 0);
                    params.addRule(RelativeLayout.BELOW, tagRView.getId());

                    noResultTxt.setLayoutParams(params);
                    rView.setLayoutParams(params);

                    //Recyclerview Related
                    TimeLineSocietyAdapter tag_adp = new TimeLineSocietyAdapter(selecTags, null, true);
                    GridLayoutManager grid = new GridLayoutManager(v.getContext(), 2, GridLayoutManager.HORIZONTAL, false);
                    tagRView.setLayoutManager(grid);
                    tagRView.setAdapter(tag_adp);

                    rView.addOnItemTouchListener(new RecyclerItemClickListener(v.getContext(), rView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if (s.equals("search")) {
                                selecTags.add("@" + username_list.get(position));
                                LinkedHashSet<String> set = new LinkedHashSet<>(selecTags);
                                selecTags.clear();
                                selecTags.addAll(set);
                                tag_adp.notifyDataSetChanged();

                            }
                            if (s.equals("tag_search")) {
                                selecTags.add(time.get(position));
                                LinkedHashSet<String> set = new LinkedHashSet<>(selecTags);
                                selecTags.clear();
                                selecTags.addAll(set);
                                tag_adp.notifyDataSetChanged();

                            }
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    }));

                    //Onclick
                    tick_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Setting data back to the first fragment

                            if (selecTags != null && selecTags.size() > 0 && getArguments().containsKey("timelineEdit")) {
                                for (int i = 0; i < selecTags.size(); i++) {
                                    tagSelector.setText(tagSelector.getText() + selecTags.get(i) + " ");
                                }
                                // Showning Keyboard
                                new Handler().postDelayed(new Runnable() {

                                    public void run() {
                                        tagSelector.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                                        tagSelector.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                                    }
                                }, 200);
                                dismiss();
                            } else if (selecTags != null && selecTags.size() > 0 && !getArguments().containsKey("timelineEdit")) {
                                Intent i = new Intent()
                                        .putStringArrayListExtra("selectedTags", selecTags);

                                getTargetFragment().onActivityResult(1919, Activity.RESULT_OK, i);
                                dismiss();
                            } else {
                                Toast.makeText(v.getContext(), "Please choose any tag(s) to proceed.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                } else {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    float d = v.getContext().getResources().getDisplayMetrics().density;
                    int margin = (int) (6 * d); // margin in pixels
                    params.setMargins(0, margin, 0, 0);
                    params.addRule(RelativeLayout.BELOW, R.id.searchItemCont);
                    noResultTxt.setLayoutParams(params);
                    rView.setLayoutParams(params);
                }

                if (getArguments().containsKey("message_search") || getArguments().containsKey("new_message_search")) {
                    if (getArguments().containsKey("message_search")) {
                        Snackbar snackbar = Snackbar.make(searchBox, "Share the post by just Clicking on names", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.parseColor("#43cea2"));
                        View sbView = snackbar.getView();
                        sbView.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.textColor));
                        TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.parseColor("#001919"));
                        snackbar.show();
                        searchBox.setHint("Search Your Friends");
                    } else {
                        searchBox.setHint("Search your friends to start chat ");
                    }
                    tabLayout.setVisibility(View.GONE);
                } else {
                    tabLayout.addTab(tabLayout.newTab().setText("People"));
                    tabLayout.addTab(tabLayout.newTab().setText("# Tags"));
                    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                    tabLayout.addOnTabSelectedListener(this);
                }

                //Searching Task
                searchBox.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        clear_btn.setVisibility(View.VISIBLE);

                        username_list.clear();
                        fullname_list.clear();
                        profile_pic_list.clear();
                        wall_pic_list.clear();
                        bio_list.clear();
                        society_list.clear();

                        adapter = new AccountSettingsAdapter(username_list, fullname_list, profile_pic_list,
                                wall_pic_list, bio_list, society_list, "search", false);
                        rView.setAdapter(adapter);
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("search", s);
                            obj.put("search_username", my_username);//It's given to check the user is blocked by someone or not
                            socket.emit("data", obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (editable.length() == 0) {
                            clear_btn.setVisibility(View.GONE);
                            if (tabLayout.getVisibility() == View.VISIBLE)
                                tabLayout.getTabAt(0).select();
                        }
                        if (editable.toString().contains("#")) {
                            if (!getArguments().containsKey("message_search") || !getArguments().containsKey("new_message_search")) {
                                s = "tag_search";
                                if (getArguments().containsKey("tagSelect")) {
                                    adapter = new AccountSettingsAdapter(null, time, null, "tag_search", true);


                                } else {
                                    adapter = new AccountSettingsAdapter(null, time, null, "tag_search", false);
                                }
                                if (adapter != null) {
                                    rView.setAdapter(adapter);
                                    if (tabLayout.getVisibility() == View.VISIBLE)
                                        tabLayout.getTabAt(1).select();
                                }
                            }
                        } else {
                            if (getArguments().containsKey("tagSelect")) {
                                // adapter = new AccountSettingsAdapter( settings,username_list,fullname_list ,"search",true);
                                adapter = new AccountSettingsAdapter(username_list, fullname_list, profile_pic_list,
                                        null, null, null, "search", true);
                            } else if (getArguments().containsKey("message_search")) {
                                adapter = new AccountSettingsAdapter(username_list, fullname_list, profile_pic_list, "message_search",
                                        getArguments().getString("postOwnerUsername"), getArguments().getString("postOwnerTime"),
                                        getArguments().getString("postImgLink"), getArguments().getString("postMulti"),
                                        getArguments().getString("postType"), getArguments().getString("download_stat"));
                            } else if (getArguments().containsKey("new_message_search")) {
                                adapter = new AccountSettingsAdapter(username_list, fullname_list, profile_pic_list,
                                        "new_message_search", getFragmentManager(), false);
                            } else {
                                adapter = new AccountSettingsAdapter(username_list, fullname_list, profile_pic_list,
                                        wall_pic_list, bio_list, society_list, "search", false);
                            }
                            rView.setAdapter(null);
                            rView.setAdapter(adapter);
                            s = "search";
                            if (!getArguments().containsKey("message_search") && !getArguments().containsKey("new_message_search"))
                                tabLayout.getTabAt(0).select();
                        }
                    }
                });
            } else if (s.equals("followers_list")) {
                search_cont.setVisibility(View.GONE);
                heading_view.setVisibility(View.VISIBLE);
                heading.setVisibility(View.VISIBLE);
                back.setVisibility(View.VISIBLE);
                commentBox.setVisibility(View.GONE);
                heading.setText("Followers");
                refresh_btn.setVisibility(View.VISIBLE);

                JSONObject obj = new JSONObject();
                try {
                    obj.put("get_followers_username", friend_username);//frndUsername is searchUsername
                    obj.put("followers_viewer_username", my_username);
                    socket.emit("data", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Setting adapter of Recyclerview
                adapter = new AccountSettingsAdapter(null, username_list, null, "followers_list", getFragmentManager());
                rView.setAdapter(adapter);

                refresh_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        username_list.clear();
                        adapter.notifyDataSetChanged();
                        rView.invalidate();

                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("get_followers_username", friend_username);//frndUsername is searchUsername
                            obj.put("followers_viewer_username", my_username);
                            socket.emit("data", obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } else if (s.equals("following_list")) {
                search_cont.setVisibility(View.GONE);
                heading_view.setVisibility(View.VISIBLE);
                heading.setVisibility(View.VISIBLE);
                back.setVisibility(View.VISIBLE);
                commentBox.setVisibility(View.GONE);
                heading.setText("Following");
                refresh_btn.setVisibility(View.VISIBLE);

                JSONObject obj = new JSONObject();
                try {
                    obj.put("get_following_username", friend_username);//frndUsername is searchUsername
                    obj.put("following_viewer_username", my_username);
                    socket.emit("data", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Setting adapter of Recyclerview
                adapter = new AccountSettingsAdapter(null, username_list, null, "following_list", getFragmentManager());
                rView.setAdapter(adapter);

                refresh_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        username_list.clear();
                        adapter.notifyDataSetChanged();
                        rView.invalidate();

                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("get_following_username", friend_username);//frndUsername is searchUsername
                            obj.put("following_viewer_username", my_username);
                            socket.emit("data", obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (s.equals("comments")) {

                heading.setText("Comments");
                refresh_btn.setVisibility(View.VISIBLE);

                Animation an = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_buttons);


                //Setting # and @ support
                char[] additionalSymbols = new char[]{'&', '.', '_'};
                final HashTagHelper commentTagHelper = HashTagHelper.Creator.create(v.getContext().getResources().getColor(R.color.comntTagColor), new HashTagHelper.OnHashTagClickListener() {
                    @Override
                    public void onHashTagClicked(String hashTag, char sign) {
                    }
                }, additionalSymbols);
                commentTagHelper.handle(comment);


                //Getting Previous Comments
                JSONObject obj = new JSONObject();
                try {
                    obj.put("comment_post_owner_username", getArguments().getString("comment_post_owner_username"));
                    obj.put("comment_post_owner_time", getArguments().getString("comment_post_owner_time"));
                    obj.put("comment_post_comment_viewer", my_username);
                    //obj.put("content",comment.getText().toString().trim());
                    socket.emit("data", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Setting Adapter
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                //manager.setExtraLayoutSpace(height);
                adapter = new AccountSettingsAdapter(settings, username_list, time, fullname_list, profile_pic_list, "comments",
                        getArguments().getString("comment_post_owner_username"), getArguments().getString("comment_post_owner_time"), comment_count, getFragmentManager(), comment);


                rView.setAdapter(adapter);
                //Caching
                rView.setItemViewCacheSize(30);
                rView.setDrawingCacheEnabled(true);
                rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

                //OnClick

                refresh_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        settings.clear();
                        username_list.clear();
                        time.clear();
                        fullname_list.clear();
                        profile_pic_list.clear();

                        adapter.notifyDataSetChanged();
                        rView.invalidate();

                        //Getting Previous Comments
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("comment_post_owner_username", getArguments().getString("comment_post_owner_username"));
                            obj.put("comment_post_owner_time", getArguments().getString("comment_post_owner_time"));
                            obj.put("comment_post_comment_viewer", my_username);
                            //obj.put("content",comment.getText().toString().trim());
                            socket.emit("data", obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

                mention_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mention_btn.startAnimation(an);
                        BottomFrag f = new BottomFrag();

                        Bundle b = new Bundle();
                        b.putString("switch", "search");
                        b.putString("tagSelect", "yes");
                        //Setting Target Fragment for getting result
                        f.setTargetFragment(BottomFrag.this, 1919);
                        f.setArguments(b);
                        f.show(getFragmentManager(), "fra");
                    }
                });


                pencil.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (comment_count.getVisibility() == View.GONE) {
                            comment_count.setVisibility(View.VISIBLE);
                            comment_count.setText("1 Comment");
                        } else {
                            int i = Integer.valueOf(comment_count.getText().toString().substring(0, comment_count.getText().toString().indexOf(" "))) + 1;
                            comment_count.setText(i + " Comments");
                        }
                        if (comment.getText().toString().trim().equals("")) {
                            Toast.makeText(v.getContext(), "Please Enter Some Text!", Toast.LENGTH_SHORT).show();
                        } else {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                            String time_format = simpleDateFormat.format(new Date());
                            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                            JSONObject obj = new JSONObject();
                            try {
                                obj.put("comment_post_owner_username", getArguments().getString("comment_post_owner_username"));
                                obj.put("comment_counter", adapter.getItemCount() + 1 + "");
                                obj.put("comment_commenter_username", Hawk.get("myUserName"));

                                if (Hawk.get("myFullName") != null)
                                    obj.put("comment_commenter_fullname", Hawk.get("myFullName"));
                                else
                                    obj.put("comment_commenter_fullname", sh.getString("myFullName", null));

                                if (Hawk.get("myProfilePic") != null)
                                    obj.put("comment_commenter_profPic", Hawk.get("myProfilePic"));
                                else
                                    obj.put("comment_commenter_profPic", sh.getString("myProfilePic", null));

                                obj.put("comment_time", time_format);

                                obj.put("comment_post_owner_time", getArguments().getString("comment_post_owner_time"));

                                obj.put("comment_content", URLEncoder.encode(comment.getText().toString().trim(), "UTF-8"));

                                if (commentTagHelper.getOnlyTags(false, "@") != null && commentTagHelper.getOnlyTags(false, "@").size() > 0) {
                                    Gson gson = new Gson();

                                    obj.put("comment_mentioned_names", gson.toJson(commentTagHelper.getOnlyTags(false, "@")));
                                }
                                socket.emit("data", obj);

                                //showing Comments in rview
                                if (Hawk.get("myUserName") != null && Hawk.get("myFullName") != null) {
                                    addComment(Hawk.get("myUserName"), time_format, comment.getText().toString().trim(),
                                            Hawk.get("myFullName"), Hawk.get("myProfilePic"));
                                } else {
                                    addComment(sharedPref.getString("myUserName", null), time_format, comment.getText().toString().trim(),
                                            sharedPref.getString("myFullName", null), sharedPref.getString("myProfilePic", null));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            comment.setText("");
                        }
                    }
                });


            }

            /////OnClik/////
            clear_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchBox.setText("");
                    clear_btn.setVisibility(View.INVISIBLE);
                }
            });
            back.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.performClick();
                    }
                }
            });
            refresh_btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.performClick();
                    }
                }
            });

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }catch (Exception e){

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();

        if(s.equals("messaging")){
            //Removing Msg Indicator
            if(AccountSettingsAdapter.seen_checker!=null&&AccountSettingsAdapter.seen_checker.size()>0)
                AccountSettingsAdapter.seen_checker.remove(friend_username);

            if(AccountSettingsAdapter.seen_checker!=null&&AccountSettingsAdapter.seen_checker.size()==0){
                //Setting Indicator
                Home_Screen.dummyTabLay.getTabAt(2).setIcon(new ColorDrawable(Color.parseColor("#00ffffff")));
                Needle.onBackgroundThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        Hawk.delete("msg_notif");
                    }
                });
            }

            //Generating Msg RoomKey
            ArrayList<String> roomUserList=new ArrayList<>();
            roomUserList.add(my_username);roomUserList.add(friend_username);
            Collections.sort(roomUserList, String.CASE_INSENSITIVE_ORDER);
            String key=roomUserList.get(0)+"+"+roomUserList.get(1);
            roomKey=key.replace(".","-");

            //setting header fullname and making refresh button visible
            refresh_btn.setVisibility(View.VISIBLE);
            userImgMsgRoom.setVisibility(View.VISIBLE);

            heading.setText("");
            if(getArguments().containsKey("searchFullname")) heading.setText(getArguments().getString("searchFullname"));
            else {
                ListItem item = new ListItem(friend_username, null, heading, "get_full_name", null, null, null, v.getContext());
            }

            //Setting Profpic of ToUser
            ListItem item=new ListItem(friend_username,userImgMsgRoom);


            msgAdp=new MessageAdapter(messages);
            mention_btn.setVisibility(View.GONE);
            commentPencilImg.setImageResource(R.drawable.send_vector);
            choose_img_btn.setVisibility(View.VISIBLE);
            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(600);
            comment.setFilters(FilterArray);
            rView.setAdapter(msgAdp);
           // msgAdp.setHasStableIds(true);
            LinearLayoutManager mng= new LinearLayoutManager(getActivity());
            mng.setStackFromEnd(true);


            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(0,getDp(1),0,getDp(20));
            params.addRule(RelativeLayout.ABOVE,commentBox.getId());
            params.addRule(RelativeLayout.BELOW,heading_view.getId());

            rView.setLayoutParams(params);

            rView.setLayoutManager(mng);
            ((SimpleItemAnimator) rView.getItemAnimator()).setSupportsChangeAnimations(false);

            //Getting Previous Msgs
            JSONObject ov=new JSONObject();
            try {
                ov.put("myMsgUsername",my_username);
                ov.put("friendMsgUsername",friend_username);
                ov.put("prevMsgRoomKey",roomKey);
                socket.emit("data",ov);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    JSONObject sendText = new JSONObject();
                    try{
                        sendText.put("fromRoom",my_username);
                        sendText.put("toRoom",friend_username);
                        sendText.put("thisRoomKey",roomKey);
                        socket.emit("data", sendText);
                    }catch(JSONException e){
                       e.printStackTrace();
                    }
                }
            }, 200);

            //OnClik Methods
            userImgMsgRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(v.getContext(),ProfileHolder.class);
                    Bundle b=new Bundle();
                    b.putString("searchUsername",friend_username);
                    i.putExtras(b);
                    i.putExtra("Open","search_profile");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(i);
                }
            });
            heading.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userImgMsgRoom.callOnClick();
                }
            });



            pencil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(comment.getText().toString().trim().equals("")){
                        Toast.makeText(v.getContext(),"Please Enter Some Text!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(getArguments().containsKey("new_message_search")) msg_counter++;

                        String message = comment.getText().toString().trim();
                        comment.setText("");

                        //Pushing the current chat to the top
                        try {
                            if(Messaging.lastMsgList.size()>0) {
                                JSONObject ob = new JSONObject(Messaging.lastMsgList.get(getArguments().getInt("position")));
                                ob.remove("msg");
                                ob.put("msg", message);

                                String username = Messaging.username.get(getArguments().getInt("position"));

                                if (Messaging.username.size() > 1) {
                                   Messaging.lastMsgList.remove(getArguments().getInt("position"));
                                   Messaging.username.remove(getArguments().getInt("position"));
                                   Messaging.username.add(0, username);
                                }
                                Messaging.lastMsgList.add(0, ob.toString());
                                Messaging.adapter.notifyDataSetChanged();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        seenText.setVisibility(View.GONE);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                        String time_format = simpleDateFormat.format(new Date());
                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                        JSONObject sendText = new JSONObject();
                        try{
                            sendText.put("msg",URLEncoder.encode(message, "UTF-8"));
                            sendText.put("from",my_username);
                            if(Hawk.get("myFullName")!=null)sendText.put("fromFullName",Hawk.get("myFullName"));
                            else sendText.put("fromFullName",sh.getString("myFullName",null));
                            sendText.put("to",friend_username);
                            sendText.put("msgTime",time_format);
                            sendText.put("roomKey", roomKey);
                            sendText.put("msg_type","text");

                            socket.emit("data", sendText);
                            if(msgAdp.getItemCount()>1) {
                                //Removing Seen Tag from last msg
                                RecyclerView.ViewHolder holder = rView.findViewHolderForAdapterPosition(msgAdp.getItemCount() - 1);
                                if (null != holder) {
                                    TextView tv = holder.itemView.findViewById(R.id.seenTxtMsg);
                                    tv.setVisibility(View.GONE);
                                }
                                Message msg = messages.get(msgAdp.getItemCount() - 1);
                                JSONObject ob = new JSONObject();
                                try {
                                    ob.put("remove_seen_roomKey", roomKey);
                                    ob.put("remove_seen_msg_owner_time", msg.getTime());
                                    socket.emit("data", ob);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            //adding current msg
                            addMessage(message,"me",null,time_format,null,null,null,null,null,null,"text");
                        }catch(Exception e){

                        }
                    }
                }
            });

            refresh_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    messages.clear();
                    msgAdp.notifyDataSetChanged();
                    rView.invalidate();
                    //Getting Previous Msgs
                    JSONObject ov=new JSONObject();
                    try {
                        ov.put("myMsgUsername",my_username);
                        ov.put("friendMsgUsername",friend_username);
                        ov.put("prevMsgRoomKey",roomKey);
                        socket.emit("data",ov);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            JSONObject sendText = new JSONObject();
                            try{
                                sendText.put("fromRoom",my_username);
                                sendText.put("toRoom",friend_username);
                                sendText.put("thisRoomKey",roomKey);
                                socket.emit("data", sendText);
                            }catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }, 200);
                }
            });
             //Gallery OnClick
            choose_img_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
                        @Override
                        public void run() {
                            Hawk.put("bookTitle", "no");
                        }
                    });
                    Intent i = new Intent(v.getContext(), ProfileHolder.class);
                    Bundle b = new Bundle();
                    b.putString("what", "msg_pic");
                    b.putString("my_username",my_username);
                    b.putString("friend_username",friend_username);
                    b.putString("roomKey",roomKey);
                    i.putExtras(b);
                    i.putExtra("Open", "chooser");
                    startActivity(i);
                }
            });
            //Sending Typing to the second Person
            comment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                  if(editable.toString().length()>1){
                    JSONObject ob=new JSONObject();
                      try {
                          ob.put("typing_stat","yes");
                          ob.put("typing_roomKey",roomKey);
                          socket.emit("data",ob);
                      } catch (JSONException e) {
                          e.printStackTrace();
                      }
                  }
                  else{
                      JSONObject ob=new JSONObject();
                      try {
                          ob.put("typing_stat","no");
                          ob.put("typing_roomKey",roomKey);
                          socket.emit("data",ob);
                      } catch (JSONException e) {
                          e.printStackTrace();
                      }
                  }
                }
            });

            //Sending ImgMsg
            if(msgImgLink!=null){
                //Pushing the current chat to the top
                try {
                    JSONObject ob=new JSONObject(Messaging.lastMsgList.get(getArguments().getInt("position")));
                    ob.remove("type");
                    ob.put("type","image");
                    String username=Messaging.username.get(getArguments().getInt("position"));

                    if(Messaging.username.size()>1) {
                        Messaging.lastMsgList.remove(getArguments().getInt("position"));
                        Messaging.username.remove(getArguments().getInt("position"));
                        Messaging.username.add(0,username);
                    }
                    Messaging.lastMsgList.add(0,ob.toString());
                    Messaging.adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AccountSettingsAdapter.adapter.notifyDataSetChanged();


                JSONObject ob=new JSONObject();
                try {
                    ob.put("msg_type","image");
                    ob.put("msg_img_link",msgImgLink);
                    ob.put("fromFullName",Hawk.get("myFullName"));
                    ob.put("from",my_username);
                    ob.put("to",friend_username);
                    ob.put("msgTime", ImageChooser.time_format);
                    ob.put("roomKey", roomKey);
                    // ob.put("msg_img", ImageCaption.getStringImage(resource));
                     socket.emit("data",ob);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(msgAdp.getItemCount()>1) {
                                //Removing Seen Tag from last msg
                                RecyclerView.ViewHolder holder = rView.findViewHolderForAdapterPosition(msgAdp.getItemCount() - 1);
                                if (null != holder) {
                                    TextView tv = holder.itemView.findViewById(R.id.seenTxtMsg);
                                    tv.setVisibility(View.GONE);
                                }
                                Message msg = messages.get(msgAdp.getItemCount() - 1);
                                JSONObject obi = new JSONObject();
                                try {
                                    obi.put("remove_seen_roomKey", roomKey);
                                    obi.put("remove_seen_msg_owner_time", msg.getTime());
                                    //socket.emit("data", obi);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },200);

                    socket.on("sent_pic_link_stat", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            JSONObject ob=(JSONObject)args[0];

                            Needle.onMainThread().execute(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        if(ob.getString("status").equals("yes")) {
                                            addMessage(null, "me", null, ImageChooser.time_format, msgImgLink, null, null,
                                                    null, null, null, "image");
                                            msgImgLink = null;
                                            ImageChooser.time_format = null;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    });


                }catch (JSONException e){
                    e.printStackTrace();
                }
            }


        }


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);// here i have fragment height 30% of window's height you can set it as per your requirement
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
       // getDialog().getWindow().getAttributes().windowAnimations = android.R.style.DialogAnimationUpDown;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case 1919:
                    if (resultCode == Activity.RESULT_OK) {
                        Bundle bundle = data.getExtras();
                        ArrayList<String> tag = bundle.getStringArrayList("selectedTags");
                        for (int i = 0; i < tag.size(); i++) {
                            comment.setText(comment.getText() + tag.get(i) + " ");
                        }
                        // Showning Keyboard
                        new Handler().postDelayed(new Runnable() {

                            public void run() {
                                comment.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                                comment.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                            }
                        }, 200);

                    }
                    break;
            }

    }



    private void addMessage(String message,String user,String username,String msg_time,String img_link,
                            String msg_post_owner_username,String msg_post_owner_time,
                            String msg_post_img_link,String msg_post_type,String msg_post_multi,String msg_type) {

        boolean isRepeated = false;
        for (Message msg:messages) {

            if (msg.getTime().equals(msg_time)) {
                isRepeated = true;
            }
        }
        if (!isRepeated) {
            messages.add(new Message(message,user,username,msg_time,img_link,msg_post_owner_username,
                    msg_post_owner_time,msg_post_img_link,msg_post_type,msg_post_multi,msg_type));
        }

        rView.postDelayed(new Runnable() {
            @Override
            public void run() {
                rView.scrollToPosition(msgAdp.getItemCount() - 1);
            }
        }, 50);

        msgAdp.notifyDataSetChanged();



    }
    private void addComment(String username,String tim,String content,String fullname,String profile_pic) {
        settings.add(content);//content
        username_list.add(username);
        fullname_list.add(fullname);
        profile_pic_list.add(profile_pic);
        time.add(tim);
        //Toast.makeText(v.getContext(),time.toString(),Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
        rView.scrollToPosition(adapter.getItemCount() - 1);
    }
    private Emitter.Listener handleLikeList = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray data = ( JSONArray) args[0];
                        for(int i=0;i<data.length();i++){
                            JSONObject ob=data.getJSONObject(i);
                            fullname_list.add(ob.getString("liker_fullname"));
                            username_list.add(ob.getString("liker_username"));
                            profile_pic_list.add(ob.getString("liker_profPic"));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        // return;
                        e.printStackTrace();

                    }

                }
            });
        }
    };
    private Emitter.Listener handleCommentLikeList = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray data = ( JSONArray) args[0];

                        username_list.clear();
                        fullname_list.clear();
                        for(int i=0;i<data.length();i++){
                            JSONObject ob=data.getJSONObject(i);
                            fullname_list.add(ob.getString("comment_liker_fullname"));
                            username_list.add(ob.getString("comment_liker_username"));
                            profile_pic_list.add(ob.getString("comment_liker_profPic"));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        // return;
                        e.printStackTrace();

                    }

                }
            });
        }
    };
    private Emitter.Listener handleTyping = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        JSONObject data = ( JSONObject) args[0];

                       // Toast.makeText(v.getContext(),data.toString(),Toast.LENGTH_SHORT).show();

                        String typing_stat = data.getString("typing_stat");
                        if(typing_stat.equals("yes")) {
                            Animation fade_btn=AnimationUtils.loadAnimation(v.getContext(),R.anim.type_anim);

                            typingTxt.startAnimation(fade_btn);
                            typingTxt.setVisibility(View.VISIBLE);
                        }
                        else {
                            Animation fade_btn=AnimationUtils.loadAnimation(v.getContext(),R.anim.type_anim);
                            fade_btn.setInterpolator(new ReverseInterpolator());

                            typingTxt.startAnimation(fade_btn);
                            typingTxt.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        // return;
                        e.printStackTrace();

                    }

                }
            });
        }
    };
    private Emitter.Listener handleSeen = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = ( JSONObject) args[0];
                        String send = data.getString("seen_stat");
                        if(send.equals("seen")){
                            rView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    RecyclerView.ViewHolder holder = rView.findViewHolderForAdapterPosition(msgAdp.getItemCount()-1);
                                    if (null != holder) {
                                        TextView tv=holder.itemView.findViewById(R.id.seenTxtMsg);
                                        tv.setVisibility(View.VISIBLE);
                                    }
                                    if(msgAdp.getItemCount()>1) {
                                        //Making last msg seen invisible
                                        Message msg=messages.get(msgAdp.getItemCount()-2);
                                        JSONObject ob=new JSONObject();
                                        try {
                                            ob.put("remove_seen_roomKey",roomKey);
                                            ob.put("remove_seen_msg_owner_time",msg.getTime());
                                            socket.emit("data",ob);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        RecyclerView.ViewHolder last_holder = rView.findViewHolderForAdapterPosition(msgAdp.getItemCount() - 2);
                                        if (null != last_holder) {
                                            TextView tv = last_holder.itemView.findViewById(R.id.seenTxtMsg);
                                            tv.setVisibility(View.GONE);
                                        }
                                    }
                                    rView.scrollToPosition(msgAdp.getItemCount() - 1);
                                }
                            }, 50);
                          //seenText.setVisibility(View.VISIBLE);
                        }

                    } catch (Exception e) {
                        // return;
                        e.printStackTrace();

                    }

                }
            });
        }
    };
    private Emitter.Listener handleCurrentMsg=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data=(JSONObject)args[0];

            Needle.onMainThread().execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        //seenText.setVisibility(View.GONE);

                        from=data.getString("from");
                        to=data.getString("to");
                        msg_time=data.getString("msgTime");
                        msg_type=data.getString("msg_type");

                        if(msgAdp.getItemCount()>1) {
                            //Removing Seen Tag from last msg
                            RecyclerView.ViewHolder holder = rView.findViewHolderForAdapterPosition(msgAdp.getItemCount() - 1);
                            if (null != holder) {
                                TextView tv = holder.itemView.findViewById(R.id.seenTxtMsg);
                                tv.setVisibility(View.GONE);
                            }
                            Message msg = messages.get(msgAdp.getItemCount() - 1);
                            JSONObject ob = new JSONObject();
                            try {
                                ob.put("remove_seen_roomKey", roomKey);
                                ob.put("remove_seen_msg_owner_time", msg.getTime());
                                socket.emit("data", ob);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if(msg_type.equals("text")){
                            String message = data.getString("msg");
                            addMessage(message, "friend",from,msg_time,null,
                                    null,null,null,null,null,msg_type);
                        }
                        else if(msg_type.equals("image")){
                            String img_link = data.getString("msg_img_link");
                            addMessage(null, "friend",from,msg_time,img_link,
                                    null,null,null,null,null,msg_type);
                        }


                        //For Seen
                        JSONObject ov=new JSONObject();
                        ov.put("seen_stat","seen");
                        ov.put("seen_roomKey",roomKey);
                        ov.put("seen_msg_owner_time",msg_time);
                         socket.emit("data",ov);

                        //Making Last Seen Tag Invisible
                        if(msgAdp.getItemCount()>1) {
                            //Making last msg seen invisible
                            Message msg=messages.get(msgAdp.getItemCount()-2);
                            JSONObject ob=new JSONObject();
                            try {
                                ob.put("remove_seen_roomKey",roomKey);
                                ob.put("remove_seen_msg_owner_time",msg.getTime());
                                socket.emit("data",ob);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            RecyclerView.ViewHolder last_holder = rView.findViewHolderForAdapterPosition(msgAdp.getItemCount() - 2);
                            if (null != last_holder) {
                                TextView tv = last_holder.itemView.findViewById(R.id.seenTxtMsg);
                                tv.setVisibility(View.GONE);
                            }
                        }
                        socket.emit("data",ov);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener handlePreviousMessages = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray data = ( JSONArray) args[0];

                        messages.clear();
                        seenText.setVisibility(View.GONE);
                        int t=0;
                        for(int i=0;i<data.length();i++){
                            JSONObject ob=data.getJSONObject(i);
                           // String my_changed_username=my_username.replaceAll("\\.","-");

                            from=ob.getString("from");
                            to=ob.getString("to");
                            msg_time=ob.getString("msg_time");
                            msg_type=ob.getString("type");
                            if(msg_type.equals("text")){
                                msg=ob.getString("msg");
                                img_link=null;
                            }
                            else if(msg_type.equals("post")){
                                msg_post_owner_username=ob.getString("post_owner_username");
                                msg_post_owner_time=ob.getString("post_owner_time");
                                msg_post_img_link=ob.getString("post_img_link");
                                msg_post_multi=ob.getString("post_multi_stat");
                                msg_post_type=ob.getString("post_type");
                            }
                            else{
                                img_link=ob.getString("msg_img_link");
                                msg=null;
                            }

                            if(from.equals(my_username)) {
                          addMessage(msg, "me", null, msg_time,img_link,
                                  msg_post_owner_username,msg_post_owner_time,msg_post_img_link,msg_post_multi,msg_post_type,msg_type);

                                //Checking Seen Stat of Msg
                                if(i==data.length()-1) {
                                    if(ob.has("seen")){
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                RecyclerView.ViewHolder holder = rView.findViewHolderForAdapterPosition(msgAdp.getItemCount()-1);
                                                if (null != holder) {
                                                    TextView tv=holder.itemView.findViewById(R.id.seenTxtMsg);
                                                    tv.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        },100);

                                    }
                                }
                            }

                            else{

                                if(i==data.length()-1){
                                    //For Seen
                                    JSONObject ov=new JSONObject();
                                    ov.put("seen_stat","seen");
                                    ov.put("seen_roomKey",roomKey);
                                    ov.put("seen_msg_owner_time",msg_time);

                                    socket.emit("data",ov);
                                }
                                addMessage(msg,"friend",from,msg_time,img_link,
                                        msg_post_owner_username,msg_post_owner_time,msg_post_img_link,msg_post_multi,msg_post_type,msg_type);
                            }
                        }

          } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };
    private  Emitter.Listener handleIncomingComments = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    settings.clear();//content
                    username_list.clear();
                    fullname_list.clear();
                    profile_pic_list.clear();
                    time.clear();
                    //JSONObject d=(JSONObject) args[1]
                    try {
                        JSONArray data=(JSONArray) args[0];

                        for(int i=0;i<data.length();i++){
                           JSONObject ob=data.getJSONObject(i);
                            String st=ob.getString("content");
                            String ll=ob.getString("commenter_fullname");
                            String mm=ob.getString("commenter_profPic");
                            String ss=ob.getString("commenter_username");
                            String tt=ob.getString("comment_time");
                            addComment(ss,tt,st,ll,mm);
                        }

                    } catch (Exception e) {
                        Log.e("error",e.toString());
                    }

                }
            });
        }
    };
    private  Emitter.Listener handleFollowers = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            //  Toast.makeText(v.getContext(),"Hello India",Toast.LENGTH_LONG).show();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //JSONObject d=(JSONObject) args[1]
                    try {
                        JSONArray data=(JSONArray) args[0];
                        // String st=data.getString("comments");
                        //JSONArray arr=data.getJSONArray("content");

                        for(int i=0;i<data.length();i++) {
                            JSONObject ob = data.getJSONObject(i);
                            String ss = ob.getString("follower_username");
                            username_list.add(ss);
                            //adapter.notifyDataSetChanged();
                        }
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("error",e.toString());
                    }

                }
            });
        }
    };
    private  Emitter.Listener handleFollowing = new Emitter.Listener(){

        @Override
        public void call(final Object... args){

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //JSONObject d=(JSONObject) args[1]
                    try {
                        JSONArray data=(JSONArray) args[0];
                        // String st=data.getString("comments");
                        //JSONArray arr=data.getJSONArray("content");

                        for(int i=0;i<data.length();i++){
                            JSONObject ob=data.getJSONObject(i);
                            String st=ob.getString("following_username");
                            username_list.add(st);
                            //adapter.notifyDataSetChanged();
                        }
                        adapter.notifyDataSetChanged();


                    } catch (Exception e) {
                        Log.e("error",e.toString());
                    }

                }
            });
        }
    };
    private  Emitter.Listener handleSearch = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String ss,wall_pic,society,bio;
                    //JSONObject d=(JSONObject) args[1]
                    try {
                        JSONArray data=(JSONArray) args[0];
                        Set<String> tags = new LinkedHashSet<>();
                        // String st=data.getString("comments");
                        //JSONArray arr=data.getJSONArray("content");

                            if(data.length()==0){
                                noResultTxt.setVisibility(View.VISIBLE);
                            }
                            else {
                                noResultTxt.setVisibility(View.GONE);
                            }
                           if(s.equals("tag_search")) {
                                time.clear();
                               tags.clear();
                           }

                           else{
                               username_list.clear();
                               fullname_list.clear();
                               profile_pic_list.clear();
                               wall_pic_list.clear();
                               bio_list.clear();
                               society_list.clear();
                           }
                        for(int i=0;i<data.length();i++){
                            JSONObject ob=data.getJSONObject(i);
                            if(s.equals("tag_search")){
                                String st=ob.getString("_tag");
                                tags.add(st);//avoiding insistent tags
                            }
                            else {
                                String uu=ob.getString("username");
                                String tt=ob.getString("fullname");
                                if(ob.has("profile_pic")) ss=ob.getString("profile_pic");
                                else ss="";
                                if(ob.has("wall_pic")) wall_pic=ob.getString("wall_pic");
                                else wall_pic="";
                                if(ob.has("bio")) bio=ob.getString("bio");
                                else bio="";
                                if(ob.has("society_list")) society=ob.getString("society_list");
                                else society="";

                                username_list.add(uu);
                                fullname_list.add(tt);
                                profile_pic_list.add(ss);
                                wall_pic_list.add(wall_pic);
                                bio_list.add(bio);
                                society_list.add(society);

                            }

                        }
                        if(s.equals("tag_search")) {
                            time.addAll(tags);
                            adapter.notifyDataSetChanged();
                        }

                        else  adapter.notifyDataSetChanged();


                    } catch (Exception e) {
                        Log.e("error",e.toString());
                    }

                }
            });
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(s.equals("messaging")) {

            if(getArguments().containsKey("new_message_search")&&msg_counter>0)
                Messaging.refresh();//Updating the chat list

            JSONObject ov = new JSONObject();
            try {
                ov.put("del_myMsgUsername", my_username);
                ov.put("del_friendMsgUsername", friend_username);
                ov.put("del_roomKey", roomKey);

                socket.emit("data", ov);
            } catch (JSONException ei) {
                ei.printStackTrace();
            }
        }

        socket.disconnect();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
       if(searchBox.getText().toString().contains("#")) tabLayout.getTabAt(1).select();
       if(tab.getPosition()==1) {
           if(!searchBox.getText().toString().contains("#")){
               String s=searchBox.getText().toString();
               searchBox.setText("");
               searchBox.setText("#"+s);
               if(searchBox.getText().toString().length()>0) searchBox.setSelection(searchBox.getText().toString().length());
           }
       }
       else{
           String s=searchBox.getText().toString();
           searchBox.setText("");
           searchBox.setText(s.replaceAll("#",""));
          if(searchBox.getText().toString().length()>0) searchBox.setSelection(searchBox.getText().toString().length());
       }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
    private int getDp(int px){
        float d = v.getContext().getResources().getDisplayMetrics().density;
        int dp= (int) (px * d);
        return dp;
    }
}