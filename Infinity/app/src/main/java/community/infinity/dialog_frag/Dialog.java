package community.infinity.dialog_frag;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import community.infinity.R;
import community.infinity.SettingsMenu.AccountSettingsFrag;
import community.infinity.activities.Home_Screen;
import community.infinity.adapters.AccountSettingsAdapter;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.ProfileHolder;
import community.infinity.adapters.SocietyAdapter;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

/**
 * Created by Srinu on 19-10-2017.
 */

public class Dialog extends DialogFragment {

    private RecyclerView dialog_recycler;
    private RecyclerView.Adapter add_soc_adapter;private ArrayList<String> uncommon;
    private TextView btn;
    private View v;
    private ArrayList<String> total_list=new ArrayList<>();
    private Socket socket;
    {
        try{
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("total_society_list",handleSocietyList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setDimAmount(0.9f);

        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.x = -10;        // set your X position here
        lp.y = -20;        // set your Y position here
        getDialog().getWindow().setAttributes(lp);
        //getDialog().getWindow().setGravity(Gravity.CENTER);
       // getDialog().setTitle("Choose Societies");
        v=inflater.inflate(R.layout.dialog_add_society,container,false);



        //Building Hawk
        Hawk.init(v.getContext()).build();

        btn=v.findViewById(R.id.add_soc_to_list);
        dialog_recycler=v.findViewById(R.id.dialog_soc_list);
        dialog_recycler.setHasFixedSize(true);
        dialog_recycler.setLayoutManager(new LinearLayoutManager(v.getContext()));
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        final Gson gson = new Gson();
        String json = sharedPrefs.getString("mySocietyList", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        final ArrayList<String> names = gson.fromJson(json, type);


        //Total List of Society
         total_list = new ArrayList<>();


        add_soc_adapter=new SocietyAdapter(total_list,names,"settings");
        dialog_recycler.setAdapter(add_soc_adapter);
        btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String jsonSocList = sharedPrefs.getString("mySocietyList", null);
                        Type type = new TypeToken<ArrayList<String>>() {}.getType();
                        Gson gson=new Gson();
                        ArrayList<String> arrayList = gson.fromJson(jsonSocList, type);

                            JSONObject ob = new JSONObject();
                            try {
                                 SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                                ob.put("update_society_list", SocietyAdapter.selected_soc_signup);
                                if(Hawk.get("myUserName")!=null)ob.put("update_society_list_username", Hawk.get("myUserName"));
                                else ob.put("update_society_list_username", sh.getString("username",null));
                                socket.emit("data", ob);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        getDialog().dismiss();
                    }
                }
        );
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Connecting Socket
        socket.disconnect();
        socket.connect();
        JSONObject obj=new JSONObject();
        try {
            obj.put("get_total_society_list","yes");
            socket.emit("data",obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private  Emitter.Listener handleSocietyList = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            try {
                JSONArray jsonArray=(JSONArray)args[0];


                //Clearing whole data in order to show new data in a proper sequence
                //timelineDataList.clear();
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Making SocietyContainer invisible if no society is selected

                            //Getting Societylist
                            JSONObject ob= (JSONObject) jsonArray.get(0);
                            String s=ob.getString("total_society_list");
                            String[] splits =  s.replace("[","").replace("]","").split(",");
                            ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(splits));

                            total_list.clear();

                            for(int i=0;i<arrayList.size();i++){
                                total_list.add(arrayList.get(i).replace("\"", ""));
                            }
                            add_soc_adapter.notifyDataSetChanged();
                            //Toast.makeText(v.getContext(),arrayList.get(0),Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                });


            } catch (Exception e) {
                Log.e("error",e.toString());
            }
        }
    };
    @Override
    public void onDestroy() {
        socket.disconnect();

        AccountSettingsFrag.adapter=null;
        AccountSettingsFrag.recyclerView.setAdapter(AccountSettingsFrag.adapter);

        AccountSettingsFrag.adapter=new AccountSettingsAdapter(SocietyAdapter.selected_soc_signup,null,null,"settings",null);
        AccountSettingsFrag.recyclerView.setAdapter(AccountSettingsFrag.adapter);

       // AccountSettingsFrag.adapter.notifyDataSetChanged();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();

        Gson gson=new Gson();
        String json = gson.toJson(SocietyAdapter.selected_soc_signup);

        editor.putString("mySocietyList", json);
        editor.apply();
        /*Intent i=new Intent(v.getContext(),ProfileHolder.class);
        i.putExtra("Open","society_list");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);*/
        super.onDestroy();
    }

}
