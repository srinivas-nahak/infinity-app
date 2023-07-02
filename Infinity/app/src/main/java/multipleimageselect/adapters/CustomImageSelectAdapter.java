package multipleimageselect.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import java.util.ArrayList;

import community.infinity.R;
import multipleimageselect.models.Image;

/**
 * Created by Darshan on 4/18/2015.
 */
public class CustomImageSelectAdapter extends CustomGenericAdapter<Image> {
    public CustomImageSelectAdapter(Context context, ArrayList<Image> images) {
        super(context, images);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_image_select, null);

            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.image_view_image_select);
            viewHolder.view = convertView.findViewById(R.id.view_alpha);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;

        viewHolder.view.getLayoutParams().width = size;
        viewHolder.view.getLayoutParams().height = size;

        if (arrayList.get(position).isSelected) {
            viewHolder.view.setAlpha(0.5f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                convertView.setForeground(context.getResources().getDrawable(R.drawable.ic_done_white,null));
            }

        } else {
            viewHolder.view.setAlpha(0.0f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                convertView.setForeground(null);
            }
        }

        Glide.with(context)
                .load(arrayList.get(position).path)
                .apply(new RequestOptions().placeholder(R.drawable.image_placeholder))
                .into(viewHolder.imageView);

        return convertView;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public View view;
    }
}
