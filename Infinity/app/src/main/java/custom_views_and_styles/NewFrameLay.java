package custom_views_and_styles;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class NewFrameLay extends FrameLayout {

    public NewFrameLay(Context context) {
        super(context);
    }

    public NewFrameLay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NewFrameLay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Intercept touch event so that inner views cannot receive it.
     *
     * If a ViewGroup contains a RecyclerView and has an OnTouchListener or something like that,
     * touch events will be directly delivered to inner RecyclerView and handled by it. As a result,
     * parent ViewGroup won't receive the touch event any longer.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}
