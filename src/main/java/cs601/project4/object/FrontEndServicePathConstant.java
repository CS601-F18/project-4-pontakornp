package cs601.project4.object;

/**
 * 
 * @author pontakornp
 *
 *
 * Contains contant path of Frontend Service API
 */
public class FrontEndServicePathConstant {
	public static final String GET_EVENT_LIST_PATH = "/events";
	public static final String POST_CREATE_EVENT_PATH = "/events/create";
	public static final String GET_EVENT_DETAILS_PATH = "/events/%d";
	public static final String POST_PURCHASE_TICKETS_PATH = "/events/%d/purchase/%d";
	public static final String POST_CREATE_USER_PATH = "/users/create";
	public static final String GET_USER_DETAILS_PATH = "/users/%d";
	public static final String POST_TRANSFER_TICKETS_PATH = "/users/%d/tickets/transfer";
}
