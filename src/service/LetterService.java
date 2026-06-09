package service;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import data.Envelope;
import data.LetterReadResult;
import data.LocalDB;

public class LetterService {

    private static LocalDB db = LocalDB.getInstance();

    public static boolean sendLetter(String senderId, String receiverId, String content, PrivateKey senderPrivateKey) {

        PublicKey receiverPublicKey = db.findPublicKeyById(receiverId);

        if (receiverPublicKey == null) {
            return false; // 수신자가 존재하지 않는 경우 실패 반환
        }

        // 1. 일회성 AES 키 생성
        SecretKey secretKey = CryptoService.generateAESKey();

        // 2. 본문 암호화 및 letters/ 폴더에 개별 파일로 저장
        String filePath = CryptoService.encryptLetterAndSave(content, secretKey, senderId, receiverId);

        // 3. 수신자 공개키로 일회성 AES 키 래핑 (암호화)
        byte[] encryptedSecretKey = CryptoService.wrapAESKey(secretKey, receiverPublicKey);

        try {
            // 4. 송신자 사설키로 편지 본문 서명 (SHA256withRSA)
            byte[] signature = CryptoService.signData(content.getBytes("UTF-8"), senderPrivateKey);

            // 5. 전자봉투를 DB 및 디스크에 영속화
            db.addEnvelope(new Envelope(senderId, receiverId, filePath, encryptedSecretKey, signature));
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static LetterReadResult readLetter(Envelope envelope, PrivateKey receiverPrivateKey) {
        // 1. 수신자 개인키로 래핑된 일회성 AES 비밀키 복구
        SecretKey secretKey = CryptoService.unwrapAESKey(envelope.getEncryptedAESKey(), receiverPrivateKey);

        // 2. 일회성 AES 비밀키로 암호화 파일 복호화
        String letter = CryptoService.decryptLetter(envelope.getEncryptedFilePath(), secretKey);
        if (letter == null) {
            return new LetterReadResult(null, false);
        }

        // 3. 송신자의 공개키 획득
        PublicKey senderPublicKey = db.findPublicKeyById(envelope.getSender());

        try {
            // 4. 송신자 공개키로 디지털 서명(SHA256withRSA) 검증
            boolean senderOK = CryptoService.verifySignature(letter.getBytes("UTF-8"), envelope.getSignature(),
                    senderPublicKey);
            return new LetterReadResult(letter, senderOK);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new LetterReadResult(letter, false);
        }
    }
}