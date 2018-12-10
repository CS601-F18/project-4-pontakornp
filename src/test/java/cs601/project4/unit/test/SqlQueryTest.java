package cs601.project4.unit.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import cs601.project4.DatabaseManager;
import cs601.project4.TicketManagementApplicationLogger;
import cs601.project4.object.Event;
import cs601.project4.object.Ticket;

/**
 * 
 * @author pontakornp
 *
 *
 * Performs unit test by calling methods that run SQL command on the database
 */
public class SqlQueryTest {
	private static DatabaseManager dbInstance;
	
	@BeforeClass
	public static void initializeLogger() {
		TicketManagementApplicationLogger.initialize(SqlQueryTest.class.getName(), "Test.txt");
		dbInstance = DatabaseManager.getInstance();
	}
	
	@Test
	public void testInsertUser() {
		String username = "testInsertUser01";
		int userId = dbInstance.insertUser(username);
		assertTrue(userId != -1);
		if(userId != -1) {
			dbInstance.deleteUser(userId);
		}
	}
	
	@Test
	public void testSelectUser() {
		int userId = 1;
		String username = dbInstance.selectUser(userId);
		assertEquals("hello", username);
	}
	
	@Test
	public void selectUserEventId() {
		String username = "testSelectUserEventId01";
		int userId = dbInstance.insertUser(username);
		//add this after write SQL for event service - create event
		Ticket ticket = new Ticket();
		ticket.setEventId(1);
		ticket.setUserId(userId);
		int numTickets = 2;
		dbInstance.insertTickets(ticket, numTickets);
		List<Integer> eventIds = dbInstance.selectUserEventId(userId);
		assertTrue(eventIds.size() == numTickets);
		dbInstance.deleteUser(userId); // need to move to test class
		//delete event
		dbInstance.deleteTickets(ticket, numTickets); // need to move to test class
	}
	
	@Test
	public void testInsertTickets() {
		Ticket ticket = new Ticket();
		ticket.setEventId(1);
		ticket.setUserId(1);
		int numTickets = 2;
		boolean areTicketsInserted = dbInstance.insertTickets(ticket, numTickets);
		assertTrue(areTicketsInserted);
		if(areTicketsInserted) {
			dbInstance.deleteTickets(ticket, numTickets);
		}
	}
	
	@Test
	public void testUpdateTickets() {
		String senderUsername = "testUpdateTicket01";
		String targetUsername = "testUpdateTicket02";
		int senderId = dbInstance.insertUser(senderUsername);
		int targetUserId = dbInstance.insertUser(targetUsername);
		int eventId = 1;
		int numTickets = 2;
		Ticket ticket = new Ticket();
		ticket.setEventId(eventId);
		ticket.setUserId(senderId);
		dbInstance.insertTickets(ticket, numTickets);
		boolean areTicketsUpdated = dbInstance.updateTickets(senderId, targetUserId, eventId, numTickets);
		assertTrue(areTicketsUpdated);
		dbInstance.deleteUser(senderId);
		dbInstance.deleteUser(targetUserId);
		ticket.setUserId(targetUserId);
		dbInstance.deleteTickets(ticket, numTickets);
	}
	
	@Test
	public void testInsertEvent() {
		Event event = new Event();
		event.setEventName("testInsertEvent01");
		event.setUserId(1);
		event.setNumTicketAvail(5);
		event.setNumTicketPurchased(0);
		int eventId = dbInstance.insertEvent(event);
		assertTrue(eventId != -1);
		dbInstance.deleteEvent(eventId);
	}
	
	@Test
	public void testSelectEvents() {
		List<Event> list = dbInstance.selectEvents();
		assertTrue(list != null);
	}
	
	@Test
	public void testSelectEvent() {
		Event event = dbInstance.selectEvent(1);
		assertTrue(event != null);
	}
	
	@Test
	public void testUpdateEvent() {
		Event event = new Event();
		event.setEventId(2);
		event.setNumTicketAvail(3);
		event.setNumTicketPurchased(1);
		boolean isEventUpdated = dbInstance.updateEvent(event);
		assertTrue(isEventUpdated);
	}
}
