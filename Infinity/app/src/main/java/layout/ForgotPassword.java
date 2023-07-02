package layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import community.infinity.R;
import community.infinity.network_related.SocketAddress;
import custom_views_and_styles.ButtonTint;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;

public class ForgotPassword extends Fragment {

    private RelativeLayout passCont;
    private AppCompatEditText usernameEditTxt,newPassEditTxt,confirmPassEditTxt;
    private TextInputLayout newPassLay,confirmPassLay;
    private AppCompatImageButton back_btn;
    private TextView fullname;
    private CircleImageView profile_pic;
    private Button check_btn,send_btn,otp_btn,new_pass_btn;
    private ProgressDialog pDialog;
    private String forgot_email;
    private View v;
    private Socket socket;
    {
        try{
            // socket = IO.socket("http://192.168.43.218:8080");
            socket = IO.socket(SocketAddress.address);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }




    public ForgotPassword() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket.on("forgot_data",handleForgotData);
        socket.on("otp_send_stat",handleOtpSendStat);
        socket.on("otp_accept_stat",handleOtpAcceptStat);
        socket.on("forgot_new_pass_update_status",handlePassUpdtStat);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.forgot_pass_lay,container,false);
        usernameEditTxt=v.findViewById(R.id.forgotUsernameEditTxt);
        check_btn=v.findViewById(R.id.check_btnForgotPass);
        back_btn=v.findViewById(R.id.backForgotPass);
        send_btn=v.findViewById(R.id.send_btnForgotPass);
        profile_pic=v.findViewById(R.id.userImgForgotPass);
        fullname=v.findViewById(R.id.userFullNameForgotPass);
        otp_btn=v.findViewById(R.id.otp_btnForgotPass);
        passCont=v.findViewById(R.id.newPassForgotPassCont);
        newPassEditTxt=v.findViewById(R.id.newPassForgotEditTxt);
        confirmPassEditTxt=v.findViewById(R.id.confirmPassForgotEditTxt);
        newPassLay=v.findViewById(R.id.text_input_lay_newPassForgotEditTxt);
        confirmPassLay=v.findViewById(R.id.text_input_lay_confirmPassForgotEditTxt);
        new_pass_btn=v.findViewById(R.id.change_pass_btnForgotPass);


        //Button Tint
        ButtonTint tint=new ButtonTint("white");
        tint.setTint(back_btn);


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
        usernameEditTxt.setFilters(new InputFilter[] { filter });
        newPassEditTxt.setFilters(new InputFilter[] { filter });
        confirmPassEditTxt.setFilters(new InputFilter[] { filter });



        //Registering EditTxts
        newPassEditTxt.addTextChangedListener(new MyTextWatcher(newPassEditTxt));
        confirmPassEditTxt.addTextChangedListener(new MyTextWatcher(confirmPassEditTxt));




        //Setting Typeface and Gradient Color of TxtView
        final Typeface font = Typeface.createFromAsset((v.getContext()).getAssets(), "fonts/"+"Lato-Bold.ttf");
        fullname.setTypeface(font);

        Shader textShader=new LinearGradient(0, 0, 0, 45,
                new int[]{Color.parseColor("#43cea2"),Color.parseColor("#185a9d")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        fullname.getPaint().setShader(textShader);


        ///On Cliks

        check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check_btn.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons));
                if(usernameEditTxt.getText().toString().length()>0) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(usernameEditTxt.getText().toString()).matches()) {
                        JSONObject ob=new JSONObject();
                        try {
                            ob.put("forgot_email",usernameEditTxt.getText().toString());
                            socket.emit("data",ob);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        JSONObject ob=new JSONObject();
                        try {
                            ob.put("forgot_username",usernameEditTxt.getText().toString());
                            socket.emit("data",ob);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_btn.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons));
                pDialog=new ProgressDialog(getActivity());
                pDialog.setMessage("Please wait for a while...");
                pDialog.setCancelable(false);
                pDialog.show();

                JSONObject ob=new JSONObject();
                try {
                    ob.put("send_otp_email",forgot_email);
                    socket.emit("data",ob);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        otp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otp_btn.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons));
               if(!usernameEditTxt.getText().toString().isEmpty()) {
                   JSONObject ob = new JSONObject();
                   try {
                       ob.put("verify_otp_email", forgot_email);
                       ob.put("verify_otp_num", usernameEditTxt.getText().toString());
                       socket.emit("data", ob);
                       pDialog=new ProgressDialog(getActivity());
                       pDialog.setMessage("Verifying...");
                       pDialog.setCancelable(false);
                       pDialog.show();
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
               }
               else{
                   Toast.makeText(v.getContext(),"Please Enter OTP",Toast.LENGTH_SHORT).show();
               }
            }
        });
        new_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_pass_btn.startAnimation(AnimationUtils.loadAnimation(v.getContext(),R.anim.fade_buttons));
                submitForm();
                if(!newPassLay.isErrorEnabled()&&!confirmPassLay.isErrorEnabled()) {
                    JSONObject ob = new JSONObject();
                    try {
                        ob.put("forgot_new_pass_email", forgot_email);
                        ob.put("forgot_new_pass",newPassEditTxt.getText().toString());
                        socket.emit("data", ob);
                        pDialog=new ProgressDialog(getActivity());
                        pDialog.setMessage("Verifying...");
                        pDialog.setCancelable(false);
                        pDialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(v.getContext(),"Please check your errors",Toast.LENGTH_SHORT).show();
                }
            }
        });


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login f=new login();
                FragmentManager fm=getFragmentManager();
                FragmentTransaction ft=fm.beginTransaction();
                ft.replace(R.id.fragment_change,f);
                ft.commit();
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        socket.disconnect();
        socket.connect();
    }
    private void submitForm() {
        if(!validateNewPass()) return;

        if(!validateConfirmPass()) return;

        //  Toast.makeText(v.getContext(), "Thank You!", Toast.LENGTH_SHORT).show();
    }
    private boolean validateNewPass(){
        if (newPassEditTxt.getText().toString().trim().isEmpty()) {
            newPassLay.setError("Enter your current password");
            requestFocus(newPassEditTxt);
            return false;
        }
        else if (newPassEditTxt.getText().toString().length()<6) {
            newPassLay.setError("Your password must contain at least 6 charecters");
            requestFocus(newPassEditTxt);
            return false;
        }
        else {
            newPassLay.setErrorEnabled(false);
        }

        return true;
    }
    private boolean validateConfirmPass(){
        if (confirmPassEditTxt.getText().toString().trim().isEmpty()) {
            confirmPassLay.setError("Please confirm your password");
            requestFocus(confirmPassEditTxt);
            return false;
        }
        else if (!confirmPassEditTxt.getText().toString().equals(newPassEditTxt.getText().toString())) {
            confirmPassLay.setError("It doesn't match with your password");
            requestFocus(confirmPassEditTxt);
            return false;
        }
        else {
            confirmPassLay.setErrorEnabled(false);
        }

        return true;
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private Emitter.Listener handleForgotData=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONArray arr=(JSONArray)args[0];
            if(arr!=null&&arr.length()>0){
                try {
                    JSONObject ob=arr.getJSONObject(0);
                    String profile_pic_link=ob.getString("profile_pic");
                    String fullname_txt=ob.getString("fullname");
                    forgot_email=ob.getString("email");
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            send_btn.setVisibility(View.VISIBLE);
                            Glide
                                    .with(v.getContext())
                                    .asBitmap()
                                    .load("http://" + profile_pic_link)
                                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true).override(250,250))
                                    .into(new SimpleTarget<Bitmap>() {

                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            profile_pic.setImageBitmap(resource);
                                        }
                                    });
                            fullname.setText(fullname_txt);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Needle.onMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        profile_pic.setImageBitmap(null);
                        fullname.setText("");
                        if(otp_btn.getVisibility()==View.VISIBLE) otp_btn.setVisibility(View.GONE);
                        Toast.makeText(v.getContext(),"User not found",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };
  private Emitter.Listener handleOtpSendStat=new Emitter.Listener() {
      @Override
      public void call(Object... args) {
          JSONObject ob=(JSONObject)args[0];
          Needle.onMainThread().execute(new Runnable() {
              @Override
              public void run() {
                  try {
                      if (ob.getString("status").equals("yes")) {

                          pDialog.dismiss();
                          String[] separated = forgot_email.split("@");
                          StringBuilder first_part = new StringBuilder(separated[0]);
                          StringBuilder sec_part = new StringBuilder(separated[1]);
                          for (int i = 0; i < first_part.length(); i++) {
                              if (i == 0 || i == 1 || i == first_part.length() - 1) continue;
                              else {
                                  first_part.setCharAt(i, '*');
                              }
                          }
                          for (int i = 0; i < sec_part.length(); i++) {
                              if (i == 0) continue;
                              else if (sec_part.charAt(i) == '.') break;
                              else {
                                  sec_part.setCharAt(i, '*');
                              }
                          }

                          Toast.makeText(v.getContext(), "OTP has been sent to your email   " + first_part + "@" + sec_part, Toast.LENGTH_LONG).show();
                          send_btn.setVisibility(View.GONE);
                          check_btn.setVisibility(View.GONE);
                          otp_btn.setVisibility(View.VISIBLE);
                          usernameEditTxt.setText("");
                          usernameEditTxt.setHint("Enter OTP");
                      }
                      else if(ob.getString("status").equals("exceeded")){
                          pDialog.dismiss();
                          Toast.makeText(v.getContext(),"Please try after some time",Toast.LENGTH_LONG).show();
                      }

                  } catch (JSONException e)

                  {
                      e.printStackTrace();
                  }
              }
          });
      }
  };
    private Emitter.Listener handleOtpAcceptStat=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject ob=(JSONObject)args[0];
            Needle.onMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(ob.getString("status").equals("yes")){
                           pDialog.dismiss();
                           profile_pic.setVisibility(View.INVISIBLE);
                           usernameEditTxt.setVisibility(View.GONE);
                           fullname.setVisibility(View.GONE);
                           otp_btn.setVisibility(View.GONE);
                           passCont.setVisibility(View.VISIBLE);

                            Toast.makeText(v.getContext(),"OTP verified",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            pDialog.dismiss();
                            Toast.makeText(v.getContext(),"OTP is wrong",Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener handlePassUpdtStat=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject ob=(JSONObject)args[0];
            try {
                if(ob.getString("status").equals("yes")){
                    Needle.onMainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            pDialog.dismiss();
                            Toast.makeText(v.getContext(),"Password Updated Successfully",Toast.LENGTH_SHORT).show();
                            back_btn.callOnClick();
                        }
                    });

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
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

                case R.id.newPassForgotEditTxt:
                    validateNewPass();
                    break;
                case R.id.confirmPassForgotEditTxt:
                    validateConfirmPass();
                    break;
            }
        }
    }
}
