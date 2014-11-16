package globalrelay.server;


import globalrelay_common.Constants;

import java.net.HttpURLConnection;
import java.util.Date;

import net.sf.json.JSONObject;

public class Protocol {
	public static JSONObject generateHelloResponse() {
		JSONObject ret = new JSONObject();
		
		ret.accumulate(Constants.CODE, HttpURLConnection.HTTP_OK);
		ret.accumulate(Constants.MESSAGE, "Welcome!");
		
		return ret;
	}
	
	public static JSONObject generateOfflineResponse() {
		JSONObject ret = new JSONObject();
		
		ret.accumulate(Constants.CODE, HttpURLConnection.HTTP_UNAVAILABLE);
		ret.accumulate(Constants.MESSAGE, "Server offline!");
		
		return ret;
	}

	
	public static JSONObject generateMaintenanceResponse(Date from, Date to) {
		JSONObject ret = new JSONObject();
		
		ret.accumulate(Constants.CODE, HttpURLConnection.HTTP_UNAVAILABLE);
		ret.accumulate(Constants.MESSAGE, "Server maintenance from: " + from.toString() + " to: " + to.toString());
		ret.accumulate(Constants.MAINTENANCE_FROM, from.getTime());
		ret.accumulate(Constants.MAINTENANCE_TO, to.getTime());

		return ret;
	}

}
