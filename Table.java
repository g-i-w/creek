package creek;

import java.util.*;

public interface Table {
	public Table data ( List<List<String>> data );
	public List<List<String>> data ();
	
	public List<List<String>> last ( int lastRows );
	public List<List<String>> slice ( int startRowInclusive, int endRowExclusive );
	public List<List<String>> slice ( int startRowInclusive, int endRowExclusive, int startColInclusive, int endColExclusive );
		
	public String item ( int row, int col );
	public String[] row ( int row );
	public String[] col ( int col );
	
	public int rowCount ();
	public int colCount ( int row );

	public Table alias ( Table table );
	public Table copy ( Table table );
	public Table append ( Table table );
	public Table append ( String[] row );
	public Table append ( List<String> row );
	public Table append ( String raw );
	
	public String serial ();
}
