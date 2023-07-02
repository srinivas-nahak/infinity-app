package community.infinity.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.util.ArrayList;

import community.infinity.ExceptionHandler.MyExceptionHandler;
import community.infinity.Reports.Reports;
import community.infinity.SettingsMenu.AccountSettingsFrag;
import community.infinity.image_related.ImageChooser;
import community.infinity.R;
import community.infinity.profile.SearchProfile;
import community.infinity.writing.RememberTextStyle;
import community.infinity.writing.ShortBook;
import community.infinity.SettingsMenu.Starred;
import community.infinity.writing.WritingLayout;
import needle.Needle;

import static com.iceteck.silicompressorr.FileUtils.isDownloadsDocument;
import static com.iceteck.silicompressorr.FileUtils.isExternalStorageDocument;

/**
 * Created by Srinu on 12-08-2017.
 */

public class ProfileHolder extends AppCompatActivity{

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private BroadcastReceiver mMessageReceiver = null;

    public static Bundle bundlei;
    public ImageView home;
    ImageChooser img;
    public  static boolean profileActive;
    String i="1";
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_holder);
        home=findViewById(R.id.go_home);
        FrameLayout parentLay=findViewById(R.id.profile_holder_frame);
        if(RememberTextStyle.themeResource==0){
            SharedPreferences sharedPref= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Hawk.init(this).build();
            if(sharedPref.contains("theme")&&sharedPref.getInt("theme",0)!=0)
                RememberTextStyle.themeResource=sharedPref.getInt("theme",0);
            else if(Hawk.contains("theme"))
                RememberTextStyle.themeResource=Hawk.get("theme");
            parentLay.setBackgroundResource(RememberTextStyle.themeResource);
        }
        else {
            parentLay.setBackgroundResource(RememberTextStyle.themeResource);
        }


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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        File dir = new File(Environment.getExternalStorageDirectory() + "/Android/data/crashReports");
        if(dir.exists() && dir.isDirectory()) {
            // do something here
        }
        else{
            dir.mkdir();
        }

        Bundle bd=getIntent().getExtras();
        String b=bd.getString("Open");
        if(b!=null) {
            //Fragment Transaction
            if (b.equals("compose")) {
                home.setVisibility(View.GONE);
                WritingLayout g = new WritingLayout();
                FragmentManager fmt = getFragmentManager();
                FragmentTransaction ftt = fmt.beginTransaction();
                ftt.replace(R.id.profile_holder_frame, g);
                ftt.commit();
            } else if (b.equals("short_book")) {
                home.setVisibility(View.GONE);
                ShortBook g = new ShortBook();
                FragmentManager fmt = getFragmentManager();
                FragmentTransaction ftt = fmt.beginTransaction();
                ftt.replace(R.id.profile_holder_frame, g);
                ftt.commit();
            } else if (b.equals("search_profile")) {
                Bundle bind = getIntent().getExtras();
                SearchProfile g = new SearchProfile();
                g.setArguments(bind);
                FragmentManager fmt = getFragmentManager();
                FragmentTransaction ftt = fmt.beginTransaction();
                ftt.replace(R.id.profile_holder_frame, g);
                ftt.commit();
                home.setVisibility(View.GONE);
            } else if (b.equals("account_settings") || b.equals("society_list") || b.equals("events") || b.equals("achievements")) {
                AccountSettingsFrag f = new AccountSettingsFrag();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.profile_holder_frame, f);
                ft.commit();
            } else if (b.equals("reports")) {
                Reports f = new Reports();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.profile_holder_frame, f);
                ft.commit();
            } else if (b.equals("starred")) {
                Bundle bind = getIntent().getExtras();
                home.setVisibility(View.GONE);
                Starred s = new Starred();
                s.setArguments(bind);
                FragmentTransaction ftt = getFragmentManager().beginTransaction();
                ftt.replace(R.id.profile_holder_frame, s);
                ftt.commit();
            } else if (b.equals("chooser")) {
                home.setVisibility(View.GONE);
                ImageChooser img = new ImageChooser();
                img.setArguments(getIntent().getExtras());
                FragmentTransaction ftt = getFragmentManager().beginTransaction();
                ftt.replace(R.id.profile_holder_frame, img);
                ftt.commit();
            }
        }
        else if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null) {
            // Handle single image being sent from Gallery
            Uri imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            String path = getRealPath(this,imageUri);
            ArrayList<String> path_list=new ArrayList<>();
            path_list.add(path);

            home.setVisibility(View.GONE);
            ImageChooser img = new ImageChooser();
            Bundle bund=new Bundle();
            bund.putString("what","timeline");
            bund.putStringArrayList("imageGallery",path_list);
            img.setArguments(bund);
            FragmentTransaction ftt = getFragmentManager().beginTransaction();
            ftt.replace(R.id.profile_holder_frame, img);
            ftt.commit();

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()) && getIntent().getType() != null) {
            // Handle multiple images being sent from Gallery
            if (getIntent().getType().startsWith("image/")) {
                ArrayList<Uri> imageUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                ArrayList<String> path_list = new ArrayList<>();
                int size=imageUris.size();
                if(size>10){
                    size=10;
                    Toast.makeText(this,"Only 10 images can be proceeded.",Toast.LENGTH_LONG).show();
                }

                for (int i = 0; i < size; i++) {
                    path_list.add(getRealPath(this ,imageUris.get(i)));
                }

                home.setVisibility(View.GONE);
                ImageChooser img = new ImageChooser();
                Bundle bund = new Bundle();
                bund.putString("what", "timeline");
                bund.putStringArrayList("imageGallery", path_list);
                img.setArguments(bund);
                FragmentTransaction ftt = getFragmentManager().beginTransaction();
                ftt.replace(R.id.profile_holder_frame, img);
                ftt.commit();
            }
        }

    }
    public void goHome(View view){
        Intent i=new Intent(getApplicationContext(),Home_Screen.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
    //Saving Bundle
    public void saveDataProfile(final Bundle data) {
        bundlei=data;
    }
    public Bundle getSavedDataProfile() {
        return bundlei;
    }
    //Dispatching TouchEvent to children
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        if(Starred.swipe!=null)
        Starred.swipe.dispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getRealPath(Context context, Uri uri) {
        String filePath = "";

        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {

                if (Build.VERSION.SDK_INT > 20) {
                    //getExternalMediaDirs() added in API 21
                    File extenal[] = context.getExternalMediaDirs();
                    if (extenal.length > 1) {
                        filePath = extenal[1].getAbsolutePath();
                        filePath = filePath.substring(0, filePath.indexOf("Android")) + split[1];
                    }
                }else{
                    filePath = "/storage/" + type + "/" + split[1];
                }
                return filePath;
            }

        } else if (isDownloadsDocument(uri)) {
            // DownloadsProvider
            final String id = DocumentsContract.getDocumentId(uri);
            //final Uri contentUri = ContentUris.withAppendedId(
            // Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow(column);
                    String result = cursor.getString(index);
                    cursor.close();
                    return result;
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } else if (DocumentsContract.isDocumentUri(context, uri)) {
            // MediaProvider
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String[] ids = wholeID.split(":");
            String id;
            String type;
            if (ids.length > 1) {
                id = ids[1];
                type = ids[0];
            } else {
                id = ids[0];
                type = ids[0];
            }

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{id};
            final String column = "_data";
            final String[] projection = {column};
            Cursor cursor = context.getContentResolver().query(contentUri,
                    projection, selection, selectionArgs, null);

            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex(column);

                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
            return filePath;
        } else {
            String[] proj = {MediaStore.Audio.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                if (cursor.moveToFirst())
                    filePath = cursor.getString(column_index);
                cursor.close();
            }


            return filePath;
        }
        return null;
    }
    //Showing Notification Snanck
    public void showSnack(String content,String action,Bundle b){
        SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext() );
        SharedPreferences.Editor editor=sh.edit();

        Hawk.init(getApplicationContext()).build();

        if(content.contains(":")){

                Needle.onBackgroundThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        Hawk.put("msg_notif","yes");
                        editor.putString("msg_notif","yes");
                        editor.apply();
                    }
                });

        }
        else{
                Needle.onBackgroundThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        Hawk.put("normal_notif","yes");
                        editor.putString("normal_notif","yes");
                        editor.apply();
                    }
                });
        }
        Snackbar snackbar=Snackbar.make(home, content, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setClickable(true);
        sbView.setFocusable(true);
        sbView.setBackgroundColor(Color.parseColor("#ffffff"));
        TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.parseColor("#001919"));
        FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)sbView.getLayoutParams();
        params.gravity = Gravity.TOP;
        sbView.setLayoutParams(params);
        snackbar.show();

        sbView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setAction(action);

                i.putExtras(b);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                 startActivity(i);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        profileActive=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_SNACKBAR_PROFILE"));

    }

    @Override
    protected void onStop() {
        super.onStop();
        profileActive=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(this );
        SharedPreferences.Editor editor=sh.edit();
        editor.remove("upload");
        editor.apply();

        Hawk.delete("upload");
    }
}

