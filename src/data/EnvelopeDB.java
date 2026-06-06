package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class EnvelopeDB {
	
	// 싱글톤 패턴
		private static final EnvelopeDB instance = new EnvelopeDB();
		private EnvelopeDB() {
			loadDB();
		}
		public static EnvelopeDB getInstance() {
			return instance;
		}
		
	    private static final String DB_FILE_PATH = "data/envelope.dat";
		// 유저 데이터를 메모리 상에서 관리할 Map 구조
	    private HashMap<String, Envelope> envelopeMap = new HashMap<>();
		
	    // 접근자
	    public Envelope findByReceiver(String receiver) {
	        return envelopeMap.get(receiver);
	    }
	    
	    // 관리용 함수
	    public boolean addEnvelope(Envelope envelope) {
	    	envelopeMap.put(envelope.getEncryptedFilePath(), envelope);
	    	File file = new File(DB_FILE_PATH);
	    	try (FileOutputStream fos = new FileOutputStream(file);
	    			ObjectOutputStream oos = new ObjectOutputStream(fos)) {
	    		oos.writeObject(envelopeMap);
	    		oos.flush();
	    		return true;
			} catch (IOException e) {
				e.printStackTrace();
				envelopeMap.remove(envelope.getEncryptedFilePath());
				return false;
			}
	    }
		
		private void loadDB() {
			File file = new File(DB_FILE_PATH);
			try (FileInputStream fis = new FileInputStream(file);
					ObjectInputStream ois = new ObjectInputStream(fis)) {
				Object obj = ois.readObject();
				this.envelopeMap = (HashMap<String, Envelope>) obj;
			} catch (FileNotFoundException e) {
				System.out.println("[시스템 정보] 초기 저장된 편지 데이터가 없어 새로 시작합니다.");
				this.envelopeMap = new HashMap<>();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
}