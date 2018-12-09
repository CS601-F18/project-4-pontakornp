package cs601.project4.object;

public class Ticket {
	private int ticketId;
	private int eventId;
	private int userId;
	
	public Ticket() {
		
	}
	
	public Ticket(int ticketId, int eventId, int userId) {
		this.ticketId = ticketId;
		this.eventId = eventId;
		this.userId = userId;
	}

	public int getTicketId() {
		return ticketId;
	}

	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}
