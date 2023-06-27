package creek;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;

public class CSVFile implements TableFile {

	private File file;
	private CSV csv;
	
	private static String removeBOM ( String raw ) {
		// remove Byte Order Mark (BOM)
		if (
			raw.charAt(0) == 0xEF &&
			raw.charAt(1) == 0xBB &&
			raw.charAt(2) == 0xBF
		) return raw.substring(3);
		return raw;
	}
	
	
	// constructors

	public CSVFile ( String path ) throws Exception {
		this( new File(path), true, null );
	}

	public CSVFile ( String path, boolean read ) throws Exception {
		this( new File(path), read, null );
	}

	public CSVFile ( String path, boolean read, CSV csv ) throws Exception {
		this( new File(path), read, csv );
	}

	public CSVFile ( File file ) throws Exception {
		this( file, true, null );
	}

	public CSVFile ( File file, boolean read ) throws Exception {
		this( file, read, null );
	}

	public CSVFile ( File file, boolean read, CSV csv ) throws Exception {
		this.file = file;
		this.csv = ( csv == null ? new CSV() : csv );
		if (read) read();
		else clear();
	}
	

	// TableFile interface
	
	public Table table () {
		return csv;
	}

	public TableFile clear () throws Exception {
		Files.write(
			file.toPath(),
			new byte[]{} // empty file
		);
		csv = new CSV( csv.comma(), csv.escape(), csv.quote() );
		return this;
	}

	public TableFile read () throws Exception {
		if (file.exists()) {
			csv = new CSV(
				removeBOM(
					new String(
						Files.readAllBytes( file.toPath() ),
						Charset.defaultCharset()
					)
				), csv.comma(), csv.escape(), csv.quote()
			);
		} else {
			clear();
		}
		return this;
	}

	public TableFile append ( Table table ) throws Exception {
		return write( table, true );
	}
	
	
	public TableFile write ( Table table ) throws Exception {
		return write( table, false );
	}
	
	public TableFile write () throws Exception {
		return write( null, false );
	}
	
	public TableFile write ( Table table, boolean append ) throws Exception {
		CSV newCsv = (
			table == null ?
			csv :
			new CSV( csv.comma(), csv.escape(), csv.quote() )
		);
		newCsv.append( table );
		Files.write(
			file.toPath(),
			newCsv.serial().getBytes(),
			( append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE )
		);
		if (append) csv.append( newCsv );
		else csv = newCsv;
		return this;
	}
	
	public String toString () {
		return csv.toString();
	}
	
	
	public static void main ( String[] args ) {
	
		try {
			CSVFile f0 = new CSVFile( args[0] );
			System.out.println( "********\nf0 empty:\n"+f0 );
			f0.append( SimpleTable.test() );
			System.out.println( "********\nf0 with test() data:\n"+f0 );
			f0.write(
				new CSV(
					"1,2,3\n"+
					"A,B,C\n"
				)
			);
			System.out.println( "********\nf0 overwritten:\n"+f0 );
			
			Thread.sleep(500);
			
			CSVFile f1 = new CSVFile( args[0]+"_2.csv", true, new CSV() );
			System.out.println( "********\nf1 empty:\n"+f1 );
			f1.append( f0.table() );
			System.out.println( "********\nf1 with f0 appended:\n"+f1 );
			
			Thread.sleep(1000);
			
			f1.append(
				new CSV(
					"a,b,c\n"+
					"d,e,f\n"
				)
			);
			System.out.println( "********\nf1 with more data...\n"+f1 );
			
			Thread.sleep(500);
			
			CSVFile f2 = new CSVFile( args[0]+"_2.csv", true, new CSV( "|", "\\", "'" ) );
			System.out.println( "********\nf2 read from f1 file...\n"+f2.table() );

			(new CSVFile( args[0]+"_3.csv" ))
				.append( f2.table() )
				.append( f0.table() );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
