package security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoService {

	private static final CryptoService instance = new CryptoService();

	private CryptoService() {
	}

	public static CryptoService getInstance() {
		return instance;
	}

	private static final String ALGORITHM_S = "AES";
	private static final int SIZE_S = 128;

	private static final String ALGORITHM_P = "RSA";
	private static final int SIZE_P = 1024;

	/*
	 * ========================================== A. 키 생성
	 * ==========================================
	 */

	// 1. RSA 키 쌍 생성
	public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM_P);
		keyGen.initialize(SIZE_P);
		KeyPair keyPair = keyGen.generateKeyPair();
		return keyPair;
	}

	// 2. AES 비밀키 생성
	public SecretKey generateAESKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM_S);
		keyGen.init(SIZE_S);
		SecretKey secretKey = keyGen.generateKey();
		return secretKey;
	}

	/*
	 * ========================================== B. 대칭키(AES) 암·복호화 (편지 본문용)
	 * ==========================================
	 */

	// 3. 편지 평문 본문을 일회성 AES 비밀키를 사용하여 암호화합니다.
	public String encryptDiaryAndSave(String plainText, SecretKey secretKey)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException {

		Cipher cipher = Cipher.getInstance(ALGORITHM_S);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		byte[] byteText = plainText.getBytes("UTF-8");
		byte[] encryptedBytes = cipher.doFinal(byteText);

		String fullPath = "data/diaries/" + System.currentTimeMillis() + ".txt";

		try (FileOutputStream fos = new FileOutputStream(fullPath)) {
			fos.write(encryptedBytes);
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fullPath;
	}

	// 4. 암호화된 편지를 수신자의 복구된 AES 비밀키로 복호화합니다.
	public String decryptLetter(String fname, SecretKey secretKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance(ALGORITHM_S);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		try (FileInputStream fis = new FileInputStream(fname);
				CipherInputStream cis = new CipherInputStream(fis, cipher);
				Scanner c = new Scanner(cis)) {
			String decryted = new String();
			while (c.hasNext()) {
				decryted += c.nextLine();
				decryted += "\n";
			}
			return decryted;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * ========================================== C. 비대칭키(RSA) 암·복호화 (전자봉투 패키징용)
	 * ==========================================
	 */

	// 5. [전자봉투 생성] 일회성 AES 비밀키 객체 자체를 수신자의 RSA 공개키로 암호화합니다.
	public byte[] wrapAESKey(SecretKey secretKey, PublicKey receiverPublicKey) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, receiverPublicKey);

		byte[] keyBytes = secretKey.getEncoded();
		byte[] encryptedSecretKey = cipher.doFinal(keyBytes);

		return encryptedSecretKey;
	}

	// 6. [전자봉투 해독] RSA로 암호화된 AES 비밀키 바이너리를 수신자의 RSA 사설키로 복호화하여 객체로 복구합니다.
	public SecretKey unwrapAESKey(byte[] encryptedSecretKey, PrivateKey receiverPrivateKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, receiverPrivateKey);

		byte[] decryptedKeyBytes = cipher.doFinal(encryptedSecretKey);
		// 수업 시간에 배우지 않은 코드, 교수님께 질문 필요
		SecretKey secretKey = new SecretKeySpec(decryptedKeyBytes, "AES");

		return secretKey;
	}

	/*
	 * ========================================== D. 패스워드 기반 사설키 보호 (사용자 인증 및 키 복구용)
	 * ==========================================
	 */

	/**
	 * 5. [회원가입] 발급된 사용자의 RSA 사설키를 패스워드 기반 대칭키로 암호화합니다. (User 엔티티 내의
	 * encryptedPrivateKey에 저장될 데이터를 생성합니다)
	 *
	 * @param privateKey 보호할 사용자의 RSA 사설키 객체
	 * @param password   사용자가 입력한 패스워드 원문
	 * @param salt       암호화 키 유도를 위한 솔트
	 * @param iv         대칭 암호화용 IV
	 * @return 암호화된 사설키 바이너리
	 */
	// 5. [회원가입] 발급된 사용자의 RSA 사설키를 패스워드 기반 대칭키로 암호화합니다.
	public byte[] encryptPrivateKey(PrivateKey privateKey, String password, byte[] salt) {
		// 1. 패스워드와 솔트를 결합하여 SHA-256 해시값 생성 (키 유도)
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(password.getBytes("UTF-8"));
		md.update(salt);
		byte[] hashedKeyBytes = md.digest();

		// 2. 해시된 바이트 배열에서 AES-128 규격에 맞게 16바이트(128비트) 추출하여 비밀키 생성
		byte[] aesKeyBytes = new byte[SIZE_S / 8]; // 128 / 8 = 16 bytes
		System.arraycopy(hashedKeyBytes, 0, aesKeyBytes, 0, aesKeyBytes.length);
		SecretKeySpec passwordBasedKey = new SecretKeySpec(aesKeyBytes, ALGORITHM_S);

		// 3. IV를 사용하지 않으므로 AES 모드로 Cipher 초기화
		Cipher cipher = Cipher.getInstance(ALGORITHM_S);
		cipher.init(Cipher.ENCRYPT_MODE, passwordBasedKey);

		// 4. RSA 사설키 객체의 인코딩된 바이너리 데이터를 획득하여 암호화 수행
		byte[] privateKeyBytes = privateKey.getEncoded();
		byte[] encryptedPrivateKey = cipher.doFinal(privateKeyBytes);

		return encryptedPrivateKey;
	}

	/**
	 * 6. [로그인 성공 시] 가상 DB에 저장되어 있던 암호화된 사설키 바이너리를 패스워드 기반 대칭키로 복호화하여 복구합니다.
	 *
	 * @param encryptedPrivateKey 암호화된 사설키 바이너리 데이터
	 * @param password            사용자가 입력한 패스워드 원문
	 * @param salt                유저 엔티티에 저장되어 있던 솔트
	 * @param iv                  유저 엔티티에 저장되어 있던 IV
	 * @return 복구된 자바 PrivateKey 객체 컨텍스트
	 */
	public PrivateKey decryptPrivateKey(byte[] encryptedPrivateKey, String password, byte[] salt, byte[] iv)
			throws Exception {
		// 본문 구현 예정
		return null;
	}

	// ==========================================
	// E. 전자서명 생성 및 검증 (부인 방지용)
	// ==========================================

	/**
	 * 7. 편지 내용(또는 암호문)에 대해 송신자의 RSA 사설키로 디지털 서명을 생성합니다.
	 *
	 * @param data             서명할 데이터 (평문 또는 암호화된 파일 경로/바이너리)
	 * @param senderPrivateKey 송신자의 RSA 사설키 객체
	 * @return 생성된 전자서명 바이너리 (Envelope의 digitalSignature 필드용)
	 */
	public byte[] signData(byte[] data, PrivateKey senderPrivateKey) throws Exception {
		// 본문 구현 예정
		// Signature 클래스 인스턴스를 활용한 서명 처리
		return null;
	}

	/**
	 * 8. 수신 측에서 해당 편지가 변조되지 않았는지 송신자의 RSA 공개키로 서명을 검증합니다.
	 *
	 * @param data            원본 데이터
	 * @param signature       검증할 전자서명 바이너리
	 * @param senderPublicKey 송신자의 RSA 공개키 객체
	 * @return 검증 성공 여부 (true/false)
	 */
	public boolean verifySignature(byte[] data, byte[] signature, PublicKey senderPublicKey) throws Exception {
		// 본문 구현 예정
		return false;
	}

}
