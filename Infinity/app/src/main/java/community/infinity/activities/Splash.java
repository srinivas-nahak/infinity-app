package community.infinity.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;

import java.io.File;

import community.infinity.R;
import community.infinity.writing.RememberTextStyle;
import custom_views_and_styles.ReverseInterpolator;

public class Splash extends Activity {

    private String my_username;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        //Building Hawk
        Hawk.init(this).build();

       /* final Typeface font = Typeface.createFromAsset(getAssets(), "fonts/"+"BerkshireSwash-Regular.ttf");
        tag_line.setTypeface(font);

        Glide.with(this).load(R.drawable.icon_five).into(icon);*/



       /* Animation grow= AnimationUtils.loadAnimation(this,R.anim.grow);
        Animation bounce= AnimationUtils.loadAnimation(this,R.anim.bounce);
        icon.startAnimation(bounce);

        grow.setInterpolator(new ReverseInterpolator());
        tag_line.startAnimation(grow);*/

       //Getting my Username



       try{
           SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

           if(Hawk.contains("myUserName")&&Hawk.get("myUserName")!=null) {
               my_username = Hawk.get("myUserName");
           }
           else if(sharedPref.contains("username")&&sharedPref.getString("username",null)!=null) {
               my_username = sharedPref.getString("username", null);

           }
           else {
               my_username = "";
           }



           new Handler().postDelayed(new Runnable() {
               @Override
               public void run() {
                   if (my_username.equals("")) {
                       if(sharedPref.contains("theme")&&sharedPref.getInt("theme",0)!=0)
                           RememberTextStyle.themeResource=sharedPref.getInt("theme",0);
                       else if(Hawk.contains("theme"))
                           RememberTextStyle.themeResource=Hawk.get("theme");
                       else
                       {
                           RememberTextStyle.themeResource=R.drawable.theme1;
                           Hawk.put("theme",R.drawable.theme1);
                           SharedPreferences.Editor editor=sharedPref.edit();
                           editor.putInt("theme",R.drawable.theme1);
                           editor.apply();
                       }
                       finish();
                       Intent intent = new Intent(getApplicationContext(), Login_Page.class);
                       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NO_ANIMATION);
                       startActivity(intent);
                   }
                   else{
                       if(sharedPref.contains("theme")&&sharedPref.getInt("theme",0)!=0)
                           RememberTextStyle.themeResource=sharedPref.getInt("theme",0);
                       else if(Hawk.contains("theme"))
                           RememberTextStyle.themeResource=Hawk.get("theme");
                       else
                       {
                           Hawk.put("theme",R.drawable.theme1);
                           SharedPreferences.Editor editor=sharedPref.edit();
                           editor.putInt("theme",R.drawable.theme1);
                           editor.apply();
                       }
                       finish();
                       Intent intent = new Intent(getApplicationContext(), Home_Screen.class);
                       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NO_ANIMATION);
                       startActivity(intent);
                   }
               }
           },500);

       }
       catch (Exception e){

       }


    }
}
