package security;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import data.User;

public class CryptoService {

	private CryptoService() {
	}

	private static final String ALGORITHM_S = "AES";
	private static final int SIZE_S = 128;

	private static final String ALGORITHM_P = "RSA";
	private static final int SIZE_P = 1024;

	private static final String ALGORITHM_SIGN = "SHA1withRSA";
	private static final String ALGORITHM_ENVELOPE = "RSA";
	private static final String ALGORITHM_HASH = "MD5";

	/*
	 * ==========================================
	 * A. 키 생성
	 * ==========================================
	 */

	// 1. RSA 키 쌍 생성
	public static KeyPair generateRSAKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM_P);
			keyGen.initialize(SIZE_P);
			KeyPair keyPair = keyGen.generateKeyPair();
			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 2. AES 비밀키 생성
	public static SecretKey generateAESKey() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM_S);
			keyGen.init(SIZE_S);
			SecretKey secretKey = keyGen.generateKey();
			return secretKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * ==========================================
	 * B. 대칭키(AES) 암·복호화 (편지 본문용)
	 * ==========================================
	 */

	// 3. 편지 평문 본문을 일회성 AES 비밀키를 사용하여 암호화합니다.
	public static String encryptDiaryAndSave(String plainText, SecretKey secretKey, String sender, String reciver) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_S);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			byte[] byteText = plainText.getBytes("UTF-8");
			byte[] encryptedBytes = cipher.doFinal(byteText);

			String fullPath = "data/diaries/" + sender + "_" + reciver + "_" + System.currentTimeMillis() + ".txt";

			try (FileOutputStream fos = new FileOutputStream(fullPath)) {
				fos.write(encryptedBytes);
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return fullPath;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | UnsupportedEncodingException
				| IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 4. 암호화된 편지를 수신자의 복구된 AES 비밀키로 복호화합니다.
	public static String decryptLetter(String fullPath, SecretKey secretKey) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_S);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			try (FileInputStream fis = new FileInputStream(fullPath);
					CipherInputStream cis = new CipherInputStream(fis, cipher);
					ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[1024];
				int bytesRead;

				while ((bytesRead = cis.read(buffer)) != -1) {
					bos.write(buffer, 0, bytesRead);
				}

				return bos.toString("UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * ==========================================
	 * C. 비대칭키(RSA) 암·복호화 (전자봉투 패키징용)
	 * ==========================================
	 */

	// 5. [전자봉투 생성] 일회성 AES 비밀키 객체 자체를 수신자의 RSA 공개키로 암호화합니다.
	public static byte[] wrapAESKey(SecretKey secretKey, PublicKey receiverPublicKey) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_ENVELOPE);
			cipher.init(Cipher.ENCRYPT_MODE, receiverPublicKey);

			byte[] keyBytes = secretKey.getEncoded();
			byte[] encryptedSecretKey = cipher.doFinal(keyBytes);

			return encryptedSecretKey;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 6. [전자봉투 해독] RSA로 암호화된 AES 비밀키 바이너리를 수신자의 RSA 사설키로 복호화하여 객체로 복구합니다.
	public static SecretKey unwrapAESKey(byte[] encryptedSecretKeyBytes, PrivateKey receiverPrivateKey) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_ENVELOPE);
			cipher.init(Cipher.DECRYPT_MODE, receiverPrivateKey);

			byte[] decryptedKeyBytes = cipher.doFinal(encryptedSecretKeyBytes);
			// 수업 시간에 배우지 않은 코드, 교수님께 질문 필요
			SecretKey secretKey = new SecretKeySpec(decryptedKeyBytes, "AES");

			return secretKey;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * ==========================================
	 * D. 패스워드 기반 사설키 보호 (사용자 인증 및 키 복구용)
	 * ==========================================
	 */

	// 7. [회원가입] 발급된 사용자의 RSA 사설키를 패스워드 기반 대칭키로 암호화합니다.
	public static byte[] encryptPrivateKey(PrivateKey privateKey, String password, byte[] passwordSalt) {
		try {
			// 1. 패스워드와 솔트를 결합하여 16Bytes 해시값 생성
			MessageDigest md = MessageDigest.getInstance(ALGORITHM_HASH);
			md.update(password.getBytes());
			md.update(passwordSalt);
			byte[] hashSource = md.digest();

			// 2. 비밀키 생성
			SecretKeySpec passwordBasedKey = new SecretKeySpec(hashSource, ALGORITHM_S);

			// 3. 비밀키로 사설키 암호화
			Cipher cipher = Cipher.getInstance(ALGORITHM_S);
			cipher.init(Cipher.ENCRYPT_MODE, passwordBasedKey);

			byte[] privateKeyBytes = privateKey.getEncoded();
			byte[] encryptedPrivateKey = cipher.doFinal(privateKeyBytes);

			return encryptedPrivateKey;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 8. [로그인 성공 시] DB에 저장되어 있던 암호화된 사설키 바이너리를 패스워드 기반 대칭키로 복호화하여 복구합니다.
	public static PrivateKey decryptPrivateKey(User user, String password) {
		try {
			// 1. 패스워드와 솔트를 결합하여 16Bytes 해시값 생성
			MessageDigest md = MessageDigest.getInstance(ALGORITHM_HASH);
			md.update(password.getBytes());
			md.update(user.getPasswordSalt());
			byte[] hashSource = md.digest();

			// 2. 비밀키 생성
			SecretKeySpec passwordBasedKey = new SecretKeySpec(hashSource, ALGORITHM_S);

			// 3. 비밀키로 사설키 복호화
			Cipher cipher = Cipher.getInstance(ALGORITHM_S);
			cipher.init(Cipher.DECRYPT_MODE, passwordBasedKey);

			byte[] decryptedKeyBytes = cipher.doFinal(user.getEncryptedPrivateKey());

			// 4. 복호화한 배열을 사설키로 복구
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_P);
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

			return privateKey;
		} catch (BadPaddingException | InvalidKeySpecException e) {
			// 패스워드가 틀린 경우
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * ==========================================
	 * E. 전자서명 생성 및 검증 (부인 방지용)
	 * ==========================================
	 */

	// 9. 편지 내용에 대해 송신자의 RSA 사설키로 디지털 서명을 생성합니다.
	public static byte[] signData(byte[] data, PrivateKey senderPrivateKey) {
		try {
			Signature sig = Signature.getInstance(ALGORITHM_SIGN);
			sig.initSign(senderPrivateKey);
			sig.update(data);
			byte[] signature = sig.sign();

			return signature;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 10. 수신 측에서 해당 편지가 변조되지 않았는지 송신자의 RSA 공개키로 서명을 검증합니다.
	public static boolean verifySignature(byte[] data, byte[] signature, PublicKey senderPublicKey) {
		try {
			Signature sig = Signature.getInstance(ALGORITHM_SIGN);
			sig.initVerify(senderPublicKey);
			sig.update(data);

			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			return false;
		}
	}

}
