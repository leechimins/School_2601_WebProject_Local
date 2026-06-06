package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class LocalDB {

    private static final LocalDB instance = new LocalDB();

    private static final String USER_DB_PATH = "data/users.dat";
    private static final String ENVELOPE_DB_PATH = "data/envelope.dat";

    private HashMap<String, User> userMap = new HashMap<>();
    private HashMap<String, Envelope> envelopeMap = new HashMap<>();

    private LocalDB() {
        File file1 = new File(USER_DB_PATH);
        if (file1.getParentFile() != null && !file1.getParentFile().exists()) {
            file1.getParentFile().mkdirs();
        }
        File file2 = new File(ENVELOPE_DB_PATH);
        if (file2.getParentFile() != null && !file2.getParentFile().exists()) {
            file2.getParentFile().mkdirs();
        }

        loadAll();
    }

    public static LocalDB getInstance() {
        return instance;
    }

    /*
     * ==========================================
     * A. 사용자 관리
     * ==========================================
     */
    public User findUserById(String id) {
        return userMap.get(id);
    }

    public boolean addUser(User user) {
        userMap.put(user.getId(), user);
        return saveFile(USER_DB_PATH, userMap);
    }

    /*
     * ==========================================
     * B. 편지 관리
     * ==========================================
     */
    public ArrayList<Envelope> findEnvelopesByReceiver(String receiverId) {
        ArrayList<Envelope> result = new ArrayList<>();
        for (Envelope env : envelopeMap.values()) {
            if (env.getReceiver().equals(receiverId)) {
                result.add(env);
            }
        }
        return result;
    }

    public boolean addEnvelope(Envelope envelope) {
        envelopeMap.put(envelope.getEncryptedFilePath(), envelope);
        return saveFile(ENVELOPE_DB_PATH, envelopeMap);
    }

    /*
     * ==========================================
     * C. 파일 입출력
     * ==========================================
     */
    private boolean saveFile(String filePath, Object data) {
        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(data);
            oos.flush();
            return true;
        } catch (IOException e) {
            System.err.println("[오류] 파일 저장 실패 (" + filePath + "): " + e.getMessage());
            return false;
        }
    }

    private void loadAll() {
        // 1. 유저 로드
        File userFile = new File(USER_DB_PATH);
        if (userFile.exists()) {
            try (FileInputStream fis = new FileInputStream(userFile);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                this.userMap = (HashMap<String, User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("[오류] 유저 데이터 로드 중 문제 발생. 새로 빈 맵을 시작합니다.");
                this.userMap = new HashMap<>();
            }
        } else {
            this.userMap = new HashMap<>();
        }

        // 2. 편지 봉투 로드
        File envFile = new File(ENVELOPE_DB_PATH);
        if (envFile.exists()) {
            try (FileInputStream fis = new FileInputStream(envFile);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                this.envelopeMap = (HashMap<String, Envelope>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("[오류] 편지 데이터 로드 중 문제 발생. 새로 빈 맵을 시작합니다.");
                this.envelopeMap = new HashMap<>();
            }
        } else {
            this.envelopeMap = new HashMap<>();
        }
    }

}