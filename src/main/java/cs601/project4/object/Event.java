package cs601.project4.object;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author pontakornp
 *
 *
 * Getter and setter class for event object
 */
public class Event {
	@SerializedName("eventid")
	private int eventId;
	
	@SerializedName("eventname")
	private String eventName;
	
	@SerializedName("userid")
	private int userId;
	
	@SerializedName("numtickets")
	private int numTickets;
	
	@SerializedName("avail")
	private int numTicketAvail;
	
	@SerializedName("purchased")
	private int numTicketPurchased;
	
	public Event() {
		
	}
	
	public Event(int eventId, String eventName, int userId, int numTickets, int numTicketAvail, int numTicketPurchased) {
		this.eventId = eventId;
		this.eventName = eventName;
		this.userId = userId;
		this.numTickets = numTickets;
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
	
	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getNumTickets() {
		return numTickets;
	}

	public void setNumTickets(int numTickets) {
		this.numTickets = numTickets;
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
