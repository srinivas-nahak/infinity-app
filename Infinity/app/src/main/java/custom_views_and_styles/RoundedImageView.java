package custom_views_and_styles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class RoundedImageView extends ImageView {

    private Path mMaskPath;
    private Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String msgUser="all";
    private int mCornerRadius = 10;

    public RoundedImageView(Context context) {
        super(context);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        init();
    }

    public RoundedImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        init();
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        init();
    }

    private void init() {
        //ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        ViewCompat.setLayerPaint(this, null);
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }


    public void setCornerRadius(int cornerRadius,String msgUser) {
        mCornerRadius = cornerRadius;
        this.msgUser=msgUser;
        generateMaskPath(getWidth(), getHeight(),msgUser);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        if (w != oldW || h != oldH) {
            generateMaskPath(w, h,msgUser);
        }
    }

    private void generateMaskPath(int w, int h,String msgUser) {
        mMaskPath = new Path();
        float[] radii = new float[8];
        if(msgUser.equals("me")){
            radii[0]=getDp(25);
            radii[1] = getDp(25);
            radii[2] = getDp(25);
            radii[3] = getDp(25);
            radii[6] = getDp(25);
            radii[7] = getDp(25);
        } else if (msgUser.equals("friend")) {
            radii[1] = getDp(25);
            radii[2] = getDp(25);
            radii[3] = getDp(25);
            radii[4] = getDp(25);
            radii[5] = getDp(25);
            radii[6] = getDp(25);
            radii[7] = getDp(25);
        } else {
            radii[0] = getDp(10);
            radii[1] = getDp(10);
            radii[2] = getDp(10);
            radii[3] = getDp(10);
            radii[4] = getDp(10);
            radii[5] = getDp(10);
            radii[6] = getDp(10);
            radii[7] = getDp(10);
        }
        mMaskPath.addRoundRect(new RectF(0,0,w,h), radii, Path.Direction.CW);
        mMaskPath.setFillType(Path.FillType.INVERSE_WINDING);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onDraw(Canvas canvas) {
        if(canvas.isOpaque()) { // If canvas is opaque, make it transparent
            canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), 255, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
        }

        super.onDraw(canvas);

        if(mMaskPath != null) {
            canvas.drawPath(mMaskPath, mMaskPaint);
        }
    }
    private int getDp(int px){
        float d = getResources().getDisplayMetrics().density;
        int dp= (int) (px * d);
        return dp;
    }
}