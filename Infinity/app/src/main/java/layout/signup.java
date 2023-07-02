package layout;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.regex.Pattern;

import community.infinity.network_related.SocketAddress;
import community.infinity.R;
import custom_views_and_styles.ButtonTint;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;


public class signup extends Fragment implements View.OnClickListener {

    private View v;
    private AppCompatImageButton back,proceed;
    private Animation an;
    private TextView existTxtUsername,existTxtEmail;
    private AppCompatEditText fullnameT,usernameT,passT,emailT,confirm_passT;
    private String user_name,user_pass,user_email,full_name;
    private TextInputLayout fullname_lay,username_lay,email_lay,password_lay,confirm_password_lay;
    DatePickerDialog.OnDateSetListener date;
    private Socket socket;
    {
        try{
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    public signup() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("check_username",handleCheckStatusUsername);
        socket.on("check_email",handleCheckStatusEmail);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_signup, container, false);
        setRetainInstance(true);
        back=v.findViewById(R.id.backSignup);
        proceed = v.findViewById(R.id.proceedSignup);
        usernameT = v.findViewById(R.id.usernameSignup);
        fullnameT=v.findViewById(R.id.fullnameSignup);
        passT = v.findViewById(R.id.signup_password);
        emailT = v.findViewById(R.id.emailSignup);
        confirm_passT=v.findViewById(R.id.signup_confirm_password);
        existTxtUsername = v.findViewById(R.id.existTxtUsername);
        existTxtEmail=v.findViewById(R.id.existTxtEmail);
        //TextInput Layout
        username_lay=v.findViewById(R.id.text_input_lay_usernameSignup);
        fullname_lay=v.findViewById(R.id.text_input_lay_fullnameSignup);
        email_lay=v.findViewById(R.id.text_input_lay_emailSignup);
        password_lay=v.findViewById(R.id.text_input_lay_passwordSignup);
        confirm_password_lay=v.findViewById(R.id.text_input_lay_confirm_passwordSignup);

        //Building Hawk
        Hawk.init(v.getContext()).build();

        user_name=usernameT.getText().toString().trim();


        //Disabling Space for username,password
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
        usernameT.setFilters(new InputFilter[] { filter });
        passT.setFilters(new InputFilter[] { filter });
        confirm_passT.setFilters(new InputFilter[] { filter });

        //Setting Tint
        ButtonTint tint=new ButtonTint("white");
        tint.setTint(back);
        tint.setTint(proceed);


        //Registering EditTxts
        usernameT.addTextChangedListener(new signup.MyTextWatcher(usernameT));
        fullnameT.addTextChangedListener(new signup.MyTextWatcher(fullnameT));
        emailT.addTextChangedListener(new signup.MyTextWatcher(emailT));
        passT.addTextChangedListener(new signup.MyTextWatcher(passT));
        confirm_passT.addTextChangedListener(new signup.MyTextWatcher(confirm_passT));


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
                    login f=new login();
                    FragmentManager fm=getFragmentManager();
                    FragmentTransaction ft=fm.beginTransaction();
                    ft.replace(R.id.fragment_change,f);
                    ft.commit();
                    return true;
                }
                return false;
            }
        } );
        return v;
    }

    private void submitForm() {
        if(!validateFullName()) return;
        if (!validateUserName()) return;

        if (!validateEmail())   return;

        if(!validatePass()) return;

        if(!validateConfirmPass()) return;

        //  Toast.makeText(v.getContext(), "Thank You!", Toast.LENGTH_SHORT).show();
    }

    private boolean validateUserName() {
        Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>^*()%!-]");
        if (usernameT.getText().toString().trim().isEmpty()) {
            username_lay.setError("Enter your username");
            requestFocus(usernameT);
            return false;
        }
        else if (regex.matcher(usernameT.getText().toString()).find()) {
            username_lay.setError("Your username can't contain ( [$&+,:;=\\?@#|/'<>^*()%!-] )");
            requestFocus(usernameT);
            return false;
        }
        else if(usernameT.getText().toString().equals("Infinity")||
                usernameT.getText().toString().equals("infinity.admin")||usernameT.getText().toString().equals("Infinity.Admin")){
            usernameT.setError("Sorry but this name is reserved , you can't use it .");
            requestFocus(username_lay);
            return false;
        }
        else {
            username_lay.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validateFullName() {
        Pattern regex = Pattern.compile("[$,:;=\\\\?@#|/'<>^*()%!]");
        if (fullnameT.getText().toString().trim().isEmpty()) {
            fullname_lay.setError("Enter your fullname");
            requestFocus(fullnameT);
            return false;
        }
        else if (regex.matcher(fullnameT.getText().toString()).find()) {
            fullname_lay.setError("Your fullname can't contain ( [$,:;=\\?@#|/'<>^*()%!] )");
            requestFocus(fullnameT);
            return false;
        }
        else if(fullnameT.getText().toString().equals("Infinity")||fullnameT.getText().toString().equals("infinity")||
                fullnameT.getText().toString().equals("Infinity Admin")||fullnameT.getText().toString().equals("infinity admin")){
            fullname_lay.setError("Sorry but this name is reserved , you can't use it .");
            requestFocus(fullname_lay);
            return false;
        }
        else {
            fullname_lay.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateEmail() {
        String emaili = emailT.getText().toString().trim();

        if (emaili.isEmpty() || !isValidEmail(emaili)) {
            email_lay.setError("Enter valid email address");
            requestFocus(emailT);
            return false;
        } else {
            email_lay.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validatePass(){
        if (passT.getText().toString().trim().isEmpty()) {
            password_lay.setError("Enter your current password");
            requestFocus(passT);
            return false;
        }
        else if (passT.getText().toString().length()<6) {
            password_lay.setError("Your password must contain at least 6 charecters");
            requestFocus(passT);
            return false;
        }
        else {
            password_lay.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validateConfirmPass(){
        if (confirm_passT.getText().toString().trim().isEmpty()) {
            confirm_password_lay.setError("Please confirm your password");
            requestFocus(confirm_passT);
            return false;
        }
        else if (!confirm_passT.getText().toString().equals(passT.getText().toString())) {
            confirm_password_lay.setError("It doesn't match with your password");
            requestFocus(confirm_passT);
            return false;
        }
        else {
            confirm_password_lay.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
    @Override
    public void onClick(View view) {
        an= AnimationUtils.loadAnimation(getContext(),R.anim.fade_buttons);
        //Things for signup
        full_name=fullnameT.getText().toString().trim();
        user_name=usernameT.getText().toString().trim();
        user_pass=passT.getText().toString().trim();
        user_email=emailT.getText().toString().trim();

        switch (view.getId()){

            case R.id.backSignup:
                back.startAnimation(an);
                login f=new login();
                FragmentManager fm=getFragmentManager();
                FragmentTransaction ft=fm.beginTransaction();
                ft.replace(R.id.fragment_change,f);
                ft.commit();
                break;
           case R.id.proceedSignup:
               proceed.startAnimation(an);
               ConnectivityManager conMgr =  (ConnectivityManager)v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
               NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
               if (netInfo == null){
                   Toast.makeText(v.getContext(),"You're not connected to internet",Toast.LENGTH_LONG).show();
               }
               else {
                   //Sending Node
                   submitForm();
                   if (!fullnameT.getText().toString().isEmpty() && !usernameT.getText().toString().isEmpty() && !passT.getText().toString().isEmpty() &&
                           !confirm_passT.getText().toString().isEmpty() && !emailT.getText().toString().isEmpty()) {
                       if (!username_lay.isErrorEnabled() && !email_lay.isErrorEnabled() && !password_lay.isErrorEnabled() && !confirm_password_lay.isErrorEnabled()
                               && existTxtUsername.getVisibility() != View.VISIBLE && existTxtEmail.getVisibility() != View.VISIBLE) {
                           JSONObject obj = new JSONObject();
                           try {
                               SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                               obj.put("fullname_signup", full_name);
                               obj.put("username_signup", user_name);
                               obj.put("password_signup", user_pass);
                               obj.put("email_signup", user_email);
                               obj.put("fcmToken_signup",sh.getString("fcmToken",null));
                               socket.emit("data", obj);
                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                           SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                           SharedPreferences.Editor editor = sharedPrefs.edit();

                           editor.putString("username", user_name);
                           editor.putString("fullname",full_name);
                           editor.apply();

                           Needle.onBackgroundThread().execute(new Runnable() {
                               @Override
                               public void run() {
                                   Hawk.put("myUserName",user_name);
                                   Hawk.put("myFullName",full_name);
                               }
                           });
                           ProgressDialog progressDialog=new ProgressDialog(v.getContext());
                           progressDialog.setMessage("Processing");
                           progressDialog.setCancelable(false);
                           progressDialog.show();
                           socket.on("signup_stat", new Emitter.Listener() {
                               @Override
                               public void call(Object... args) {
                                   JSONObject ob=(JSONObject)args[0];
                                           Needle.onMainThread().execute(new Runnable() {
                                               @Override
                                               public void run() {

                                                   try{
                                                       if(ob.getString("status").equals("yes")) {
                                                           progressDialog.dismiss();
                                                           //Fragment Change
                                                           Society g = new Society();
                                                           FragmentManager fmt = getFragmentManager();
                                                           FragmentTransaction ftt = fmt.beginTransaction();
                                                           ftt.replace(R.id.fragment_change, g);
                                                           ftt.commit();
                                                       }
                                                       else {
                                                           progressDialog.dismiss();
                                                           Toast.makeText(v.getContext(), "Please try again", Toast.LENGTH_LONG).show();
                                                       }
                                                       }
                                                   catch (Exception e){
                                                       e.printStackTrace();
                                                   }
                                               }
                                           });

                               }
                           });

                       } else {
                           Toast.makeText(v.getContext(), "Please check your errors", Toast.LENGTH_SHORT).show();
                       }
                   } else {
                       Toast.makeText(v.getContext(), "Please fill the fields", Toast.LENGTH_SHORT).show();
                   }
               }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
         socket.disconnect();
        //socket.disconnect();
    }

    private Emitter.Listener handleCheckStatusUsername=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject ob=(JSONObject)args[0];

                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(ob.getString("status").equals("exists")) existTxtUsername.setVisibility(View.VISIBLE);
                            else existTxtUsername.setVisibility(View.GONE);
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
                case R.id.fullnameSignup:
                    validateFullName();
                    break;
                case R.id.usernameSignup:
                    JSONObject ob=new JSONObject();
                    try {
                        ob.put("check_username",editable.toString());
                        socket.emit("data",ob);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    validateUserName();
                    break;
                case R.id.emailSignup:
                    JSONObject obi=new JSONObject();
                    try {
                        obi.put("check_email",editable.toString());
                        socket.emit("data",obi);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    validateEmail();
                    break;
                case R.id.signup_password:
                    validatePass();
                    break;
                case R.id.signup_confirm_password:
                    validateConfirmPass();
                    break;
            }
        }
    }
}
