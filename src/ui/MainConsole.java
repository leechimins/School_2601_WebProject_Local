package ui;

import java.security.PrivateKey;
import java.util.Scanner;

import data.LocalDB;
import data.User;
import service.AuthService;

public class MainConsole {

    private static User currentUser = null;
    private static PrivateKey privateKey = null;
    private static LocalDB db = LocalDB.getInstance();

    enum Menu {
        AUTH(3), MAIN(11), WRITE(1), INBOX(2), READ(12), EXIT(4);

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
                    currentMenu = auth(sc);
                    break;
                case MAIN:
                    ConsoleView.printMainMenu(currentUser.getId());
                    currentMenu = Menu.fromValue(sc.nextInt());
                    break;
                case WRITE:
                    write(sc);
                    break;
                case INBOX:
                    inbox(sc);
                    break;
                case READ:
                    read(sc);
                    break;
                case EXIT:
                default:
                    currentMenu = Menu.EXIT;
                    break;
            }
        }

        sc.close();
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

    private static boolean write(Scanner sc) {
        return false;
    }

    private static boolean inbox(Scanner sc) {
        return false;
    }

    private static boolean read(Scanner sc) {
        return false;
    }
}
