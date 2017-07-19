

package hu.zoliweb.android.dndcallblocker;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DNDCallBlockerDBAdapter {
	private static final String DATABASE_NAME = "dndcb.db";
	private static final String TABLE_NAME = "filter_history";
	private static final int DATABASE_VERSION = 1;

	public static final String KEY_ID = "_id";
	public static final String KEY_PHONENR = "phone_nr";
	public static final String KEY_CREATION_DATE = "creation_date";

	private SQLiteDatabase db;
	private final Context context;
	private toDoDBOpenHelper dbHelper;

	public DNDCallBlockerDBAdapter(Context _context) {
		this.context = _context;
		dbHelper = new toDoDBOpenHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	public void open() throws SQLiteException {
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}

	public void close() {
		db.close();
	}

	// Insert a new task
	public long insertToLog(String _phone_nr) {
		// Create a new row of values to insert.
		ContentValues newLogValues = new ContentValues();
		// Assign values for each row.
		newLogValues.put(KEY_PHONENR, _phone_nr);
		newLogValues.put(KEY_CREATION_DATE,
				new Date(java.lang.System.currentTimeMillis()).getTime());
		// Insert the row.
		return db.insert(TABLE_NAME, null, newLogValues);
	}

	public Cursor getAllLogCursor() {
		return db.query(TABLE_NAME, new String[] { KEY_ID, KEY_PHONENR,
				KEY_CREATION_DATE }, null, null, null, null, null);
	}

	public void clearAllItems() {
		// delete all records
		db.delete(TABLE_NAME, null, null);
	}

	private static class toDoDBOpenHelper extends SQLiteOpenHelper {

		public toDoDBOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		// SQL Statement to create a new database.
		private static final String DATABASE_CREATE = "create table "
				+ TABLE_NAME + " (" + KEY_ID
				+ " integer primary key autoincrement, " + KEY_PHONENR
				+ " text not null, " + KEY_CREATION_DATE + " long);";

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
				int _newVersion) {
			Log.w("TaskDBAdapter", "Upgrading from version " + _oldVersion
					+ " to " + _newVersion
					+ ", which will destroy all old data");

			// Drop the old table.
			_db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			// Create a new one.
			onCreate(_db);
		}
	}
}
