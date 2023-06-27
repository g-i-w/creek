package creek;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractTable implements Table {

	// data
	private List<List<String>> data;
	
	// stats
	private Map<Integer,Integer> maxItemLength = new HashMap<>();

	// conversion process tools
	private List<String> rowUnderConstruction;
	private StringBuilder itemUnderConstruction;
	
	// concurrency
	private AtomicBoolean writeLock = new AtomicBoolean(false);
		
	void obtainWriteLock () {
		while (writeLock == null || !writeLock.compareAndSet( false, true )) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void releaseWriteLock () {
		writeLock.set( false );
	}
	
	private void checkItemLength ( String item, int col ) {
		if (maxItemLength.get(col) == null || item.length() > maxItemLength.get(col).intValue())
			maxItemLength.put( col, item.length() );
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
		if (table == null) return this;
		obtainWriteLock();
		for (int row=0; row<table.rowCount(); row++) {
			List<String> newRow = new ArrayList<>();
			for (int col=0; col<table.colCount( row ); col++) {
				String item = table.item( row, col );
				newRow.add( item );
				checkItemLength( item, col );
			}
			data.add( newRow );
		}
		releaseWriteLock();
		return this;
	}
	
	public Table append ( List<String> row ) {
		if (row == null) return this;
		obtainWriteLock();
		List<String> newRow = new ArrayList<>();
		for (int i=0; i<row.size(); i++) {
			String item = row.get(i);
			newRow.add( item );
			checkItemLength( item, i );
		}
		data.add( newRow );
		releaseWriteLock();
		return this;
	}
	
	public Table append ( String[] row ) {
		if (row == null) return this;
		append( Arrays.asList( row ) );
		return this;
	}
	
	////////////////// Abstract method //////////////////
	public abstract Table append ( String raw );
	
	////////////////// Abstract method //////////////////
	public abstract String serial ();
	
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
	
	public int maxItemLength ( int col ) {
		return maxItemLength.get(col);
	}
	
	
	
	// "friendly" tools for child classes
	
	List<List<String>> data () {
		return data;
	}
	
	void data ( List<List<String>> data ) {
		this.data = data;
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
		String item = itemUnderConstruction.toString();
		if (rowUnderConstruction == null) newRow();
		checkItemLength( item, rowUnderConstruction.size() );
		rowUnderConstruction.add( item );
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
	
	
	// default toString
	
	public String toString () {
		return serial();
	}

}
