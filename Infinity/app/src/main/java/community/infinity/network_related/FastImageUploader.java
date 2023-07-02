package community.infinity.network_related;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.orhanobut.hawk.Hawk;

import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import community.infinity.CurrentSociety;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FastImageUploader {
private String my_username;
String upload_time;
Activity activity;
private List<MultipartBody.Part> parts = new ArrayList<>();

    public FastImageUploader(Activity activity, List<MultipartBody.Part> part, String username, String time) {
        this.parts=part;
        this.my_username=username;
        this.activity=activity;
        this.upload_time=time;
        Hawk.init(activity.getApplicationContext()).build();
        if(part!=null||part.size()>0) uploadImages();
    }


    void uploadImages() {


        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());


        final JSONObject obji=new JSONObject();
        try {
            obji.put("uploads_username", my_username);

            if(Hawk.get("myFullName")!=null) obji.put("uploads_fullname",Hawk.get("myFullName"));
            else obji.put("uploads_fullname",sh.getString("myFullName",null));

            if(upload_time==null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy(hh-mm-ss) a");
                upload_time = simpleDateFormat.format(new Date());
            }
            obji.put("uploads_time", upload_time);
            obji.put("upload_society_name", CurrentSociety.home_society.toLowerCase());

            if(Hawk.get("myProfilePic")!=null) obji.put("uploads_profPic",Hawk.get("myProfilePic"));
            else obji.put("uploads_profPic",sh.getString("myProfilePic",null));

            if (Hawk.get("bookTitle").toString().equals("yes"))
                obji.put("short_book_content", Hawk.get("html").toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        RequestBody description = createPartFromString(obji.toString());

        FileUploadService service = ServiceGenerator.createService(FileUploadService.class);

        Call<ResponseBody> call = service.uploadMultipleFilesDynamic(description, parts);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload", "success");

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(t.getMessage()!=null) {
                    Log.e("Upload error:", t.getMessage());
                }
            }
        });

    }
    @NonNull
    private static RequestBody createPartFromString(String desc) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM,desc);
    }

}
