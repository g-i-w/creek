package creek;

import java.util.*;

public interface Table {
	public String item ( int row, int col );
	public String[] row ( int row );
	public String[] col ( int col );
	
	public int rowCount ();
	public int colCount ( int row );

	public Table append ( Table obj );
	public Table append ( String[] row );
	
	public String serial ();
}
