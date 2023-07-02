package community.infinity.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.FragmentManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import community.infinity.BottomFrags.BottomFrag;
import community.infinity.ListItem;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.SettingsMenu.AccountSettingsFrag;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.Home_Screen;
import community.infinity.activities.ProfileHolder;
import community.infinity.R;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.HashTagHelper;
import custom_views_and_styles.ReverseInterpolator;
import custom_views_and_styles.ToggleButton;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import layout.Messaging;
import needle.Needle;
import ooo.oxo.library.widget.TouchImageView;

/**
 * Created by Srinu on 10-10-2017.
 */

public class AccountSettingsAdapter  extends RecyclerView.Adapter<AccountSettingsAdapter.AccViewHolder> {

    private String show,myUsername;
    private Animation an;
    private int lastPosition = -1;
    private ArrayList<String> fullname,profile_pic_list,wall_pic_list,bio_list,society_list;
    public static AccountSettingsAdapter adapter;
    public ArrayList<String> username=new ArrayList<>();
    public ArrayList<String> time=new ArrayList<>();
    public ArrayList<String>settings=new ArrayList<>();
    private ArrayList<String> convict_name=new ArrayList<>();

    public ArrayList<String> roomKeyList=new ArrayList<>();
    public ArrayList<String> comm_username=new ArrayList<>();
    public ArrayList<String> comm_time=new ArrayList<>();
    public ArrayList<String> comm_content=new ArrayList<>();
    private ArrayList<String> report_comnt_post_owner_name=new ArrayList<>();
    private ArrayList<String> report_comnt_post_owner_time=new ArrayList<>();

    private ListItem item;//It's used
    private FragmentManager manager;
    public static ArrayList<String> seen_checker=new ArrayList<>();
    private String postOwner,postTime,postMulti,postImgLink,postType,post_download_stat;
    private AlertDialog dialog;
    private TextView comments_counter;
    private EditText comment_editTxt;
    private  boolean tagSelection;
    private ViewGroup parent;
    private Socket socket;
    {
        try{
           // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    //For settings & society list menu
    public AccountSettingsAdapter(ArrayList<String> settings,ArrayList<String> username,ArrayList<String> time,String show,FragmentManager fm) {
        this.settings=settings;
        this.show=show;
        this.username=username;
        this.time=time;
        this.manager=fm;
        adapter=this;
    }
    //For Reports
    public AccountSettingsAdapter(ArrayList<String> report_type,ArrayList<String> reporter_name,
                                  ArrayList<String> report_of,ArrayList<String> time,ArrayList<String> comnt_post_owner_name,
                                  ArrayList<String> comnt_post_owner_time,String show,FragmentManager fm) {
        this.settings=report_type;
        this.show=show;
        this.username=reporter_name;
        this.convict_name=report_of;
        this.time=time;
        this.report_comnt_post_owner_name=comnt_post_owner_name;
        this.report_comnt_post_owner_time=comnt_post_owner_time;
        this.manager=fm;
    }
    //For Comments
    public AccountSettingsAdapter(ArrayList<String> content, ArrayList<String> username, ArrayList<String> time,ArrayList<String> fullname,
                                  ArrayList<String> profile_pic_list,String show, String post_owner, String post_time, TextView comments_counter,
                                  FragmentManager manager, EditText comment_editTxt) {
        this.comm_content=content;
        this.show=show;
        this.comm_username=username;
        this.fullname=fullname;
        this.profile_pic_list=profile_pic_list;
        this.comm_time=time;
        this.postOwner=post_owner;
        this.postTime=post_time;
        this.manager=manager;
        this.comments_counter=comments_counter;
        this.comment_editTxt=comment_editTxt;
    }
    //Search
    public AccountSettingsAdapter(ArrayList<String> username,ArrayList<String> fullname,ArrayList<String> profile_pic_list,
                                  ArrayList<String> wall_pic_list,ArrayList<String> bio_list,ArrayList<String> society_list,
                                  String show,boolean tagSelection){
        this.username=username;
        this.fullname=fullname;
        this.profile_pic_list=profile_pic_list;
        this.wall_pic_list=wall_pic_list;
        this.society_list=society_list;
        this.bio_list=bio_list;
        this.show=show;
        this.tagSelection=tagSelection;
    }
    //NewMsg Search
    public AccountSettingsAdapter(ArrayList<String> username,ArrayList<String> fullname,ArrayList<String> profile_pic_list,
                                  String show,FragmentManager fm,boolean tagSelection){
        this.username=username;
        this.fullname=fullname;
        this.profile_pic_list=profile_pic_list;
        this.show=show;
        this.manager=fm;
        this.tagSelection=tagSelection;
    }
    // TagSelection
    public AccountSettingsAdapter(ArrayList<String> settings, ArrayList<String> username,ArrayList<String> fullname,String show,boolean tagSelection){
        this.settings=settings;//here ProfilePicLinks
        this.username=username;
        this.fullname=fullname;
        this.show=show;
        this.tagSelection=tagSelection;
    }
    //PostMsg
    public AccountSettingsAdapter(ArrayList<String> username,ArrayList<String> fullname,ArrayList<String> profile_pic_list,String show,
                                  String postOwnerUsername,String postOwnerTime,String postImgLink,
                                  String postMulti,String postType,String post_download_stat){
        this.username=username;
        this.fullname=fullname;
        this.profile_pic_list=profile_pic_list;
        this.show=show;
        this.postOwner=postOwnerUsername;
        this.postTime=postOwnerTime;
        this.postImgLink=postImgLink;
        this.postMulti=postMulti;
        this.postType=postType;
        this.post_download_stat=post_download_stat;
    }
    @NonNull
    @Override
    public AccViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       // socket.connect();
        this.parent=parent;
        Hawk.init(parent.getContext()).build();

        if(Hawk.get("myUserName")!=null){
            myUsername=Hawk.get("myUserName");
        }
        else{
            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(parent.getContext());
            myUsername=sh.getString("username",null);
        }
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.design_of_acc_settings,parent,false);
        return new AccViewHolder (view);
    }

    class AccViewHolder extends RecyclerView.ViewHolder{
        private TextView contents,fullname,like_counter,reply,comm_time;
        private AppCompatImageButton like_outline,liked;
        private CircleImageView profile_pic;
        private RelativeLayout rl;
        private View margin;
        private Button unblock;
        private ToggleButton toggleSwitch;
        public AccViewHolder (View v) {
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
            toggleSwitch=v.findViewById(R.id.toggleSwitch);
            unblock=v.findViewById(R.id.unblock_btn_acc_sett);
            comm_time=v.findViewById(R.id.timeCom);


            final Typeface bold_font = Typeface.createFromAsset((fullname.getContext()).getAssets(), "fonts/"+"Lato-Bold.ttf");
            final Typeface reg_font = Typeface.createFromAsset((fullname.getContext()).getAssets(), "fonts/"+"Lato-Regular.ttf");
            fullname.setTypeface(bold_font);

            Shader textShader=new LinearGradient(0, 0, 0, 45,
                    new int[]{Color.parseColor("#43cea2"),Color.parseColor("#185a9d")},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            //fullname.getPaint().setShader(textShader);

            like_counter.setTypeface(reg_font);
            contents.setTypeface(reg_font);
            reply.setTypeface(reg_font);


            ButtonTint tint=new ButtonTint("white");
            tint.setTint(rl);
            //contents.setTypeface(EasyFonts.droidSerifRegular( parent.getContext()));

        }
        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
                if (position > lastPosition) {
                    Animation animation = AnimationUtils.loadAnimation( parent.getContext(), R.anim.slide_up);
                    viewToAnimate.startAnimation(animation);
                    lastPosition = position;
                }

        }

    }
    @Override
    public void onBindViewHolder(AccViewHolder hold,int position) {
        try {
            an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.fade_buttons);
            final Animation heart_ani = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
            hold.contents.invalidate();
            if (settings != null && show.equals("settings")) {

                //Setting Settings Menu


                final String s = settings.get(hold.getAdapterPosition());
                hold.contents.setText(s);
                if (s.equals("General")) {
                    if (!myUsername.equals("infinity")) {
                        hold.rl.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AccountSettingsFrag.changeInfo();
                            }
                        });
                    }

                } else if (s.equals("Change Password")) {
                    hold.rl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AccountSettingsFrag.changePassword();
                        }
                    });

                } else if (s.equals("Block List")) {
                    hold.rl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AccountSettingsFrag.showBlockList();
                        }
                    });
                } else if (s.equals("Make Account Private")) {
                    hold.toggleSwitch.setVisibility(View.VISIBLE);

                    //Checking Account is private or not
                    socket.disconnect();
                    socket.connect();
                    JSONObject ob = new JSONObject();
                    try {
                        ob.put("check_acc_private_username", myUsername);
                        socket.emit("data", ob);
                        socket.on("check_private_stat", new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                JSONObject ob = (JSONObject) args[0];
                                try {
                                    if (ob.getString("status").equals("yes")) {
                                        Needle.onMainThread().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                hold.toggleSwitch.setToggleOn();
                                            }
                                        });
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
                    //Toggling switch on click of container
                    hold.rl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            socket.disconnect();
                            socket.connect();
                            if (hold.toggleSwitch.isToggleOn()) {
                                hold.toggleSwitch.setToggleOff();
                                // Toast.makeText( parent.getContext(), "It's Off", Toast.LENGTH_SHORT).show();
                                JSONObject ob = new JSONObject();
                                try {
                                    ob.put("remove_acc_private_username", myUsername);
                                    socket.emit("data", ob);
                                    socket.on("remove_private_set_stat", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            JSONObject ob = (JSONObject) args[0];
                                            try {
                                                if (ob.getString("status").equals("yes")) {
                                                    socket.disconnect();
                                                    Needle.onMainThread().execute(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(parent.getContext(), "Your account is public now.", Toast.LENGTH_SHORT).show();
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

                            } else {
                                hold.toggleSwitch.setToggleOn();
                                // Toast.makeText( parent.getContext(), "It's On", Toast.LENGTH_SHORT).show();
                                JSONObject ob = new JSONObject();
                                try {
                                    ob.put("make_acc_private_username", myUsername);
                                    socket.emit("data", ob);
                                    socket.on("private_set_stat", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            JSONObject ob = (JSONObject) args[0];
                                            try {
                                                if (ob.getString("status").equals("yes")) {
                                                    socket.disconnect();
                                                    Needle.onMainThread().execute(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(parent.getContext(), "Your account is private now.", Toast.LENGTH_SHORT).show();
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
                        }
                    });
                    hold.toggleSwitch.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
                        @Override
                        public void onToggle(boolean isOn) {
                            socket.disconnect();
                            socket.connect();
                            if (isOn) {

                                JSONObject ob = new JSONObject();
                                try {
                                    ob.put("make_acc_private_username", myUsername);
                                    socket.emit("data", ob);
                                    socket.on("private_set_stat", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            JSONObject ob = (JSONObject) args[0];
                                            try {
                                                if (ob.getString("status").equals("yes")) {
                                                    socket.disconnect();
                                                    Needle.onMainThread().execute(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(parent.getContext(), "Your account is private now.", Toast.LENGTH_SHORT).show();
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

                            } else {
                                JSONObject ob = new JSONObject();
                                try {
                                    ob.put("remove_acc_private_username", myUsername);
                                    socket.emit("data", ob);
                                    socket.on("remove_private_set_stat", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            JSONObject ob = (JSONObject) args[0];
                                            try {
                                                if (ob.getString("status").equals("yes")) {
                                                    socket.disconnect();
                                                    Needle.onMainThread().execute(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(parent.getContext(), "Your account is public now.", Toast.LENGTH_SHORT).show();
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
                        }
                    });
                } else if (s.equals("About Us")) {
                    hold.rl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AccountSettingsFrag.aboutPage();
                        }
                    });

                } else if (s.equals("Privacy Policy")) {
                    hold.rl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent viewIntent =
                                    new Intent("android.intent.action.VIEW",
                                            Uri.parse("https://medium.com/@srinivas.nahak/privacy-policy-3b0e582e21a2"));
                            hold.rl.getContext().startActivity(viewIntent);
                        }
                    });

                }

            } else if (settings != null && show.equals("triple_dot")) {
                String s = settings.get(hold.getAdapterPosition());
                hold.contents.setText(s);
                hold.margin.setVisibility(View.GONE);
                hold.contents.setTextColor(Color.WHITE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(45));
                hold.contents.setTextSize(15);
                hold.rl.setLayoutParams(params);
                hold.rl.setBackgroundColor(Color.TRANSPARENT);

                //setting Tint
                ButtonTint tint = new ButtonTint("white");
                tint.setTint(hold.rl);
            }

            // hold.time.setText(time.get(position));
            //Showing Comments
            else if (show.equals("comments")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.reply.setVisibility(View.VISIBLE);
                hold.like_outline.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.comm_time.setVisibility(View.VISIBLE);
                hold.margin.setVisibility(View.GONE);

                String s = comm_content.get(hold.getAdapterPosition());


                try {
                    hold.contents.setText(URLDecoder.decode(s, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //showing time
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                try {
                    Date date = simpleDateFormat.parse(comm_time.get(hold.getAdapterPosition()));
                    long time = date.getTime();
                    String timeStamp = CustomRecyclerViewAdapter.getTimeAgo(time);
                    if (timeStamp.contains("ago"))
                        hold.comm_time.setText(CustomRecyclerViewAdapter.getTimeAgo(time).replace("ago", ""));
                    else hold.comm_time.setText(timeStamp);

                } catch (ParseException e) {
                    e.printStackTrace();
                }


                //Making Reply Text Clickable & Bold
                hold.reply.setTypeface(null, Typeface.BOLD);
                hold.reply.setClickable(true);
                hold.reply.setFocusable(true);

                char[] additionalSymbols = new char[]{'&', '.', '_'};

                HashTagHelper mTextHashTagHelper = HashTagHelper.Creator.create(parent.getContext().getResources().getColor(R.color.tagColor), new HashTagHelper.OnHashTagClickListener() {
                    @Override
                    public void onHashTagClicked(String hashTag, char sign) {
                        if (sign == '@') {
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle b = new Bundle();
                            b.putString("searchUsername", hashTag);
                            i.putExtras(b);
                            i.putExtra("Open", "search_profile");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);
                        }
                        if (sign == '#') {
                            //Passing Intent
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle b = new Bundle();
                            b.putString("what", "tags");
                            b.putString("tag_searched", "#" + hashTag);
                            i.putExtras(b);
                            i.putExtra("Open", "starred");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);
                        }
                        //Toast.makeText( parent.getContext(),hashTag,Toast.LENGTH_SHORT).show();
                    }
                }, additionalSymbols);
                mTextHashTagHelper.handle(hold.contents);

                //Setting Fullname
                hold.fullname.setText(this.fullname.get(hold.getAdapterPosition()));


                ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));
                if (profile_pic_list.get(position).equals("")) {
                    hold.profile_pic.setImageResource(R.drawable.profile);
                } else {
                    Glide.with(parent.getContext()).load(profile_pic_list.get(position)).
                            apply(new RequestOptions().override(50, 50).placeholder(cd).diskCacheStrategy(DiskCacheStrategy.NONE).
                                    error(R.drawable.profile).skipMemoryCache(true)).thumbnail(0.1f).into(hold.profile_pic);
                }

                hold.setAnimation(hold.rl, hold.getAdapterPosition());


                //Setting Clickable
                hold.profile_pic.setClickable(true);
                hold.fullname.setClickable(true);
                hold.rl.setClickable(true);
                hold.rl.setFocusable(true);


                //Onclick Methods
                hold.profile_pic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        hold.rl.callOnClick();
                        return true;
                    }
                });
                hold.fullname.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        hold.rl.callOnClick();
                        return true;
                    }
                });
                hold.contents.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        hold.rl.callOnClick();
                        return true;
                    }
                });
                hold.reply.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        hold.rl.callOnClick();
                        return true;
                    }
                });
                hold.rl.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        hold.rl.callOnClick();
                        return true;
                    }
                });

                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                        final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_image, null);
                        RelativeLayout mainCont = vi.findViewById(R.id.dialogImgRelLay);
                        RecyclerView fontList = vi.findViewById(R.id.fontRecycler);
                        TouchImageView img = vi.findViewById(R.id.dialog_image);
                        img.setVisibility(View.GONE);
                        fontList.setVisibility(View.VISIBLE);

                        //Setting Wrap Content
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        mainCont.setLayoutParams(params);
                        mainCont.setBackgroundResource(R.drawable.bg_gradient);


                        final ArrayList<String> name = new ArrayList<>();
                        try{

                            if (comm_username.get(hold.getAdapterPosition()).equals(myUsername)|| postOwner.equals(myUsername)||
                                    myUsername.equals("infinity"))

                                name.add("Delete");
                            name.add("Report");
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }


                        AccountSettingsAdapter adapter = new AccountSettingsAdapter(name, null, null, "triple_dot", null);
                        adapter.setHasStableIds(true);
                        fontList.setHasFixedSize(true);
                        fontList.setLayoutManager(new LinearLayoutManager(parent.getContext()));
                        fontList.setAdapter(adapter);

                        //Onclick Listener
                        fontList.addOnItemTouchListener(new RecyclerItemClickListener(parent.getContext(), fontList, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if (name.get(position).equals("Delete")) {
                                    dialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                                    final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.delete_dialog, null);
                                    //Dialog Properties////////
                                    AppCompatImageButton ok = vi.findViewById(R.id.okDelete);
                                    AppCompatImageButton cancel = vi.findViewById(R.id.cancelDelete);


                                    //Setting Tint
                                    ButtonTint tint = new ButtonTint("white");
                                    tint.setTint(ok);
                                    tint.setTint(cancel);

                                    socket.disconnect();
                                    socket.connect();


                                    //Onclick
                                    ok.setOnClickListener(view1 -> {
                                        //Getting usernames
                                        final Animation rev_an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                                        rev_an.setInterpolator(new ReverseInterpolator());

                                        JSONObject ob = new JSONObject();
                                        try {
                                            ob.put("comment_del_time", comm_time.get(hold.getAdapterPosition()));
                                            ob.put("comment_del_commenter", comm_username.get(hold.getAdapterPosition()));
                                            ob.put("comment_del_owner_post_time", postTime);
                                            ob.put("comment_del_post_owner", postOwner);

                                                    socket.emit("data", ob);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                        hold.itemView.startAnimation(rev_an);

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (hold.getAdapterPosition() != RecyclerView.NO_POSITION) {
                                                    comm_username.remove(hold.getAdapterPosition());
                                                    comm_content.remove(hold.getAdapterPosition());
                                                    notifyItemRemoved(hold.getAdapterPosition());
                                                    notifyItemRangeChanged(hold.getAdapterPosition(), comm_username.size());
                                                }
                                            }
                                        }, 200);


                                        dialog.dismiss();
                                        socket.on("comment_del_status", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject obj = (JSONObject) args[0];
                                                Needle.onMainThread().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            if (obj.getString("status").equals("yes")) {
                                                                hold.rl.startAnimation(rev_an);
                                                                socket.disconnect();
                                                                //mMessages.removeAll(templist);


                                                                if (comments_counter.getText().equals("1 Comment"))
                                                                    comments_counter.setVisibility(View.GONE);
                                                                else {
                                                                    int i = Integer.valueOf(comments_counter.getText().toString().substring(0, comments_counter.getText().toString().indexOf(" "))) - 1;
                                                                    if (i == 1)
                                                                        comments_counter.setText(i + " Comment");
                                                                    else if (i > 0)
                                                                        comments_counter.setText(i + " Comments");
                                                                    else
                                                                        comments_counter.invalidate();
                                                                }
                                                                // MessageAdapter.this.notifyDataSetChanged();
                                                                //}
                                                                //
                                                            } else {
                                                                Toast.makeText(parent.getContext(), "Sorry some error occured.Please try again.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });


                                            }
                                        });
                                    });
                                    cancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                            socket.disconnect();
                                        }
                                    });
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
                                    dialog.getWindow().setDimAmount(0.3f);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                } else if(name!=null&&name.get(position)!=null&&name.get(position).equals("Report")){
                                    Needle.onBackgroundThread().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            socket.disconnect();
                                            socket.connect();

                                            JSONObject ob = new JSONObject();
                                            try {
                                                ob.put("report_reporter_name", myUsername);
                                                ob.put("report_comment_time", comm_time.get(hold.getAdapterPosition()));
                                                ob.put("report_comment_owner_name", comm_username.get(hold.getAdapterPosition()));
                                                ob.put("report_comment_owner_post_time", postTime);
                                                ob.put("report_comment_post_owner", postOwner);
                                                ob.put("report_type", "comments");
                                                socket.emit("data", ob);
                                                socket.on("report_stat", new Emitter.Listener() {
                                                    @Override
                                                    public void call(Object... args) {
                                                        JSONObject ob = (JSONObject) args[0];
                                                        try {
                                                            if (ob.getString("status").equals("yes")) {
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
                                            Toast.makeText(parent.getContext(), "We would check your report very soon .", Toast
                                                    .LENGTH_SHORT).show();
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
                        dialog.getWindow().setDimAmount(0.3f);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        /////////////////////////
                    }
                });
                hold.profile_pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hold.fullname.callOnClick();
                    }
                });
                hold.fullname.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                        Bundle b = new Bundle();
                        b.putString("searchUsername", comm_username.get(hold.getAdapterPosition()));

                        i.putExtras(b);
                        i.putExtra("Open", "search_profile");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.getContext().startActivity(i);
                    }
                });
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        //Checking Liked by the current user or  not
                        item = new ListItem(postOwner, postTime, comm_username.get(hold.getAdapterPosition()),
                                comm_time.get(hold.getAdapterPosition()), hold.like_outline, hold.liked, null, myUsername, "comment_check_like");


                        //Getting Number of likes
                        item = new ListItem(postOwner, postTime, comm_username.get(hold.getAdapterPosition()),
                                comm_time.get(hold.getAdapterPosition()), null, null, hold.like_counter, myUsername, "comment_like_count");

                    }
                });

                //Making Reply Invisible if comment is own comment
                if (comm_username.get(hold.getAdapterPosition()).equals(myUsername))
                    hold.reply.setVisibility(View.GONE);
                //Replying
                hold.reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        comment_editTxt.setText("@" + comm_username.get(hold.getAdapterPosition()) + " ");
                        comment_editTxt.requestFocus();
                        new Handler().postDelayed(new Runnable() {

                            public void run() {
                                comment_editTxt.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                                comment_editTxt.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                            }
                        }, 200);
                    }
                });
                //Like Related
                hold.like_counter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BottomFrag f = new BottomFrag();
                        Bundle b = new Bundle();
                        b.putString("switch", "comment_like_list");
                        b.putString("comment_like_list_post_owner_username", postOwner);
                        b.putString("comment_like_list_post_owner_time", postTime);
                        b.putString("comment_like_list_comment_owner_username", comm_username.get(hold.getAdapterPosition()));
                        b.putString("comment_like_list_comment_owner_time", comm_time.get(hold.getAdapterPosition()));
                        f.setArguments(b);
                        f.show(manager, "fra");
                    }
                });
                hold.like_outline.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hold.like_outline.startAnimation(heart_ani);
                                hold.like_outline.setVisibility(View.INVISIBLE);
                                hold.liked.setVisibility(View.VISIBLE);
                                //item=new ListItem(username.get(hold.getAdapterPosition()),time.get(hold.getAdapterPosition()),hold.like_counter,"comment_likes",null,null,null);
                                item = new ListItem(postOwner, postTime, comm_username.get(position),
                                        comm_time.get(position), hold.like_outline, hold.liked, null, myUsername, "comment_likes");

                                if (hold.like_counter.getVisibility() == View.INVISIBLE || hold.like_counter.getVisibility() == View.GONE) {
                                    hold.like_counter.setVisibility(View.VISIBLE);
                                    hold.like_counter.setText("1 like");
                                } else {
                                    int i = Integer.valueOf(hold.like_counter.getText().toString().substring(0, hold.like_counter.getText().toString().indexOf(" "))) + 1;
                                    hold.like_counter.setText(i + " likes");
                                }
                            }
                        }
                );
                hold.liked.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                heart_ani.setInterpolator(new ReverseInterpolator());
                                hold.liked.startAnimation(heart_ani);
                                hold.like_outline.setVisibility(View.VISIBLE);
                                hold.liked.setVisibility(View.INVISIBLE);
                                //item=new ListItem(username.get(hold.getAdapterPosition()),time.get(hold.getAdapterPosition()),hold.like_counter,"comment_unlike",null,null,null);
                                item = new ListItem(postOwner, postTime, comm_username.get(hold.getAdapterPosition()),
                                        comm_time.get(hold.getAdapterPosition()), hold.like_outline, hold.liked, null, myUsername, "comment_unlike");
                                if (hold.like_counter.getText().equals("1 like"))
                                    hold.like_counter.setVisibility(View.GONE);
                                else {
                                    int i = Integer.valueOf(hold.like_counter.getText().toString().substring(0, hold.like_counter.getText().toString().indexOf(" "))) - 1;
                                    if (i == 1) hold.like_counter.setText(i + " like");
                                    else hold.like_counter.setText(i + " likes");
                                }
                            }
                        }
                );
            } else if (show.equals("msgList")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.margin.setVisibility(View.GONE);
                hold.contents.setVisibility(View.VISIBLE);


                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(55));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Fullname Position
                RelativeLayout.LayoutParams fullname_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                fullname_params.setMargins(getDp(10), getDp(3), 0, 0);
                fullname_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                hold.fullname.setLayoutParams(fullname_params);

                //Setting LastMsg Position
                RelativeLayout.LayoutParams last_msg_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                last_msg_params.setMargins(getDp(10), 0, 0, 0);
                last_msg_params.addRule(RelativeLayout.BELOW, hold.fullname.getId());
                last_msg_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                hold.contents.setLayoutParams(last_msg_params);
                hold.contents.setMaxLines(1);
                hold.contents.setEllipsize(TextUtils.TruncateAt.END);

                //Setting LastMsg
                try {
                    // JSONArray arr=new JSONArray(time.get(position));
                    if (time != null && time.size() > 0) {
                        JSONObject ob = new JSONObject(time.get(position));
                        if (ob.getString("type").equals("text")) {
                            hold.contents.invalidate();
                            //String txt= URLEncoder.encode(ob.getString("msg"), "UTF-8");
                            hold.contents.setText(URLDecoder.decode((ob.getString("msg")), "UTF-8"));
                        } else if (ob.getString("type").equals("post"))
                            hold.contents.setText("post");
                        else hold.contents.setText("image");
                        if (!ob.getString("from").equals(myUsername)) {

                            Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        item = new ListItem(ob.getString("from"), hold.profile_pic);
                                        item = new ListItem(ob.getString("from"), null, hold.fullname, "get_full_name", null, null, null, parent.getContext());

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            if (ob.has("seen")) {
                                if (hold.contents.getTypeface().getStyle() == Typeface.BOLD) {
                                    final Typeface reg_font = Typeface.createFromAsset((parent.getContext()).getAssets(), "fonts/" + "Lato-Regular.ttf");
                                    hold.contents.setTypeface(reg_font);
                                }
                            } else {
                                seen_checker.add(ob.getString("from"));
                                hold.contents.setTypeface(null, Typeface.BOLD);
                                //Setting Indicator
                                Home_Screen.dummyTabLay.getTabAt(2).setIcon(R.drawable.indicator);
                                Needle.onBackgroundThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Hawk.put("msg_notif", "yes");
                                        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
                                        SharedPreferences.Editor editor = sh.edit();
                                        editor.putString("msg_notif", "yes");
                                        editor.apply();
                                    }
                                });
                            }
                        } else {
                            Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        item = new ListItem(ob.getString("to"), hold.profile_pic);
                                        item = new ListItem(ob.getString("to"), null, hold.fullname, "get_full_name", null, null, null, parent.getContext());

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                hold.rl.setOnClickListener(view -> {
                    if (hold.contents.getTypeface().getStyle() == Typeface.BOLD) {
                        final Typeface reg_font = Typeface.createFromAsset((parent.getContext()).getAssets(), "fonts/" + "Lato-Regular.ttf");
                        hold.contents.setTypeface(reg_font);
                        seen_checker.remove(username.get(position));
                        if (seen_checker.size() == 0) {

                            //Setting Indicator
                            Home_Screen.dummyTabLay.getTabAt(2).setIcon(new ColorDrawable(Color.parseColor("#00ffffff")));
                            Needle.onBackgroundThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Hawk.delete("msg_notif");
                                    SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
                                    SharedPreferences.Editor editor = sh.edit();
                                    editor.remove("msg_notif");
                                    editor.apply();
                                }
                            });
                        }

                    }

                    BottomFrag f = new BottomFrag(hold.contents);
                    Bundle dialogBind = new Bundle();
                    dialogBind.putString("switch", "messaging");
                    dialogBind.putString("searchUsername", username.get(hold.getAdapterPosition()));
                    dialogBind.putInt("position", hold.getAdapterPosition());
                    if (hold.fullname.getText().toString().length() > 0)
                        dialogBind.putString("searchFullname", hold.fullname.getText().toString());
                    f.setArguments(dialogBind);
                    FragmentManager manager = ((Activity) hold.rl.getContext()).getFragmentManager();
                    f.show(manager, "frag");
                });
                hold.rl.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        // Toast.makeText( parent.getContext(),username.get(hold.getAdapterPosition()),Toast.LENGTH_SHORT).show();
                        longClickMsgList(username.get(hold.getAdapterPosition()), hold.getAdapterPosition(), hold.itemView);
                        return false;
                    }
                });
            } else if (show.equals("followers_list")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);

                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Txt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);


                // Setting  ProfilePic & Fullname
                Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
                    @Override
                    public void run() {
                        item = new ListItem(username.get(hold.getAdapterPosition()), hold.profile_pic);
                        item = new ListItem(username.get(hold.getAdapterPosition()), null, hold.fullname, "get_full_name", null, null, null, parent.getContext());
                    }
                });

                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Passing Intent
                        Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                        Bundle b = new Bundle();
                        b.putString("searchUsername", username.get(hold.getAdapterPosition()));

                        i.putExtras(b);
                        i.putExtra("Open", "search_profile");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.getContext().startActivity(i);
                    }
                });
            } else if (show.equals("following_list")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);

                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Txt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);

                Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
                    @Override
                    public void run() {
                        item = new ListItem(username.get(hold.getAdapterPosition()), hold.profile_pic);
                        item = new ListItem(username.get(hold.getAdapterPosition()), null, hold.fullname, "get_full_name", null, null, null, parent.getContext());
                    }
                });

                //Onclik
                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Passing Intent
                        Intent i = new Intent(parent.getContext(), ProfileHolder.class);

                        Bundle b = new Bundle();
                        b.putString("searchUsername", username.get(hold.getAdapterPosition()));

                        i.putExtras(b);
                        i.putExtra("Open", "search_profile");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.getContext().startActivity(i);
                    }
                });
            } else if (show.equals("search")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);

                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Txt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);


                if (fullname != null && fullname.size() > 0) {
                    hold.fullname.setText(fullname.get(position));
                }


                if (profile_pic_list.size() != 0 && !profile_pic_list.get(hold.getAdapterPosition()).equals("")) {
                    Glide
                            .with(parent.getContext())
                            .asBitmap()
                            .load(profile_pic_list.get(hold.getAdapterPosition()))
                            .apply(new RequestOptions().placeholder(new ColorDrawable(Color.parseColor("#20ffffff"))).override(50, 50).error(R.drawable.profile))

                            .thumbnail(0.1f)
                            .into(new SimpleTarget<Bitmap>() {

                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    hold.profile_pic.setImageBitmap(resource);
                                }
                            });
                } else {
                    hold.profile_pic.setImageResource(R.drawable.profile);
                }


                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!tagSelection) {
                            //Passing Intent
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle b = new Bundle();
                            b.putString("searchUsername", username.get(hold.getAdapterPosition()));
                            b.putString("searchFullname", fullname.get(hold.getAdapterPosition()));
                            b.putString("searchBio", bio_list.get(hold.getAdapterPosition()));
                            b.putString("searchProfilePic", profile_pic_list.get(hold.getAdapterPosition()));
                            b.putString("searchWallPic", wall_pic_list.get(hold.getAdapterPosition()));
                            b.putString("searchSocietyList", society_list.get(hold.getAdapterPosition()));
                            i.putExtras(b);
                            i.putExtra("Open", "search_profile");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);
                        }
                    }
                });
            } else if (show.equals("tag_search")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);

                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Txt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);


                if (username != null && username.size() > 0) {
                    hold.fullname.setText(username.get(position));
                }
                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!tagSelection) {
                            //Passing Intent
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle b = new Bundle();
                            b.putString("what", "tags");
                            b.putString("tag_searched", username.get(hold.getAdapterPosition()));
                            i.putExtras(b);
                            i.putExtra("Open", "starred");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);
                        }
                    }
                });
            } else if (show.equals("message_search")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);

                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Txt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);

                if (fullname != null && fullname.size() > 0) {
                    if (hold.getAdapterPosition() != fullname.size())
                        hold.fullname.setText(fullname.get(hold.getAdapterPosition()));
                }

                if (profile_pic_list.size() != 0 && !profile_pic_list.get(hold.getAdapterPosition()).equals("")) {
                    Glide
                            .with(parent.getContext())
                            .asBitmap()
                            .load(profile_pic_list.get(hold.getAdapterPosition()))
                            .apply(new RequestOptions().placeholder(new ColorDrawable(Color.parseColor("#20ffffff"))).override(50, 50).error(R.drawable.profile))
                            .thumbnail(0.1f)
                            .into(new SimpleTarget<Bitmap>() {

                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    hold.profile_pic.setImageBitmap(resource);
                                }
                            });
                } else {
                    hold.profile_pic.setImageResource(R.drawable.profile);
                }
                //Generating Msg RoomKey
                if (username.size() > 0) {
                    ArrayList<String> roomUserList = new ArrayList<>();
                    roomUserList.add(myUsername);
                    roomUserList.add(username.get(hold.getAdapterPosition()));
                    Collections.sort(roomUserList, String.CASE_INSENSITIVE_ORDER);
                    String key = roomUserList.get(0) + "+" + roomUserList.get(1);
                    String finalKey = key.replace(".", "-");//final Key
                    roomKeyList.add(finalKey);
                }
                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (username.get(position).equals(myUsername)) {
                            Snackbar snackbar = Snackbar.make(hold.itemView, "You can't send message to yourself", Snackbar.LENGTH_SHORT);
                            View sbView = snackbar.getView();
                            sbView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorPrimaryDark));
                            snackbar.show();
                            //  Toast.makeText( parent.getContext(), "You can't send message to yourself", Toast.LENGTH_SHORT).show();
                        } else {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                            String time_format = simpleDateFormat.format(new Date());
                            JSONObject sendText = new JSONObject();
                            try {
                                socket.disconnect();
                                socket.connect();
                                ProgressDialog pDialog = new ProgressDialog(parent.getContext());
                                pDialog.setMessage("Sending Message...");
                                pDialog.setCancelable(false);
                                pDialog.show();
                                sendText.put("postMsg_postOwnerUsername", postOwner);
                                sendText.put("postMsg_postOwnerTime", postTime);
                                sendText.put("postMsg_postImg", postImgLink);
                                sendText.put("postMsg_multi", postMulti);
                                sendText.put("postMsg_postType", postType);
                                sendText.put("fromRoom", myUsername);
                                sendText.put("toRoom", username.get(hold.getAdapterPosition()));
                                sendText.put("thisRoomKey", roomKeyList.get(hold.getAdapterPosition()));
                                sendText.put("fromFullNamePostMsg", Hawk.get("myFullName"));
                                sendText.put("postDownloadStat", post_download_stat);
                                sendText.put("msgTimePostMsg", time_format);
                                socket.emit("data", sendText);
                                socket.on("postMsgSendStat", new Emitter.Listener() {
                                    @Override
                                    public void call(Object... args) {
                                        JSONObject ob = (JSONObject) args[0];
                                        try {
                                            if (ob.getString("status").equals("yes")) {
                                                socket.disconnect();
                                                Needle.onMainThread().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pDialog.dismiss();
                                                        Snackbar snackbar = Snackbar.make(hold.itemView, "Post sent to " + hold.fullname.getText().toString(), Snackbar.LENGTH_LONG);
                                                        View sbView = snackbar.getView();
                                                        sbView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorPrimaryDark));
                                                        snackbar.show();


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
                    }
                });
            } else if (show.equals("new_message_search")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);

                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Txt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);

                if (fullname != null && fullname.size() > 0) {
                    if (hold.getAdapterPosition() != fullname.size())
                        hold.fullname.setText(fullname.get(hold.getAdapterPosition()));
                }

                if (profile_pic_list.size() != 0 && !profile_pic_list.get(hold.getAdapterPosition()).equals("")) {
                    Glide
                            .with(parent.getContext())
                            .asBitmap()
                            .load(profile_pic_list.get(hold.getAdapterPosition()))
                            .apply(new RequestOptions().error(R.drawable.profile).placeholder(new ColorDrawable(Color.parseColor("#20ffffff"))).override(50, 50))
                            .thumbnail(0.1f)
                            .into(new SimpleTarget<Bitmap>() {

                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    hold.profile_pic.setImageBitmap(resource);
                                }
                            });
                } else {
                    hold.profile_pic.setImageResource(R.drawable.profile);
                }
                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (myUsername.equals(username.get(hold.getAdapterPosition()))) {
                            Snackbar snackbar = Snackbar.make(hold.itemView, "Obviouly you can't chat with yourself .", Snackbar.LENGTH_LONG);
                            View sbView = snackbar.getView();
                            sbView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorPrimaryDark));
                            snackbar.show();
                        } else {
                            BottomFrag f = new BottomFrag(hold.contents);
                            Bundle dialogBind = new Bundle();
                            dialogBind.putString("switch", "messaging");
                            dialogBind.putString("new_message_search", "yes");
                            dialogBind.putString("searchUsername", username.get(hold.getAdapterPosition()));
                            //dialogBind.putInt("position",hold.getAdapterPosition());
                            if (hold.fullname.getText().toString().length() > 0)
                                dialogBind.putString("searchFullname", hold.fullname.getText().toString());
                            f.setArguments(dialogBind);
                            f.show(manager, "frag");
                        }
                    }
                });
            }
            //Showing Block List with unblock option
            else if (show.equals("block_list")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);
                hold.unblock.setVisibility(View.VISIBLE);

                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Txt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);

                if (username.size() > 0) {
                    //Setting ProfilePic
                    item = new ListItem(username.get(position), hold.profile_pic);

                    //Setting Fullname
                    item = new ListItem(username.get(hold.getAdapterPosition()), null, hold.fullname, "get_full_name", null, null, null, parent.getContext());
                }

                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Passing Intent
                        Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                        Bundle b = new Bundle();
                        b.putString("searchUsername", username.get(hold.getAdapterPosition()));
                        i.putExtras(b);
                        i.putExtra("Open", "search_profile");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.getContext().startActivity(i);
                    }
                });
                hold.unblock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hold.unblock.startAnimation(an);
                        JSONObject ob = new JSONObject();
                        try {
                            socket.disconnect();
                            socket.connect();

                            Toast.makeText(parent.getContext(), "User is unblocked successfully.", Toast.LENGTH_SHORT).show();

                            ob.put("unblock_blocked_user", username.get(hold.getAdapterPosition()));
                            ob.put("unblock_blocker", myUsername);

                            Animation rev_an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                            rev_an.setInterpolator(new ReverseInterpolator());
                            hold.itemView.startAnimation(rev_an);
                            if (hold.getAdapterPosition() != RecyclerView.NO_POSITION) {
                                username.remove(hold.getAdapterPosition());
                                notifyItemRemoved(hold.getAdapterPosition());
                                notifyItemRangeChanged(hold.getAdapterPosition(), username.size());
                            }


                            socket.emit("data", ob);
                            socket.on("unblock_status", new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    JSONObject ob = (JSONObject) args[0];
                                    try {
                                        if (ob.getString("status").equals("yes")) {
                                            Needle.onMainThread().execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    socket.disconnect();
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
            }
            //Showing Likes List
            else if (show.equals("like_list")) {
                hold.fullname.setVisibility(View.VISIBLE);
                hold.profile_pic.setVisibility(View.VISIBLE);
                hold.contents.setVisibility(View.GONE);
                hold.like_outline.setVisibility(View.GONE);
                hold.like_counter.setVisibility(View.GONE);
                hold.reply.setVisibility(View.GONE);
                hold.margin.setVisibility(View.GONE);

                //Setting Container
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(50));
                //params.setMargins(20,25,0,0);
                params.setMargins(0, 0, 0, getDp(10));
                hold.rl.setLayoutParams(params);

                //Setting ProfilePic position
                RelativeLayout.LayoutParams prof_pic_params = new RelativeLayout.LayoutParams(getDp(35), getDp(35));
                prof_pic_params.setMargins(getDp(3), 0, 0, 0);
                prof_pic_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.profile_pic.setLayoutParams(prof_pic_params);

                //Setting Txt Position
                RelativeLayout.LayoutParams content_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getDp(30));
                content_params.setMargins(getDp(10), 0, 0, 0);
                content_params.addRule(RelativeLayout.RIGHT_OF, hold.profile_pic.getId());
                content_params.addRule(RelativeLayout.CENTER_VERTICAL);
                hold.fullname.setLayoutParams(content_params);
                //setting fullname
                hold.fullname.setText(time.get(hold.getAdapterPosition()));


                if (settings.get(position).equals("")) {
                    hold.profile_pic.setImageResource(R.drawable.profile);
                } else {
                    ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));
                    Glide.with(parent.getContext()).load(settings.get(position)).
                            apply(new RequestOptions().override(50, 50).placeholder(cd).diskCacheStrategy(DiskCacheStrategy.NONE).
                                    skipMemoryCache(true).error(R.drawable.profile)).thumbnail(0.1f).into(hold.profile_pic);
                }

                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Passing Intent
                        Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                        Bundle b = new Bundle();
                        b.putString("searchUsername", username.get(hold.getAdapterPosition()));
                        i.putExtras(b);
                        i.putExtra("Open", "search_profile");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.getContext().startActivity(i);
                    }
                });

            } else if (show.equals("reports")) {
                hold.margin.setVisibility(View.GONE);
                hold.contents.setText(settings.get(hold.getAdapterPosition()) + " reports by  " + username.get(hold.getAdapterPosition()));
                hold.rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle b = new Bundle();
                        if (settings.get(hold.getAdapterPosition()).equals("comments")) {
                            b.putString("post_owner_username", report_comnt_post_owner_name.get(hold.getAdapterPosition()));
                            b.putString("post_owner_time", report_comnt_post_owner_time.get(hold.getAdapterPosition()));
                            b.putString("notif_commenter_username", convict_name.get(hold.getAdapterPosition()));
                            b.putString("notif_comment_time", time.get(hold.getAdapterPosition()));
                            b.putString("fromComntReport", "yes");
                            b.putString("what", "show_notification_post");
                            b.putString("Open", "starred");
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            i.putExtras(b);
                            parent.getContext().startActivity(i);
                        } else if (settings.get(hold.getAdapterPosition()).equals("profile")) {
                            //Passing Intent
                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            Bundle bi = new Bundle();
                            bi.putString("searchUsername", convict_name.get(hold.getAdapterPosition()));
                            i.putExtras(bi);
                            i.putExtra("Open", "search_profile");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            parent.getContext().startActivity(i);

                        } else {
                            b.putString("post_owner_username", convict_name.get(hold.getAdapterPosition()));
                            b.putString("post_owner_time", time.get(hold.getAdapterPosition()));
                            b.putString("what", "show_notification_post");
                            b.putString("fromPostReport", "yes");
                            b.putString("Open", "starred");

                            Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                            i.putExtras(b);
                            parent.getContext().startActivity(i);
                        }

                    }
                });
            } else if (show.equals("font_list")) {

                final String ss = settings.get(position).substring(0, settings.get(hold.getAdapterPosition()).indexOf("."));

                final Typeface font = Typeface.createFromAsset((parent.getContext()).getAssets(), "fonts/" + settings.get(position));
                hold.contents.setText(ss);
                hold.contents.setTextColor(Color.parseColor("#001919"));
                hold.contents.setTypeface(font);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }


    void longClickMsgList(String frnd_username,int adp_position,View itemView){
        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder( parent.getContext());
        final View vi = LayoutInflater.from( parent.getContext()).inflate(R.layout.dialog_image, null);
        RelativeLayout mainCont=vi.findViewById(R.id.dialogImgRelLay);
        RecyclerView fontList=vi.findViewById(R.id.fontRecycler);
        TouchImageView img=vi.findViewById(R.id.dialog_image);
        img.setVisibility(View.GONE);
        fontList.setVisibility(View.VISIBLE);


        //Generating Msg RoomKey
        ArrayList<String> roomUserList=new ArrayList<>();
        roomUserList.add(myUsername);
        roomUserList.add(frnd_username);
        Collections.sort(roomUserList, String.CASE_INSENSITIVE_ORDER);
        String key=roomUserList.get(0)+"+"+roomUserList.get(1);
        final String roomKey=key.replace(".","-");

        //Setting Wrap Content
        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mainCont.setLayoutParams(params);
        mainCont.setBackgroundResource(R.drawable.bg_gradient);


        final ArrayList<String> name=new ArrayList<>();
        name.add("Delete Conversation");

        AccountSettingsAdapter adapter=new AccountSettingsAdapter(name,null,null,"triple_dot",null);
        fontList.setHasFixedSize(true);
        fontList.setLayoutManager(new LinearLayoutManager( parent.getContext()));
        fontList.setAdapter(adapter);
        fontList.addOnItemTouchListener(new RecyclerItemClickListener( parent.getContext(), fontList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int cur_position) {
                if(name.get(cur_position).equals("Delete Conversation")){
                    dialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder( parent.getContext());
                    final View vi = LayoutInflater.from( parent.getContext()).inflate(R.layout.delete_dialog, null);
                    //Dialog Properties////////
                    AppCompatImageButton ok=vi.findViewById(R.id.okDelete);
                    AppCompatImageButton cancel=vi.findViewById(R.id.cancelDelete);


                    //Setting Tint
                    ButtonTint tint=new ButtonTint("white");
                    tint.setTint(ok);
                    tint.setTint(cancel);

                    //Onclick
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Animation rev_an=AnimationUtils.loadAnimation( parent.getContext(),R.anim.grow);
                            rev_an.setInterpolator(new ReverseInterpolator());
                            if (adp_position != RecyclerView.NO_POSITION) {
                                itemView.startAnimation(rev_an);
                                Messaging.username.remove(adp_position);
                                Messaging.lastMsgList.remove(adp_position);
                                Messaging.adapter.notifyDataSetChanged();
                            }
                            Toast.makeText( parent.getContext(),"Chat Deleted",Toast.LENGTH_SHORT).show();

                            socket.disconnect();
                            socket.connect();

                            JSONObject ob=new JSONObject();
                            try {
                                ob.put("whole_key_del",roomKey);
                                ob.put("whole_key_del_MymsgUsername",myUsername);
                                ob.put("whole_key_del_FrndmsgUsername",frnd_username);
                                socket.emit("data",ob);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                            socket.on("total_deleteMsg_status", new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    JSONObject obj = (JSONObject)args[0];
                                    ((Activity) parent.getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if(obj.getString("status").equals("yes")){
                                                 socket.disconnect();

                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });


                                }
                            });
                            dialog.dismiss();
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
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
                    dialog.getWindow().setDimAmount(0.3f);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        dialog.getWindow().setDimAmount(0.3f);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        /////////////////////////
    }

    private int getDp(int px){
        float d =  parent.getContext().getResources().getDisplayMetrics().density;
        int dp= (int) (px * d);
        return dp;
    }
    @Override
    public int getItemCount() {
        if(show.equals("search")||show.equals("message_search")||show.equals("new_message_search")||show.equals("msgList")&&username!=null){
            return username.size();
        }
        else if(show.equals("search")||show.equals("message_search")||show.equals("new_message_search")||show.equals("msgList")&&username==null){
            return 0;
        }
        else if(show.equals("comments")){
            return comm_username.size();
        }
        else {
            if (settings == null) {
                if (username != null) return username.size();
                else if (fullname != null) return fullname.size();
                else return 0;
            } else {
                return settings.size();
            }
        }
    }
}
