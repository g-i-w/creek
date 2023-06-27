package creek;

public interface TableFile {

	public Table table ();

	public TableFile clear () throws Exception;

	public TableFile read () throws Exception;
	
	public TableFile append ( Table table ) throws Exception;
	
	public TableFile write ( Table table ) throws Exception;
	
	public TableFile write () throws Exception;
	
}
