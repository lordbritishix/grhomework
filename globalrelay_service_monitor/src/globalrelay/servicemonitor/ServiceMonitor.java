package globalrelay.servicemonitor;

import globalrelay.servicemonitor.ClientHandler.ClientStatus;
import globalrelay.servicemonitor.poller.IPollListener;
import globalrelay.servicemonitor.poller.PollItem;
import globalrelay.servicemonitor.poller.ServicePoller;
import globalrelay_common.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

/**
 * Registers clients, interprets poll results, and informs clients
 * if there is a change in the service status.  
 * 
 * @author JimRyan
 *
 */
public class ServiceMonitor implements IPollListener {
	private ServerSocket m_server;
	private ServicePoller m_servicePoller = new ServicePoller(this);
	private LinkedHashMap<String, ClientHandler> m_handlers = new LinkedHashMap<String, ClientHandler>();
	
	public void start() {
		try {
			m_server = new ServerSocket(Constants.SERVICE_MONITOR_PORT);
			m_servicePoller.start();
			
			System.out.println("Service Monitor started.. " + Constants.SERVICE_MONITOR_ADDRESS + ":" + Constants.SERVICE_MONITOR_PORT);
			
			while(true) {
				processConnection(m_server.accept());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processConnection(final Socket socket) {
		ClientHandler connection = new ClientHandler(socket, this);
		connection.listen();
	}

	public void addPollService(ClientHandler clientHandler) {
		m_handlers.put(clientHandler.getClientId(), clientHandler);
		
		PollItem pollItem = new PollItem(clientHandler.getHost(),
											clientHandler.getPort(),
											clientHandler.getPollingFrequency(), 
											clientHandler.getClientId());

		m_servicePoller.addPollService(pollItem);
	}
	
	public void removePollService(ClientHandler clientHandler) {
		m_handlers.remove(clientHandler);
		m_servicePoller.removePollService(clientHandler.getClientId());
	}

	@Override
	public void pollComplete(String clientId, Reason reason) {
		ClientHandler handler = m_handlers.get(clientId);
		
		if (handler != null) {
			switch(reason) {
			case SERVER_NOT_RESPONDING:
				if (!handler.hasGraceTime()) {
					handler.setClientStatus(ClientStatus.OFFLINE);
				}
				else {
					if (handler.getGraceTimeCounter() == -1L) {
						System.out.println("Grace time kicking in...");
						handler.setGraceTimeCounter(System.currentTimeMillis());
					}
					else {
						long duration = System.currentTimeMillis() - handler.getGraceTimeCounter();
						if (duration > handler.getGraceTime()) {
							System.out.println("Grace time expired...");
							handler.setGraceTimeCounter(-1L);
							handler.setClientStatus(ClientStatus.OFFLINE);
						}
					}
				} 
				
				break;
				
			case SERVER_OFFLINE:
				handler.setClientStatus(ClientStatus.OFFLINE);
				break;
				
			case SERVER_ON_MAINTENANCE_MODE:
				handler.setClientStatus(ClientStatus.MAINTENANCE_MODE);
				break;
				
			case SERVER_ONLINE:
				handler.setGraceTimeCounter(-1L);
				handler.setClientStatus(ClientStatus.ONLINE);
				break;
			}
		}
	}
}
