package creek;

import java.util.*;

public interface Grid {
	public String item ( int row, int col );
	public List<List<String>> data ();
	
	public int rowCount ();
	public int colCount ();

	public Grid append ( Grid obj );
	public Grid append ( List<String> row );
	
	public String serial ();	
}
