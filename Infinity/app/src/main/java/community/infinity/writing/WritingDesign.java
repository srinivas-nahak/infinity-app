package community.infinity.writing;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.madrapps.pikolo.HSLColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;
import com.orhanobut.hawk.Hawk;
import com.wang.avi.AVLoadingIndicatorView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import community.infinity.RecyclerViewItems.SpeedyLinearLayoutManager;
import community.infinity.network_related.FastImageUploader;
import community.infinity.network_related.SocketAddress;
import community.infinity.adapters.MyPreloadModelProvider;
import community.infinity.adapters.StarredAdapter;
import community.infinity.image_related.ImageUpload;
import community.infinity.image_related.ImageChooser;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.adapters.AccountSettingsAdapter;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.ToggleButton;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import ooo.oxo.library.widget.TouchImageView;

/**
 * Created by Srinu on 10-01-2018.
 */

public class WritingDesign extends Fragment {

    private Button color_btn,dismiss,img,left,right,middle,bg_store_btn,user_gallery;
    private ImageButton back,next;
    private AlertDialog dialog;
    private HSLColorPicker color_chooser;
    private TextView caption,dialog_txt,fontShow,penName,bookTitle,writerName;
    private RelativeLayout capContainer;
    private FrameLayout frameBox,titleBox,writerBox;
    private ImageView bG,userChosenImg;
    private  Bundle bundle=new Bundle();
    private ArrayList<Bitmap> bit=new ArrayList<>();
    private ArrayList<String> img_names=new ArrayList<>();
    private String my_username,upload_time;
    private AVLoadingIndicatorView aviLoader;
    private FrameLayout progressContainer;
    private TextView progressTxt;
    private List<MultipartBody.Part> parts = new ArrayList<>();
    private Animation an;
    private boolean move=true,moveWriter=true,moveTitle=true;
    private RecyclerView fontList;
    private ArrayList<String> bg_links=new ArrayList<>();
    private RecyclerView horzRview;
    private StarredAdapter adapter;
    private int colorInt,currentColor;
    private Bitmap selected_bitmap;
    View v;
    //Text Related
    private final int TEXT_MAX_SIZE = 75,TEXT_MIN_SIZE = 20;
    private float ORIGINAL_SIZE,ORIGINAL_SIZE_WRITER_NAME,ORIGINAL_SIZE_BOOK_TITLE;
    private static final float STEP =0.5f;
    private int mBaseDistZoomIn, mBaseDistZoomOut,mBaseDistZoomInWriter, mBaseDistZoomOutWriter,n;
    private long startTime,touchTime=0,startTimeTitle,touchTimeTitle=0,startTimeWriter,touchTimeWriter=0;
    //constant for defining the time duration between the click that can be considered as double-tap
    private static final int MAX_DURATION = 200;
     private float dX, dY,dXwriter,dYwriter;
    private String color;
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    public WritingDesign() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.disconnect();
        socket.connect();
        socket.on("writing_bgs",handleBgs);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (v == null) {
            v = inflater.inflate(R.layout.writing_edit, container, false);
        }
        else {}
        return v;
    }

    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {


            back = v.findViewById(R.id.backWritingEdit);
            next = v.findViewById(R.id.nextWritingEdit);
            bg_store_btn=v.findViewById(R.id.bg_store_btn);
            bG = v.findViewById(R.id.writingBg);
            caption = v.findViewById(R.id.designCap);
            color_btn = v.findViewById(R.id.color_Btn);
            left = v.findViewById(R.id.left);
            right = v.findViewById(R.id.right);
            middle = v.findViewById(R.id.middle);
            frameBox = v.findViewById(R.id.imgBox);
            fontShow=v.findViewById(R.id.fontShowList);
            capContainer=v.findViewById(R.id.captionContainer);
            penName=v.findViewById(R.id.penNameDesign);
            user_gallery=v.findViewById(R.id.userGallery);
            titleBox=v.findViewById(R.id.titleBook);
            writerBox=v.findViewById(R.id.writerBookDesignBox);
            bookTitle=v.findViewById(R.id.bookTitleDesign);
            writerName=v.findViewById(R.id.bookWriterDesign);
            userChosenImg=v.findViewById(R.id.userChosenImg);
            horzRview=v.findViewById(R.id.horizontalRViewWritingEdit);
            DragToClose dragToClose=v.findViewById(R.id.dragViewWritingDesign);
            aviLoader=v.findViewById(R.id.aviLoaderWriting);
            progressContainer=v.findViewById(R.id.loadingViewContainerWriting);
            progressTxt=v.findViewById(R.id.processingTxtWriting);
            final RelativeLayout parentLay=v.findViewById(R.id.writingDesignCont);
            ImageView inf_logo=v.findViewById(R.id.inf_logo);

            //Setting Bg
            parentLay.setBackgroundResource(RememberTextStyle.themeResource);
            
            

            //Building Hawk
            Hawk.init(v.getContext()).build();



            //Showing Instructions
        Snackbar snackbar = Snackbar.make(back, "Pinch or Double tap on text to change size", Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.parseColor("#43cea2"));
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.textColor));
        TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.parseColor("#001919"));
        snackbar.show();

            //Getting my Username
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());

            if(Hawk.get("myUserName")!=null) my_username = Hawk.get("myUserName");
            else my_username = sharedPref.getString("username", null);

        AlphaAnimation  blinkanimation= new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        blinkanimation.setDuration(800); // duration - half a second
        blinkanimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        blinkanimation.setRepeatCount(1); // Repeat animation infinitely
        blinkanimation.setRepeatMode(Animation.REVERSE);






             //Setting Tint
             ButtonTint black_tint=new ButtonTint("white");
             black_tint.setTint(back);
             black_tint.setTint(next);

             //Setting background of btns
             left.setBackgroundResource(R.drawable.align_left);
             middle.setBackgroundResource(R.drawable.align_center);
             right.setBackgroundResource(R.drawable.align_right);
             color_btn.setBackgroundResource(R.drawable.color_choose_icon);
             user_gallery.setBackgroundResource(R.drawable.gallery_new);
             bg_store_btn.setBackgroundResource(R.drawable.store);


        //Drag to Close view
        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {}

            @Override
            public void onViewCosed() {
                WritingLayout ic=new WritingLayout();
                FragmentManager fmt=getFragmentManager();
                FragmentTransaction ftt=fmt.beginTransaction();
                ftt.replace(R.id.profile_holder_frame,ic).commit();
                getFragmentManager().executePendingTransactions();
            }
        });



           currentColor=Color.parseColor("#001919");
            //Adding Links



        //Horizontal RView
        adapter=new StarredAdapter(bg_links,false);
        horzRview.setHasFixedSize(true);
        horzRview.setNestedScrollingEnabled(false);
        SpeedyLinearLayoutManager.MILLISECONDS_PER_INCH=60f;
        SpeedyLinearLayoutManager layoutManager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.HORIZONTAL,false);

        ((SimpleItemAnimator) horzRview.getItemAnimator()).setSupportsChangeAnimations(false);
        horzRview.setItemViewCacheSize(10);
        horzRview.setDrawingCacheEnabled(true);
        horzRview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        horzRview.setLayoutManager(layoutManager);
        horzRview.setNestedScrollingEnabled(false);
        horzRview.setAdapter(adapter);
        //Downloading Images in advance
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                ListPreloader.PreloadSizeProvider sizeProvider =
                        new FixedPreloadSizeProvider(600, 600);
                ListPreloader.PreloadModelProvider modelProvider = new MyPreloadModelProvider(bg_links,v.getContext());
                RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader =
                        new RecyclerViewPreloader<>(
                                Glide.with(v.getContext()), modelProvider, sizeProvider,15);
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        horzRview.addOnScrollListener(preloader);
                    }
                });

            }
        });

        horzRview.addOnItemTouchListener(new RecyclerItemClickListener(v.getContext(), horzRview, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                SharedPreferences.Editor editor=sh.edit();
                if(sh.contains("upload")) {
                    editor.remove("upload");
                    editor.apply();
                }

                if(Hawk.contains("upload"))Hawk.delete("upload");
                
                
                //Setting autoscroll on click
                int avg =
                        (layoutManager.findFirstCompletelyVisibleItemPosition()+(layoutManager.findFirstCompletelyVisibleItemPosition()+1)+
                        layoutManager.findLastCompletelyVisibleItemPosition())/3;


                if(position>avg) horzRview.getLayoutManager().smoothScrollToPosition(horzRview, null, position+1);
                else if(position!=0) horzRview.getLayoutManager().smoothScrollToPosition(horzRview, null, position-1);
                else horzRview.getLayoutManager().smoothScrollToPosition(horzRview, null, 0);



                Glide.with(v.getContext()).asBitmap().load(bg_links.get(position)).into(new SimpleTarget<Bitmap>(500, 500) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bG.setImageBitmap(null);
                        bG.setImageBitmap(resource);

                        inf_logo.setAnimation(blinkanimation);

                        Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {

                                int vibrantColor = palette.getLightVibrantColor(Color.parseColor("#ffffff"));
                                if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")){
                                    bookTitle.setTextColor(vibrantColor);
                                    writerName.setTextColor(vibrantColor);
                                }
                                else {
                                    caption.setTextColor(vibrantColor);
                                    penName.setTextColor(vibrantColor);
                                }

                            }
                        });
                    }
                });
            }

            @Override
            public void onLongItemClick(View view, int position) {
            }
        }));






            //Loading clicking animation
            an= AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons);



        //Text Related
            final ArrayList<String> name=new ArrayList<>();


        //Getting FontNames

        String files[] ;
        try {
            files = getActivity().getAssets().list("fonts");
            if (files != null) name.addAll(Arrays.asList(files));
        } catch (IOException e) {
            e.printStackTrace();
        }




            //////////Checking Book or Writing
        if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")){
            titleBox.setVisibility(View.VISIBLE);
            writerBox.setVisibility(View.VISIBLE);
            capContainer.setVisibility(View.GONE);
            bookTitle.setText(Hawk.get("titleOfBook").toString());

            if(Hawk.get("penName")!=null&&!Hawk.get("penName").equals(""))
            writerName.setText("By  "+Hawk.get("penName").toString());
            else writerName.setText("By  "+Hawk.get("myFullName").toString());
            left.setVisibility(View.GONE);
            right.setVisibility(View.GONE);
            middle.setVisibility(View.GONE);
            //alignment
            writerName.setGravity(Gravity.CENTER);
            bookTitle.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            color="both";
            ORIGINAL_SIZE_WRITER_NAME=writerName.getTextSize();
            ORIGINAL_SIZE_BOOK_TITLE=bookTitle.getTextSize();

            //Getting BookCovers
            JSONObject ob=new JSONObject();
            try {
                ob.put("getBookCovers","yes");
                socket.emit("data",ob);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else {
            //Getting and Setting the text
            caption.setText(Hawk.get("feelings").toString());
            if(Hawk.get("penName")!=null&&!Hawk.get("penName").equals(""))
            penName.setText("- "+Hawk.get("penName").toString());
            else penName.setText("- "+Hawk.get("myFullName").toString());
            caption.setGravity(Gravity.CENTER);
            caption.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            color="notBook";

            //Getting Bgs
            JSONObject ob=new JSONObject();
            try {
                ob.put("getWritingBgs","yes");
                socket.emit("data",ob);
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
        if (caption.getText().toString().length() >= 300) {
            caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10f);
            penName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10f);
        }
        ORIGINAL_SIZE = caption.getTextSize();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ///User Gallery
            user_gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                    SharedPreferences.Editor editor=sh.edit();
                    if(sh.contains("upload")) {
                        editor.remove("upload");
                        editor.apply();
                    }

                    if(Hawk.contains("upload"))Hawk.delete("upload");
                    
                    ImageChooser ic = new ImageChooser();
                    Bundle b=new Bundle();
                    b.putString("what","writing_design");
                    ic.setArguments(b);
                    FragmentManager fmt = getFragmentManager();
                    FragmentTransaction ftt = fmt.beginTransaction();
                    ftt.replace(R.id.profile_holder_frame, ic);
                    ftt.addToBackStack("designer").commit();
                    getFragmentManager().executePendingTransactions();
                }
            });

            bg_store_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bg_store_btn.startAnimation(an);

                    //Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(caption.getContext());
                    final View vi = LayoutInflater.from(caption.getContext()).inflate(R.layout.starred_showing, null);
                    RecyclerView rView = vi.findViewById(R.id.starred_rView);
                    ImageButton back_this=vi.findViewById(R.id.back_of_starred);
                    TextView heading=vi.findViewById(R.id.headingStarred);
                    SwipeRefreshLayout swipeRefreshLayout=vi.findViewById(R.id.swipe_containerStarred);
                    DragToClose dragToClose=vi.findViewById(R.id.dragViewStarred);

                    heading.setText("Store");

                    ButtonTint tint=new ButtonTint("white");
                    tint.setTint(back_this);

                    swipeRefreshLayout.setEnabled(false);




                    StarredAdapter adp=new StarredAdapter(bg_links,true);
                    GridLayoutManager mng = new GridLayoutManager(v.getContext(),3);
                    ((SimpleItemAnimator) rView.getItemAnimator()).setSupportsChangeAnimations(false);
                    mng.setItemPrefetchEnabled(true);
                    mng.setInitialPrefetchItemCount(20);
                    rView.setItemViewCacheSize(20);
                    rView.setDrawingCacheEnabled(true);
                    rView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    rView.setLayoutManager(mng);
                    rView.setNestedScrollingEnabled(false);
                    rView.setAdapter(adp);

                    //Drag to Close view
                    dragToClose.setDragListener(new DragListener() {
                        @Override
                        public void onStartDraggingView() {}

                        @Override
                        public void onViewCosed() {
                            dialog.dismiss();
                        }
                    });
                    back_this.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    rView.addOnItemTouchListener(new RecyclerItemClickListener(v.getContext(), rView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                            SharedPreferences.Editor editor=sh.edit();
                            if(sh.contains("upload")) {
                                editor.remove("upload");
                                editor.apply();
                            }

                            if(Hawk.contains("upload"))Hawk.delete("upload");
                            
                            final AlertDialog img_dialog;
                            android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(v.getContext());
                            final View dialog_view= LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_image,null);
                            builder.setView(dialog_view);
                            img_dialog=builder.create();
                            img_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            img_dialog.getWindow().setDimAmount(0.9f);
                            final TouchImageView img=dialog_view.findViewById(R.id.dialog_image);
                            final ImageButton done_btn=dialog_view.findViewById(R.id.doneBgShow);
                            final DragToClose dragToClose=dialog_view.findViewById(R.id.dragViewDialogImg);
                            Glide.with(v.getContext()).load(bg_links.get(position)).apply(new RequestOptions().
                                    override(600, 600).placeholder(new ColorDrawable(Color.parseColor("#20ffffff")))).into(img);

                            done_btn.setVisibility(View.VISIBLE);
                            ButtonTint tint=new ButtonTint("white");
                            tint.setTint(done_btn);


                            img_dialog.show();
                            img_dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
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
                                    img_dialog.dismiss();
                                }
                            });
                            done_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Glide.with(v.getContext()).asBitmap().load(bg_links.get(position)).into(new SimpleTarget<Bitmap>(500, 500) {

                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            bG.setImageBitmap(null);
                                            bG.setImageBitmap(resource);
                                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                                @Override
                                                public void onGenerated(Palette palette) {
                                                    Palette.Swatch swatch = palette.getDominantSwatch();
                                                    int vibrantColor = swatch.getRgb();

                                                    if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")){
                                                        bookTitle.setTextColor(vibrantColor);
                                                        writerName.setTextColor(vibrantColor);
                                                    }
                                                    else {
                                                        caption.setTextColor(vibrantColor);
                                                        penName.setTextColor(vibrantColor);
                                                    }

                                                }
                                            });
                                        }
                                    });
                                    img_dialog.dismiss();
                                    dialog.dismiss();
                                }
                            });
                            img_dialog.setOnDismissListener(
                                    new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            ViewGroup v=(ViewGroup)dialog_view.getParent();
                                            v.removeAllViews();
                                        }
                                    }
                            );
                            //Glide.with(v.getContext()).load(bg_links.get(position)).into(bG);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {
                        }
                    }));






                    //Dialog Properties////////
                    builder.setView(vi);
                    dialog = builder.create();
                    dialog.show();
                   // dialog.getWindow().setAttributes(lp);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);


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
                    // dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    /////////////////////////
                }
            });

            left.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {

                    SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                    SharedPreferences.Editor editor=sh.edit();
                    if(sh.contains("upload")) {
                        editor.remove("upload");
                        editor.apply();
                    }

                    if(Hawk.contains("upload"))Hawk.delete("upload");
                    
                    left.startAnimation(an);
                    caption.setGravity(Gravity.START);
                    caption.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    penName.setGravity(Gravity.START);
                    penName.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    //Changing BG color
                    ViewCompat.setBackgroundTintList(
                            left,
                            ColorStateList.valueOf(Color.parseColor("#001919")));
                    ViewCompat.setBackgroundTintList(
                            right,
                            ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    ViewCompat.setBackgroundTintList(
                            middle,
                            ColorStateList.valueOf(Color.parseColor("#ffffff")));

                }
            });

            right.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {

                    SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                    SharedPreferences.Editor editor=sh.edit();
                    if(sh.contains("upload")) {
                        editor.remove("upload");
                        editor.apply();
                    }

                    if(Hawk.contains("upload"))Hawk.delete("upload");
                    
                    right.startAnimation(an);
                    caption.setGravity(Gravity.END);
                    caption.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    penName.setGravity(Gravity.END);
                    penName.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    //Changing BG color
                    ViewCompat.setBackgroundTintList(
                            right,
                            ColorStateList.valueOf(Color.parseColor("#001919")));
                    ViewCompat.setBackgroundTintList(
                            left,
                            ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    ViewCompat.setBackgroundTintList(
                            middle,
                            ColorStateList.valueOf(Color.parseColor("#ffffff")));

                }
            });

            middle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                    SharedPreferences.Editor editor=sh.edit();
                    if(sh.contains("upload")) {
                        editor.remove("upload");
                        editor.apply();
                    }

                    if(Hawk.contains("upload"))Hawk.delete("upload");
                    
                    middle.startAnimation(an);
                    caption.setGravity(Gravity.CENTER);
                    caption.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    penName.setGravity(Gravity.CENTER);
                    penName.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                    //Chaning BG color
                    ViewCompat.setBackgroundTintList(
                            middle,
                            ColorStateList.valueOf(Color.parseColor("#001919")));
                    ViewCompat.setBackgroundTintList(
                            left,
                            ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    ViewCompat.setBackgroundTintList(
                            right,
                            ColorStateList.valueOf(Color.parseColor("#ffffff")));

                }
            });

            /////////////////////////Color
            color_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    color_btn.startAnimation(an);
                    //Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(caption.getContext());
                    final View vi = LayoutInflater.from(caption.getContext()).inflate(R.layout.society_message, null);
                    RelativeLayout rl = vi.findViewById(R.id.dialogMsgLay);
                    FrameLayout toggleCont=vi.findViewById(R.id.toggleContColor);
                    ToggleButton bgToggle=vi.findViewById(R.id.bgColorToggle);
                    ToggleButton textToggle=vi.findViewById(R.id.txtColorToggle);
                    dismiss = vi.findViewById(R.id.dismiss);
                    dialog_txt = vi.findViewById(R.id.textView2);
                    color_chooser = vi.findViewById(R.id.colorPicker);
                    img = vi.findViewById(R.id.demoImg);
                    dismiss.setVisibility(View.GONE);
                    dialog_txt.setVisibility(View.GONE);
                    img.setVisibility(View.VISIBLE);

                    img.setBackgroundResource(R.drawable.circle_tick);

                    //Setting Txt Toggle On
                    textToggle.setToggleOn();

                    toggleCont.setVisibility(View.VISIBLE);

                    color_chooser.setVisibility(View.VISIBLE);
                    ViewCompat.setBackgroundTintList(
                            img,
                            ColorStateList.valueOf(Color.parseColor("#ffffff")));
                    //On Clik
                    color_chooser.setColorSelectionListener(new SimpleColorSelectionListener() {
                        @Override
                        public void onColorSelected(final int color) {
                            // Do whatever you want with the color
                            ViewCompat.setBackgroundTintList(
                                    img,
                                    ColorStateList.valueOf(color));
                            colorInt=color;
                        }
                    });

                    textToggle.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
                        @Override
                        public void onToggle(boolean on) {
                            bgToggle.setToggleOff();
                        }
                    });
                    bgToggle.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
                        @Override
                        public void onToggle(boolean on) {
                            textToggle.setToggleOff();
                        }
                    });


                    img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            img.startAnimation(an);

                            if(textToggle.isToggleOn()){
                                SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                                SharedPreferences.Editor editor=sh.edit();
                                if(sh.contains("upload")) {
                                    editor.remove("upload");
                                    editor.apply();
                                }

                                if(Hawk.contains("upload"))Hawk.delete("upload");

                                if(color.equals("both")) {
                                    RememberTextStyle.shortBookTitleColor=colorInt;
                                    RememberTextStyle.shortBookWriterColor=colorInt;
                                    bookTitle.setTextColor(colorInt);
                                    writerName.setTextColor(colorInt);
                                }
                                else if(color.equals("writer")){
                                    RememberTextStyle.shortBookWriterColor=colorInt;
                                    writerName.setTextColor(colorInt);
                                }
                                else if(color.equals("title")){
                                    RememberTextStyle.shortBookTitleColor=colorInt;
                                    bookTitle.setTextColor(colorInt);
                                }
                                else {
                                    RememberTextStyle.writingDesignColor=colorInt;
                                    caption.setTextColor(colorInt);
                                    penName.setTextColor(colorInt);
                                }
                            }
                            else{
                                bG.setImageBitmap(null);
                                bG.setBackgroundColor(colorInt);
                                inf_logo.setAnimation(blinkanimation);
                            }


                            dialog.dismiss();
                        }
                    });
                    //Dialog Properties////////
                    builder.setView(vi);
                    dialog = builder.create();
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    lp.gravity = Gravity.CENTER;

                    dialog.getWindow().setAttributes(lp);
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
                    // dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    rl.setBackground(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    /////////////////////////
                }
            });
//////////////////////////////////Font Show
            fontShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(caption.getContext());
                    final View vi = LayoutInflater.from(caption.getContext()).inflate(R.layout.dialog_image, null);
                    fontList=vi.findViewById(R.id.fontRecycler);
                    TouchImageView img=vi.findViewById(R.id.dialog_image);
                    img.setVisibility(View.GONE);
                    fontList.setVisibility(View.VISIBLE);

                    AccountSettingsAdapter adapter=new AccountSettingsAdapter(name,null,null,"font_list",null);
                    fontList.setHasFixedSize(true);
                    fontList.setLayoutManager(new LinearLayoutManager(caption.getContext()));
                    fontList.setItemViewCacheSize(30);
                    fontList.setDrawingCacheEnabled(true);
                    fontList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    fontList.setAdapter(adapter);
                    fontList.addOnItemTouchListener(
                            new RecyclerItemClickListener(caption.getContext(), fontList ,new RecyclerItemClickListener.OnItemClickListener() {
                                @Override public void onItemClick(View view, int position) {

                                    SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                                    SharedPreferences.Editor editor=sh.edit();
                                    if(sh.contains("upload")) {
                                        editor.remove("upload");
                                        editor.apply();
                                    }

                                    if(Hawk.contains("upload"))Hawk.delete("upload");
                                    
                                    String item = name.get(position).substring(0,name.get(position).indexOf("."));
                                    Typeface font = Typeface.createFromAsset(getActivity().getAssets(),"fonts/"+ name.get(position));
                                    fontShow.setText(item);
                                    fontShow.setTypeface(font);
                                        if(color.equals("both")) {
                                            RememberTextStyle.shortBookTitle=font;
                                            RememberTextStyle.shortBookWriter=font;
                                            bookTitle.setTypeface(font);
                                            writerName.setTypeface(font);
                                        }
                                        else if(color.equals("writer")){
                                            RememberTextStyle.shortBookWriter=font;
                                            writerName.setTypeface(font);
                                        }
                                        else if(color.equals("title")){
                                            RememberTextStyle.shortBookTitle=font;
                                            bookTitle.setTypeface(font);
                                        }

                                    else {
                                            RememberTextStyle.writingdesign=font;
                                            caption.setTypeface(font);
                                        penName.setTypeface(font);
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
                    dialog.getWindow().setDimAmount(0.9f);
                    // dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    /////////////////////////
                }
            });
            //Pinch to zoom of Text
           frameBox.setOnTouchListener(new View.OnTouchListener() {
               @Override
               public boolean onTouch(View view, MotionEvent event) {
                   capContainer.dispatchTouchEvent(event);
                   return true;
               }
           });

            /////////////////////
            capContainer.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            
                            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                            SharedPreferences.Editor editor=sh.edit();
                            if(sh.contains("upload")) {
                                editor.remove("upload");
                                editor.apply();
                            }

                            if(Hawk.contains("upload"))Hawk.delete("upload");


                            currentColor=caption.getCurrentTextColor();

                            //Layout Params
                            final RelativeLayout.LayoutParams contParam=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                            contParam.addRule(RelativeLayout.CENTER_HORIZONTAL);

                            if (touchTime==0||System.currentTimeMillis() - touchTime >= 100) {
                                if(event.getPointerCount() == 1){
                                    if (event.getAction() == MotionEvent.ACTION_MOVE) {

                                        v.animate()
                                                .x(event.getRawX() + dX)
                                                .y(event.getRawY() + dY)
                                                .setDuration(0)
                                                .start();


                                    }
                                    if (event.getAction() == MotionEvent.ACTION_UP) {
                                        startTime = System.currentTimeMillis();

                                    }
                                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                        if (System.currentTimeMillis() - startTime <= MAX_DURATION) {
                                            if (!move && caption.getTextSize() > ORIGINAL_SIZE) {
                                                caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, ORIGINAL_SIZE);
                                                RememberTextStyle.writingDesignTxtSize=caption.getTextSize();
                                                move = true;
                                            }
                                            if (caption.getTextSize() < 60f) {
                                                ValueAnimator animator = ValueAnimator.ofFloat(caption.getTextSize(), caption.getTextSize()+15f);
                                                animator.setDuration(200);

                                                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                    @Override
                                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                        float animatedValue = (float) valueAnimator.getAnimatedValue();
                                                        caption.setTextSize(TypedValue.COMPLEX_UNIT_PX,animatedValue);
                                                        penName.setTextSize(TypedValue.COMPLEX_UNIT_PX,animatedValue);
                                                        RememberTextStyle.writingDesignTxtSize=caption.getTextSize();

                                                    }
                                                });

                                                animator.start();
                                                caption.setLayoutParams(contParam);
                                            } else {

                                                ValueAnimator animator = ValueAnimator.ofFloat(caption.getTextSize(), ORIGINAL_SIZE);
                                                animator.setDuration(200);

                                                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                    @Override
                                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                        float animatedValue = (float) valueAnimator.getAnimatedValue();
                                                        caption.setTextSize(TypedValue.COMPLEX_UNIT_PX,animatedValue);
                                                        penName.setTextSize(TypedValue.COMPLEX_UNIT_PX,animatedValue);
                                                        RememberTextStyle.writingDesignTxtSize=caption.getTextSize();

                                                    }
                                                });

                                                animator.start();
                                                caption.setLayoutParams(contParam);
                                            }
                                        }

                                        dX = v.getX() - event.getRawX();
                                        dY = v.getY() - event.getRawY();



                                        //capContainer.setLayoutParams(params);
                                        caption.setLayoutParams(contParam);
                                    }
                                    return true;
                                }
                            }

                            if (event.getPointerCount() == 2) {
                                int action = event.getAction();
                                int pure = action & MotionEvent.ACTION_MASK;

                                if (pure == MotionEvent.ACTION_POINTER_DOWN
                                        && caption.getTextSize() <= TEXT_MAX_SIZE
                                        && caption.getTextSize() >= TEXT_MIN_SIZE) {

                                    mBaseDistZoomIn = getDistanceFromEvent(event);
                                    mBaseDistZoomOut = getDistanceFromEvent(event);

                                } else {
                                    int currentDistance = getDistanceFromEvent(event);
                                    if (currentDistance > mBaseDistZoomIn) {
                                        float finalSize = caption.getTextSize() + STEP;
                                        if (finalSize > TEXT_MAX_SIZE) {
                                            finalSize = TEXT_MAX_SIZE;
                                        }
                                        touchTime=System.currentTimeMillis();
                                        caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize);
                                        penName.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize);
                                        RememberTextStyle.writingDesignTxtSize=caption.getTextSize();
                                        // capContainer.setLayoutParams(params);
                                        caption.setLayoutParams(contParam);
                                        //penName.setLayoutParams(contParam);
                                    } else {
                                        if (currentDistance < mBaseDistZoomOut) {
                                            float finalSize = caption.getTextSize() - STEP;
                                            if (finalSize < TEXT_MIN_SIZE) {
                                                finalSize = TEXT_MIN_SIZE;
                                            }
                                            touchTime=System.currentTimeMillis();
                                            caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize);
                                            penName.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize);
                                            RememberTextStyle.writingDesignTxtSize=caption.getTextSize();
                                            // capContainer.setLayoutParams(params);
                                            caption.setLayoutParams(contParam);
                                            //  penName.setLayoutParams(contParam);
                                        }
                                    }
                                }
                                return true;
                            }

                            return false;
                        }
                    });
        titleBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                bookTitle.dispatchTouchEvent(motionEvent);
                return true;
            }
        });
       bookTitle.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                        SharedPreferences.Editor editor=sh.edit();
                        if(sh.contains("upload")) {
                            editor.remove("upload");
                            editor.apply();
                        }

                        if(Hawk.contains("upload"))Hawk.delete("upload");
                        
                        color="title";
                        currentColor=bookTitle.getCurrentTextColor();
                        //Layout Params
                        Resources r=getResources();
                        float dp =TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,r.getDisplayMetrics());
                        final FrameLayout.LayoutParams contParam=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,(int)dp);
                        contParam.gravity=Gravity.CENTER;

                        if (touchTimeTitle==0||System.currentTimeMillis() - touchTimeTitle >= 100) {
                            if(event.getPointerCount() == 1){
                                if (event.getAction() == MotionEvent.ACTION_MOVE) {


                                        v.animate()
                                                .x(event.getRawX() + dX)
                                                .y(event.getRawY() + dY)
                                                .setDuration(0)
                                                .start();



                                }
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                    startTimeTitle = System.currentTimeMillis();


                                }
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    if (System.currentTimeMillis() - startTimeTitle <= MAX_DURATION) {
                                        if (!moveTitle && bookTitle.getTextSize() > ORIGINAL_SIZE) {
                                            bookTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, ORIGINAL_SIZE_BOOK_TITLE);
                                            moveTitle = true;
                                            RememberTextStyle.shortBookTitleSize=bookTitle.getTextSize();
                                        }
                                        if (bookTitle.getTextSize() < 60f) {
                                            ValueAnimator animator = ValueAnimator.ofFloat(bookTitle.getTextSize(),bookTitle.getTextSize() + 15f);
                                            animator.setDuration(200);

                                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                                                    bookTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
                                                    RememberTextStyle.shortBookTitleSize=bookTitle.getTextSize();
                                                }
                                            });

                                            animator.start();
                                        } else {
                                            ValueAnimator animator = ValueAnimator.ofFloat(bookTitle.getTextSize(),ORIGINAL_SIZE_BOOK_TITLE);
                                            animator.setDuration(200);

                                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                                                    bookTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
                                                    RememberTextStyle.shortBookTitleSize=bookTitle.getTextSize();
                                                }
                                            });

                                            animator.start();
                                        }
                                    }

                                    dX = v.getX() - event.getRawX();
                                    dY = v.getY() - event.getRawY();

                                }
                                return true;
                            }
                        }


                        if (event.getPointerCount() == 2) {
                            int action = event.getAction();
                            int pure = action & MotionEvent.ACTION_MASK;

                            if (pure == MotionEvent.ACTION_POINTER_DOWN
                                    && bookTitle.getTextSize() <= TEXT_MAX_SIZE
                                    && bookTitle.getTextSize() >= TEXT_MIN_SIZE) {

                                mBaseDistZoomIn = getDistanceFromEvent(event);
                                mBaseDistZoomOut = getDistanceFromEvent(event);

                            } else {
                                int currentDistance = getDistanceFromEvent(event);
                                if (currentDistance > mBaseDistZoomIn) {
                                    float finalSize = bookTitle.getTextSize() + STEP;
                                    if (finalSize > TEXT_MAX_SIZE) {
                                        finalSize = TEXT_MAX_SIZE;
                                    }
                                    touchTimeTitle=System.currentTimeMillis();
                                    bookTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize);
                                    RememberTextStyle.shortBookTitleSize=bookTitle.getTextSize();
                                    //penName.setLayoutParams(contParam);
                                } else {
                                    if (currentDistance < mBaseDistZoomOut) {
                                        float finalSize = bookTitle.getTextSize() - STEP;
                                        if (finalSize < TEXT_MIN_SIZE) {
                                            finalSize = TEXT_MIN_SIZE;
                                        }
                                        touchTimeTitle=System.currentTimeMillis();
                                        bookTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize);
                                        RememberTextStyle.shortBookTitleSize=bookTitle.getTextSize();
                                    }
                                }
                            }
                            return true;
                        }

                        return false;
                    }
                });
        writerBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                writerName.dispatchTouchEvent(motionEvent);
                return true;
            }
        });
        writerName.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                        SharedPreferences.Editor editor=sh.edit();
                        if(sh.contains("upload")) {
                            editor.remove("upload");
                            editor.apply();
                        }

                        if(Hawk.contains("upload"))Hawk.delete("upload");
                        
                        
                        color="writer";
                        currentColor=writerName.getCurrentTextColor();
                        //Layout Params
                        Resources r=getResources();
                        float dp =TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,70,r.getDisplayMetrics());
                        final FrameLayout.LayoutParams contParam=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,(int)dp);
                        contParam.gravity=Gravity.CENTER;

                        if (touchTimeWriter==0||System.currentTimeMillis() - touchTimeWriter >= 100) {
                            if(event.getPointerCount() == 1){
                                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                                    //Getting Distance
                                    // if(move){
                                    v.animate()
                                            .x(event.getRawX() + dXwriter)
                                            .y(event.getRawY() + dYwriter)
                                            .setDuration(0)
                                            .start();


                                }
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                    startTimeWriter = System.currentTimeMillis();

                                }
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    if (System.currentTimeMillis() - startTimeWriter <= MAX_DURATION) {
                                        if (!moveWriter && writerName.getTextSize() > ORIGINAL_SIZE_WRITER_NAME) {
                                            writerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, ORIGINAL_SIZE_WRITER_NAME);
                                            RememberTextStyle.shortBookWriterSize=writerName.getTextSize();
                                            moveWriter = true;
                                        }
                                        if (writerName.getTextSize() < 60f) {
                                            ValueAnimator animator = ValueAnimator.ofFloat(writerName.getTextSize(),writerName.getTextSize() + 10f);
                                            animator.setDuration(200);

                                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                                                    writerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
                                                    RememberTextStyle.shortBookWriterSize=writerName.getTextSize();
                                                }
                                            });

                                            animator.start();
                                            //writerName.setLayoutParams(contParam);

                                        } else {
                                            ValueAnimator animator = ValueAnimator.ofFloat(writerName.getTextSize(),ORIGINAL_SIZE_WRITER_NAME);
                                            animator.setDuration(200);

                                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                                                    writerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
                                                    RememberTextStyle.shortBookWriterSize=writerName.getTextSize();
                                                }
                                            });

                                            animator.start();
                                        }
                                    }
                                    dXwriter = v.getX() - event.getRawX();
                                    dYwriter = v.getY() - event.getRawY();

                                }
                                return true;
                            }
                        }


                        if (event.getPointerCount() == 2) {
                            int action = event.getAction();
                            int pure = action & MotionEvent.ACTION_MASK;

                            if (pure == MotionEvent.ACTION_POINTER_DOWN
                                    && writerName.getTextSize() <= TEXT_MAX_SIZE
                                    && writerName.getTextSize() >= TEXT_MIN_SIZE) {

                                mBaseDistZoomInWriter = getDistanceFromEvent(event);
                                mBaseDistZoomOutWriter = getDistanceFromEvent(event);

                            } else {
                                int currentDistance = getDistanceFromEvent(event);
                                if (currentDistance > mBaseDistZoomInWriter) {
                                    float finalSize = writerName.getTextSize() + STEP;
                                    if (finalSize > TEXT_MAX_SIZE) {
                                        finalSize = TEXT_MAX_SIZE;
                                    }
                                    touchTimeWriter=System.currentTimeMillis();
                                    writerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize);
                                    RememberTextStyle.shortBookWriterSize=writerName.getTextSize();

                                } else {
                                    if (currentDistance < mBaseDistZoomOutWriter) {
                                        float finalSize = writerName.getTextSize() - STEP;
                                        if (finalSize < TEXT_MIN_SIZE) {
                                            finalSize = TEXT_MIN_SIZE;
                                        }
                                        touchTimeWriter=System.currentTimeMillis();
                                        writerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalSize);
                                        RememberTextStyle.shortBookWriterSize=writerName.getTextSize();
                                        // capContainer.setLayoutParams(params);
                                        //  penName.setLayoutParams(contParam);
                                    }
                                }
                            }
                            return true;
                        }

                        return false;
                    }
                });
            v.setFocusableInTouchMode(true);
            v.requestFocus();
            v.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                        WritingLayout ic=new WritingLayout();
                        FragmentManager fmt=getFragmentManager();
                        FragmentTransaction ftt=fmt.beginTransaction();
                        ftt.replace(R.id.profile_holder_frame,ic).commit();
                        getFragmentManager().executePendingTransactions();
                        return true;
                    }
                    return false;
                }
            });
        back.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.performClick();
                }
            }
        });
        next.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.performClick();
                }
            }
        });
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                    SharedPreferences.Editor editor=sh.edit();
                    editor.remove("upload");
                    editor.apply();

                    Hawk.delete("upload");
                    
                    WritingLayout ic=new WritingLayout();
                    FragmentManager fmt=getFragmentManager();
                    FragmentTransaction ftt=fmt.beginTransaction();
                    ftt.replace(R.id.profile_holder_frame,ic).commit();
                    getFragmentManager().executePendingTransactions();

                }
            });
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Bitmap b = Bitmap.createBitmap( frameBox.getWidth(), frameBox.getHeight(), Bitmap.Config.RGB_565);
                    Canvas c = new Canvas(b);
                    frameBox.layout(frameBox.getLeft(), frameBox.getTop(), frameBox.getRight(), frameBox.getBottom());
                    frameBox.draw(c);

                            bit.add(b);
                            
                            //bundle.putParcelableArrayList("cropped",bit);
                            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());




                    if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")){


                                //SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                                if(!sh.contains("upload")|| !Hawk.contains("upload")) {
                                    RememberTextStyle.bitmap = b;
                                    aviLoader.smoothToShow();
                                    progressContainer.setVisibility(View.VISIBLE);
                                    progressTxt.setText("Processing ...");
                                   // bit.add(b);

                                    Random generator = new Random();
                                    int n = 10000;
                                    n = generator.nextInt(n);
                                    String fname = "Image-" + n + ".jpg";
                                    try {
                                        saveImageFast(fname, bit.get(0));
                                        img_names.add(fname);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                            ///Fragment
                                            ShortBook ic = new ShortBook();
                                            FragmentManager fmt = getFragmentManager();
                                            FragmentTransaction ftt = fmt.beginTransaction();
                                            ftt.replace(R.id.profile_holder_frame, ic);
                                            ftt.addToBackStack("designer").commit();
                                            getFragmentManager().executePendingTransactions();
                                }
                               

                    }
                    else{
                        if(!sh.contains("upload")|| !Hawk.contains("upload")) {
                            aviLoader.smoothToShow();
                            progressContainer.setVisibility(View.VISIBLE);
                            progressTxt.setText("Processing ...");

                            Random generator = new Random();
                            int n = 10000;
                            n = generator.nextInt(n);
                            String fname = "Image-" + n + ".jpg";
                            try {
                                saveImageFast(fname, bit.get(0));
                                img_names.add(fname);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            ///Fragment
                            ImageUpload ic = new ImageUpload();
                            ////Bundle
                            bundle.putParcelableArrayList("cropped",bit);
                            bundle.putStringArrayList("img_names",img_names);
                            bundle.putString("upload_time",upload_time);
                            if(!bundle.isEmpty())
                                ic.setArguments(bundle);
                            /////
                            FragmentManager fmt = getFragmentManager();
                            FragmentTransaction ftt = fmt.beginTransaction();
                            ftt.replace(R.id.profile_holder_frame, ic);
                            ftt.addToBackStack("designer").commit();
                            getFragmentManager().executePendingTransactions();
                        }
                    }
                }
            });
            userChosenImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selected_bitmap!=null) {
                        bG.setImageBitmap(null);
                        bG.setImageBitmap(selected_bitmap);
                    }

                }
            });

    }

    @Override
    public void onStart() {
        super.onStart();
        ///////////////////////Img Related

        if(getArguments()!=null) {
            if (getArguments().getString("fragName").equals("cropper")) {
                selected_bitmap = getArguments().getParcelable("bitmapDesign");
                bG.setImageBitmap(null);
                userChosenImg.setImageBitmap(null);
                userChosenImg.setImageBitmap(selected_bitmap);
                bG.setImageBitmap(selected_bitmap);

                ///////Setting Color
                Palette.from(selected_bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {

                        //Set normal shade to textview
                        int vibrantColor = palette.getVibrantColor(Color.parseColor("#001919"));
                        if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")){
                            bookTitle.setTextColor(vibrantColor);
                            writerName.setTextColor(vibrantColor);
                        }
                        else {
                            caption.setTextColor(vibrantColor);
                            penName.setTextColor(vibrantColor);
                        }
                    }
                });
            }
        }
        if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")){
            if(RememberTextStyle.shortBookTitle!=null) bookTitle.setTypeface(RememberTextStyle.shortBookTitle);
            if(RememberTextStyle.shortBookTitleSize!=0) bookTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,RememberTextStyle.shortBookTitleSize);

            if(RememberTextStyle.shortBookWriter!=null) writerName.setTypeface(RememberTextStyle.shortBookWriter);
            if(RememberTextStyle.shortBookWriterSize!=0) writerName.setTextSize(TypedValue.COMPLEX_UNIT_PX,RememberTextStyle.shortBookWriterSize);

        }
        else{

            if(RememberTextStyle.writingDesignTxtSize!=0){
                caption.setTextSize(TypedValue.COMPLEX_UNIT_PX,RememberTextStyle.writingDesignTxtSize);
                penName.setTextSize(TypedValue.COMPLEX_UNIT_PX,RememberTextStyle.writingDesignTxtSize);
            }
            if(RememberTextStyle.writingdesign!=null){
                caption.setTypeface(RememberTextStyle.writingdesign);
                penName.setTypeface(RememberTextStyle.writingdesign);
            }

        }
    }

    private void setBg(){
        adapter.notifyDataSetChanged();
    }
    void saveImageFast(String imgName, Bitmap bm) throws IOException {
        //Create Path to save Image
        File file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Infinity"); //Creates app specific folder
        file_path.mkdirs();
        File imageFile = new File(file_path, imgName ); // Imagename.png
        FileOutputStream out = new FileOutputStream(imageFile);
        try {
            bm.compress(Bitmap.CompressFormat.JPEG, 95, out); // Compress Image
            out.flush();
            out.close();



            MediaScannerConnection.scanFile(v.getContext(), new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String pathi, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + file_path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                    parts.add(prepareFilePart("photo", pathi));
                    if(parts.size()==bit.size()){
                        SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(v.getContext());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                        upload_time = simpleDateFormat.format(new Date());
                        if(!sh.contains("upload")|| !Hawk.contains("upload")) {



                            //Magic
                            FastImageUploader uploader=new FastImageUploader(getActivity(), parts, my_username,upload_time);


                            SharedPreferences.Editor editor=sh.edit();
                            editor.putString("upload","yes");
                            editor.apply();

                            Needle.onBackgroundThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Hawk.put("upload","yes");
                                }
                            });
                        }

                        //Doing the Magic
                        Needle.onMainThread().execute(new Runnable() {
                            @Override
                            public void run() {



                                //pDialog.dismiss();
                                int time;
                                time=1000;
                                bundle.putStringArrayList("img_names",img_names);
                                bundle.putString("upload_time",upload_time);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressContainer.setVisibility(View.GONE);

                                        SharedPreferences.Editor editor=sh.edit();
                                        editor.putString("upload_time",upload_time);
                                        editor.apply();

                                               // bit.clear();
                                               // bit.add(bm);


                                        if(Hawk.contains("bookTitle")&&Hawk.get("bookTitle").toString().equals("yes")){
                                            ShortBook shortBook=new ShortBook();
                                            shortBook.setArguments(bundle);
                                            FragmentManager fmt = getFragmentManager();
                                            FragmentTransaction ftt = fmt.beginTransaction();
                                            ftt.replace(R.id.profile_holder_frame, shortBook);
                                            ftt.addToBackStack("designer").commit();
                                            getFragmentManager().executePendingTransactions();
                                        }
                                        else {
                                            bundle.putParcelableArrayList("cropped", bit);
                                            ///Fragment
                                            ImageUpload ic = new ImageUpload();
                                            ic.setArguments(bundle);
                                            FragmentManager fmt = getFragmentManager();
                                            FragmentTransaction ftt = fmt.beginTransaction();
                                            ftt.replace(R.id.profile_holder_frame, ic);
                                            ftt.addToBackStack("designer").commit();
                                            getFragmentManager().executePendingTransactions();
                                        }
                                    }
                                },time);

                            }
                        });

                    }
                }
            });
        } catch (Exception e) {
            throw new IOException();
        }
    }
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, String path) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = new File(path);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(ImageUpload.getMimeType(path)),
                        file
                );
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
    private int getDistanceFromEvent(MotionEvent event) {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) (Math.sqrt(dx * dx + dy * dy));
    }
    Emitter.Listener handleBgs=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONArray arr=(JSONArray)args[0];
            Needle.onMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    if(arr.length()>0){
                        bg_links.clear();
                        for(int i=0;i<arr.length();i++){
                            try {
                                bg_links.add(arr.getString(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        Collections.shuffle(bg_links);
                        setBg();
                    }
                }
            });
        }
    };
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}