package creek;

import java.util.*;

public interface Table {
	// Non-restricted and not intrinsically thread-safe
	public Table data ( List<List<String>> data );
	public List<List<String>> data ();
	
	public Table last ( int lastRows );
	public Table slice ( int startRowInclusive, int endRowExclusive );
	public Table slice ( int startRowInclusive, int endRowExclusive, int startColInclusive, int endColExclusive );
	public Table set ();
	public Table set ( int col );
	public Table reverse ();
		
	// Simple, restricted, and mostly thread-safe
	public String item ( int row, int col );
	public String[] row ( int row );
	public String[] col ( int col );
	
	public int rowCount ();
	public int colCount ( int row );
	
	// Write-lock mechanism
	public void obtainWriteLock();
	public void releaseWriteLock();

	// Write-locked and thread-safe
	public Table alias ( Table table );
	public Table copy ( Table table );
	public Table append ( Table table );
	public Table append ( String[] row );
	public Table append ( List<String> row );
	public Table append ( String raw );
	public Table replace ( Map<String,String> replacements );
	public Table replace ( Map<String,String> replacements, int col );
	public Table replace ( Map<String,String> replacements, int startRowInclusive, int endRowExclusive, int startColInclusive, int endColExclusive );
	
	public String serial ();
}
