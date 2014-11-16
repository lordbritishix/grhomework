package globalrelay.test;
import static org.mockito.Mockito.when;
import globalrelay.servicemonitor.ClientHandler;
import globalrelay.servicemonitor.ServiceMonitor;
import globalrelay.servicemonitor.ClientHandler.ClientStatus;
import globalrelay.servicemonitor.poller.IPollListener.Reason;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;  

/**\
 * Tests requirement:
 * 
 * The monitor will allow callers to register interest in a service, and a polling 
 * frequency. The callers will be notified when the service goes up and down.
 * 
 * At any time a service can be configured with a planned service outage; however, 
 * not all services need to specify an outage. The service outage will specify a start 
 * and end time for which no notifications for that service will be delivered.
 * 
 * The monitor should allow callers to define a grace time. If a service is not 
 * responding, the monitor will wait for the grace time to expire before notifying any
 * clients. If the service goes back on line during this grace time, no notification will 
 * be sent.
 * 
 * @author JimRyan
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ServiceMonitorTest {
	private ServiceMonitor m_monitor;
	private @Mock ClientHandler m_clientHandler;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		m_monitor = new ServiceMonitor();
	}
	
	@After
	public void cleanup() {
		m_monitor = null;
	}
	
	@Test
	public void testClientIsNotifiedWithOnlineWhenServiceGoesUp() {
		final String clientId = "12345";
		
		when(m_clientHandler.getClientId()).thenReturn(clientId);
		m_monitor.addPollService(m_clientHandler);
		m_monitor.pollComplete(clientId, Reason.SERVER_ONLINE);
		
		verify(m_clientHandler, times(1)).setClientStatus(ClientStatus.ONLINE);
	}
	
	@Test
	public void testClientIsNotifiedWithOfflineWhenServiceGoesDown() {
		final String clientId = "12345";
		
		when(m_clientHandler.getClientId()).thenReturn(clientId);
		m_monitor.addPollService(m_clientHandler);
		m_monitor.pollComplete(clientId, Reason.SERVER_OFFLINE);
		
		verify(m_clientHandler, times(1)).setClientStatus(ClientStatus.OFFLINE);
	}

	@Test
	public void testClientIsNotifiedWithMaintenanceWhenServiceGoesDown() {
		final String clientId = "12345";
		
		when(m_clientHandler.getClientId()).thenReturn(clientId);
		m_monitor.addPollService(m_clientHandler);
		m_monitor.pollComplete(clientId, Reason.SERVER_ON_MAINTENANCE_MODE);
		
		verify(m_clientHandler, times(1)).setClientStatus(ClientStatus.MAINTENANCE_MODE);
	}

	
	@Test
	public void testClientIsNotifiedBeforeGraceTimeExpiresWhenServiceIsUnreachable() {
		final String clientId = "12345";
		
		when(m_clientHandler.getClientId()).thenReturn(clientId);
		when(m_clientHandler.getGraceTime()).thenReturn(500L);
		when(m_clientHandler.hasGraceTime()).thenReturn(true);
		when(m_clientHandler.getGraceTimeCounter()).thenReturn(-1L);

		m_monitor.addPollService(m_clientHandler);
		m_monitor.pollComplete(clientId, Reason.SERVER_NOT_RESPONDING);
		verify(m_clientHandler, times(0)).setClientStatus(ClientStatus.OFFLINE);
		when(m_clientHandler.getGraceTimeCounter()).thenReturn(System.currentTimeMillis());

		m_monitor.pollComplete(clientId, Reason.SERVER_NOT_RESPONDING);
		verify(m_clientHandler, times(0)).setClientStatus(ClientStatus.OFFLINE);

		try {
			synchronized(this) {
				wait(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		m_monitor.pollComplete(clientId, Reason.SERVER_NOT_RESPONDING);
		verify(m_clientHandler, times(1)).setClientStatus(ClientStatus.OFFLINE);
	}

}
