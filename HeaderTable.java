package creek;

import java.util.*;

public class HeaderTable extends BasicTable {

	public List<String> headerList () {
		if (data().size()>0) return data().get(0);
		else return new ArrayList<String>();
	}
	
	public Set<String> headerSet () {
		if (data().size()>0) return new LinkedHashSet<String>( data().get(0) );
		else return new LinkedHashSet<String>();
	}
	
	public Map<String,Integer> headerMap () {
		Map<String,Integer> headerMap = new LinkedHashMap<String,Integer>();
		List<String> headerList = headerList();
		if (data().size()>0) {
			for (int i=0; i<headerList.size(); i++) {
				headerMap.put( headerList.get(i), i );
			}
		}
		return headerMap;
	}
	
	public HeaderTable ( Table table ) {
		super( table );
	}

	public HeaderTable () {
		super( new BasicTable() );
	}


	public Table append ( Table table, int headerRow, int lastRow ) {
		LookupTable otherTable = new LookupTable( table );
		Set<String> otherHeader = otherTable.rowLookup( headerRow ).keySet();
		
		Set<String> header = headerSet();
		header.addAll( otherHeader );
		List<String> newHeader = new ArrayList<String>( header );
		if (data().size()>0) {
			data().set( 0, newHeader );
		} else {
			data().add( newHeader );
		}
		
		for (int i=headerRow+1; i<lastRow; i++) {
			List<String> toRow = new ArrayList<>();
			Map<String,String> fromRow = otherTable.rowLookup( headerRow, i );
			for (String colHeader : header) {
				if (fromRow.containsKey( colHeader )) {
					toRow.add( fromRow.get( colHeader ) );
				} else {
					toRow.add( null );
				}
			}
			append( toRow );
		}
		
		return this;
	}
	
	public Table join ( String thisKeyCol, Table otherTable, String otherKeyCol, String otherValCol ) {
		if (rowCount()==0) return this;
		
		// convert other table into a LookupTable
		LookupTable otherLookup = new LookupTable( otherTable );
		
		// get the header maps for this and the other table
		Map<String,Integer> thisHeaderMap = headerMap();
		Map<String,Integer> otherHeaderMap = otherLookup.rowLookup(0);
		
		// convert header Strings to int
		int thisKey = thisHeaderMap.get(thisKeyCol);
		int otherKey = otherHeaderMap.get(otherKeyCol);
		int otherVal = otherHeaderMap.get(otherValCol);
		
		// get the key->val map from the other table
		Map<String,String> otherMap = otherLookup.colLookup( otherKey, otherVal );
		
		// add a new column on the far right side
		List<String> headerList = headerList();
		int thisVal = headerList.size(); // NEXT column in THIS table
		headerList.add( otherValCol ); // NEW column in THIS table
		
		// loop through rows of this table and use the other table key->val to assign a val
		for (int i=1; i<rowCount(); i++) {
			List<String> row = data().get(i);
			
			// workaround for a bug where evidently compiler optimization meant I couldn't edit an item in data().get(i) on the fly...
			List<String> newRow = new ArrayList<String>();
			newRow.addAll( row );			
			// pad end of line with nulls
			for (int n=newRow.size(); n<=thisVal; n++) newRow.add( null );
			
			// get the key from this table (newRow) and val from other table (otherMap)
			String key = newRow.get(thisKey);
			String val = otherMap.get(key);

			// replace the last null with val (or null)
			newRow.set( thisVal, val );
			
			// replace the row in data()
			data().set( i, newRow );
		}
		return this;
	}
	
	public Table append ( Table table, int headerRow ) {
		return append( table, headerRow, table.rowCount() );
	}
	
	@Override
	public Table append ( Table table ) {
		return append( table, 0 );
	}
	
	// testing
	public static void main ( String[] args ) {
		HeaderTable test1 = new HeaderTable();
		test1
			.append( new String[]{ "a", "b", "c" } )
			.append( new String[]{ "A", "B", "C" } )
			.append( new String[]{ "AA", "BB", "CC" } )
		;
		Table test2 = new HeaderTable();
		test2
			.append( new String[]{ "b", "d", "e" } )
			.append( new String[]{ "B", "D", "E" } )
			.append( new String[]{ "BB", "DD", "EE" } )
		;
		Table test3 = new HeaderTable();
		test3
			.append( new String[]{ "key", "value" } )
			.append( new String[]{ "A", "-A-" } )
			.append( new String[]{ "AA", "-AA-" } )
			.append( new String[]{ "B", "-B-" } )
			.append( new String[]{ "BB", "-BB-" } )
		;
		test1.append( test2 );
		test1.join( "a", test3, "key", "value" );
		System.out.println( test1 );
	}

}

