package cs601.project4.database;

import com.google.gson.annotations.SerializedName;

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