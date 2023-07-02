package community.infinity.BottomFrags;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;

import community.infinity.R;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.Login_Page;
import community.infinity.activities.ProfileHolder;
import community.infinity.writing.RememberTextStyle;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by Srinu on 10-08-2017.
 */

public class FloatingSettings extends AAH_FabulousFragment  {


    private FloatingActionButton circleBtn,profile_btn,invite_btn,starred_btn,settings_btn,
            opportunity_btn,tips_btn,rating_btn,society_btn,logout_btn,report_btn;
    ArrayList<String> names;
    private TextView report_txt,invite_txt;
    private String myUserName;
    private View contentView;
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public static FloatingSettings newInstance() {
        FloatingSettings f = new FloatingSettings();
        return f;
    }

    @Override

    public void setupDialog(Dialog dialog, int style) {
        socket.disconnect();
        socket.connect();


        contentView = View.inflate(getContext(), R.layout.filter_view, null);

        //Type Casting
        RelativeLayout rl_content = contentView.findViewById(R.id.rl_content);
        RelativeLayout ll_buttons = contentView.findViewById(R.id.ll_buttons);
        circleBtn= contentView.findViewById(R.id.circleButton);
        profile_btn= contentView.findViewById(R.id.profile_btn);
        invite_btn= contentView.findViewById(R.id.invite_btn);
        starred_btn= contentView.findViewById(R.id.starred_btn);
        settings_btn= contentView.findViewById(R.id.settings_btn);
        opportunity_btn= contentView.findViewById(R.id.opportunity_btn);
        tips_btn= contentView.findViewById(R.id.tips_btn);
        rating_btn= contentView.findViewById(R.id.rating_btn);
        society_btn= contentView.findViewById(R.id.society_list_btn);
        logout_btn= contentView.findViewById(R.id.logout_btn);
        report_btn=contentView.findViewById(R.id.report_btn);
        report_txt=contentView.findViewById(R.id.report_txt);
        invite_txt=contentView.findViewById(R.id.invite_txt);
        
        //Setting Bg
        rl_content.setBackgroundResource(RememberTextStyle.themeResource);

        Hawk.init(contentView.getContext()).build();
        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(contentView.getContext());
        if(Hawk.get("myUserName")!=null) myUserName=Hawk.get("myUserName");
        else myUserName=sh.getString("username",null);

        if(myUserName.equals("infinity")){
            invite_btn.setVisibility(View.GONE);
            invite_txt.setVisibility(View.GONE);
            report_btn.setVisibility(View.VISIBLE);
            report_txt.setVisibility(View.VISIBLE);
        }

        checking();

        //params to set
        setAnimationDuration(600); //optional; default 500ms
        setPeekHeight(300); // optional; default 400dp
        setCallbacks((AAH_FabulousFragment.Callbacks) getActivity()); //optional; to get back result
        setAnimationListener((AAH_FabulousFragment.AnimationListener) getActivity()); //optional; to get animation callbacks
        setViewgroupStatic(ll_buttons); // optional; layout to stick at bottom on slide
        setViewMain(rl_content); //necessary; main bottomsheet view
        setMainContentView(contentView); // necessary; call at end before super
        super.setupDialog(dialog,1); //call super at last
    }
void checking(){
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(contentView.getContext());
    Gson gson = new Gson();
    String json = sharedPrefs.getString("savedArray", null);
    Type type = new TypeToken<ArrayList<String>>() {}.getType();
    names = gson.fromJson(json, type);

    //Onclick Methods
    circleBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            closeFilter("closed");
        }
    });
    profile_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent i=new Intent(contentView.getContext(),ProfileHolder.class);
                    Bundle b=new Bundle();
                    b.putString("searchUsername",myUserName);
                    i.putExtras(b);
                    i.putExtra("Open","search_profile");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    contentView.getContext().startActivity(i);

                }
            }
    );
    starred_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(getContext(),ProfileHolder.class);
                    Bundle b=new Bundle();
                    b.putString("what","starred");
                    i.putExtras(b);
                    i.putExtra("Open","starred");
                    startActivity(i);
                }
            }
    );
    invite_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String sAux = "\nBe a part of the Infinity world to connect with your like minded people\n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=community.infinity\n";
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            }
    );
    tips_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle b = new Bundle();
                    b.putString("what", "tips");
                    Intent i = new Intent("profile");
                    i.putExtras(b);
                    i.putExtra("Open", "starred");
                    contentView.getContext().startActivity(i);
                }
            }
    );
    opportunity_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle b = new Bundle();
                    b.putString("what", "opportunities");
                    Intent i = new Intent("profile");
                    i.putExtras(b);
                    i.putExtra("Open", "starred");
                    contentView.getContext().startActivity(i);
                }
            }
    );
    settings_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(getContext(),ProfileHolder.class);
                    i.putExtra("Open","account_settings");
                    startActivity(i);
                }
            }
    );
    report_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(getContext(),ProfileHolder.class);
                    i.putExtra("Open","reports");
                    startActivity(i);
                }
            }
    );
    society_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!myUserName.equals("infinity")) {
                        Intent i = new Intent(getContext(), ProfileHolder.class);
                        i.putExtra("Open", "society_list");
                        startActivity(i);
                    }
                }
            }
    );
    rating_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent viewIntent =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse("https://play.google.com/store/apps/details?id=community.infinity"));
                    startActivity(viewIntent);
                }
            }
    );
    logout_btn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Hawk.deleteAll();
                    RememberTextStyle.themeResource=0;
                    SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(contentView.getContext());
                    String fcm_key=sh.getString("fcmToken", null);
                    JSONObject obj = new JSONObject();
                    try {
                        //Removing fcm key
                        if (fcm_key != null) {
                            obj.put("logout_fcm_token_username", myUserName);
                            obj.put("logout_fcm_token", fcm_key);
                            socket.emit("data", obj);
                            SharedPreferences.Editor ed = sh.edit();
                            ed.clear();
                            ed.putString("fcmToken",fcm_key);
                            ed.apply();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RememberTextStyle.themeResource=R.drawable.theme1;
                    Hawk.put("theme",R.drawable.theme1);
                    SharedPreferences.Editor editor=sh.edit();
                    editor.putInt("theme",R.drawable.theme1);
                    editor.apply();

                    Intent i=new Intent(getContext(),Login_Page.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }
    );
}

    @Override
    public void onStart() {
        checking();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}