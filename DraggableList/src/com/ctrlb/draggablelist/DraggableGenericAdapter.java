package com.ctrlb.draggablelist;

import com.ctrlb.draggablelist.R;
import android.content.Context;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * An adapter used by {@link DraggableListView} to allow drag and drop
 * @author philip brown
 *
 */
public class DraggableGenericAdapter extends GenericAdapter {

    private int mHoverPosition = -1;
    private boolean mIsDragable = false;
    private BitmapDrawable mPlaceHolderBitmap;
    private BitmapDrawable mDragButtonBitmap;

    /**
     * Constructor
     * 
     * @param context
     * @param dataProvider
     * @param viewResourceId
     *            the layout resource for the list item
     * @param from
     *            the keys in the data set to get the data from
     * @param to
     *            ids in the layout to bind the data to
     * @param placeHolderDrawableResorce
     *            the resource id for the image to be used as the placeholder.
     *            this will be tiled
     * @param dragIconDrawableResource
     *            the resource id for the image to be used as the button
     */

    public DraggableGenericAdapter(Context context, MoveableDataProvider dataProvider, int viewResourceId,
	    String[] from, int[] to, int placeHolderDrawableResorce, int dragIconDrawableResource) {
	super(context, dataProvider, viewResourceId, from, to);

	mPlaceHolderBitmap = (BitmapDrawable) mContext.getResources().getDrawable(placeHolderDrawableResorce);
	mPlaceHolderBitmap.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
	mDragButtonBitmap = (BitmapDrawable) mContext.getResources().getDrawable(dragIconDrawableResource);

    }

    /**
     * should be called by the {@link DraggableListView} NOT the user to start
     * dragging a list item
     * 
     * @param position
     */

    public void setMoveStart(int position) {
	mHoverPosition = position;
	notifyDataSetChanged();
    }

    /**
     * should be called by the {@link DraggableListView} NOT the user to stop
     * dragging a list item
     */
    public void setMoveEnd() {
	mHoverPosition = -1;
	notifyDataSetChanged();
    }

    /**
     * This should be called by the {@link DraggableListView} NOT the user to
     * show that the {@link DraggableListView} is in drag mode when the drag
     * images will be shown
     * 
     * @param isDragable
     *            if true show the drag button image
     */
    public void setDragStatus(boolean isDragable) {
	mIsDragable = isDragable;
	notifyDataSetChanged();
    }

    /**
     * Get the width of the image that is being shown as the drag button image
     * 
     * @return
     */
    public int getDragIconWidth() {
	return mDragButtonBitmap.getIntrinsicWidth();
    }

    /**
     * update where the item being dragged has moved to
     * 
     * @param position
     */
    public void move(int position) {

	if (position == ListView.INVALID_POSITION)
	    return;

	// check if the last known position is != to the current position the
	// item has been moved
	// move the data in the data set to reflect this
	if (mHoverPosition > position) {
	    // move down

	    while (mHoverPosition > position) {
		((MoveableDataProvider) mDataProvider).move(mHoverPosition, mHoverPosition - 1);
		mHoverPosition--;
	    }

	} else {
	    while (mHoverPosition < position) {
		((MoveableDataProvider) mDataProvider).move(mHoverPosition, mHoverPosition + 1);
		mHoverPosition++;
	    }
	}

	notifyDataSetChanged();

    }

    /**
     * Contains the logic for rendering the correct view. The view is rendered
     * with of without drag button images or the View substituted with
     * placeholder view if the view is being dragged over
     * 
     */
    @Override
    protected View modifyView(int position, View v, boolean isConverted) {

	// if its a newly created view add the extra views required to allow
	// dragging to work
	if (!isConverted) {
	    btnView((ViewGroup) v);
	    addPlaceholderView((ViewGroup) v);
	}

	RelativeLayout buttonView = (RelativeLayout) v.findViewById(R.id.ma_button_overlay);

	if (mIsDragable) {
	    // has been set to allow drag interactions so show the drag button
	    buttonView.bringToFront();
	    buttonView.setVisibility(View.VISIBLE);
	} else {
	    buttonView.setVisibility(View.GONE);
	}

	RelativeLayout placeHolderView = (RelativeLayout) v.findViewById(R.id.ma_placeholder_overlay);

	if (position == mHoverPosition) {
	    // if the item being dragged by the user is over this view show the
	    // placeholder view
	    RelativeLayout.LayoutParams hoverLayoutPrams = (RelativeLayout.LayoutParams) placeHolderView
		    .getLayoutParams();
	    View parent = (View) placeHolderView.getParent();
	    hoverLayoutPrams.height = parent.getHeight();
	    placeHolderView.setLayoutParams(hoverLayoutPrams);
	    placeHolderView.bringToFront();
	    placeHolderView.setVisibility(View.VISIBLE);

	} else {
	    // if the item being dragged by the user is NOT over this view hide
	    // the placeholder view
	    placeHolderView.setVisibility(View.GONE);
	}

	return v;
    }

    private void addPlaceholderView(ViewGroup vg) {

	RelativeLayout rl = new RelativeLayout(mContext);
	rl.setId(R.id.ma_placeholder_overlay);

	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		LayoutParams.MATCH_PARENT);
	rl.setLayoutParams(params);
	rl.setId(R.id.ma_placeholder_overlay);
	rl.setBackgroundDrawable(mPlaceHolderBitmap);
	vg.addView(rl);
    }

    private void btnView(ViewGroup vg) {

	RelativeLayout rl = new RelativeLayout(mContext);
	rl.setId(R.id.ma_button_overlay);

	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
		LayoutParams.WRAP_CONTENT);
	rl.setLayoutParams(params);

	ImageView iv = new ImageView(mContext);
	iv.setImageDrawable(mDragButtonBitmap);

	RelativeLayout.LayoutParams ivLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		LayoutParams.WRAP_CONTENT);
	ivLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	rl.addView(iv, ivLayoutParams);

	vg.addView(rl, params);

    }

}
