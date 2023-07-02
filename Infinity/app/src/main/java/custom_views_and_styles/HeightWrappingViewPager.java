package custom_views_and_styles;

/**
 * Created by Srinu on 30-12-2017.
 */

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class HeightWrappingViewPager extends ViewPager {
    private int mMaxHeight=0;
    public HeightWrappingViewPager(Context context) {
        super(context);
    }

    public HeightWrappingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if (h > mMaxHeight) mMaxHeight = h;
        }

        if (mMaxHeight != 0) heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        // do what you need to with the event, and then...
        return super.dispatchTouchEvent(e);
    }
}
