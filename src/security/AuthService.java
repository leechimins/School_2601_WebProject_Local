package security;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

import data.LocalDB;
import data.User;

public class AuthService {
    private static final AuthService instance = new AuthService();
    private static final LocalDB db = LocalDB.getInstance();

    private static final String ALGORITHM_HASH = "MD5";

    private AuthService() {
    }

    public static AuthService getInstance() {
        return instance;
    }

    public boolean isUserExists(String id) {
        return db.findUserById(id) != null;
    }

    public boolean register(String id, String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM_HASH);
        md.update(password.getBytes());
        byte[] hashedPassword = md.digest();

        byte[] passwordSalt = new byte[16];
        SecureRandom rd = new SecureRandom();
        rd.nextBytes(passwordSalt);

        KeyPair keyPair = CryptoService.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        byte[] encryptedPrivateKey = CryptoService.encryptPrivateKey(privateKey, password, passwordSalt);

        User user = new User(id, hashedPassword, passwordSalt, publicKey, encryptedPrivateKey);
        db.addUser(user);

        return true;
    }

    public boolean login(String id, String password) {
        User user = db.findUserById(id);
        if (user == null) {
            return false;
        }
        // 여기서 해시 검증 및 패스워드 기반 사설키 복구 로직 수행
        return verifyPassword(password, user);
    }

    private boolean verifyPassword(String password, User user) {
        // 해시 및 솔트 검증 로직 구현부
        return true;
    }
}