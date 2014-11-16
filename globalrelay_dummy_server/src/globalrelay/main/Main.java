package globalrelay.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import globalrelay.server.Server;
import globalrelay.server.Server.LaunchMode;

/**
 * This creates a dummy server. It expects the following items to be passed via the command line:
 * 1. Service address = ip:port (e.g. 192.168.1.70:4000)  
 * 2. Server is online or offline = online | offline
 * 3. Server is in maintenance mode = true | false
 * 
 * When server is in maintenance mode, it generates a random outage period with startDate = now and 
 * endDate = startDate + random(0-2 days)
 * 
 * When server is online and a client connects, it responds with a message:
 * {
 * 	"code": 200,
 *  "message": "welcome"
 * }
 * 
 * When the server is offline and a client connects, it responds with a message:
 * {
 * 	"code": 503,
 *  "message": "Server offline!"
 * } 
 * 
 * When the server is in maintenance mode and a client connects, it responds with a message:
 * {
 * 	"code": 503,
 *  "message": "Server maintenance from...",
 *  "from": [from],
 *  "to": [to]
 *  "
 * }
 *  
 * @author JimRyan
 *
 */
public class Main {
	private static final int EXPECTED_ADDRESS_LENGTH = 2;
	private static final int EXPECTED_PARAM_COUNT = 3;
	
	private static final int SERVER_ADDRESS_INDEX = 0;
	private static final int LAUNCH_MODE_INDEX = 1;
	private static final int PLANNED_MAINTENANCE_INDEX = 2;

	private static final int IP_INDEX = 0;
	private static final int PORT_INDEX = 1;

	public static void main(String[] args) {
		if (!isValidArgs(args)) {
			printUsage();
			return;
		}
		
		try {
			final Server server = new Server();
			
			try {
				server.start(getPort(args), InetAddress.getByName(getServerAddress(args)), getLaunchMode(args), false);
			} catch (UnknownHostException e) {
				System.out.println("Unknown host: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO Exception: " + e.getMessage());
			}	
		}
		catch (NumberFormatException e) {
			printUsage();
		}
	}
	
	static boolean isValidArgs(String[] args) {
		boolean ret = true;
		
		//Check for number of params
		if (args.length < EXPECTED_PARAM_COUNT) {
			ret = false;
		}
		else {
			String[] address = args[SERVER_ADDRESS_INDEX].split(":");
			if (address.length < EXPECTED_ADDRESS_LENGTH) {
				ret = false;
			}
		}
		
		return ret;
	}
	
	static LaunchMode getLaunchMode(String[] args) {
		LaunchMode ret;
		String param = args[LAUNCH_MODE_INDEX];
		
		if (param.equalsIgnoreCase("online")) {
			ret = LaunchMode.ONLINE;
		}
		else if (param.equalsIgnoreCase("offline")) {
			ret = LaunchMode.OFFLINE;
		}
		else {
			ret = LaunchMode.OFFLINE;
		}
		
		return ret;
	}
	
	
	static String getServerAddress (String[] args) {
		String ret;
		String[] address = args[SERVER_ADDRESS_INDEX].split(":");
		ret = address[IP_INDEX];
		
		return ret;
	}
	
	static int getPort (String[] args) {
		int ret;
		String[] address = args[SERVER_ADDRESS_INDEX].split(":");

		try {
			ret = Integer.parseInt(address[PORT_INDEX]);
		}
		catch (NumberFormatException e) {
			ret = -1;
		}
		
		return ret;
	}

	static boolean getPlannedMaintenance(String[] args) {
		boolean ret = Boolean.parseBoolean(args[PLANNED_MAINTENANCE_INDEX]);

		return ret;
	}
	
	static void printUsage() {
		System.out.println("Syntax is: [ip:port] [online | offline] [true | false]");
		System.out.println("Example for online server with planned outage: 192.168.0.70:4000 online true");
	}
}
