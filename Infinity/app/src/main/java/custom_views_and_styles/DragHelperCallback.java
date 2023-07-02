package custom_views_and_styles;

import android.support.v4.widget.ViewDragHelper.Callback;
import android.view.View;

class DragHelperCallback extends Callback {
    private DragToClose dragToClose;
    private View draggableContainer;
    private int lastDraggingState;
    private int topBorderDraggableContainer;

    DragHelperCallback(DragToClose dragToClose, View draggableContainer) {
        this.dragToClose = dragToClose;
        this.draggableContainer = draggableContainer;
        this.lastDraggingState = 0;
    }

    public void onViewDragStateChanged(int state) {
        if (state != this.lastDraggingState) {
            if ((this.lastDraggingState == 1 || this.lastDraggingState == 2) && state == 0 && this.topBorderDraggableContainer == this.dragToClose.getDraggableRange()) {
                this.dragToClose.closeActivity();
            }

            if (state == 1) {
                this.dragToClose.onStartDraggingView();
            }

            this.lastDraggingState = state;
        }
    }

    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        this.topBorderDraggableContainer = top;
        this.dragToClose.changeDragViewViewAlpha();
    }

    public void onViewReleased(View releasedChild, float xVel, float yVel) {
        if (this.topBorderDraggableContainer != 0 && this.topBorderDraggableContainer < this.dragToClose.getDraggableRange()) {
            boolean settleToClosed = false;
            int settleDestY;
            if (yVel > 800.0F) {
                settleToClosed = true;
            } else {
                settleDestY = (int)((float)this.dragToClose.getDraggableRange() * 0.5F);
                if (this.topBorderDraggableContainer > settleDestY) {
                    settleToClosed = true;
                }
            }

            settleDestY = settleToClosed ? this.dragToClose.getDraggableRange() : 0;
            this.dragToClose.smoothScrollToY(settleDestY);
        }
    }

    public int getViewVerticalDragRange(View child) {
        return this.dragToClose.getDraggableRange();
    }

    public boolean tryCaptureView(View child, int pointerId) {
        return child.equals(this.draggableContainer);
    }

    public int clampViewPositionHorizontal(View child, int left, int dx) {
        return child.getLeft();
    }

    public int clampViewPositionVertical(View child, int top, int dy) {
        int topBound = this.dragToClose.getPaddingTop();
        int bottomBound = this.dragToClose.getDraggableRange();
        return Math.min(Math.max(top, topBound), bottomBound);
    }
}
