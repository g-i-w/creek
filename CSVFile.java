package creek;

import java.io.*;
import java.util.*;
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
		this( file, new ArrayList<List<String>>(), comma, escape, quote );
	}

	public CSVFile ( String path, CSV csvObj, String comma, String escape, String quote ) throws Exception {
		this( new File(path), csvObj.data(), comma, escape, quote );
	}

	public CSVFile ( File file, CSV csvObj, String comma, String escape, String quote ) throws Exception {
		this( file, csvObj.data(), comma, escape, quote );
	}

	public CSVFile ( File file, List<List<String>> data, String comma, String escape, String quote ) throws Exception {
		super( comma, escape, quote );
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
		for (List<String> row : data) {
			for (int i=0; i<row.size(); i++) {
				if (i > 0) append( comma() );
				append( new String(row.get(i)) );
			}
			append( newline() );
		}
	}
	
	public void addRow () {
		if (initialized) {
			try {
				while(true) {
					if ( writeLock.compareAndSet( false, true ) ) break;
					Thread.sleep(1);
				}
				Files.write(file.toPath(), line(lastRow()).getBytes(), StandardOpenOption.APPEND);
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
	
	protected void appendException ( Exception e ) {
		appendException = e;
		e.printStackTrace();
	}
	
	public Exception appendException () {
		return appendException;
	}
	
	public String exportHTML () {
		StringBuilder html = new StringBuilder();
		html.append( "<table>\n" );
		for (List<String> row : data()) {
			html.append( "\t<tr>\n" );
			for (String item : row) {
				html
					.append( "\t\t<td>" )
					.append( item )
					.append( "</td>\n" )
				;
			}
			html.append( "\t</tr>\n" );
		}
		html.append( "</table>\n" );
		return html.toString();
	}
	
	public String exportHTML ( String path ) throws Exception {
		File file = new File( path );
		String html = exportHTML();
		Files.write(file.toPath(), html.getBytes());
		return html;
	}

	
	public static void main ( String[] args ) {
		try {
		
			// bash command: java creek.CSVFile creek/test_in.csv creek/test_out.csv > creek/test_results.txt
			
			// test input then output
			// creek/test_in.csv -> test_in.csv_out.csv
			
			CSVFile f0 = new CSVFile( args[0] );
			System.out.println( "Read in "+args[0]+":\n"+f0 );
			
			Thread.sleep(500);
			
			CSVFile f1 = new CSVFile( args[0]+"_out.csv", f0, "|", "\\", "'" );
			System.out.println( "Cloned "+args[0]+" using '|':\n"+f1 );
			
			Thread.sleep(1000);
			
			// test output then input
			// creek/test_out.csv -> test_out.csv.html
		
			CSVFile f2 = new CSVFile( args[1] );
			f2.append(
				"a,b,c\n"+
				"d,e,f\n"
			);
			f2.append( "1,\"\"1.1\"\"" );
			f2.append( "2\"3,3.1,3.2\"4" );
			f2.append( ",5,6\\,6.1\\,6.2" );
			System.out.println( "Wrote to "+args[1]+": "+f2 );
			
			Thread.sleep(500);
			
			CSVFile f3 = new CSVFile( args[1] );
			System.out.println( "Read in "+args[1]+": "+f3 );
			
			
			// test HTML export
			
			f3.exportHTML( args[1]+".html" );
			System.out.println( "Exported HTML (table) to "+args[1]+".html" );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
