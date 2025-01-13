package creek;

import java.util.*;
import java.util.regex.*;
import java.util.function.Function;

public class Regex {

	// Pattern caching

	public static Map<String,Pattern> patternCache = null;
	
	public static Pattern pattern ( String regex ) {
		if (patternCache==null) patternCache = new HashMap<>();
		if (! patternCache.containsKey(regex)) {
			Pattern pattern = Pattern.compile( regex );
			patternCache.put(regex, pattern);
		}
		return patternCache.get( regex );
	}
	
	public static Matcher matcher ( String input, String regex ) {
		return pattern( regex ).matcher( input );
	}

	////////// Simple output methods //////////

	// Groups

	public static List<String> groups ( Matcher matcher ) throws Exception {
		return groups( new ArrayList<String>( matcher.groupCount() ), matcher );
	}

	public static List<String> groups ( List<String> output, Matcher matcher ) throws Exception {
		for (int i=0; i<matcher.groupCount(); i++) {
			output.add( matcher.group(i+1) );
		}
		//System.out.println( output );
		return output;
	}
	
	public static List<String> groups ( String input, String regex ) throws Exception {
		List<String> output = new ArrayList<>();
		if (input==null || regex==null) return output;
		Matcher matcher = matcher( input, regex );
		while( matcher.find() ) groups( output, matcher );
		return output;
	}
	
	public static String first ( String input, String regex ) {
		try {
			return groups( input, regex ).get(0);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Table columnGroup ( Table input, String regex, int col, Table output ) throws Exception {
		for (List<String> row : input.data()) {
			int rowSize = row.size();
			if (col > rowSize-1) continue;
			List<String> subCols = groups( row.get( col ), regex );
			List<String> newRow = new ArrayList<>( rowSize-1+subCols.size() );
			for (int i=0; i<col; i++) newRow.add( row.get( i ) );
			newRow.addAll( subCols );
			for (int i=col+1; i<rowSize; i++) newRow.add( row.get( i ) );
			output.append( newRow );
		}
		return output;
	}
	
	// Compounds
	
	public static String compound ( Matcher matcher, String[] framing ) throws Exception {
		return compound( matcher, Arrays.asList( framing ) );
	}

	public static String compound ( Matcher matcher, List<String> framing ) throws Exception {
		StringBuilder output = new StringBuilder();
		int ceiling = ( framing!=null ? Math.max(framing.size(), matcher.groupCount()) : matcher.groupCount() );
		for (int i=0; i<ceiling; i++) {
			if (framing!=null && framing.size()>i) output.append(framing.get(i));
			if (i+1<=matcher.groupCount()) output.append(matcher.group(i+1));
		}
		return output.toString();
	}
	
	public static List<String> compounds ( String input, String regex, List<String> framing ) throws Exception {
		return compounds( new ArrayList<>(), input, regex, framing );
	}

	public static List<String> compounds ( List<String> output, String input, String regex, List<String> framing ) throws Exception {
		Matcher matcher = matcher( input, regex );
		while (matcher.find()) output.add( compound( matcher, framing ) );
		return output;
	}
	
	////////// Table output methods //////////

	// Each group becomes one row of table
	
	public static Table table ( String input, String regex, Table table ) throws Exception {
		return table( input, regex, table, null );
	}
	
	public static Table table ( String input, String regex, Table table, List<String> tag ) throws Exception {
		//System.out.println( "Regex.table input blob: "+input );
		Matcher matcher = matcher( input, regex );
		//System.out.println( "Regex.table regex: "+regex );
		while( matcher.find() ) {
			List<String> row = new ArrayList<>( matcher.groupCount()+1 );
			if (tag!=null) row.addAll( tag );
			table.append( groups( row, matcher ) );
		}
		return table;
	}
	
	// Collection of all groups in each line become one row of table
	
	public static Table table ( List<String> input ) throws Exception {
		return table ( input, "(\\w+)" );
	}
		
	public static Table table ( List<String> input, String regex ) throws Exception {
		return table ( input, regex, new SimpleTable(), null );
	}
	
	public static Table table ( List<String> input, String regex, Table table ) throws Exception {
		return table ( input, regex, table, null );
	}
	
	public static Table table ( List<String> input, String regex, Table table, List<String> tag ) throws Exception {
		//System.out.println( "Regex.table input lines: "+input );
		List<String> row = new ArrayList<>();
		for (String line : input) {
			//System.out.println( "Regex.table line: "+line );
			Matcher matcher = matcher( line, regex );
			while( matcher.find() ) {
				if (tag!=null) row.addAll( tag );
				groups( row, matcher );
			}
			//System.out.println( "Regex.table row: "+row );
			if (row.size()>0) {
				table.append( row );
				row = new ArrayList<>();
			}
		}
		return table;
	}
	
	// Collection of all compounds (created using "framing") in each line become one row of table
	
	public static Table table ( List<String> input, String regex, List<String> framing, Table table ) throws Exception {
		for (String rawLine : input) {
			List<String> compounds = new ArrayList<>();
			compounds( compounds, rawLine, regex, framing );
			if (compounds.size()>0) {
				table.append( compounds );
				compounds = new ArrayList<>();
			}
		}
		return table;
	}
	
	public static Table table ( List<String> input, Table regexFraming, Table table ) throws Exception {
		for (String rawLine : input) {
			List<String> compounds = new ArrayList<>();
			for (List<String> row : regexFraming.data()) {
				String regex = row.get(0);
				List<String> framing = row.subList(1,row.size());
				compounds( compounds, rawLine, regex, framing );
			}
			if (compounds.size()>0) {
				table.append( compounds );
				compounds = new ArrayList<>();
			}
		}
		return table;
	}
	
	////////// Replace //////////

	public static String replace ( String input, String regex, List<String> framing ) throws Exception {
		return replace( input, regex, framing, null, null );
	}
	
	public static String replace ( String input, String regex, String subOld, String subNew ) throws Exception {
		return replace( input, regex, null, subOld, subNew );
	}
	
	public static String replace ( String input, String regex, List<String> framing, String subOld, String subNew ) throws Exception {
		return replace( input, regex, framing, subOld, subNew, null ); 
	}

	public static String replace ( String input, String regex, List<String> framing, Function<String,String> transform ) throws Exception {
		return replace( input, regex, framing, null, null, transform ); 
	}

	public static String replace ( String input, String regex, List<String> framing, String subOld, String subNew, Function<String,String> transform ) throws Exception {
		Matcher matcher = matcher( input, regex );
		StringBuilder output = new StringBuilder();
		int lastIndex = 0;
		while (matcher.find()) {
			String compound = compound( matcher, framing );
			//System.out.print( compound+" --> " );
			if (transform==null && subOld!=null & subNew!=null) compound = compound.replaceAll( subOld, subNew );
			if (transform!=null) compound = transform.apply( compound );
			//System.out.println( compound );
			output
				.append( input, lastIndex, matcher.start() )
				.append( compound )
			;
			lastIndex = matcher.end();
		}
		output.append( input, lastIndex, input.length() );
		return output.toString();
	}
	
	////////// Exists //////////

	public static boolean exists ( String line, String regex ) {
		return matcher( line, regex ).find();
	}
	


	// testing
	public static void main ( String[] args ) throws Exception {
		List<String> list = Arrays.asList(
			new String[]{ "1: 12ab,.-[34cd Hi Hello", "2: -h-e-r-e-ABC   DEF", "other stuff 3: 123\n456 789\n\n101112" }
		);
		String regex = "(\\w+)\\W+(\\w+)";
		List<String> framing =  Arrays.asList( new String[]{ "(", "-", ")" } );
		Table regexTable = new CSV(
			"(\\\\d):,, ->\\\n\n"+
			"(\\\\w\\\\w+),(,)\n"
		);
		System.out.println( "testing Regex.groups( List<String> output, Matcher matcher ):" );
		for (String str : list) System.out.println( "group: "+Regex.groups( str, regex ) );
		System.out.println();
		System.out.println( "testing Regex.table( List<String> input ):" );
		System.out.println();
		System.out.println( Regex.table( list ) );
		System.out.println( "testing Regex.table( List<String> input, Table regexFraming, Table table ):" );
		System.out.println( "regex table:\n"+(new SimpleTable(regexTable)) );
		System.out.println( Regex.table( list, regex, framing, new SimpleTable() ) );
		System.out.println( Regex.table( list, regexTable, new SimpleTable() ) );
		System.out.println();
		System.out.println( "testing Regex.replace( String input, String regex, List<String> framing ):" );
		System.out.println( Regex.replace( "12ab,.-[34cd Hi Hello -- 123\n456", regex, framing ) );
	}
	
}
