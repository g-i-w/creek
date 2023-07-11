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
	
	public void data ( List<List<String>> data ) {
		obtainWriteLock();
		this.data = data;
		releaseWriteLock();
	}
	
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
	
	public Table alias ( Table table ) {
		if (table == null) return this;
		obtainWriteLock();
		data = table.data();
		releaseWriteLock();
		return this;
	}
	
	public Table append ( Table table ) {
		if (table == null) return this;
		int rowsSafe = table.data().size();
		// guards against a java.util.ConcurrentModificationException if a table appends itself
		for (int row=0; row<rowsSafe; row++) append( table.data().get( row ) );
		return this;
	}
	
	public Table append ( List<String> row ) {
		if (row == null) return this;
		obtainWriteLock();
		data.add( row );
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
	
	// "friendly" tools for child classes
	
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
