package custom_views_and_styles;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NewCordiLay extends CoordinatorLayout {
    public NewCordiLay(Context context) {
        super(context);
    }

    public NewCordiLay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NewCordiLay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        // do what you need to with the event, and then...
        return super.dispatchTouchEvent(e);
    }
}
