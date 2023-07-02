package community.infinity.message;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import community.infinity.BottomFrags.BottomFrag;
import community.infinity.ListItem;
import community.infinity.R;
import community.infinity.RecyclerViewItems.RecyclerItemClickListener;
import community.infinity.network_related.SocketAddress;
import community.infinity.activities.ProfileHolder;
import community.infinity.adapters.AccountSettingsAdapter;
import custom_views_and_styles.ButtonTint;
import custom_views_and_styles.DragListener;
import custom_views_and_styles.DragToClose;
import custom_views_and_styles.ReverseInterpolator;
import custom_views_and_styles.RoundedImageView;
import custom_views_and_styles.TextDrawable;
import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import needle.Needle;
import ooo.oxo.library.widget.TouchImageView;

/**
 * Created by Srinu on 17-12-2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private int lastPosition = -1;
    private float x1,x2;
    private ListItem item;
    private ViewGroup parent;
    private AlertDialog dialog;
    private Socket socket;

    {
        try {
            socket = IO.socket(SocketAddress.address);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public MessageAdapter(List<Message> messages) {
        this.mMessages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.timeline_society_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = mMessages.get(holder.getAdapterPosition());
        if(message.getMessage()!=null) {
            holder.setMessage(message.getMessage());
        }



        if(message.getImgLink()!=null) {
            holder.img_container.setVisibility(View.VISIBLE);
            holder.msgPostFullname.setVisibility(View.GONE);
            holder.msgPostProfPic.setVisibility(View.GONE);
            holder.setImage(message.getImgLink());
            //Setting Shape of Image
            holder.mImageView.setCornerRadius(25,message.getWho());

            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(getDp(250),getDp(250));
            params.setMargins(0,0,0,0);
            holder.img_container.setLayoutParams(params);
        }
        if(message.getMsgType().equals("post")){
            holder.img_container.setVisibility(View.VISIBLE);
            holder.mMessageView.setVisibility(View.GONE);
            holder.setPost(message.getMsg_post_img_link(),message.getMsg_post_owner_username());
            //Setting Shape of Image
            holder.mImageView.setCornerRadius(25,message.getWho());

            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(getDp(250),getDp(250));
            params.setMargins(0,0,0,0);
            holder.img_container.setLayoutParams(params);

            holder.msgPostFullname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.msgPostProfPic.callOnClick();
                }
            });
            holder.msgPostProfPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(parent.getContext(),ProfileHolder.class);
                    Bundle b=new Bundle();
                    b.putString("searchUsername",message.getMsg_post_owner_username());
                    i.putExtras(b);
                    i.putExtra("Open","search_profile");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    parent.getContext().startActivity(i);
                }
            });
        }


        if (message.getWho().equals("friend")) {


            //setting msg to left
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            lp.setMargins(getDp(6),getDp(6),getDp(6),getDp(6));

            holder.container.setLayoutParams(lp);
            RelativeLayout.LayoutParams lpi = new RelativeLayout.LayoutParams(getDp(600),ViewGroup.LayoutParams.WRAP_CONTENT);
            lpi.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.wholeCont.setLayoutParams(lpi);

            if(message.getMsgType().equals("text"))holder.container.setBackgroundResource(R.drawable.msg_white_capsule);
            else holder.container.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.transparent));

            holder.mMessageView.setTextSize(17);
            holder.mMessageView.setTextColor(Color.parseColor("#001919"));


            //Animation
            setAnimation(holder.container, holder.getAdapterPosition(), "friend");


            //Long Click
            holder.wholeCont.setOnLongClickListener(view -> {
                //longClickMsg(holder.wholeCont.getContext(),message.getTime(),holder);
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_image, null);
                RelativeLayout mainCont = vi.findViewById(R.id.dialogImgRelLay);
                RecyclerView fontList = vi.findViewById(R.id.fontRecycler);
                TouchImageView img = vi.findViewById(R.id.dialog_image);
                img.setVisibility(View.GONE);
                fontList.setVisibility(View.VISIBLE);

                //Setting Wrap Content
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                mainCont.setLayoutParams(params);
                mainCont.setBackgroundResource(R.drawable.bg_gradient);


                final ArrayList<String> name = new ArrayList<>();
                if(message.getMsgType().equals("text"))name.add("Copy Text");
                else name.add("Download");

                AccountSettingsAdapter adapter = new AccountSettingsAdapter(name, null, null,  "triple_dot", null);
                fontList.setHasFixedSize(true);
                fontList.setLayoutManager(new LinearLayoutManager(parent.getContext()));
                fontList.setAdapter(adapter);

                //Onclick Listener
                fontList.addOnItemTouchListener(new RecyclerItemClickListener(parent.getContext(), fontList, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                       if(name.get(position).equals("Copy Text")){
                            ClipboardManager clipboard = (ClipboardManager) parent.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("message", holder.mMessageView.getText().toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(parent.getContext(),"Text Copied",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                       else if (name.get(position).equals("Download")) {
                           Glide.with(parent.getContext()).asBitmap().load(message.getImgLink()).into(new SimpleTarget<Bitmap>(700,700) {
                               @Override
                               public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                   super.onLoadFailed(errorDrawable);
                                   Toast.makeText(parent.getContext(),"Please try again ",Toast.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                   Needle.onBackgroundThread().execute(new Runnable() {
                                       @Override
                                       public void run() {

                                           Random generator = new Random();
                                           int n = 10000;
                                           n = generator.nextInt(n);
                                           String fname = "Image-" + n + ".jpg";
                                           try {
                                               Needle.onMainThread().execute(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       Toast.makeText(parent.getContext(),"Downloaded Successfully",Toast.LENGTH_SHORT).show();
                                                   }
                                               });
                                               saveImage(fname,resource);
                                           } catch (IOException e) {
                                               e.printStackTrace();
                                           }
                                       }
                                   });

                               }
                           });
                           dialog.dismiss();
                       }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));

                //Dialog Properties////////
                builder.setView(vi);
                dialog = builder.create();
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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
                dialog.getWindow().setDimAmount(0.3f);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                /////////////////////////
                return false;
            });

        } else {

            //item = new ListItem(holder.container, holder.wholeCont, holder.mMessageView, holder.prof_pic, "me",message.getMsgType());

            //Setting the msg container to right
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.setMargins(getDp(6),getDp(6),getDp(6),getDp(6));
            if(message.getMsgType().equals("text")) holder.container.setBackgroundResource(R.drawable.grey_capsule);
            else holder.container.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.transparent));
            holder.mMessageView.setTextColor(Color.parseColor("#001919"));
            holder.mMessageView.setTextSize(17);
            holder.container.setLayoutParams(lp);
            RelativeLayout.LayoutParams lpi = new RelativeLayout.LayoutParams(getDp(600),ViewGroup.LayoutParams.WRAP_CONTENT);
            lpi.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.wholeCont.setLayoutParams(lpi);


            //Animation
            setAnimation(holder.container, holder.getAdapterPosition(), "me");

            //Long Click
            holder.wholeCont.setOnLongClickListener(view -> {
                //longClickMsg(holder.wholeCont.getContext(),message.getTime(),holder);
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_image, null);
                RelativeLayout mainCont = vi.findViewById(R.id.dialogImgRelLay);
                RecyclerView fontList = vi.findViewById(R.id.fontRecycler);
                TouchImageView img = vi.findViewById(R.id.dialog_image);
                img.setVisibility(View.GONE);
                fontList.setVisibility(View.VISIBLE);

                //Setting Wrap Content
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                mainCont.setLayoutParams(params);
                mainCont.setBackgroundResource(R.drawable.bg_gradient);


                final ArrayList<String> name = new ArrayList<>();
                name.add("Unsend");
                if(message.getMsgType().equals("text"))name.add("Copy Text");
                else {
                 if(!message.getMsgType().equals("post"))   name.add("Download");
                }
                name.add("Time");

                AccountSettingsAdapter adapter = new AccountSettingsAdapter(name, null, null,  "triple_dot", null);
                fontList.setHasFixedSize(true);
                fontList.setLayoutManager(new LinearLayoutManager(parent.getContext()));
                fontList.setAdapter(adapter);

                //Onclick Listener
                fontList.addOnItemTouchListener(new RecyclerItemClickListener(parent.getContext(), fontList, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (name.get(position).equals("Unsend")) {
                            dialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                            final View vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.delete_dialog, null);
                            //Dialog Properties////////
                            AppCompatImageButton ok = vi.findViewById(R.id.okDelete);
                            AppCompatImageButton cancel = vi.findViewById(R.id.cancelDelete);


                            //Setting Tint
                            ButtonTint tint = new ButtonTint("white");
                            tint.setTint(ok);
                            tint.setTint(cancel);

                            //Onclick
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Getting Emails
                                    final Animation rev_an = AnimationUtils.loadAnimation(parent.getContext(), R.anim.grow);
                                    rev_an.setInterpolator(new ReverseInterpolator());

                                    int position = holder.getAdapterPosition();

                                    if (position != RecyclerView.NO_POSITION) {
                                        holder.itemView.startAnimation(rev_an);
                                        mMessages.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, mMessages.size());
                                    }

                                    socket.disconnect();
                                    socket.connect();
                                    JSONObject ob = new JSONObject();
                                    try {
                                        Hawk.init(parent.getContext()).build();
                                        ob.put("key_single_del_msgUsername", BottomFrag.roomKey);
                                        ob.put("key_single_del_msgTime", message.getTime());
                                        ob.put("key_single_del_msgType",message.getMsgType());
                                        socket.emit("data", ob);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                    socket.on("single_msg_del_status", new Emitter.Listener() {
                                        @Override
                                        public void call(Object... args) {
                                            JSONObject obj = (JSONObject) args[0];
                                            Needle.onMainThread().execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        if (obj.getString("status").equals("no")) {

                                                            Toast.makeText(parent.getContext(), "Sorry some error occured.Please try again.", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else{
                                                            socket.disconnect();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });


                                        }
                                    });
                                }
                            });
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            builder.setView(vi);
                            dialog = builder.create();
                            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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
                            dialog.getWindow().setDimAmount(0.3f);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        }
                        else if(name.get(position).equals("Time")){
                            holder.timeStamp.setVisibility(View.VISIBLE);
                            holder.timeStamp.setText(message.getTime());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    holder.timeStamp.setVisibility(View.GONE);
                                }
                            }, 1500);
                            dialog.dismiss();
                        }
                        else if(name.get(position).equals("Copy Text")){
                            ClipboardManager clipboard = (ClipboardManager) parent.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("message", holder.mMessageView.getText().toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(parent.getContext(),"Text Copied",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        else if (name.get(position).equals("Download")) {
                            Glide.with(parent.getContext()).asBitmap().load(message.getImgLink()).into(new SimpleTarget<Bitmap>(700,700) {
                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    super.onLoadFailed(errorDrawable);
                                    Toast.makeText(parent.getContext(),"Please try again ",Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    Needle.onBackgroundThread().execute(new Runnable() {
                                        @Override
                                        public void run() {

                                            Random generator = new Random();
                                            int n = 10000;
                                            n = generator.nextInt(n);
                                            String fname = "Image-" + n + ".jpg";
                                            try {
                                                Needle.onMainThread().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(parent.getContext(),"Downloaded Successfully",Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                saveImage(fname,resource);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                }
                            });
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));

                //Dialog Properties////////
                builder.setView(vi);
                dialog = builder.create();
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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
                dialog.getWindow().setDimAmount(0.3f);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                /////////////////////////
                return false;
            });

        }
        holder.wholeCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getMsgType().equals("text")) {
                    holder.timeStamp.setVisibility(View.VISIBLE);
                    holder.timeStamp.setText(message.getTime());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            holder.timeStamp.setVisibility(View.GONE);
                        }
                    }, 1500);
                }
                else if(message.getMsgType().equals("post")){
                    Bundle b = new Bundle();
                    b.putString("what", "show_notification_post");
                    b.putString("post_owner_username", message.getMsg_post_owner_username());
                    b.putString("post_owner_time",message.getMsg_post_owner_time());
                    Intent i = new Intent("profile");
                    i.putExtras(b);
                    i.putExtra("Open", "starred");
                    parent.getContext().startActivity(i);
                }
                else{
                    View dialog_view= LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_image,null);
                    android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(parent.getContext());
                    builder.setView(dialog_view);
                    dialog=builder.create();
                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.getWindow().setDimAmount(0.9f);
                    final TouchImageView img=dialog_view.findViewById(R.id.dialog_image);
                    final DragToClose dragToClose=dialog_view.findViewById(R.id.dragViewDialogImg);
                    Glide.with(parent.getContext()).load(message.getImgLink()).into(img);

                    dialog.show();
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                    dragToClose.setVisibility(View.VISIBLE);
                    dragToClose.openDraggableContainer();
                    //Drag to Close view
                    dragToClose.setDragListener(new DragListener() {
                        @Override
                        public void onStartDraggingView() {

                        }

                        @Override
                        public void onViewCosed() {
                            dialog.dismiss();
                        }
                    });
                    dialog.setOnDismissListener(
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ViewGroup v=(ViewGroup)dialog_view.getParent();
                                    v.removeAllViews();
                                }
                            }
                    );
                }
            }
        });

    }

    void saveImage(String imgName, Bitmap bm) throws IOException {
     //Create Path to save Image
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Infinity"); //Creates app specific folder
        path.mkdirs();
        File imageFile = new File(path, imgName + ".png"); // Imagename.png
        FileOutputStream out = new FileOutputStream(imageFile);
        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
            out.flush();
            out.close();


            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(parent.getContext(), new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @SuppressLint("LogConditional")
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getItemCount() {
        return this.mMessages.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView mImageView;
        private TextView timeStamp,msgPostFullname;
        private TextView mMessageView;
        private FrameLayout container,img_container;
        private RelativeLayout wholeCont;
        private CircleImageView prof_pic,msgPostProfPic;

        @SuppressLint("NewApi")
        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.msgImg);
            mMessageView = itemView.findViewById(R.id.timeline_soc_names);
            container = itemView.findViewById(R.id.msgContainer);
            wholeCont = itemView.findViewById(R.id.socListContainer);
            prof_pic = itemView.findViewById(R.id.userImageMsg);
            timeStamp=itemView.findViewById(R.id.timeStampMsg);
            //seenTxt=itemView.findViewById(R.id.seenTxtMsg);
            img_container=itemView.findViewById(R.id.msgImgHolder);
            msgPostFullname=itemView.findViewById(R.id.msgPostFullname);
            msgPostProfPic=itemView.findViewById(R.id.msgPostProfPic);

            final Typeface bold_font = Typeface.createFromAsset((msgPostFullname.getContext()).getAssets(), "fonts/"+"Lato-Bold.ttf");
            msgPostFullname.setTypeface(bold_font);

            Shader textShader=new LinearGradient(0, 0, 0, 45,
                    new int[]{Color.parseColor("#43cea2"),Color.parseColor("#185a9d")},
                    new float[]{0, 1}, Shader.TileMode.CLAMP);
            msgPostFullname.getPaint().setShader(textShader);

            //alignment
            mMessageView.setGravity(Gravity.START);
            mMessageView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);

            //seenTxt.setVisibility(View.VISIBLE);
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            if (null == message) return;
            try {
                mMessageView.setText(URLDecoder.decode(message, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public void setImage(String url) {
            if (null == mImageView) return;
            if (null == url) return;
            Glide.with(mImageView.getContext()).asBitmap().load(url).
                    apply(new RequestOptions().
                            placeholder(new ColorDrawable(Color.parseColor("#20ffffff"))))
                    .into(mImageView);
        }
         void setPost(String imgLink,String username){
             Glide.with(mImageView.getContext()).load(imgLink).
             apply(new RequestOptions().override(250,250).
                     placeholder(new ColorDrawable(Color.parseColor("#20ffffff"))))
                     .into(mImageView);
             //Setting ProfilePic of Comment
             item=new ListItem(username,msgPostProfPic);

             //Setting Fullname of Comment
             item=new ListItem(username,null,msgPostFullname,"get_full_name",null,null,null,msgPostProfPic.getContext());



        }
    }
    private void setAnimation(View viewToAnimate, int position, String user) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (user.equals("me")) {
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(parent.getContext(), R.anim.slide_up);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        } else {
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(parent.getContext(), R.anim.slide_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

    }
    private int getDp(int px){
        float d = parent.getContext().getResources().getDisplayMetrics().density;
        return (int) (px * d);
    }

}

