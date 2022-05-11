package creek;

import java.util.*;

public class CSV {


	private List<List<String>> data;
	private String comma;
	private String escape;
	
	private List<String> lastLine;
	private String lastItem;
	
	// states
	private static final int LINE_START_STATE = 0;
	private static final int DATA_STATE = 1;
	private static final int COMMA_STATE = 2;
	private static final int ESCAPE_STATE = 3;
	private static final int LINE_END_STATE = 4;

	// state variable
	private int state = LINE_START_STATE;
	
	
	private boolean comma (String c) {
		return c.equals(comma);
	}
	
	private boolean escape (String c) {
		return c.equals(escape);
	}
	
	private boolean newline (String c) {
		return c.equals("\n") || c.equals("\r");
	}
	
	private void newRow () {
		lastLine = new ArrayList<String>();
	}
	
	private void addRow () {
		data.add( lastLine );
	}
	
	private void addChar (String c) {
		if (lastItem==null) lastItem = new String();
		lastItem += c;
	}
	
	private void addItem () {
		lastLine.add( lastItem );
		lastItem = null;
	}
	
	private void addBlank () {
		lastItem = "";
		addItem();
	}
	
	public CSV append ( String[] line ) {
		append( Arrays.asList( line ) );
		return this;
	}
	
	public CSV append ( List<String> line ) {
		data.add( line );
		lastLine = line;
		return this;
	}
	
	public CSV append ( String csv ) {
	
		for (int i=0; i<csv.length(); i++) {
		
			String thisChar = csv.substring(i, i+1);

			// transition and output logic
			if (state == LINE_START_STATE) {
				if (comma(thisChar)) {
					newRow();
					addBlank();
					state = COMMA_STATE;
				} else if (newline(thisChar)) {
					newRow();
					addRow();
					state = LINE_END_STATE;
				} else if (escape(thisChar)) {
					// Only output the row and data start tags
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
					// Output nothing and go to the ESCAPE_STATE
					state = ESCAPE_STATE;
				} else {
					addChar( thisChar );
					state = DATA_STATE;
				}

			} else if (state == COMMA_STATE) {
				if (comma(thisChar)) {
					addBlank();
					state = COMMA_STATE;
				} else if (newline(thisChar)) {
					addBlank();
					addRow();
					state = LINE_END_STATE;
				} else if (escape(thisChar)) {
					// Only output the data start tag
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
				} else if (newline(thisChar)) {
					// Output nothing and stay in this state
				} else if (escape(thisChar)) {
					// Only output the row and data start tags
					newRow();
					state = ESCAPE_STATE;
				} else {
					newRow();
					addChar( thisChar );
					state = DATA_STATE;
				}

			} else if (state == ESCAPE_STATE) {
				addChar( thisChar );
				state = DATA_STATE;

			}
			
		}
		return this;
	
	}
	
	public List<List<String>> rows () {
		return data;
	}
	
	public List<String> row ( int i ) {
		return data.get(i);
	}
	
	public List<String> lastRow () {
		return lastLine;
	}
	
	public String csv ( int i ) {
		return String.join( comma, row(i) ) + "\n";
	}
	
	public String csv () {
		String csvText = "";
		for (int i=0; i<data.size(); i++) {
			csvText += csv(i);
		}
		return csvText;
	}
	
	public String toString () {
		return csv();
	}


	
	public CSV () {
		this( "", ",", "\\" );
	}
	
	public CSV ( String csv ) {
		this( csv, ",", "\\" );
	}

	public CSV ( String csv, String comma, String escape ) {
		this.comma = comma;
		this.escape = escape;
		data = new ArrayList<List<String>>();
		append( csv );
	}
	
	
	public static void main ( String[] args ) {
		String csv =
			"1,22,333,4444,55555\n"+
			"a, b, c ,d   ,e    \r\n"+
			"A,B,C\n"+
			"\n"+
			"1,2,3,\\,4\\,,5\n"+
			",,A,,\n"+
			",\r\n";
		System.out.println( "\ncsv:\n"+csv );
			
		CSV csvObj = new CSV( csv );
		csvObj
			.append( "a,b" )
			.append( ",c,d" )
			.append( "\n" )
		;
		
		System.out.println( "\ndata:\n"+csvObj.rows() );
		System.out.println( "\ncsvObj:\n"+csvObj );
			
	}

}
