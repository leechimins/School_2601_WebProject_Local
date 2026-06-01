package security;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyManager {
	// 미사용 (CryptoService로 통합)
	/*
    private static final String ALGORITHM_S = "AES";
    private static final int SIZE_S = 128;
    
    private static final String ALGORITHM_P = "RSA";
    private static final int SIZE_P = 1024;
	
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
    }*/
}