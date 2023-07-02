package layout;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.support.v7.widget.AppCompatEditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import community.infinity.network_related.SocketAddress;
import community.infinity.activities.Home_Screen;
import community.infinity.activities.ProfileHolder;
import community.infinity.R;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class login extends Fragment implements View.OnClickListener {


Button login,signup;Animation an;TextView tv;
    private AppCompatEditText user,pass;
    private View v;
    private Socket socket;
    {
        try{
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("login",handleIncomingMessage);
    }

    public login() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v=inflater.inflate(R.layout.fragment_login, container, false);
        user= v.findViewById(R.id.loginUserNameEmail);
        pass=v.findViewById(R.id.loginPassword) ;
        tv=v.findViewById(R.id.forgotPassword);
        login=v.findViewById(R.id.proceed);
        signup=v.findViewById(R.id.sign_up_button);
        tv.setOnClickListener(this);
        login.setOnClickListener(this);
        signup.setOnClickListener(this);
        

        //Building Hawk
        Hawk.init(v.getContext()).build();

        //Disabling Space for username
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }

        };
        user.setFilters(new InputFilter[] { filter });
        pass.setFilters(new InputFilter[] { filter });
        
        //Setting Default NotifType
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("notifType","nothing");
        editor.apply();
        return v;
    }

    @Override
    public void onClick(View view) {
        an= AnimationUtils.loadAnimation(getContext(),R.anim.fade_buttons);
        ProfileHolder profileHolder=new ProfileHolder();
        switch (view.getId()){
            case R.id.proceed:
                login.startAnimation(an);
                ConnectivityManager conMgr =  (ConnectivityManager)v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if (netInfo == null){
                    Toast.makeText(v.getContext(),"You're not connected to internet",Toast.LENGTH_LONG).show();
                }
                else {
                    //Dismissing Keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(),
                            InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    //Sending username for shortname
                    Bundle args = new Bundle();
                    String user_name = user.getText().toString().trim();
                    String user_pass = pass.getText().toString().trim();
                    args.putString("username", user_name);
                    profileHolder.saveDataProfile(args);
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(user_name).matches()) {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("log_email", user_name);
                            obj.put("pass", user_pass);
                            socket.emit("data", obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("log_username", user_name);
                            obj.put("pass", user_pass);
                            socket.emit("data", obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            case R.id.sign_up_button:
                signup.startAnimation(an);
                ConnectivityManager conMgri =  (ConnectivityManager)v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfoi = conMgri.getActiveNetworkInfo();
                if (netInfoi == null){
                    Toast.makeText(v.getContext(),"You're not connected to internet",Toast.LENGTH_LONG).show();
                }
                else {
                    signup f = new signup();
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_change, f);
                    ft.commit();
                }
                break;
            case R.id.forgotPassword:
                tv.startAnimation(an);

                ForgotPassword g=new ForgotPassword();
                FragmentManager gm=getFragmentManager();
                FragmentTransaction gt=gm.beginTransaction();
                gt.replace(R.id.fragment_change,g);
                gt.commit();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();

        //Checking Internet Connection Status
        ConnectivityManager conMgr =  (ConnectivityManager)v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null){
            Toast.makeText(v.getContext(),"You're not connected to internet",Toast.LENGTH_LONG).show();
        }
    }

    private  Emitter.Listener handleIncomingMessage = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            //  Toast.makeText(v.getContext(),"Hello India",Toast.LENGTH_LONG).show();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray arr=(JSONArray)args[0];
                  if(arr!=null &&arr.length()>0) {
                      try {
                          JSONObject data = arr.getJSONObject(0);

                          String username = data.getString("username");
                          String fullname = data.getString("fullname");

                          SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                          SharedPreferences.Editor ed = sh.edit();
                          ed.putString("username", username);
                          ed.apply();

                          Needle.onBackgroundThread().execute(new Runnable() {
                              @Override
                              public void run() {
                                  Hawk.put("myUserName",username);
                              }
                          });



                          JSONObject obj = new JSONObject();
                          try {
                              if (sh.getString("fcmToken", null) != null) {
                                  obj.put("fcm_token_username", username);
                                  obj.put("fcm_token", sh.getString("fcmToken", null));
                                  socket.emit("data", obj);
                                  Intent i = new Intent(v.getContext(), Home_Screen.class);
                                  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                  i.putExtra("hey","hello");
                                  startActivity(i);
                              }
                          } catch (JSONException e) {
                              e.printStackTrace();
                          }




                      } catch (JSONException e) {
                          e.printStackTrace();
                      }
                  }
                    else{
                      if (android.util.Patterns.EMAIL_ADDRESS.matcher(user.getText().toString().trim()).matches())
                          Toast.makeText(v.getContext(),"Email or Password is Incorrect!",Toast.LENGTH_LONG).show();

                      else Toast.makeText(v.getContext(),"Username or Password is Incorrect!",Toast.LENGTH_LONG).show();

                      }

                }
            });
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socket.disconnect();
    }
}
