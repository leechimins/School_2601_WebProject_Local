package service;

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
        SecretKey secretKey = CryptoService.generateAESKey();
        String filePath = CryptoService.encryptLetterAndSave(content, secretKey, senderId, receiverId);
        byte[] encryptedSecretKey = CryptoService.wrapAESKey(secretKey, receiverPublicKey);
        byte[] signature = CryptoService.signData(content.getBytes(), senderPrivateKey);
        db.addEnvelope(new Envelope(senderId, receiverId, filePath, encryptedSecretKey, signature));
        return true;
    }

    public static LetterReadResult readLetter(Envelope envelope, PrivateKey receiverPrivateKey) {
        SecretKey secretKey = CryptoService.unwrapAESKey(envelope.getEncryptedAESKey(), receiverPrivateKey);
        String letter = CryptoService.decryptLetter(envelope.getEncryptedFilePath(), secretKey);
        PublicKey senderPublicKey = db.findPublicKeyById(envelope.getSender());
        boolean senderOK = CryptoService.verifySignature(letter.getBytes(), envelope.getSignature(), senderPublicKey);
        return new LetterReadResult(letter, senderOK);
    }

}