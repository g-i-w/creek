package creek;

import java.util.*;

public class BasicTable extends AbstractTable {

	public BasicTable () {
		data( new ArrayList<List<String>>() );
	}
	
	public BasicTable ( Table table ) {
		if ( table == null ) data( new ArrayList<List<String>>() );
		else data( table.data() );
	}

	public Table append ( String raw ) {
		for (char c : raw.toCharArray()) addItem( c );
		addRow();
		return this;
	}
	
	public String serial () {
		return data().toString();
	}
	
	public Table create () {
		return new BasicTable();
	}
	
	public static void main ( String[] args ) {
		System.out.println(
			(new BasicTable( new BasicTable() ))
			.append( "abc" )
			.append( new String[]{ "d", "e", "f" } )
		);
	}
	
}
