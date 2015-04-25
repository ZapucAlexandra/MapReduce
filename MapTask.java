/*
 * Clasa MapTask reprezinta un task de tip map.
 */
public class MapTask implements Task{

	String fileName;
	int offset;
	int sizeFragment;
	public MapTask (String fileName, int offset, int sizeFragment) {
		this.fileName = fileName;
		this.offset = offset;
		this.sizeFragment = sizeFragment;
	}
	
}
