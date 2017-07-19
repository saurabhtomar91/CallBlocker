

package hu.zoliweb.android.dndcallblocker;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DNDCallBlockerLogItemAdapter extends
		ArrayAdapter<DNDCallBlockerLogItem> {
	Context context;
	int resource;

	public DNDCallBlockerLogItemAdapter(Context _context, int _resource,
			List<DNDCallBlockerLogItem> _items) {
		super(_context, _resource, _items);
		context = _context;
		resource = _resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout todoView;

		DNDCallBlockerLogItem item = getItem(position);

		String phoneNrString = item.getPhoneNr();
		if (phoneNrString.equals("")) {
			phoneNrString = context.getString(R.string.text_unknownnr);
		}

		String dateString = new String("");
		Date createdDate = item.getCreated();
		Date today = new Date(java.lang.System.currentTimeMillis());
		if (DateFormat.getDateInstance().format(today)
				.equals(DateFormat.getDateInstance().format(createdDate))) {
			dateString = context.getString(R.string.text_today) + ", "
					+ DateFormat.getTimeInstance().format(createdDate);
		} else {
			dateString = DateFormat.getDateTimeInstance().format(createdDate);
		}

		if (convertView == null) {
			todoView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					inflater);
			vi.inflate(resource, todoView, true);
		} else {
			todoView = (LinearLayout) convertView;
		}

		TextView dateView = (TextView) todoView.findViewById(R.id.rowDate);
		TextView phoneNrView = (TextView) todoView.findViewById(R.id.row);

		dateView.setText(dateString);
		phoneNrView.setText(phoneNrString);

		return todoView;
	}
}
