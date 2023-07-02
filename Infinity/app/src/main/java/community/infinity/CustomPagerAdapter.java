package community.infinity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import community.infinity.SettingsMenu.AccountSettingsFrag;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.HeightWrappingViewPager;
import custom_views_and_styles.ReverseInterpolator;
import ooo.oxo.library.widget.TouchImageView;

/**
 * Created by Srinu on 11-11-2017.
 */

public class CustomPagerAdapter extends PagerAdapter {
    //Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<String> mResources;
    private ImageView like_outline,like_btn,middle_heart;
    private Activity activity;
    private boolean about_us=false;
    private HeightWrappingViewPager pager;
    public CustomPagerAdapter(){

    }
    public CustomPagerAdapter(Activity activity, ArrayList<String> mResources, ImageView like_outline,
                              ImageView like_btn, ImageView middle_heart) {

        this.like_outline=like_outline;
        this.like_btn=like_btn;
        this.mResources=mResources;
        this.middle_heart=middle_heart;
        this.activity=activity;
        mLayoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notifyDataSetChanged();
    }
    public CustomPagerAdapter(Context ctx,ArrayList<String> imgResources,boolean about_us){
        this.mResources=imgResources;
        this.activity=(Activity)ctx;
        this.about_us=about_us;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ( object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
        final ImageView imageView = itemView.findViewById(R.id.imageView);


         ColorDrawable cd = new ColorDrawable(Color.parseColor("#20ffffff"));
            Activity activity=(Activity)imageView.getContext();
            if(!activity.isFinishing())
                Glide.with(activity).asBitmap().load(mResources.get(position)).
                        apply(new RequestOptions().placeholder(cd).diskCacheStrategy(DiskCacheStrategy.RESOURCE)).thumbnail(0.1f).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(compressImage(resource,"timeline"));
                    }
                });
            if(!about_us&&like_btn!=null){
                GestureDetector detector;
                detector = new GestureDetector(container.getContext(),
                        new GestureTap(container.getContext(), mResources.get(position), like_outline, like_btn, middle_heart, imageView));

                imageView.setOnTouchListener((view, event) -> {
                    detector.onTouchEvent(event);
                    return true;
                });



        }
        else{
               GestureDetector detector =new GestureDetector(container.getContext(),new GestureTap(container.getContext(),mResources.get(position),about_us));


                imageView.setOnTouchListener((view, event) -> {
                    detector.onTouchEvent(event);
                    return true;
                });
            }
        container.addView(itemView);

        return itemView;
    }
    public Bitmap compressImage(Bitmap bitmap,String what) {

        Bitmap scaledBitmap = null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95/*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeStream(bs,new Rect(),options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612
        float maxHeight;
        float maxWidth;
        if(what.equals("timeline")){
            maxHeight = 800.0f;
            maxWidth = 600.0f;
        }
        else{
            maxHeight = 600.0f;
            maxWidth = 600.0f;
        }

        float imgRatio = bitmap.getWidth() / bitmap.getHeight();
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
        options.inTempStorage = new byte[16 * 800];

        try {
//          load the bitmap from its path
            //bmp = BitmapFactory.decodeStream(bs);
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
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly

            Matrix matrix = new Matrix();
           matrix.postRotate(0);
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);


        return scaledBitmap;

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
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
    public boolean isVisible(final View view) {
        if (view == null) {
            return false;
        }
        if (!view.isShown()) {
            return false;
        }
        final Rect actualPosition = new Rect();
        view.getGlobalVisibleRect(actualPosition);

        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        final Rect screen = new Rect(0, 0, width, height);
        return actualPosition.intersect(screen);
    }
}

class GestureTap extends GestureDetector.SimpleOnGestureListener {
    private Context ctx;
    private String s;
    private boolean about_us;
    private AlertDialog dialog;
    private View dialog_view;
    private ImageView like_outline,like_btn,heart;
    private Animation ani;
    private ImageView imageView;
    public GestureTap(Context ctx,String s,ImageView like_outline,ImageView like_btn,ImageView heart,ImageView imageView) {
        this.like_outline=like_outline;
        this.like_btn=like_btn;
        this.heart=heart;
        this.ctx=ctx;
        this.s=s;
        this.imageView=imageView;
        dialog_view= LayoutInflater.from(ctx).inflate(R.layout.dialog_image,null);
    }
    public GestureTap(Context ctx,String s,boolean about_us) {
        this.ctx=ctx;
        this.s=s;
        this.about_us=about_us;
        this.imageView=imageView;
        dialog_view= LayoutInflater.from(ctx).inflate(R.layout.dialog_image,null);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if(!about_us) {
            if (like_outline.getVisibility() == View.VISIBLE) {
                ani = AnimationUtils.loadAnimation(ctx, R.anim.grow);
                like_outline.callOnClick();
                heart.setVisibility(View.VISIBLE);
                heart.startAnimation(ani);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        heart.setVisibility(View.INVISIBLE);
                    }
                }, 700);
            } else {
                ani = AnimationUtils.loadAnimation(ctx, R.anim.grow);
                like_btn.callOnClick();
                heart.setVisibility(View.VISIBLE);
                ani.setInterpolator(new ReverseInterpolator());
                heart.startAnimation(ani);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        heart.setVisibility(View.INVISIBLE);
                    }
                }, 700);
            }
        }
        return true;
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {

        android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(ctx);
        builder.setView(dialog_view);
        dialog=builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.9f);
        final TouchImageView img=dialog_view.findViewById(R.id.dialog_image);
        final DragToClose dragToClose=dialog_view.findViewById(R.id.dragViewDialogImg);
        Glide.with(ctx).asBitmap().load(s).apply(new RequestOptions().placeholder(new ColorDrawable(Color.parseColor("#20001919")))).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                CustomPagerAdapter c=new CustomPagerAdapter();
                img.setImageBitmap(c.compressImage(resource,"timeline"));
            }
        });

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
        return true;
    }
}