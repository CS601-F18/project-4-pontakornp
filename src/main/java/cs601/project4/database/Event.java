package cs601.project4.database;

public class Event {
	private int eventId;
	private String eventName;
	private int numTicketAvail;
	private int numTicketPurchased;
	
	public Event() {
		
	}
	
	public Event(int eventId, String eventName, int numTicketAvail, int numTicketPurchased) {
		this.eventId = eventId;
		this.eventName = eventName;
		this.numTicketAvail = numTicketAvail;
		this.numTicketPurchased = numTicketPurchased;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public int getNumTicketAvail() {
		return numTicketAvail;
	}

	public void setNumTicketAvail(int numTicketAvail) {
		this.numTicketAvail = numTicketAvail;
	}

	public int getNumTicketPurchased() {
		return numTicketPurchased;
	}

	public void setNumTicketPurchased(int numTicketPurchased) {
		this.numTicketPurchased = numTicketPurchased;
	}
	
}
