package hu.zoliweb.android.dndcallblocker;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DNDCallBlockerBlackListActivity extends ListActivity {

	private static final String TAG = "DNDCallBlockerBlackListActivity";
	private static final int PICK_CONTACT = 3;
	private static final int PICK_LOG = 4;
	private static final String BLACKLIST_PREF = "blacklist";

	private ArrayList<String> m_phones;
	private ArrayList<String> m_contacts;
	private LayoutInflater m_Inflater;
	private ArrayAdapter<String> m_adapter;
	private SharedPreferences settings;
	private int checkedItem;
	private Context myContext;

	/**
	 * Called when the activity is first created. Responsible for initializing
	 * the UI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myContext = this;
		settings = PreferenceManager.getDefaultSharedPreferences(myContext);

		PackageManager manager = getPackageManager();
		if (manager.checkSignatures("hu.zoliweb.android.dndcallblocker", "hu.zoliweb.android.dndcallblocker.key")
		    == PackageManager.SIGNATURE_MATCH) {
			setContentView(R.layout.noad_blacklist);
		} else {
			setContentView(R.layout.blacklist);
		}
		

		m_Inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_phones = new ArrayList<String>();
		m_contacts = new ArrayList<String>();

		String tmp_phones = settings.getString(BLACKLIST_PREF, "");
		if (tmp_phones != "") {
			String[] tmp_phonesArr = tmp_phones.split(", ");
			Collections.addAll(m_phones, tmp_phonesArr);

			for (String s : m_phones) {
				// search contact for every saved phone number
				ContentResolver cr = getContentResolver();
				Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
						Uri.encode(s));
				Cursor cur = cr.query(uri,
						new String[] { PhoneLookup.DISPLAY_NAME }, null, null,
						null);
				if (cur.moveToFirst()) {
					String name = cur.getString(cur
							.getColumnIndex(PhoneLookup.DISPLAY_NAME));
					m_contacts.add(name);
				} else {
					m_contacts.add("N/A");
				}
				cur.close();
			}
		} else {
			// do nothing, list is empty
		}

		// this adapter makes the black list visible to user
		m_adapter = new ArrayAdapter<String>(this, R.layout.blacklist_list_item, m_phones) {
			@Override
			public View getView(int position, View reusableView,
					ViewGroup parent) {
				View row;

				if (reusableView == null) {
					row = m_Inflater.inflate(R.layout.blacklist_list_item, null);
				} else {
					row = reusableView;
				}

				TextView tv1 = (TextView) row.findViewById(android.R.id.text1);
				tv1.setText(getItem(position));

				TextView tv2 = (TextView) row.findViewById(android.R.id.text2);
				tv2.setText(m_contacts.get(position));

				return row;
			}
		};
		setListAdapter(m_adapter);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		// to handle taps on list items
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// remove item from list
				m_phones.remove(position);
				m_contacts.remove(position);
				String tmp_phones = "";
				if (m_phones.size() > 0) {
					tmp_phones = m_phones.toString();
					tmp_phones = tmp_phones.substring(1,
							tmp_phones.length() - 1);
				}
				// save updated list to sharedpreferences
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(BLACKLIST_PREF, tmp_phones);
				editor.commit();
				// refresh ui
				m_adapter.notifyDataSetChanged();
				// inform user
				String toast_text = getString(R.string.phone_removed);
				Toast.makeText(getApplicationContext(), toast_text,
						Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				// somehow could be possible that data.getData is null :(
				if (contactData != null) {
					Cursor c = managedQuery(contactData, null, null, null, null);
					// get selected phone number & contact name
					if (c.moveToFirst()) {
						try {
							m_phones.add(c
									.getString(
											c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
									.trim());
							m_contacts
									.add(c.getString(
											c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
											.trim());
							// add new item to list
							String tmp_phones = m_phones.toString();
							tmp_phones = tmp_phones.substring(1,
									tmp_phones.length() - 1);
							// save to sharedpreferences
							SharedPreferences.Editor editor = settings.edit();
							editor.putString(BLACKLIST_PREF, tmp_phones);
							editor.commit();
							// refresh ui
							m_adapter.notifyDataSetChanged();
							// inform user
							String toast_text = getString(R.string.phone_added);
							Toast.makeText(getApplicationContext(), toast_text,
									Toast.LENGTH_SHORT).show();
						} catch (IllegalArgumentException e) {
							Log.e(TAG, e.getMessage());
						}
					}
					c.close();
				}
			} break;
		case (PICK_LOG):
			if (resultCode == Activity.RESULT_OK) {
				String numtoadd = data.getAction();
				m_phones.add(numtoadd);
				// search contact for every saved phone number
				ContentResolver cr = getContentResolver();
				Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
						Uri.encode(numtoadd));
				Cursor cur = cr.query(uri,
						new String[] { PhoneLookup.DISPLAY_NAME }, null, null,
						null);
				if (cur.moveToFirst()) {
					String name = cur.getString(cur
							.getColumnIndex(PhoneLookup.DISPLAY_NAME));
					m_contacts.add(name);
				} else {
					m_contacts.add("N/A");
				}
				cur.close();
				// add new item to list
				String tmp_phones = m_phones.toString();
				tmp_phones = tmp_phones.substring(1,
						tmp_phones.length() - 1);
				// save to sharedpreferences
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(BLACKLIST_PREF, tmp_phones);
				editor.commit();
				// refresh ui
				m_adapter.notifyDataSetChanged();
				// inform user
				String toast_text = getString(R.string.phone_added);
				Toast.makeText(getApplicationContext(), toast_text,
						Toast.LENGTH_SHORT).show();
			} break;
		}
	}

	public void pickFromContacts(View target) {
		// start contact list activity to pick one phone number
		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, PICK_CONTACT);
	}

	public void addPhoneNumber(View target) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);

		checkedItem = 0;

		input.setInputType(InputType.TYPE_CLASS_PHONE);

		input.setHint(R.string.hint_typehere);

		alert.setSingleChoiceItems(R.array.manual_phonenumber_types, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						checkedItem = item;
					}
				});

		alert.setView(input);
		alert.setIcon(R.drawable.icon);
		alert.setTitle(R.string.title_add_number);

		alert.setPositiveButton(R.string.message_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString().trim();
				if (!value.equals("")) {
					switch (checkedItem) {
					case 1:
						value = value.trim() + "*";
						break;
					case 2:
						value = "*" + value.trim() + "*";
						break;
					case 3:
						value = "*" + value.trim();
						break;

					default:
						break;
					}
					// put input to list
					m_phones.add(value);
					m_contacts.add("N/A");

					// add new item to list
					String tmp_phones = m_phones.toString();
					tmp_phones = tmp_phones.substring(1,
							tmp_phones.length() - 1);
					// save to sharedpreferences
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(BLACKLIST_PREF, tmp_phones);
					editor.commit();
					// refresh ui
					m_adapter.notifyDataSetChanged();
					// inform user
					String toast_text = getString(R.string.phone_added);
					Toast.makeText(getApplicationContext(), toast_text,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		alert.setNegativeButton(R.string.message_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		alert.show();
	}
	
	public void pickFromCallLog(View target) {
		// start callog list activity to pick one phone number
		Intent intent = new Intent(DNDCallBlockerBlackListActivity.this, DNDCallBlockerCallogListActivity.class);
		startActivityForResult(intent, PICK_LOG);
	}

}
