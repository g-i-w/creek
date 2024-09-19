package creek;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.*;

public class FileActions {

	public static final Charset UTF8 = StandardCharsets.UTF_8;
	public static final Charset UTF16 = StandardCharsets.UTF_16;
	public static final Charset UTF16BE = StandardCharsets.UTF_16BE;
	public static final Charset UTF16LE = StandardCharsets.UTF_16LE;
	public static final Charset ASCII = StandardCharsets.US_ASCII;

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
	
	public static Comparator<File> fileComparator () {
		return new Comparator<File>() {
			public int compare(File f1, File f2) {
				if ( f1.getParentFile().equals( f2.getParentFile() ) ) {
					try {
						return Integer.valueOf(f1.getName()).compareTo(Integer.valueOf(f2.getName()));
					} catch (Exception e) {
						return f1.getName().compareTo(f2.getName());
					}
				} else {
					return f1.compareTo(f2);
				}
			}
		};
	}
	
	public static List<File> sortFiles ( List<File> files ) {
		return sortFiles( files.toArray(new File[0]) );
	}

	public static List<File> sortFiles ( File[] files ) {
		Arrays.sort( files, fileComparator() );
		return Arrays.asList( files );
	}

	public static List<File> dir ( File dir ) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) return new ArrayList<File>(0);
		File[] files = dir.listFiles();
		sortFiles( files );
		List<File> dirList = new ArrayList<>(files.length);
		for (File f : files) {
			if (f.getName().charAt(0)=='.') continue; // skip hidden files, this, & parent dirs
			dirList.add( f );
		}
		return dirList;
	}
	
	public static List<File> recurse ( String path ) {
		return sortFiles( recurse( new File(path) ) );
	}

	public static List<File> recurse ( File file ) {
		return sortFiles( recurse( file, new ArrayList<File>() ) );
	}

	public static List<File> recurse ( File file, List<File> list ) {
		if (file == null || !file.exists()) return new ArrayList<File>(0);
		if (file.getName().charAt(0)!='.') {
			if (file.isDirectory()) {
				for (File f : file.listFiles()) recurse( f, list );
			} else {
				list.add( file );
			}
		}
		//Collections.sort(list);
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
	
	public static String minName ( String path ) {
		return name( (new File(path)).getName() );
	}

	public static String minName ( File file ) {
		return name( file.getName() );
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

	// Read/write File object

	public static List<String> readLines ( File file ) throws Exception {
		return readLines( file, Charset.defaultCharset() );
	}
	
	public static List<String> readLines ( File file, Charset charset ) throws Exception {
		return Files.readAllLines( file.toPath(), charset );
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

	public static File write ( File file, String input, String encoding, boolean append ) throws Exception {
		//byte[] output = Charset.forName(encoding).encode(input).array();
		byte[] output = input.getBytes( encoding );
		return write( file, output, append );
	}
	
	public static File write ( File file, byte[] bytes, boolean append ) throws Exception {
		if (append) {
			// append operation
			if (!file.exists()) file.createNewFile();
		} else {
			// overwrite operation
			if (file.exists()) file.delete();
			file.createNewFile();
		}
		Files.write(
			file.toPath(),
			bytes,
			( append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE )
		);
		return file;
	}
	
	// Read/write String path
	
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

	public static File write ( String path, String input, String encoding ) throws Exception {
		return write( new File( path ), input, encoding, false );
	}

	// Regexs

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
		return regex( fileOrDir, tableFile, "(\\w+)", false, "(\\w+)" );
	}

	public static TableFile regex ( File fileOrDir, TableFile tableFile, String regex, boolean verbose, String pathRegex ) throws Exception {
		for (File file : recurse(fileOrDir)) {
			if (verbose) System.err.println( "FileActions.regex: reading "+file.getAbsolutePath() );
			tableFile.append(
				Regex.table(
					readLines( file ), // read lines
					regex,
					new CSV(),
					Regex.groups( file.getPath(), pathRegex )
				)
			);
		}
		return tableFile;
	}
	
	public static TableFile regexBlob ( File fileOrDir, TableFile tableFile, String regex ) throws Exception {
		return regexBlob( fileOrDir, tableFile, regex, false, "(\\w+)" );
	}

	public static TableFile regexBlob ( File fileOrDir, TableFile tableFile, String regex, boolean verbose, String pathRegex ) throws Exception {
		for (File file : recurse(fileOrDir)) {
			if (verbose) System.err.println( "FileActions.regexBlob: reading "+file.getAbsolutePath() );
			tableFile.append(
				Regex.table(
					read( file ), // read blob
					regex,
					new CSV(),
					Regex.groups( file.getPath(), pathRegex )
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
	
	// misc
	
	public static TableFile derive ( TableFile parent, String suffix, Table table ) throws Exception {
		return parent
			.create( addSuffix( parent.file(), suffix ) )
			.append( table );
	}
	
	public static List<TableFile> split ( TableFile parent, int col, String[] breakRegex ) throws Exception {
		List<TableFile> files = new ArrayList<>();
		Table tempTable = new CSV();
		
		int breakRegexIndex = 0;
		int fileCount = 0;
		
		for (List<String> row : parent.table().data()) {
			if (
				breakRegexIndex<breakRegex.length
				&& col<row.size()
				&& Regex.exists( row.get(col), breakRegex[breakRegexIndex] )
			) {
				// increment index
				breakRegexIndex++;
				// create a new TableFile, append temp Table, and add to list
				files.add( derive( parent, "."+(++fileCount), tempTable ) );
				// create a new temp Table
				tempTable = new CSV();
			}
			tempTable.append( row );
		}
		
		if (tempTable.rowCount()>0) {
			// add remainder rows to a final file
			files.add( derive( parent, "."+(++fileCount), tempTable ) );
		}

		return files;
	}
	
	public static Table combine ( Table table, List<TableFile> files ) {
		for (TableFile file : files) {
			table.append( file.table() );
		}
		return table;
	}
	
	public static Table combine ( Table table, File parent ) throws Exception {
		int fileCount = 0;
		File subFile;
		while (
			(
				subFile = addSuffix( parent, "."+(++fileCount) ) // assign-eval
			).exists()
		) {
			table.append(
				(new CSVFile(subFile)).table()
			);
		}
		return table;
	}
	
	public static Table combine ( Table table, String path ) throws Exception {
		return combine( table, new File(path) );
	}


}

class ExecSplit {
	public static void main ( String[] args ) throws Exception {
		String[] breakPoints = Arrays.copyOfRange( args, 2, args.length );
		FileActions.split( new CSVFile(args[0]), Integer.parseInt(args[1]), breakPoints );
	}
}

class ExecCombine {
	public static void main ( String[] args ) throws Exception {
		new CSVFile( args[0], false, FileActions.combine( new CSV(), args[0] ) );
	}
}

class ExecConvertText {
	// <input> <output> <encoding>
	public static void main ( String[] args ) throws Exception {
		FileActions.write( args[1], FileActions.read( args[0] ), args[2] );
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

class ExecRegexReplace {
	// <oldFile> <newFile> <regexFile> <subOld> <subNew>
	public static void main ( String[] args ) throws Exception {
		String regex = FileActions.read( args[2] );
		FileActions.replace( new File(args[0]), new File(args[1]), regex, null, args[3], args[4] );
	}
}

class ExecRegexCSV {

	public static void main ( String[] args ) throws Exception {
		System.err.println( "ExecRegexCSV: "+args[2]+", "+args[3] );
		FileActions.regex( new File(args[0]), new CSVFile(args[1], false), args[2], true, args[3] );
	}
}

class ExecRegexBlobCSV {

	public static void main ( String[] args ) throws Exception {
		System.err.println( "ExecRegexBlobCSV: "+args[2]+", "+args[3] );
		FileActions.regexBlob( new File(args[0]), new CSVFile(args[1], false), args[2], true, args[3] );
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

class ExecRemoveDuplicatesCSV {

	public static void main ( String[] args ) throws Exception {
		TableFile input = new CSVFile(args[0]);
		if (args.length>2) new CSVFile( args[1], false, input.table().set(Integer.parseInt(args[2])) );
		else new CSVFile( args[1], false, input.table().set() );
	}
}

