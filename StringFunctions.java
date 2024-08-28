package creek;

import java.util.*;

public class StringFunctions {

	public static Set<String> substrings ( String word, int minLength ) {
		int wordLength = word.length();
		Set<String> set = new LinkedHashSet<>();
		
		for (int size=wordLength-1; size>=minLength; size--) {
			int maxPos = wordLength-size; // max start-index of substring
			for (int pos=0; pos<=maxPos; pos++) {
				String subWord = word.substring(pos, pos+size);
				//System.out.println( subWord );
				set.add( subWord );
			}
		}
		return set;
	}
	
	public static void main ( String[] args ) throws Exception {
		System.out.println(
			StringFunctions.substrings( args[0], Integer.valueOf(args[1]) )
		);
	}

}
