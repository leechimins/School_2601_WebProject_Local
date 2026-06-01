package security;

import data.UserDB;
import data.User;

public class AuthService {
    private static final AuthService instance = new AuthService();
    private final UserDB userDB = UserDB.getInstance();

    private AuthService() { }

    public static AuthService getInstance() {
        return instance;
    }

    public boolean register(String id, String password) {
    	return true;
    }
    
    
    public boolean login(String id, String password) {
        User user = userDB.findById(id);
        if (user == null) {
        	return register(id, password);
        }
        // 여기서 해시 검증 및 패스워드 기반 사설키 복구 로직 수행
        return verifyPassword(password, user);
    }
    
    private boolean verifyPassword(String password, User user) {
        // 해시 및 솔트 검증 로직 구현부
        return true;
    }
}
