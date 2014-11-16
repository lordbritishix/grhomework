package globalrelay.servicemonitor.poller;

import globalrelay.servicemonitor.poller.IPollListener.Reason;
import globalrelay_common.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import net.sf.json.JSONObject;

/**
 * Performs the actual polling. Interprets the result returned by the server.
 * 
 * @author JimRyan
 *
 */
public class PollItem  {
	private final String m_host;
	private final long m_frequency;
	private final int m_port;
	private final String m_clientId;
	private long m_timeSinceLastPoll = -1L;
	private long m_maintenanceStart = -1L;
	private long m_maintenanceEnd = -1L;
	private SocketFactory m_socketFactory = null;
	
	public PollItem(String host, int port, long frequency, String clientId) {
		m_port = port;
		m_host = host;
		m_frequency = frequency;
		m_clientId = clientId;
	}

	public String getHost() {
		return m_host;
	}

	public long getFrequency() {
		return m_frequency;
	}

	public long getMaintenanceStart() {
		return m_maintenanceStart;
	}

	public void setMaintenanceStart(long maintenanceStart) {
		m_maintenanceStart = maintenanceStart;
	}

	public long getMaintenanceEnd() {
		return m_maintenanceEnd;
	}

	public void setMaintenanceEnd(long maintenanceEnd) {
		m_maintenanceEnd = maintenanceEnd;
	}

	public int getPort() {
		return m_port;
	}

	public String getServiceAddress() {
		StringBuffer ret = new StringBuffer();
		
		ret.append(getHost());
		ret.append(":");
		ret.append(getPort());
		
		return ret.toString();
	}
	
	public Reason poll() {
		Reason ret = Reason.SERVER_OFFLINE;
		
		setTimeSinceLastPoll(System.currentTimeMillis());

		Socket socket = null;
		BufferedReader in = null;

		try {
			if (getSocketFactory() == null) {
				socket = new Socket(getHost(), getPort());
			}
			else {
				socket = getSocketFactory().createSocket(getHost(), getPort());	
			}
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String message = null;
			
			while ((message = in.readLine()) != null) {
				ret = handleMessage(message);
				break;
			}
		} 
		catch (UnknownHostException e) {
			ret = Reason.SERVER_OFFLINE;
		} 
		catch (IOException e) {
			ret = Reason.SERVER_NOT_RESPONDING;
		} 
		catch (Exception e) {
			ret = Reason.SERVER_NOT_RESPONDING;
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
				
				if (socket != null) {
					socket.close();
				}
			}
			catch(IOException e) {
			}
		}
		
		return ret;
	}
	
	private Reason handleMessage(String message) {
		Reason ret = Reason.SERVER_OFFLINE;
		
		JSONObject messageAsJson = JSONObject.fromObject(message);
		int code = messageAsJson.optInt(Constants.CODE);
		
		switch(code) {
			case HttpURLConnection.HTTP_OK:
				ret = Reason.SERVER_ONLINE;
				break;
				
			case HttpURLConnection.HTTP_UNAVAILABLE:
				m_maintenanceStart = messageAsJson.optLong(Constants.MAINTENANCE_FROM, -1L);
				m_maintenanceEnd = messageAsJson.optLong(Constants.MAINTENANCE_TO, -1L);
				
				if ((m_maintenanceStart != -1L) && (m_maintenanceEnd != -1L)) {
					ret = Reason.SERVER_ON_MAINTENANCE_MODE;
				}
				
				break;
		}
		
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(getClientId());
		ret.append(", ");
		ret.append(getServiceAddress());

		return ret.toString();
	}

	public boolean isTimeToPoll() {
		long now = System.currentTimeMillis();
		
		boolean isElapsed = (now - m_timeSinceLastPoll) > m_frequency;
		
		if (isElapsed) {
			m_timeSinceLastPoll = now;
		}
		
		return isElapsed;
	}
	
	public long getTimeSinceLastPoll() {
		return m_timeSinceLastPoll;
	}

	public void setTimeSinceLastPoll(long timeSinceLastPoll) {
		m_timeSinceLastPoll = timeSinceLastPoll;
	}

	public String getClientId() {
		return m_clientId;
	}

	public void doNotPoll() {
		// TODO Auto-generated method stub
		
	}

	public SocketFactory getSocketFactory() {
		return m_socketFactory;
	}

	public void setSocketFactory(SocketFactory socketFactory) {
		m_socketFactory = socketFactory;
	}
}
