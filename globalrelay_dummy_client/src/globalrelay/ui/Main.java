package globalrelay.ui;

import org.apache.commons.lang.math.RandomUtils;

import globalrelay.servicemonitor.api.IServiceMonitorListener;
import globalrelay.servicemonitor.api.ServiceMonitorApi;

/**
 * This is a dummy client which demonstrates how you would express intent
 * to be alerted when a service goes up or down.
 * 
 * @author JimRyan
 */
public class Main {
	//Replace this address with the service address used to launch globalrelay_dummy_server
	private static final String SERVICE_ADDRESS = "192.168.1.70:4000";
	
	public static void main(String[] args) {
		ServiceMonitorApi.getInstance().register(SERVICE_ADDRESS, 1000 + RandomUtils.nextInt(10000), 5000L, new IServiceMonitorListener() {
			@Override
			public void updateReceived(boolean isServiceUp) {
				System.out.println("Is server up: " + isServiceUp);
			}

			@Override
			public void registerComplete(boolean isSuccessful, String sessionId) {
				System.out.println("Registration complete: " + isSuccessful + " " + sessionId);
			}

			@Override
			public void unregistered() {
				System.out.println("Unregistered");				
			}
		});
	}
}
