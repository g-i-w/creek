package creek;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

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

	public static List<String> readLines ( File file ) throws Exception {
		return Files.readAllLines( file.toPath() );
	}
	
	public static byte[] readBytes ( File file ) throws Exception {
		return Files.readAllBytes( file.toPath() );
	}
	
	public static String read ( File file ) throws Exception {
		return new String( readBytes( file ), Charset.defaultCharset() );
	}
		
	public static String read ( File file, Charset charset ) throws Exception {
		return new String( readBytes( file ), charset );
	}
	
	public static File write ( File file, byte[] bytes ) throws Exception {
		return write( file, bytes, false );
	}

	public static File write ( File file, byte[] bytes, boolean append ) throws Exception {
		Files.write(
			file.toPath(),
			bytes,
			( append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE )
		);
		return file;
	}
	

	public static List<String> readLines ( String path ) throws Exception {
		return readLines( new File( path ) );
	}
	
	public static byte[] readBytes ( String path ) throws Exception {
		return readBytes( new File( path ) );
	}
	
	public static String read ( String path ) throws Exception {
		return read( new File( path ) );
	}
	
	public static File write ( String path, String text ) throws Exception {
		return write( new File( path ), text.getBytes() );
	}

		

	public static TableFile regexGroups ( File rawFile, TableFile tableFile, String regex ) throws Exception {
		return tableFile.append(
			Tables.regexGroups(
				readLines( rawFile ),
				regex
			)
		);
	}
	
	
	// Test methods

	public static void test_addSuffix ( String args[] ) throws Exception {
		File file = new File( args[0] );
		for( File f : FileActions.recurse( file ) ) {
			System.out.println( f = addSuffix( f, "_TEST_SUFFIX_" ) );
			f.createNewFile();
		}
	}
	
	public static void test_regexGroups ( String args[] ) throws Exception {
		System.out.println(
			regexGroups( new File(args[0]), new CSVFile(args[1]), args[2] )
		);
	}

	public static void main ( String[] args ) throws Exception {
		test_addSuffix( args );
		test_regexGroups( args );
	}

}
