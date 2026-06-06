package data;

import java.io.Serializable;

public class Envelope implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String sender;
	private final String receiver;
	private final String encryptedFilePath;
	private final byte[] encryptedAESKey;
	private final byte[] signature;

	public Envelope(String sender, String receiver, String encryptedFilePath,
			byte[] encryptedAESKey, byte[] signature) {
		this.sender = sender;
		this.receiver = receiver;
		this.encryptedFilePath = encryptedFilePath;
		this.encryptedAESKey = encryptedAESKey;
		this.signature = signature;
	}

	public String getSender() {
		return sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public String getEncryptedFilePath() {
		return encryptedFilePath;
	}

	public byte[] getEncryptedAESKey() {
		return encryptedAESKey;
	}

	public byte[] getSignature() {
		return signature;
	}

}