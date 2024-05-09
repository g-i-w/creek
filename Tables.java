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
		return html( original, true );
	}

	public static String html ( Table original, boolean merge ) {
		return html(
			original,
			"table {\n"+
			"\tborder-collapse:collapse;\n"+
			"}\n"+
			"th,td {\n"+
			"\tborder:1px solid #ddd;\n"+

			"\tpadding:8px;\n"+
			"}"
			,
			merge
		);
	}
	
	public static String html ( Table original, String css ) {
		return html( original, css, true );
	}

	public static String html ( Table original, String css, boolean merge ) {
		Table table = ( merge ? mergeVertical( original, null ) : original );
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
