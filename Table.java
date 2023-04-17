package creek;

import java.util.*;

public interface Table {
	public String item ( int row, int col );
	
	public int rowCount ();
	public int colCount ( int row );

	public Table append ( Table obj );
	
	public String serial ();
}
