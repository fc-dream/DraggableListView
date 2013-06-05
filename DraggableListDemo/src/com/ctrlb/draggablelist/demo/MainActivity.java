package com.ctrlb.draggablelist.demo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import com.ctrlb.draggablelistdemo.R;
import com.ctrlb.draggablelist.DraggableGenericAdapter;
import com.ctrlb.draggablelist.DraggableListView;
import com.ctrlb.draggablelist.MoveableDataProvider;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

    private DraggableGenericAdapter mAdapter;
    private DraggableListView mDraggableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	final String from[] = { data.ITEM_1, data.ITEM_2 };
	final int to[] = { R.id.textView1, R.id.textView2 };
	mAdapter = new DraggableGenericAdapter(this, new data(), R.layout.listitem, from, to, R.drawable.bg_striped_img, R.drawable.ic_drag);
	mDraggableListView = (DraggableListView) findViewById(R.id.listView1);
	mDraggableListView.setAdapter(mAdapter);
	
	mDraggableListView.setHoverColor(Color.parseColor("#99cc00"));
	mDraggableListView.setHoverAlpha(0.75f);

	Button btn = (Button) findViewById(R.id.button1);
	btn.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// toggle whether the list is draggable
		if (mDraggableListView.getDragStatus()) {
		    mDraggableListView.setDragStatus(false);
		}
		else {
		    mDraggableListView.setDragStatus(true);
		}
	    }
	});

    }

    // some random test data
    class data implements MoveableDataProvider {

	public final static String ITEM_1 = "item1";
	public final static String ITEM_2 = "item2";
	private ArrayList<HashMap<String, String>> mArrayList;

	public data() {
	    mArrayList = new ArrayList<HashMap<String, String>>();
	    for (int i = 0; i < 30; i++) {
		HashMap<String, String> hm = new HashMap<String, String>();

		hm.put(ITEM_1, "Text text text !");
		hm.put(ITEM_2, "Item number " + i);

		mArrayList.add(hm);
	    }
	}

	@Override
	public HashMap<String, String> getItem(int position) {
	    return mArrayList.get(position);
	}

	@Override
	public int getCount() {
	    return mArrayList.size();
	}

	@Override
	public long getItemId(int position) {
	    return 0;
	}
	
	
	public void move(int from, int to) {
	    Collections.swap(mArrayList, from, to);
	}

    }

}
