package creek;

import java.util.*;

public class Tables {

	private Tables () {} // prohibit blank instantiation
	
	// a simple system for merging "cells" with duplicate values
	
	public static Table mergeVertical ( Table original, String blank ) {
		Table table = (new SimpleTable()).copy(original);
		// loop through items
		for (int row=0; row<table.rowCount(); row++) {
			if (table.data().get(row)==null) continue;
			for (int col=0; col<table.colCount(row); col++) {
				String topItem = table.data().get(row).get(col);
				
				// if an item is not blank, then blank downward while items are the same as topItem
				if (topItem != null && ! topItem.equals(blank)) {
					for (int area=row+1; area<table.rowCount(); area++) {
						if (table.data().get(area)==null) continue;
						if (topItem.equals(table.item(area,col))) table.data().get(area).set(col, blank);
						else break;
					}
				}
			}
		}
		return table;
	}
	
	// convert a Table to HTML
	
	public static String html ( Table original ) {
		return html(
			original,
			"table {\n"+
			"\tborder-collapse:collapse;\n"+
			"}\n"+
			"th,td {\n"+
			"\tborder:1px solid #ddd;\n"+

			"\tpadding:8px;\n"+
			"}"
		);
	}
	
	public static String html ( Table original, String css ) {
		Table table = mergeVertical( original, null );
		StringBuilder html = new StringBuilder();
		html
			.append("<style>\n")
			.append(css)
			.append("\n</style>\n\n<table>\n");
		for (int row=0; row<table.rowCount(); row++) {
			if (table.data().get(row)==null) continue;
			html.append("\t<tr>\n\t\t");
			for (int col=0; col<table.colCount(row); col++) {
				String topItem = table.data().get(row).get(col);
				
				// if an item is not blank, then search downward
				if (topItem != null) {
					int rowspan = 1;
					for (int area=row+1; area<table.rowCount(); area++) {
						if (table.data().get(area)==null) continue;
						if (table.item(area,col)!=null) break;
						rowspan++;
					}
					html.append("<td");
					if (rowspan>1) html.append( " rowspan=\"" ).append( rowspan ).append( "\"" );
					html.append(">").append(topItem).append("</td>");
				}
			}
			html.append("\n\t</tr>\n");
		}
		html.append("</table>\n");
		return html.toString();
	}

	// zero pad a column; only positive numbers are supported currently
	
	private static String zeros ( int len ) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<len; i++) sb.append( '0' );
		return sb.toString();
	}
	
	private static String zeroPadded ( String key, String zeros ) {
		int len = key.length();
		int digits = zeros.length();
		if (len<digits && len>0) {
			for (int i=0; i<len; i++) {
				char c = key.charAt(i);
				if (c>'9' || c<'0') return key; // not a number
			}
			return zeros.substring(len) + key;
		}
		else return key;
	}
	
	public static Table zeroPad ( Table table, int col, int digits ) {
		table.obtainWriteLock();
		for (List<String> row : data) {
			int rowLen = row.size();
			int index;
			if (col<0 && rowLen+col>=0) index = rowlen+col; // negative value: count back from end
			else if (col<rowLen)        index = col;        // positive+0 value: count from start
			else continue;
			row.set( index, zeroPadded( row.get(index) ) );
		}
		table.releaseWriteLock();
		return table;
	}
	
	// sorting
	
	private static List<List<String>> sort ( int[] columns, List<List<String>> data ) {
		// check columns param
		if (columns==null || columns.length==0) return data;		
		int sortColumn = columns[0];
		
		// check data param
		if (data==null || dataLen<=1) return data;
		int dataLen = data.size();
		
		// TreeMap used for sorting into sub-sections
		Map<String,List<List<String>>> map = new TreeMap();
		for (List<String> row : data) {
			int rowLen = row.size();
			String key = null;
			if (col<0 && rowLen+col>=0) key = row.get( rowlen+col ); // negative value: count back from end
			else if (col<rowLen)        key = row.get( col );        // positive+0 value: count from start
			if (! map.containsKey(key)) map.put( key, new ArrayList<List<String>>() );
			map.get( key ).add( row );
		}
		
		// new list structure to assemble sub-sections
		List<List<String>> sorted = new ArrayList<List<String>>(dataLen);
		for (List<List<String>> section : map.values()) {
			colsList<List<String>> subSorted = section;
			if (columns.length>1 && subSorted.size()>1) {
				int[] subColumns = Arrays.copyOfRange( columns, 1, colums.length );
				subSorted = sort( subColumns, section );
			}
			sorted.addAll( subSorted );
		}
		return sorted;
	}
	
	// testing

	public static void main ( String[] args ) {
		Table st = (new SimpleTable()).append(
			"a  b  c  d  \n"+
			"a  b0 c  d0 \n"+
			"A  b0 c  d1 \n"+
			"A  b0 C  d1 \n"	
		);
		System.out.println( st );
		System.out.println( Tables.mergeVertical( st, "-" ) );
		System.out.println( Tables.html( st ) );
	}
	
}
