package com.ctrlb.draggablelist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A ListView that allows drag an drop functionality for API level 8 and above
 * @author philip brown
 *
 */

public class DraggableListView extends ListView {

    static final int SCROLL_UP = 1;
    static final int SCROLL_DOWN = 2;

    /** whether the list view should respond to drag gestures */
    private boolean mIsDragging = false;
    /** the adapter for the list data */
    private DraggableGenericAdapter mAdapter;
    /**
     * the runnable used to execute the auto scroll on a seperat thread when the
     * top or bottom item in the list is dragged over
     */
    private Runnable mAutoScrollRunnable;
    /** the layout params used to move the floating view */
    private WindowManager.LayoutParams mWindowParams;
    /** the window manager used to move the floating view */
    private WindowManager mWindowManager;
    /**
     * The bitmap used to create the drag view this is a copy of the view in the
     * list being dragged
     */
    private Bitmap mDragBitmap;
    /** The {@link android.view.View} being dragged */
    private View mDragView;
    /** the height of the view being dragged */
    private int mDragViewHeight;

    /** true if the view is being dragged */
    private boolean mDragStarted;
    /** the direction to auto scrol */
    private int mAutoScrollDirection;
    /** the color the background of the floating view will be set to */
    private int mHoverColor = Color.GREEN;
    /**
     * the alpha the floating view will be set to 1.0 = opaque 0.0 = transparent
     */
    private float mHoverAlpha = 0.5f;

    public DraggableListView(Context context) {
	super(context);
    }

    public DraggableListView(Context context, AttributeSet attrs) {
	super(context, attrs);
    }

    public DraggableListView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
    }

    /**
     * The {@link Color} constant used to set the background color of the
     * floating view used when the user drags a list item
     * 
     * @param hoverColor
     *            the {@link Color} constant
     */
    public void setHoverColor(int hoverColor) {
	this.mHoverColor = hoverColor;
    }

    /**
     * The alpha value used to set the transparency of the
     * floating view used when the user drags a list item
     * 
     * @param hoverAlpha
     * the float value for the alpha
     */
    public void setHoverAlpha(float hoverAlpha) {
	this.mHoverAlpha = hoverAlpha;
    }

    /**
     * Sets the adapter
     * 
     * @param adapter
     *            must be type {@link DraggableGenericAdapter}
     * @throws RuntimeException
     *             i the adapter type is not {@link DraggableGenericAdapter}
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
	if (!(adapter instanceof DraggableGenericAdapter))
	    throw new RuntimeException("Trying to set adapter that is not type MovableAdapter on a MovableListView");
	mAdapter = (DraggableGenericAdapter) adapter;
	super.setAdapter(mAdapter);
    }

    /**
     * Set the drag status for the list
     * 
     * @param status
     *            true = items draggable, false = items not draggable
     */
    public void setDragStatus(boolean status) {
	mIsDragging = status;
	mAdapter.setDragStatus(status);
    }

    /**
     * Get the drag status for the list
     * 
     * @return The drag status (true = items draggable, false = items NOT
     *         draggable)
     */
    public boolean getDragStatus() {
	return mIsDragging;
    }

    /**
     * handles the MotionEvent if in dragging mode (mIsDragging == true)
     * otherwise it is passed to superclass
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

	if (mIsDragging) {

	    int y = (int) ev.getY();
	    int x = (int) ev.getX();

	    switch (ev.getAction()) {
	    case MotionEvent.ACTION_DOWN:

		if (x > getWidth() - mAdapter.getDragIconWidth()) {

		    mDragStarted = true;
		    performDrag(ev);
		    mAdapter.setMoveStart(pointToPositionFix(x, y));
		    return true;
		}

		break;
	    case MotionEvent.ACTION_MOVE:
		if (mDragStarted) {
		    performDrag(ev);

		    int position = pointToPositionFix(x, y);
		    mAdapter.move(position);

		    // if at top or bottom of list scroll
		    if (position == getLastVisiblePosition()) {
			autoScroll();
			mAutoScrollDirection = SCROLL_DOWN;
		    } else if (position == getFirstVisiblePosition()) {
			autoScroll();
			mAutoScrollDirection = SCROLL_UP;
		    } else {
			removeCallbacks(mAutoScrollRunnable);
		    }
		    return true;
		}
		break;
	    case MotionEvent.ACTION_UP:
		if (mDragStarted) {
		    // drag has come to an end
		    mAdapter.setMoveEnd();
		    stopDragging();
		    mDragStarted = false;
		    removeCallbacks(mAutoScrollRunnable);
		    return true;
		}
		break;
	    default:
		break;
	    }

	}
	return super.onTouchEvent(ev);
    }

    /**
     * Gets the {@link View} that is being used to display the data at a given
     * position in the underlying data set
     * 
     * @param position
     *            the position in the underlying data set
     * @return the {@link View} displaying the underlying data sets data at the
     *         given position
     */
    public View getViewAtPosition(int position) {
	return getChildAt(position - getFirstVisiblePosition());

    }

    /**
     * Moves the View that floats above the list when the user drags a list
     * item. If the view does not yet exist create the view
     * 
     * @param ev
     *            the {@link MotionEvent} to be used to perform the drag
     *            functionality
     */
    private void performDrag(MotionEvent ev) {

	if (mDragView == null) {

	    // create the drag view

	    int position = pointToPositionFix((int) ev.getX(), (int) ev.getY());

	    if (position != INVALID_POSITION) {

		Context context = getContext();

		if (mWindowManager == null) {
		    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}

		View dragStartView = getViewAtPosition(position);

		mDragViewHeight = dragStartView.getMeasuredHeight();

		Drawable bg = dragStartView.getBackground();

		dragStartView.setBackgroundColor(mHoverColor);
		dragStartView.setDrawingCacheEnabled(true);
		mDragBitmap = Bitmap.createBitmap(dragStartView.getDrawingCache());
		dragStartView.setBackgroundDrawable(bg);

		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;

		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
			| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
			| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
			| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
			| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		mWindowParams.format = PixelFormat.TRANSLUCENT;
		mWindowParams.windowAnimations = 0;

		mWindowParams.alpha = mHoverAlpha;

		ImageView v = new ImageView(context);
		v.setPadding(0, 0, 0, 0);
		v.setImageBitmap(mDragBitmap);

		mWindowParams.x = 0; // - mDragPointX + mXOffset;
		mWindowParams.y = (int) ev.getRawY() - mDragViewHeight / 2;
		mWindowManager.addView(v, mWindowParams);
		mDragView = v;
	    }
	} else {
	    mWindowParams.x = 0;
	    mWindowParams.y = (int) ev.getRawY() - mDragViewHeight / 2;
	    mWindowManager.updateViewLayout(mDragView, mWindowParams);
	}

    }

    /**
     * Removes the View that floats above the list when the user drags a list
     * item and tidies up associated resources
     */
    private void stopDragging() {

	if (mDragView != null) {
	    mDragView.setVisibility(GONE);
	    mWindowManager.removeView(mDragView);
	    mDragView = null;
	}
	if (mDragBitmap != null) {
	    mDragBitmap.recycle();
	    mDragBitmap = null;
	}
    }

    /**
     * Fix so that if the point is actually on the divider it returns the
     * position for the item above
     * 
     * @param x
     *            coordinate
     * @param y
     *            coordinate
     * @return the position in the underlying data set re to the x,y coordinates
     *         on the screen
     */

    private int pointToPositionFix(int x, int y) {

	int position = INVALID_POSITION;

	position = super.pointToPosition(x, y);

	if (position == INVALID_POSITION) {
	    // fix for if y is on divider. return position of row above
	    position = super.pointToPosition(x, y - getDividerHeight());
	}

	return position;

    }

    /**
     * Starts a new thread to scroll the list if the user drags the item to the
     * top or bottom of the list and more items are available
     */

    private void autoScroll() {

	if (mAutoScrollRunnable == null) {

	    mAutoScrollRunnable = new Runnable() {

		public void run() {

		    if (mAutoScrollDirection == SCROLL_DOWN) {

			int lastPos = getLastVisiblePosition();
			int count = getCount();

			smoothScrollToPosition(lastPos + 1);

			if (lastPos == (count - 1)) {
			    // the end
			    mAdapter.move(count - 1);
			} else {
			    mAdapter.move(lastPos + 1);
			    postDelayed(this, 100);
			}
		    } else {
			int firstPos = getFirstVisiblePosition();

			smoothScrollToPosition(firstPos - 1);

			if (firstPos == 0) {
			    // the end
			    mAdapter.move(0);
			} else {
			    mAdapter.move(firstPos - 1);
			    postDelayed(this, 100);
			}
		    }

		}
	    };
	}

	post(mAutoScrollRunnable);
    }

}
