package creek;

import java.util.*;

public class HeaderTable extends BasicTable {

	public Set<String> header () {
		if (data().size()>0) return new LinkedHashSet<String>( data().get(0) );
		else return new LinkedHashSet<String>();
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
		
		Set<String> header = header();
		header.addAll( otherHeader );
		if (data().size()>0) data().set( 0, new ArrayList<String>( header ) );
		else data().add( new ArrayList<String>( header ) );
		
		for (int i=headerRow; i<lastRow; i++) {
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
	
	public Table append ( Table table, int headerRow ) {
		return append( table, headerRow, table.rowCount() );
	}
	
	@Override
	public Table append ( Table table ) {
		return append( table, 0 );
	}
	
	// testing
	public static void main ( String[] args ) {
		Table test1 = new HeaderTable();
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
		test1.append( test2 );
		System.out.println( test1 );
	}

}

