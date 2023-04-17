package creek;

import java.util.*;

public class SimpleGrid implements Grid {

	// data
	private List<List<String>> data;
	
	// stats
	private int colCount = 0;
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

	
	private void checkColCount ( List<String> row ) {
		if (row.size() > colCount) colCount = row.size();
	}
	
	private void checkItemLength ( String item ) {
		if (item.length() > maxItemLength) maxItemLength = item.length();
	}
	
	
	// Constructors
	
	public SimpleGrid () {
		data = new ArrayList<List<String>>();
	}
	
	public SimpleGrid ( Grid grid ) {
		data = grid.data();
		for (List<String> row : data) {
			checkColCount( row );
			for (String item : row) checkItemLength( item );
		}
	}
	
	public SimpleGrid ( String serial ) {
		this();
		data( serial );
	}
	
	
	
	// Grid interface
	
	public String item ( int row, int col ) {
		if (data.size() < row && col < colCount) {
			return data.get(row).get(col);
		} else {
			return "";
		}
	}
	
	public List<List<String>> data () {
		return data;
	}
	
	public Grid append ( Grid grid ) {
		int size = data.size(); // required in case we have a circular reference between Grid objects
		for (int i=0; i<size; i++) {
			List<String> row = grid.data().get(i);
			data.add( row );
			checkColCount( row );
		}
		return this;
	}
	
	public Grid append ( List<String> row ) {
		data.add( row );
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
	
	public int colCount () {
		return colCount;
	}
	
	public int maxItemLength () {
		return maxItemLength;
	}
	
	
	
	// "friendly" tools for child classes
	
	void newRow () {
		rowUnderConstruction = new ArrayList<String>();
	}
	
	void addRow () {
		data.add( rowUnderConstruction );
		checkColCount( rowUnderConstruction );
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
	
	public static void main ( String[] args ) {
		String serial =
		"abc  def  ghi\n"+
		"jklm nopq \n"+
		"\"r s t u v\"    w    x  \n"+
		"y  z"
		;
		
		Grid grid = new SimpleGrid( serial );
		System.out.println( grid.data() );
		System.out.println( grid );
		System.out.println( new SimpleGrid( grid ) );
	}

}
