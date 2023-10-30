package creek;

public class CompoundCSVLog extends CSVLog {

	private File latest;
	private Map<String,File> files;
	private Map<Integer,File> segments;
	
	private Table loadCSVFile ( File file ) {
		
	}

	public CompoundCSVFile ( File file, int min ) throws Exception {
		files = new TreeMap<>();
		for (File file : FileActions.recurse( file )) read( file, true );
	}
	
	public TableFile read ( File file ) throws Exception {
		return read( file, false );
	}
		
	public TableFile read ( File file, boolean checkExtension ) throws Exception {
		if (checkExtension && !path.substring( path.size()-4, path.size() ).equals(".csv")) return null;
		CSVFile csv = new CSVFile( file );
		files.put( path, file );
		latest = file;
	}
	
	// Log

	public Table last ( int lastRows );
	
	public Table slice ( int startRow, int endRow );
	
	public Map<String,Table> last ( int mapColumn, String afterThis );
	
	public Map<String,Table> slice ( int mapColumn, String includingThis, String beforeThis );	

}
