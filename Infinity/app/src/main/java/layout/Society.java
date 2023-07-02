package layout;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import community.infinity.R;
import community.infinity.network_related.SocketAddress;
import community.infinity.adapters.SocietyAdapter;
import custom_views_and_styles.ButtonTint;
import io.socket.client.IO;
import io.socket.client.Socket;

public class Society extends Fragment implements View.OnClickListener{

    private Button dismiss;
    private AlertDialog dialog;
    private Animation an;
    private AppCompatImageButton proceed;
    private TextView back;
    private ArrayList<String> names=new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private String my_username,my_fullname;
    private View view;
    private Socket socket;
    {
        try{
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public Society() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_society, container, false);
        back=view.findViewById(R.id.back_button_society);
        proceed=view.findViewById(R.id.proceed_society);
        recyclerView=view.findViewById(R.id.societyList_recyclerView);

        //Building Hawk
        Hawk.init(view.getContext()).build();

        //Tint
        ButtonTint tint=new ButtonTint("white");
        tint.setTint(back);
        tint.setTint(proceed);


        //TypeCasting Elements
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        if(Hawk.get("myUserName")!=null) my_username=Hawk.get("myUserName");
        else my_username = sharedPref.getString("username", null);
        if(Hawk.get("myFullName")!=null) my_fullname=Hawk.get("myFullName");
        else my_fullname = sharedPref.getString("fullname", null);

        //Following Infinity Account
        socket.disconnect();
        socket.connect();
        JSONObject obj=new JSONObject();
        try {
            obj.put("follower_username", my_username);
            obj.put("follower_fullname",my_fullname);
            obj.put("following_username", "infinity");
            socket.emit("data", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //RView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        names.add("Art"); names.add("Photography"); names.add("Writing");
        names.add("Automobile");names.add("Fun");names.add("Tricks");
        adapter=new SocietyAdapter(names,"login");
        recyclerView.setAdapter(adapter);


        //Alert Dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
         View v=LayoutInflater.from(view.getContext()).inflate(R.layout.society_message,null);
        dismiss=v.findViewById(R.id.dismiss);
        builder.setView(v);
        dialog=builder.create();
        dialog.show();
        dialog.getWindow().setDimAmount(0.3f);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
       //Onclick
        back.setOnClickListener(this);
        proceed.setOnClickListener(this);
        dismiss.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {

        an= AnimationUtils.loadAnimation(view.getContext(),R.anim.fade_buttons);
        switch (view.getId()){
            case R.id.dismiss:
                dialog.dismiss();
                break;
            case R.id.back_button_society:
                back.startAnimation(an);
                signup f=new signup();
                FragmentManager fm=getFragmentManager();
                FragmentTransaction ft=fm.beginTransaction();
                ft.replace(R.id.fragment_change,f);
                ft.commit();
                break;
            case R.id.proceed_society:
                proceed.startAnimation(an);
                //Sending Node
                JSONObject obj=new JSONObject();
                try {
                    Set<String> soc_list = new LinkedHashSet<>();
                    soc_list.addAll(SocietyAdapter.selected_soc_signup);

                    if(soc_list.size()>3) {
                        for (int i = 0; i < soc_list.size(); i++) {
                            if (i == 0 || i == 1 || i == 2) {
                                return;
                            }
                            else soc_list.remove(i);
                        }
                    }
                    obj.put("societyList_username",my_username);
                    obj.put("societyList",soc_list);
                    socket.emit("data",obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                profile_pic fi=new profile_pic();
                FragmentManager fmi=getFragmentManager();
                FragmentTransaction fti=fmi.beginTransaction();
                fti.replace(R.id.fragment_change,fi);
                fti.commit();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
       //Toast.makeText(view.getContext(),"Society list added",Toast.LENGTH_SHORT).show();
    }
}
