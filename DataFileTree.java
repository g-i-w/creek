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
	
	public String type () {
		File typeFile = createFile("type");
		if (typeFile.exists()) return read(typeFile);
		else return null;
	}
	
	public void type ( String type ) {
		write( createFile("type"), type );
	}
	
	public Map<String,Tree> mapJSON () {
		try {
			return new JSON().deserialize( value() ).map();
		} catch (Exception e) {
			e.printStackTrace();
			return super.map();
		}
	}
	
	public Map<String,Tree> mapCSV () {
		try {
			return new JSON().data( new CSV().append( value() ).data() ).map();
		} catch (Exception e) {
			e.printStackTrace();
			return super.map();		
		}
	}
	
	public int teir () {
		return teir;
	}
	
	@Override
	public Tree createTree ( File f ) {
		//System.out.println( "teir: "+teir+", max: "+maxTeirs );
		return new DataFileTree( f, teir+1, maxTeirs );
	}
	
	@Override
	public Tree map ( Map<String,Tree> map ) {
		if (teir > maxTeirs && map != null) {
			System.out.println( "add( Map<String,String> )" );
			clear();
			toDirectory();
			for (String key : map.keySet()) {
				Tree tree = map.get(key);
				DataFileTree dft = new DataFileTree( createFile( key ), teir+1, maxTeirs );
				if (canBeCSV(tree)) {
					// CSV
					dft.toDirectory();
					dft.value( new CSV().data( tree.paths() ).serial() );
					dft.type( "CSV" );
				} else if (canBeJSON(tree)) {
					// JSON
					dft.toDirectory();
					dft.value( new JSON().map( tree.map() ).serialize() );
					dft.type( "JSON" );
				} else {
					System.out.println( "value" );
					dft.value( tree.value() );
				}
			}
		} else {
			System.out.println( "map( Map<String,Tree> )" );
			super.map( map );
		}
		return this;
	}
	
	@Override
	public Map<String,Tree> map () {
		String type = type();
		System.out.println( type );
		if (type != null && type.equals("JSON")) return mapJSON();
		if (type != null && type.equals("CSV")) return mapCSV();
		return super.map();
	}
	
	
	public static void main ( String[] args ) throws Exception {
		String json = FileActions.read( args[0] );
		System.out.println( json );
		
		Tree d0 = new DataFileTree( new File(args[1]), 1 );
		d0.deserialize( json );

		Tree d1 = new DataFileTree( new File(args[1]), 1 );
		System.out.println( d1.serialize() );
	}
	
}


