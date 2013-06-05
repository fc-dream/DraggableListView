package com.ctrlb.draggablelist;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * An adapter that allows data for be bound to a {@link ListView} using keys for
 * the data provided by the {@link GenericDataProvider}
 * 
 * @author philip brown
 * 
 */
public class GenericAdapter extends BaseAdapter {

    protected Context mContext;
    protected GenericDataProvider mDataProvider;
    private int mViewResourceId;
    private String[] mFrom;
    private int[] mTo;
    private final LayoutInflater mInflater;
    private ViewBinder mViewBinder;

    /**
     * Constructor
     * 
     * @param context
     *            the context
     * @param dataProvider
     *            the data for the list
     * @param viewResourceId
     *            the view resource for the list row
     * @param from
     *            an array of keys in the data provided by the
     *            {@link GenericDataProvider} used to bind to the rows views
     * @param to
     *            an array of view resource ids that the data will be bound to
     */
    public GenericAdapter(Context context, GenericDataProvider dataProvider, int viewResourceId, String[] from, int[] to) {
	mContext = context;
	mDataProvider = dataProvider;
	mViewResourceId = viewResourceId;
	mFrom = from;
	mTo = to;
	mInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
	if (mDataProvider == null)
	    return 0;
	return mDataProvider.getCount();
    }

    @Override
    public HashMap<String, String> getItem(int position) {
	return mDataProvider.getItem(position);
    }

    @Override
    public long getItemId(int position) {
	return mDataProvider.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	View view = convertView;
	boolean isConverted = true;

	if (view == null) {

	    Log.v("talkinginterval", "create view " + position);

	    view = mInflater.inflate(mViewResourceId, parent, false);
	    isConverted = false;
	}

	modifyView(position, view, isConverted);

	for (int i = 0; i < mTo.length; i++) {

	    View v = view.findViewById(mTo[i]);
	    String from = mFrom[i];
	    HashMap<String, String> data = mDataProvider.getItem(position);

	    String d = data.get(mFrom[i]);

	    boolean bound = false;
	    if (mViewBinder != null) {
		bound = mViewBinder.setViewValue(view, v, d, from);
	    }

	    if (!bound) {

		if (v instanceof TextView) {

		    ((TextView) v).setText(d);

		} else {
		    throw new IllegalStateException(v.getClass().getName() + " is not a "
			    + " view that can be bounds by this Adapter");
		}
	    }

	}

	return view;
    }

    /**
     * Modifies the view at a given position
     * 
     * @param position
     *            the position of the view with respect to the data set
     * @param view
     *            the view
     * @param isConverted
     *            whether its a newly created view or has been recycled
     * @return the view after the modifications
     */
    protected View modifyView(int position, View view, boolean isConverted) {
	return view;
    }

    /**
     * Set a custom {@link ViewBinder}
     * 
     * @param viewBinder
     */
    public void setViewBinder(ViewBinder viewBinder) {
	mViewBinder = viewBinder;
    }

    /**
     * Swaps the current {@link GenericDataProvider} with a new one and redraws
     * the ListView to show the new data
     * 
     * @param dataProvider
     *            the new {@link GenericDataProvider}
     */
    public void reloadData(GenericDataProvider dataProvider) {
	mDataProvider = dataProvider;
	notifyDataSetChanged();
    }

    /**
     * Interface used to allow custom binding of data to views in the lists row
     */
    public static interface ViewBinder {

	/**
	 * called to do the binding
	 * 
	 * @param parent
	 *            the top level view for the row in the list
	 * @param view
	 *            the view to try to bind the data to
	 * @param data
	 *            the data to be bound
	 * @param from
	 *            the key given to the data
	 * @return whether the data has been bound to a view
	 */
	boolean setViewValue(View parent, View view, String data, String from);
    }

}
