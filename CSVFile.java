package creek;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.concurrent.atomic.*;

public class CSVFile extends CSVLookup {

	private boolean initialized = false;
	private Exception appendException;
	private File file;
	private AtomicBoolean writeLock;
	
	public CSVFile ( String path ) throws Exception {
		this( new File(path) );
	}
	
	public CSVFile ( File file ) throws Exception {
		this( file, ",", "\\", "\"" );
	}

	public CSVFile ( File file, String comma, String escape, String quote ) throws Exception {
		super( "", comma, escape, quote );
		initialized = false;
		this.file = file;
		writeLock = new AtomicBoolean(false);
		if (file.exists()) {
			append(
				new String(
					Files.readAllBytes( file.toPath() ), Charset.defaultCharset()
				)
			);
		} else {
			Files.write( file.toPath(), new byte[]{} );
		}
		initialized = true;
	}
	
	public void addRow () {
		if (initialized) {
			try {
				while(true) {
					if ( writeLock.compareAndSet( false, true ) ) break;
					Thread.sleep(1);
				}
				super.addRow();
				Files.write(file.toPath(), lastLine().getBytes(), StandardOpenOption.APPEND);
			} catch (Exception e) {
				appendException(e);
			} finally {
				writeLock.set( false );
			}
		} else {
			super.addRow();
		}
	}
	
	protected void appendException ( Exception e ) {
		appendException = e;
		e.printStackTrace();
	}
	
	public Exception appendException () {
		return appendException;
	}
	
	public static void main ( String[] args ) {
		try {
			CSVFile f = new CSVFile( args[0] );
			f.append(
				"a,b,c\n"+
				"d,e,f\n"
			);
			f.append( "1," );
			f.append( "234" );
			f.append( ",5,6" );
			System.out.println( "f: "+f );
			f.append( "\n" );
			System.out.println( "f: "+f );
			
			Thread.sleep(1000);
			
			CSVFile f1 = new CSVFile( args[0] );
			System.out.println( "f1: "+f1 );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
