package custom_views_and_styles;

public interface DragListener {

    /**
     * Invoked when the view has just started to be dragged.
     */
    void onStartDraggingView();

    /**
     * Invoked when the view has being dragged out of the screen
     * and just before calling activity.finish().
     */
    void onViewCosed();
}