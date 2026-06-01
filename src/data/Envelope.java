package data;

public class Envelope {
	private final String sender;
	public String receiver;
	public final String encryptedFilePath;
	private final byte[] encryptedAESKey;
	private final byte[] digitalSignature;

	public Envelope(String sender, String receiver, String encryptedFilePath,
			byte[] encryptedAESKey, byte[] digitalSignature) {
		this.sender = sender;
		this.receiver = receiver;
		this.encryptedFilePath = encryptedFilePath;

		this.encryptedAESKey = encryptedAESKey;
		this.digitalSignature = digitalSignature;
	}
}