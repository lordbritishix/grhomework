package globalrelay.server;

import globalrelay.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.lang.math.RandomUtils;

/**
 * A simple echo server
 * 
 * @author JimRyan
 *
 */
public class Server {
	public enum LaunchMode {
		ONLINE,
		OFFLINE
	}
	
	private static final int BACKLOG = 50;

	private ServerSocket m_server;
	private Date m_outageStart = null;
	private Date m_outageEnd = null;
	private LaunchMode m_mode;
	
	/**
	 * Starts the server using the local host port - blocks until server is terminated
	 * 
	 * @param port 
	 * @param ip
	 * @param withRandomPlannedOutage - if true, then server will not accept connection and will send 503 message to the client
	 * 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public void start(int port, InetAddress ip, LaunchMode mode, boolean withRandomPlannedOutage) throws UnknownHostException, IOException {
		m_server = new ServerSocket(port, BACKLOG, ip);
		m_mode = mode;
		
		System.out.println("Starting the server: " + m_server.getLocalSocketAddress().toString());
		System.out.println(String.format("Port: %d Ip: %s With Outage: %b Launch Mode: %s", port, ip.getHostName(), withRandomPlannedOutage, mode.toString()));

		if (withRandomPlannedOutage) {
			m_outageStart = new Date();
			m_outageEnd = Utils.addDaysToDate(m_outageStart, 1 + RandomUtils.nextInt(2));
		}

		while (true) {
			processConnection(m_server.accept());
		}
	}

	/**
	 * Spawns a new thread to handle the connection.
	 * 
	 * @param socket
	 */
	private void processConnection(final Socket socket) {
		new Thread(new Runnable() {
			PrintWriter out = null;
			BufferedReader in = null;
			String message;
			
			@Override
			public void run() {
				try {
					System.out.println("New connection from: " + socket.getLocalSocketAddress().toString());
					
					out = new PrintWriter(socket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
					switch(m_mode) {
					case OFFLINE:
						System.out.println("Server offline - not accepting connections.");
						out.println(Protocol.generateOfflineResponse());

						break;

					case ONLINE:
						if (isOnMaintenance()) {
							System.out.println("Server in maintenance mode - not accepting connections.");
							out.println(Protocol.generateMaintenanceResponse(m_outageStart, m_outageEnd));
						}
						else {
							out.println(Protocol.generateHelloResponse().toString());
							
							while ((message = in.readLine()) != null) {
								processMessage(message, out);
							}
						}

						break;
					}
				} catch (IOException e) {
				} finally {
					System.out.println("Client disconnected: " + socket.getLocalSocketAddress().toString());

					if (out != null) {
						out.close();
					}
					
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}).start();
	}
	
	/**
	 * Checks if we are on maintenance mode or not
	 * 
	 * @return
	 */
	private boolean isOnMaintenance() {
		if ((m_outageStart == null) || (m_outageEnd == null)) {
			return false;
		}

		final long now = System.currentTimeMillis();
		
		return (((now >= m_outageStart.getTime()) && 
				 (now < m_outageEnd.getTime())));
	}
	
	/**
	 * Echoes back sent message to the client
	 * 
	 * @param message
	 * @param out
	 */
	private void processMessage(String message, PrintWriter out) {
		out.println("Echo: " + message);
	}
}
