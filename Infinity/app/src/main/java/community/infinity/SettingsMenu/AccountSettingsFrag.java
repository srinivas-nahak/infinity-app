package community.infinity.SettingsMenu;

import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;


import android.os.Bundle;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import community.infinity.CustomPagerAdapter;
import community.infinity.R;
import community.infinity.network_related.SocketAddress;
import community.infinity.adapters.AccountSettingsAdapter;
import community.infinity.dialog_frag.Dialog;
import community.infinity.writing.RememberTextStyle;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.HeightWrappingViewPager;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.relex.circleindicator.CircleIndicator;
import needle.Needle;


/**
 * Created by Srinu on 10-10-2017.
 */

public class AccountSettingsFrag extends Fragment {


    public static RecyclerView recyclerView;
    public static AccountSettingsAdapter adapter;
    ArrayList<String> settings=new ArrayList<>();
    private TextView edit;
    private static HeightWrappingViewPager viewPager;
    private static CircleIndicator viewPgIndicator;
    private TextView existTxtEmail;
    private static TextView about_us,fullnameAboutUs,designationAboutUs;
    public static TextView heading;
    private ImageButton back_to_settings;
    private ArrayList<String> names=new ArrayList<>();
    private String b;
    private static RelativeLayout nameCont;
    public static ImageButton tick_btn;
    private TextInputLayout inputLayoutName,inputLayoutEmail,inputLayoutCurrentPass,inputLayoutNewPass,inputLayoutConfirmPass;
    public static FrameLayout change_info_container,change_password_container;
    private static FrameLayout about_us_frameLay;
    private FrameLayout loadingView;
    public static EditText fullname,email,bio,current_password,new_password,confirm_password;
    public static boolean close=false;
    private  ProgressDialog pDialog;
    public static String what;
    private ScrollView about_scrollView;
    private View v;
   // private View v;
    static Socket socket;
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
        socket.on("info_change_status",handleIncomingStatus);
        socket.on("password_update_stat",handlePasswordStatus);
        socket.on("society_list",handleSocietyList);
        socket.on("check_email",handleCheckStatusEmail);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.account_settings_frag,container,false);


        //Original Recycler Viw
        recyclerView=v.findViewById(R.id.accsettingsList_recyclerView);
        edit=v.findViewById(R.id.edit_soc_list);
        heading=v.findViewById(R.id.headingSettings);
        existTxtEmail=v.findViewById(R.id.existTxtEmailSettings);
        back_to_settings=v.findViewById(R.id.back_of_acc);
        loadingView=v.findViewById(R.id.loadingViewAccSettFrag);
        about_scrollView=v.findViewById(R.id.about_us_scroll_view);
        about_us=v.findViewById(R.id.about_us_txt);
        about_us_frameLay=v.findViewById(R.id.about_us_frame);
        tick_btn=v.findViewById(R.id.tick_of_acc);
        viewPager=v.findViewById(R.id.multiImgContainerAboutUs);
        viewPgIndicator=v.findViewById(R.id.indicatorAboutUs);
        nameCont=v.findViewById(R.id.nameContViewPg);
        fullnameAboutUs=v.findViewById(R.id.nameAboutUs);
        designationAboutUs=v.findViewById(R.id.designationAboutUs);
        change_info_container=v.findViewById(R.id.change_info_container);
        //change info components
        fullname=v.findViewById(R.id.change_fullname);
        email=v.findViewById(R.id.change_email);
        bio=v.findViewById(R.id.change_bio);
        inputLayoutName=v.findViewById(R.id.input_layout_change_fullname);
        inputLayoutEmail=v.findViewById(R.id.input_layout_change_email);

        //Changing Pass Components
        change_password_container=v.findViewById(R.id.change_password_container);
        current_password=v.findViewById(R.id.current_password);
        new_password=v.findViewById(R.id.new_password);
        confirm_password=v.findViewById(R.id.confirm_password_settings);
        inputLayoutCurrentPass=v.findViewById(R.id.input_layout_change_password_current_pass);
        inputLayoutNewPass=v.findViewById(R.id.input_layout_change_password_new_pass);
        inputLayoutConfirmPass=v.findViewById(R.id.input_layout_change_password_confirm_pass);

        final RelativeLayout parentLay=v.findViewById(R.id.accSettingsMainLay);
        final ScrollView changeInfoScrollView=v.findViewById(R.id.changeInfoScrollView);
        final ScrollView changePassScrollView=v.findViewById(R.id.changePaswordScrollView);

        //DragToClose Lay
        DragToClose dragToClose=v.findViewById(R.id.dragViewAccSettings);

        //settting bg
        parentLay.setBackgroundResource(RememberTextStyle.themeResource);
        changeInfoScrollView.setBackgroundResource(RememberTextStyle.themeResource);
        changePassScrollView.setBackgroundResource(RememberTextStyle.themeResource);
        //about_us_frameLay.setBackgroundResource(RememberTextStyle.themeResource);
        

        //Context
        Hawk.init(v.getContext()).build();


        //Disabling Space for email
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
        email.setFilters(new InputFilter[] { filter });
        current_password.setFilters(new InputFilter[] { filter });
        new_password.setFilters(new InputFilter[] { filter });
        confirm_password.setFilters(new InputFilter[] { filter });

        //Drag to Close view
        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {}

            @Override
            public void onViewCosed() {
                if(close){
                    close=false;
                    AccountSettingsFrag f=new AccountSettingsFrag();
                    FragmentManager fm=getFragmentManager();
                    FragmentTransaction ft=fm.beginTransaction();
                    ft.replace(R.id.profile_holder_frame,f);
                    ft.commit();

                }
                else{
                    getActivity().finish();
                }
            }
        });



        //Registering EditTxts
        fullname.addTextChangedListener(new MyTextWatcher(fullname));
        email.addTextChangedListener(new MyTextWatcher(email));
        current_password.addTextChangedListener(new MyTextWatcher(current_password));
        new_password.addTextChangedListener(new MyTextWatcher(new_password));
        confirm_password.addTextChangedListener(new MyTextWatcher(confirm_password));
        //setting tint to btns
        ButtonTint tint=new ButtonTint("white");
        tint.setTint(back_to_settings);
        tint.setTint(tick_btn);

        //Getting Intent Extra
        b=getActivity().getIntent().getStringExtra("Open");

        //Setting TextLimit for Fullname
        InputFilter[] FilterFullnameArray = new InputFilter[1];

        FilterFullnameArray[0] = new InputFilter.LengthFilter(25);
        fullname.setFilters(FilterFullnameArray);

        //Setting TextLimit for Bio
        InputFilter[] FilterBioArray = new InputFilter[1];

        FilterBioArray[0] = new InputFilter.LengthFilter(500);
        bio.setFilters(FilterBioArray);


        tick_btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.performClick();
                }
            }
        });
        back_to_settings.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.performClick();
                }
            }
        });

       tick_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

                if(what.equals("General")) {
                    submitForm();
                    if(!inputLayoutName.isErrorEnabled()&&!inputLayoutEmail.isErrorEnabled()) {
                        pDialog=new ProgressDialog(getActivity());
                        pDialog.setMessage("Please wait for a while...");
                        pDialog.setCancelable(false);
                        pDialog.show();
                        JSONObject ob = new JSONObject();
                        try {
                            ob.put("change_info_username", Hawk.get("myUserName").toString());
                            ob.put("fullname_settings", fullname.getText().toString());
                            ob.put("new_email", email.getText().toString());
                            ob.put("bio_settings", URLEncoder.encode(bio.getText().toString().trim(), "UTF-8"));
                            socket.emit("data", ob);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Toast.makeText(v.getContext(),"Please check your errors",Toast.LENGTH_SHORT).show();
                    }
                }
                else if(what.equals("Change Password")){
                    submitPasswordForm();
                    if(!inputLayoutCurrentPass.isErrorEnabled()&&!inputLayoutNewPass.isErrorEnabled()&&
                            !inputLayoutConfirmPass.isErrorEnabled()) {

                        pDialog = new ProgressDialog(getActivity());
                        pDialog.setMessage("Please wait for a while...");
                        pDialog.setCancelable(false);
                        pDialog.show();
                        JSONObject ob = new JSONObject();
                        try {
                            ob.put("change_password_username", Hawk.get("myUserName").toString());
                            ob.put("current_password_settings", current_password.getText().toString());
                            ob.put("new_password_settings", new_password.getText().toString());
                            socket.emit("data", ob);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Toast.makeText(v.getContext(),"Please check your errors",Toast.LENGTH_SHORT).show();
                    }
                }
           }
       });
        back_to_settings.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(close){
                            close=false;
                            AccountSettingsFrag f=new AccountSettingsFrag();
                            FragmentManager fm=getFragmentManager();
                            FragmentTransaction ft=fm.beginTransaction();
                            ft.replace(R.id.profile_holder_frame,f);
                            ft.commit();
                        }
                        else{
                        getActivity().finish();
                        }
                    }
                }
        );
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                    if(close){
                    close=false;
                    AccountSettingsFrag f=new AccountSettingsFrag();
                    FragmentManager fm=getFragmentManager();
                    FragmentTransaction ft=fm.beginTransaction();
                    ft.replace(R.id.profile_holder_frame,f);
                    ft.commit();
                }
                else{
                    getActivity().finish();
                }
                    return true;
                }
                return false;
            }
        } );
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
        //checking(v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();
        checking(v);
        if(b.equals("society_list")) {
            Needle.onBackgroundThread().execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject obj = new JSONObject();
                    try {
                        String myUsername;
                        if(Hawk.get("myUsername")!=null) myUsername=Hawk.get("myUsername");
                        else {
                            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(v.getContext());
                            myUsername=sh.getString("username",null);
                        }
                        obj.put("get_society_list_username", myUsername);
                        socket.emit("data", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void checking(View v){


     if(b.equals("account_settings")){
         settings.clear();

         settings.add("General");settings.add("Change Password"); settings.add("Make Account Private");
         settings.add("Block List");settings.add("About Us"); settings.add("Privacy Policy");

         adapter=new AccountSettingsAdapter(settings,null,null,"settings",null);
         recyclerView.setAdapter(adapter);
     }
     if(b.equals("society_list")||b.equals("events")||b.equals("achievements")){

         names.clear();

         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
         final Gson gson = new Gson();
         //String json = sharedPrefs.getString("savedArray", null);
        // Type type = new TypeToken<ArrayList<String>>() {}.getType();
         //names = gson.fromJson(json, type);
         String s = null;
         if(Hawk.get("mySocietyList")!=null) s=Hawk.get("mySocietyList");
         else if(sharedPrefs.contains("mySocietyList"))s= sharedPrefs.getString("mySocietyList",null);
         String[] splits = new String[3];
         if(s!=null) splits =  s.replace("[","").replace("]","").split(",");
         else splits[0]="";


         ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(splits));

         names=new ArrayList<>();
         //if(names!=null)names.clear();
         if(!arrayList.get(0).equals(""))
         for(int i=0;i<arrayList.size();i++) {
             names.add(arrayList.get(i).replace("\"", ""));
         }
         if(!names.contains("null")) {
             Needle.onBackgroundThread().execute(new Runnable() {
                 @Override
                 public void run() {
                     SharedPreferences.Editor editor = sharedPrefs.edit();
                     String json = gson.toJson(names);
                     editor.putString("mySocietyList", json);
                     editor.apply();

                     Hawk.put("mySocietyList",json);
                 }
             });

         }
         else names.clear();


         adapter=new AccountSettingsAdapter(names,null,null,"settings",null);


        //Society List's Menu
         if(b.equals("society_list")) {
             heading.setText("Society List");
             //Making LoadingView visible first then RView
             loadingView.setVisibility(View.VISIBLE);
             recyclerView.setVisibility(View.INVISIBLE);

             new Handler().postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     loadingView.setVisibility(View.GONE);
                     recyclerView.setVisibility(View.VISIBLE);
                 }
             },1000);
             edit.setVisibility(View.VISIBLE);
             edit.setOnClickListener(
                     new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {

                             FragmentTransaction ft = getFragmentManager().beginTransaction();
                             // Create and show the dialog.
                             Dialog newFragment = new Dialog ();
                             newFragment.show(ft, "dialog");

                         }
                     }
             );

             recyclerView.setAdapter(adapter);
             adapter.notifyDataSetChanged();
         }
         else if(b.equals("events")){
             heading.setText("Events");
             recyclerView.setAdapter(adapter);
         }
         else if(b.equals("achievements")){
             heading.setText("Achievements");
             recyclerView.setAdapter(adapter);
         }
         else {
             recyclerView.setAdapter(adapter);
         }
     }
 }


    public static void changeInfo(){
        what="General";
        close=true;
        heading.setText("General");

        //setting existing information
        tick_btn.setVisibility(View.VISIBLE);
        change_info_container.setVisibility(View.VISIBLE);
        if(Hawk.get("myFullName")!=null&&Hawk.get("myBio")!=null&&Hawk.get("myEmail")!=null){
            fullname.setText(Hawk.get("myFullName").toString());
            email.setText(Hawk.get("myEmail").toString());
            try {
                bio.setText(URLDecoder.decode(Hawk.get("myBio").toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else{
            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(fullname.getContext());
            fullname.setText(sh.getString("myFullName",null));
            email.setText(sh.getString("myEmail",null));
            try {
                    bio.setText(URLDecoder.decode(sh.getString("myBio",null), "UTF-8"));
            } catch (Exception e) {
                bio.setText("");
            }
        }
    }
    public static void changePassword(){
        what="Change Password";
        close=true;
        heading.setText("Change Password");
        //setting existing information
        tick_btn.setVisibility(View.VISIBLE);
        change_password_container.setVisibility(View.VISIBLE);
    }
    public static void aboutPage(){
        what="About Us";
        close=true;

        ArrayList<String> img_links=new ArrayList<>();
        img_links.add("http://103.93.17.48:81/infinity_members/srinu.jpg");
        img_links.add("http://103.93.17.48:81/infinity_members/kanauj.jpg");
        img_links.add("http://103.93.17.48:81/infinity_members/nikhil.jpg");

        CustomPagerAdapter adp=new CustomPagerAdapter(viewPager.getContext(),img_links,true);
        viewPager.setAdapter(adp);

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                viewPager.getParent().requestDisallowInterceptTouchEvent(true);
            }
        });

        fullnameAboutUs.setText("Srinivas Nahak");
        designationAboutUs.setText("Founder/CEO/Chief Designer");

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        fullnameAboutUs.setText("Srinivas Nahak");
                        designationAboutUs.setText("Founder/CEO/Chief Designer");
                        break;
                    case 1:
                        fullnameAboutUs.setText("Kanauj Raj Sahu");
                        designationAboutUs.setText("Co-Founder/CFO");
                        break;
                    case 2:
                        fullnameAboutUs.setText("Nikhil Kumar Patra");
                        designationAboutUs.setText("Content Manager/Designer");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPgIndicator.setViewPager(viewPager);
        heading.setText("About Us");
        about_us_frameLay.setVisibility(View.VISIBLE);
        about_us.setText(Html.fromHtml("<p>First of all thanks for taking your valuable time to check the &ldquo;About Us&rdquo; page.&nbsp;</p>\n" +
                "<h4>Our Vision</h4>\n" +
                "<p>We believe that we always crave for our like-minded people so that we can be understood in a better and right way. Maybe that&rsquo;s the reason we love specification in contents according to our interest.</p>\n" +
                "<p>Thus, we designed a platform where you can connect with your like-minded people easily by reaching to the specific society and #tags. We&rsquo;re also trying to help you grow in your field by suggesting&nbsp; you several tips and opportunities.</p>\n" +
                "<p>We also believe that &ldquo;Simplicity is the ultimate sophistication&rdquo; so, we tried to make our ui as simple and as useful as possible.</p>\n" +
                "<p>To enhance your talent and to help you convert your &ldquo;Passion into Profession&rdquo; in near future we&rsquo;re going to act like a bridge between you and the appropriate platforms.</p>\n" +
                "<h4>Our Chief Members</h4>\n" +
                "<p>Our platform could never be made without the hearty and relentless efforts of the following people. By the way these are not the only members there are a lot of other members out of the frames who&rsquo;ve helped it to be built and they&rsquo;ll be mentioned in the coming updates.</p>"));

    }
    public static void showBlockList(){
        close=true;
        heading.setText("Block List");
        ArrayList<String> set=new ArrayList<>();
        adapter=new AccountSettingsAdapter(null,set,null,"block_list",null);
        recyclerView.setAdapter(adapter);
        JSONObject ob=new JSONObject();
        try {
            ob.put("get_total_blocked_blocker",Hawk.get("myUserName"));
            socket.emit("data",ob);
            socket.on("total_block_list", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray arr=(JSONArray)args[0];
                    for(int i=0;i<arr.length();i++){
                        try {
                            JSONObject ob=arr.getJSONObject(i);
                            set.add(ob.getString("blocked_user"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void submitForm() {
        if (!validateName()) {
            return;
        }

        if (!validateEmail()) {
            return;
        }

      //  Toast.makeText(v.getContext(), "Thank You!", Toast.LENGTH_SHORT).show();
    }

    private boolean validateName() {
        Pattern regex = Pattern.compile("[$,:;=\\\\?@#|/'<>^*()%!]");
        String txt_fullname=fullname.getText().toString().trim();
        if (txt_fullname.isEmpty()) {
            inputLayoutName.setError("Enter your full name");
            requestFocus(fullname);
            return false;
        }
        else if (regex.matcher(txt_fullname).find()) {
            inputLayoutName.setError("Your fullname can't contain ( [$,:;=\\?@#|/'<>^*()%!] )");
            requestFocus(fullname);
            return false;
        }
        else if(txt_fullname.equals("Infinity")||txt_fullname.equals("infinity")||
                txt_fullname.equals("Infinity Admin")||txt_fullname.equals("infinity admin")){
            inputLayoutName.setError("Sorry but this name is reserved , you can't use it .");
            requestFocus(fullname);
            return false;
        }
        else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateEmail() {
        String emaili = email.getText().toString().trim();

        if (emaili.isEmpty() || !isValidEmail(emaili)) {
            inputLayoutEmail.setError("Enter valid email address");
        requestFocus(email);
        return false;
    } else {
        inputLayoutEmail.setErrorEnabled(false);
    }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void submitPasswordForm() {
        if (!validateCurrentPass()) {
            return;
        }

        if (!validateNewPass()) {
            return;
        }
        if (!validateConfirmPass()) {
            return;
        }

    }
    private boolean validateCurrentPass(){
        if (current_password.getText().toString().trim().isEmpty()) {
            inputLayoutCurrentPass.setError("Enter your current password");
            requestFocus(current_password);
            return false;
        } else {
            inputLayoutCurrentPass.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validateNewPass(){
        if (new_password.getText().toString().trim().isEmpty()) {
            inputLayoutNewPass.setError("Enter your new password");
            requestFocus(new_password);
            return false;
        }
        else if (new_password.getText().toString().length()<6) {
            inputLayoutNewPass.setError("Your password must contain at least 6 charecters");
            requestFocus(new_password);
            return false;
        }
        else {
            inputLayoutNewPass.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validateConfirmPass(){
        if (confirm_password.getText().toString().trim().isEmpty()) {
            inputLayoutConfirmPass.setError("Please confirm your password");
            requestFocus(confirm_password);
            return false;
        }
        else if (!confirm_password.getText().toString().equals(new_password.getText().toString())) {
            inputLayoutConfirmPass.setError("It doesn't match with your new password");
            requestFocus(confirm_password);
            return false;
        }
        else {
            inputLayoutConfirmPass.setErrorEnabled(false);
        }

        return true;
    }



    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
                            String s=ob.getString("society_list");
                            String[] splits =  s.replace("[","").replace("]","").split(",");
                            ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(splits));

                            names=new ArrayList<>();
                            //if(names!=null)names.clear();

                            for(int i=0;i<arrayList.size();i++) {
                                names.add(arrayList.get(i).replace("\"", ""));
                            }
                            if(!names.contains("null")) {
                                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                Gson gson = new Gson();
                                String json = gson.toJson(names);

                                editor.putString("savedArray", json);
                                editor.apply();
                            }
                            else names.clear();
                            adapter.notifyDataSetChanged();
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
    Emitter.Listener handleIncomingStatus=new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject obj=(JSONObject) args[0];
                    try {
                        if(obj.getString("change_status").equals("yes")){
                            pDialog.dismiss();
                            Toast.makeText(v.getContext(),"Information changed successfully .",Toast.LENGTH_SHORT).show();
                            String new_fullname=fullname.getText().toString();
                            String new_email=email.getText().toString();
                            String new_bio=bio.getText().toString();
                            Needle.onBackgroundThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Hawk.put("myFullName",new_fullname);
                                    Hawk.put("myEmail",new_email);
                                    Hawk.put("myBio",new_bio);
                                }
                            });
                        }
                        else {
                            pDialog.dismiss();
                            Toast.makeText(v.getContext(),"Sorry, some error occured .",Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    Emitter.Listener handlePasswordStatus=new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject obj=(JSONObject) args[0];
                    try {
                        if(obj.getString("status").equals("yes")){
                            pDialog.dismiss();
                            current_password.setText("");
                            new_password.setText("");
                            confirm_password.setText("");
                            inputLayoutCurrentPass.setErrorEnabled(false);
                            inputLayoutNewPass.setErrorEnabled(false);
                            inputLayoutConfirmPass.setErrorEnabled(false);
                            Toast.makeText(v.getContext(),"Password changed successfully .",Toast.LENGTH_SHORT).show();

                        }
                        else if(obj.getString("status").equals("wrong password")){
                            pDialog.dismiss();
                            inputLayoutCurrentPass.setErrorEnabled(true);
                            inputLayoutCurrentPass.setError("Current password is wrong");
                            requestFocus(current_password);
                        }
                        else {
                            pDialog.dismiss();
                            Toast.makeText(v.getContext(),"Sorry, some error occured .",Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener handleCheckStatusEmail=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject ob=(JSONObject)args[0];

            Needle.onMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(ob.getString("status").equals("exists")) existTxtEmail.setVisibility(View.VISIBLE);
                        else existTxtEmail.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
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


    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.change_fullname:
                    validateName();
                    break;
                case R.id.change_email:
                    if(!editable.toString().equals(Hawk.get("myEmail"))) {
                        JSONObject obi = new JSONObject();
                        try {
                            obi.put("check_email", editable.toString());
                            socket.emit("data", obi);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    validateEmail();
                    break;
                case R.id.current_password:
                    validateCurrentPass();
                    break;
                case R.id.new_password:
                    validateNewPass();
                    break;
                case R.id.confirm_password_settings:
                    validateConfirmPass();
                    break;
            }
        }
    }
}

