package data;

import java.io.Serializable;
import java.security.PublicKey;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private byte[] hashedPassword;
	private byte[] passwordSalt;
	private PublicKey publicKey;
	private byte[] encryptedPrivateKey;

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
