package creek;

import java.util.*;

public class CSV extends AbstractTable {

	// settings
	private String comma;
	private String escape;
	private String quote;
	
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

	/*private static final String[] reverse_state = {
		"LINE_START_STATE","DATA_STATE","COMMA_STATE","ESCAPE_STATE","LINE_END_STATE","QUOTE_DATA_STATE","QUOTE_END_STATE","QUOTE_ESCAPE_STATE"
	};*/


	private void init ( String comma, String escape, String quote ) {
		this.comma = comma;
		this.escape = escape;
		this.quote = quote;
		data( new ArrayList<List<String>>() );
	}

	
	// constructors

	public CSV () {
		this( null, ",", "\\", "\"" );
	}

	public CSV ( String raw ) {
		this( raw, ",", "\\", "\"" );
	}

	public CSV ( String comma, String escape, String quote ) {
		this( null, comma, escape, quote );
	}

	public CSV ( String raw, String comma, String escape, String quote ) {
		init( comma, escape, quote );
		if (raw != null) append( raw );
	}
	
	public CSV ( Table table ) {
		init( ",", "\\", "\"" );
		alias( table );
	}
	
	
	
	public String serial () {
		StringBuilder csv = new StringBuilder();
		for (List<String> row : data()) {
			for (int i=0; i<row.size(); i++) {
				if (i>0) csv.append(comma());
				String item = row.get(i);
				if (item == null) continue;
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
			csv.append(newline());
		}
		return csv.toString();
	}
	
		
	// internal methods
	
	private boolean comma (String c) {
		return c.equals(comma);
	}
	
	public String comma () {
		return comma;
	}
	
	private boolean escape (String c) {
		return c.equals(escape);
	}
	
	public String escape () {
		return escape;
	}
	
	private boolean quote (String c) {
		return c.equals(quote);
	}
	
	public String quote () {
		return quote;
	}
	
	private boolean newline (String c) {
		return c.equals("\n") || c.equals("\r");
	}
	
	public String newline () {
		return "\n";
	}
	
	
	// convert CSV to data
	public Table append ( String csv ) {
		obtainWriteLock();
		
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
					addString( thisChar );
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
					addString( thisChar );
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
					addString( thisChar );
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
					addString( thisChar );
					state = DATA_STATE;
				}

			} else if (state == QUOTE_DATA_STATE) {
				if (quote(thisChar)) {
					state = QUOTE_END_STATE;
				} else if (escape(thisChar)) {
					// here we have just the traditional escape character that will cause us to ignore anything
					addString( thisChar );
					state = QUOTE_ESCAPE_STATE;
				} else {
					addString( thisChar );
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
					addString( quote() );
					addString( thisChar );
					state = QUOTE_DATA_STATE;
				} else {
					// this is the edge case where you have raw data trailing the quoted data
					addString( thisChar );
					state = DATA_STATE;
				}

			} else if (state == ESCAPE_STATE) {
				addString( thisChar );
				state = DATA_STATE;

			} else if (state == QUOTE_ESCAPE_STATE) {
				addString( thisChar );
				state = QUOTE_DATA_STATE;

			}
			
			//System.out.println( "thisChar: '"+thisChar+"', state: "+reverse_state[state] );
		}
		finalRow();
		
		releaseWriteLock();
		return this;
	}


	// testing

	public static CSV test() {
		return new CSV(
			"1,22,333,4444,55555\n"+
			"a, b, c ,d   ,\"e\"     ,f\"    \r\n"+
			"\"A,B,C\",hello!\n"+
			"\n"+
			"1,\",2,\",3,\\,4\\,,5\n"+
			",,Lone Item,,\n"+
			",,\"quoted item,-,,,very good!\",,\n"+
			",,\"quoted item,-\\\"very good!\\\"\",,\n"+
			",\r\nfinal line..."
		);
	}

	public static void main ( String[] args ) {
	
		CSV csv0 = CSV.test();
		CSV csv1 = new CSV( csv0 );
		
		csv1.append(csv0);
		
		System.out.println( "\ndata:\n"+csv0.data() );
		System.out.println( "\ncsv0:\n"+csv0 );
		System.out.println( "\ncsv1:\n"+csv1 );
			
	}

}
