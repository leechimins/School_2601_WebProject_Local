package ui;

import java.util.ArrayList;
import data.Envelope;

public class ConsoleView {

	private ConsoleView() {
	}

	public static void printStart() {
		System.out.print(start);
	}

	public static void printAuthGuidance() {
		System.out.print(authGuidance);
	}

	public static String getAuthPrompt(int step) {
		return authPrompts[step];
	}

	public static void printMainMenu(String userName) {
		System.out.print(mainHeader1 + userName + mainHeader2);
	}

	public static String getMainMenuPrompt() {
		return mainPrompt;
	}

	public static void printWriteHeader() {
		System.out.print(writeHeader);
	}

	public static String getWritePrompt1() {
		return writePrompt1;
	}

	public static String getWritePrompt2() {
		return writePrompt2;
	}

	public static class Step {
		public final int depth;
		public final String method;
		public final String result;

		public Step(int depth, String method, String result) {
			this.depth = depth;
			this.method = method;
			this.result = result;
		}
	}

	// 성공 화면 공통 출력 함수
	private static void printSuccess(String title, Step... steps) {
		System.out.println(
				"====================================================================================================");
		System.out.println("   [✅ " + title + " SUCCESS ]");
		System.out.println(
				"----------------------------------------------------------------------------------------------------");
		int stepNum = 1;
		for (int i = 0; i < steps.length; i++) {
			Step step = steps[i];
			if (step.depth == 0) {
				System.out.printf("   %d. %s : %s\n", stepNum++, step.method, step.result);
			} else {
				StringBuilder sb = new StringBuilder();
				for (int d = 0; d < step.depth; d++) {
					sb.append("    ");
				}
				System.out.printf("   %s└ %s : %s\n", sb.toString(), step.method, step.result);
			}
		}
		System.out.println(
				"====================================================================================================");
		System.out.println();
	}

	// 실패 화면 공통 출력 함수
	public static void printFail(String title, String reason) {
		System.out.println(
				"====================================================================================================");
		System.out.println("   [❌ ERROR : " + title + " ]");
		System.out.println("   ▶ 사유: " + reason);
		System.out.println(
				"====================================================================================================");
		System.out.println();
	}

	public static void printLoginSuccess() {
		printSuccess("LOGIN",
				new Step(0, "MainConsole.auth()", "사용자 로그인 절차 개시"),
				new Step(1, "AuthService.login()", "데이터 검증 및 세션 로그인 처리"),
				new Step(2, "LocalDB.findUserById()", "로컬 DB에서 유저 객체 조회 완료."),
				new Step(0, "AuthService.verifyPassword()", "패스워드 대조 검증 수행"),
				new Step(1, "MessageDigest.getInstance()", "SHA-256 해시 인스턴스 획득"),
				new Step(2, "MessageDigest.isEqual()", "저장된 해시값과 대조 완료."),
				new Step(0, "CryptoService.decryptPrivateKey()", "암호화된 사용자 사설키 복구"),
				new Step(1, "Cipher.getInstance()", "AES 복호화 인스턴스 획득"),
				new Step(2, "Cipher.doFinal()", "대칭키 복호화를 통한 사설키 복원 완료."),
				new Step(0, "MainConsole.auth()", "로그인 완료 및 키 세션 적재"),
				new Step(1, "MainConsole.privateKey", "복구된 사설키 객체 메모리 탑재 완료."));
	}

	public static void printRegisterSuccess() {
		printSuccess("REGISTER",
				new Step(0, "MainConsole.auth()", "신규 사용자 자동 회원가입 절차 개시"),
				new Step(1, "AuthService.register()", "회원 등록 정보 및 암호키 생성 처리"),
				new Step(2, "CryptoService.generateRSAKeyPair()", "RSA 1024-bit 공개키 및 사설키 쌍 생성 완료."),
				new Step(0, "SecureRandom.nextBytes()", "암호화 안전 난수 Salt 생성 완료."),
				new Step(1, "MessageDigest.getInstance()", "SHA-256 해시 인스턴스 획득"),
				new Step(2, "MessageDigest.digest()", "비밀번호 단방향 솔티드 해시 생성 완료."),
				new Step(0, "CryptoService.encryptPrivateKey()", "사용자 사설키 암호화 보호"),
				new Step(1, "SecretKeySpec", "패스워드 해시값을 통한 AES-256 키 유도 완료."),
				new Step(2, "Cipher.doFinal()", "패스워드 대칭키로 사설키(PrivateKey) 암호화 완료."),
				new Step(0, "LocalDB.addUser()", "회원 정보 및 키 정보 디스크 영속화"),
				new Step(1, "LocalDB.saveFile()", "users.dat 직렬화 파일 쓰기 완료."));
	}

	public static void printWriteSuccess() {
		printSuccess("WRITE & SEAL",
				new Step(0, "MainConsole.write()", "비밀 편지 전송 및 봉인 절차 개시"),
				new Step(1, "LetterService.sendLetter()", "편지 작성 처리 시작"),
				new Step(2, "LocalDB.findPublicKeyById()", "로컬 DB에서 수신자 RSA 공개키 획득 완료."),
				new Step(0, "CryptoService.generateAESKey()", "편지 암호화용 세션키 생성"),
				new Step(1, "KeyGenerator.generateKey()", "일회성 AES 대칭키 동적 생성 완료."),
				new Step(0, "CryptoService.encryptLetterAndSave()", "편지 본문 암호화 및 디스크 저장"),
				new Step(1, "Cipher.doFinal()", "일회성 AES 키를 이용해 본문 암호화 완료."),
				new Step(2, "FileOutputStream.write()", "letters/ 디렉토리에 암호화 편지 본문 텍스트 파일 저장 완료."),
				new Step(0, "CryptoService.wrapAESKey()", "일회성 AES 대칭키 암호화(Key Wrap)"),
				new Step(1, "Cipher.doFinal()", "수신자의 RSA 공개키로 일회성 AES 키 암호화 완료."),
				new Step(0, "LetterService.sendLetter()", "송신자 서명 생성 및 봉투화"),
				new Step(1, "CryptoService.signData()", "송신자 사설키로 본문의 SHA256withRSA 디지털 서명 생성 완료."),
				new Step(2, "LocalDB.addEnvelope()", "수신함 envelope.dat 데이터 직렬화 저장 완료."));
	}

	public static void printLogoutSuccess() {
		printSuccess("LOGOUT",
				new Step(0, "MainConsole.main()", "로그아웃 요청 처리"),
				new Step(1, "MainConsole 세션 초기화", "로그인 사용자(currentUser) 객체 파기 완료."),
				new Step(2, "MainConsole.privateKey", "메모리 내 탑재된 사용자 사설키(PrivateKey) 객체 파기 완료."));
	}

	public static void printExitSuccess() {
		printSuccess("EXIT",
				new Step(0, "MainConsole.main()", "시스템 종료 루프 진입"),
				new Step(1, "Scanner.close()", "콘솔 입력 리소스 반납 완료."),
				new Step(2, "ConsoleView.printExitSuccess()", "프로그램 안전 마감 완료."));
	}

	public static void printReadSuccess() {
		printSuccess("READ & VERIFY",
				new Step(0, "MainConsole.read()", "편지 해독 및 검증 절차 개시"),
				new Step(1, "LetterService.readLetter()", "수신 편지 읽기 처리 시작"),
				new Step(2, "CryptoService.unwrapAESKey()", "수신자의 RSA 사설키로 래핑된 일회성 AES 키 복호화(Unwrap) 완료."),
				new Step(0, "CryptoService.decryptLetter()", "암호화 파일 로드 및 본문 복호화"),
				new Step(1, "CipherInputStream", "파일 시스템에서 암호문 스트림 로드 완료."),
				new Step(2, "Cipher.doFinal()", "일회성 AES 키로 복호화한 편지 평문 획득 완료."),
				new Step(0, "LetterService.readLetter()", "디지털 서명 무결성 검증"),
				new Step(1, "LocalDB.findPublicKeyById()", "송신자의 RSA 공개키 데이터 획득 완료."),
				new Step(2, "CryptoService.verifySignature()", "송신자 공개키로 서명(SHA256withRSA) 검증 완료 (SUCCESS)."));
	}

	public static boolean printInboxList(ArrayList<Envelope> envelopes) {
		System.out.print(inbox1);
		if (envelopes.isEmpty()) {
			System.out.println("   [안내] 수신된 편지가 없습니다.");
			System.out.println(
					"====================================================================================================");
			System.out.println();
			return false;
		} else {
			for (int i = 0; i < envelopes.size(); i++) {
				Envelope env = envelopes.get(i);
				System.out.printf("   [%d] ✉️ 발신자: %-10s | 경로: %s\n",
						(i + 1), env.getSender(), env.getEncryptedFilePath());
			}
		}
		System.out.print(inboxDivider);
		return true;
	}

	public static String getInboxPrompt() {
		return inboxPrompt;
	}

	public static void printReadWorkspace(data.Envelope envelope, String decryptedText) {
		System.out.print(read1 + envelope.getSender() + "\n");
		System.out.print(read2 + envelope.getReceiver() + "\n");
		System.out.print(read3 + decryptedText + "\n");
		System.out.print(readDivider);
	}

	public static String getReadPrompt() {
		return readPrompt;
	}

	// ==========================================
	// UI 문자열 템플릿
	// ==========================================

	private static final String start = """
			====================================================================================================
			                  ██████╗  ██████╗ ██████╗  ██████╗ ██████╗ ██████╗ ███╗   ██╗
			                  ██╔══██╗██╔═══██╗██╔══██╗██╔════╝██╔═══██╗██╔══██╗████╗  ██║
			                  ██████╔╝██║   ██║██████╔╝██║     ██║   ██║██████╔╝██╔██╗ ██║
			                  ██╔═══╝ ██║   ██║██╔═══╝ ██║     ██║   ██║██╔══██╗██║╚██╗██║
			                  ██║     ╚██████╔╝██║     ╚██████╗╚██████╔╝██║  ██║██║ ╚████║
			                  ╚═╝      ╚═════╝ ╚═╝      ╚═════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝
			====================================================================================================
			                                     [ SYSTEM : SECRET LETTER ]
			====================================================================================================
			""";

	private static final String authGuidance = "   📢 안내: 처음 접속하는 ID는 자동으로 회원가입 됩니다.\n";
	private static final String[] authPrompts = { "▶ 사용할 사용자 ID 입력: ", "▶ 비밀번호: " };

	private static final String mainHeader1 = "   [ CURRENT SESSION : ";
	private static final String mainHeader2 = """
			님 로그인 중 ]
			====================================================================================================
			   1. 📝 편지 쓰기\t(일회성 대칭키 생성 및 전자봉투 송신)
			   2. 📂 편지 보관함\t(나에게 온 암호화 편지 목록 조회)
			   3. 🔓 로그아웃\t(현재 사용자의 인증 세션 종료)
			   4. ❌ 시스템 종료\t(안전하게 프로그램 마감)
			====================================================================================================
			""";
	private static final String mainPrompt = "▶ 수행할 작업 번호를 선택하세요: ";

	private static final String writeHeader = """
			====================================================================================================
			   [ WRITE WORKSPACE : 비밀 편지 작성 및 봉인 ]
			====================================================================================================
			""";
	private static final String writePrompt1 = "▶ 수신자 ID 입력: ";
	private static final String writePrompt2 = "▶ 편지 내용 입력 (AES 대칭키로 자동 암호화됩니다):\n";

	private static final String inbox1 = """
			====================================================================================================
			   [ INBOX : 수신된 전자봉투 보관함 ]
			====================================================================================================
			""";
	private static final String inboxDivider = "----------------------------------------------------------------------------------------------------\n";
	private static final String inboxPrompt = "▶ 복호화하여 상세히 읽을 편지 번호를 입력하세요 (이전 메뉴는 0): ";

	private static final String read1 = """
			====================================================================================================
			   [ READ WORKSPACE : 전자봉투 검증 및 복호화 완료 ]
			====================================================================================================
			   ✉️ 발신자(Sender)   : """;
	private static final String read2 = "   🔒 수신자(Receiver) : ";
	private static final String read3 = """
			----------------------------------------------------------------------------------------------------
			   📝 편지 본문 (Decrypted Text) :
			""";
	private static final String readDivider = "====================================================================================================\n";
	private static final String readPrompt = "▶ 편지를 쓰려면 W, 목록으로 가려면 L, 메인으로 돌아가려면 M을 입력하세요: ";
}