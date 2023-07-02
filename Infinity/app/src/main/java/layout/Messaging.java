package layout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import community.infinity.BottomFrags.BottomFrag;
import community.infinity.R;
import community.infinity.RecyclerViewItems.SpeedyLinearLayoutManager;
import community.infinity.network_related.SocketAddress;
import community.infinity.adapters.AccountSettingsAdapter;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;


public class Messaging extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private View v;
    private TextView noChatTxt;
    public static AccountSettingsAdapter adapter;
    public static ArrayList<String> username=new ArrayList<>();
    public static ArrayList<String> lastMsgList=new ArrayList<>();
    private RelativeLayout welcome_cont;
    private static String my_username;
    private FloatingActionButton new_msg_btn;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static Socket socket;
    {
        try{
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    public Messaging() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        socket.on("msgList",handleMessageList);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messaging, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        this.v=v;
        recyclerView=v.findViewById(R.id.totalMsgRecycler);
        swipeRefreshLayout=v.findViewById(R.id.swipe_containerMessaging);
        noChatTxt=v.findViewById(R.id.noChatTxt);
        new_msg_btn=getActivity().findViewById(R.id.add_msg_btn);
        welcome_cont= getActivity().findViewById(R.id.welcomeMsgCont);


        Hawk.init(v.getContext()).build();

        //Loading data with swipeRefreshLay
        swipeRefreshLayout.setOnRefreshListener(this);
       /* try {
            Field f =swipeRefreshLayout.getClass().getDeclaredField("mCircleView");
            f.setAccessible(true);
            ImageView img = (ImageView)f.get(swipeRefreshLayout);
            img.setBackgroundColor(Color.parseColor("#001919"));
            // img.setImageResource(R.drawable.heart_icon);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#ffffff"));*/

        //Clearing previous data
        username.clear();
        lastMsgList.clear();
        adapter=null;

        //Setting RecyclerView
        adapter=new AccountSettingsAdapter(null,username,lastMsgList,"msgList", null);


         SpeedyLinearLayoutManager manager=new SpeedyLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);
         recyclerView.setLayoutManager(manager);
         recyclerView.setAdapter(adapter);


        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        if(Hawk.get("myUsername")!=null)my_username=Hawk.get("myUsername");
        else my_username = sharedPref.getString("username", null);

        //On Click
        new_msg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomFrag f=new BottomFrag();
                Bundle b=new Bundle();
                b.putString("switch","search");
                b.putString("new_message_search","yes");
                f.setArguments(b);
                f.show(getActivity().getFragmentManager(),"fra");
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        welcome_cont.setVisibility(View.GONE);

        socket.disconnect();
        socket.connect();
        ///Getting Chat List
        refresh();
    }
    public static void refresh(){
        JSONObject ob=new JSONObject();
        try {
            ob.put("totMsgUsername",my_username);
            socket.emit("data",ob);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        welcome_cont.setVisibility(View.GONE);
        ///Getting Chat List
        JSONObject ob=new JSONObject();
        try {
            ob.put("totMsgUsername",my_username);
            socket.emit("data",ob);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ConnectivityManager conMgr =  (ConnectivityManager)v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if (netInfo == null){
                    Toast.makeText(v.getContext(),"You're not connected to internet",Toast.LENGTH_LONG).show();
                }
                if(swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(v.getContext(),"Sorry couldn't fetch data . Please try again",Toast.LENGTH_LONG).show();
                }
            }
        },15000);
    }
    private  Emitter.Listener handleMessageList = new Emitter.Listener(){

        @Override
        public void call(final Object... args){
            //  Toast.makeText(v.getContext(),"Hello India",Toast.LENGTH_LONG).show();

            Needle.onMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    JSONArray data = (JSONArray) args[0];
                    if(data==null||data.length()==0) noChatTxt.setVisibility(View.VISIBLE);
                    else noChatTxt.setVisibility(View.GONE);
                    try {
                        // JSONArray username_arr=data.getJSONArray(0);
                        JSONArray last_msg_arr=data.getJSONArray(1);
                        username.clear();
                        lastMsgList.clear();

                        List<JSONObject> jsonList = new ArrayList<JSONObject>();
                        //Toast.makeText(v.getContext(),last_msg_arr.getJSONArray(1).getJSONObject(0)+"",Toast.LENGTH_LONG).show();
                        //Sorting LastMsg Array According to time
                       for (int i = 0; i < last_msg_arr.length(); i++) {
                            jsonList.add(last_msg_arr.getJSONArray(i).getJSONObject(0));
                        }

                        Collections.sort( jsonList, new Comparator<JSONObject>() {

                            public int compare(JSONObject a, JSONObject b) {
                                String valA = null,valB=null;
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                                int compareResult = 0;
                                try {
                                    valA = a.get("msg_time").toString();
                                    valB = b.get("msg_time").toString();

                                    Date arg0Date = simpleDateFormat.parse(valA);
                                    Date arg1Date = simpleDateFormat.parse(valB);

                                    compareResult = arg0Date.compareTo(arg1Date);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    compareResult = valA.compareTo(valB);
                                }

                                return compareResult;
                            }
                        });
                        Collections.reverse(jsonList);



                        for(int i=0;i<jsonList.size();i++) lastMsgList.add(jsonList.get(i).toString());
                        // ArrayList<String> tmp=new ArrayList<>();
                        for(int i=0;i<lastMsgList.size();i++){
                            JSONObject ob=new JSONObject( lastMsgList.get(i) );


                            if(!ob.getString("from").equals(my_username)){
                                username.add(ob.getString("from"));
                            }
                            else{
                                username.add(ob.getString("to"));
                            }
                        }

                        swipeRefreshLayout.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });



        }
    };
    @Override
    public void onDestroyView() {
        socket.disconnect();
        super.onDestroyView();
    }
}
