package creek;

public class CSVSet extends CSVLog {

	private String numberFormat;

	public CSVSet ( File file ) {
		this( file, "%05d" );
	}

	public CSVSet ( File file, String numberFormat ) {
		super( file );
		this.numberFormat = numberFormat;
	}

	public SetTable set () {
		SetTable slice =
			table()
			.set(1) // order column
			.slice( 0, table().rowCount(), 3, table().colCount() ); // remove first 3 columns
		SetTable enabled = new IndexedTable();
		for (int i=0; i<slice.rowCount(); i++) {
			if (slice.item(i,2).equals("1")) enabled.append( slice.row(i) );
		}
		return enabled;
	}
	
	public CSVSet append ( List<String> row, int order, boolean enable ) throws Exception {
		LinkedList<String> setData = new LinkedList<>( row );
		setData.addFirst( enable ? "1" : "0" );
		try {
			setData.addFirst( String.format(numberFormat, order) );
		} catch ( Exception e ) {
			e.printStackTrace();
			return this;
		}
		append( setData );
		return this;
	}
	
}
