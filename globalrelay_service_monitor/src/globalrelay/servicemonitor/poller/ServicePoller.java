package globalrelay.servicemonitor.poller;

import globalrelay.servicemonitor.poller.IPollListener.Reason;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Checks every second to see if a service needs to be polled.
 * Also manages clients that wants to poll the service and ensures that
 * a service does not get polled more than once every second.
 * 
 * @author JimRyan
 *
 */
public class ServicePoller extends TimerTask {
	private static class ServiceStatus {
		private final long m_lastPoll;
		private final Reason m_reason;
		
		public ServiceStatus(long lastPoll, Reason reason) {
			m_lastPoll = lastPoll;
			m_reason = reason;
		}
		
		public long getLastPoll() {
			return m_lastPoll;
		}
		public Reason getReason() {
			return m_reason;
		}
	}
	
	private static final long CHECK_SERVICES_INTERVAL = 1000L;
	private final IPollListener m_listener;
	private ConcurrentHashMap<String, PollItem> m_services = new ConcurrentHashMap<String, PollItem>();
	private ConcurrentHashMap<String, ServiceStatus> m_lastResults = new ConcurrentHashMap<String, ServiceStatus>();

	private boolean m_started = false;
	
	public ServicePoller(IPollListener listener) {
		m_listener = listener;
	}
	
	public void start() {
		if (m_started) {
			return;
		}

		System.out.println("Starting polling service");
		new Timer().schedule(this, 0, CHECK_SERVICES_INTERVAL);
	}
	
	public void addPollService(PollItem item) {
		m_services.put(item.getClientId(),  item);
	}
	
	public void removePollService(String clientId) {
		m_services.remove(clientId);
	}

	private boolean isLastServicePollTimePastInterval(String serviceAddress, long interval) {
		boolean ret = false;
		
		if (m_lastResults.containsKey(serviceAddress)) {
			ServiceStatus status = m_lastResults.get(serviceAddress);
			long lastPoll = status.getLastPoll();
			long now = System.currentTimeMillis();
			
			if ((now - lastPoll) > interval) {
				ret = true;
			}
		}
		else {
			return true;
		}
		
		return ret;
	}
	
	@Override
	public void run() {
		poll();
	}
	
	public void poll() {
		for (PollItem item : m_services.values()) {
			if (item.isTimeToPoll()) {
				if (isLastServicePollTimePastInterval(item.getServiceAddress(), 1000L)) {
					Reason ret = item.poll();
					System.out.println("Polling: " + item.toString());
	
					ServiceStatus status = new ServiceStatus(System.currentTimeMillis(), ret);
					m_lastResults.put(item.getServiceAddress(), status);
					
					m_listener.pollComplete(item.getClientId(), ret);
				}
				else {
					System.out.println("Not polling - sending previous result: " + item.toString());
					
					ServiceStatus status = m_lastResults.get(item.getServiceAddress());
					m_listener.pollComplete(item.getClientId(), status.getReason());
				}
			}
		}
	}
}
