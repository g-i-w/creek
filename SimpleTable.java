package creek;

import java.util.*;

public class SimpleTable implements Table {

	// data
	private List<List<String>> data;
	
	// stats
	private int maxItemLength = 0;

	// conversion process tools
	private List<String> rowUnderConstruction;
	private StringBuilder itemUnderConstruction;	

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

	
	private void checkItemLength ( String item ) {
		if (item.length() > maxItemLength) maxItemLength = item.length();
	}
	
	
	// Constructors
	
	public SimpleTable () {
		data = new ArrayList<List<String>>();
	}
	
	public SimpleTable ( Table table ) {
		this();
		append( table );
	}
	
	public SimpleTable ( String serial ) {
		this();
		data( serial );
	}
	
	
	
	// Table interface
	
	public String item ( int row, int col ) {
		if (row > -1 && row < data.size() && col > -1 && col < data.get(row).size()) {
			return data.get(row).get(col);
		} else {
			return null;
		}
	}
	
	public String[] row ( int row ) {
		if (row > -1 && row < data.size()) {
			List<String> rowList = data.get(row);
			int rowLength = rowList.size();
			String[] rowCopy = new String[rowLength];
			for (int i=0; i<rowLength; i++) rowCopy[i] = rowList.get(i);
			return rowCopy;
		} else {
			return new String[]{};
		}
	}
	
	public String[] col ( int col ) {
		int colLength = data.size();
		String[] colCopy = new String[colLength];
		for (int i=0; i<colLength; i++) {
			if (col > -1 && col < data.get(i).size()) {
				colCopy[i] = data.get(i).get(col);
			} else {
				colCopy[i] = null;
			}
		}
		return colCopy;
	}
	
	public Table append ( Table table ) {
		for (int row=0; row<table.rowCount(); row++) {
			List<String> newRow = new ArrayList<>();
			for (int col=0; col<table.colCount( row ); col++) {
				String item = table.item( row, col );
				newRow.add( item );
				checkItemLength( item );
			}
			data.add( newRow );
		}
		return this;
	}
	
	public Table append ( String[] row ) {
		data.add( Arrays.asList( row ) );
		return this;
	}
	
	public Table append ( List<String> row ) {
		append( row.toArray( new String[0] ) );
		return this;
	}
	
	public String serial () {
		StringBuilder serial = new StringBuilder();
		for (List<String> row : data) {
			int itemCount = row.size();
			for (int i=0; i<itemCount; i++) {
				String item = row.get(i);
				serial.append( item );
				if (i<itemCount-1) {
					for (int spaces=item.length(); spaces<maxItemLength+1; spaces++) serial.append( " " );
				}
			}
			serial.append( "\n" );
		}
		return serial.toString();
	}
	
	public int rowCount () {
		return data.size();
	}
	
	public int colCount ( int row ) {
		if (row > -1 && row < data.size()) {
			return data.get(row).size();
		} else {
			return -1;
		}
	}
	
	public int maxItemLength () {
		return maxItemLength;
	}
	
	
	
	// "friendly" tools for child classes
	
	List<List<String>> data () {
		return data;
	}
	
	void newRow () {
		rowUnderConstruction = new ArrayList<String>();
	}
	
	void addRow () {
		data.add( rowUnderConstruction );
		rowUnderConstruction = null;
	}
	
	void addChar (Character c) {
		if (itemUnderConstruction==null) itemUnderConstruction = new StringBuilder();
		itemUnderConstruction.append( c );
	}
	
	void addString (String s) {
		if (itemUnderConstruction==null) itemUnderConstruction = new StringBuilder();
		itemUnderConstruction.append( s );
	}
	
	void addItem () {
		rowUnderConstruction.add( itemUnderConstruction.toString() );
		checkItemLength( itemUnderConstruction.toString() );
		itemUnderConstruction = null;
	}
	
	void addBlank () {
		itemUnderConstruction = new StringBuilder();
		addItem();
	}
	
	void finalRow () {
		if (itemUnderConstruction != null) addItem();
		if (rowUnderConstruction != null) addRow();
	}
	
	
	// convert serial to data
	private void data ( String serial ) {
	
		for (Character c : serial.toCharArray()) {

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
					addChar( c );
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
					addChar( c );
					state = DATA_STATE;
				}

			} else if (state == SPACE_STATE) {
				if (c == '\n') {
					addRow();
					state = LINE_END_STATE;
				} else if (! Character.isWhitespace(c)) {
					addChar( c );
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
					addChar( c );
					state = DATA_STATE;
				}

			} else if (state == QUOTE_DATA_STATE) {
				if (c == '"') {
					state = QUOTE_END_STATE;
				} else if (c == '\\') {
					addChar( c );
					state = ESCAPE_STATE;
				} else {
					addChar( c );
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
					addChar( c );
					state = DATA_STATE;
				}

			} else if (state == ESCAPE_STATE) {
				addChar( c );
				state = DATA_STATE;
			}
			
			//System.out.println( "thisChar: '"+c+"', state: "+reverse_state[state] );
		}
		finalRow();
	}
	
	

	public String toString () {
		return serial();
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
		System.out.println( table.data() );
		System.out.println( table );
		
		SimpleTable table2 = new SimpleTable( table );
		List<String> newRow = new ArrayList<>();
		newRow.add( "A" );
		newRow.add( "B" );
		newRow.add( "C" );
		table2.append( newRow );
		
		System.out.println( table2.data() );
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
