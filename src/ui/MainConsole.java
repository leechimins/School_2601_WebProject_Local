package ui;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Scanner;

import data.Envelope;
import data.LetterReadResult;
import data.LocalDB;
import data.User;
import service.AuthService;
import service.LetterService;

public class MainConsole {

    private static User currentUser = null;
    private static PrivateKey privateKey = null;
    private static final LocalDB db = LocalDB.getInstance();
    private static Envelope selectedEnvelope = null;

    enum Menu {
        AUTH(13), MAIN(10), WRITE(1), INBOX(2), READ(12), LOGOUT(3), EXIT(4);

        private final int value;

        Menu(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static Menu fromValue(int val) {
            for (Menu m : Menu.values()) {
                if (m.getValue() == val) {
                    return m;
                }
            }
            return MAIN;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Menu currentMenu = Menu.AUTH;

        ConsoleView.printStart();

        while (currentMenu != Menu.EXIT) {
            switch (currentMenu) {
                case AUTH:
                    currentUser = null;
                    privateKey = null;
                    currentMenu = auth(sc);
                    break;
                case MAIN:
                    ConsoleView.printMainMenu(currentUser.getId());
                    String choice = sc.nextLine().trim();
                    try {
                        currentMenu = Menu.fromValue(Integer.parseInt(choice));
                    } catch (NumberFormatException e) {
                        ConsoleView.printFail("잘못된 입력", "숫자를 입력해주세요.");
                        currentMenu = Menu.MAIN;
                    }
                    break;
                case WRITE:
                    currentMenu = write(sc);
                    break;
                case INBOX:
                    currentMenu = inbox(sc);
                    break;
                case READ:
                    currentMenu = read(sc);
                    break;
                case LOGOUT:
                    ConsoleView.printLogoutSuccess();
                    currentUser = null;
                    privateKey = null;
                    currentMenu = Menu.AUTH;
                    break;
                case EXIT:
                    currentMenu = Menu.EXIT;
                    break;
                default:
                    currentMenu = Menu.LOGOUT;
                    break;
            }
        }

        sc.close();
        ConsoleView.printExitSuccess();
    }

    private static Menu auth(Scanner sc) {
        AuthService authService = AuthService.getInstance();
        String id = null;
        String pw = null;

        do {
            ConsoleView.printAuthMenu(0); // "▶ 사용할 사용자 ID 입력: " 출력
            id = sc.nextLine().trim();
        } while (id.isEmpty());

        do {
            ConsoleView.printAuthMenu(1); // "▶ 비밀번호: " 출력
            pw = sc.nextLine().trim();
        } while (pw.isEmpty());

        if (authService.isUserExists(id)) {
            PrivateKey key = authService.login(id, pw);
            if (key != null) {
                ConsoleView.printLoginSuccess();
                currentUser = db.findUserById(id);
                privateKey = key; // 복구된 개인키 세션 저장
                return Menu.MAIN;
            }
            ConsoleView.printFail("로그인 실패", "비밀번호가 일치하지 않습니다.");
            return Menu.AUTH;
        }

        // 신규 유저인 경우 자동 회원가입 진행
        PrivateKey key = authService.register(id, pw);
        if (key != null) {
            ConsoleView.printRegisterSuccess();
            currentUser = db.findUserById(id);
            privateKey = key; // 복구된 개인키 세션 저장
            return Menu.MAIN;
        }
        ConsoleView.printFail("회원가입 실패", "회원가입 처리에 실패했습니다.");
        return Menu.AUTH;
    }

    private static Menu write(Scanner sc) {
        ConsoleView.printWritePrompt();
        String receiverId = sc.nextLine().trim();
        if (receiverId.isEmpty()) {
            return Menu.MAIN;
        }

        ConsoleView.printWriteContentPrompt();
        String content = sc.nextLine().trim();
        if (content.isEmpty()) {
            return Menu.MAIN;
        }

        boolean success = LetterService.sendLetter(currentUser.getId(), receiverId, content, privateKey);
        if (success) {
            ConsoleView.printWriteSuccess();
        } else {
            ConsoleView.printFail("편지 전송 실패", "수신자 ID가 존재하지 않거나 처리 중 오류가 발생했습니다.");
        }
        return Menu.MAIN;
    }

    private static Menu inbox(Scanner sc) {
        ArrayList<Envelope> envelopes = db.findEnvelopesByReceiver(currentUser.getId());
        boolean hasEnvelopes = ConsoleView.printInboxList(envelopes);
        if (!hasEnvelopes) {
            return Menu.MAIN;
        }

        String input = sc.nextLine().trim();
        if (input.equals("0")) {
            return Menu.MAIN;
        }

        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < envelopes.size()) {
                selectedEnvelope = envelopes.get(index);
                return Menu.READ;
            } else {
                ConsoleView.printFail("편지 번호 오류", "올바르지 않은 편지 번호입니다.");
            }
        } catch (NumberFormatException e) {
            ConsoleView.printFail("편지 번호 오류", "올바른 번호 형식을 입력해주세요.");
        }
        return Menu.MAIN;
    }

    private static Menu read(Scanner sc) {
        if (selectedEnvelope == null) {
            ConsoleView.printFail("편지 선택 오류", "선택된 편지가 없습니다.");
            return Menu.INBOX;
        }

        LetterReadResult result = LetterService.readLetter(selectedEnvelope, privateKey);
        if (result.getContent() == null) {
            ConsoleView.printFail("편지 읽기 실패", "편지 본문을 복호화할 수 없거나 파일이 손상되었습니다.");
            return Menu.INBOX;
        }

        if (!result.isSignatureVerified()) {
            ConsoleView.printFail("전자서명 검증 실패", "발신자 서명 검증에 실패했습니다! 본문이 변조되었을 위험이 있습니다.");
        }

        ConsoleView.printReadWorkspace(selectedEnvelope, result.getContent());

        String choice = sc.nextLine().trim().toUpperCase();
        switch (choice) {
            case "W":
                return Menu.WRITE;
            case "L":
                return Menu.INBOX;
            case "M":
                return Menu.MAIN;
            default:
                ConsoleView.printFail("잘못된 입력", "올바르지 않은 입력입니다.");
                return Menu.MAIN;
        }
    }
}
