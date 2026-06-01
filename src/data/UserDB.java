package data;

public class UserDB {
	
	private static final UserDB instance = new UserDB();
	private UserDB() { }
	public static UserDB getInstance() {
		return instance;
	}
	
	public User findById(String id) {
		return new User("", "", null, null, null);
	}
	
}