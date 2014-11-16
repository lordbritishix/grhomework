package globalrelay.test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

import javax.net.SocketFactory;

import static org.junit.Assert.*;  
import static org.mockito.Mockito.*;  
import globalrelay.servicemonitor.poller.IPollListener.Reason;
import globalrelay.servicemonitor.poller.PollItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests requirement: 
 * 
 * To check if a service is up, the monitor will establish a TCP
 * connection to the host on the specified port. If a connection is established, the 
 * service is up, if the connection is refused, the service is not up.
 * 
 * @author JimRyan
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PollItemTest {
	private PollItem m_pollItem = null;
	
	@Mock private SocketFactory m_factory;
	@Mock private InputStream m_inputStream;
	@Mock private Socket m_socket;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		m_pollItem = new PollItem("", 0, 0, "");
	}
	
	@After
	public void cleanup() {
		m_pollItem = null;
	}
	
	@Test 
	public void testPollReturnsOnlineIfServerIsOnline() {
		try {
			when(m_socket.getInputStream())
				.thenReturn(new ByteArrayInputStream(
						globalrelay.server.Protocol.generateHelloResponse().toString().getBytes()));
			
			when(m_factory.createSocket(anyString(), anyInt())).thenReturn(m_socket);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		m_pollItem.setSocketFactory(m_factory);
		assertEquals(m_pollItem.poll(), Reason.SERVER_ONLINE);
	}
	
	@Test 
	public void testPollReturnsOfflineIfServerIsOffline() {
		try {
			when(m_socket.getInputStream())
				.thenReturn(new ByteArrayInputStream(
						globalrelay.server.Protocol.generateOfflineResponse().toString().getBytes()));
			
			when(m_factory.createSocket(anyString(), anyInt())).thenReturn(m_socket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		m_pollItem.setSocketFactory(m_factory);
		assertEquals(m_pollItem.poll(), Reason.SERVER_OFFLINE);
	}
	
	@Test 
	public void testPollReturnsMaintenanceIfServerIsInMaintenance() {
		try {
			when(m_socket.getInputStream())
				.thenReturn(new ByteArrayInputStream(
						globalrelay.server.Protocol.generateMaintenanceResponse(new Date(), new Date()).toString().getBytes()));
			
			when(m_factory.createSocket(anyString(), anyInt())).thenReturn(m_socket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		m_pollItem.setSocketFactory(m_factory);
		assertEquals(m_pollItem.poll(), Reason.SERVER_ON_MAINTENANCE_MODE);
	}

	@Test 
	public void testPollReturnsNotRespondingWhenServerIsUnreachable() {
		try {
			when(m_socket.getInputStream()).thenThrow(new IOException());
			
			when(m_factory.createSocket(anyString(), anyInt())).thenReturn(m_socket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		m_pollItem.setSocketFactory(m_factory);
		assertEquals(m_pollItem.poll(), Reason.SERVER_NOT_RESPONDING);
	}
}
