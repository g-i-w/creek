package creek;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class FileActions {


	public static List<File> recurse ( File file ) {
		return recurse( file, new ArrayList<File>() );
	}

	public static List<File> recurse ( File file, List<File> list ) {
		if (file.getName().indexOf(".")!=0) {
			if (file.isDirectory()) {
				for (File f : file.listFiles()) recurse( f, list );
			} else {
				list.add( file );
			}
		}
		return list;
	}
	
	public static boolean hasExtension ( String fileName ) {
		int lastPeriod = fileName.lastIndexOf(".");
		return ( lastPeriod > 0 && lastPeriod < fileName.length()-2 );
	}
	
	public static String name ( String fileName ) {
		if (hasExtension(fileName)) return fileName.substring( 0, fileName.lastIndexOf(".") );
		else return fileName;
	}

	public static String extension ( String fileName ) {
		if (hasExtension(fileName)) return fileName.substring( fileName.lastIndexOf("."), fileName.length() );
		else return "";
	}
	
	public static File addSuffix ( File file, String suffix ) throws Exception {
		String path = file.getAbsolutePath();
		String newPath = name( path ) + suffix + extension( path );
		File newFile = new File( newPath );
		if (newFile.exists()) throw new Exception( newPath+" already exists!" );
		return newFile;
	}
	
	
	public static void main ( String[] args ) throws Exception {
		File file = new File( args[0] );
		for( File f : FileActions.recurse( file ) ) {
			System.out.println( f = addSuffix( f, "_TEST_SUFFIX_" ) );
			f.createNewFile();
		}
	}

}
