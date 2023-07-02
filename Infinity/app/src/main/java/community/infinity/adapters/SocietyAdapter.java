package community.infinity.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import community.infinity.R;
import custom_views_and_styles.ButtonTint;

/**
 * Created by Srinu on 15-08-2017.
 */

public class SocietyAdapter extends RecyclerView.Adapter<SocietyAdapter.SocViewHolder> {
    private ArrayList<String> names,selected_soc;
    private String s;
    private ViewGroup parent;
    private int numberOfCheckboxesChecked = 0;
    private Gson gson = new Gson();
    private String activity_name;
    public static ArrayList<String> selected_soc_signup=new ArrayList<>();
    public SocietyAdapter(ArrayList<String>name,String activity_name) {
        this.names=name;
        this.activity_name=activity_name;
    }
    public SocietyAdapter(ArrayList<String>name,ArrayList<String> selected_soc,String activity_name) {
        this.names=name;
        this.selected_soc=selected_soc;
        this.activity_name=activity_name;
        if(activity_name.equals("settings")){
            selected_soc_signup=selected_soc;
            numberOfCheckboxesChecked=selected_soc.size();
        }
    }
    @Override
    public SocViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent=parent;
        View view= LayoutInflater.from( parent.getContext()).inflate(R.layout.list_of_society,parent,false);
        return new SocViewHolder(view);
    }
 class SocViewHolder extends RecyclerView.ViewHolder{
     TextView tv;AppCompatCheckBox chk;RelativeLayout  mainCont;
     public SocViewHolder(View v) {
         super(v);
         tv=v.findViewById(R.id.societyName);
         chk=v.findViewById(R.id.checkBoxSociety);
         mainCont=v.findViewById(R.id.soc_list_cont);

         ButtonTint tint=new ButtonTint("white");
         tint.setTint(mainCont);
         final Typeface reg_font = Typeface.createFromAsset(( parent.getContext()).getAssets(), "fonts/"+"Lato-Regular.ttf");
         tv.setTypeface(reg_font);
     }
 }
    @Override
    public void onBindViewHolder(final SocViewHolder holder, final int position) {
        try {
            s = names.get(position);
            holder.tv.setText(s);
            holder.mainCont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.chk.isChecked()) holder.chk.setChecked(false);
                    else holder.chk.setChecked(true);
                }
            });
            if (selected_soc != null) {
                if (selected_soc.size() > 0) {
                    if (selected_soc.contains(names.get(position))) holder.chk.setChecked(true);
                }
            }
            //Getting Checked Society Names
            holder.chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (activity_name.equals("settings")) {
                        if (isChecked && numberOfCheckboxesChecked >= 3) {
                            Toast.makeText(parent.getContext(), "You can select only three societies at once.", Toast.LENGTH_SHORT).show();
                            holder.chk.setChecked(false);
                        } else {

                            if (isChecked) {
                                numberOfCheckboxesChecked++;
                                selected_soc_signup.add(names.get(position));
                                //arrayList.add(names.get(position));
                            } else {
                                numberOfCheckboxesChecked--;
                                int rem_pos = selected_soc_signup.indexOf(names.get(position));
                                selected_soc_signup.remove(rem_pos);
                            }
                        }
                    } else {
                        if (isChecked && numberOfCheckboxesChecked >= 3) {
                            Toast.makeText(parent.getContext(), "You can select only three societies at once.", Toast.LENGTH_LONG).show();
                            holder.chk.setChecked(false);
                        } else {

                            if (isChecked) {
                                numberOfCheckboxesChecked++;
                                selected_soc_signup.add(names.get(position));
                            } else {
                                numberOfCheckboxesChecked--;
                                int rem_pos = selected_soc_signup.indexOf(names.get(position));
                                selected_soc_signup.remove(rem_pos);
                            }


                        }
                    }
                }
            });
        }catch (Exception e){}

    }

    @Override
    public int getItemCount() {
        return names.size();
    }

}
