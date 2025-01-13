package creek;

import java.util.*;
import java.io.*;

public class DataFileTree extends FilesystemTree {

	private static Map<File,Tree> dataFiles = new LinkedHashMap<>();
	private static final int maxDataFiles = 10;

	private int teir;
	private int maxTeirs;
	private boolean debug;
	private Stats stats;

	public DataFileTree ( File file ) {
		this( file, 1, -1, false );
	}

	public DataFileTree ( File file, boolean debug ) {
		this( file, 1, -1, debug );
	}

	public DataFileTree ( File file, int maxTeirs ) {
		this( file, 1, maxTeirs, false );
	}

	public DataFileTree ( File file, int teir, int maxTeirs ) {
		this( file, teir, maxTeirs, false );
	}
	
	public DataFileTree ( File file, int teir, int maxTeirs, boolean debug ) {
		super( file );
		this.teir = teir;
		this.maxTeirs = maxTeirs;
		this.debug = debug;
		if (debug) stats = new Stats( getClass().getName() );
	}
	
	public Tree JSON ( String value, String errorMsg ) {
		try {
			return new JSON().deserialize( value );
		} catch (Exception e) {
			System.err.println( errorMsg );
			e.printStackTrace();
			return null;
		}
	}
	
	public Tree CSV ( String value, String errorMsg ) {
		try {
			return new JSON().data( new CSV().append( value ).data() );
		} catch (Exception e) {
			System.err.println( errorMsg );
			e.printStackTrace();
			return null;
		}
	}
	
	public int teir () {
		return teir;
	}
	
	public void createTreeFile ( String key, Tree tree ) {
		if (tree==null) {
			write( fileFromKey( key ), "" );
		} else if (tree.size()==0) {
			// no keys
			write( fileFromKey( key ), tree.value() );
		} else {
			// at least one key
			write( fileFromKey( key+".json" ), new JSON().map( tree.map() ).serialize() );
		}
	}
	
	@Override
	public Tree createTree ( File f ) {
		//System.out.println( "teir: "+teir+", max: "+maxTeirs );
		return new DataFileTree( f, teir+1, maxTeirs, debug );
	}
	
	@Override
	public File fileFromKey ( String key ) {
		File testFile = super.fileFromKey( key );
		if (!testFile.exists()) { // if it doesn't exist, check for file names with extensions
			for (File extendedFile : FileActions.dir( file() )) {
				if (FileActions.minName( extendedFile ).equals( key )) return extendedFile; // file with an extension
			}
		}
		return testFile;
	}
	
	@Override
	public Tree map ( Map<String,Tree> map ) {
		if (map==null) return this;
		if (teir >= maxTeirs) {
			clear();
			toDirectory();
			for (String key : map.keySet()) {
				Tree tree = map.get(key);
				createTreeFile( key, tree );
			}
		} else {
			super.map( map );
		}
		return this;
	}
	
	@Override
	public String keyFromFile( File f ) {
		return FileActions.minName( f );
	}
	
	@Override
	public Map<String,Tree> map () {
		if (dir()) {
			return super.map();
		} else {
			String key = keyFromFile( file() );
			String type = FileActions.extension( file() ).toUpperCase();
			if (debug) stats.display( "reading "+file() );
			String value = value();
			if (debug) stats.display( "done" );
			if (type.equals("JSON")) {
				if (debug) stats.display( "parsing JSON: "+file() );
				Tree fromJson = JSON( value, "Error parsing "+file() );
				if (debug) stats.display( "done" );
				return fromJson.map();
			} else if (type.equals("CSV")) {
				if (debug) stats.display( "parsing CSV: "+file() );
				Tree fromCsv = CSV( value, "Error parsing "+file() );
				if (debug) stats.display( "done" );
				return fromCsv.map();
			} else {
				return empty;
			}
		}
	}

	@Override
	public Tree get ( String key ) {
		return map().get( key );
	}
	
	// testing
	/*public static void main ( String[] args ) throws Exception {
		String json = FileActions.read( args[0] );
		System.out.println( json );
		
		Tree d0 = new DataFileTree( new File(args[1]), 1 );
		d0.deserialize( json );

		Tree d1 = new DataFileTree( new File(args[1]), 1 );
		d1.auto( "test2" ).auto( "hello" ).auto( "1" ).auto( "2" ).auto( "3" );
		d1.auto( "test1" ).auto( "101" ).auto( "d" ).auto( "e" );
		System.out.println( d1.serialize() );
	}*/
	
	// general purpose data conversion
	public static void main ( String[] args ) throws Exception {
		System.out.println( Arrays.asList(args) );
		
		// FROM: JSON file
		String json = FileActions.read( args[0] );
		
		// TO: directory
		Tree d0 = new DataFileTree( new File(args[1]), Integer.valueOf(args[2]) );

		// WARNING: will erase and overwrite!!!
		d0.deserialize( json );
	}
	
}


