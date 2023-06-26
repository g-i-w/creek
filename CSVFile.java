package creek;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CSVFile extends CSV {

	private Exception appendException;
	private File file;
	private int colCount = 0;
	private AtomicBoolean writeLock = new AtomicBoolean(true);
		
	private void obtainWriteLock () {
		while (writeLock == null || !writeLock.compareAndSet( false, true )) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void releaseWriteLock () {
		writeLock.set( false );
	}
	
	private String removeBOM ( String raw ) {
		// remove Byte Order Mark (BOM)
		if (
			raw.charAt(0) == 0xEF &&
			raw.charAt(1) == 0xBB &&
			raw.charAt(2) == 0xBF
		) return raw.substring(3);
		return raw;
	}
	
	public CSV readFile () throws Exception {
		if (file.exists()) {
			return new CSV(
				removeBOM(
					new String(
						Files.readAllBytes( file.toPath() ),
						Charset.defaultCharset()
					)
				),
				comma(),
				escape(),
				quote()
			);
		} else {
			Files.write(
				file.toPath(),
				new byte[]{} // empty file
			);
			return new CSV();
		}
	}

	private CSVFile writeFile ( Table table, boolean append ) {
		try {
			Files.write(
				file.toPath(),
				(
					new CSV(
						table,
						comma(),
						escape(),
						quote()
					)
				).serial().getBytes(),
				( append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE )
			);
			if (! append) super.data().clear();
			super.append( table );
		} catch (Exception e) {
			onAppendException( e );
		}
		return this;
	}
	
	
	// constructors

	public CSVFile ( String path ) throws Exception {
		this( path, true, ",", "\\", "\"" );
	}

	public CSVFile ( String path, boolean read ) throws Exception {
		this( path, read, ",", "\\", "\"" );
	}

	public CSVFile ( String path, String comma, String escape, String quote ) throws Exception {
		this( new File(path), true, comma, escape, quote );
	}

	public CSVFile ( String path, boolean read, String comma, String escape, String quote ) throws Exception {
		this( new File(path), read, comma, escape, quote );
	}

	public CSVFile ( File file ) throws Exception {
		this( file, true, ",", "\\", "\"" );
	}

	public CSVFile ( File file, boolean read ) throws Exception {
		this( file, read, ",", "\\", "\"" );
	}

	public CSVFile ( File file, boolean read, String comma, String escape, String quote ) throws Exception {
		super( comma, escape, quote );
		this.file = file;
		if (read) super.append( readFile() );
		releaseWriteLock();
	}
	
	
	void onAppendException ( Exception e ) {
		System.out.println( "we had an exception!" );
		e.printStackTrace();
	}
	
	
	public Table write ( Table table ) {
		obtainWriteLock();
		writeFile( table, false );
		releaseWriteLock();
		return this;
	}
	
	public Table append ( Table table ) {
		obtainWriteLock();
		writeFile( table, true );
		releaseWriteLock();
		return this;
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
			
			CSVFile f1 = new CSVFile( args[0]+"_2.csv", "|", "\\", "'" );
			System.out.println( "********\nf1 empty:\n"+f1 );
			f1.append( f0 );
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
			
			CSVFile f2 = new CSVFile( args[0]+"_2.csv", "|", "\\", "'" );
			System.out.println( "********\nf2 read from f1 file...\n"+f2.data() );

			(new CSVFile( args[0]+"_3.csv" ))
				.append( f2 )
				.append( f0 );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
