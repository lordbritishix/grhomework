package globalrelay.main;

import globalrelay.servicemonitor.ServiceMonitor;

/**
 * This class listens to connection requests made by the ServiceMonitorApi class.
 * Its responsibility is to poll services requested by the ServiceMonitorApi class. It also
 * informs the caller if there is a change in status of the service that the caller wants to monitor
 * via TCP.
 * 
 * This server is currently bound to localhost:9999 when it launches
 * 
 * @author JimRyan
 *
 */
public class Main {
	public static void main(String[] args) {
		ServiceMonitor monitor = new ServiceMonitor();
		monitor.start();
	}
}
