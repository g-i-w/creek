package creek;

import java.util.*;
import java.io.*;

public class FilesystemTree extends AbstractTree {
	
	private static final String valueKeyword = "value";

	private File file;    // file not null if directory
	
	Map<String,Tree> empty = new HashMap<>(); // null map
	
	public FilesystemTree ( String path ) {
		this( new File( path ) );
	}
	
	public FilesystemTree ( File file ) {
		if (file==null) throw new RuntimeException( "Error: File object is null" );
		file( file );
	}
	
	public boolean dir () {
		return (file.exists() && file.isDirectory());
	}
	
	public void clear () {
		clear( file );
	}
	
	public void clear ( File ff ) {
		//System.out.println( "clear: "+ff.getAbsolutePath() );
		if (ff == null || !ff.exists()) return; // null or doesn't exist
		if (ff.isDirectory()) for (File f : ff.listFiles()) {
			clear( f );
		}
		try {
			//System.out.println( "delete: "+ff.getAbsolutePath() );
			ff.delete();
		} catch (Exception e) {
			System.err.println( "Error while deleting '"+ff.getAbsolutePath()+"'" );
			e.printStackTrace();
		}
	}
	
	public void mkdir () {
		mkdir( file );
	}
	
	public void mkdir ( File ff ) {
		if (ff == null) return;
		if(ff.exists() && ff.isDirectory()) return; // already a directory
		try {
			file.delete();
			file.mkdir();
		} catch (Exception e) {
			System.err.println( "Error while creating directory '"+file.getAbsolutePath()+"'" );
			e.printStackTrace();
		}
	}
	
	public void write ( String data ) {
		write( file, data );
	}
	
	public void write ( File ff, String data ) {
		if (ff == null || data == null) return;
		try {
			FileActions.write( ff, data, "UTF-8", false );
		} catch (Exception e) {
			System.err.println( "Error while writing to '"+ff.getAbsolutePath()+"'" );
			e.printStackTrace();
		}
	}
	
	public String read () {
		return read( file );
	}
	
	public String read ( File ff ) {
		if (ff == null) return null;
		try {
			return FileActions.read( ff );
		} catch (Exception e) {
			System.err.println( "Error while reading from '"+ff.getAbsolutePath()+"'" );
			e.printStackTrace();
			return null;
		}
	}
	
	public void toDirectory () {
		//System.out.println( "toDirectory: "+file.getAbsolutePath() );
		if (dir()) return;
		String value = value();
		mkdir();
		write( valueFile(), value );
	}
	
	public void sort ( File[] files ) {
		Arrays.sort(
			files,
			new Comparator<File>() {
				public int compare(File f1, File f2) {
					try {
						return Integer.valueOf(f1.getName()).compareTo(Integer.valueOf(f2.getName()));
					} catch (Exception e) {
						return f1.getName().compareTo(f2.getName());
					}
				}
			}
		);
	}
	
	public File file () {
		return file;
	}
	
	public void file ( File file ) {
		this.file = file;
	}
	
	@Override
	public Tree create () {
		// does nothing; we need a filesystem object, not a memory object
		return this; // to satisfy return type
	}
	
	public File create ( String key ) {
		return new File( file, key );
	}

	public File valueFile () {
		return create( valueKeyword );
	}

	@Override
	public Map<String,Tree> map () {
		if (dir()) {
			Map<String,Tree> map = new LinkedHashMap<>();
			File[] files = file.listFiles();
			sort( files );
			for (File f : files) {
				String key = f.getName();
				if (key.charAt(0)=='.') continue; // skip hidden, this, & parent dirs
				if (key.equals( valueKeyword )) continue; // skip value file
				Tree branch = new FilesystemTree( f );
				map.put( key, branch );
			}
			return map;
		}
		else return empty;
	}
	
	@Override
	public String value () {
		if (dir()) {
			if (valueFile().exists()) return read( valueFile() );
		} else {
			if (file.exists()) return read();
		}
		return null;
	}

	@Override
	public Tree value ( String value ) {
		if (dir()) write( valueFile(), value );
		else       write( value );
		return this;
	}
	
	@Override
	public Tree map ( Map<String,Tree> map ) {
		clear();
		toDirectory();
		for (String key : map.keySet()) {
			Tree treeBranch = map.get(key);
			add( key, treeBranch ); // recurse; via add( String, Tree )
		}
		return this;
	}

	@Override
	public Tree add ( List<String> values ) {
		if (values!=null) for (String value : values) add( value );
		return this;
	}
	
	@Override
	public Tree add ( String value ) {
		add( integerKey(), value );
		return this;
	}
	
	@Override
	public Tree add ( Tree branch ) {
		add( integerKey(), branch );
		return this;
	}
	
	@Override
	public Tree add ( Map<String,String> map ) {
		toDirectory();
		for (String key : map.keySet()) {
			String value = map.get(key);
			add( key, value );
		}
		return this;
	}
	
	@Override
	public Tree add ( String key, String value ) {
		toDirectory();
		write( create( key ), value );
		return this;
	}
	
	@Override
	public Tree add ( String key, Tree treeBranch ) {
		toDirectory();
		File fileBranch = create( key );
		if (treeBranch.size()==0) { // no map keys, so check for value
			if (treeBranch.value() != null) write( fileBranch, treeBranch.value() );
			else mkdir( fileBranch );
		} else {
			Tree fileBranchObj = new FilesystemTree( fileBranch );
			fileBranchObj.map( treeBranch.map() ); // recurse; via add( Map<String,Tree> )
		}
		return this;
	}

	@Override
	public Tree get ( String key ) {
		File f = create( key );
		if (f.exists()) return new FilesystemTree( f );
		else return null;
	}
	
	@Override
	public Tree auto ( String key ) {
		File f = create( key );
		if (!f.exists()) mkdir( f );
		return new FilesystemTree( f );
	}	
		
	// I/O
	public String serialize () {
		Tree memory = new JSON();
		synchronize( memory );
		return memory.serialize();
	}
	
	public Tree deserialize ( String serial ) throws Exception {
		map(
			new JSON().deserialize( serial ).map()
		);
		return this;
	}
	
	
	public static void main ( String[] args ) throws Exception {
		String json = FileActions.read( args[0] );
		
		Tree fst0 = new FilesystemTree( args[1] );
		fst0.deserialize( json );

		//Tree fst1 = new FilesystemTree( args[1] );
		//System.out.println( fst1.serialize() );
	}
	

}
