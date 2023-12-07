package creek;

import java.util.*;

public class FilterTable extends LookupTable implements Filter {


	// < columns < items , Table >>
	private Map<Integer,Map<String,Table>> filtered; // whole words in a column
	
	// < columns < fragments < subset_of_items , Table >>>
	private Map<Integer,Map<String,Map<String,Table>>> searchable; // partial words in a column
	

	public FilterTable ( Table table ) {
		super( table );
		filtered = new LinkedHashMap<>();
	}
	
	public FilterTable () {
		this( new SimpleTable() );
	}
	

	private Table autovivify ( Map<String,Table> map, int col, String key ) {
		if (! map.containsKey(col)) map.put( col, new TreeMap<>() );
		if (! map.get(col).containsKey(key)) map.get(col).put( key, new SimpleTable() );
		return map.get(col).get(key);
	}
	
	public NavigatableMap<String,Table> subTables ( int col ) {
		if (col < 0 || col > colCount()) return new TreeMap<>();
		if (filtered.containsKey(col)) return filtered.get(col);
		obtainWriteLock();
		for (List<String> row : data()) {
			if (row.size()>col) {
				String item = row.get(col);
				Table table = autovivify( filtered, col, item ).append( row );
			}
		}
		releaseWriteLock();
		return filtered.get(col);
	}
	

	public Filter first ( int rows ) {
		Filter newFilter = new FilterTable();
		for (int i=0; i<rows && i<rowCount(); i++) newFilter.append( row(i) );
		return newFilter;
	}
	
	public Filter first ( int column, String lastVal ) {
		
	}
	
	public Filter last ( int rows ) {
		if (rows>rowCount()) rows = rowCount();
		Filter newFilter = new FilterTable();
		for (int i=rowCount()-rows; i<rowCount(); i++) newFilter.append( row(i) );
		return newFilter;
	}
	
	public Filter last ( int column, String firstVal ) {
		
	}
	

	public Filter range ( int column, String minVal, String maxVal ) {
		// sorted navigatable map
		NavigatableMap<String,Table> subs = subTables( column );
		
		// get nearest minVal (which is actually a key)
		Map.Entry<String,Integer> minEntry = subs.floorEntry( minVal );
		if (minEntry==null) minEntry = subs.firstEntry();
		if (minEntry==null) return null; // if there's no firstEntry, then there's no lastEntry!
		// get nearest maxVal (actually a key)
		Map.Entry<String,Integer> maxEntry = subs.ceilingEntry( maxVal );
		if (maxEntry==null) maxEntry = subs.lastEntry();
		
		// new Filter
		Filter newFilter = FilterTable();
		SortedMap<String,Table> subMap = subs.subMap( minEntry.getKey(), maxEntry.getKey() );
		for (Map.Entry<String,Table> entry : subMap) {
			newFilter.append( entry.getValue() );
		}
		return newFilter;
	}
	
	public String min ( int column ) {
		return rowLookupSorted( column ).firstEntry().getKey();
	}
	
	public String max ( int column ) {
		return rowLookupSorted( column ).lastEntry().getKey();
	}
	
	public static void main ( String[] args ) {
		System.out.println(
			(new FilterTable(
				new SimpleTable(
					"a -\n"+
					"b -\n"+
					"g 1\n"+
					"h 2\n"+
					"k 3\n"+
					"x -\n"+
					"z -\n"
				)
			))
			.range( 0, "c", "m" )
		);
	}
}
