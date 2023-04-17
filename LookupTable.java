package creek;

import java.util.*;

public class LookupTable extends SimpleTable {

	private Map<String,Integer> emptyMap = new HashMap<>();
	
	private Map<Integer,Map<String,Integer>> rowLookupCol;
	private Map<Integer,Map<String,Integer>> colLookupRow;
	
	
	public LookupTable ( Table table ) {
		super( table );
		rowLookupCol = new LinkedHashMap<>();
		colLookupRow = new LinkedHashMap<>();
	}
	


	public Map<String,Integer> rowLookup ( int keyRow ) {
		if (keyRow >= rowCount()) return emptyMap;
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
		if (keyCol < 0) return emptyMap;
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
		if (keyRow >= rowCount() || valRow >= rowCount() || keyRow < 0 || valRow < 0) return "";
		Integer col = rowLookup( keyRow ).get( key );
		try {
			return data().get(valRow).get(col);
		} catch (Exception e) {
			return "";
		}
	}
	
	public String colLookup ( int keyCol, int valCol, String key ) {
		if (keyCol >= rowCount() || valCol >= rowCount() || keyCol < 0 || valCol < 0) return "";
		Integer row = colLookup( keyCol ).get( key );
		try {
			return data().get(row).get(valCol);
		} catch (Exception e) {
			return "";
		}
	}
	
	public Map<String,String> rowLookup ( int keyRow, int valRow ) {
		Map<String,String> map = new LinkedHashMap<>();
		if (keyRow >= rowCount() || valRow >= rowCount() || keyRow < 0 || valRow < 0) return map;
		for ( String key : data().get(keyRow) ) {
			map.put( key, rowLookup(keyRow, valRow, key) );
		}
		return map;
	}
	
	public Map<String,String> colLookup ( int keyCol, int valCol ) {
		Map<String,String> map = new LinkedHashMap<>();
		if (keyCol >= rowCount() || valCol >= rowCount() || keyCol < 0 || valCol < 0) return map;
		for ( String key : colLookup(keyCol).keySet() ) {
			map.put( key, colLookup(keyCol, valCol, key) );
		}
		return map;
	}
	

	public String lookup ( int leftCol, String rowKey, int topRow, String colKey ) {
		Map<String,Integer> colMap = colLookup( leftCol );

		Map<String,Integer> rowMap = rowLookup( topRow );

		try {
			return data().get( colMap.get(rowKey) ).get( rowMap.get(colKey) );
		} catch (Exception e) {
			//e.printStackTrace();
			return "";
		}
	}
	
	public String lookup ( String rowKey, String colKey ) {
		return lookup (0, rowKey, 0, colKey);
	}
	
	
	
	public static void main ( String[] args ) {
		LookupTable lookup = new LookupTable(
			new CSV(
				"1,22,333,4444,55555\n"+
				"a, b, c ,d   ,e    \r\n"+
				"A,B,C,found it!\n"+
				"\n"+
				"1,\",2,\",3,\\,4\\,,5\n"+
				"AA,,Lone Item,,\n"+
				",,\"quoted item\",,\n"+
				",,\"\\\"hi!\\\"\",,\n"+
				",\r\n"
			)
		);
		
		lookup.append(
			new CSV(
				"a,b,c"+
				",d,e,f"+
				",g,h,i\n"
			)
		);
		
		System.out.println( lookup );

		System.out.println( "row String: '"+lookup.rowLookup( 0, 2, "4444" )+"'" );
		System.out.println( "row Map: '"+lookup.rowLookup( 0, 2 )+"'" );
			
		System.out.println( "col String: '"+lookup.colLookup( 1, 3, "B" )+"'" );
		System.out.println( "col Map: '"+lookup.colLookup( 1, 3 )+"'" );

		System.out.println( "lookup: '"+lookup.lookup( "AA","333" )+"'" );			
		System.out.println( "lookup: '"+lookup.lookup( "A","4444" )+"'" );			
	}

}
