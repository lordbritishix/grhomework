package globalrelay.servicemonitor;

import globalrelay_common.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.UUID;

import net.sf.json.JSONObject;

public class ClientHandler {
	public enum ClientStatus {
		ONLINE,
		OFFLINE,
		MAINTENANCE_MODE
	}
	
	private final Socket m_socket;
	private final ServiceMonitor m_serviceMonitor;
	private long m_pollingFrequency = -1L;
	private String m_sessionId = UUID.randomUUID().toString();
	private String m_clientId;
	private String m_hostName;
	private int m_portNumber;
	private long m_graceTime;
	private long m_graceTimeCounter = -1L;

	private boolean m_isOnline;
	private ClientStatus m_clientStatus;
	
	public ClientHandler(Socket socket, ServiceMonitor serviceMonitor) {
		m_socket = socket;
		m_serviceMonitor = serviceMonitor;
	}
	
	public String getServiceAddress() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getHost());
		buffer.append(":");
		buffer.append(getPort());
		
		return buffer.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getServiceAddress());
		buffer.append(", ");
		buffer.append(getPollingFrequency());
		buffer.append(", ");
		buffer.append(getSessionId());
		buffer.append(", ");
		buffer.append(getClientId());
		buffer.append(", ");
		buffer.append(getGraceTime());


		return buffer.toString();
	}

	public long getPollingFrequency() {
		return m_pollingFrequency;
	}

	public void setPollingFrequency(long pollingFrequency) {
		m_pollingFrequency = pollingFrequency;
	}

	public String getClientId() {
		return m_clientId;
	}

	public void setClientId(String clientId) {
		m_clientId = clientId;
	}

	public String getHost() {
		return m_hostName;
	}

	public void setHost(String hostName) {
		m_hostName = hostName;
	}

	public int getPort() {
		return m_portNumber;
	}

	public void setPort(int portNumber) {
		m_portNumber = portNumber;
	}

	public long getGraceTime() {
		return m_graceTime;
	}

	public void setGraceTime(long graceTime) {
		m_graceTime = graceTime;
	}

	public String getSessionId() {
		return m_sessionId;
	}

	public void setSessionId(String sessionId) {
		m_sessionId = sessionId;
	}

	public boolean hasGraceTime() {
		return (m_graceTime > 0L);
	}
	
	public void listen() {
		new Thread(new Runnable() {
			PrintWriter out = null;
			BufferedReader in = null;
			String message;
			
			@Override
			public void run() {
				try {
					System.out.println("New connection from: " + m_socket.getLocalSocketAddress().toString());
					
					out = new PrintWriter(m_socket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
					
					String response = null;
					while ((message = in.readLine()) != null) {
						response = processMessage(message);
						
						if (response != null) {
							System.out.println("Sending back to client: " + response);
							out.println(response);
						}
					}
				} catch (IOException e) {
				} finally {
					System.out.println("Client disconnected: " + ClientHandler.this.toString());
	
					if (out != null) {
						out.close();
					}
	
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
						}
					}
					
					m_serviceMonitor.removePollService(ClientHandler.this);
				}
			}
		}).start();		
	}
	
	private String processMessage(String message) {
		System.out.println("Message from client: " + message);

		JSONObject messageAsJson = JSONObject.fromObject(message);

		String command = messageAsJson.optString(Constants.COMMAND);
		String response = null;

		if (command.equals(Constants.REGISTER_COMMAND)) {
			response = handleRegister(messageAsJson);
		}

		return response;
	}

	private String handleRegister(JSONObject messageAsJson) {
		String host = messageAsJson.optString(Constants.HOST);
		String[] parsedHost = host.split(":");
		
		if (parsedHost.length == 2) {
			setHost(parsedHost[0]);
			setPort(Integer.parseInt(parsedHost[1]));
		}
		
		setPollingFrequency(messageAsJson.optInt(Constants.INTERVAL));
		setGraceTime(messageAsJson.optLong(Constants.GRACE_TIME));
		setClientId(messageAsJson.optString(Constants.CLIENT_ID));
		
		System.out.println("Client wants to register: " + toString());

		m_serviceMonitor.addPollService(this);

		// Handles client registration
		return Protocol.generateResponseToRegisterCommand(HttpURLConnection.HTTP_OK, 
															getSessionId(), 
															getClientId()).toString();
	}

	private void handleServiceUpdateChanged(boolean isServiceRunning) {
		try {
			PrintWriter out = new PrintWriter(m_socket.getOutputStream(), true);
			out.println(Protocol.generateServiceStatusChanged(isServiceRunning,
																getSessionId(), getClientId()).toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isOnline() {
		return m_isOnline;
	}

	public void setOnline(boolean isOnline) {
		m_isOnline = isOnline;
	}

	public ClientStatus getClientStatus() {
		return m_clientStatus;
	}

	public void setClientStatus(ClientStatus clientStatus) {
		if (clientStatus != m_clientStatus) {
			switch (clientStatus) {
			case OFFLINE:
				handleServiceUpdateChanged(false);
				break;
				
			case ONLINE:
				handleServiceUpdateChanged(true);
				break;
				
			default:
				break;
			}
		}
		
		m_clientStatus = clientStatus;
	}

	public long getGraceTimeCounter() {
		return m_graceTimeCounter;
	}

	public void setGraceTimeCounter(long graceTimeCounter) {
		m_graceTimeCounter = graceTimeCounter;
	}
}
