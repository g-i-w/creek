package creek;

import java.util.*;

public class SimpleTable extends AbstractTable {

	// states
	private static final int LINE_START_STATE = 0;
	private static final int DATA_STATE = 1;
	private static final int SPACE_STATE = 2;
	private static final int ESCAPE_STATE = 3;
	private static final int LINE_END_STATE = 4;
	private static final int QUOTE_DATA_STATE = 5;
	private static final int QUOTE_END_STATE = 6;
	
	private int state = LINE_START_STATE;

	/*private static final String[] reverse_state = {
		"LINE_START_STATE","DATA_STATE","SPACE_STATE","ESCAPE_STATE","LINE_END_STATE","QUOTE_DATA_STATE","QUOTE_END_STATE"
	};*/

		
	// Constructors
	
	public SimpleTable () {
		data( new ArrayList<List<String>>() );
	}
	
	public SimpleTable ( Table table ) {
		alias( table );
	}
	
	public SimpleTable ( String serial ) {
		this();
		append( serial );
	}
	
	public Table create () {
		return new SimpleTable();
	}
	
	public String serial () {
		obtainWriteLock();
		// find column widths
		Map<Integer,Integer> colWidth = new HashMap<>();
		for (List<String> row : data()) {
			int itemCount = row.size();
			for (int col=0; col<itemCount; col++) {
				String item = row.get(col);
				if (item == null) continue;
				if (colWidth.get(col) == null || item.length() > colWidth.get(col).intValue()) colWidth.put( col, item.length() );
			}
		}
		// build output string
		StringBuilder serial = new StringBuilder();
		for (List<String> row : data()) {
			int itemCount = row.size();
			for (int col=0; col<itemCount; col++) {
				String item = row.get(col);
				if (item == null) item = "";
				serial.append( item );
				if (col<itemCount-1) {
					int width = ( colWidth.get(col)!=null ? colWidth.get(col) : 0 );
					for (int spaces=item.length(); spaces<width+4; spaces++) serial.append( " " );
				}
			}
			serial.append( "\n" );
		}
		releaseWriteLock();
		return serial.toString();
	}
	
	
	// convert serial to data
	public Table append ( String serial ) {
		obtainWriteLock();
		for (char c : serial.toCharArray()) {

			// transition and output logic
			if (state == LINE_START_STATE) {
				if (c == '\n') {
					newRow();
					addRow();
					state = LINE_END_STATE;
				} else if (Character.isWhitespace(c)) {
					newRow();
					state = SPACE_STATE;
				} else if (c == '"') {
					state = QUOTE_DATA_STATE;
				} else if (c == '\\') {
					newRow();
					state = ESCAPE_STATE;
				} else {
					newRow();
					buildItem( c );
					state = DATA_STATE;
				}

			} else if (state == DATA_STATE) {
				if (c == '\n') {
					addItem();
					addRow();
					state = LINE_END_STATE;
				} else if (Character.isWhitespace(c)) {
					addItem();
					state = SPACE_STATE;
				} else if (c == '\\') {
					state = ESCAPE_STATE;
				} else {
					buildItem( c );
					state = DATA_STATE;
				}

			} else if (state == SPACE_STATE) {
				if (c == '\n') {
					addRow();
					state = LINE_END_STATE;
				} else if (! Character.isWhitespace(c)) {
					buildItem( c );
					state = DATA_STATE;
				} else if (c == '"') {
					state = QUOTE_DATA_STATE;
				} else if (c == '\\') {
					// Output nothing and go to ESCAPE_STATE
					state = ESCAPE_STATE;
				} else {
					// Output nothing and stay in this state
				}

			} else if (state == LINE_END_STATE) {
				if (c == '"') {
					newRow();
					state = QUOTE_DATA_STATE;
				} else if (c == '\n') {
					// Output nothing and stay in this state
				} else if (c == '\\') {
					// Output nothing and go to ESCAPE_STATE
					newRow();
					state = ESCAPE_STATE;
				} else {
					newRow();
					buildItem( c );
					state = DATA_STATE;
				}

			} else if (state == QUOTE_DATA_STATE) {
				if (c == '"') {
					state = QUOTE_END_STATE;
				} else if (c == '\\') {
					buildItem( c );
					state = ESCAPE_STATE;
				} else {
					buildItem( c );
					state = QUOTE_DATA_STATE;
				}

			} else if (state == QUOTE_END_STATE) {
				if (c == '\n') {
					addItem();
					addRow();
					state = LINE_END_STATE;
				} else if (Character.isWhitespace(c)) {
					addItem();
					state = SPACE_STATE;
				} else {
					addItem();
					buildItem( c );
					state = DATA_STATE;
				}

			} else if (state == ESCAPE_STATE) {
				buildItem( c );
				state = DATA_STATE;
			}
			
			//System.out.println( "thisChar: '"+c+"', state: "+reverse_state[state] );
		}
		finalRow();
		releaseWriteLock();
		return this;
	}
	
	

	// testing
	
	public static SimpleTable test () {
		return new SimpleTable(
			"abc  def  ghi\n"+
			"jklm nopq \n"+
			"\"r s t u v\"    w    x  \n"+
			"y  z"
		);
	}
	
	public static void main ( String[] args ) {
		SimpleTable table = SimpleTable.test();
		System.out.println( "table:\n"+table.data() );
		System.out.println( table );
		
		SimpleTable table2 = new SimpleTable( table );
		String[] newRow = new String[]{ "A", "B", "C" };
		table.append( newRow );
		table2.append( newRow );
		
		System.out.println( "table:\n"+table.data() );
		System.out.println( table );
		System.out.println( "table2:\n"+table2.data() );
		System.out.println( table2 );
		
		System.out.println( "row testing" );
		System.out.println( Arrays.asList(table2.row( -1 )) );
		System.out.println( Arrays.asList(table2.row( 1 )) );
		System.out.println( Arrays.asList(table2.row( 4 )) );
		
		System.out.println( "col testing" );
		System.out.println( Arrays.asList(table2.col( -1 )) );
		System.out.println( Arrays.asList(table2.col( 1 )) );
		System.out.println( Arrays.asList(table2.col( 2 )) );
		System.out.println( Arrays.asList(table2.col( 4 )) );
	}

}
