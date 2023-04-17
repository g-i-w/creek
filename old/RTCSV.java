package creek;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RTCSV implements Text,Grid {

	// data & stats
	private List<List<String>> data;
	private int width = 0;
	
	// used by appendInternal
	private String comma;
	private String escape;
	private String quote;
	private List<String> rowUnderConstruction;
	private StringBuilder itemUnderConstruction;
	
	// states
	private static final int LINE_START_STATE = 0;
	private static final int DATA_STATE = 1;
	private static final int COMMA_STATE = 2;
	private static final int ESCAPE_STATE = 3;
	private static final int LINE_END_STATE = 4;
	private static final int QUOTE_DATA_STATE = 5;
	private static final int QUOTE_END_STATE = 6;
	private static final int QUOTE_ESCAPE_STATE = 7;
	
	private int state = LINE_START_STATE;

	//private static final String[] reverse_state = {
	//	"LINE_START_STATE","DATA_STATE","COMMA_STATE","ESCAPE_STATE","LINE_END_STATE","QUOTE_DATA_STATE","QUOTE_END_STATE","QUOTE_ESCAPE_STATE"
	//};


	// concurrency
	private AtomicBoolean writeLock = new AtomicBoolean(true);
	
	private void obtainWriteLock () {
		while (! writeLock.compareAndSet( false, true )) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void releaseWriteLock () {
		version.add( data.size() );
		writeLock.set( false );
	}
	
	
	
	// constructors

	public CSV () {
		this( new ArrayList<List<String>>(), ",", "\\", "\"" );
	}
	
	public CSV ( String csv ) {
		this( csv, ",", "\\", "\"" );
	}

	public CSV ( String csv, String comma ) {
		this( csv, comma, "\\", "\"" );
	}

	public CSV ( String csv, String comma, String escape, String quote ) {
		this.comma = comma;
		this.escape = escape;
		this.quote = quote;
		this.data = new ArrayList<List<String>>();
		appendInternal( csv );
		releaseWriteLock();
	}
	
	public CSV ( List<List<String>> data ) {
		this( data, ",", "\\", "\"" );
	}

	public CSV ( List<List<String>> data, String comma ) {
		this( data, comma, "\\", "\"" );
	}

	public CSV ( List<List<String>> data, String comma, String escape, String quote ) {
		this.comma = comma;
		this.escape = escape;
		this.quote = quote;
		this.data = data;
		releaseWriteLock();
	}
	
	
	// Grid interface
	
	public String item ( int row, int col ) {
		return data.get(row).get(col);
	}
	
	public List<String> row ( int i ) {
		return data.get(i);
	}
	
	public List<String> col ( int i, boolean nulls ) {
		List<String> colList = new ArrayList<>();
		for (List<String> row : data) {
			if (row.size() > i) {
				colList.add( row.get( i ) );
			} else if (nulls) {
				colList.add( null );
			}
		}
		return colList;
	}
	
	public List<List<String>> data () {
		return data;
	}
	
	public int length () {
		data.size();
	}
	
	public int width () {
		return width;
	}
	
	public Grid append ( List<String> line ) {
		obtainWriteLock();
		appendInternal( line );
		releaseWriteLock();
		return this;
	}

	
	// Text interface
	
	public int lineCount () {
		return data.size();
	}
	
	public String line ( int lineNum ) {
		return line( row(lineNum) );
	}
	
	
	public List<String> lines () {
		List<String> lineList = new ArrayList<>();
		for (List<String> row : data) lineList.add( line(row) );
		return lineList;
	}
		
	public Text append ( String input ) {
		obtainWriteLock();
		appendInternal( input );
		releaseWriteLock();
		return this;
	}
	
	
	// Specialized appends
	
	public CSV append ( String[] line ) {
		append( Arrays.asList( line ) );
		return this;
	}
	
	public CSV append ( Set<String> line ) {
		append( new ArrayList<String>(line) );
		return this;
	}
	
	public CSV append ( Collection<String> line ) {
		append( new ArrayList<String>(line) );
		return this;
	}
	
	private CSV appendInternal ( List<String> line ) {
		rowUnderConstruction = line;
		addRow();
		return this;
	}
	
	
	
	// "friendly" methods used by appendInternal( String csv )
	
	public boolean comma (String c) {
		return c.equals(comma);
	}
	
	public String comma () {
		return comma;
	}
	
	public boolean escape (String c) {
		return c.equals(escape);
	}
	
	public String escape () {
		return escape;
	}
	
	public boolean quote (String c) {
		return c.equals(quote);
	}
	
	public String quote () {
		return quote;
	}
	
	public boolean newline (String c) {
		return c.equals("\n") || c.equals("\r");
	}
	
	public String newline () {
		return "\n";
	}
	
	List<String> rowUnderConstruction () {
		return rowUnderConstruction;
	}
	
	void newRow () {
		rowUnderConstruction = new ArrayList<String>();
	}
	
	void addRow () {
		data.add( rowUnderConstruction );
		if (rowUnderConstruction.size() > width) width = rowUnderConstruction.size();
	}
	
	void addChar (String c) {
		if (itemUnderConstruction==null) itemUnderConstruction = new StringBuilder();
		itemUnderConstruction.append( c );
	}
	
	void addItem () {
		rowUnderConstruction.add( itemUnderConstruction.toString() );
		itemUnderConstruction = null;
	}
	
	void addBlank () {
		itemUnderConstruction = new StringBuilder();
		addItem();
	}
	
	
	// convert CSV to data
	
	private CSV appendInternal ( String csv ) {
	
		for (int i=0; i<csv.length(); i++) {
		
			String thisChar = csv.substring(i, i+1);

			// transition and output logic
			if (state == LINE_START_STATE) {
				if (comma(thisChar)) {
					newRow();
					addBlank();
					state = COMMA_STATE;
				} else if (quote(thisChar)) {
					state = QUOTE_DATA_STATE;
				} else if (newline(thisChar)) {
					newRow();
					addRow();
					state = LINE_END_STATE;
				} else if (escape(thisChar)) {
					newRow();
					state = ESCAPE_STATE;
				} else {
					newRow();
					addChar( thisChar );
					state = DATA_STATE;
				}

			} else if (state == DATA_STATE) {
				if (comma(thisChar)) {
					addItem();
					state = COMMA_STATE;
				} else if (newline(thisChar)) {
					addItem();
					addRow();
					state = LINE_END_STATE;
				} else if (escape(thisChar)) {
					state = ESCAPE_STATE;
				} else {
					addChar( thisChar );
					state = DATA_STATE;
				}

			} else if (state == COMMA_STATE) {
				if (comma(thisChar)) {
					addBlank();
					state = COMMA_STATE;
				} else if (quote(thisChar)) {
					state = QUOTE_DATA_STATE;
				} else if (newline(thisChar)) {
					addBlank();
					addRow();
					state = LINE_END_STATE;
				} else if (escape(thisChar)) {
					// Output nothing and go to ESCAPE_STATE
					state = ESCAPE_STATE;
				} else {
					addChar( thisChar );
					state = DATA_STATE;
				}

			} else if (state == LINE_END_STATE) {
				if (comma(thisChar)) {
					newRow();
					addBlank();
					state = COMMA_STATE;
				} else if (quote(thisChar)) {
					newRow();
					state = QUOTE_DATA_STATE;
				} else if (newline(thisChar)) {
					// Output nothing and stay in this state
				} else if (escape(thisChar)) {
					// Output nothing and go to ESCAPE_STATE
					newRow();
					state = ESCAPE_STATE;
				} else {
					newRow();
					addChar( thisChar );
					state = DATA_STATE;
				}

			} else if (state == QUOTE_DATA_STATE) {
				if (quote(thisChar)) {
					state = QUOTE_END_STATE;
				} else if (escape(thisChar)) {
					// here we have just the traditional escape character that will cause us to ignore anything
					addChar( thisChar );
					state = QUOTE_ESCAPE_STATE;
				} else {
					addChar( thisChar );
					state = QUOTE_DATA_STATE;
				}

			} else if (state == QUOTE_END_STATE) {
				if (comma(thisChar)) {
					addItem();
					state = COMMA_STATE;
				} else if (newline(thisChar)) {
					addItem();
					addRow();
					state = LINE_END_STATE;
				} else if (quote(thisChar)) {
					// here we find out it's not the end of the quoted, just an escaped quote (by using two quotes) inside the quoted
					addChar( quote() );
					addChar( thisChar );
					state = QUOTE_DATA_STATE;
				} else {
					// this is the edge case where you have raw data trailing the quoted data
					addChar( thisChar );
					state = DATA_STATE;
				}

			} else if (state == ESCAPE_STATE) {
				addChar( thisChar );
				state = DATA_STATE;

			} else if (state == QUOTE_ESCAPE_STATE) {
				addChar( thisChar );
				state = QUOTE_DATA_STATE;

			}
			
			//System.out.println( "thisChar: '"+thisChar+"', state: "+reverse_state[state] );
		}
		return this;
	
	}
	
	// convert line of data to CSV
	
	public String line ( List<String> list ) {
		StringBuilder csv = new StringBuilder();
		for (int i=0; i<list.size(); i++) {
			if (i>0) csv.append(comma());
			String item = list.get(i);
			if (item.indexOf(quote()) > -1) {
				csv
					.append(quote())
					.append( item.replaceAll(quote(), quote()+quote()) )
					.append(quote());
			}
			else if (item.indexOf(comma()) > -1) {
				csv
					.append(quote())
					.append(item)
					.append(quote());
			} else {
				csv
					.append(item);
			}
		}
		return csv.append(newline()).toString();
	}
	
	// convert data to HTML
	
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
	

	public String toString () {
		StringBuilder csvText = new StringBuilder();
		for (List<String> row : data) csvText.append( line(row) );
		return csvText.toString();
	}
	
	// testing

	public static void main ( String[] args ) {
		String csv =
			"1,22,333,4444,55555\n"+
			"a, b, c ,d   ,\"e\"     ,f\"    \r\n"+
			"\"A,B,C\",hello!\n"+
			"\n"+
			"1,\",2,\",3,\\,4\\,,5\n"+
			",,Lone Item,,\n"+
			",,\"quoted item,-,,,very good!\",,\n"+
			",,\"quoted item,-\\\"very good!\\\"\",,\n"+
			",\r\n";
		System.out.println( "\ncsv:\n"+csv );
			
		CSV csvObj = new CSV( csv );
		csvObj
			.append( "a,b,c" )
			.append( ",d,e,f" )
			.append( ",g,h,i" )
			.append( "\n" )
		;
		
		System.out.println( "\ndata:\n"+csvObj.data() );
		System.out.println( "\ncsvObj:\n"+csvObj );
			
	}

}
