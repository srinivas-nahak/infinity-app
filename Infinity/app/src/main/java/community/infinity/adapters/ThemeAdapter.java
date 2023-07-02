package community.infinity.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import community.infinity.R;
import community.infinity.writing.RememberTextStyle;
import de.hdodenhof.circleimageview.CircleImageView;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder> {
    ViewGroup parent;
    public static List<ImageView> imageViewList=new ArrayList<>();

    public ThemeAdapter() {
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent=parent;
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.design_of_theme_recycler, parent, false);
        return new ThemeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
       //Storing Tick Imgview in list for checking it's visibilty
       imageViewList.add(holder.tick);

       String s="theme"+(position+1);
       int i=parent.getContext().getResources().getIdentifier(s, "drawable", parent.getContext().getPackageName());
       if(i== RememberTextStyle.themeResource) holder.tick.setVisibility(View.VISIBLE);

       Glide.with(parent.getContext())
               .asBitmap()
               .thumbnail(0.1f)
               .load(i)
               .apply(new RequestOptions()
               .diskCacheStrategy(DiskCacheStrategy.RESOURCE)).into(holder.theme);
    }

    @Override
    public int getItemCount() {
        return 18;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ThemeViewHolder extends RecyclerView.ViewHolder{
        CircleImageView theme;
        ImageView tick;
        public ThemeViewHolder(View itemView) {
            super(itemView);
            theme=itemView.findViewById(R.id.theme_img);
            tick=itemView.findViewById(R.id.tickTheme);

        }
    }
}
