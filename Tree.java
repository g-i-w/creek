package creek;

import java.util.*;

public interface Tree {

	public Tree create ();

	public String value ();
	public Tree value ( String value );
	
	public Map<String,Tree> map ();
	public Tree map ( Map<String,Tree> branches );

	// Array style
	public Tree add ( String value );
	public Tree add ( Tree value );
	
	// Map/Object style
	public Tree add ( String key, String value );
	public Tree add ( String key, Tree value );

	public Tree get ( String key );
	public Tree get ( List<String> path );

	public Tree auto ( String key );
	public Tree auto ( List<String> path );
	
	public Set<String> keys ();
	public List<String> values ();
	public Collection<Tree> branches ();
	public int size ();
	
	public boolean integerKeys ();
	public String integerKey();
	
	public String serialize ();
	public Tree deserialize ( String serial ) throws Exception;
	
}
