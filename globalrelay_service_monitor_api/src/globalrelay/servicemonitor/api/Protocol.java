package globalrelay.servicemonitor.api;

import globalrelay_common.Constants;
import net.sf.json.JSONObject;

public class Protocol {

	public static JSONObject generateRegisterRequest(String serviceName, long interval, long graceTime, String clientId) {
		JSONObject ret = new JSONObject();
		
		ret.accumulate(Constants.COMMAND, Constants.REGISTER_COMMAND);
		ret.accumulate(Constants.HOST, serviceName);
		ret.accumulate(Constants.INTERVAL, interval);
		ret.accumulate(Constants.GRACE_TIME, graceTime);
		ret.accumulate(Constants.CLIENT_ID, clientId);

		return ret;
	}
}
