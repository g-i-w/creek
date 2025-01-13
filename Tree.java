package creek;

import java.util.*;

public interface Tree {

	public Tree create ();

	public String value ();
	public Tree value ( String value );
	
	public Map<String,Tree> map ();
	public Tree map ( Map<String,Tree> branches );

	// Array style
	public Tree add ( List<String> values );
	public Tree add ( String value );
	public Tree add ( Tree value );
	
	// Map/Object style
	public Tree add ( Map<String,String> map );
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
	
	// numerical keys
	public boolean integerKeys ();
	public String integerKey();
	
	// numerical values
	public int integerValue ();
	public double doubleValue ();
	public Tree increment ();
	public Tree decrement ();
	
	// I/O
	public String serialize ();
	public String serialize ( boolean readableText );
	public Tree deserialize ( String serial ) throws Exception;
	
	// Flattening
	public Set<Set<Tree>> routes ();
	public boolean routes ( Set<Set<Tree>> allRoutes, Set<Tree> startingPoint ); // true if no loops
	
	public List<List<String>> paths ();
	public void paths ( List<List<String>> allPaths, List<String> startingPoint );
	
	// data
	public Tree data ( List<List<String>> data );
	public void synchronize ( Tree toUpdate );
	
}
