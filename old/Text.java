package creek;

import java.util.*;

public interface Text {
	public String line ( int lineNum );
	public int lineCount ();
	
	public Text append ( String line );
}
