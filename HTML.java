package creek;

import java.util.*;

public class HTML extends SimpleTable {


	public HTML ( Table obj ) {
		super( obj );
		
	}

	// TODO
	//public HTML ( String raw ) {
		// data( raw );
	//}
		
	public String serial () {
		StringBuilder html = new StringBuilder();
		html.append( "<table>\n" );
		for (List<String> row : data()) {
			html.append( "\t<tr>\n" );
			for (String item : row) {
				html
					.append( "\t\t<td>" )
					.append( item )
					.append( "</td>\n" )
				;
			}
			html.append( "\t</tr>\n" );
		}
		html.append( "</table>\n" );
		return html.toString();
	}
	
	// testing
	public static void main ( String[] args ) {
		String csv =
			"1,22,333,4444,55555\n"+
			"a, b, c ,d   ,\"e\"     ,f\"    \r\n"+
			"\"A,B,C\",hello!\n"+
			"\n"+
			"1,\",2,\",3,\\,4\\,,5\n"+
			",,Lone Item,,\n"+
			",,\"quoted item,-,,,very good!\",,\n"+
			",,\"quoted item,-\\\"very good!\\\"\",,\n"+
			",\r\n";
		System.out.println( "\ncsv:\n"+csv );
			
		SimpleTable csv0 = new CSV( csv );
		SimpleTable html = new HTML( csv0 );
		String serial0 = csv0.serial();
		String serial1 = html.serial();
		
		System.out.println( "\ndata:\n"+html.data() );
		System.out.println( "\nHTML:\n"+html.serial() );
	}

}
