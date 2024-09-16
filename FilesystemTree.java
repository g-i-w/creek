package creek;

import java.util.*;
import java.io.*;

public class FilesystemTree extends AbstractTree {

	private File file;    // file not null if directory
	
	Map<String,Tree> empty = new HashMap<>(); // null map
	

	public FilesystemTree ( String path ) {
		if (path==null) throw new RuntimeException( "Path string is null!" );
		this( new File( path ) );
	}
	
	public FilesystemTree ( File file ) {
		if (file==null) throw new RuntimeException( "File object is null!" );
		this.file = file;
	}
	
	public File file () {
		return file;
	}
	
	public Tree create () {
		return new FilesystemTree( file );
	}

	@Override
	public Map<String,Tree> map () {
		if (!file.exists()) return null;
	
		// directory
		else if (f.isDirectory()) {
			Map<String,Tree> map = new LinkedHashMap<>();
			List<File> files = file.listFiles();
			if (files.size()>0) {
				for (File f : file.listFiles()) {
					map().put( f.getName(), new FilesystemTree( f ) );
				}
			}
			return map;
		}

		// empty
		else return empty;
	}
	
	@Override
	public String value () {
		if (!file.exists() || file.isDirectory()) return null;
		
		try {
			return ( FileActions.read( file ) );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clear ( File ff ) {
		if (ff == null || !ff.exists()) return;
		if (ff.isDirectory()) for (File f : ff.listFiles()) clear( f );
		try {
			ff.delete();
		} catch (Exception e) {
			System.err.println( "Error while deleting '"+ff.getAbsolutePath()+"'" );
			e.printStackTrace();
		}
	}
	
	@Override
	public void value ( String value ) {
		clear( file );
		try {
			FileActions.write( file, value, "UTF-8", false );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Tree map ( Map<String,Tree> branches ) {
		clear( file );
		file.mkdir();
		for (String key : branches.keySet()) {
			Tree branch = branches.get(key);
			try {
				if (branch.size()==0) {
					if (branch.value()!=null) FileActions.write( file, branch.value(), "UTF-8", false );
		}
	}

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
	public Tree deserialize ( String serial ) throws Exception;
	
	// Flattening
	public Set<Set<Tree>> routes ();
	public void routes ( Set<Set<Tree>> allRoutes, Set<Tree> startingPoint );
	
	public List<List<String>> paths ();
	public void paths ( List<List<String>> allPaths, List<String> startingPoint );
	
	// data
	public Tree data ( List<List<String>> data );


}
