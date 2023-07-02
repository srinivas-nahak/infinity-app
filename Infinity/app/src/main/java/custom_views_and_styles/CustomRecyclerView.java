package custom_views_and_styles;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class CustomRecyclerView extends RecyclerView {

    Context context;

    public CustomRecyclerView(Context context) {
        super(context);
        this.context = context;
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {

        velocityY *= 0.9;
        // velocityX *= 0.7; for Horizontal recycler view. comment velocityY line not require for Horizontal Mode.

        return super.fling(velocityX, velocityY);
    }

}
