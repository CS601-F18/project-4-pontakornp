package cs601.project4.database;

public class Event {
	private int eventid;
	private String eventname;
	private int avail;
	private int purchased;
	
	public Event() {
		
	}
	
	public Event(int eventid, String eventname, int avail, int purchased) {
		this.setEventid(eventid);
		this.setEventname(eventname);
		this.setAvail(avail);
		this.setPurchased(purchased);
	}

	public int getEventid() {
		return eventid;
	}

	public void setEventid(int eventid) {
		this.eventid = eventid;
	}

	public String getEventname() {
		return eventname;
	}

	public void setEventname(String eventname) {
		this.eventname = eventname;
	}

	public int getAvail() {
		return avail;
	}

	public void setAvail(int avail) {
		this.avail = avail;
	}

	public int getPurchased() {
		return purchased;
	}

	public void setPurchased(int purchased) {
		this.purchased = purchased;
	}
	
}
