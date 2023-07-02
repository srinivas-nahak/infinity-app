package layout;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.net.URISyntaxException;
import java.net.URLEncoder;

import community.infinity.network_related.SocketAddress;
import community.infinity.activities.Home_Screen;
import community.infinity.activities.ProfileHolder;
import community.infinity.R;
import custom_views_and_styles.ButtonTint;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import multipleimageselect.activities.AlbumSelectActivity;
import multipleimageselect.helpers.Constants;
import needle.Needle;


public class profile_pic extends Fragment implements View.OnClickListener{

    Button back;
    private AppCompatImageButton proceed;
    boolean t;
    Animation an;
    private View v;
    private AppCompatEditText bio;
    private ImageView camera_img;
    CircleImageView profile_pic;
     ImageView rotate_right,rotate_mirror;
    private String username;


    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public profile_pic() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //socket.connect();
        /*socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.on("link", handleIncomingMessage);
            }
        });*/
       // socket.on("profile",handleIncomingMessage);
      // socket.on("data",handleIncomingMessage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v= inflater.inflate(R.layout.fragment_profile_pic, container, false);
        profile_pic=v.findViewById(R.id.profile_pic_set);
        //Glide.with(this).load(R.drawable.profile_pic_set_new).into(profile_pic);
        bio=v.findViewById(R.id.bio_signup);
        back=v.findViewById(R.id.back_button_profile);
        proceed=v.findViewById(R.id.proceed_profile);
        camera_img=v.findViewById(R.id.cameraImgProfilePic);

        //Getting Username
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        username=sharedPrefs.getString("username",null);

        //Setting Tint
        ButtonTint tint=new ButtonTint("white");
        tint.setTint(proceed);


        //Setting Onclik
        profile_pic.setOnClickListener(this);
        back.setOnClickListener(this);
        proceed.setOnClickListener(this);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                    /*if(close){
                        profile_pic.setVisibility(View.VISIBLE);
                        shortname.setVisibility(View.VISIBLE);
                        proceed.setVisibility(View.VISIBLE);
                        back.setVisibility(View.VISIBLE);
                        ivCrop.setVisibility(View.GONE);
                        cropHolder.setVisibility(View.GONE);
                        rotate_right.setVisibility(View.GONE);
                        rotate_mirror.setVisibility(View.GONE);
                        closeThis.setVisibility(View.GONE);
                        close=false;
                        return true;
                    }*/
                    return false;
                }

                return false;
            }
        } );
        //bind.remove("array");
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                socket.connect();
                JSONObject obj=new JSONObject();
                try {
                    obj.put("prof_username",username);
                    socket.emit("data",obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.on("profile", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        final JSONObject data = (JSONObject)args[0];
                        try {
                            JSONArray ar=data.getJSONArray("profile_pic");
                            JSONObject ob=ar.getJSONObject(0);
                            final String st = ob.getString("profile_pic");
                            Handler handler = new Handler(Looper.getMainLooper()) {
                                @Override
                                public void handleMessage(Message msg) {
                                    // Any UI task, example
                                    try {

                                        if (st.length() > 0) {

                                            ///Setting Only person icon if profile pid is null
                                                Glide.with(v.getContext()).asBitmap().load(st).
                                                        apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                .skipMemoryCache(true).override(250,250))
                                                        .into(new SimpleTarget<Bitmap>() {
                                                            @Override
                                                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                                super.onLoadFailed(errorDrawable);
                                                                Glide.with(v.getContext()).load(R.drawable.cicle_shape).
                                                                        apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                                                                                .skipMemoryCache(true))
                                                                        .into(profile_pic);
                                                                camera_img.setVisibility(View.VISIBLE);
                                                            }

                                                            @Override
                                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super
                                                                    Bitmap> transition) {
                                                                profile_pic.setImageBitmap(resource);
                                                                camera_img.setVisibility(View.GONE);

                                                            }
                                                        });

                                        }
                                        else{
                                            Glide.with(v.getContext()).load(R.drawable.cicle_shape).
                                                    apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true))
                                                    .into(profile_pic);
                                            camera_img.setVisibility(View.VISIBLE);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            handler.sendEmptyMessage(1);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    @Override
    public void onClick(View view) {
        an= AnimationUtils.loadAnimation(getContext(),R.anim.fade_buttons);
        //String short_name=shortname.getText().toString().trim();
        switch (view.getId()){
            case R.id.back_button_profile:
                back.startAnimation(an);
                Society f=new Society();
                FragmentManager fm=getFragmentManager();
                FragmentTransaction ft=fm.beginTransaction();
                ft.replace(R.id.fragment_change,f);
                ft.commit();
                break;
            case R.id.profile_pic_set:
               // choosePic();
                Intent i = new Intent(v.getContext(), ProfileHolder.class);
                Bundle b = new Bundle();
                b.putString("what", "profile_pic");
                i.putExtras(b);
                i.putExtra("Open", "chooser");
                startActivity(i);
                t=true;
                break;

            case R.id.proceed_profile:
               proceed.startAnimation(an);


               /* if(bio.getText().toString().trim().equals("")){
                    Toast.makeText(getContext(),"Please Enter Your Profession",Toast.LENGTH_LONG).show();
              }*/
               if(bio.getText().toString()!=null&&bio.getText().toString().length()>0) {
                   JSONObject obj=new JSONObject();
                   try {
                       obj.put("bio_username",username);
                       obj.put("bio_signup", URLEncoder.encode(bio.getText().toString().trim(), "UTF-8"));
                       socket.emit("data",obj);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }

            Intent intent=new Intent(getContext(), Home_Screen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("signup","yes");
            startActivity(intent);
                break;
        }
    }


    public void choosePic(){
        Intent intent = new Intent(v.getContext(), AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

      /*private  Emitter.Listener handleIncomingMessage = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
          //  Toast.makeText(getActivity().getApplicationContext(),"Hello India",Toast.LENGTH_LONG).show();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    //JSONObject d=(JSONObject) args[1];
                    String imgLink;
                    try {
                        imgLink = data.getString("data");
                        profile_pic.setImageBitmap(null);
                      Glide.with(getActivity().getApplicationContext()).load("http://"+imgLink).into(profile_pic);
                        if(imgLink!=null){
                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                        SharedPreferences.Editor editor = sharedPrefs.edit();

                        editor.putString("link",imgLink);
                        editor.apply();
                        }




                    } catch (JSONException e) {
                        Toast.makeText(getActivity().getApplicationContext(),"Please Try Again!",Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    };*/
    void setProfilePic(Bitmap bmp){
        profile_pic.setImageBitmap(null);
        profile_pic.setImageBitmap(bmp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}


