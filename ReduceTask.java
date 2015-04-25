import java.util.HashMap;
import java.util.Vector;

/*
 * Clasa ReduceTask reprezinta un task de tip reduce.
 */
public class ReduceTask implements Task {
	String fileName;
	Vector <HashMap<String, Integer>> list;
	ReduceTask (String fileName, Vector<HashMap<String, Integer>> list) {
		this.fileName = fileName;
		this.list = list;
		
	}
}
