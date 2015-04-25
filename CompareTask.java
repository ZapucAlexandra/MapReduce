import java.util.HashMap;
/*
 * Clasa CompareTask reprezinta un task de tip Compare
 */
public class CompareTask implements Task {

	String fileName1, fileName2;
	HashMap<String, Integer> hash1, hash2;
	CompareTask (String file1, String file2, HashMap<String, Integer> hash1,
			HashMap<String, Integer> hash2) {
		this.fileName1 =  file1;
		this.fileName2 = file2;
		this.hash1 = hash1;
		this.hash2 = hash2;
	}
	String getFile1() {
		return fileName1;
	}
	String getFile2() {
		return fileName2;
	}
	HashMap<String, Integer> getHash1 () {
		return hash1;
	}
	HashMap<String, Integer> getHash2 () {
		return hash2;
	}
}
