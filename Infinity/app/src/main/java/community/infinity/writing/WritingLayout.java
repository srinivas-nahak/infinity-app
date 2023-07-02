package community.infinity.writing;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import community.infinity.R;
import community.infinity.writing.WritingDesign;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import needle.Needle;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by Srinu on 10-01-2018.
 */

public class WritingLayout extends Fragment {


    private ImageButton back,next;
    private EditText write,penName;
    private TextView heading;
    private String text,feelings,bookTitle,check;

    View v;
    public WritingLayout() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///Checking Book or only Writing
        check=Hawk.get("bookTitle").toString();
        text=Hawk.get("penName",null);
        feelings=Hawk.get("feelings",null);
        bookTitle=Hawk.get("titleOfBook",null);
        Hawk.init(getActivity().getApplicationContext()).build();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(v==null){
        v=inflater.inflate(R.layout.writing_lay,container,false);
        back=v.findViewById(R.id.backWriting);
        next=v.findViewById(R.id.nextWriting);
        write=v.findViewById(R.id.compose);
        penName=v.findViewById(R.id.penName);
        heading=v.findViewById(R.id.headingComposeTxt);
        final RelativeLayout parentLay=v.findViewById(R.id.writingLayCont);
        DragToClose dragToClose=v.findViewById(R.id.dragViewWritingLayout);

        //Setting Bg
            parentLay.setBackgroundResource(RememberTextStyle.themeResource);

            String s="Artificial Intelligence is not that wise to defeat Human Intelligence so please Be honest to not copy anyone’s content.";
            Snackbar snackbar = Snackbar.make(back, s, Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.parseColor("#43cea2"));
            snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
            snackbar.setAction("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });

            View sbView = snackbar.getView();
            sbView.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.textColor));
            TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.parseColor("#001919"));
            tv.setMaxLines(3);
            snackbar.show();

            //Setting Tint
            //setting tint to btns
            ButtonTint tint=new ButtonTint("white");
            tint.setTint(back);
            tint.setTint(next);


            //PenName Drawable
            penName.setCompoundDrawablesWithIntrinsicBounds( R.drawable.pencil_symbol, 0, 0, 0);
            //Drag to Close view
            dragToClose.setDragListener(new DragListener() {
                @Override
                public void onStartDraggingView() {

                }
                @Override
                public void onViewCosed() {
                    getActivity().finish();
                }
            });


            if(text!=null) {
                penName.setText(text);
            }

            if(check.equals("yes")){
                heading.setText("Book Title");
                write.setHint("Set a Title for your Book...");
                if(bookTitle!=null)
                     write.setText(bookTitle);
                InputFilter[] FilterArray = new InputFilter[1];

                FilterArray[0] = new InputFilter.LengthFilter(100);
                write.setFilters(FilterArray);
                if (write.getText().toString().length() == 100) {
                    Toast.makeText(v.getContext(), "Sorry You Can't Enter More Text...", Toast.LENGTH_SHORT).show();
                }
            }

            if(check.equals("no")) {
                if(feelings!=null)
                   write.setText(feelings);


                InputFilter[] FilterArray = new InputFilter[1];

                FilterArray[0] = new InputFilter.LengthFilter(1200);
                write.setFilters(FilterArray);
                if (write.getText().toString().length() == 1200) {
                    Toast.makeText(v.getContext(), "Sorry You Can't Enter More Text...", Toast.LENGTH_SHORT).show();
                }
            }
            back.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.performClick();
                    }
                }
            });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
            next.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.performClick();
                    }
                }
            });
           next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///closing keyboard
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);

                final String con=write.getText().toString().trim();
                int n=0;

                if(check.equals("no")) n=5;
                else n=1;
                if(con.length()<n){
                    Toast.makeText(v.getContext(),"Too Short Text...",Toast.LENGTH_SHORT).show();
                }
                else if(con.length()>n&&penName.getText().toString().trim().length()==1){
                    Toast.makeText(v.getContext(),"Too Short Pen Name",Toast.LENGTH_SHORT).show();
                }
                else{
                    ///////
                    WritingDesign ic = new WritingDesign();
                    Bundle b=new Bundle();
                    b.putString("fragName","composer");
                    ic.setArguments(b);
                    FragmentManager fmt = getFragmentManager();
                    FragmentTransaction ftt = fmt.beginTransaction();
                    ftt.replace(R.id.profile_holder_frame, ic).commit();
                    getFragmentManager().executePendingTransactions();

                }
            }
        });
        }
        else{}
        return v;
    }

    @Override
    public void onDestroyView() {
        //AppPreferences aPrefs=new AppPreferences(v.getContext());
        Needle.onBackgroundThread().withThreadPoolSize(9).execute(new Runnable() {
            @Override
            public void run() {
                if(Hawk.get("bookTitle").toString().equals("yes")) {
                    Hawk.put("titleOfBook", write.getText().toString().trim());
                }
                else {
                    Hawk.put("feelings", write.getText().toString().trim());
                }
                Hawk.put("penName",penName.getText().toString().trim());
            }
        });

        super.onDestroyView();
    }
}
