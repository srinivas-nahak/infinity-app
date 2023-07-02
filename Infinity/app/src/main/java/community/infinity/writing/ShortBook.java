package community.infinity.writing;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;
import com.pixplicity.htmlcompat.HtmlCompat;


import java.util.ArrayList;

import community.infinity.image_related.ImageUpload;
import community.infinity.R;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.DroidWriterEditText;
import needle.Needle;

/**
 * Created by Srinu on 23-01-2018.
 */

public class ShortBook extends Fragment {

    private Button show_short_book;
    private android.widget.ToggleButton bold,italic,underline,strike;
    private ImageButton next,back;
    private DroidWriterEditText droidWriterEditText;
    private AlertDialog dialog;
    private ArrayList<String> img_names=new ArrayList<>();
    public static String upload_time;
    private Animation an;
    TextView cap_vert,caption;
    View v;

    //Text Related
    private String html;
    private float x1,x2;
    private int mCurrentIndex = 0;
    private Pagination mPagination;

    public ShortBook() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (v == null){
        v = inflater.inflate(R.layout.short_book, container, false);
    }
        else{}
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Checking SharedPref
        Hawk.init(getActivity().getApplicationContext()).build();
                html=Hawk.get("bookContent",null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bold = v.findViewById(R.id.boldShortBook);
        italic = v.findViewById(R.id.italicShortBook);
        underline = v.findViewById(R.id.underlineShortBook);
        strike = v.findViewById(R.id.strikeShortBook);
        show_short_book = v.findViewById(R.id.showShortBook);
        droidWriterEditText=v.findViewById(R.id.droidEditTxt);
        next=v.findViewById(R.id.nextShortBook);
        back=v.findViewById(R.id.backShortBook);
        final RelativeLayout  parentLay=v.findViewById(R.id.shortBookCont);
        DragToClose dragToClose=v.findViewById(R.id.dragViewShortBook);

        //Setting Bg
        parentLay.setBackgroundResource(RememberTextStyle.themeResource);
        

        if(getArguments().getStringArrayList("img_names")!=null)img_names=getArguments().getStringArrayList("img_names");
        upload_time=getArguments().getString("upload_time");


        //Removing data from bundle
        getArguments().remove("upload_time");
        getArguments().remove("img_names");



        //Setting Bg of btns
        bold.setBackgroundResource(R.drawable.bold_symbol);
        italic.setBackgroundResource(R.drawable.italic_symbol);
        underline.setBackgroundResource(R.drawable.underline_symbol);
        strike.setBackgroundResource(R.drawable.strike_through);
        show_short_book.setBackgroundResource(R.drawable.eye_symbol);
        
        //Setting Tint
        ButtonTint black_tint=new ButtonTint("white");
        black_tint.setTint(back);
        black_tint.setTint(next);

        //Drag to Close view
        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {}

            @Override
            public void onViewCosed() {
                int index = getActivity().getFragmentManager().getBackStackEntryCount() - 1;
                FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(index);
                getFragmentManager().popBackStack(backEntry.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });



        //Color String
        final String baseColor = "#5e91ec";
        final String white = "#ffffff";

        //Animation
        an = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade_buttons);





        droidWriterEditText.setBoldToggleButton(bold);
        droidWriterEditText.setItalicsToggleButton(italic);
        droidWriterEditText.setUnderlineToggleButton(underline);
        droidWriterEditText.setStrikeThroughToggleButton(strike);


        //On Clik
        bold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                bold.startAnimation(an);
                if (on) {
                    bold.getBackground().setColorFilter(Color.parseColor(baseColor), PorterDuff.Mode.SRC_IN);

                } else {
                    bold.getBackground().setColorFilter(Color.parseColor(white), PorterDuff.Mode.SRC_IN);
                }
            }
        });
        italic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                italic.startAnimation(an);
                if (on) {
                    italic.getBackground().setColorFilter(Color.parseColor(baseColor), PorterDuff.Mode.SRC_IN);

                } else {
                    italic.getBackground().setColorFilter(Color.parseColor(white), PorterDuff.Mode.SRC_IN);
                }
            }
        });
        underline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                underline.startAnimation(an);
                if (on) {
                    underline.getBackground().setColorFilter(Color.parseColor(baseColor), PorterDuff.Mode.SRC_IN);

                } else {
                    underline.getBackground().setColorFilter(Color.parseColor(white), PorterDuff.Mode.SRC_IN);
                }
            }
        });
        strike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                strike.startAnimation(an);
                if (on) {
                    strike.getBackground().setColorFilter(Color.parseColor(baseColor), PorterDuff.Mode.SRC_IN);

                } else {
                    strike.getBackground().setColorFilter(Color.parseColor(white), PorterDuff.Mode.SRC_IN);
                }
            }
        });

        /*bold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bold.startAnimation(an);
                if (bold_bool) {
                    bold_bool = false;
                    ViewCompat.setBackgroundTintList(
                            bold,
                            ColorStateList.valueOf(Color.parseColor(white)));
                    editor.removeFormat();

                } else {
                    bold_bool = true;
                    ViewCompat.setBackgroundTintList(
                            bold,
                            ColorStateList.valueOf(Color.parseColor(baseColor)));
                    editor.setBold();
                }
            }
        });
        italic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                italic.startAnimation(an);
                if (italic_bool) {
                    italic_bool = false;
                    ViewCompat.setBackgroundTintList(
                            italic,
                            ColorStateList.valueOf(Color.parseColor(white)));
                    editor.setItalic();
                } else {
                    italic_bool = true;
                    ViewCompat.setBackgroundTintList(
                            italic,
                            ColorStateList.valueOf(Color.parseColor(baseColor)));
                    editor.setItalic();
                }
            }
        });

        underline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                underline.startAnimation(an);
                if (underline_bool) {
                    underline_bool = false;
                    ViewCompat.setBackgroundTintList(
                            underline,
                            ColorStateList.valueOf(Color.parseColor(white)));
                    editor.setUnderline();
                } else {
                    underline_bool = true;
                    ViewCompat.setBackgroundTintList(
                            underline,
                            ColorStateList.valueOf(Color.parseColor(baseColor)));
                    editor.setUnderline();
                }
            }
        });
        strike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strike.startAnimation(an);
                if (strike_bool) {
                    strike_bool = false;
                    ViewCompat.setBackgroundTintList(
                            strike,
                            ColorStateList.valueOf(Color.parseColor(white)));
                    editor.setStrikeThrough();
                } else {
                    strike_bool = true;
                    ViewCompat.setBackgroundTintList(
                            strike,
                            ColorStateList.valueOf(Color.parseColor(baseColor)));
                    editor.setStrikeThrough();
                }
            }
        });*/

        ////////////////////////////////////////////ShortBook Show

        show_short_book.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onClick(View v) {
                show_short_book.startAnimation(an);
                if (droidWriterEditText.getTextHTML().length() > 3) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(back.getContext());
                    final View vi = LayoutInflater.from(back.getContext()).inflate(R.layout.dialog_image, null);
                    RelativeLayout rl = vi.findViewById(R.id.dialogImgRelLay);
                    //final ScrollView scrollView = vi.findViewById(R.id.dialogScrollView);
                    caption = vi.findViewById(R.id.dialog_text);
                    cap_vert=vi.findViewById(R.id.dialog_text_vertical);
                    final RelativeLayout countBox=vi.findViewById(R.id.pgCounter);
                    final TextView curPg=vi.findViewById(R.id.curPg);
                    final TextView totPg=vi.findViewById(R.id.totPg);
                    final FloatingActionButton close=vi.findViewById(R.id.closeButtonBook);
                    final FloatingActionButton horz_btn=vi.findViewById(R.id.horz_ButtonBook);
                    final FloatingActionButton vert_btn=vi.findViewById(R.id.vert_ButtonBook);
                    final FrameLayout main_cont=vi.findViewById(R.id.bookTxtFrame);
                    final ScrollView scrollView=vi.findViewById(R.id.scrollBook);
                    final RelativeLayout bottomBtn=vi.findViewById(R.id.ll_buttonsBook);
                    main_cont.setVisibility(View.VISIBLE);
                    bottomBtn.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.VISIBLE);
                    caption.setBackgroundResource(R.drawable.circle);
                    scrollView.setBackgroundResource(R.drawable.circle);
                    main_cont.setBackgroundResource(R.drawable.book_bg);
                    Spanned htmlString = HtmlCompat.fromHtml(v.getContext(), droidWriterEditText.getTextHTML(), 0);
                    //Spanned htmlString=Html.fromHtml(droidWriterEditText.getTextHTML());
                    cap_vert.setText(htmlString);





                    //Onclik

                    horz_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            horz_btn.setVisibility(View.INVISIBLE);
                            vert_btn.setVisibility(View.VISIBLE);
                            countBox.setVisibility(View.VISIBLE);
                            caption.setVisibility(View.VISIBLE);
                            main_cont.setVisibility(View.VISIBLE);
                            scrollView.setVisibility(View.INVISIBLE);

                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    countBox.setVisibility(View.INVISIBLE);
                                }
                            }, 2000);

                            caption.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    // Removing layout listener to avoid multiple calls
                                    caption.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    mPagination = new Pagination(htmlString,
                                            caption.getWidth(),
                                            caption.getHeight(),
                                            caption.getPaint(),
                                            caption.getLineSpacingMultiplier(),
                                            caption.getLineSpacingExtra(),
                                            caption.getIncludeFontPadding());
                                    update(caption);
                                    totPg.setText(mPagination.size()+"");
                                    curPg.setText("1");
                                    if(caption.getTextSize()==32.0f) caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, caption.getTextSize()-3.5f);
                                    //  Toast.makeText(caption.getContext(),caption.getTextSize()+"",Toast.LENGTH_SHORT).show();
                                    //caption.setTextSize(TypedValue.COMPLEX_UNIT_PX, caption.getTextSize()-1f);
                                    // Toast.makeText(v.getContext(),mPagination.size()+"",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                    vert_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            horz_btn.setVisibility(View.VISIBLE);
                            vert_btn.setVisibility(View.INVISIBLE);
                            countBox.setVisibility(View.INVISIBLE);
                            caption.setVisibility(View.INVISIBLE);
                            main_cont.setVisibility(View.INVISIBLE);
                            scrollView.setVisibility(View.VISIBLE);



                            caption.setText(htmlString);
                            caption.setMovementMethod(new ScrollingMovementMethod());
                        }
                    });

                  /////////////closeBtn
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });



                    ///On Touch
                    caption.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                                       if(event.getPointerCount() == 1) {
                                           /*countBox.setVisibility(View.VISIBLE);
                                           new Handler().postDelayed(new Runnable() {

                                               @Override
                                               public void run() {
                                                   countBox.setVisibility(View.INVISIBLE);
                                               }
                                           }, 1000);*/
                                           if (event.getAction() == MotionEvent.ACTION_UP) {

                                               x2 = event.getX();
                                               float deltaX = x2 - x1;

                                               if (Math.abs(deltaX) > 150) {
                                                   // Left to Right swipe action
                                                   if (x2 > x1) {
                                                       // mController.previous();
                                                       mCurrentIndex = (mCurrentIndex > 0) ? mCurrentIndex - 1 : 0;
                                                       update(caption);
                                                       //Animation
                                                       Animation an = AnimationUtils.loadAnimation(caption.getContext(), R.anim.page_prev);
                                                       caption.startAnimation(an);
                                                       curPg.setText(mCurrentIndex + 1 + "");
                                                       countBox.setVisibility(View.VISIBLE);
                                                       new Handler().postDelayed(new Runnable() {

                                                           @Override
                                                           public void run() {
                                                               countBox.setVisibility(View.INVISIBLE);
                                                           }
                                                       }, 2000);
                                                       // Toast.makeText(caption.getContext(),mCurrentIndex+1+"",Toast.LENGTH_SHORT).show();
                                                   }

                                                   // Right to left swipe action
                                                   else {
                                                       mCurrentIndex = (mCurrentIndex < mPagination.size() - 1) ? mCurrentIndex + 1 : mPagination.size() - 1;
                                                       update(caption);

                                                       //Animation
                                                       Animation an = AnimationUtils.loadAnimation(caption.getContext(), R.anim.page_prev);
                                                       caption.startAnimation(an);
                                                       curPg.setText(mCurrentIndex + 1 + "");
                                                       countBox.setVisibility(View.VISIBLE);
                                                       new Handler().postDelayed(new Runnable() {

                                                           @Override
                                                           public void run() {
                                                               countBox.setVisibility(View.INVISIBLE);
                                                           }
                                                       }, 2000);
                                                       //Toast.makeText(caption.getContext(),mCurrentIndex+1+"",Toast.LENGTH_SHORT).show();
                                                   }

                                               }


                                           }
                                           if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                               x1 = event.getX();

                                           }
                                           return true;
                                       }


                            return false;
                        }
                    });

                    //Dialog Properties////////
                    builder.setView(vi);
                    dialog = builder.create();
                    dialog.show();


                    dialog.setOnDismissListener(
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ViewGroup v = (ViewGroup) vi.getParent();
                                    v.removeAllViews();
                                }
                            }
                    );
                    dialog.getWindow().setDimAmount(0.9f);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);

                    // dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    rl.setBackground(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    /////////////////////////
                } else
                    Toast.makeText(v.getContext(), "Book is too short to show ...", Toast.LENGTH_SHORT).show();
            }
        });


        //Next and Back
        back.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.performClick();
                }
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
            public void onClick(View view) {
                next.startAnimation(an);
                if (droidWriterEditText.getTextHTML()!=null&&droidWriterEditText.getTextHTML().length() > 500) {
                    Needle.onBackgroundThread().withThreadPoolSize(6).execute(new Runnable() {
                        @Override
                        public void run() {
                            Hawk.put("html", droidWriterEditText.getTextHTML());
                        }
                    });
                    Bundle bundle=new Bundle();
                    bundle.putString("upload_time",upload_time);
                    bundle.putStringArrayList("img_names",img_names);

                    ImageUpload ic = new ImageUpload();
                    ic.setArguments(bundle);
                    FragmentManager fmt = getFragmentManager();
                    FragmentTransaction ftt = fmt.beginTransaction();
                    ftt.replace(R.id.profile_holder_frame, ic);
                    ftt.addToBackStack("bookEditor").commit();
                    getFragmentManager().executePendingTransactions();
                }
                else {
                    Toast.makeText(v.getContext(), "Book is too short to proceed ...", Toast.LENGTH_SHORT).show();
                }

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = getActivity().getFragmentManager().getBackStackEntryCount() - 1;
                FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(index);
                getFragmentManager().popBackStack(backEntry.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private int getDistanceFromEvent(MotionEvent event) {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) (Math.sqrt(dx * dx + dy * dy));
    }
    private void update(TextView mTextView) {
        final CharSequence text = mPagination.get(mCurrentIndex);
        if(text != null) mTextView.setText(text);
    }




    @Override
    public void onDestroyView() {
        Needle.onBackgroundThread().withThreadPoolSize(8).execute(new Runnable() {
            @Override
            public void run() {
                Hawk.put("bookContent",droidWriterEditText.getTextHTML());
            }
        });
        super.onDestroyView();
    }
}
