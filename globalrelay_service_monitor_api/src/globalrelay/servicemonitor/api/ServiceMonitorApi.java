package globalrelay.servicemonitor.api;

import java.util.LinkedHashMap;

/**
 * Provides a mechanism to allow clients to register to the server for notifications.
 * 
 * @author JimRyan
 *
 */
public class ServiceMonitorApi {
	public static ServiceMonitorApi m_instance = new ServiceMonitorApi();
	private LinkedHashMap<String, Client> m_clients = new LinkedHashMap<String, Client>();
	
	private ServiceMonitorApi() {
	}
	
	public static ServiceMonitorApi getInstance() {
		return m_instance;
	}

	/**
	 * Registers a client. A registered client will receive updates whenever the specified service
	 * goes up or down.
	 * 
	 * @param serviceAddress - address and port combination - e.g. 192.168.0.70:4000
	 * @param pollFrequency - how often the polling is done in milliseconds
	 * @param graceTime - if the server is unreachable, server waits until grace time before telling the client that the
	 * 						service is down - in milliseconds. 
	 * @param callback - callback that the server uses for communication.
	 * 
	 * @return client id for reference
	 */
	public String register(String serviceAddress, int pollFrequency, long graceTime, final IServiceMonitorListener callback) {
		if (callback == null) {
			return "";
		}
		
		Client client = new Client(serviceAddress, pollFrequency, graceTime, callback);
		m_clients.put(client.getClientId(), client);
		client.requestConnectAndRegister();
		
		return client.getClientId();
	}
	
	public void unregister(String clientId) {
		m_clients.remove(clientId);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
