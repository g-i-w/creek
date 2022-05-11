package creek;

import java.util.*;

public class CSV {


	private List<List<String>> data;
	private String comma;
	private String escape;
	private String quote;
	
	private List<String> lastRow;
	private String lastItem;
	
	private Map<Integer,Map<String,Integer>> autoIndex;
	
	// states
	private static final int LINE_START_STATE = 0;
	private static final int DATA_STATE = 1;
	private static final int COMMA_STATE = 2;
	private static final int ESCAPE_STATE = 3;
	private static final int LINE_END_STATE = 4;
	private static final int QUOTE_DATA_STATE = 5;
	private static final int QUOTE_END_STATE = 6;
	private static final int QUOTE_ESCAPE_STATE = 7;

	// state variable
	private int state = LINE_START_STATE;
	
	public int size () {
		return data.size();
	}
	
	public boolean comma (String c) {
		return c.equals(comma);
	}
	
	public boolean escape (String c) {
		return c.equals(escape);
	}
	
	public boolean quote (String c) {
		return c.equals(quote);
	}
	
	public boolean newline (String c) {
		return c.equals("\n") || c.equals("\r");
	}
	
	public void newRow () {
		lastRow = new ArrayList<String>();
	}
	
	public void addRow () {
		data.add( lastRow );
	}
	
	public void addChar (String c) {
		if (lastItem==null) lastItem = new String();
		lastItem += c;
	}
	
	public void addItem () {
		lastRow.add( lastItem );
		lastItem = null;
	}
	
	public void addBlank () {
		lastItem = "";
		addItem();
	}
	
	public CSV append ( String[] line ) {
		append( Arrays.asList( line ) );
		return this;
	}
	
	public CSV append ( List<String> line ) {
		lastRow = line;
		addRow();
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
					addItem();
				} else if (escape(thisChar)) {
					addChar( thisChar );
					state = QUOTE_ESCAPE_STATE;
				} else {
					addChar( thisChar );
					state = QUOTE_DATA_STATE;
				}

			} else if (state == QUOTE_END_STATE) {
				if (comma(thisChar)) {
					state = COMMA_STATE;
				} else {
					// Output nothing and continue in the QUOTE_END_STATE
					state = QUOTE_END_STATE;
				}

			} else if (state == ESCAPE_STATE) {
				addChar( thisChar );
				state = DATA_STATE;

			} else if (state == QUOTE_ESCAPE_STATE) {
				addChar( thisChar );
				state = QUOTE_DATA_STATE;

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
		return lastRow;
	}
	
	public String line ( int r ) {
		String csv = "";
		for (int i=0; i<row(r).size(); i++) {
			if (i>0) csv += comma;
			String item = row(r).get(i);
			if (item.indexOf(comma) < 0) {
				csv += item;
			} else {
				csv += quote + item + quote;
			}
		}
		return csv + "\n";
	}
	
	public String lines () {
		String csvText = "";
		for (int i=0; i<data.size(); i++) {
			csvText += line(i);
		}
		return csvText;
	}
	
	public String lastLine () {
		return line( data.size()-1 );
	}
	
	public String toString () {
		return lines();
	}
	
	public Map<String,Integer> index ( int indexRow ) {
		if (indexRow >= data.size()) return null;
		if (! autoIndex.containsKey(indexRow)) {
			Map<String,Integer> map = new LinkedHashMap<>();
			int i=0;
			for (String item : data.get(indexRow)) {
				map.put( item, i++ );
			}
			autoIndex.put( indexRow, map );
			return map;
		} else {
			return autoIndex.get(indexRow);
		}
	}
	
	public String index ( int indexRow, String key, int targetRow ) {
		if (indexRow >= data.size() || targetRow >= data.size()) return "";
		Integer col = index( indexRow ).get( key );
		if (col==null) return "";
		return data.get(targetRow).get(col);
	}


	
	public CSV () {
		this( "", ",", "\\", "\"" );
	}
	
	public CSV ( String csv ) {
		this( csv, ",", "\\", "\"" );
	}

	public CSV ( String csv, String comma, String escape, String quote ) {
		this.comma = comma;
		this.escape = escape;
		this.quote = quote;
		data = new ArrayList<List<String>>();
		append( csv );
		autoIndex = new HashMap<>();
	}
	
	
	public static void main ( String[] args ) {
		String csv =
			"1,22,333,4444,55555\n"+
			"a, b, c ,d   ,e    \r\n"+
			"A,B,C,found it!\n"+
			"\n"+
			"1,\",2,\",3,\\,4\\,,5\n"+
			",,Lone Item,,\n"+
			",,\"quoted item, ,,,very good!\",,\n"+
			",,\"quoted item, \\\"very good!\\\"\",,\n"+
			",\r\n";
		System.out.println( "\ncsv:\n"+csv );
			
		CSV csvObj = new CSV( csv );
		csvObj
			.append( "a,b,c" )
			.append( ",d,e,f" )
			.append( ",g,h,i" )
			.append( "\n" )
		;
		
		System.out.println( "\ndata:\n"+csvObj.rows() );
		System.out.println( "\ncsvObj:\n"+csvObj );
		System.out.println( "index output: '"+csvObj.index( 0, "4444", 2 )+"'" );
		System.out.println( "index output: '"+csvObj.index( 0, "333", 6 )+"'" );
			
	}

}
