package creek;

import java.util.*;

public abstract class AbstractTree implements Tree {

	// Private
	
	private String value;
	private Map<String,Tree> map;
	private int integerKey;
	
	// Abstract
	
	public abstract Tree create ();
	
	public abstract Tree deserialize ( String serial ) throws Exception;

	
	// Public

	public String value () {
		return value;
	}
	
	public Tree value ( String value ) {
		this.value = value;
		return this;
	}
	
	public Map<String,Tree> map () {
		if (map==null) map = new TreeMap<>();
		return map;
	}
	
	public Tree map ( Map<String,Tree> map ) {
		this.map = map;
		return this;
	}
	
	// Array style entry
	public Tree add ( String value ) {
		Tree branch = create();
		branch.value( value );
		add( branch );
		return this;
	}
	
	// Array style entry
	public Tree add ( Tree arrayValue ) {
		map().put( integerKey(), arrayValue );
		return this;
	}
	
	// Map or Object style entry
	public Tree add ( String key, String value ) {
		Tree branch = create();
		branch.value( value );
		add( key, branch );
		return this;
	}
	
	// Map or Object style entry
	public Tree add ( String key, Tree other ) {
		map().put( key, other );
		return this;
	}
		
	public Tree get ( String key ) {
		return map().get( key );
	}
	
	public Tree get ( List<String> path ) {
		// null check
		if (path==null) return null;
		// end-point
		if (path.size()==0) return this;
		// this key
		String key = path.get(0);
		// get
		Tree branch = get( key );
		// null
		if (branch == null) return null;
		// recurse
		return branch.get( path.subList(1,path.size()) );
	}
	
	public Tree auto ( String key ) {
		if (!map().containsKey(key)) add( key, create() );
		return map().get( key );
	}
	
	public Tree auto ( List<String> path ) {
		// null check
		if (path==null) return null;
		// end-point
		if (path.size()==0) return this;
		// this key
		String key = path.get(0);
		// auto
		Tree branch = auto( key );
		return branch.auto( path.subList(1,path.size()) );
	}
	
	public Set<String> keys () {
		return map().keySet();
	}
	
	public List<String> values () {
		List<String> values = new ArrayList<>(map().size());
		for (Tree branch : branches()) values.add( branch.value() );
		return values;
	}
	
	public Collection<Tree> branches () {
		return map().values();
	}
	
	public int size () {
		if (map==null) return 0;
		else return map.size();
	}
	
	public boolean integerKeys () {
		for (String key : keys()) {
			if (key==null || key.equals("") || Regex.exists( key, "\\D+" )) return false; // if non-digit char
		}
		return true;
	}
	
	public String integerKey () {
		String key;
		while (map().containsKey(key=String.valueOf(integerKey))) integerKey++;
		return key;
	}
	
	public String toString () {
		return ( map().size()>0 ? map().toString() : value() );
	}

	public String serialize () {
		return toString();
	}
	
}

class TestAbstractTree extends AbstractTree {

	public Tree create () {
		//System.out.println( "created!" );
		return new TestAbstractTree();
	}
	
	public Tree deserialize ( String serial ) {
		return new TestAbstractTree();
	}
	
	public static void main ( String[] args ) {
		Tree tree = new TestAbstractTree();
		List<String> path0 = Arrays.asList( new String[]{ "level0", "level1", "level2" } );
		List<String> path1 = Arrays.asList( new String[]{ "a0", "b1" } );
		
		tree.auto( path0 ).add( "0", "this is something" ).add( "1", "true" ).add( "2", "1.001" ).add( "3", "2" );
		tree.auto( "level0" ).add( "a" ).add( "2.002" ).auto( path1 );

		System.out.println( "toString:\n"+tree );
		System.out.println( "serialize:\n"+tree.serialize() );
	}

}
