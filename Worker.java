import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;


public class Worker extends Thread{
	WorkPool wp;
	
	public Worker(WorkPool workpool) {
		this.wp = workpool;
	}	
	
	/**
	 * Procesarea unui MapTask. Rezultatul este o pereche 
	 * nume fisier - hash( avand ca si chei cuvintele gasite
	 *  in fragment, iar ca valori numarul de aparitii ale 
	 *  acestor cuvinte).
	 */
	void processMapTask(MapTask task) {
		try{
			HashMap<String, Integer> hash;
			String beforeFrag = "";
			byte[] array =  new byte[task.sizeFragment];
			String delim = ";:/?~\\.,><~`[]{}()!@#$%^&-_+\'=*\"| \t\n";
			File file = new File(task.fileName);
			RandomAccessFile randFile = new RandomAccessFile(file, "r");	
			randFile.seek(task.offset);
			randFile.read(array);
			
			String fragment = new String(array);
			int position = 0;
			if (task.offset != 0) {
				randFile.seek(task.offset - 1);
				beforeFrag = (char)randFile.readByte() + "";
				if (!delim.contains(beforeFrag) && !delim.contains((char)array[position] + "")) {
					position++;
					while ( task.offset + position < (file.length() - 1) &&
							!delim.contains((char)array[position] + "")) {
						position++;
					}
				}
			}
			fragment = fragment.substring(position);
			int pos = task.offset + task.sizeFragment - 1;
			randFile.seek(pos);
			beforeFrag = (char)randFile.read() + "";	
			while (pos < (file.length() - 1) && !delim.contains(beforeFrag)) {		
				beforeFrag = (char)randFile.read() + "";
				fragment = fragment + beforeFrag;
				pos++;
			}
			StringTokenizer token = new StringTokenizer(fragment, delim);
			hash = new HashMap<String, Integer>();
			String word;
			int begin, end;
			begin = end = 0;
			for (int i = 0; i < fragment.length(); i++) {
				if (delim.contains(fragment.charAt(i) + "")) {
					begin = i+1;
				}
				
			}
			
			while(token.hasMoreTokens())  {
				String tokenizer = token.nextToken().toLowerCase();
				if (!tokenizer.equals("")) {
					if (hash.containsKey(tokenizer)) {
						hash.put(tokenizer, hash.get(tokenizer) + 1);
					} else {
						hash.put(tokenizer, 1);
					}
				}
			}
			/*Salveaza rezultatele partiale*/
			HashMap<String, Vector<HashMap<String, Integer>>> map;
			map = ReplicatedWorkers.mapResult;
			if (map.containsKey(task.fileName)) {
				synchronized(map) {
				 map.get(task.fileName).add(hash);
				 }
			} else {
				synchronized(map) {
				map.put(task.fileName, new Vector<HashMap<String, Integer>>());
				}
			}			
		} catch(IOException e) {
			System.out.println("Error!\n");
			e.printStackTrace();
			
		}
	}
	/**
	 * Procesarea unui MapTask. Rezultatul este o pereche 
	 * nume fisier - hash( avand ca si chei cuvintele gasite
	 *  in fragment, iar ca valori numarul de aparitii ale 
	 *  acestor cuvinte).
	 */
	void processReduceTask(ReduceTask task) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (HashMap<String, Integer> hash: task.list) {
			for (Map.Entry<String, Integer> element: hash.entrySet()) {
				String word = element.getKey();
				if (result.containsKey(word)) {
					result.put(word, result.get(word) + element.getValue());
				} else {
					result.put(word, element.getValue());
				}
			}
		}
		/*Salveaza hash-ul pentru fisierul curent*/
		synchronized(ReplicatedWorkers.reduceResult) {
			ReplicatedWorkers.reduceResult.put(task.fileName, result);
		}
	
	}
	/**
	 * Procesarea unui MapTask. Rezultatul este o pereche 
	 * nume fisier - hash( avand ca si chei cuvintele gasite
	 *  in fragment, iar ca valori numarul de aparitii ale 
	 *  acestor cuvinte).
	 */
	void processCompareTask(CompareTask task) {
		int length1, length2;
		double freq1, freq2;
		double sim = 0;
		String name1, name2;
		HashMap<String, Integer> hash1, hash2;
		name1 = task.getFile1();
		name2 = task.getFile2();
		length1 = 0;
		length2= 0;
		hash1 = task.getHash1();
		hash2 = task.getHash2();
		for (Entry<String, Integer> entry: hash1.entrySet()) {
			length1 += entry.getValue();
		}
		for (Entry<String, Integer> entry: hash2.entrySet()) {
			length2 += entry.getValue();
		}
		for (Entry<String, Integer> entry: hash1.entrySet()) {
			if(hash2.containsKey(entry.getKey())) {
				freq1 = ((double)entry.getValue()/ length1) * 100;
				freq2 = ((double)hash2.get(entry.getKey())/ length2);
				sim += freq1 * freq2;
			}
		}
		String result = "";
		if (name1.length() < name2.length()) {
			result = name1 + ";" + name2 + ";";
		} else {
			if (name2.length() < name1.length()) {
				
				result = name2 + ";" + name1 + ";";

			} else {
				if (name1.compareTo(name2) < 0 ) {
					result = name1 + ";" + name2 + ";";
				} else {
					result = name2 + ";" + name1 + ";";
				}
			}
		}
		synchronized(ReplicatedWorkers.finalResult) {
			ReplicatedWorkers.finalResult.put(sim, result);
		}
		synchronized(ReplicatedWorkers.compareResult) {
			ReplicatedWorkers.compareResult.add(sim);
		}	
	}
	public void run() {
		while (true) {
			Task task = wp.getWork();
			if (task == null) {
				break;
			} else {
				if (task instanceof MapTask) {
					processMapTask((MapTask)task);
				} else {
					if ( task instanceof ReduceTask) {
						processReduceTask((ReduceTask)task);
					} else {
						processCompareTask((CompareTask)task);
					}
				}
			}
			
		}
	}

}
