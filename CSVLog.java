package creek;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class CSVLog extends CSVFile implements LogFile {

	private String dateTimeFormat;
	
	private String timestamp () {
		return timestamp( LocalDateTime.now() );
	}

	private String timestamp ( LocalDateTime time ) {
		return DateTimeFormatter.ofPattern( dateTimeFormat ).format( time );
	}
	
	private CSVLog newLog ( Table subTable ) throws Exception {
		//System.out.println( "subTable: "+subTable );
		//System.out.println( "item 0,0: "+subTable.item(0,0) );
		File log = FileActions.addSuffix( file(), "_"+subTable.item(0,0) );
		return new CSVLog( log, subTable, dateTimeFormat, ((CSV)super.table()).comma() );
	}
	

	public CSVLog ( String path ) throws Exception {
		this( new File( path ) );
	}
	
	public CSVLog ( File file ) throws Exception {
		this( file, null, "yyyy-MM-dd_HHmmss_SSS", "," );
	}

	public CSVLog ( File file, Table table, String dateTimeFormat, String comma ) throws Exception {
		super( file, true, table, comma );
		this.dateTimeFormat = dateTimeFormat;
	}
	
	
	// Extra CSVFile functionality
	
	public LogFile trimmed ( LocalDateTime oldest ) throws Exception {
		return newLog( (new IndexedTable(table())).last( 0, timestamp(oldest) ) );	
	}
	
	
	// LogFile interface

	public SetTable table () {
		return new IndexedTable( super.table() );
	}
	
	public LogFile trimmed ( int rows ) throws Exception {
		//System.out.println( "table.last: "+table().last( rows ) );
		return newLog( (new CSV()).data( table().last( rows ).data() ) );
	}
	
	public LogFile append ( List<String> sample ) throws Exception {
		LinkedList<String> timeSample = new LinkedList<>( sample );
		timeSample.addFirst( timestamp() );
		super.append( (new CSV()).append(timeSample) );
		return this;
	}
	
	public LogFile append ( String[] row ) throws Exception {
		if (row == null) return this;
		append( Arrays.asList( row ) );
		return this;
	}
	
	// testing
	public static void main ( String[] args ) {
		try {
			CSVLog log = new CSVLog( new File(args[0]), null, "yyyy-MM-dd_HHmmss_SSS", "\t" );
			log.append( new String[]{"a","b","c"} );
			System.out.println( "CSVLog:\n"+log.table().data() );

			Thread.sleep(1000);
			log.append( new String[]{"d","e","f"} );
			System.out.println( "CSVLog:\n"+log.table() );
			
			Thread.sleep(1000);
			log.append( new String[]{"g","h","i"} );
			System.out.println( "CSVLog:\n"+log.table() );
			
			LogFile log2 = log.trimmed( 2 );
			System.out.println( "log:\n"+log );
			System.out.println( "last 2 entries:\n"+log2 );
			
			LogFile log3 = log.trimmed( LocalDateTime.now().minusSeconds(5) );
			System.out.println( "last 5 seconds of entries:\n"+log3 );
			
			System.out.println( "x:\n"+(new CSV()).append( log3.table().col( 0 ) ) );
			System.out.println( "y:\n"+(new CSV()).append( log3.table().col( 1 ) ) );
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}


}
