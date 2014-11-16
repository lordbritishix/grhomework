package globalrelay.servicemonitor.poller;

public interface IPollListener {
	public enum Reason {
		SERVER_ONLINE,
		SERVER_OFFLINE,
		SERVER_NOT_RESPONDING,
		SERVER_ON_MAINTENANCE_MODE
	}
	
	public void pollComplete(String clientId, Reason reason);
}
