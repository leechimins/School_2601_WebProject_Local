package data;

public class User {
	public String id;
	private String hashedPassword;
	private byte[] passwordSalt;
	private byte[] publicKeyEncoded;	// 타인이 이 사용자에게 편지를 보낼 때 AES 키를 암호화하기 위한 용도
	private byte[] encryptedPrivateKey;	// 사용자의 패스워드 기반 대칭키로 암호화된 개인키 데이터
	
	public User(String id, String hashedPassword, byte[] passwordSalt, 
            byte[] publicKeyEncoded, byte[] encryptedPrivateKey) {
    this.id = id;
    this.hashedPassword = hashedPassword;
    
    this.passwordSalt = passwordSalt;
    this.publicKeyEncoded = publicKeyEncoded;
    this.encryptedPrivateKey = encryptedPrivateKey;
    }
	
}
