package community.infinity.Reports;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import community.infinity.R;
import community.infinity.SettingsMenu.AccountSettingsFrag;
import community.infinity.adapters.AccountSettingsAdapter;
import community.infinity.network_related.SocketAddress;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

public class Reports extends Fragment {
    private ImageButton back,refresh_btn;
    private TextView heading;
    private RecyclerView rView;
    private AccountSettingsAdapter adapter;
    private ArrayList<String> report_type=new ArrayList<>();
    private ArrayList<String> reporter_name=new ArrayList<>();
    private ArrayList<String> report_of=new ArrayList<>();
    private ArrayList<String> time=new ArrayList<>();
    private ArrayList<String> comnt_post_owner_name=new ArrayList<>();
    private ArrayList<String> comnt_post_owner_time=new ArrayList<>();
    private FrameLayout noReportTxtCont;
    private View v;
    private TextView noReportTxt;
    private static Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("reports",handleReports);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.account_settings_frag,container,false);
        heading=v.findViewById(R.id.headingSettings);
        back=v.findViewById(R.id.back_of_acc);
        refresh_btn=v.findViewById(R.id.refresh_of_settings);
        rView=v.findViewById(R.id.accsettingsList_recyclerView);
        noReportTxtCont=v.findViewById(R.id.loadingViewAccSettFrag);
        noReportTxt=v.findViewById(R.id.loading_txt_settings);
        //DragToClose Lay
        DragToClose dragToClose=v.findViewById(R.id.dragViewAccSettings);

        //Getting Context
        v.getContext();

        //setting tint to btns
        ButtonTint tint=new ButtonTint("white");
        tint.setTint(back);
        tint.setTint(refresh_btn);

        //Setting Heading
        heading.setText("Reports");

        //Making Refresh Btn Visible
        refresh_btn.setVisibility(View.VISIBLE);


        //Recyclerview
        adapter=new AccountSettingsAdapter(report_type,reporter_name,report_of,time,comnt_post_owner_name,comnt_post_owner_time,"reports",getFragmentManager());
        rView.setLayoutManager(new LinearLayoutManager(v.getContext(),LinearLayoutManager.VERTICAL,false));
        rView.setAdapter(adapter);



        //Drag to Close view
        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {}

            @Override
            public void onViewCosed() {
                    getActivity().finish();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadReports();
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();
        reloadReports();
    }
    public static void reloadReports(){
        JSONObject ob=new JSONObject();
        try {
            ob.put("get_reports","yes");
            socket.emit("data",ob);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private Emitter.Listener handleReports=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Needle.onMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    report_type.clear();
                    reporter_name.clear();
                    report_of.clear();
                    time.clear();
                    JSONArray data = (JSONArray) args[0];
                    if(data==null||data.length()==0) {
                        noReportTxtCont.setVisibility(View.VISIBLE);
                        noReportTxt.setText("No Reports Found");
                    }
                    for(int i=0;i<data.length();i++){
                        try {
                            JSONObject ob=data.getJSONObject(i);
                            if(ob.getString("report_type").equals("comments")){
                                report_type.add(ob.getString("report_type"));
                                reporter_name.add(ob.getString("reporter_name"));
                                report_of.add(ob.getString("report_comment_owner_name"));
                                time.add(ob.getString("report_comment_time"));
                                comnt_post_owner_name.add(ob.getString("report_comment_post_owner"));
                                comnt_post_owner_time.add(ob.getString("report_comment_owner_post_time"));
                            }
                            else if(ob.getString("report_type").equals("profile")){
                                report_type.add(ob.getString("report_type"));
                                reporter_name.add(ob.getString("reporter_name"));
                                report_of.add(ob.getString("report_profile_owner"));
                                time.add("");
                            }
                            else{
                                report_type.add(ob.getString("report_type"));
                                reporter_name.add(ob.getString("reporter_name"));
                                report_of.add(ob.getString("report_post_owner_name"));
                                time.add(ob.getString("report_post_time"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    adapter.notifyDataSetChanged();

                }
            });
        }
    };
}
