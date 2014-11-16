package globalrelay.servicemonitor.api;

/**
 * Callback interface used by the API to inform clients of service status.
 * 
 * @author JimRyan
 *
 */
public interface IServiceMonitorListener {
	/**
	 * Client is successfully registered on the server. Client will start to recieve 
	 * service updates via updateReceived(..) function.
	 * 
	 * @param isSuccessful
	 * @param sessionId
	 */
	public void registerComplete(boolean isSuccessful, String sessionId);
	
	/**
	 * Client has been unregistered from the server. Client needs to register again.
	 */
	public void unregistered();
	
	/**
	 * Service status updates.
	 * 
	 * @param isServiceUp - true if service is running. False otherwise.
	 */
	public void updateReceived(boolean isServiceUp);
}
