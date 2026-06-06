package data;

import java.io.Serializable;
import java.security.PublicKey;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private byte[] hashedPassword;
	private byte[] passwordSalt; // password 원문과 더해서 해싱 -> 사설키를 암호화 해서 encryptedPrivateKey를 만드는 용도
	private PublicKey publicKey; // 타인이 이 사용자에게 편지를 보낼 때 AES 키를 암호화하기 위한 용도
	private byte[] encryptedPrivateKey; // 사용자의 패스워드 기반 대칭키로 암호화된 개인키 데이터

	public User(String id, byte[] hashedPassword, byte[] passwordSalt,
			PublicKey publicKey, byte[] encryptedPrivateKey) {
		this.id = id;
		this.hashedPassword = hashedPassword;

		this.passwordSalt = passwordSalt;
		this.publicKey = publicKey;
		this.encryptedPrivateKey = encryptedPrivateKey;
	}

	public String getId() {
		return id;
	}

	public byte[] getHashedPassword() {
		return hashedPassword;
	}

	public byte[] getPasswordSalt() {
		return passwordSalt;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public byte[] getEncryptedPrivateKey() {
		return encryptedPrivateKey;
	}
}
