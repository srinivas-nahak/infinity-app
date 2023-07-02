package fcm;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orhanobut.hawk.Hawk;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Random;

import community.infinity.R;
import community.infinity.activities.Home_Screen;
import community.infinity.activities.ProfileHolder;
import needle.Needle;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

/**
 * Created by Srinu on 12-02-2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    //Varibales for notifications
    String title, body, post_owner_username, post_owner_time, notif_commenter_username, notif_comment_time, follower_username,messager_username,type,action;
    private Bundle b;
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        title=remoteMessage.getData().get("title");//remoteMessage.getNotification().getTitle();
        body=remoteMessage.getData().get("body");//remoteMessage.getNotification().getBody();
        type=remoteMessage.getData().get("type");
        action=remoteMessage.getData().get("click_action");//remoteMessage.getNotification().getClickAction();


       if(type.equals("likes")||type.equals("infinity_notif")){
           post_owner_username=remoteMessage.getData().get("post_owner_username");
           post_owner_time=remoteMessage.getData().get("owner_post_time");
       }
       else if(type.equals("comments")||type.equals("comment_likes")){
           post_owner_username=remoteMessage.getData().get("post_owner_username");
           post_owner_time=remoteMessage.getData().get("owner_post_time");
           notif_commenter_username=remoteMessage.getData().get("commenter_username");
           notif_comment_time=remoteMessage.getData().get("comment_time");
       }
       else if(type.equals("follow")){
           follower_username=remoteMessage.getData().get("follower_username");
       }
       else if(type.equals("messaging")){
           messager_username=remoteMessage.getData().get("sender_username");
       }

        //Showing Notification
        /*MyNotificationManager.getInstance(getApplicationContext()).displayNotification(title,body,post_owner_username,
                post_owner_time,notif_commenter_username,notif_comment_time,follower_username,messager_username,type,action);*/

        NotificationCompat.Builder mBuilder =
                null;
        try {
            mBuilder = new NotificationCompat.Builder(getApplicationContext(), Constants.CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon_five)
                    .setContentTitle(title)
                    .setContentText(URLDecoder.decode(body, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Intent i = new Intent();
        i.setAction(action);

        b = new Bundle();

            //Adding this to send user to homescreen on back click instead of exit
            b.putString("go_home","yes");
        if (type.equals("likes")) {
            b.putString("post_owner_username", post_owner_username);
            b.putString("post_owner_time", post_owner_time);
            b.putString("what", "show_notification_post");
            b.putString("Open","starred");

            // i.putExtra("Open", "starred");
        }
        else if (type.equals("comments")) {
            b.putString("post_owner_username",post_owner_username);
            b.putString("post_owner_time", post_owner_time);
            b.putString("notif_commenter_username", notif_commenter_username);
            b.putString("notif_comment_time",notif_comment_time);
            b.putString("what", "show_notification_post");
            b.putString("Open","starred");
            //i.putExtra("Open", "starred");
        }
        else if (type.equals("comment_likes")) {

            b.putString("post_owner_username", post_owner_username);
            b.putString("post_owner_time", post_owner_time);
            b.putString("notif_commenter_username", notif_commenter_username);
            b.putString("notif_comment_time", notif_comment_time);
            b.putString("what", "show_notification_post");
            b.putString("Open","starred");
            // i.putExtra("Open", "starred");
        }
        else if (type.equals("follow")) {
            b.putString("searchUsername", follower_username);
            b.putString("Open","search_profile");
            //i.putExtra("Open", "search_profile");
        }
        else if(type.equals("follow_request")){
            b.putString("Open","notifications");
        }
        else if(type.equals("messaging")){
            b.putString("Open","messaging");
            b.putString("msg_username",messager_username);
        }

        i.putExtras(b);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
        //ctx.startActivity(i);

        Random random=new Random();
        int n=1000;
        n=random.nextInt(n);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, n, i, PendingIntent.FLAG_ONE_SHOT);

        /*
         *  Setting the pending intent to notification builder
         * */

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        if (mNotifyMgr != null) {
            mBuilder.setAutoCancel(true);
            //mNotifyMgr.notify(1, mBuilder.build());
           if(!foregrounded()) {
               mNotifyMgr.notify(n, mBuilder.build());
               Hawk.init(this).build();
               SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(this);
               SharedPreferences.Editor editor=sh.edit();
               if(type.equals("messaging")) {
                   Hawk.put("msg_notif","yes");
                   editor.putString("msg_notif","yes");
                   editor.apply();
                  // Home_Screen.msgNotif = true;
               }
               else {
                   Hawk.put("normal_notif","yes");
                   editor.putString("normal_notif","yes");
                   editor.apply();
                  // Home_Screen.normalNotif = true;
               }
           }
            else {
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(Home_Screen.homeActive) {
                            Intent it = new Intent("EVENT_SNACKBAR");
                            it.putExtra("body",body);
                            it.putExtra("action",action);
                            it.putExtras(b);

                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(it);
                            /*Home_Screen h=new Home_Screen();
                            h.showSnack(body,action,b,getApplicationContext());*/
                        }
                        if(ProfileHolder.profileActive){
                            Intent it = new Intent("EVENT_SNACKBAR_PROFILE");
                            it.putExtra("body",body);
                            it.putExtra("action",action);
                            it.putExtras(b);

                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(it);
                            /*ProfileHolder p=new ProfileHolder();
                            p.showSnack(body,action,b,getApplicationContext());*/
                        }
                    }
                });

            }
        }

    }

    public boolean foregrounded() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }
}
