package security;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

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

    public boolean register(String id, String password) {
        byte[] passwordSalt = new byte[16];
        SecureRandom rd = new SecureRandom();
        rd.nextBytes(passwordSalt);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance(ALGORITHM_HASH);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        md.update(password.getBytes());
        md.update(passwordSalt); // [보고서용 허점] 해시할 때 Salt를 적용할 것 (완료)
        byte[] hashedPassword = md.digest();

        KeyPair keyPair = CryptoService.generateRSAKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        byte[] encryptedPrivateKey = CryptoService.encryptPrivateKey(privateKey, password, passwordSalt);

        User user = new User(id, hashedPassword, passwordSalt, publicKey, encryptedPrivateKey);
        db.addUser(user);

        return true;
    }

    public PrivateKey login(String id, String password) {
        User user = db.findUserById(id);
        if (user == null) {
            return null;
        }
        // 여기서 해시 검증 및 패스워드 기반 사설키 복구 로직 수행
        if (verifyPassword(password, user)) {
            PrivateKey result = CryptoService.decryptPrivateKey(user, password);
            return result;
        }
        return null;
    }

    private boolean verifyPassword(String password, User user) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_HASH);
            md.update(password.getBytes());
            md.update(user.getPasswordSalt());
            byte[] hashedPassword = md.digest();
            if (MessageDigest.isEqual(user.getHashedPassword(), hashedPassword)) {
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }
}