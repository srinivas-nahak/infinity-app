package community.infinity.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import community.infinity.R;
import custom_views_and_styles.ReverseInterpolator;

/**
 * Created by Srinu on 11-10-2017.
 */

public class TimeLineSocietyAdapter extends RecyclerView.Adapter<TimeLineSocietyAdapter.TimeLineViewHolder> {

    private ViewGroup parent;
    private ArrayList<String> names;
    private String s;
    private boolean tag_show;
    public static List<FrameLayout> msgContainerList = new ArrayList<>();
    private String selected_soc;
    public TimeLineSocietyAdapter(ArrayList<String>name,String selected_soc,boolean tag_show) {
        this.names=name;
        this.tag_show=tag_show;
        this.selected_soc=selected_soc;
    }

    @Override
    public TimeLineSocietyAdapter.TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent=parent;
        View view=null;
        if(tag_show){
            view=LayoutInflater.from( parent.getContext()).inflate(R.layout.tag_select,parent,false);
            return new TimeLineSocietyAdapter.TimeLineViewHolder(view,true);
        }
        else {
            view = LayoutInflater.from( parent.getContext()).inflate(R.layout.timeline_society_list, parent, false);

            return new TimeLineSocietyAdapter.TimeLineViewHolder(view,false);
        }

    }
    class TimeLineViewHolder extends RecyclerView.ViewHolder{
        TextView delete,tv;
        RelativeLayout container;FrameLayout tagCont, msgContainer;
        public TimeLineViewHolder(View v,boolean tag_show) {
            super(v);
            if(tag_show){
                tv=v.findViewById(R.id.tagText);
                delete=v.findViewById(R.id.tagDelete);
                tagCont=v.findViewById(R.id.tagSelectFrame);
            }
            else {
                tv = v.findViewById(R.id.timeline_soc_names);
                container = v.findViewById(R.id.socListWholeContainer);
                msgContainer=v.findViewById(R.id.msgContainer);
                // tv.setTypeface(EasyFonts.droidSerifRegular( parent.getContext()));
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                float d =  parent.getContext().getResources().getDisplayMetrics().density;
                int margin = (int) (3 * d);
                lp.setMargins(margin, 0, margin, 0);
                container.setLayoutParams(lp);

                //Making msgContainer Clickable
                msgContainer.setClickable(true);
                msgContainer.setFocusable(true);
            }

            }
         //Setting Focus
        }

    @Override
    public void onBindViewHolder(final TimeLineSocietyAdapter.TimeLineViewHolder holder, final int position) {
        try {
            s = names.get(position);
            holder.tv.setText(s);
            if (tag_show) {
                holder.tagCont.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeToast(holder.tagCont, names.get(holder.getAdapterPosition()));
                    }
                });

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Animation rev_an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                        rev_an.setInterpolator(new ReverseInterpolator());
                        holder.itemView.startAnimation(rev_an);
                        if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                            names.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            notifyItemRangeChanged(holder.getAdapterPosition(), names.size());
                        }
                    }
                });
            } else {
                //holder.msgContainer.setSelected(holder.msgContainer.isSelected()?true:false);

                msgContainerList.add(holder.msgContainer);
                if (selected_soc != null)
                    if (selected_soc.equals(names.get(position))) {
                        // holder.msgContainer.setBackgroundResource(R.drawable.followed_btn);
                        holder.msgContainer.setBackgroundResource(R.drawable.textinputborder);
                        holder.tv.setTextColor(Color.parseColor("#001919"));
                    }

            }
        }catch (Exception e){

        }
    }
    public void makeToast(View view,String s){

        int x = view.getLeft();
        int y = view.getTop() + 2*view.getHeight();
        Toast toast = Toast.makeText( parent.getContext(), s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP| Gravity.LEFT, x, y);
        toast.show();
    }
    @Override
    public int getItemCount() {
        return names.size();
    }

}