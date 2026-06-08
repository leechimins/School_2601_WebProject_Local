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

    public static boolean sendLetter(String senderId, String receiverId,
            String content, PrivateKey senderPrivateKey) {

        PublicKey receiverPublicKey = db.findPublicKeyById(receiverId);

        if (receiverPublicKey == null) {
            return false; // 수신자가 존재하지 않는 경우 실패 반환
        }

        SecretKey secretKey = CryptoService.generateAESKey();
        String filePath = CryptoService.encryptLetterAndSave(content, secretKey, senderId, receiverId);
        byte[] encryptedSecretKey = CryptoService.wrapAESKey(secretKey, receiverPublicKey);
        try {
            byte[] signature = CryptoService.signData(content.getBytes("UTF-8"), senderPrivateKey);
            db.addEnvelope(new Envelope(senderId, receiverId, filePath, encryptedSecretKey, signature));
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static LetterReadResult readLetter(Envelope envelope, PrivateKey receiverPrivateKey) {
        SecretKey secretKey = CryptoService.unwrapAESKey(envelope.getEncryptedAESKey(), receiverPrivateKey);
        String letter = CryptoService.decryptLetter(envelope.getEncryptedFilePath(), secretKey);
        if (letter == null) {
            return new LetterReadResult(null, false);
        }
        PublicKey senderPublicKey = db.findPublicKeyById(envelope.getSender());
        try {
            boolean senderOK = CryptoService.verifySignature(letter.getBytes("UTF-8"), envelope.getSignature(),
                    senderPublicKey);
            return new LetterReadResult(letter, senderOK);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new LetterReadResult(letter, false);
        }
    }

}