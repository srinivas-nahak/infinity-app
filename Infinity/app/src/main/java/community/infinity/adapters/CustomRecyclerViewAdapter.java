package community.infinity.adapters;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;
import com.pixplicity.htmlcompat.HtmlCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import community.infinity.BottomFrags.BottomFrag;
import community.infinity.ListItem;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.Reports.Reports;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.ProfileHolder;
import community.infinity.writing.Pagination;
import community.infinity.writing.RememberTextStyle;
import custom_views_and_styles.BetterLinkMovementMethod;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.HashTagHelper;
import custom_views_and_styles.HeightWrappingViewPager;
import custom_views_and_styles.LinkMovementMethodOverride;
import custom_views_and_styles.NoScrollTextView;
import custom_views_and_styles.ReverseInterpolator;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.relex.circleindicator.CircleIndicator;
import needle.Needle;
import ooo.oxo.library.widget.TouchImageView;
public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomRecyclerViewHolder>{
    
    private ViewGroup parent;
    private List<TimelineData> totalList;
    private AlertDialog dialog;
    private Animation an,fade_buttons;
    private Activity activity;
    private HashTagHelper tagHelperTripleDot;
    private final char[] additionalSymbols = new char[]{'&', '.', '_'};
    private final int MAX_LINES = 3;
    private final String TWO_SPACES = " ";
    private boolean isTextCheck=true;
    private String report_type;

    // private ArrayList<String> arrayList=new ArrayList<>();
    private ListItem item;//it's used
    private Date postTime,currentTime;
    private String notif_commenter_username,notif_comment_time;
    public static ArrayList<String>mentioned_name=new ArrayList<>();
    //Text Related
    private float x1,x2;
    private int mCurrentIndex = 0;
    private Pagination mPagination;
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public CustomRecyclerViewAdapter(List<TimelineData> totalList) {
        this.totalList=totalList;
    }
    //Showing Reports Post
    public CustomRecyclerViewAdapter(List<TimelineData> totalList,String report_type) {
        this.totalList=totalList;
        this.report_type=report_type;
    }
    //Showing Post with comments
    public CustomRecyclerViewAdapter(List<TimelineData> totalList,String notif_commenter_username,String notif_comment_time,String report_type) {
        this.totalList=totalList;
        this.notif_commenter_username=notif_commenter_username;
        this.notif_comment_time=notif_comment_time;
        this.report_type=report_type;
    }

    @Override
    public CustomRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent=parent;
        Hawk.init( parent.getContext()).build();
        this.activity=(Activity) parent.getContext();
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.timeline_holder, parent, false);

        return new CustomRecyclerViewHolder(v);

    }

    @Override
    public void onBindViewHolder( CustomRecyclerViewHolder holder, int position) {
    try {
        //Fetching TimelineData
        TimelineData timelineData = totalList.get(holder.getAdapterPosition());


        //Animation
        fade_buttons = AnimationUtils.loadAnimation(parent.getContext(), R.anim.fade_buttons);
        //Getting Imglink
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> arrayList = gson.fromJson(timelineData.getImg_link(), type);

        //Setting ViewPager
        item = new ListItem(activity, holder.pager, arrayList, position, holder.like_btn_outline, holder.like_btn, holder.middle_heart);
        //Making Viewpg Indicator Visible
        if (arrayList.size() > 1) {
            holder.indicator.setViewPager(holder.pager);
        }
////////////////////Showing Mentioned Comments of Notifications
        if (notif_commenter_username != null) {
            View commentView = holder.itemView.findViewById(R.id.showNotifComments);
            commentView.setVisibility(View.VISIBLE);
            RelativeLayout rl = commentView.findViewById(R.id.showNotifComments);
            TextView fullname = commentView.findViewById(R.id.fullnameComm);
            TextView content = commentView.findViewById(R.id.accountSettingsName);
            CircleImageView profile_pic = commentView.findViewById(R.id.userImageComm);
            AppCompatImageButton like_outline = commentView.findViewById(R.id.like_out_Comm);
            AppCompatImageButton liked = commentView.findViewById(R.id.liked_Comm);
            TextView like_counter = commentView.findViewById(R.id.likeComm);
            View margin = commentView.findViewById(R.id.headAccSett);


            final Typeface bold_font = Typeface.createFromAsset((fullname.getContext()).getAssets(), "fonts/" + "Lato-Bold.ttf");
            fullname.setTypeface(bold_font);

            Shader textShader = new LinearGradient(0, 0, 0, 45,
                    new int[]{Color.parseColor("#43cea2"), Color.parseColor("#185a9d")},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            //fullname.getPaint().setShader(textShader);

            final Animation an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.page_prev);
            final Animation heart_ani = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
            rl.startAnimation(an);
            rl.setSelected(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rl.startAnimation(an);
                    rl.setSelected(false);
                }
            }, 2000);

            like_outline.setVisibility(View.VISIBLE);
            fullname.setVisibility(View.VISIBLE);
            profile_pic.setVisibility(View.VISIBLE);
            margin.setVisibility(View.GONE);

            Hawk.init(fullname.getContext()).build();
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(fullname.getContext());
            String myUsername;
            if (Hawk.get("myUserName") != null) myUsername = Hawk.get("myUserName");
            else myUsername = sh.getString("username", null);

            //Checking Liked by the current user or  not
            item = new ListItem(timelineData.getUsername(), timelineData.getTime(), notif_commenter_username,
                    notif_comment_time, like_outline, liked, null, myUsername, "comment_check_like");


            //Getting Number of likes
            item = new ListItem(timelineData.getUsername(), timelineData.getTime(), notif_commenter_username,
                    notif_comment_time, null, null, like_counter, myUsername, "comment_like_count");


            //Setting Comment Content
            item = new ListItem(notif_commenter_username, notif_comment_time, content, "notification_comment", null, null, null, parent.getContext());

            //Setting ProfilePic of Comment
            item = new ListItem(notif_commenter_username, profile_pic);

            //Setting Fullname of Comment
            item = new ListItem(notif_commenter_username, null, fullname, "get_full_name", null, null, null, parent.getContext());

            //username.setText(notif_commenter_username);

            fullname.setClickable(true);
            profile_pic.setClickable(true);
            rl.setClickable(true);
            rl.setFocusable(true);

            fullname.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    rl.performLongClick();
                    return false;
                }
            });
            profile_pic.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    rl.performLongClick();
                    return false;
                }
            });
            rl.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
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
                    if (notif_commenter_username.equals(myUsername) || timelineData.getUsername().equals(myUsername) ||
                            myUsername.equals("infinity"))
                        name.add("Delete");
                    name.add("Report");

                    AccountSettingsAdapter adapter = new AccountSettingsAdapter(name, null, null, "triple_dot", null);
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
                                        ob.put("comment_del_time", notif_comment_time);
                                        ob.put("comment_del_commenter", notif_commenter_username);
                                        ob.put("comment_del_owner_post_time", timelineData.getTime());
                                        ob.put("comment_del_post_owner", timelineData.getUsername());
                                        Needle.onBackgroundThread().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                socket.emit("data", ob);
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                    rl.startAnimation(rev_an);


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
                                                            rl.startAnimation(rev_an);
                                                            rl.setVisibility(View.GONE);
                                                            if (holder.comments_count.getText().equals("1 Comment"))
                                                                holder.comments_count.setVisibility(View.GONE);
                                                            else {
                                                                int i = Integer.valueOf(holder.comments_count.getText().toString().substring(0, holder.comments_count.getText().toString().indexOf(" "))) - 1;
                                                                if (i == 1)
                                                                    holder.comments_count.setText(i + " Comment");
                                                                else if (i > 0)
                                                                    holder.comments_count.setText(i + " Comments");
                                                                else
                                                                    holder.comments_count.invalidate();
                                                            }
                                                            socket.disconnect();
                                                            if (report_type != null) {
                                                                socket.connect();
                                                                JSONObject ov = new JSONObject();
                                                                ov.put("del_report_comment_post_owner_name", timelineData.getUsername());
                                                                ov.put("del_report__comment_post_time", timelineData.getTime());
                                                                ov.put("del_report_comment_owner_name", notif_commenter_username);
                                                                ov.put("del_report_comment_time", notif_comment_time);
                                                                ov.put("del_report_type", "comments");
                                                                socket.emit("data", ov);
                                                                socket.on("report_del_stat", new Emitter.Listener() {
                                                                    @Override
                                                                    public void call(Object... args) {
                                                                        JSONObject ob = (JSONObject) args[0];
                                                                        try {
                                                                            if (ob.getString("status").equals("yes")) {
                                                                                Reports.reloadReports();
                                                                                socket.disconnect();
                                                                            }
                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                            // MessageAdapter.this.notifyDataSetChanged();
                                                            //}
                                                            //
                                                        } else {
                                                            Toast.makeText(parent.getContext(), "Sorry some error occured.Please try again.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (JSONException e) {
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
                            } else {
                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
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
                    return false;
                }
            });
            profile_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                    Bundle b = new Bundle();
                    b.putString("searchUsername", notif_commenter_username);
                    i.putExtras(b);
                    i.putExtra("Open", "search_profile");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    parent.getContext().startActivity(i);
                }
            });
            fullname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                    Bundle b = new Bundle();
                    b.putString("searchUsername", notif_commenter_username);
                    i.putExtras(b);
                    i.putExtra("Open", "search_profile");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    parent.getContext().startActivity(i);
                }
            });

            //Like Related
            like_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BottomFrag f = new BottomFrag();
                    Bundle b = new Bundle();
                    b.putString("switch", "comment_like_list");
                    b.putString("comment_like_list_post_owner_username", timelineData.getUsername());
                    b.putString("comment_like_list_post_owner_time", timelineData.getTime());
                    b.putString("comment_like_list_comment_owner_username", notif_commenter_username);
                    b.putString("comment_like_list_comment_owner_time", notif_comment_time);
                    f.setArguments(b);
                    f.show(activity.getFragmentManager(), "fra");
                }
            });
            like_outline.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            like_outline.startAnimation(heart_ani);
                            like_outline.setVisibility(View.INVISIBLE);
                            liked.setVisibility(View.VISIBLE);
                            //item=new ListItem(email.get(getAdapterPosition()),time.get(getAdapterPosition()),like_counter,"comment_likes",null,null,null);
                            item = new ListItem(timelineData.getUsername(), timelineData.getTime(), notif_commenter_username,
                                    notif_comment_time, like_outline, liked, null, myUsername, "comment_likes");

                            if (like_counter.getVisibility() == View.INVISIBLE || like_counter.getVisibility() == View.GONE) {
                                like_counter.setVisibility(View.VISIBLE);
                                like_counter.setText("1 like");
                            } else {
                                int i = Integer.valueOf(like_counter.getText().toString().substring(0, like_counter.getText().toString().indexOf(" "))) + 1;
                                like_counter.setText(i + " likes");
                            }
                        }
                    }
            );
            liked.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            heart_ani.setInterpolator(new ReverseInterpolator());
                            liked.startAnimation(heart_ani);
                            like_outline.setVisibility(View.VISIBLE);
                            liked.setVisibility(View.INVISIBLE);
                            //item=new ListItem(email.get(getAdapterPosition()),time.get(getAdapterPosition()),like_counter,"comment_unlike",null,null,null);
                            item = new ListItem(timelineData.getUsername(), timelineData.getTime(), notif_commenter_username,
                                    notif_comment_time, like_outline, liked, null, myUsername, "comment_unlike");
                            if (like_counter.getText().equals("1 like"))
                                like_counter.setVisibility(View.INVISIBLE);
                            else {
                                int i = Integer.valueOf(like_counter.getText().toString().substring(0, like_counter.getText().toString().indexOf(" "))) - 1;
                                if (i == 1) like_counter.setText(i + " like");
                                else like_counter.setText(i + " likes");
                            }
                        }
                    }
            );


            char[] additionalSymbols = new char[]{
                    '_',
                    '.'
            };
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
                    //Toast.makeText(ctx,hashTag,Toast.LENGTH_SHORT).show();
                }
            }, additionalSymbols);
            mTextHashTagHelper.handle(content);
        }


//////////////Setting Timestamp

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
        try {
            Date date = simpleDateFormat.parse(timelineData.getTime());
            long time = date.getTime();

            holder.time_stamp.setText(getTimeAgo(time));

        } catch (ParseException e) {
            e.printStackTrace();
        }


        //Checking Short Book or not
        if (timelineData.getShort_book_content() != null) {
            holder.readBookTxt.setVisibility(View.VISIBLE);
            holder.short_book_indicator.setVisibility(View.VISIBLE);
            holder.short_book_indicator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.readBookTxt.callOnClick();
                }
            });
            holder.readBookTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                    final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_image, null);
                    RelativeLayout rl = vi.findViewById(R.id.dialogImgRelLay);
                    //final ScrollView scrollView = vi.findViewById(R.id.dialogScrollView);
                    final TextView caption = vi.findViewById(R.id.dialog_text);
                    final TextView cap_vert = vi.findViewById(R.id.dialog_text_vertical);
                    final RelativeLayout countBox = vi.findViewById(R.id.pgCounter);
                    final TextView curPg = vi.findViewById(R.id.curPg);
                    final TextView totPg = vi.findViewById(R.id.totPg);
                    final FloatingActionButton close = vi.findViewById(R.id.closeButtonBook);
                    final FloatingActionButton horz_btn = vi.findViewById(R.id.horz_ButtonBook);
                    final FloatingActionButton vert_btn = vi.findViewById(R.id.vert_ButtonBook);
                    final FrameLayout main_cont = vi.findViewById(R.id.bookTxtFrame);
                    final ScrollView scrollView = vi.findViewById(R.id.scrollBook);
                    final RelativeLayout bottomBtn = vi.findViewById(R.id.ll_buttonsBook);
                    main_cont.setVisibility(View.VISIBLE);
                    bottomBtn.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.VISIBLE);
                    caption.setBackgroundResource(R.drawable.circle);
                    scrollView.setBackgroundResource(R.drawable.circle);
                    main_cont.setBackgroundResource(R.drawable.book_bg);
                    final Spanned htmlString = HtmlCompat.fromHtml(vi.getContext(), timelineData.getShort_book_content(), 0);
                    cap_vert.setText(htmlString);


                    //Onclik
                    horz_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            horz_btn.setVisibility(View.INVISIBLE);
                            vert_btn.setVisibility(View.VISIBLE);
                            countBox.setVisibility(View.VISIBLE);
                            caption.setVisibility(View.VISIBLE);
                            main_cont.setVisibility(View.VISIBLE);
                            scrollView.setVisibility(View.INVISIBLE);

                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    countBox.setVisibility(View.INVISIBLE);
                                }
                            }, 2000);

                            caption.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    // Removing layout listener to avoid multiple calls
                                    caption.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    mPagination = new Pagination(htmlString,
                                            caption.getWidth(),
                                            caption.getHeight(),
                                            caption.getPaint(),
                                            caption.getLineSpacingMultiplier(),
                                            caption.getLineSpacingExtra(),
                                            caption.getIncludeFontPadding());
                                    update(caption);
                                    totPg.setText(mPagination.size() + "");
                                    curPg.setText("1");
                                    if (caption.getTextSize() == 32.0f)
                                        caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, caption.getTextSize() - 3.5f);

                                }
                            });
                        }
                    });

                    vert_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            horz_btn.setVisibility(View.VISIBLE);
                            vert_btn.setVisibility(View.INVISIBLE);
                            countBox.setVisibility(View.INVISIBLE);
                            caption.setVisibility(View.INVISIBLE);
                            main_cont.setVisibility(View.INVISIBLE);
                            scrollView.setVisibility(View.VISIBLE);


                            caption.setText(htmlString);
                            caption.setMovementMethod(new ScrollingMovementMethod());
                        }
                    });


                    /////////////closeBtn
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });


                    ///On Touch
                    caption.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getPointerCount() == 1) {
                                if (event.getAction() == MotionEvent.ACTION_UP) {

                                    x2 = event.getX();
                                    float deltaX = x2 - x1;

                                    if (Math.abs(deltaX) > 150) {
                                        // Left to Right swipe action
                                        if (x2 > x1) {
                                            // mController.previous();
                                            mCurrentIndex = (mCurrentIndex > 0) ? mCurrentIndex - 1 : 0;
                                            update(caption);
                                            //Animation
                                            Animation an = AnimationUtils.loadAnimation(caption.getContext(), R.anim.page_prev);
                                            caption.startAnimation(an);
                                            curPg.setText(mCurrentIndex + 1 + "");
                                            countBox.setVisibility(View.VISIBLE);
                                            new Handler().postDelayed(new Runnable() {

                                                @Override
                                                public void run() {
                                                    countBox.setVisibility(View.INVISIBLE);
                                                }
                                            }, 2000);
                                            // Toast.makeText(caption.getContext(),mCurrentIndex+1+"",Toast.LENGTH_SHORT).show();
                                        }

                                        // Right to left swipe action
                                        else {
                                            mCurrentIndex = (mCurrentIndex < mPagination.size() - 1) ? mCurrentIndex + 1 : mPagination.size() - 1;
                                            update(caption);

                                            //Animation
                                            Animation an = AnimationUtils.loadAnimation(caption.getContext(), R.anim.page_prev);
                                            caption.startAnimation(an);
                                            curPg.setText(mCurrentIndex + 1 + "");
                                            countBox.setVisibility(View.VISIBLE);
                                            new Handler().postDelayed(new Runnable() {

                                                @Override
                                                public void run() {
                                                    countBox.setVisibility(View.INVISIBLE);
                                                }
                                            }, 2000);
                                            //Toast.makeText(caption.getContext(),mCurrentIndex+1+"",Toast.LENGTH_SHORT).show();
                                        }

                                    }


                                }
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    x1 = event.getX();

                                }
                                return true;
                            }

                            return false;
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
                    dialog.getWindow().setDimAmount(0.9f);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                    // dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    rl.setBackground(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    /////////////////////////
                }
            });
        }

        //Setting Captions
        if (timelineData.getCaption() != null && !timelineData.getCaption().equals("null")) {
            holder.caption.setVisibility(View.VISIBLE);
            try {
                holder.caption.setText(URLDecoder.decode(timelineData.getCaption(), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
                holder.caption.setText("");
            }
            // holder.caption.setOnTouchListener(new LinkMovementMethodOverride());
            Linkify.addLinks(holder.caption, Linkify.ALL);

            //Aligning Text
            holder.caption.setGravity(Gravity.START);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                holder.caption.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            }

            holder.caption.post(new Runnable() {
                @Override
                public void run() {
                    int lineCount = holder.caption.getLineCount();
                    // Use lineCount here
                    if (lineCount > 2) {
                        holder.showMoreTxt.setVisibility(View.VISIBLE);
                    }
                }
            });
            holder.showMoreTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isTextCheck) {
                        holder.caption.setMaxLines(45);
                        holder.showMoreTxt.setText("...less");
                        isTextCheck = false;
                    } else {
                        holder.caption.setMaxLines(2);
                        holder.showMoreTxt.setText("...more");
                        isTextCheck = true;
                    }
                }
            });
            //Copying Text OnLongClick
            holder.caption.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) parent.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("message", timelineData.getCaption());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(parent.getContext(), "Text Copied", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            //Tag Clicking
            HashTagHelper mTextHashTagHelper = HashTagHelper.Creator.create(parent.getContext().getResources().getColor(R.color.tagColor), new HashTagHelper.OnHashTagClickListener() {
                @Override
                public void onHashTagClicked(String hashTag, char sign) {
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
                    if (sign == '@') {
                        Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                        Bundle b = new Bundle();
                        b.putString("searchUsername", hashTag);
                        i.putExtras(b);
                        i.putExtra("Open", "search_profile");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        parent.getContext().startActivity(i);
                    }
                }
            }, additionalSymbols);
            mTextHashTagHelper.handle(holder.caption);
        } else {
            holder.caption.setVisibility(View.GONE);
        }

        //Setting fullname
        holder.fullName.setText(timelineData.getOwner_fullname());

        //Setting ProfilePic
        if (timelineData.getOwner_profPic().equals("")) {
            holder.userImg.setImageResource(R.drawable.profile);
        } else {

            ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));
            Glide.with(parent.getContext()).load(timelineData.getOwner_profPic()).
                    apply(new RequestOptions().override(50, 50).error(R.drawable.profile).placeholder(cd).diskCacheStrategy(DiskCacheStrategy.NONE).
                            skipMemoryCache(true)).thumbnail(0.1f).into(holder.userImg);
        }


        //Setting Number of likes
        int i = Integer.valueOf(timelineData.getLikes_counter());
        if (i != 0) {
            holder.likes_count.setVisibility(View.VISIBLE);
            if (i == 1) holder.likes_count.setText(i + " like");
            else holder.likes_count.setText(i + " likes");
        } else {
            //Making txtView invisible if number of likes is 0

           // holder.likes_count.setVisibility(View.GONE);
        }

        Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
            @Override
            public void run() {

                //Setting ProfilePic
                //item=new ListItem(timelineData.getUsername(),holder.userImg);

                //Setting Username
                //  item=new ListItem(timelineData.getUsername(),null,holder.fullName,"name_full",null,null,null, parent.getContext());


                //Checking Liked by the current user or  not
                item = new ListItem(timelineData.getUsername(), timelineData.getTime(), null, "check_like", holder.like_btn_outline, holder.like_btn, null, parent.getContext());

                //Checking Starred by the current user or  not
                item = new ListItem(timelineData.getUsername(), timelineData.getTime(), null, "check_star", holder.star_btn_outline, holder.star_btn, null, parent.getContext());


            }
        });

        //Making Comments Visibility Gone if disabled by post_owner
        if (timelineData.getComment_disabled().equals("yes")) {
            holder.comment_btn.setVisibility(View.GONE);
            holder.comments_count.setVisibility(View.GONE);
        } else {
            int j = Integer.valueOf(timelineData.getComments_counter());
            if (j != 0) {
                holder.comments_count.setVisibility(View.VISIBLE);
                if (j == 1) holder.comments_count.setText(j + " comment");
                else holder.comments_count.setText(j + " comments");
            } else {
                //Making txtView invisible if number of comments is 0
                holder.comments_count.setVisibility(View.GONE);
            }
        }

        //Making Share Gone if disabled by post_owner
        if (timelineData.getShare_disabled().equals("yes")) {
            holder.share_btn.setVisibility(View.GONE);
        }

        //Onclick Methods

        holder.fullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                Bundle b = new Bundle();
                b.putString("searchUsername", timelineData.getUsername());
                i.putExtras(b);
                i.putExtra("Open", "search_profile");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                parent.getContext().startActivity(i);
            }
        });

        holder.userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(parent.getContext(), ProfileHolder.class);
                Bundle b = new Bundle();
                b.putString("searchUsername", timelineData.getUsername());
                i.putExtras(b);
                i.putExtra("Open", "search_profile");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                parent.getContext().startActivity(i);
            }
        });


        holder.comment_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.comment_btn.startAnimation(fade_buttons);
                        BottomFrag f = new BottomFrag(holder.comments_count);
                        Bundle b = new Bundle();
                        b.putString("switch", "comments");
                        b.putString("postOwner", timelineData.getUsername());
                        b.putString("comment_post_owner_username", timelineData.getUsername());
                        b.putString("comment_post_owner_time", timelineData.getTime());
                        f.setArguments(b);
                        f.show(activity.getFragmentManager(), "frag");
                    }
                }
        );
        holder.comments_count.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.comments_count.startAnimation(fade_buttons);
                        BottomFrag f = new BottomFrag(holder.comments_count);

                        Bundle b = new Bundle();
                        b.putString("switch", "comments");
                        b.putString("comment_post_owner_username", timelineData.getUsername());
                        b.putString("comment_post_owner_time", timelineData.getTime());
                        f.setArguments(b);
                        f.show(activity.getFragmentManager(), "frag");

                    }
                }
        );
        holder.likes_count.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.likes_count.startAnimation(fade_buttons);
                        BottomFrag f = new BottomFrag();
                        Bundle b = new Bundle();
                        b.putString("switch", "like_list");
                        b.putString("like_list_post_owner_username", timelineData.getUsername());
                        b.putString("like_list_time", timelineData.getTime());
                        f.setArguments(b);
                        f.show(activity.getFragmentManager(), "fra");
                    }
                }
        );
        holder.share_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.share_btn.startAnimation(fade_buttons);

                        //Alert Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                        final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_image, null);
                        RelativeLayout mainCont = vi.findViewById(R.id.dialogImgRelLay);
                        RecyclerView postSettings = vi.findViewById(R.id.fontRecycler);
                        TouchImageView img = vi.findViewById(R.id.dialog_image);
                        img.setVisibility(View.GONE);
                        postSettings.setVisibility(View.VISIBLE);

                        //Setting Wrap Content
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        mainCont.setLayoutParams(params);
                        mainCont.setBackgroundResource(R.drawable.bg_gradient);


                        final ArrayList<String> name = new ArrayList<>();
                        //name.add("Report this");
                        name.add("Share as Message");
                        name.add("Share on Instagram");
                        name.add("Share on Whatsapp");
                        name.add("Share on Facebook");

                        AccountSettingsAdapter adapter = new AccountSettingsAdapter(name, null, null, "triple_dot", null);
                        postSettings.setHasFixedSize(true);
                        postSettings.setLayoutManager(new LinearLayoutManager(parent.getContext()));
                        postSettings.setAdapter(adapter);
                        postSettings.addOnItemTouchListener(
                                new RecyclerItemClickListener(parent.getContext(), postSettings, new RecyclerItemClickListener.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(View view, int position) {

                                        String sAux = "Check this post by " +
                                                holder.fullName.getText().toString() + " in Infinity\n";
                                        sAux = sAux + "https://play.google.com/store/apps/details?id=community.infinity\n\n";

                                        if (name.get(position).equals("Share as Message")) {
                                            dialog.dismiss();
                                            BottomFrag f = new BottomFrag();
                                            Bundle b = new Bundle();
                                            b.putString("switch", "search");
                                            b.putString("postOwnerUsername", timelineData.getUsername());
                                            b.putString("postOwnerTime", timelineData.getTime());
                                            b.putString("postImgLink", arrayList.get(0));
                                            if (arrayList.size() > 1)
                                                b.putString("postMulti", "yes");
                                            else b.putString("postMulti", "no");
                                            if (timelineData.getShort_book_content() != null)
                                                b.putString("postType", "shortBook");
                                            else b.putString("postType", "image");
                                            b.putString("message_search", "yes");
                                            b.putString("download_stat", timelineData.getDownload_disabled());
                                            f.setArguments(b);
                                            f.show(activity.getFragmentManager(), "fran");

                                        } else if (name.get(position).equals("Share on Instagram")) {
                                            String finalSAux = sAux;
                                            Glide.with(parent.getContext()).asBitmap().load(arrayList.get(0)).into(new SimpleTarget<Bitmap>(700, 700) {

                                                @Override
                                                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                                    String path = MediaStore.Images.Media.insertImage(parent.getContext().getContentResolver(), bitmap, "Title", null);
                                                    Uri imageUri = Uri.parse(path);

                                                    Intent shareIntent = new Intent();
                                                    shareIntent.setAction(Intent.ACTION_SEND);
                                                    //Target whatsapp:
                                                    shareIntent.setType("image/*");
                                                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                                    shareIntent.putExtra(Intent.EXTRA_TEXT, finalSAux);
                                                    shareIntent.setPackage("com.instagram.android");

                                                    //shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                    try {
                                                        parent.getContext().startActivity(shareIntent);
                                                    } catch (ActivityNotFoundException ex) {
                                                        Toast.makeText(parent.getContext(),
                                                                "Instagram have not been installed.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            dialog.dismiss();
                                        } else if (name.get(position).equals("Share on Whatsapp")) {
                                            String finalSAux1 = sAux;
                                            Glide.with(parent.getContext()).asBitmap().load(arrayList.get(0)).into(new SimpleTarget<Bitmap>(700, 700) {

                                                @Override
                                                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                                    String path = MediaStore.Images.Media.insertImage(parent.getContext().getContentResolver(), bitmap, "Title", null);
                                                    Uri imageUri = Uri.parse(path);


                                                    Intent shareIntent = new Intent();
                                                    shareIntent.setAction(Intent.ACTION_SEND);
                                                    //Target whatsapp:
                                                    shareIntent.setPackage("com.whatsapp");
                                                    //Add text and then Image URI

                                                    shareIntent.putExtra(Intent.EXTRA_TEXT, finalSAux1);
                                                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                                    shareIntent.setType("image/jpeg");
                                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                    try {
                                                        parent.getContext().startActivity(shareIntent);
                                                    } catch (ActivityNotFoundException ex) {
                                                        Toast.makeText(parent.getContext(),
                                                                "Whatsapp have not been installed.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            dialog.dismiss();
                                        } else if (name.get(position).equals("Share on Facebook")) {
                                            String finalSAux2 = sAux;
                                            Glide.with(parent.getContext()).asBitmap().load(arrayList.get(0)).into(new SimpleTarget<Bitmap>(700, 700) {

                                                @Override
                                                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                                    String path = MediaStore.Images.Media.insertImage(parent.getContext().getContentResolver(), bitmap, "Title", null);
                                                    Uri imageUri = Uri.parse(path);

                                                    Intent shareIntent = new Intent();
                                                    shareIntent.setAction(Intent.ACTION_SEND);
                                                    //Target whatsapp:
                                                    shareIntent.setType("image/*");
                                                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                                    shareIntent.putExtra(Intent.EXTRA_TEXT, finalSAux2);
                                                    shareIntent.setPackage("com.facebook.katana");

                                                    //shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                    try {
                                                        parent.getContext().startActivity(shareIntent);
                                                    } catch (ActivityNotFoundException ex) {
                                                        Toast.makeText(parent.getContext(),
                                                                "Facebook have not been installed.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
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
                }
        );

        holder.like_btn_outline.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                        holder.like_btn_outline.setVisibility(View.INVISIBLE);
                        holder.like_btn.startAnimation(an);
                        holder.like_btn.setVisibility(View.VISIBLE);
                        item = new ListItem(timelineData.getUsername(), timelineData.getTime(), holder.fullName, "likes", null, null, null, parent.getContext());
                        //sending holder.fullName to listItem for getting  parent.getContext()
                        if (holder.likes_count.getVisibility() == View.GONE) {
                            holder.likes_count.setVisibility(View.VISIBLE);
                            holder.likes_count.setText("1" + " " + "like");
                        } else {
                            int i = Integer.valueOf(holder.likes_count.getText().toString().substring(0, holder.likes_count.getText().toString().indexOf(" "))) + 1;
                            holder.likes_count.setText(i + " " + "likes");

                        }


                    }
                }
        );
        holder.like_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                        an.setInterpolator(new ReverseInterpolator());
                        holder.like_btn.startAnimation(an);
                        holder.like_btn_outline.startAnimation(an);
                        holder.like_btn.setVisibility(View.INVISIBLE);
                        holder.like_btn_outline.setVisibility(View.VISIBLE);

                        item = new ListItem(timelineData.getUsername(), timelineData.getTime(), holder.fullName, "unlike", null, null, null, parent.getContext());
                        //sending holder.username for getting  parent.getContext()

                        int i = Integer.valueOf(holder.likes_count.getText().toString().substring(0, holder.likes_count.getText().toString().indexOf(" "))) - 1;
                        if (i == 0) {
                            holder.likes_count.setVisibility(View.GONE);
                        } else if (i == 1) {
                            holder.likes_count.setText(i + " " + "like");
                        } else {
                            holder.likes_count.setText(i + " " + "likes");
                        }
                    }
                }
        );
        holder.star_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.star_btn_outline.setVisibility(View.VISIBLE);
                        holder.star_btn.setVisibility(View.GONE);
                        item = new ListItem(timelineData.getUsername(), timelineData.getTime(), holder.fullName, "unstar", null, null, null, parent.getContext());

                    }
                }
        );
        holder.star_btn_outline.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.star_btn_outline.setVisibility(View.GONE);
                        holder.star_btn.startAnimation(fade_buttons);
                        holder.star_btn.setVisibility(View.VISIBLE);
                        item = new ListItem(timelineData.getUsername(), timelineData.getTime(), holder.fullName, "star", null, null, null, parent.getContext());
                        //  Toast.makeText( parent.getContext(),"Starred",Toast.LENGTH_SHORT).show();
                    }
                }
        );


        holder.triple_dot.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.performClick();
                }
            }
        });

        holder.triple_dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_image, null);
                RelativeLayout mainCont = vi.findViewById(R.id.dialogImgRelLay);
                RecyclerView postSettings = vi.findViewById(R.id.fontRecycler);
                TouchImageView img = vi.findViewById(R.id.dialog_image);
                img.setVisibility(View.GONE);
                postSettings.setVisibility(View.VISIBLE);

                //Setting Wrap Content
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                mainCont.setLayoutParams(params);
                mainCont.setBackgroundResource(R.drawable.bg_gradient);


                final ArrayList<String> name = new ArrayList<>();
                //name.add("Report this");
                if (timelineData.getUsername().equals(Hawk.get("myUserName")) || Hawk.get("myUserName").equals("infinity")) {
                    name.add("Edit");
                    name.add("Delete");
                    if (timelineData.getShare_disabled() != null && timelineData.getShare_disabled().equals("no"))
                        name.add("Share");
                    if (holder.comment_btn.getVisibility() == View.GONE) name.add("Enable comment");
                    else name.add("Disable comment");
                    if (holder.share_btn.getVisibility() == View.GONE) name.add("Enable share");
                    else name.add("Disable share");
                    if (timelineData.getDownload_disabled().equals("yes"))
                        name.add("Enable download");
                    else name.add("Disable download");
                    //name.add("Disable comment");
                    //name.add("Disable share");
                    if (timelineData.getPrivate_post_stat().equals("yes"))
                        name.add("Make this post public");
                    else name.add("Make this post private");
                    name.add("Download");
                } else {
                    if (timelineData.getShare_disabled() != null && timelineData.getShare_disabled().equals("no"))
                        name.add("Share");
                    name.add("Report this post");
                    if (timelineData.getDownload_disabled().equals("no")) name.add("Download");
                }


                AccountSettingsAdapter adapter = new AccountSettingsAdapter(name, null, null, "triple_dot", null);
                postSettings.setHasFixedSize(true);
                postSettings.setLayoutManager(new LinearLayoutManager(parent.getContext()));
                postSettings.setAdapter(adapter);
                postSettings.addOnItemTouchListener(
                        new RecyclerItemClickListener(parent.getContext(), postSettings, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                final Animation rev_an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                                final Animation an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                                rev_an.setInterpolator(new ReverseInterpolator());

                                //If statements

                                if (name.get(position).equals("Edit")) {
                                    dialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                                    final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_caption_lay, null);
                                    final EditText editCaption = vi.findViewById(R.id.textFeelTimelineEdit);
                                    final ImageButton back = vi.findViewById(R.id.backTimeline);
                                    final ImageButton done = vi.findViewById(R.id.doneTimeline);
                                    final TextView mention_btn = vi.findViewById(R.id.mentionTxtEditTimeline);
                                    final DragToClose dragToClose = vi.findViewById(R.id.dragViewEditTimeline);

                                    //Drag to Close option
                                    dragToClose.setDragListener(new DragListener() {
                                        @Override
                                        public void onStartDraggingView() {
                                        }

                                        @Override
                                        public void onViewCosed() {
                                            dialog.dismiss();
                                        }
                                    });


                                    //Tint
                                    ButtonTint tint = new ButtonTint("white");
                                    tint.setTint(back);
                                    tint.setTint(done);

                                    if (holder.caption.getText().toString() != null) {
                                        tagHelperTripleDot = HashTagHelper.Creator.create(parent.getContext().getResources().getColor(R.color.tagColor), new HashTagHelper.OnHashTagClickListener() {
                                            @Override
                                            public void onHashTagClicked(String hashTag, char sign) {
                                            }
                                        }, additionalSymbols);
                                        tagHelperTripleDot.handle(editCaption);
                                        editCaption.setText(holder.caption.getText().toString());
                                    }


                                    //OnClik
                                    back.setOnClickListener(view1 -> dialog.dismiss());

                                    mention_btn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            mention_btn.startAnimation(an);
                                            BottomFrag f = new BottomFrag();
                                            BottomFrag.tagSelector = editCaption;
                                            Bundle b = new Bundle();
                                            b.putString("switch", "search");
                                            b.putString("tagSelect", "yes");
                                            //Setting Target Fragment for getting result
                                            b.putString("timelineEdit", "yes");
                                            f.setArguments(b);
                                            f.show(activity.getFragmentManager(), "fra");
                                        }
                                    });
                                    done.setOnClickListener(view12 -> {
                                        dialog.dismiss();
                                        if (editCaption.getText().toString() != null && editCaption.getText().toString().length() > 0) {
                                            holder.caption.setVisibility(View.VISIBLE);
                                            holder.caption.setText(editCaption.getText().toString());

                                            //Aligning Text
                                            holder.caption.setGravity(Gravity.START);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                                holder.caption.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                                            }

                                            holder.caption.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    int lineCount = holder.caption.getLineCount();
                                                    // Use lineCount here
                                                    if (lineCount > 2) {
                                                        holder.showMoreTxt.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            });
                                            holder.showMoreTxt.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if (isTextCheck) {
                                                        holder.caption.setMaxLines(45);
                                                        holder.showMoreTxt.setText("...less");
                                                        isTextCheck = false;
                                                    } else {
                                                        holder.caption.setMaxLines(2);
                                                        holder.showMoreTxt.setText("...more");
                                                        isTextCheck = true;
                                                    }
                                                }
                                            });
                                            //Copying Text OnLongClick
                                            holder.caption.setOnLongClickListener(new View.OnLongClickListener() {
                                                @Override
                                                public boolean onLongClick(View view) {
                                                    ClipboardManager clipboard = (ClipboardManager) parent.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("message", timelineData.getCaption());
                                                    clipboard.setPrimaryClip(clip);
                                                    Toast.makeText(parent.getContext(), "Text Copied", Toast.LENGTH_SHORT).show();
                                                    return false;
                                                }
                                            });

                                            //Tag Clicking
                                            HashTagHelper mTextHashTagHelper = HashTagHelper.Creator.create(parent.getContext().getResources().getColor(R.color.tagColor), new HashTagHelper.OnHashTagClickListener() {
                                                @Override
                                                public void onHashTagClicked(String hashTag, char sign) {
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
                                                }
                                            }, additionalSymbols);
                                            mTextHashTagHelper.handle(holder.caption);

                                            JSONObject ob = new JSONObject();
                                            try {
                                                socket.disconnect();
                                                socket.connect();
                                                ob.put("change_caption_post_username", timelineData.getUsername());
                                                ob.put("change_caption_post_time", timelineData.getTime());
                                                ob.put("change_caption_content", URLEncoder.encode(editCaption.getText().toString().trim(), "UTF-8"));
                                                if (tagHelperTripleDot.getOnlyTags(true, "#") != null &&
                                                        tagHelperTripleDot.getOnlyTags(true, "#").size() > 0) {
                                                    Gson gson = new Gson();
                                                    ob.put("change_caption_tags", gson.toJson(tagHelperTripleDot.getOnlyTags(true, "#")));
                                                }
                                                socket.emit("data", ob);
                                                socket.on("change_caption_status", new Emitter.Listener() {
                                                    @Override
                                                    public void call(Object... args) {
                                                        JSONObject object = (JSONObject) args[0];
                                                        try {
                                                            if (object.getString("change_caption_status").equals("yes")) {
                                                                socket.disconnect();
                                                                dialog.dismiss();
                                                            } else {
                                                                Toast.makeText(parent.getContext(), "Sorry some error occured . Please try again.",
                                                                        Toast.LENGTH_SHORT).show();

                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } else {
                                            holder.showMoreTxt.setVisibility(View.GONE);
                                        }
                                    });

                                    builder.setView(vi);
                                    dialog = builder.create();
                                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

                                    dialog.show();
                                    dialog.setOnDismissListener(
                                            dialog -> {
                                                ViewGroup viewGroup = (ViewGroup) vi.getParent();
                                                viewGroup.removeAllViews();


                                            }
                                    );
                                    DisplayMetrics metrics = new DisplayMetrics();
                                    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                    dialog.getWindow().setBackgroundDrawableResource(RememberTextStyle.themeResource);
                                } else if (name.get(position).equals("Share")) {
                                    dialog.dismiss();
                                    holder.share_btn.performClick();
                                } else if (name.get(position).equals("Disable download")) {

                                    //Changing Timelinedata list
                                    timelineData.setDownload_disabled("yes");

                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("disable_download_username", timelineData.getUsername());
                                        ob.put("disable_download_post_time", timelineData.getTime());
                                        socket.emit("data", ob);
                                        socket.on("disable_download_stat", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob = (JSONObject) args[0];
                                                try {
                                                    if (ob.getString("status").equals("yes")) {
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(parent.getContext(), "Download is disabled.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                } else if (name.get(position).equals("Enable download")) {

                                    //Changing Timelinedata list
                                    timelineData.setDownload_disabled("no");

                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("enable_download_username", timelineData.getUsername());
                                        ob.put("enable_download_post_time", timelineData.getTime());
                                        socket.emit("data", ob);
                                        socket.on("enable_download_stat", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob = (JSONObject) args[0];
                                                try {
                                                    if (ob.getString("status").equals("yes")) {
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(parent.getContext(), "Download is enabled.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                } else if (name.get(position).equals("Disable comment")) {
                                    holder.comment_btn.startAnimation(rev_an);
                                    holder.comments_count.startAnimation(rev_an);
                                    holder.comment_btn.setVisibility(View.GONE);
                                    holder.comments_count.setVisibility(View.GONE);
                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("disable_comment_username", timelineData.getUsername());
                                        ob.put("disable_comment_post_time", timelineData.getTime());
                                        socket.emit("data", ob);
                                        socket.on("disable_comment_stat", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob = (JSONObject) args[0];
                                                try {
                                                    if (ob.getString("status").equals("yes")) {
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(parent.getContext(), "Comment is disabled.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                } else if (name.get(position).equals("Enable comment")) {
                                    holder.comment_btn.startAnimation(an);
                                    holder.comments_count.startAnimation(an);
                                    holder.comment_btn.setVisibility(View.VISIBLE);
                                    holder.comments_count.setVisibility(View.VISIBLE);
                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("enable_comment_username", timelineData.getUsername());
                                        ob.put("enable_comment_post_time", timelineData.getTime());
                                        socket.emit("data", ob);
                                        socket.on("enable_comment_stat", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob = (JSONObject) args[0];
                                                try {
                                                    if (ob.getString("status").equals("yes")) {
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(parent.getContext(), "Comment is enabled.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                } else if (name.get(position).equals("Disable share")) {
                                    holder.share_btn.startAnimation(rev_an);
                                    holder.share_btn.setVisibility(View.GONE);
                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("disable_share_username", timelineData.getUsername());
                                        ob.put("disable_share_post_time", timelineData.getTime());
                                        socket.emit("data", ob);
                                        socket.on("disable_share_stat", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob = (JSONObject) args[0];
                                                try {
                                                    if (ob.getString("status").equals("yes")) {
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(parent.getContext(), "Share is disabled.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                } else if (name.get(position).equals("Enable share")) {
                                    holder.share_btn.startAnimation(an);
                                    holder.share_btn.setVisibility(View.VISIBLE);
                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("enable_share_username", timelineData.getUsername());
                                        ob.put("enable_share_post_time", timelineData.getTime());
                                        socket.emit("data", ob);
                                        socket.on("enable_share_stat", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob = (JSONObject) args[0];
                                                try {
                                                    if (ob.getString("status").equals("yes")) {
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(parent.getContext(), "Share is enabled.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                } else if (name.get(position).equals("Make this post private")) {
                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("make_post_private_username", timelineData.getUsername());
                                        ob.put("make_post_private_post_time", timelineData.getTime());
                                        socket.emit("data", ob);
                                        socket.on("make_post_private_stat", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob = (JSONObject) args[0];
                                                try {
                                                    if (ob.getString("status").equals("yes")) {
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                timelineData.setPrivate_post_stat("yes");
                                                                Toast.makeText(parent.getContext(), "Post is private now.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                } else if (name.get(position).equals("Make this post public")) {
                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        ob.put("make_post_public_username", timelineData.getUsername());
                                        ob.put("make_post_public_post_time", timelineData.getTime());
                                        socket.emit("data", ob);
                                        socket.on("make_post_public_stat", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                JSONObject ob = (JSONObject) args[0];
                                                try {
                                                    if (ob.getString("status").equals("yes")) {
                                                        Needle.onMainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                timelineData.setPrivate_post_stat("no");
                                                                Toast.makeText(parent.getContext(), "This post is public now.", Toast.LENGTH_SHORT).show();
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
                                    dialog.dismiss();
                                } else if (name.get(position).equals("Delete")) {
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

                                    //Onclick
                                    ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            holder.itemView.startAnimation(rev_an);
                                            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                                                totalList.remove(holder.getAdapterPosition());
                                                notifyItemRemoved(holder.getAdapterPosition());
                                                notifyItemRangeChanged(holder.getAdapterPosition(), totalList.size());
                                            }
                                            JSONObject ob = new JSONObject();
                                            try {
                                                ob.put("del_post_username", timelineData.getUsername());
                                                ob.put("del_post_time", timelineData.getTime());
                                                socket.disconnect();
                                                socket.connect();
                                                socket.emit("data", ob);
                                                socket.on("del_post_stat", new Emitter.Listener() {
                                                    @Override
                                                    public void call(Object... args) {
                                                        JSONObject ob = (JSONObject) args[0];
                                                        try {
                                                            if (ob.getString("status").equals("yes")) {
                                                                socket.disconnect();
                                                                if (report_type != null) {
                                                                    socket.connect();
                                                                    JSONObject ov = new JSONObject();
                                                                    ov.put("del_report_post_owner_name", timelineData.getUsername());
                                                                    ov.put("del_report_post_time", timelineData.getTime());
                                                                    ov.put("del_report_type", "post");
                                                                    socket.emit("data", ov);
                                                                    socket.on("report_del_stat", new Emitter.Listener() {
                                                                        @Override
                                                                        public void call(Object... args) {
                                                                            JSONObject ob = (JSONObject) args[0];
                                                                            try {
                                                                                if (ob.getString("status").equals("yes")) {
                                                                                    Reports.reloadReports();
                                                                                    socket.disconnect();
                                                                                }
                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

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
                                } else if (name.get(position).equals("Report this post")) {

                                    Needle.onBackgroundThread().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            socket.disconnect();
                                            socket.connect();

                                            JSONObject ob = new JSONObject();
                                            try {
                                                ob.put("report_reporter_name", Hawk.get("myUserName"));
                                                ob.put("report_post_time", timelineData.getTime());
                                                ob.put("report_post_owner_name", timelineData.getUsername());
                                                ob.put("report_type", "post");
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
                                            Toast.makeText(parent.getContext(), "We would check your report very soon .", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    });
                                } else if (name.get(position).equals("Download")) {
                                    ConnectivityManager conMgri = (ConnectivityManager) parent.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo netInfoi = conMgri.getActiveNetworkInfo();
                                    if (netInfoi == null) {
                                        Toast.makeText(parent.getContext(), "You're not connected to internet", Toast.LENGTH_LONG).show();
                                    } else {
                                        for (int i = 0; i < arrayList.size(); i++) {
                                            int finalI = i;
                                            Glide.with(parent.getContext()).asBitmap().load(arrayList.get(i)).into(new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                    super.onLoadFailed(errorDrawable);
                                                    Toast.makeText(parent.getContext(), "You're not connected to internet", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    Needle.onBackgroundThread().execute(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            Random generator = new Random();
                                                            int n = 10000;
                                                            n = generator.nextInt(n);
                                                            String fname = "Image-" + n + ".jpg";
                                                            try {
                                                                Needle.onMainThread().execute(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (finalI == 0)
                                                                            Toast.makeText(parent.getContext(), "Downloaded Successfully", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                                saveImage(fname, resource);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                        }
                                    }
                                    dialog.dismiss();
                                }

                                //dialog.dismiss();

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
    }catch (Exception e){}
    }
    void saveImage(String imgName, Bitmap bm) throws IOException {
        //Create Path to save Image
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Infinity"); //Creates app specific folder
        path.mkdirs();
        File imageFile = new File(path, imgName + ".jpeg"); // Imagename.png
        FileOutputStream out = new FileOutputStream(imageFile);
        try {
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out); // Compress Image
            out.flush();
            out.close();


            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile( parent.getContext(), new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch (Exception e) {
            throw new IOException();
        }
    }
    @Override
    public int getItemCount() {
        //if(b){return 1;}
        //else{
        return this.totalList.size();//}
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }



    public static class CustomRecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView middle_heart;
        CircleImageView userImg;
        AppCompatImageButton star_btn,star_btn_outline,share_btn,comment_btn,like_btn,like_btn_outline,triple_dot,short_book_indicator;
        //CircleImageView userImg;
        HeightWrappingViewPager pager;
        CircleIndicator indicator;
        TextView time_stamp,likes_count,fullName,comments_count,showMoreTxt;
        TextView caption;
        FrameLayout header;
        ConstraintLayout likeContainer;
        TextView readBookTxt;

        public CustomRecyclerViewHolder(View itemView) {
            super(itemView);
            userImg=itemView.findViewById(R.id.userImage);
            like_btn=itemView.findViewById(R.id.like_btn);
            like_btn_outline=itemView.findViewById(R.id.like_btn_outline);
            star_btn=itemView.findViewById(R.id.starred_btn);
            star_btn_outline=itemView.findViewById(R.id.starred_btn_outline);
            share_btn=itemView.findViewById(R.id.share_btn);
            comment_btn=itemView.findViewById(R.id.comment_btn);
            pager=itemView.findViewById(R.id.multiImgContainer);
            indicator = itemView.findViewById(R.id.indicator);
            caption=itemView.findViewById(R.id.capShow);
            time_stamp=itemView.findViewById(R.id.timestamp);
            likes_count=itemView.findViewById(R.id.no_of_likes);
            fullName=itemView.findViewById(R.id.fullnameTimeLine);
            comments_count=itemView.findViewById(R.id.no_of_comments);
            middle_heart=itemView.findViewById(R.id.middleHeart);
            triple_dot=itemView.findViewById(R.id.triple_dot);
            likeContainer=itemView.findViewById(R.id.likeContainer);
            header=itemView.findViewById(R.id.editHeaderTimeline);
            readBookTxt=itemView.findViewById(R.id.readBookTxt);
            short_book_indicator=itemView.findViewById(R.id.short_book_indicatorTimeline);
            showMoreTxt=itemView.findViewById(R.id.showMoreTxtTimeline);

            final Typeface bold_font = Typeface.createFromAsset((fullName.getContext()).getAssets(), "fonts/"+"Raleway-SemiBold.ttf");
            final Typeface reg_font = Typeface.createFromAsset((fullName.getContext()).getAssets(), "fonts/"+"Raleway-Light.ttf");
            fullName.setTypeface(bold_font);

            Shader textShader=new LinearGradient(0, 0, 0, 45,
                    new int[]{Color.parseColor("#43cea2"),Color.parseColor("#185a9d")},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            //fullName.getPaint().setShader(textShader);

            likes_count.setTypeface(reg_font);
            comments_count.setTypeface(reg_font);
            caption.setTypeface(reg_font);
            time_stamp.setTypeface(reg_font);


            //Setting Username Bold
            //userName.setTypeface(Typeface.create(userName.getTypeface(), Typeface.BOLD)); ;

            //Button Tint
            ButtonTint tint=new ButtonTint("white");
            tint.setTint(triple_dot);
            tint.setTint(comment_btn);
            tint.setTint(share_btn);
            tint.setTint(star_btn);
            tint.setTint(star_btn_outline);
            tint.setTint(short_book_indicator);

            //Onclik



            //Clearing Animations
            itemView.clearAnimation();






            //tv.setMovementMethod(new ScrollingMovementMethod());
        }
    }
    @Override
    public void onViewDetachedFromWindow(CustomRecyclerViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    public static String getTimeAgo(long time) {
        int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
    private void update(TextView mTextView) {
        final CharSequence text = mPagination.get(mCurrentIndex);
        if(text != null) mTextView.setText(text);
    }

}