package ui;

import java.util.ArrayList;
import data.Envelope;

public class ConsoleView {

	private ConsoleView() {
	}

	public static void printStart() {
		System.out.print(start);
	}

	public static void printAuthMenu(int step) {
		System.out.print(auth[step]);
	}

	public static void printMainMenu(String userName) {
		System.out.print(main1 + userName + main2);
	}

	public static void printWritePrompt() {
		System.out.print(write1);
	}

	public static void printWriteContentPrompt() {
		System.out.print(write2);
	}

	// 성공 화면 공통 출력 함수
	private static void printSuccess(String title, String... steps) {
		System.out.println("======================================================================");
		System.out.println("   [✔ " + title + " SUCCESS ]");
		System.out.println("----------------------------------------------------------------------");
		System.out.println("[✔ PROCESS SUCCESS]");
		for (int i = 0; i < steps.length; i++) {
			System.out.println((i + 1) + ". " + steps[i]);
		}
		System.out.println("======================================================================\n");
	}

	// 실패 화면 공통 출력 함수
	public static void printFail(String title, String reason) {
		System.out.println("\n======================================================================");
		System.out.println("[❌ ERROR : " + title + " ]");
		System.out.println("----------------------------------------------------------------------");
		System.out.println("▶ 사유: " + reason);
		System.out.println("======================================================================\n");
	}

	public static void printLoginSuccess() {
		printSuccess("LOGIN",
				"LocalDB.findUserById() -> 로컬 데이터베이스에서 사용자 정보 로드 완료.",
				"AuthService.verifyPassword() -> MessageDigest.isEqual()을 통한 해시 일치 여부 확인 완료.",
				"CryptoService.decryptPrivateKey() -> 사용자 사설키(PrivateKey) 복구 완료.",
				"MainConsole 세션 내 개인키 객체 적재 완료.");
	}

	public static void printRegisterSuccess() {
		printSuccess("REGISTER",
				"CryptoService.generateRSAKeyPair() -> RSA 1024-bit 공개키 및 사설키 쌍 자동 생성 완료.",
				"SecureRandom & MessageDigest.getInstance(\"MD5\") -> 패스워드 기반 16바이트 Salt 생성 및 해싱 완료.",
				"CryptoService.encryptPrivateKey() -> 솔팅된 패스워드로 사설키 암호화 완료.",
				"LocalDB.addUser() -> 로컬 디스크에 회원 정보 저장 완료.");
	}

	public static void printWriteSuccess() {
		printSuccess("WRITE & SEAL",
				"일회성 AES 비밀키(SecretKey) 동적 생성 완료.",
				"수신자의 RSA Public Key를 로컬 디스크에서 획득 완료.",
				"본문 암호문 + 암호화된 AES 키 + IV 결합 완료.",
				"전자봉투(Digital Envelope) 인메모리 DB 업로드 완료!");
	}

	public static void printLogoutSuccess() {
		printSuccess("LOGOUT",
				"MainConsole 세션 내 개인키 객체 파기 완료.",
				"현재 사용자의 인증 세션 종료.");
	}

	public static void printExitSuccess() {
		printSuccess("EXIT",
				"안전하게 프로그램을 종료합니다.");
	}

	public static boolean printInboxList(ArrayList<Envelope> envelopes) {
		System.out.print(inbox1);
		if (envelopes.isEmpty()) {
			System.out.println("   [안내] 수신된 편지가 없습니다.");
			return false;
		} else {
			for (int i = 0; i < envelopes.size(); i++) {
				Envelope env = envelopes.get(i);
				System.out.printf("   [%d] ✉ 발신자: %-10s | 경로: %s\n",
						(i + 1), env.getSender(), env.getEncryptedFilePath());
			}
		}
		System.out.print(inbox2);
		return true;
	}

	public static void printReadWorkspace(data.Envelope envelope, String decryptedText) {
		System.out.print(read1 + envelope.getSender() + "\n");
		System.out.print(read2 + envelope.getReceiver() + "\n");
		System.out.print(read3 + decryptedText + "\n");
		System.out.print(read4);
	}

	// ==========================================
	// UI 문자열 템플릿
	// ==========================================

	private static final String start = """
			======================================================================
			     ██████╗  ██████╗ ██████╗  ██████╗ ██████╗ ██████╗ ███╗   ██╗
			     ██╔══██╗██╔═══██╗██╔══██╗██╔════╝██╔═══██╗██╔══██╗████╗  ██║
			     ██████╔╝██║   ██║██████╔╝██║     ██║   ██║██████╔╝██╔██╗ ██║
			     ██╔═══╝ ██║   ██║██╔═══╝ ██║     ██║   ██║██╔══██╗██║╚██╗██║
			     ██║     ╚██████╔╝██║     ╚██████╗╚██████╔╝██║  ██║██║ ╚████║
			     ╚═╝      ╚═════╝ ╚═╝      ╚═════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝
			======================================================================
			                     [ SYSTEM : EXCHANGE DIARY ]
			======================================================================

			""";

	private static final String[] auth = { """
			▶ 안내: 처음 접속하는 ID는 자동으로 회원가입 됩니다.
			▶ 사용할 사용자 ID 입력: """, "▶ 비밀번호: " };

	private static final String main1 = """
			[ CURRENT SESSION : """;
	private static final String main2 = """
			님 로그인 중 ]
			======================================================================
			   1. 📝 편지 쓰기\t(일회성 대칭키 생성 및 전자봉투 송신)
			   2. 📂 편지 보관함\t(나에게 온 암호화 일기 목록 조회)
			   3. 🔓 로그아웃\t(현재 사용자의 인증 세션 종료)
			   4. ❌ 시스템 종료\t(안전하게 프로그램 마감)
			======================================================================

			▶ 수행할 작업 번호를 선택하세요: """;

	private static final String write1 = """
			======================================================================
			   [ WRITE WORKSPACE : 보안 교환일기 작성 및 봉인 ]
			======================================================================

			▶ 수신자 ID 입력 (상대방의 RSA 공개키가 필요합니다): """;
	private static final String write2 = """
			----------------------------------------------------------------------
			▶ 일기 내용 입력 (AES-128-CBC 모드로 자동 암호화됩니다):
			""";

	private static final String inbox1 = """
			======================================================================
			   [ INBOX : 수신된 전자봉투 보관함 ]
			======================================================================

			""";
	private static final String inbox2 = """
			----------------------------------------------------------------------
			▶ 복호화하여 상세히 읽을 편지 번호를 입력하세요 (이전 메뉴는 0): """;

	private static final String read1 = """
			======================================================================
			   [ READ WORKSPACE : 전자봉투 검증 및 복호화 완료 ]
			======================================================================
			 ✉ 발신자(Sender)\t\t: """;
	private static final String read2 = """
			🔒 수신자(Receiver)\t: """;
	private static final String read3 = """
			----------------------------------------------------------------------
			 📝 일기 본문 (Decrypted Text) :
			 """;
	private static final String read4 = """
			======================================================================
			▶ 편지를 쓰려면 W, 목록으로 가려면 L, 메인으로 돌아가려면 M을 입력하세요: """;
}