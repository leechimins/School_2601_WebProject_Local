package ui;

import ui.ConsoleView;
import ui.ConsoleView.Menu;
import java.util.Scanner;

public class MainConsole {

	private static String currentUser = null;

    public static void main(String[] args) {
        ConsoleView view = ConsoleView.getInstance();
        
        try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
            boolean isRunning = true;
            while (isRunning) {
                if (currentUser == null) {
                    // AUTH 메뉴 출력 요구 시 enum 전달
                    view.printMenu(Menu.AUTH, null);
                    String id = scanner.nextLine().trim();
                    if (!id.isEmpty()) {
                        currentUser = id;
                    }
                } else {
                    // MAIN 메뉴 출력 요구 시 enum과 세션 유저명 전달
                    view.printMenu(Menu.MAIN, currentUser);
                    String choice = scanner.nextLine().trim();
                    if (choice.equals("4")) {
                        isRunning = false;
                    } else if (choice.equals("3")) {
                        currentUser = null;
                    }
                    // 각 번호에 따른 후속 UI/비즈니스 로직 연계...
                }
            }
        } catch (Exception e) {
            System.out.println("[오류] 시스템 예외 발생: " + e.getMessage());
        }
    }

}
