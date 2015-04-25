import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;


public class ReplicatedWorkers {
	private static WorkPool mapPool;
	private static WorkPool reducePool;
	private static WorkPool comparePool;
	public static HashMap<String, Vector <HashMap<String, Integer>>> mapResult;
	public static HashMap<String, HashMap<String, Integer>> reduceResult;
	public static Vector<Double> compareResult;
	public static HashMap<Double, String> finalResult;
	public static void main(String args[]) {
		/* 
		 * nThreads = Numarul de thread-uri
		 * fileNameIn = Numele fisierului de intrare
		 * fileNameOut = Numele fisierului de iesire
		 * sizeD = dimensiunea D in octeti pentru fragmentare
		 * nDoc = nr de documente
		 * similarityX = pragul de similaritate
		 * files = lista cu numele celor nDoc documente 
		 * mapPool = Thread Pool-ul in care sunt adaugate MapTask-uri
		 */
		int nThreads;
		String fileNameIn;
		String fileNameOut;
		int sizeD, nDoc;
		double similarityX;
		String files[];
		if (args.length != 3) {
			
			System.out.println("Error: Nthreads filein fileout\n");
			return;
		}
		nThreads = Integer.parseInt(args[0]);
		fileNameIn = args[1];
		fileNameOut = args[2];
		File fileIn = new File(fileNameIn);
		File fileOut = new File(fileNameOut);
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			FileReader inFile = new FileReader(fileIn);
			br = new BufferedReader(inFile);
			/*Citeste din fisierul de intrare*/
			sizeD = Integer.parseInt(br.readLine());
			similarityX = Double.parseDouble(br.readLine());
			nDoc = Integer.parseInt(br.readLine());
			/*Creeaza un pool pentru MapTask*/
			mapPool = new WorkPool(nThreads);
			MapTask mapTask;
			files = new String[nDoc];
			for (int i = 0; i < nDoc; i++) {
				files[i] = br.readLine();
				File auxFile = new File(files[i]);
				int fileSize = (int)auxFile.length();
				
				/*
				 * Creeaza MapTask-uri prin impartirea fiecarui fisier. Merge din sizeD
				 * in sizeD octeti. Daca exista octeti ramasi dupa impartire, atunci 
				 * ultimul fragment este mai mic decat sizeD.
				 */
				int nrOfFragments = fileSize / sizeD;
				for (int j = 0; j < nrOfFragments; j ++) {
					mapTask  = new MapTask(files[i], j * sizeD, sizeD);
					mapPool.putWork(mapTask);
				}
				if ( (fileSize % sizeD) != 0 ) {
					mapTask = new MapTask(files[i], nrOfFragments * sizeD, fileSize % sizeD);
					mapPool.putWork(mapTask);
				}
			}
			
			mapResult = new HashMap<String, Vector<HashMap<String, Integer>>>();;
			Worker [] workers = new Worker[nThreads];
			for (int i = 0; i < nThreads; i++) {
				workers[i] = new Worker(mapPool);
				workers[i].start();		
			}
			/* Inainte de a porni executia task-urilor reduce
			 * este necesara terminarea tuturor task-urilor map
			 */
			try{
				for (int i = 0; i < nThreads; i++) {
					workers[i].join();
					
				}
			}
			 catch (InterruptedException e) {
					e.printStackTrace();
			}
			/* Creaza WorkPool pentru Reduce 
			 * si task-uri ReduceTask pe care le adauga in 
			 * workpool.
			 */
			reducePool = new WorkPool(nThreads);
			reduceResult = new HashMap<String,HashMap<String, Integer>>();;
			ReduceTask reduceTask;
			for (String fileName: mapResult.keySet()) {
					reduceTask = new ReduceTask(fileName, mapResult.get(fileName));
					reducePool.putWork(reduceTask);

			}
			/*Se pornesc thread-urile pentru ReducePool.*/
			workers = new Worker[nThreads];
			for (int i = 0; i < nThreads; i++) {
				workers[i] = new Worker(reducePool);
				workers[i].start();		
			}
			/* Inainte de a porni executia task-urilor compare
			 * este necesara terminarea tuturor task-urilor reduce.
			 */
			try{
				for (int i = 0; i < nThreads; i++) {
					workers[i].join();
					
				}
			}
			 catch (InterruptedException e) {
					e.printStackTrace();
			}
			comparePool = new WorkPool(nThreads);
			compareResult = new  Vector<Double>();
			finalResult = new  HashMap<Double, String>();
			Vector<Entry<String, HashMap<String, Integer>>> vector;
			vector = new Vector<Entry<String, HashMap<String, Integer>>>();
			for (Entry<String, HashMap<String, Integer>> entry: reduceResult.entrySet()) {
				vector.add(entry);
			}
			CompareTask compareTask;
			//Creeaza compareTask
			for (int i = 0; i < (vector.size() - 1); i++) {
				Entry<String, HashMap<String, Integer>> myFile = vector.get(i);
				for (int j = i + 1; j < vector.size(); j++) {
					compareTask = new CompareTask(myFile.getKey(), vector.get(j).getKey(),
							myFile.getValue(), vector.get(j).getValue());
					comparePool.putWork(compareTask);
				}
			}
					
			/*Se pornesc thread-urile pentru ComparePool.*/
			
			workers = new Worker[nThreads];
			for (int i = 0; i < nThreads; i++) {
				workers[i] = new Worker(comparePool);
				workers[i].start();		
			}
			/* Inainte de a filtra rezultatele task-urilor compare
			 * este necesara terminarea tuturor task-urilor.
			 */
			try{
				for (int i = 0; i < nThreads; i++) {
					workers[i].join();
					
				}
			}
			 catch (InterruptedException e) {
					e.printStackTrace();
			}	
			
			Comparator comparator = Collections.reverseOrder();
			Collections.sort(compareResult, comparator);
			FileWriter outFile = new FileWriter(fileOut);
			bw = new BufferedWriter(outFile);
			int i = 0;
			while(i < compareResult.size() && compareResult.elementAt(i) > similarityX) {
				String string = finalResult.get(compareResult.elementAt(i));
				bw.write(string + String.format("%.4f", compareResult.elementAt(i)) + "\n");
				i++;
			}
			
			bw.close();
		} catch (IOException e) {
			System.out.println("File I/O error!");
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) { 
					br.close();
				}
			} catch (IOException ex) {
				System.out.println("File error!\n");
			}
		}
						
		
		
	}
}
