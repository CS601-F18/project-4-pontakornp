package cs601.project4.database;

public class Ticket {
	private int ticketid;
	private int eventid;
	private int userid;
	
	public Ticket() {
		
	}
	
	public Ticket(int ticketid, int eventid, int userid) {
		this.ticketid = ticketid;
		this.eventid = eventid;
		this.userid = userid;
	}

	public int getTicketid() {
		return ticketid;
	}

	public void setTicketid(int ticketid) {
		this.ticketid = ticketid;
	}

	public int getEventid() {
		return eventid;
	}

	public void setEventid(int eventid) {
		this.eventid = eventid;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}
}
