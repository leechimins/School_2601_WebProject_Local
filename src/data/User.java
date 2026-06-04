package data;

public class User {
	private String id;
	private String hashedPassword;
	private byte[] passwordSalt;		// password 원문과 더해서 해싱 -> 사설키를 암호화 해서 encryptedPrivateKey를 만드는 용도
	private byte[] encodedPublicKey;	// 타인이 이 사용자에게 편지를 보낼 때 AES 키를 암호화하기 위한 용도
	private byte[] encryptedPrivateKey;	// 사용자의 패스워드 기반 대칭키로 암호화된 개인키 데이터
	
	public User(String id, String hashedPassword, byte[] passwordSalt, 
            byte[] publicKeyEncoded, byte[] encryptedPrivateKey) {
    this.id = id;
    this.hashedPassword = hashedPassword;
    
    this.passwordSalt = passwordSalt;
    this.encodedPublicKey = publicKeyEncoded;
    this.encryptedPrivateKey = encryptedPrivateKey;
    }

	public String getId() { return id; }

	public String getHashedPassword() { return hashedPassword; }

	public byte[] getPasswordSalt() { return passwordSalt; }

	public byte[] getPublicKeyEncoded() { return encodedPublicKey; }

	public byte[] getEncryptedPrivateKey() { return encryptedPrivateKey; }
}
