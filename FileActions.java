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
		Collections.sort(list);
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
		if (append && !file.exists()) file.createNewFile();
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


	public static Table regex ( String path ) throws Exception {
		return Regex.table( read(path), "(\\w+)", new SimpleTable() );
	}

	public static TableFile regex ( File fileOrDir, TableFile tableFile ) throws Exception {
		return regex( fileOrDir, tableFile, "(\\w+)", null );
	}

	public static TableFile regex ( File fileOrDir, TableFile tableFile, String regex, List<String> framing ) throws Exception {
		return regex( fileOrDir, tableFile, regex, framing, false );
	}

	public static TableFile regex ( File fileOrDir, TableFile tableFile, String regex, List<String> framing, boolean verbose ) throws Exception {
		for (File file : recurse(fileOrDir)) {
			if (verbose) System.out.println( "FileActions.regex: reading "+file.getAbsolutePath() );
			tableFile.append(
				Regex.table(
					readLines( file ),
					regex,
					framing,
					new SimpleTable()
				)
			);
		}
		return tableFile;
	}
	
	public static File replace ( File fileOrDir, File newFile, String regex, String replacement ) throws Exception {
		return replace( fileOrDir, newFile, regex, replacement, false );
	}

	public static File replace ( File fileOrDir, File newFile, String regex, String replacement, boolean verbose ) throws Exception {
		for (File file : recurse(fileOrDir)) {
			if (verbose) System.out.println( "FileActions.replace: reading "+file.getAbsolutePath() );
			String replaced = read( file ).replaceAll( regex, replacement );
			write( newFile, replaced.getBytes(), true );
		}
		return newFile;
	}

}

class ExecAddSuffix {

	public static void main ( String[] args ) throws Exception {
		File file = new File( args[0] );
		for( File f : FileActions.recurse( file ) ) {
			System.out.println( f = FileActions.addSuffix( f, args[1] ) );
			f.createNewFile();
		}
	}
}

class ExecRegex {

	public static void main ( String[] args ) throws Exception {
		List<String> framing = new ArrayList<>();
		for (int i=3; i<args.length; i++) framing.add( args[i] );
		
		FileActions.regex( new File(args[0]), new CSVFile(args[1]), args[2], framing, true );
	}
}

class ExecReplace {

	public static void main ( String[] args ) throws Exception {
		FileActions.replace( new File(args[0]), new File(args[1]), args[2], args[3], true );
	}
}

class ExecReplaceNewline {

	public static void main ( String[] args ) throws Exception {
		FileActions.replace( new File(args[0]), new File(args[1]), args[2], "\n", true );
	}
}

