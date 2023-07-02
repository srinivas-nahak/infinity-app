package community.infinity.image_related;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adamstyrc.cookiecutter.CookieCutterImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fenchtose.nocropper.CropperView;
import com.novoda.merlin.MerlinsBeard;
import com.orhanobut.hawk.Hawk;
import com.wang.avi.AVLoadingIndicatorView;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


import community.infinity.BottomFrags.BottomFrag;
import community.infinity.network_related.FastImageUploader;
import community.infinity.network_related.FileUploadService;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.network_related.ServiceGenerator;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.ProfileHolder;
import community.infinity.adapters.AccountSettingsAdapter;
import community.infinity.adapters.StarredAdapter;
import community.infinity.writing.RememberTextStyle;
import community.infinity.writing.WritingDesign;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import io.socket.client.IO;
import io.socket.client.Socket;
import multipleimageselect.activities.AlbumSelectActivity;
import multipleimageselect.helpers.Constants;
import multipleimageselect.models.Image;
import needle.Needle;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import ooo.oxo.library.widget.TouchImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
/**
 * Created by Srinu on 03-11-2017.
 */

public class ImageChooser extends Fragment implements View.OnClickListener {

    private CropperView cropView;
    private Bitmap bitmap;
    boolean snap=true,set=false;
    public static String time_format;//ImgMsgSending TimeFormat
    ArrayList<Bitmap> bit=new ArrayList<>();
    ArrayList<String> path=new ArrayList<>();
    private RecyclerView horzRview;
    private final int maxSize = 700;
    private ArrayList<String> img_names=new ArrayList<>();
    private StarredAdapter adapter;
    private AVLoadingIndicatorView aviLoader;
    private FrameLayout progressContainer;
    private TextView progressTxt,heading;
    private int pos=0,tos=0;
    private String my_username,upload_time;
    private Animation an,ani;
    private ImageView imageView;
    private AlertDialog dialog;
    private AppCompatImageButton rotate,snap_btn,crop,choose,mirror,back,next;
    private CookieCutterImageView circular_cutter;
    List<MultipartBody.Part> parts = new ArrayList<>();
    private ProgressDialog pDialog;
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    //private ArrayList<Bitmap> rit=bind.getParcelableArrayList("selected");
    View v;
    public ImageChooser() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(v==null){
        v=inflater.inflate(R.layout.image_chooser,container,false);
        heading=v.findViewById(R.id.headTxtImgChooser);
        cropView=v.findViewById(R.id.cropper_view);
        circular_cutter=v.findViewById(R.id.circularCropper);
        rotate=v.findViewById(R.id.rotateChooser);
        snap_btn=v.findViewById(R.id.snap);
        crop=v.findViewById(R.id.cropChooser);
        choose=v.findViewById(R.id.choose);
        mirror=v.findViewById(R.id.mirrorChooser);
        back=v.findViewById(R.id.backChooser);
        next=v.findViewById(R.id.nextChooser);
        horzRview=v.findViewById(R.id.horizontalRViewImgChooser);
        aviLoader=v.findViewById(R.id.aviLoaderImgChooser);
        progressContainer=v.findViewById(R.id.loadingViewContainerImgChooser);
        progressTxt=v.findViewById(R.id.processingTxtImgChooser);
        final RelativeLayout parentLay=v.findViewById(R.id.imgChooserCont);
        DragToClose dragToClose=v.findViewById(R.id.dragViewImgChooser);

        //Setting bg
            parentLay.setBackgroundResource(RememberTextStyle.themeResource);
        


           //Building Hawk
            Hawk.init(v.getContext()).build();

            final Typeface bold_font = Typeface.createFromAsset((v.getContext()).getAssets(), "fonts/"+"Raleway-SemiBold.ttf");
            final Typeface reg_font = Typeface.createFromAsset((v.getContext()).getAssets(), "fonts/"+"Raleway-Light.ttf");
            heading.setTypeface(reg_font);


            //Deleting Previous Things
            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
            SharedPreferences.Editor editor=sh.edit();
            editor.remove("upload");
            editor.apply();

            Hawk.delete("upload");



            //aviLoader.smoothToShow();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    aviLoader.setVisibility(View.GONE);
                }
            },20000);

            //Setting Tint
            ButtonTint white_tint=new ButtonTint("white");
            //ButtonTint black_tint=new ButtonTint("black");
            white_tint.setTint(back);
            white_tint.setTint(next);
            white_tint.setTint(choose);
            white_tint.setTint(rotate);
            white_tint.setTint(crop);
            white_tint.setTint(mirror);

            //Getting my Username
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());

            if(Hawk.get("myUserName")!=null) my_username = Hawk.get("myUserName");
            else my_username = sharedPref.getString("username", null);


            //Drag to Close view
            dragToClose.setDragListener(new DragListener() {
                @Override
                public void onStartDraggingView() {}

                @Override
                public void onViewCosed() {
                    if(getArguments().getString("what").equals("writing_design")) {
                        getFragmentManager().popBackStack("designer", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    }

                    else {
                            getActivity().finish();
                        }
                }
            });
           //Changing Next Buttons icon if opened from profile
            if(getArguments().getString("what").equals("profile_pic")||
                    getArguments().getString("what").equals("wall_pic")||getArguments().getString("what").equals("msg_pic")) {
                next.setImageResource(R.drawable.tick_mark);
                next.setScaleX(1f);//as it was rotated horizontally for nextbtn
            }
            if(getArguments().containsKey("imageGallery")){
                path=getArguments().getStringArrayList("imageGallery");
                set=false;
                setImage(path);

                aviLoader.smoothToShow();
                progressContainer.setVisibility(View.VISIBLE);
                progressTxt.setText("Please wait for a while...");

            }

            //Showing Instructions
            Snackbar snackbar = Snackbar.make(back, "Tip : Crop and upload the images", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.parseColor("#43cea2"));
            View sbView = snackbar.getView();
            sbView.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.textColor));
            TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.parseColor("#001919"));
            snackbar.show();

         //Horizontal RView
            adapter=new StarredAdapter(bit);
            horzRview.setHasFixedSize(true);
            horzRview.setNestedScrollingEnabled(false);
            horzRview.setLayoutManager(new LinearLayoutManager(v.getContext(),LinearLayoutManager.HORIZONTAL,false));
            horzRview.setAdapter(adapter);

            horzRview.addOnItemTouchListener(new RecyclerItemClickListener(v.getContext(), horzRview, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Glide.with(v.getContext()).asBitmap().load(bit.get(position)).into(new SimpleTarget<Bitmap>(500, 500) {

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (getArguments().getString("what").equals("profile_pic")) {

                                circular_cutter.setImageBitmap(null);
                                circular_cutter.setImageBitmap(resource);

                            } else {
                                snap = true;
                                cropView.setImageBitmap(null);
                                cropView.setImageBitmap(resource);
                                cropView.cropToCenter();
                            }
                            bitmap = resource;
                        }
                    });
                    pos=position;
                }

                @Override
                public void onLongItemClick(View view, int position) {
                  tos=position;

                    //longClickMsg(holder.wholeCont.getContext(),message.getTime(),holder);
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    final View vi = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_image, null);
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
                    name.add("Remove");

                    AccountSettingsAdapter adapter = new AccountSettingsAdapter(name, null, null,  "triple_dot", null);
                    fontList.setHasFixedSize(true);
                    fontList.setLayoutManager(new LinearLayoutManager(v.getContext()));
                    fontList.setAdapter(adapter);

                    //Onclick Listener
                    fontList.addOnItemTouchListener(new RecyclerItemClickListener(v.getContext(), fontList, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            if(name.get(position).equals("Remove")){
                                bit.remove(tos);
                                setThis();
                                if(bit.size()==0){
                                    circular_cutter.setImageBitmap(null);
                                    cropView.setImageBitmap(null);
                                    bitmap=null;
                                    pos=4;tos=5;//just setting unequal numbers
                                }
                                if(tos==pos-1){
                                    pos--;
                                }
                                if(pos==tos){
                                    if(pos==bit.size()){
                                        cropView.setImageBitmap(bit.get(tos-1));
                                    }
                                    else{
                                        cropView.setImageBitmap(bit.get(tos));
                                    }
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
                    dialog.getWindow().setDimAmount(0.3f);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));



                }
            }));


        //CropView
        cropView.fitToCenter();
        cropView.setGestureEnabled(true);
        cropView.initWithFitToCenter(true);
        cropView.cropToCenter();

        if(path.size()==0){
            Intent intent = new Intent(v.getContext(), AlbumSelectActivity.class);
            if(getArguments().getString("what").equals("timeline"))
                intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10-bit.size());
            else
                intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1-bit.size());
            startActivityForResult(intent, Constants.REQUEST_CODE);
        }
            if(getArguments().getString("what").equals("profile_pic")){
             cropView.setVisibility(View.INVISIBLE);
             circular_cutter.setVisibility(View.VISIBLE);
             snap_btn.setVisibility(View.INVISIBLE);
             crop.setVisibility(View.INVISIBLE);
            }
        back.setOnClickListener(this);
        next.setOnClickListener(this);
        rotate.setOnClickListener(this);
        snap_btn.setOnClickListener(this);
        crop.setOnClickListener(this);
        choose.setOnClickListener(this);
        mirror.setOnClickListener(this);
        if(getArguments().getString("what").equals("writing_design")){
            v.setFocusableInTouchMode(true);
            v.requestFocus();
            v.setOnKeyListener( new View.OnKeyListener()
            {
                @Override
                public boolean onKey( View v, int keyCode, KeyEvent event )
                {
                    if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                    {

                        getFragmentManager().popBackStack("designer", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        return true;
                    }
                    return false;
                }
            } );
        }
        }
        else{}
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            path.clear();

            for (int i = 0, l = images.size(); i < l; i++) {
                path.add(images.get(i).path);
            }



            set=false;
            setImage(path);
            pDialog = new ProgressDialog(v.getContext(),R.style.AppCompatAlertDialogStyle);



            aviLoader.smoothToShow();
            progressContainer.setVisibility(View.VISIBLE);
            progressTxt.setText("Please wait for a while...");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(pDialog.isShowing()){
                        bit.clear();
                        setThis();
                        setImage(path);

                        aviLoader.smoothToShow();
                        progressContainer.setVisibility(View.VISIBLE);
                        progressTxt.setText("Please wait for a while...");


                    }
                }
            },2500);
        }
    }

     void setImage( ArrayList<String> lit){
        pos=bit.size();
        for(int j=0;j<lit.size();j++){




            int finalJ = j;
            Needle.onBackgroundThread().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        bit.add(compressImage(path.get(finalJ)));


                        if(path.size()==bit.size()){

                            Needle.onMainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    setThis();
                                    if (getArguments().getString("what").equals("profile_pic")) {
                                        circular_cutter.setImageBitmap(null);
                                        circular_cutter.setImageBitmap(bit.get(0));
                                    } else {
                                        cropView.setImageBitmap(null);
                                        cropView.setImageBitmap(bit.get(0));
                                        cropView.cropToCenter();
                                    }
                                    bitmap = bit.get(0);
                                }
                            });

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        //setThis();
    }

    public Bitmap compressImage(String imageUri) {

        String filePath = imageUri;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 1024.0f;
        float maxWidth = 768.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return scaledBitmap;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float ratio  = (float)width/(float)height;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float)newWidth/ratio) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }

    public void setThis(){
        progressContainer.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    Bitmap RotateBitmap(float angle)
    {
        Matrix matrix = new Matrix();
        matrix.preRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

    }
     Bitmap RotateMirror()
    {
        Matrix matrix = new Matrix();
        matrix.preScale(true ? -1 : 1, false ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

    }

    @Override
    public void onClick(View v) {
        an= AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons);
        switch(v.getId()){
            case R.id.rotateChooser:
                rotate.startAnimation(an);
                try {
                    bitmap = RotateBitmap(90);
                    if (getArguments().getString("what").equals("profile_pic")) {
                        circular_cutter.setImageBitmap(bitmap);
                    } else {
                        snap = true;
                        cropView.setImageBitmap(null);
                        cropView.setImageBitmap(bitmap);
                    }
                }catch (NullPointerException e){
                    Toast.makeText(v.getContext(),"Please choose image(s) first",Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.snap:
                snap_btn.startAnimation(an);
                try {
                    if (snap) {
                        cropView.cropToCenter();
                    } else {
                        cropView.fitToCenter();
                        cropView.initWithFitToCenter(true);
                    }
                    snap = !snap;
                }catch (NullPointerException exception){
                    Toast.makeText(v.getContext(),"Please choose image(s) first",Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.cropChooser:
                crop.startAnimation(an);
                ani= AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_new);
                try{
                    snap=true;
                    cropView.setImageBitmap(cropView.getCroppedBitmap());
                    bit.set(pos,cropView.getCroppedBitmap());
                    bitmap=bit.get(pos);
                    horzRview.scrollToPosition(pos);
                    setThis();
                }
                catch (Exception e){
                    Toast.makeText(v.getContext(),"Please Choose Again",Toast.LENGTH_SHORT).show();
                   /* getActivity().finish();
                    Intent mIntent = new Intent(v.getContext(), ProfileHolder.class);
                    mIntent.putExtra("Open","chooser");
                    startActivity(mIntent);*/
                }
                break;

            case R.id.mirrorChooser:
                mirror.startAnimation(an);
                try {
                    bitmap = RotateMirror();
                    if (getArguments().getString("what").equals("profile_pic")) {
                        circular_cutter.setImageBitmap(bitmap);
                    } else {
                        snap = true;
                        cropView.replaceBitmap(bitmap);
                    }
                }catch (NullPointerException exception){
                    Toast.makeText(v.getContext(),"Please choose image(s) first",Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.choose:
                snap=true;
                //choose.startAnimation(an);
                set=true;

                //It's related to upload
                SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                SharedPreferences.Editor editor=sh.edit();
                editor.remove("upload");
                editor.apply();

                Hawk.delete("upload");

                Intent intent = new Intent(v.getContext(), AlbumSelectActivity.class);
                if(getArguments().getString("what").equals("timeline"))
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10-bit.size());
                else {
                    bit.clear();
                    cropView.setImageBitmap(null);
                    adapter.notifyDataSetChanged();
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1 );
                }
                startActivityForResult(intent, Constants.REQUEST_CODE);
                break;
            case R.id.backChooser:
                back.startAnimation(an);
                if(getArguments().getString("what").equals("writing_design")) {

                    getFragmentManager().popBackStack("designer", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                else  getActivity().finish();
                break;
            case R.id.nextChooser:

                MerlinsBeard merlinsBeard=MerlinsBeard.from(v.getContext());
                if (merlinsBeard.isConnected()) {
                    if(getArguments().getString("what").equals("timeline")&&bit.size()!=0){


                        sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                        if(!sh.contains("upload")|| !Hawk.contains("upload")) {
                            aviLoader.smoothToShow();
                            progressContainer.setVisibility(View.VISIBLE);
                            progressTxt.setText("Processing ...");


                            img_names.clear();
                            for (int i = 0; i < bit.size(); i++) {
                                Random generator = new Random();
                                int n = 10000;
                                n = generator.nextInt(n);
                                String fname = "Image-" + n + ".jpg";
                                try {
                                    saveImageFast(fname, bit.get(i));
                                    img_names.add(fname);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else{
                            Bundle bundle=new Bundle();

                            bundle.putParcelableArrayList("cropped",bit);
                            bundle.putStringArrayList("img_names",img_names);
                            bundle.putString("upload_time",upload_time);


                            ImageUpload ic=new ImageUpload();
                            ic.setArguments(bundle);
                            FragmentManager fmt=getFragmentManager();
                            FragmentTransaction ftt=fmt.beginTransaction();
                            ftt.replace(R.id.profile_holder_frame,ic);
                            ftt.addToBackStack( "cropper" ).commit();
                        }

                    }
                    else if(getArguments().getString("what").equals("writing_design")&&bit.size()!=0){

                        ViewGroup mContainer = getActivity().findViewById(R.id.profile_holder_frame);
                        mContainer.removeAllViews();
                        WritingDesign ic=new WritingDesign();
                        Bundle b=new Bundle();
                        b.putParcelable("bitmapDesign",bit.get(0));
                        b.putString("fragName","cropper");
                        ic.setArguments(b);
                        FragmentManager fmt=getFragmentManager();
                        FragmentTransaction ftt=fmt.beginTransaction();
                        ftt.replace(R.id.profile_holder_frame,ic).commit();
                    }
                    else if(getArguments().getString("what").equals("profile_pic")&&bit.size()!=0){

                        aviLoader.smoothToShow();
                        progressContainer.setVisibility(View.VISIBLE);
                        progressTxt.setText("Uploading ...");


                        Random generator = new Random();
                        int n = 10000;
                        n = generator.nextInt(n);
                        String fname = "Image-" + n + ".jpg";

                        try {
                            saveImage(fname,getResizedBitmap(circular_cutter.getCroppedBitmap(),circular_cutter.getCroppedBitmap().getWidth()),
                                    "profile_pic");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    else if(getArguments().getString("what").equals("wall_pic")&&bit.size()!=0){

                        aviLoader.smoothToShow();
                        progressContainer.setVisibility(View.VISIBLE);
                        progressTxt.setText("Uploading ...");

                        Random generator = new Random();
                        int n = 10000;
                        n = generator.nextInt(n);
                        String fname = "Image-" + n + ".jpg";

                        try {
                            saveImage(fname,bit.get(0),"wall_pic");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(getArguments().getString("what").equals("msg_pic")&&bit.size()!=0){

                        aviLoader.smoothToShow();
                        progressContainer.setVisibility(View.VISIBLE);
                        progressTxt.setText("Uploading ...");

                        Random generator = new Random();
                        int n = 10000;
                        n = generator.nextInt(n);
                        String fname = "Image-" + n + ".jpg";

                        try {
                            saveImage(fname,bit.get(0),"msg_pic");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(v.getContext(),"Please choose any image(s) to proceed.",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(next, "You're not connected to internet .", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(Color.parseColor("#43cea2"));
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.textColor));
                    TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.parseColor("#001919"));
                    snackbar.show();
                }

                break;
        }
    }
    @NonNull
    private RequestBody createPartFromString(String desc) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM,desc);
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
                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
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
                        Bundle bundle=new Bundle();
                        Needle.onBackgroundThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                bundle.putParcelableArrayList("cropped",bit);
                                bundle.putStringArrayList("img_names",img_names);
                                bundle.putString("upload_time",upload_time);
                            }
                        });

                        //Doing the Magic
                        Needle.onMainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                //pDialog.dismiss();
                                int time;
                                if(bit.size()==1) time=1000;
                                else if(bit.size()>1 &&bit.size()<=3) time=2000;
                                else time=5000;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressContainer.setVisibility(View.GONE);
                                        SharedPreferences.Editor editor=sh.edit();
                                        editor.putString("upload_time",upload_time);
                                        editor.apply();

                                        ImageUpload ic=new ImageUpload();
                                        ic.setArguments(bundle);
                                        FragmentManager fmt=getFragmentManager();
                                        FragmentTransaction ftt=fmt.beginTransaction();
                                        ftt.replace(R.id.profile_holder_frame,ic);
                                        ftt.addToBackStack( "cropper" ).commit();
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
    void saveImage(String imgName, Bitmap bm,String what) throws IOException {
        //Create Path to save Image
        File file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/InfinityPic"); //Creates app specific folder
        file_path.mkdirs();
        File imageFile = new File(file_path, imgName); // Imagename.png
        FileOutputStream out = new FileOutputStream(imageFile);
        try {
            bm.compress(Bitmap.CompressFormat.JPEG, 95, out); // Compress Image
            out.flush();
            out.close();


            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(v.getContext(), new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);


                    RequestBody description ;

                    if(getArguments().getString("what").equals("msg_pic")) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                        time_format = simpleDateFormat.format(new Date());
                        JSONObject ob=new JSONObject();
                        try {
                            ob.put("msgTime",time_format);
                            ob.put("roomKey", getArguments().getString("roomKey"));
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        description = createPartFromString(ob.toString());
                    }
                    else{
                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                        description = createPartFromString(sh.getString("username",null));
                    }


                    FileUploadService service = ServiceGenerator.createService(FileUploadService.class);

                    Call<ResponseBody> call;

                    if(what.equals("profile_pic"))
                    call = service.uploadProfilePic(description, prepareFilePart("photo", path));

                    else if(what.equals("msg_pic"))
                        call = service.uploadMsgPic(description, prepareFilePart("photo", path));

                    else  call = service.uploadWallPic(description, prepareFilePart("photo", path));


                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call,
                                               Response<ResponseBody> response) {

                            //Log.v("Upload", "success");
                            //Deleting saved Img

                            File file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/InfinityPic");


                            if (file_path.exists()) {
                                String deleteCmd = "rm -r " + file_path;
                                Runtime runtime = Runtime.getRuntime();
                                try {
                                    runtime.exec(deleteCmd);
                                    // Toast.makeText(getContext(),"Folder Deleted Successfully",Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(getArguments().getString("what").equals("msg_pic")){
                                BottomFrag.msgImgLink="http://103.93.17.48:81/messaging/"+
                                        getArguments().getString("roomKey")+"/"+time_format+"/"+"pic.jpg";
                            }

                            pDialog.dismiss();

                            getActivity().finish();

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            if(t.getMessage()!=null) {
                                Log.e("Upload error:", t.getMessage());
                                Toast.makeText(v.getContext(),"Sorry ! Couldn't upload", Toast.LENGTH_LONG).show();
                                progressContainer.setVisibility(View.GONE);
                                //Don't toast t.getMessage it would show the ip address which is bad
                            }
                        }
                    });








                }
            });
        } catch (Exception e) {
            throw new IOException();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socket.disconnect();
    }
}
