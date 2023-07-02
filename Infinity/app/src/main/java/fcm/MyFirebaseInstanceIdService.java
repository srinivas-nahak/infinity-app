package fcm;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import needle.Needle;

import static android.content.ContentValues.TAG;

/**
 * Created by Srinu on 12-02-2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Hawk.init(this).build();
        if(refreshedToken!=null){
            SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor edit=sh.edit();
            edit.putString("fcmToken",refreshedToken);
            edit.apply();
        }
    }
}
