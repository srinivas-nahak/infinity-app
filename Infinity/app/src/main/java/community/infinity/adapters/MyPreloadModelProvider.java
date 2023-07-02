package community.infinity.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import needle.Needle;

/**
 * Created by Srinu on 07-03-2018.
 */

public class MyPreloadModelProvider implements ListPreloader.PreloadModelProvider {
    List<TimelineData> myUrls;
    ArrayList<String> bg_links;
    String url;
    Context ctx;
    public MyPreloadModelProvider(List<TimelineData> myUrls,Context ctx) {
        this.ctx=ctx;
        this.myUrls = myUrls;
    }
    public MyPreloadModelProvider(ArrayList<String> bg_links,Context ctx) {
        this.ctx=ctx;
        this.bg_links=bg_links;
    }

    @Override
    @NonNull
    public List<String> getPreloadItems(int position) {
                if(myUrls!=null) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<String>>() {
                    }.getType();
                    ArrayList<String> arrayList = gson.fromJson(myUrls.get(position).getImg_link(), type);
                    for (int i = 0; i < arrayList.size(); i++) {
                        url = arrayList.get(i);
                    }
                }
                else{
                    for (int i = 0; i < bg_links.size(); i++) {
                        url = bg_links.get(i);
                    }

                }

        if (TextUtils.isEmpty(url)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(url);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(Object item) {
        return  Glide.with(ctx)
                .load(url)
                .apply(new RequestOptions().override(600, 600));
    }

}