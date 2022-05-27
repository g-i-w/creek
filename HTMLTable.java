package creek;

import java.util.*;
import java.io.*;

public class HTMLTable extends CSVFile {

	public HTMLTable ( String path ) throws Exception {
		this( new File(path) );
	}
	
	public HTMLTable ( File file ) throws Exception {
		this( file, ",", "\\", "\"" );
	}

	public HTMLTable ( File file, String comma, String escape, String quote ) throws Exception {
		super( file, comma, escape, quote );
	}
	
	public String toString () {
		String html = "<table>\n";
		for (List<String> row : data()) {
			html += "\t<tr>\n";
			for (String item : row) {
				html += "\t\t<td>"+item+"</td>\n";
			}
			html += "\t</tr>\n";
		}
		html += "</table>\n";
		return html;
	}
	
	public static void main ( String[] args ) {
		try {
			System.out.print( new HTMLTable( args[0] ) );
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
