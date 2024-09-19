package creek;

import java.util.*;
import java.io.*;

public class DataFileTree extends FilesystemTree {

	private static Map<File,Tree> dataFiles = new LinkedHashMap<>();
	private static final int maxDataFiles = 10;

	private int teir;
	private int maxTeirs;

	public DataFileTree ( File file, int maxTeirs ) {
		this( file, 1, maxTeirs );
	}

	public DataFileTree ( File file, int teir, int maxTeirs ) {
		super( file );
		this.teir = teir;
		this.maxTeirs = maxTeirs;
	}
	
	private boolean canBeCSV ( Tree tree ) {
		if (tree==null || tree.size()==0 || !tree.integerKeys()) return false; // level 0: integer keys
		for (Tree level1 : tree.branches()) {
			if (!level1.integerKeys()) return false; // level 1: integer keys
			for (Tree level2 : level1.branches()) {
				if (level2.size()>0) return false; // level 2: no keys, proving subBranch is a "leaf"
			}
		}
		return true;
	}
	
	private boolean canBeJSON ( Tree tree ) {
		if (tree==null || tree.size()==0 ) return false;
		return tree.routes( null, new HashSet<Tree>() ); // check for recursive loops, which JSON format cannot natively handle
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
	
	public File typeFile () {
		return fileFromKey( "type" );
	}
	
	public String type () {
		if (dir()) {
			File typeFile = typeFile();
			if (typeFile.exists()) return read(typeFile);
			else return "";
		} else {
			return FileActions.extension( file() ).toUpperCase();
		}
	}
	
	public void type ( String type ) {
		write( fileFromKey("type"), type );
	}
	
	public void createTreeFile ( String key, Tree tree ) {
		if (canBeCSV(tree)) {
			// CSV
			write( fileFromKey( key+".csv" ), new CSV().data( tree.paths() ).serial() );
		} else if (canBeJSON(tree)) {
			// JSON
			write( fileFromKey( key+".json" ), new JSON().map( tree.map() ).serialize() );
		} else {
			write( fileFromKey( key ), tree.value() );
		}
	}
	
	@Override
	public Tree createTree ( File f ) {
		//System.out.println( "teir: "+teir+", max: "+maxTeirs );
		return new DataFileTree( f, teir+1, maxTeirs );
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
		if (teir > maxTeirs) {
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
	
	public Map<String,Tree> map () {
		if (dir()) {
			Map<String,Tree> map = new LinkedHashMap<>();
			for (File f : FileActions.dir(file())) {
				String key = keyFromFile( f );
				DataFileTree branch = new DataFileTree( f, teir+1, maxTeirs );
				String type = branch.type();
				String value = branch.value();
				if (type.equals("JSON")) map.put( key, JSON( value, "Error parsing "+branch.file() ) );
				else if (type.equals("CSV")) map.put( key, CSV( value, "Error parsing "+branch.file() ) );
				else map.put( key, branch );
			}
			return map;
		}
		else return empty;
	}

	@Override
	public Tree get ( String key ) {
		return map().get(key);
	}
	
	public static void main ( String[] args ) throws Exception {
		String json = FileActions.read( args[0] );
		System.out.println( json );
		
		Tree d0 = new DataFileTree( new File(args[1]), 1 );
		d0.deserialize( json );

		Tree d1 = new DataFileTree( new File(args[1]), 1 );
		d1.auto( "test2" ).auto( "hello" ).auto( "1" ).auto( "2" ).auto( "3" );
		System.out.println( d1.serialize() );
	}
	
}


