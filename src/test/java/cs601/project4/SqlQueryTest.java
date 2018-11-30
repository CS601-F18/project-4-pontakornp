package cs601.project4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cs601.project4.database.DatabaseManager;
import cs601.project4.database.Ticket;
import cs601.project4.database.User;

public class SqlQueryTest {
	private static DatabaseManager dbInstance;
	
	@BeforeClass
	public static void initializeLogger() {
		TicketPurchaseApplicationLogger.initialize(SqlQueryTest.class.getName(), "Test.txt");
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
		User user = new User();
		user.setUserId(userId);
		String username = dbInstance.selectUser(userId);
		assertEquals("hello", username);
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
}
