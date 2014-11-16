package globalrelay.servicemonitor;

import java.net.HttpURLConnection;

import globalrelay_common.Constants;
import net.sf.json.JSONObject;

public class Protocol {
	public static JSONObject generateResponseToRegisterCommand(int retCode, String sessionId, String clientId) {
		JSONObject ret = new JSONObject();
		
		ret.accumulate(Constants.COMMAND, Constants.REGISTER_COMMAND);
		ret.accumulate(Constants.CODE, retCode);
		ret.accumulate(Constants.SESSION_ID, sessionId);
		ret.accumulate(Constants.CLIENT_ID, clientId);

		return ret;
	}
	
	public static JSONObject generateServiceStatusChanged(boolean isServiceRunning, String sessionId, String clientId) {
		JSONObject ret = new JSONObject();
		
		ret.accumulate(Constants.COMMAND, Constants.SERVICE_STATUS_UPDATE_COMMAND);
		ret.accumulate(Constants.SESSION_ID, sessionId);
		ret.accumulate(Constants.CLIENT_ID, clientId);

		if (isServiceRunning) {
			ret.accumulate(Constants.CODE, HttpURLConnection.HTTP_OK);
		}
		else {
			ret.accumulate(Constants.CODE, HttpURLConnection.HTTP_UNAVAILABLE);
		}

		return ret;

	}
}
