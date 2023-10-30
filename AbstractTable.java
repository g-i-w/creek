package creek;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractTable implements Table {

	// data
	private List<List<String>> data;
	
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
		
	// Table interface
	
	public List<List<String>> data () {
		return data;
	}
	
	public Table data ( List<List<String>> data ) {
		obtainWriteLock();
		this.data = data;
		releaseWriteLock();
		return this;
	}
	
	public String item ( int row, int col ) {
		if (row > -1 && data != null && row < data.size() && col > -1 && col < data.get(row).size()) {
			return data.get(row).get(col);
		} else {
			return null;
		}
	}
	
	public String[] row ( int row ) {
		if (row > -1 && data != null && row < data.size()) {
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
		if (data == null) return new String[]{};
		int colLength = data.size();
		String[] colCopy = new String[colLength];
		for (int i=0; i<colLength; i++) {
			if (col > -1 && data.get(i) != null && col < data.get(i).size()) {
				colCopy[i] = data.get(i).get(col);
			} else {
				colCopy[i] = null;
			}
		}
		return colCopy;
	}
	
	public Table alias ( Table table ) {
		if (table == null) return this;
		obtainWriteLock();
		data = table.data();
		releaseWriteLock();
		return this;
	}
	
	public Table copy ( Table table ) {
		if (table == null) return this;
		obtainWriteLock();
		data = new ArrayList<List<String>>();
		for (int row=0; row<table.rowCount(); row++) {
			List<String> newRow = new ArrayList<String>();
			for (int col=0; col<table.colCount(row); col++) {
				newRow.add( table.item(row,col) );
			}
			data.add( newRow );
		}
		releaseWriteLock();
		return this;
	}
	
	public Table append ( Table table ) {
		if (table == null || table.data() == null) return this;
		int rowsSafe = table.rowCount();
		// guards against a java.util.ConcurrentModificationException if a table appends itself
		for (int row=0; row<rowsSafe; row++) append( table.row( row ) );
		return this;
	}
	
	public Table append ( List<String> row ) {
		if (row == null) return this;
		obtainWriteLock();
		if (data == null) data = new ArrayList<List<String>>();
		data.add( row );
		releaseWriteLock();
		return this;
	}
	
	public Table append ( String[] row ) {
		if (row == null) return this;
		append( Arrays.asList( row ) );
		return this;
	}
	
	public List<List<String>> last ( int lastRows ) {
		int rowCount = rowCount();
		return slice( rowCount-lastRows, rowCount );
	}
	
	public List<List<String>> slice ( int startRowInclusive, int endRowExclusive ) {
		int rowCount = rowCount();
		if (startRowInclusive < 0) startRowInclusive = 0;
		if (endRowExclusive > rowCount) endRowExclusive = rowCount;
		List<List<String>> aSlice = new ArrayList<>();
		for (int i=startRowInclusive; i<endRowExclusive; i++) aSlice.add( data.get(i) );
		return aSlice;
	}
	
	public List<List<String>> slice ( int startRowInclusive, int endRowExclusive, int startColInclusive, int endColExclusive ) {
		List<List<String>> aSlice = new ArrayList<>();
		for (int a=startRowInclusive; a<endRowExclusive; a++) {
			List<String> row = new ArrayList<>();
			for (int b=startColInclusive; b<endColExclusive; b++) {
				row.add( item(a,b) );
			}
			aSlice.add( row );
		}
		return aSlice;
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
	
	// "friendly" tools for child classes
	
	List<String> rowUnderConstruction () {
		if (rowUnderConstruction==null) newRow();
		return rowUnderConstruction;
	}
	
	void newRow () {
		rowUnderConstruction = new ArrayList<String>();
	}
	
	void addRow () {
		data.add( rowUnderConstruction() );
		rowUnderConstruction = null;
	}
	
	StringBuilder itemUnderConstruction () {
		if (itemUnderConstruction==null) newItem();
		return itemUnderConstruction;
	}
	
	void newItem () {
		itemUnderConstruction = new StringBuilder();
	}
	
	void buildItem (Character c) {
		itemUnderConstruction().append( c );
	}
	
	void buildItem (String s) {
		itemUnderConstruction().append( s );
	}
	
	void addItem () {
		addItem( itemUnderConstruction().toString() );
	}
	
	void addItem ( Character c ) {
		addItem( String.valueOf( c ) );
	}
	
	void addItem ( String item ) {
		rowUnderConstruction().add( item );
		itemUnderConstruction = null;
	}
	
	void addBlank () {
		addItem( "" );
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
