package creek;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.concurrent.atomic.*;

public class CSVFile extends CSV {

	private boolean initialized = false;
	private File file;
	private AtomicBoolean writeLock;
	
	public CSVFile ( String path ) throws Exception {
		this( new File(path) );
	}
	
	public CSVFile ( File file ) throws Exception {
		this( file, ",", "\\" );
	}

	public CSVFile ( File file, String comma, String escape ) throws Exception {
		super( "", comma, escape );
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
				Files.write(file.toPath(), lastLine().getBytes(), StandardOpenOption.APPEND);
				super.addRow();
			} catch (Exception e) {
				appendException(e);
			} finally {
				writeLock.set( false );
			}
		} else {
			super.addRow();
		}
	}
	
	public void appendException ( Exception e ) {
		e.printStackTrace();
	}
	
	public static void main ( String[] args ) {
		try {
			CSVFile f = new CSVFile( args[0] );
			f.append(
				"a,b,c\n"+
				"d,e,f\n"
			);
			System.out.println( "f: "+f );
			
			Thread.sleep(1000);
			
			CSVFile f1 = new CSVFile( args[0] );
			System.out.println( "f1: "+f1 );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
