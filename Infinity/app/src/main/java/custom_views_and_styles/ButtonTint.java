package custom_views_and_styles;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by Srinu on 17-02-2018.
 */

public class ButtonTint {
    String tint;
    public ButtonTint( String tint) {
      this.tint=tint;
    }
    public void setTint(View button){
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    if(tint.equals("white")) v.setBackgroundColor(Color.parseColor("#20ffffff"));
                    else v.setBackgroundColor(Color.parseColor("#20001919"));
                }

                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                {
                    /*if(tint.equals("white")) v.setBackgroundColor(Color.parseColor("#001919"));
                    if(tint.equals("grey")) v.setBackgroundColor(Color.parseColor("#efeeee"));
                    if (tint.equals("dialog_tint")) v.setBackgroundColor(Color.TRANSPARENT);
                    if (tint.equals("black"))v.setBackgroundColor(Color.parseColor("#ffffff"));*/
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });
    }
}
