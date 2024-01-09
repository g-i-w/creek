package creek;

import java.util.*;
import java.util.regex.*;

public class Regex {

	public static Map<String,Pattern> patternCache = null;
	
	public static Pattern pattern ( String regex ) {
		if (patternCache==null) patternCache = new HashMap<>();
		if (! patternCache.containsKey(regex)) {
			Pattern pattern = Pattern.compile( regex );
			patternCache.put(regex, pattern);
		}
		return patternCache.get( regex );
	}
		
	public static Table table ( List<String> rawLines ) throws Exception {
		return table ( rawLines, "(\\w+)" );
	}
		
	public static Table table ( List<String> rawLines, String regex ) throws Exception {
		return table ( rawLines, regex, null, new CSV() );
	}
	
	public static Table table ( List<String> rawLines, String regex, List<String> framing, Table table ) throws Exception {
		for (String rawLine : rawLines) {
			List<String> compounds = new ArrayList<>();
			compounds( compounds, rawLine, regex, framing );
			if (compounds.size()>0) {
				table.append( compounds );
				compounds = new ArrayList<>();
			}
		}
		return table;
	}
	
	public static Table table ( List<String> rawLines, Table regexFraming, Table table ) throws Exception {
		for (String rawLine : rawLines) {
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
	
	public static String compound ( Matcher matcher, String[] framing ) throws Exception {
		return compound( matcher, Arrays.asList( framing ) );
	}

	public static String compound ( Matcher matcher, List<String> framing ) throws Exception {
		StringBuilder output = new StringBuilder();
		for (int i=0; i<Math.max(framing.size(), matcher.groupCount()); i++) {
			if (framing!=null && framing.size()>i) output.append(framing.get(i));
			if (i+1<=matcher.groupCount()) output.append(matcher.group(i+1));
		}
		return output.toString();
	}
	
	public static List<String> compounds ( String input, String regex, List<String> framing ) throws Exception {
		return compounds( new ArrayList<>(), input, regex, framing );
	}

	public static List<String> compounds ( List<String> output, String input, String regex, List<String> framing ) throws Exception {
		Matcher matcher = pattern(regex).matcher(input);
		while (matcher.find()) {
			if (output!=null) output.add( compound( matcher, framing ) );
		}
		//System.out.println( "compounds found for "+input+","+regex+","+framing+": "+compounds );
		return output;
	}
	
	public static String replace ( String input, String regex, List<String> framing ) throws Exception {
		Matcher matcher = pattern(regex).matcher(input);
		StringBuilder output = new StringBuilder();
		int lastIndex = 0;
		while (matcher.find()) {
			output
				.append( input, lastIndex, matcher.start() )
				.append( compound( matcher, framing ) )
			;
			lastIndex = matcher.end();
		}
		return output.toString();
	}
	
	public static boolean exists ( String line, String regex ) throws Exception {
		return pattern( regex ).matcher( line ).find();
	}
	
	// testing
	public static void main ( String[] args ) throws Exception {
		List<String> list = Arrays.asList(
			new String[]{ "1: 12ab,.-[34cd Hi Hello", "2: -h-e-r-e-ABC   DEF", "other stuff 3: 123\n456 789\n\n101112" }
		);
		String regex = "(\\w+)\\W*(\\w+)";
		List<String> framing =  Arrays.asList( new String[]{ "(", "-", ")" } );
		Table regexTable = new CSV(
			"(\\\\d):,, ->\\\n\n"+
			"(\\\\w\\\\w+),(,)\n"
		);
		System.out.println( new SimpleTable(regexTable) );
		
		System.out.println( Regex.table( list, regex, framing, new SimpleTable() ) );
		System.out.println( Regex.table( list, regexTable, new SimpleTable() ) );
		System.out.println( Regex.replace( "12ab,.-[34cd Hi Hello -- 123\n456", regex, framing ) );
	}
	
}
