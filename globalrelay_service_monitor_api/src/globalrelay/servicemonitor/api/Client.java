package globalrelay.servicemonitor.api;

import globalrelay_common.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import net.sf.json.JSONObject;

public class Client {
	private final String m_serviceAddress;
	private final long m_pollingFrequency;
	private final long m_graceTime;
	private final IServiceMonitorListener m_listener;
	private final String m_clientId = UUID.randomUUID().toString();
	private boolean m_isConnected = false;
	private boolean m_isRegistered = false;

	private Socket m_socket;
	PrintWriter m_out = null;
	BufferedReader m_in = null;

	public Client(String serviceAddress, int pollingFrequency, long graceTime, IServiceMonitorListener listener) {
		m_graceTime = graceTime;
		m_serviceAddress = serviceAddress;
		m_pollingFrequency = pollingFrequency;
		m_listener = listener;
	}
	
	public String getClientId() {
		return m_clientId;
	}
	
	public String getServiceAddress() {
		return m_serviceAddress;
	}
	
	public long getPollingFrequency() {
		return m_pollingFrequency;
	}

	public IServiceMonitorListener getListener() {
		return m_listener;
	}

	public long getGraceTime() {
		return m_graceTime;
	}

	/**
	 * Creates a socket connection to the service monitor, attempts to register to it, and then listens for events 
	 */
	public void requestConnectAndRegister() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					m_socket = new Socket(Constants.SERVICE_MONITOR_ADDRESS, Constants.SERVICE_MONITOR_PORT);
					m_out = new PrintWriter(m_socket.getOutputStream(), true);
					m_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
					String message = null;
					
					m_isConnected = true;
					
					//Attempt to register
					m_out.println(Protocol.generateRegisterRequest(
							getServiceAddress(), getPollingFrequency(), getGraceTime(), getClientId()).toString());
					
					//And listen to socket messages
					while ((message = m_in.readLine()) != null) {
						handleMessage(message);
					}
				} catch (UnknownHostException e) {
					getListener().registerComplete(false, "");
				} catch (IOException e) {
					if (!m_isConnected) {
						getListener().registerComplete(false, "");
					}
				} finally {
					close();

					if (m_isConnected && m_isRegistered) {
						getListener().unregistered();
					}
				}				
			}
		}).start();		
	}
	
	private void close() {
		try {
			if (m_socket != null) {
				m_socket.close();
			}
		
			if (m_in != null) {
				m_in.close();
			}
			
			if (m_out != null) {
				m_out.close();
			}
		} catch (IOException e) {
		}
	}
	
	private void handleMessage(String message) {
		System.out.println("Message from the server: " + message);
		
		JSONObject messageAsJson = JSONObject.fromObject(message);
		String command = messageAsJson.optString(Constants.COMMAND);
		
		if (command.equals(Constants.REGISTER_COMMAND)) {
			handleRegisterCommand(messageAsJson);
		}
		else if (command.equals(Constants.SERVICE_STATUS_UPDATE_COMMAND)) {
			handleServiceStatusUpdateCommand(messageAsJson);
		}
	}

	private void handleServiceStatusUpdateCommand(JSONObject messageAsJson) {
		int code = messageAsJson.optInt(Constants.CODE);

		switch(code) {
		case HttpURLConnection.HTTP_OK:
			getListener().updateReceived(true);
			break;
		
		default:
			getListener().updateReceived(false);
			
			break;
		}
	}

	private void handleRegisterCommand(JSONObject messageAsJson) {
		int code = messageAsJson.optInt(Constants.CODE);
		
		switch(code) {
		case HttpURLConnection.HTTP_OK:
			m_isRegistered = true;
			getListener().registerComplete(true, messageAsJson.optString(Constants.SESSION_ID));
			break;
			
		default:
			close();
			getListener().registerComplete(false, "");
			
			break;
		}
	}
}
