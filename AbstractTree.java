package creek;

import java.util.*;

public abstract class AbstractTree implements Tree {

	// Friendly
	
	Map<String,Tree> map;

	// Private
	
	private String value;
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
		if (map==null) map = new LinkedHashMap<>();
		return map;
	}
	
	public Tree map ( Map<String,Tree> map ) {
		this.map = map;
		return this;
	}
	
	// Array style entry
	public Tree add ( List<String> values ) {
		if (values!=null) for (String value : values) add( value );
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
	public Tree add ( Map<String,String> m ) {
		if (m!=null) for (Map.Entry<String,String> entry : m.entrySet()) add( entry.getKey(), entry.getValue() );
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
		if (key==null || key.equals("")) map().put( integerKey(), other );
		else map().put( key, other );
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
		if (key==null || key.equals("")) key = integerKey();
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
		int i = 0;
		for (String key : keys()) {
			if (key==null || key.equals("") || Regex.exists( key, "\\D+" )) return false; // if non-digit char
			if (Integer.parseInt(key) != i++) return false; // must be consecutive from 0
		}
		return true;
	}
	
	public String integerKey () {
		String key;
		while (map().containsKey(key=String.valueOf(integerKey))) integerKey++;
		return key;
	}
	
	public String toString () {
		return ( map()!=null && map().size()>0 ? map().toString() : String.valueOf( value() ) );
	}

	public String serialize () {
		return toString();
	}
	
	public Set<Set<Tree>> routes () {
		Set<Set<Tree>> allRoutes = new LinkedHashSet<>();
		Set<Tree> startingPoint = new LinkedHashSet<>();
		routes( allRoutes, startingPoint );
		return allRoutes;
	}
	
	public void routes ( Set<Set<Tree>> routes, Set<Tree> previousBranches ) {
		Set<Tree> futureBranches = new LinkedHashSet<Tree>( previousBranches );
		futureBranches.add( this );
		boolean isLeaf = true;  // may be disproved
		for (Tree branch : branches()) {
			if (! futureBranches.contains(branch)) {
				isLeaf = false; // further branches exist and we haven't seen them before
				branch.routes( routes, futureBranches );
			}
		}
		if (isLeaf) routes.add( futureBranches );
	}
	
	public List<List<String>> paths () {
		List<List<String>> allPaths = new ArrayList<>();
		List<String> startingPoint = new ArrayList<>();
		paths( allPaths, startingPoint );
		return allPaths;
	}
	
	public void paths ( List<List<String>> paths, List<String> previousKeys ) {
		boolean isLeaf = true;  // may be disproved
		for (Map.Entry<String,Tree> entry : map().entrySet()) {
			isLeaf = false; // further keys exist
			List<String> futureKeys = new ArrayList<String>( previousKeys );
			futureKeys.add( entry.getKey() );
			Tree branch = entry.getValue();
			branch.paths( paths, futureKeys );
		}
		if (isLeaf) {
			previousKeys.add( this.value() );
			paths.add( previousKeys );
		}
	}
	
	public Tree data ( List<List<String>> data ) {
		for (List<String> row : data) {
			int size = row.size();
			if (size>2) {
				int last = size-1;
				auto( row.subList(0,last-1) ).add( row.get(last-1), row.get(last) );
			}
			else if (size>1) add( row.get(0), row.get(1) );
			else if (size>0) add( row.get(0) );
		}
		return this;
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
		
		System.out.println( "\nroutes (values):" );
		for (Set<Tree> route : tree.routes()) {
			for (Tree branch : route) {
				System.out.print( ","+branch.value() );
			}
			System.out.println();
		}

		Table table = new SimpleTable();
		System.out.println( "\npaths (keys..value):\n"+ table.data( tree.paths() ) );
		
		Tree anotherTree = new JSON();
		anotherTree.data( table.data() );
		System.out.println( "\nderived JSON object:\n"+anotherTree );
		System.out.println( "\nderived JSON object:\n"+ (new SimpleTable()).data( anotherTree.paths() ) );
	}

}
