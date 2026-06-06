package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class UserDB {
	
	// 싱글톤 패턴
	private static final UserDB instance = new UserDB();
	private UserDB() {
		loadDB();
	}
	public static UserDB getInstance() {
		return instance;
	}
	
    private static final String DB_FILE_PATH = "data/users.dat";
	// 유저 데이터를 메모리 상에서 관리할 Map 구조
    private HashMap<String, User> userMap = new HashMap<>();
	
    // 접근자
    public User findById(String id) {
        return userMap.get(id);
    }
    
    // 관리용 함수
    public boolean addUser(User user) {
    	userMap.put(user.getId(), user);
    	File file = new File(DB_FILE_PATH);
    	try (FileOutputStream fos = new FileOutputStream(file);
    			ObjectOutputStream oos = new ObjectOutputStream(fos)) {
    		oos.writeObject(userMap);
    		oos.flush();
    		return true;
		} catch (IOException e) {
			e.printStackTrace();
			userMap.remove(user.getId());
			return false;
		}
    }
	
	private void loadDB() {
		File file = new File(DB_FILE_PATH);
		try (FileInputStream fis = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			Object obj = ois.readObject();
			this.userMap = (HashMap<String, User>) obj;
		} catch (FileNotFoundException e) {
			System.out.println("[시스템 정보] 초기 저장된 유저 데이터가 없어 새로 시작합니다.");
			this.userMap = new HashMap<>();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}