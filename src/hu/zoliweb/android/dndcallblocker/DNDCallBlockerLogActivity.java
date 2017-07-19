
package hu.zoliweb.android.dndcallblocker;

import java.util.ArrayList;
import java.util.Date;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class DNDCallBlockerLogActivity extends Activity {

	DNDCallBlockerDBAdapter logDBAdapter;
	Cursor logCursor;

	private ArrayList<DNDCallBlockerLogItem> logItems;
	private ListView myListView;
	private DNDCallBlockerLogItemAdapter lia;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.loglist);
		
		myListView = (ListView)findViewById(R.id.myListView);

	    logItems = new ArrayList<DNDCallBlockerLogItem>();
	    int resID = R.layout.loglist_item;
	    lia = new DNDCallBlockerLogItemAdapter(this, resID, logItems);
	    myListView.setAdapter(lia);

		logDBAdapter = new DNDCallBlockerDBAdapter(this);
		logDBAdapter.open();

		populateLog();
	}

	private void populateLog() {
		// Get all the todo list items from the database.
		logCursor = logDBAdapter.getAllLogCursor();
		startManagingCursor(logCursor);
		// Update the array.
		updateArray();
	}

	private void updateArray() {
		logCursor.requery();
		
		logItems.clear();
		
		if (logCursor.moveToFirst())
		    do { 
		      String task = logCursor.getString(logCursor.getColumnIndex(DNDCallBlockerDBAdapter.KEY_PHONENR));
		      long created = logCursor.getLong(logCursor.getColumnIndex(DNDCallBlockerDBAdapter.KEY_CREATION_DATE));

		      DNDCallBlockerLogItem newItem = new DNDCallBlockerLogItem(task, new Date(created));
		      logItems.add(0, newItem);
		    } while(logCursor.moveToNext());
		  
		  lia.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Close the database
		logDBAdapter.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.log_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.log_clear:
	        logDBAdapter.clearAllItems();
	        updateArray();
	        lia.notifyDataSetChanged();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

}
