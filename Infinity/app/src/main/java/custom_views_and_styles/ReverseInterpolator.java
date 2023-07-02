package custom_views_and_styles;

import android.view.animation.Interpolator;

/**
 * Created by Srinu on 16-02-2018.
 */

public class ReverseInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float paramFloat) {
        return Math.abs(paramFloat -1f);
    }
}