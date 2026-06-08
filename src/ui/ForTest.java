package ui;

import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import data.Envelope;
import data.LetterReadResult;
import data.LocalDB;
import service.AuthService;
import service.LetterService;

public class ForTest {
    public static void main(String[] args) {
        System.out.println("========== Exchange Diary System Integration Test ==========");

        // 0. Clean database files first if they exist to run from a clean state
        File userDb = new File("data/users.dat");
        File envDb = new File("data/envelope.dat");
        if (userDb.exists()) userDb.delete();
        if (envDb.exists()) envDb.delete();

        // Also clean letters directory if any
        File lettersDir = new File("data/letters");
        if (lettersDir.exists()) {
            File[] files = lettersDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }

        // Re-initialize/fetch LocalDB instance
        LocalDB db = LocalDB.getInstance();
        AuthService authService = AuthService.getInstance();

        // 1. Register Alice
        System.out.println("[Test] Registering user 'alice'...");
        PrivateKey alicePrivateKey = authService.register("alice", "password123");
        if (alicePrivateKey == null) {
            System.err.println("[Fail] Alice registration failed.");
            System.exit(1);
        }
        System.out.println("[Success] Alice registered successfully.");

        // 2. Register Bob
        System.out.println("[Test] Registering user 'bob'...");
        PrivateKey bobPrivateKey = authService.register("bob", "bobpassword");
        if (bobPrivateKey == null) {
            System.err.println("[Fail] Bob registration failed.");
            System.exit(1);
        }
        System.out.println("[Success] Bob registered successfully.");

        // 3. Verify user existence
        if (!authService.isUserExists("alice")) {
            System.err.println("[Fail] Alice user not found in database.");
            System.exit(1);
        }
        if (!authService.isUserExists("bob")) {
            System.err.println("[Fail] Bob user not found in database.");
            System.exit(1);
        }

        // 4. Test login with wrong password
        System.out.println("[Test] Testing login for 'alice' with WRONG password...");
        PrivateKey loginFailKey = authService.login("alice", "wrongpassword");
        if (loginFailKey != null) {
            System.err.println("[Fail] Login succeeded with wrong password! Security vulnerability!");
            System.exit(1);
        }
        System.out.println("[Success] Login with wrong password failed as expected.");

        // 5. Test login with correct password
        System.out.println("[Test] Testing login for 'alice' with correct password...");
        PrivateKey aliceLoginKey = authService.login("alice", "password123");
        if (aliceLoginKey == null) {
            System.err.println("[Fail] Alice login failed with correct password.");
            System.exit(1);
        }
        System.out.println("[Success] Alice logged in successfully.");

        // 6. Alice sends a secret letter to Bob
        String letterContent = "Hello Bob! This is a super secret diary entry. 1234!@#$";
        System.out.println("[Test] Alice sending letter to Bob...");
        boolean sendSuccess = LetterService.sendLetter("alice", "bob", letterContent, aliceLoginKey);
        if (!sendSuccess) {
            System.err.println("[Fail] Alice failed to send letter to Bob.");
            System.exit(1);
        }
        System.out.println("[Success] Letter sent successfully.");

        // 7. Check Bob's inbox
        System.out.println("[Test] Checking Bob's inbox...");
        ArrayList<Envelope> bobEnvelopes = db.findEnvelopesByReceiver("bob");
        if (bobEnvelopes.isEmpty()) {
            System.err.println("[Fail] Bob's inbox is empty.");
            System.exit(1);
        }
        System.out.println("[Success] Found " + bobEnvelopes.size() + " letter(s) in Bob's inbox.");
        Envelope env = bobEnvelopes.get(0);

        // 8. Bob logs in and decrypts/reads the letter
        System.out.println("[Test] Bob logging in to read the letter...");
        PrivateKey bobLoginKey = authService.login("bob", "bobpassword");
        if (bobLoginKey == null) {
            System.err.println("[Fail] Bob login failed.");
            System.exit(1);
        }
        
        System.out.println("[Test] Bob reading the letter...");
        LetterReadResult readResult = LetterService.readLetter(env, bobLoginKey);
        if (readResult.getContent() == null) {
            System.err.println("[Fail] Failed to decrypt or read the letter.");
            System.exit(1);
        }
        System.out.println("[Success] Decrypted content: " + readResult.getContent());

        if (!readResult.isSignatureVerified()) {
            System.err.println("[Fail] Alice's signature verification failed.");
            System.exit(1);
        }
        System.out.println("[Success] Alice's signature verified successfully.");

        if (!readResult.getContent().equals(letterContent)) {
            System.err.println("[Fail] Decrypted content does not match original content.");
            System.exit(1);
        }
        System.out.println("[Success] Decrypted content matches the original text perfectly!");

        System.out.println("\n========== All Integration Tests Passed Successfully! ==========");
    }
}