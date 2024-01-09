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
	
	public static Table table ( List<String> rawLines, String regex, String[] framing, Table table ) throws Exception {
		Pattern pattern = pattern( regex );
		List<String> tableLine = new ArrayList<>();
		for (String rawLine : rawLines) {
			Matcher matcher = pattern.matcher( rawLine );
			boolean found = false;
			while (matcher.find()) {
				tableLine.add( compound( matcher, framing ) );
				found = true;
			}
			if (found) {
				table.append( tableLine );
				tableLine = new ArrayList<>();
			}
		}
		return table;
	}
	
	public static String compound ( Matcher matcher, String[] framing ) throws Exception {
		StringBuilder output = new StringBuilder();
		for (int i=0; i<Math.max(framing.length, matcher.groupCount()); i++) {
			if (framing!=null && framing.length>i) output.append(framing[i]);
			if (i+1<=matcher.groupCount()) output.append(matcher.group(i+1));
		}
		return output.toString();
	}
	
	public static String replace ( String input, String regex, String[] framing ) throws Exception {
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
			new String[]{ "12ab,.-[34cd Hi Hello", "--ABC   DEF", "123\n456 789\n\n101112" }
		);
		String regex = "(\\w+)\\W*(\\w+)";
		String[] framing =  new String[]{ "(", "-", ")" };
		
		System.out.println( Regex.table( list, regex, framing, new SimpleTable() ) );
		System.out.println( Regex.replace( "12ab,.-[34cd Hi Hello -- 123\n456", regex, framing ) );
	}
	
}
