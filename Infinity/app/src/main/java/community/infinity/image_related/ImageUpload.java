package community.infinity.image_related;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.novoda.merlin.MerlinsBeard;
import com.orhanobut.hawk.Hawk;
import com.pixplicity.htmlcompat.HtmlCompat;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import community.infinity.BottomFrags.BottomFrag;
import community.infinity.CurrentSociety;
import community.infinity.network_related.FileUploadService;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.network_related.ServiceGenerator;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.Home_Screen;
import community.infinity.activities.ProfileHolder;
import community.infinity.adapters.StarredAdapter;
import community.infinity.writing.Pagination;
import community.infinity.writing.RememberTextStyle;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.HashTagHelper;
import custom_views_and_styles.ToggleButton;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import ooo.oxo.library.widget.TouchImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Srinu on 05-11-2017.
 */

public class ImageUpload extends Fragment implements View.OnClickListener  {


    private ImageButton back,share;
    private ArrayList<Bitmap> bit=new ArrayList<>();
    private ArrayList<String> img_names=new ArrayList<>();
    private Animation an;
    private AppCompatEditText caption=null;
    private ProfileHolder profileHolder =new ProfileHolder();
    private RelativeLayout info_toggle_container;
    private AVLoadingIndicatorView aviLoader;
    private FrameLayout progressContainer;
    private TextView progressTxt;
    private StarredAdapter adapter;
    private Button readBook;
    private TextView mention_btn;
    private AlertDialog dialog;
    private String captionString,my_username,upload_time;
    private HashTagHelper mTextHashTagHelper;
    private ToggleButton downloadDisableToggle,shareDisableToggle,makePostPrivateToggle,
            commentDisableToggle,info_toggle,challenge_toggle,tips_toggle,opportunity_toggle;
    private boolean download_disabled_boolean=false,comment_disabled_boolean=false,share_disabled_boolean=false,private_acc_boolean=false;
    private RecyclerView horzRview;
    View v;

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
    public ImageUpload() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("status",handleIncomingMessage);
        //parts= FastImageUploader.parts;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(v==null){
         v=inflater.inflate(R.layout.image_upload,container,false);
        back=v.findViewById(R.id.back_of_captions);
        share=v.findViewById(R.id.share_of_captions);
        caption=v.findViewById(R.id.textFeel);
        readBook=v.findViewById(R.id.readCap);
        DragToClose dragToClose=v.findViewById(R.id.dragViewImgCap);
        commentDisableToggle=v.findViewById(R.id.commentDisableToggle);
        shareDisableToggle=v.findViewById(R.id.shareDisableToggle);
        downloadDisableToggle=v.findViewById(R.id.downloadDisableToggle);
        makePostPrivateToggle=v.findViewById(R.id.postPrivateToggle);
        challenge_toggle=v.findViewById(R.id.challengeToggle);
        tips_toggle=v.findViewById(R.id.tipsToggle);
        opportunity_toggle=v.findViewById(R.id.opportunityToggle);
        horzRview=v.findViewById(R.id.horizontalRViewImgCaption);
        mention_btn=v.findViewById(R.id.mentionTxtImgCap);
        info_toggle_container=v.findViewById(R.id.info_toggle_container);
        info_toggle=v.findViewById(R.id.infoToggle);
        aviLoader=v.findViewById(R.id.aviLoaderImgUpload);
        progressContainer=v.findViewById(R.id.loadingViewContainerImgUpload);
        progressTxt=v.findViewById(R.id.processingTxtImgUpload);
        final RelativeLayout parentLay=v.findViewById(R.id.imageCapCont);

        //Setting Bg
            parentLay.setBackgroundResource(RememberTextStyle.themeResource);



        
            
            //Building Hawk
            Hawk.init(v.getContext()).build();

            //Getting my Username
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());

            if(Hawk.get("myUserName")!=null) my_username = Hawk.get("myUserName");
            else my_username = sharedPref.getString("username", null);

            img_names=getArguments().getStringArrayList("img_names");
            upload_time=getArguments().getString("upload_time");




            //Making info Toggle visible
            if(my_username.equals("infinity")||my_username.equals("infinity.admin")){
                info_toggle_container.setVisibility(View.VISIBLE);
            }

            //Drag to Close option
            dragToClose.setDragListener(new DragListener() {
                @Override
                public void onStartDraggingView() {}

                @Override
                public void onViewCosed() {
                    int index = getActivity().getFragmentManager().getBackStackEntryCount() - 1;
                    FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(index);
                    getFragmentManager().popBackStack(backEntry.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
            Bundle b=getArguments();
            if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")) {
                bit.add(RememberTextStyle.bitmap);
            }
            else bit=b.getParcelableArrayList("cropped");


            //Removing data from bundle
            if(getArguments().containsKey("cropped"))getArguments().remove("cropped");
            getArguments().remove("upload_time");
            getArguments().remove("img_names");

            //Horizontal RView
            adapter=new StarredAdapter(bit);
            horzRview.setHasFixedSize(true);
            horzRview.setLayoutManager(new LinearLayoutManager(v.getContext(),LinearLayoutManager.HORIZONTAL,false));
            horzRview.setAdapter(adapter);
            horzRview.setVisibility(View.GONE);



            aviLoader.smoothToShow();
            progressContainer.setVisibility(View.VISIBLE);
            progressTxt.setText("Setting Data ...");

            int time;
            if(bit!=null&&bit.size()==1) time=1000;
            else if(bit!=null&&bit.size()>1 &&bit.size()<=3) time=2000;
            else time=4500;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //pDialog.dismiss();
                    progressContainer.setVisibility(View.GONE);
                    horzRview.setVisibility(View.VISIBLE);
                }
            },time);

            horzRview.addOnItemTouchListener(new RecyclerItemClickListener(v.getContext(), horzRview, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Glide.with(v.getContext()).asBitmap().load(bit.get(position)).into(new SimpleTarget<Bitmap>(700, 700) {

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // Opening Dialog
                            android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(v.getContext());
                            final View dialog_view= LayoutInflater.from(back.getContext()).inflate(R.layout.dialog_image,null);
                            builder.setView(dialog_view);
                            dialog=builder.create();
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.getWindow().setDimAmount(0.9f);
                            final TouchImageView img=dialog_view.findViewById(R.id.dialog_image);
                            final DragToClose dragToClose=dialog_view.findViewById(R.id.dragViewDialogImg);

                            //Setting Bitmap
                            img.setImageBitmap(resource);

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
                                            ViewGroup v=(ViewGroup)dialog_view.getParent();
                                            v.removeAllViews();
                                        }
                                    }
                            );
                        }
                    });
                }

                @Override
                public void onLongItemClick(View view, int position) {
                }
            }));



            //Setting Tint
            ButtonTint white_tint=new ButtonTint("white");
            white_tint.setTint(back);
            white_tint.setTint(share);

            //Showing Instructions
            Snackbar snackbar = Snackbar.make(back, "Click on the mention button to mention people and tags easily", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.parseColor("#43cea2"));
            View sbView = snackbar.getView();
            sbView.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.textColor));
            TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.parseColor("#001919"));
            snackbar.show();





            //Toggle Listeners
            downloadDisableToggle.setOnToggleChanged(new custom_views_and_styles.ToggleButton.OnToggleChanged() {
                @Override
                public void onToggle(boolean isOn) {
                    download_disabled_boolean = isOn;
                }
            });
            commentDisableToggle.setOnToggleChanged(new custom_views_and_styles.ToggleButton.OnToggleChanged() {
                @Override
                public void onToggle(boolean isOn) {
                    comment_disabled_boolean = isOn;
                }
            });
            shareDisableToggle.setOnToggleChanged(new ToggleButton.OnToggleChanged(){
                @Override
                public void onToggle(boolean isOn) {
                    share_disabled_boolean = isOn;
                }
            });
            makePostPrivateToggle.setOnToggleChanged(new ToggleButton.OnToggleChanged(){
                @Override
                public void onToggle(boolean isOn) {
                    private_acc_boolean = isOn;
                }
            });


            char[] additionalSymbols = new char[]{'&', '.', '_'};

            mTextHashTagHelper=HashTagHelper.Creator.create(v.getContext().getResources().getColor(R.color.tagColor), new HashTagHelper.OnHashTagClickListener() {
                @Override
                public void onHashTagClicked(String hashTag,char sign) {
                }
            },additionalSymbols);
            mTextHashTagHelper.handle(caption);



        //OnClik
            back.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.performClick();
                    }
                }
            });
            share.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.performClick();
                    }
                }
            });
        back.setOnClickListener(this);
        share.setOnClickListener(this);
        readBook.setOnClickListener(this);
        mention_btn.setOnClickListener(this);
        //////////////



        //Checking Book or writing
            if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")) readBook.setVisibility(View.VISIBLE);
            else readBook.setVisibility(View.GONE);



        profileHolder.saveDataProfile(b);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                    int index = getActivity().getFragmentManager().getBackStackEntryCount() - 1;
                    FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(index);
                    getFragmentManager().popBackStack(backEntry.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    return true;
                }
                return false;
            }
        } );

        }
        else{}

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1919:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    ArrayList<String> tag = bundle.getStringArrayList("selectedTags");
                    for (int i = 0; i < tag.size(); i++) {
                        caption.setText(caption.getText() + tag.get(i) + " ");
                    }
                    // Showning Keyboard
                    new Handler().postDelayed(new Runnable() {

                        public void run() {
                            caption.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                            caption.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                        }
                    }, 200);

                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        an= AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons);

        switch (v.getId()){
            case R.id.share_of_captions:
                share.startAnimation(an);


                MerlinsBeard merlinsBeard=MerlinsBeard.from(v.getContext());
                if(merlinsBeard.isConnected()){
                    aviLoader.smoothToShow();
                    progressContainer.setVisibility(View.VISIBLE);
                    progressTxt.setText("Uploading ...");

                    SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                    SharedPreferences.Editor editor=sh.edit();
                    editor.remove("upload");
                    editor.apply();

                    Hawk.delete("upload");



                    //Getting text of editText
                    try {
                        captionString=URLEncoder.encode(caption.getText().toString().trim(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                            Gson gson=new Gson();
                            final JSONObject obji=new JSONObject();
                            try {
                                //obji.put("uploads",convert(bit));
                                obji.put("uploads_username", my_username);

                                if(Hawk.get("myFullName")!=null) obji.put("uploads_fullname",Hawk.get("myFullName"));
                                else obji.put("uploads_fullname",sh.getString("myFullName",null));

                                if(my_username.equals("infinity")) {
                                    if(info_toggle.isToggleOn()) obji.put("upload_type","info");
                                    else if(challenge_toggle.isToggleOn()) obji.put("upload_type", "challenge");
                                    else if(tips_toggle.isToggleOn()) obji.put("upload_type", "tips");
                                    else if(opportunity_toggle.isToggleOn()) obji.put("upload_type", "opportunities");
                                }

                                if(CurrentSociety.home_society!=null) obji.put("upload_society_name", CurrentSociety.home_society.toLowerCase());
                                else obji.put("upload_society_name", "home");

                                if(Hawk.get("myProfilePic")!=null) obji.put("uploads_profPic",Hawk.get("myProfilePic"));
                                else obji.put("uploads_profPic",sh.getString("myProfilePic",null));

                                obji.put("uploads_time",upload_time);
                                obji.put("uploads_img_names",gson.toJson(img_names));
                                if(download_disabled_boolean)obji.put("download_disabled","yes");
                                else obji.put("download_disabled","no");

                                if(comment_disabled_boolean)obji.put("comment_disabled","yes");
                                else obji.put("comment_disabled","no");

                                if(share_disabled_boolean)obji.put("share_disabled","yes");
                                else obji.put("share_disabled","no");

                                if(private_acc_boolean)obji.put("private_post_stat","yes");
                                else obji.put("private_post_stat","no");


                                if(mTextHashTagHelper.getAllTags(true).size()>0) {

                                    obji.put("uploads_tags",gson.toJson(mTextHashTagHelper.getAllTags(true)));
                                    //Toast.makeText(v.getContext(),mTextHashTagHelper.getAllHashTags(true).toString(),Toast.LENGTH_SHORT).show();
                                }
                                if(captionString!=null)  obji.put("uploads_text",captionString);
                                else  obji.put("uploads_text","");
                                if (Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes"))
                                    obji.put("short_book_content", Hawk.get("html").toString());
                                //if(Hawk.get("bookTitle").toString().equals("yes")) obji.put("short_book_content",Hawk.get("html").toString());



                                socket.emit("data",obji);
                                socket.on("upload_status", new Emitter.Listener() {
                                    @Override
                                    public void call(Object... args) {
                                        JSONObject ob=(JSONObject)args[0];
                                        try {
                                            if(ob.getString("status").equals("yes")){
                                                        Needle.onBackgroundThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if(Hawk.contains("bookTitle")&&
                                                                        Hawk.get("bookTitle").toString().equals("yes")){
                                                                    Hawk.delete("titleOfBook");
                                                                    Hawk.delete("bookContent");
                                                                    Hawk.delete("writerNameFont");
                                                                    Hawk.delete("bookTitle");
                                                                }

                                                                else {
                                                                    Hawk.delete("feelings");
                                                                }
                                                            }
                                                        });
                                                //pDialog.dismiss();
                                                SharedPreferences.Editor editor=sh.edit();
                                                editor.remove("upload_time");
                                                editor.apply();


                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                Needle.onMainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressContainer.setVisibility(View.GONE);

                                                Intent i=new Intent(getActivity(),Home_Screen.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                v.getContext().startActivity(i);


                                                //getActivity().finish();

                                                //

                                            }
                                        },3000);

                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                }
                else{
                    Snackbar snackbar = Snackbar.make(back, "You're not connected to internet .", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.parseColor("#43cea2"));
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.textColor));
                    TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.parseColor("#001919"));
                    snackbar.show();
                }

                break;

            case R.id.readCap:
                AlertDialog.Builder builder = new AlertDialog.Builder(back.getContext());
                final View vi = LayoutInflater.from(back.getContext()).inflate(R.layout.dialog_image, null);
                RelativeLayout rl = vi.findViewById(R.id.dialogImgRelLay);
                //final ScrollView scrollView = vi.findViewById(R.id.dialogScrollView);
                final TextView caption = vi.findViewById(R.id.dialog_text);
                final TextView cap_vert=vi.findViewById(R.id.dialog_text_vertical);
                final RelativeLayout countBox=vi.findViewById(R.id.pgCounter);
                final TextView curPg=vi.findViewById(R.id.curPg);
                final TextView totPg=vi.findViewById(R.id.totPg);
                final FloatingActionButton close=vi.findViewById(R.id.closeButtonBook);
                final FloatingActionButton horz_btn=vi.findViewById(R.id.horz_ButtonBook);
                final FloatingActionButton vert_btn=vi.findViewById(R.id.vert_ButtonBook);
                final FrameLayout main_cont=vi.findViewById(R.id.bookTxtFrame);
                final ScrollView scrollView=vi.findViewById(R.id.scrollBook);
                final RelativeLayout bottomBtn=vi.findViewById(R.id.ll_buttonsBook);
                main_cont.setVisibility(View.VISIBLE);
                bottomBtn.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.VISIBLE);
                caption.setBackgroundResource(R.drawable.circle);
                scrollView.setBackgroundResource(R.drawable.circle);
                main_cont.setBackgroundResource(R.drawable.book_bg);
                final Spanned htmlString = HtmlCompat.fromHtml(v.getContext(),Hawk.get("html").toString(), 0);
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
                                totPg.setText(mPagination.size()+"");
                                curPg.setText("1");
                                if(caption.getTextSize()==32.0f) caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, caption.getTextSize()-3.5f);
                                //  Toast.makeText(caption.getContext(),caption.getTextSize()+"",Toast.LENGTH_SHORT).show();
                                //caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, caption.getTextSize()-1f);
                                // Toast.makeText(v.getContext(),mPagination.size()+"",Toast.LENGTH_SHORT).show();
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
                            if(event.getPointerCount() == 1) {
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
                break;

            case R.id.mentionTxtImgCap:
                mention_btn.startAnimation(an);
                BottomFrag f=new BottomFrag();

                Bundle b=new Bundle();
                b.putString("switch","search");
                b.putString("tagSelect","yes");
                //Setting Target Fragment for getting result
                f.setTargetFragment(ImageUpload.this, 1919);
                f.setArguments(b);
                f.show(getFragmentManager(),"fra");
                break;
            case R.id.back_of_captions:
                back.startAnimation(an);
                //getFragmentManager().popBackStack("cropper", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                int index = getActivity().getFragmentManager().getBackStackEntryCount() - 1;
                FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(index);
                getFragmentManager().popBackStack(backEntry.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;

        }
    }


    public static String getMimeType(String uri) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private  Emitter.Listener handleIncomingMessage = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            //
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    //JSONObject d=(JSONObject) args[1];
                    try {
                        String status=data.getString("status");
                        if(status.equals("true")){
                            Intent i=new Intent(v.getContext(),Home_Screen.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            i.putExtra("society_name",Hawk.get("society_name").toString());
                            startActivity(i);
                        }


                    } catch (JSONException e) {
                    }

                }
            });
        }
    };
    private int getDistanceFromEvent(MotionEvent event) {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) (Math.sqrt(dx * dx + dy * dy));
    }
    private void update(TextView mTextView) {
        final CharSequence text = mPagination.get(mCurrentIndex);
        if(text != null) mTextView.setText(text);
    }
    private int getDp(int px){
        float d = v.getContext().getResources().getDisplayMetrics().density;
        int dp= (int) (px * d);
        return dp;
    }
    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
       // pDialog.dismiss();
    }
}
