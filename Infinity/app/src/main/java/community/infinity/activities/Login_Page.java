package community.infinity.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.util.ArrayList;

import community.infinity.R;
import community.infinity.writing.RememberTextStyle;
import layout.login;


public  class Login_Page extends AppCompatActivity {
RelativeLayout rl;
    public static final String name= "user_name";
    public static final String pass= "user_pass";
    public static final String email= "user_email";
    public static final String dob= "user_dob";
    public static final String gender= "user_gender";
    public static final String shortname= "short_nam";
    public static final String keymail= "email_id";
    private ArrayList<String> dames=new ArrayList<>();
    public static Bundle bundle;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean bool=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login__page);
        rl = findViewById(R.id.login_relativeLayout);
        if(RememberTextStyle.themeResource==0){
            SharedPreferences sharedPref= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Hawk.init(this).build();
            if(sharedPref.contains("theme")&&sharedPref.getInt("theme",0)!=0)
                RememberTextStyle.themeResource=sharedPref.getInt("theme",0);
            else if(Hawk.contains("theme"))
                RememberTextStyle.themeResource=Hawk.get("theme");
            rl.setBackgroundResource(RememberTextStyle.themeResource);
        }
        else {
            rl.setBackgroundResource(RememberTextStyle.themeResource);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Checking Permissions
        verifyStoragePermissions(this);


        //Saving in SharedPreferences


        login f=new login();
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        ft.replace(R.id.fragment_change,f);
        ft.commit();
    }


    public void saveData(final Bundle data) {
    bundle=data;
    }
    public Bundle getSavedData() {
        return bundle;
    }

    //Deleting Cache
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteCache(this);
        //socket.disconnect();
    }
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
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
    protected void onStop() {
        super.onStop();
        deleteCache(this);
    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
            );
        }
    }
    @Override
    public void onBackPressed() {
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

}
