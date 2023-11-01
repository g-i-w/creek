package creek;

import java.util.List;

public interface LogFile {

	public SetTable table ();
	
	public LogFile trimmed ( int rows ) throws Exception;
	
	public LogFile append ( List<String> sample ) throws Exception;
	public LogFile append ( String[] sample ) throws Exception;

}
