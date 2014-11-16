package globalrelay.utils;

import java.util.Calendar;
import java.util.Date;

public class Utils {
	public static Date addDaysToDate(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);

		return c.getTime();
	}
}
