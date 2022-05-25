package creek;

import java.util.*;

public class CSVLookup extends CSV {

	private Map<String,Integer> emptyMap = new HashMap<>();
	
	private Map<Integer,Map<String,Integer>> rowLookupCol;
	private Map<Integer,Map<String,Integer>> colLookupRow;
	
	private void init () {
		rowLookupCol = new LinkedHashMap<>();
		colLookupRow = new LinkedHashMap<>();
	}
		
	// constructors
	public CSVLookup () {
		super( "", ",", "\\", "\"" );
		init();
	}
	
	public CSVLookup ( String csv ) {
		super( csv, ",", "\\", "\"" );
		init();
	}

	public CSVLookup ( String csv, String comma, String escape, String quote ) {
		super( csv, comma, escape, quote );
		init();
	}
	


	public Map<String,Integer> rowLookup ( int keyRow ) {
		if (keyRow >= length()) return emptyMap;
		if (! rowLookupCol.containsKey(keyRow)) {
			Map<String,Integer> map = new LinkedHashMap<>();
			int i=0;
			for (String item : data().get(keyRow)) {
				map.put( item, i++ );
			}
			rowLookupCol.put( keyRow, map );
			return map;
		} else {
			return rowLookupCol.get(keyRow);
		}
	}
	
	public Map<String,Integer> colLookup ( int keyCol ) {
		if (keyCol >= width()) return emptyMap;
		if (! colLookupRow.containsKey(keyCol)) {
			Map<String,Integer> map = new LinkedHashMap<>();
			for (int row=0; row<data().size(); row++) {
				if (keyCol < data().get(row).size()) {
					map.put( data().get(row).get(keyCol), row );
				}
			}
			colLookupRow.put( keyCol, map );
			return map;
		} else {
			return colLookupRow.get(keyCol);
		}
	}
	
	public String rowLookup ( int keyRow, int valRow, String key ) {
		if (keyRow >= length() || valRow >= length()) return "";
		Integer col = rowLookup( keyRow ).get( key );
		try {
			return data().get(valRow).get(col);
		} catch (Exception e) {
			return "";
		}
	}
	
	public String colLookup ( int keyCol, int valCol, String key ) {
		if (keyCol >= width() || valCol >= width()) return "";
		Integer row = colLookup( keyCol ).get( key );
		try {
			return data().get(row).get(valCol);
		} catch (Exception e) {
			return "";
		}
	}
	
	public Map<String,String> rowLookup ( int keyRow, int valRow ) {
		Map<String,String> map = new LinkedHashMap<>();
		if (keyRow >= length() || valRow >= length()) return map;
		for ( String key : data().get(keyRow) ) {
			map.put( key, rowLookup(keyRow, valRow, key) );
		}
		return map;
	}
	
	public Map<String,String> colLookup ( int keyCol, int valCol ) {
		Map<String,String> map = new LinkedHashMap<>();
		if (keyCol >= length() || valCol >= length()) return map;
		for ( String key : colLookup(keyCol).keySet() ) {
			map.put( key, colLookup(keyCol, valCol, key) );
		}
		return map;
	}
	

	
	
	
	public static void main ( String[] args ) {
		String csv =
			"1,22,333,4444,55555\n"+
			"a, b, c ,d   ,e    \r\n"+
			"A,B,C,found it!\n"+
			"\n"+
			"1,\",2,\",3,\\,4\\,,5\n"+
			",,Lone Item,,\n"+
			",,\"quoted item, ,,,very good!\",,\n"+
			",,\"quoted item, \\\"very good!\\\"\",,\n"+
			",\r\n";
		System.out.println( "\ncsv:\n"+csv );
			
		CSVLookup csvObj = new CSVLookup( csv );
		csvObj
			.append( "a,b,c" )
			.append( ",d,e,f" )
			.append( ",g,h,i" )
			.append( "\n" )
		;
		
		System.out.println( "\ndata:\n"+csvObj.rows() );
		System.out.println( "\ncsvObj:\n"+csvObj );

		System.out.println( "row String: '"+csvObj.rowLookup( 0, 2, "4444" )+"'" );
		System.out.println( "row Map: '"+csvObj.rowLookup( 0, 2 )+"'" );
			
		System.out.println( "col String: '"+csvObj.colLookup( 1, 3, "B" )+"'" );
		System.out.println( "col Map: '"+csvObj.colLookup( 1, 3 )+"'" );
			
	}

}
