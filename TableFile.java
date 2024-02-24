package creek;

import java.io.File;

public interface TableFile {

	public TableFile create ( File file ) throws Exception;

	public File file ();

	public Table table ();

	public TableFile clear () throws Exception;

	public TableFile read () throws Exception;
	
	public TableFile append ( Table table ) throws Exception;
	
	public TableFile write ( Table table ) throws Exception;
	
	public TableFile write () throws Exception;
	
}
