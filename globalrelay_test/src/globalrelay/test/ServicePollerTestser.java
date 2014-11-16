package globalrelay.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import globalrelay.servicemonitor.poller.IPollListener;
import globalrelay.servicemonitor.poller.PollItem;
import globalrelay.servicemonitor.poller.ServicePoller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests requirement: 
 * 
 * The monitor should detect multiple callers registering interest in the same service, 
 * and should not poll any service more frequently than once a second.
 * 
 * @author JimRyan
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ServicePollerTestser {
	private ServicePoller m_poller;
	
	@Mock private IPollListener m_listener;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		m_poller = new ServicePoller(m_listener);
	}
	
	@After
	public void cleanup() {
		m_poller = null;
	}
	
	@Test
	public void testServicePollerShouldNotPollAServiceMoreThanOnceASecond() {
		PollItem poller1 = mock(PollItem.class);
		PollItem poller2 = mock(PollItem.class);
		PollItem poller3 = mock(PollItem.class);

		when(poller1.getClientId()).thenReturn("1");
		when(poller1.getServiceAddress()).thenReturn("dummy.com");
		when(poller1.getFrequency()).thenReturn(1000L);
		when(poller1.isTimeToPoll()).thenReturn(true);
		when(poller2.getClientId()).thenReturn("2");
		when(poller2.getServiceAddress()).thenReturn("dummy.com");
		when(poller2.getFrequency()).thenReturn(1000L);
		when(poller2.isTimeToPoll()).thenReturn(true);
		when(poller3.getClientId()).thenReturn("3");
		when(poller3.getServiceAddress()).thenReturn("dummy.com");
		when(poller3.getFrequency()).thenReturn(1000L);
		when(poller3.isTimeToPoll()).thenReturn(true);

		m_poller.addPollService(poller1);
		m_poller.addPollService(poller2);
		m_poller.addPollService(poller3);

		m_poller.poll();
		
		verify(poller1, times(1)).poll();
		verify(poller2, times(0)).poll();
		verify(poller3, times(0)).poll();
	}
	
	@Test
	public void testServicePollerShouldPollCorrectly() {
		PollItem poller1 = mock(PollItem.class);

		when(poller1.getClientId()).thenReturn("1");
		when(poller1.getServiceAddress()).thenReturn("dummy.com");
		when(poller1.getFrequency()).thenReturn(500L);
		when(poller1.isTimeToPoll()).thenReturn(true);

		m_poller.addPollService(poller1);
		m_poller.poll();
		
		verify(poller1, times(1)).poll();
		when(poller1.isTimeToPoll()).thenReturn(false);

		m_poller.poll();
		
		verify(poller1, times(1)).poll();
		when(poller1.isTimeToPoll()).thenReturn(true);

		m_poller.poll();
		
		verify(poller1, times(1)).poll();
		when(poller1.isTimeToPoll()).thenReturn(true);
		
		try {
			synchronized (this) {
				wait(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_poller.poll();
		verify(poller1, times(2)).poll();
	}

}
