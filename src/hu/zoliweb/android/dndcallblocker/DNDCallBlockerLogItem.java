

package hu.zoliweb.android.dndcallblocker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DNDCallBlockerLogItem {
	String phone_nr;
	Date created;

	public String getPhoneNr() {
		return phone_nr;
	}

	public Date getCreated() {
		return created;
	}

	public DNDCallBlockerLogItem(String _phone_nr) {
		this(_phone_nr, new Date(java.lang.System.currentTimeMillis()));
	}

	public DNDCallBlockerLogItem(String _phone_nr, Date _created) {
		phone_nr = _phone_nr;
		created = _created;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		String dateString = sdf.format(created);
		return "(" + dateString + ") " + phone_nr;
	}
}
