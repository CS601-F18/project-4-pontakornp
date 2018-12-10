package cs601.project4.object;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author pontakornp
 *
 *
 * Getter and setter class for user object
 */
public class User {
	@SerializedName("userid")
	private int userId;
	
	@SerializedName("username")
	private String username;
	
	public User() {
		
	}
	
	public User(int userId, String username) {
		this.userId = userId;
		this.username = username;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
