package creek;

import java.util.*;

public interface Tree {

	public String get ( String[] path );
	
	public String put ( String[] path );
	
	public Set<String> keys ( String[] path );
	
	public List<String> values ( String[] path );
	
	public String serialize ();
	public Tree deserialize ( String serial );

}
