package creek;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

public class FileActions {

	public static File pwd () {
		return new File( System.getProperty("user.dir") );
	}

	public static boolean exists ( String path ) {
		if (path==null) return false;
		return exists( new File(path), null );
	}
	
	public static boolean exists ( String[] path ) {
		return exists( null, path );
	}

	public static boolean exists ( File base, String[] path ) {
		if (base==null) base = pwd();
		if (path==null) return base.exists();
		StringBuilder fullPath = new StringBuilder();
		fullPath.append( base.getAbsolutePath() );
		for (String level : path) fullPath.append( File.separator ).append( level );
		return exists( fullPath.toString() );
	}

	public static List<File> recurse ( String path ) {
		return recurse( new File(path) );
	}

	public static List<File> recurse ( File file ) {
		return recurse( file, new ArrayList<File>() );
	}

	public static List<File> recurse ( File file, List<File> list ) {
		if (file == null || !file.exists()) return new ArrayList<File>();
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
	
	public static File auto ( String[] path ) throws Exception {
		return auto( path, null );
	}
	
	public static File auto ( String[] path, byte[] data ) throws Exception {
		return auto( pwd(), path, data );
	}
	
	public static File auto ( File file, String[] path, byte[] data ) throws Exception {
		StringBuilder newPath = new StringBuilder();
		if (file.isDirectory()) {
			newPath.append( file.getAbsolutePath() );
		} else {
			Path parent = file.toPath().getParent();
			String parentPath = ( parent != null ? parent.toAbsolutePath().toString() : "" );
			newPath.append( parentPath );
		}
		for (int i=0; i<path.length-1; i++) {
			file = new File( newPath.append( File.separator ).append( path[i] ).toString() );
			if ( !(file.exists() || file.mkdir()) ) throw new Exception( "Unable to create directory "+newPath );
		}
		file = new File( newPath.append( File.separator ).append( path[path.length-1] ).toString() );
		if (data != null) write( file, data );
		return file;
	}
	
	public static boolean hasExtension ( String fileName ) {
		int lastPeriod = fileName.lastIndexOf(".");
		return ( lastPeriod > 0 && lastPeriod < fileName.length()-2 );
	}
	
	public static String name ( File file ) {
		return name( file.getAbsolutePath() );
	}

	public static String name ( String fileName ) {
		if (hasExtension(fileName)) return fileName.substring( 0, fileName.lastIndexOf(".") );
		else return fileName;
	}

	public static String extension ( File file ) {
		return extension( file.getName() );
	}
	
	public static String extension ( String fileName ) {
		if (hasExtension(fileName)) return fileName.substring( fileName.lastIndexOf(".")+1, fileName.length() );
		else return "";
	}
	
	public static File replaceExtension ( File file, String extension ) {
		return new File( name(file) + "." + extension );
	}
	
	public static File replaceExtension ( String path, String extension ) {
		return new File( name(path) + "." + extension );
	}
	
	public static File addSuffix ( File file, String suffix ) {
		return new File( name( file ) + suffix + "." + extension( file ) );
	}

	public static File addSuffix ( String path, String suffix ) {
		return new File( name( path ) + suffix + "." + extension( path ) );
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
		if (!file.exists()) file.createNewFile();
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
		return regex( path, "(\\w+)" );
	}

	public static Table regex ( String path, String regex ) throws Exception {
		return regex( path, regex, new SimpleTable() );
	}

	public static Table regex ( String path, String regex, Table table ) throws Exception {
		return Regex.table( readLines(path), regex, table );
	}

	public static Table regexBlob ( String path, String regex, Table table ) throws Exception {
		return Regex.table( read(path), regex, table );
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
			tableFile.write(
				Regex.table(
					readLines( file ), // read lines
					regex,
					framing,
					new CSV()
				)
			);
		}
		return tableFile;
	}
	
	public static TableFile regexBlob ( File fileOrDir, TableFile tableFile, String regex ) throws Exception {
		return regexBlob( fileOrDir, tableFile, regex, false );
	}

	public static TableFile regexBlob ( File fileOrDir, TableFile tableFile, String regex, boolean verbose ) throws Exception {
		for (File file : recurse(fileOrDir)) {
			if (verbose) System.out.println( "FileActions.regex: reading "+file.getAbsolutePath() );
			tableFile.write(
				Regex.table(
					read( file ), // read blob
					regex,
					new CSV()
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
	
	public static File replace ( File file, File newFile, String regex, List<String> framing, String subOld, String subNew ) throws Exception {
		String input = read( file );
		String output = Regex.replace( input, regex, framing, subOld, subNew );
		return write( newFile, output.getBytes() );
	}

}

class ExecAuto {

	public static void main ( String[] args ) throws Exception {
		String[] path = new String[args.length-1];
		System.arraycopy( args, 0, path, 0, args.length-1 );
		FileActions.auto( path, args[args.length-1].getBytes() );
	}
}

class ExecAddSuffix {

	public static void main ( String[] args ) throws Exception {
		for( File f0 : FileActions.recurse( new File( args[0] ) ) ) {
			File f1 = FileActions.addSuffix( f0, args[1] );
			System.out.println( f1 );
			Files.move(f0.toPath(), f1.toPath(), StandardCopyOption.ATOMIC_MOVE);
		}
	}
}

class ExecReplaceExtension {

	public static void main ( String[] args ) throws Exception {
		for( File f0 : FileActions.recurse( new File( args[0] ) ) ) {
			if (FileActions.extension(f0).equals( args[1] )) {
				File f1 = FileActions.replaceExtension( f0, args[2] );
				System.out.println( f1 );
				Files.move(f0.toPath(), f1.toPath(), StandardCopyOption.ATOMIC_MOVE);
			}
		}
	}
}

class ExecRegex {

	public static void main ( String[] args ) throws Exception {
		List<String> framing = new ArrayList<>();
		for (int i=3; i<args.length; i++) framing.add( args[i] );
		System.err.println( args[2] );
		FileActions.regex( new File(args[0]), new CSVFile(args[1]), args[2], framing, true );
	}
}

class ExecRegexReplace {
	// <oldFile> <newFile> <regexFile> <subOld> <subNew>
	public static void main ( String[] args ) throws Exception {
		String regex = FileActions.read( args[2] );
		FileActions.replace( new File(args[0]), new File(args[1]), regex, null, args[3], args[4] );
	}
}

class ExecRegexBlob {

	public static void main ( String[] args ) throws Exception {
		System.err.println( args[2] );
		FileActions.regexBlob( new File(args[0]), new CSVFile(args[1]), args[2], true );
	}
}

class ExecReplace {

	public static void main ( String[] args ) throws Exception {
		FileActions.replace( new File(args[0]), new File(args[1]), args[2], args[3], true );
	}
}

class ExecReplaceWithNewline {

	public static void main ( String[] args ) throws Exception {
		FileActions.replace( new File(args[0]), new File(args[1]), args[2], System.lineSeparator(), true );
	}
}

class ExecReplaceNewlineWith {

	public static void main ( String[] args ) throws Exception {
		FileActions.replace( new File(args[0]), new File(args[1]), "[\r\n]+", args[2], true );
	}
}

class ExecSubstring {

	public static void main ( String[] args ) throws Exception {
		String input = FileActions.read(args[0]);
		String output = input.substring( Integer.parseInt(args[2]), Integer.parseInt(args[3]) );
		FileActions.write( args[1], output );
	}
}

class ExecSubstringFind {

	public static void main ( String[] args ) throws Exception {
		String input = FileActions.read(args[0]);
		String output = input.substring( input.indexOf(args[2]), input.indexOf(args[3]) );
		FileActions.write( args[1], output );
	}
}

