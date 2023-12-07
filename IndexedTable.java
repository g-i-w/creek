package creek;

import java.util.*;

public class IndexedTable extends SimpleTable implements SetTable {

	private Map<Integer,NavigableMap<String,List<String>>> index = new HashMap<>();
	

	public IndexedTable () {
		super();
	}

	public IndexedTable ( Table table ) {
		this( table.data() );
	}
	
	public IndexedTable ( Collection<List<String>> rows ) {
		for (List<String> row : rows) append( row );
	}
	

	@Override
	public Table append ( List<String> row ) {
		for (int i=0; i<row.size(); i++) {
			if (! index.containsKey(i)) index.put( i, new TreeMap<>() );
			index.get(i).put( row.get(i), row );
		}
		return super.append( row );
	}

	// SetTable interface

	public SetTable set ( int col ) {
		if (! index.containsKey(col)) return null;
		return new IndexedTable( index.get(col).values() );
	}

	public SetTable setReverse ( int col ) {
		if (! index.containsKey(col)) return null;
		return new IndexedTable( index.get(col).descendingMap().values() );
	}

	public SetTable last ( int col, String thisAndFollowing ) {
		return slice( col, thisAndFollowing, index.get(col).lastEntry().getKey() );
	}
	
	public SetTable slice ( int col, String thisAndFollowing, String approachingThisLimit ) {
		if (! index.containsKey(col)) return null;
		// get the column index
		NavigableMap<String,List<String>> thisCol = index.get(col);
		// get the beginning and ending keys (both keys inclusive if exact match or closest possible enclosed keys)
		thisAndFollowing = thisCol.ceilingKey( thisAndFollowing );
		approachingThisLimit = thisCol.floorKey( approachingThisLimit );
		if (thisAndFollowing==null || approachingThisLimit==null) return null;
		// return submap as new IndexedTable
		return new IndexedTable( thisCol.subMap( thisAndFollowing, true, approachingThisLimit, true ).values() );
	}
	
	
	
	
	// testing
	public static void main ( String[] args ) {
		Table simple = new SimpleTable(
			"a first\n"+
			"a second\n"+
			"c third\n"+
			"b fourth\n"
		);
		SetTable indexed = new IndexedTable( simple );
		System.out.println( "toString():\n"+indexed );
		System.out.println( "setReverse(col 0):\n"+indexed.setReverse( 0 ) );
		System.out.println( "slice(col 0, aa-z):\n"+indexed.slice( 0, "aa", "z" ) );
		// inherited
		System.out.println( "Inherited last(3):\n"+indexed.last( 3 ) );
		System.out.println( "Inherited slice(-1, 2, 0, 2):\n"+indexed.slice( -1, 2, 0, 2 ) );
	}

}
