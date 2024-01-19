package creek;

import java.util.*;

public class JSON extends AbstractTree {

	// states
	private static final int INIT = 0;
	private static final int FIND_KEY = 1;
	private static final int KEY = 2;
	private static final int KEY_DELIM = 3;
	private static final int OBJECT = 4;
	private static final int STRING = 5;
	private static final int STRING_NONQUOTE = 6;
	private static final int STRING_ESCAPE = 7;
	
	private static final String[] reverse_state = {
		"INIT",
		"FIND_KEY",
		"KEY",
		"KEY_DELIM",
		"OBJECT",
		"STRING",
		"STRING_NONQUOTE",
		"STRING_ESCAPE"
	};


	private List<String> keys = new ArrayList<>();
	private List<Boolean> modes = new ArrayList<Boolean>();
	private StringBuilder currentKey = null;
	private StringBuilder currentValue = null;
	private int charCount;
	private int lineCount;
	private boolean strict;
	private boolean printDebug;

	
	public JSON ( boolean strict, boolean printDebug ) {
		this.strict = strict;
		this.printDebug = printDebug;
	}
	
	public JSON () {
		this( true, false );
	}

	public Tree create () {
		return new JSON();
	}
	
	
	private boolean objectMode () {
		if (modes.size()==0) return true;
		return modes.get(modes.size()-1).booleanValue();
	}
		
	private Tree currentBranch () {
		return auto(keys);
	}

	private void throwException ( String message ) throws Exception {
		if (printDebug) System.out.println( serialize() );
		if (strict) throw new Exception( message );
	}

	private boolean isWord ( char c ) {
		return (c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || c=='.';
	}
	
	private boolean isSpaceOrComma ( char c ) {
		return (c==' ' || c=='\t' || c=='\r' || c=='\n' || c==',');
	}
	
	private void levelUp ( boolean mode ) {
		keys.add( currentKey.toString() );
		modes.add( mode );
		if (printDebug) System.out.println( "pushed: "+keys+modes );
		currentKey = null;
	}
	
	private void levelDown () throws Exception {
		if (keys.size()==0) return;
		//if (keys.size()==0) throwException( "more closing '}' than opening '{' at "+charLocation() );
		keys.remove( keys.size()-1 );
		modes.remove( modes.size()-1 );
	}
		
	private void newValue () {
		if (currentKey==null) currentKey = new StringBuilder( currentBranch().integerKey() );
		currentBranch().add( currentKey.toString(), currentValue.toString() );
		if (printDebug) System.out.println( "updated value: "+this );
		currentValue = null;
		currentKey = null;
	}
	
	private void initKey () {
		currentKey = new StringBuilder();
	}
	
	private void initValue () {
		currentValue = new StringBuilder();
	}
	
	private String objectHierarchy () {
		return "\""+String.join(".",keys)+(currentKey==null ? "" : "."+currentKey)+"\"";
	}
	
	private String charLocation () {
		return "line "+lineCount+" character "+charCount;
	}

	public Tree deserialize ( String serial ) throws Exception {
		charCount = 0;
		lineCount = 1;
		int state = INIT;
		
		for (Character c : serial.toCharArray()) {
			if (printDebug) System.out.print( c+": "+reverse_state[state]+" -> " );

			if (state == INIT) {
				if (c == '"') {
					state = KEY;
					initKey();
				} else if (c == '}') {
					// throw exception for empty data set
				}
				
			} else if (state == FIND_KEY) {
				if (c == '"') {
					initKey();
					state = KEY;
				} else if (c == '}') {
					levelDown();
					if (objectMode()) state = FIND_KEY; // object mode
				}
				
			} else if (state == KEY) {
				if (c == '"') {
					state = KEY_DELIM;
				} else {
					currentKey.append(c);
				}

			} else if (state == KEY_DELIM) {
				if (c == ':') {
					state = OBJECT;
				}

			} else if (state == OBJECT) {
				if (currentKey==null) {
					// throw exception
				}
				
				if (c == '{') {
					levelUp( true );
					initKey();
					state = FIND_KEY;
				} else if (c == '[') {
					levelUp( false );
					// no initKey for arrays
				} else if (c == '"') {
					initValue();
					state = STRING;
				} else if (isWord(c)) {
					state = STRING_NONQUOTE;
					initValue();
					currentValue.append(c);
				} else if (c == ']') {
					levelDown();
					if (objectMode()) state = FIND_KEY; // object mode
				} else if (c == '}') {
					// throw exception, } should be found in FIND_KEY
				}

			} else if (state == STRING) {
				if (c == '\\') {
					state = STRING_ESCAPE;
				} else if (c == '"') {
					newValue();
					if (objectMode()) state = FIND_KEY; // object mode
					else state = OBJECT; // array mode
				} else {
					currentValue.append(c);
				}

			} else if (state == STRING_NONQUOTE) {
				if (c == '\\') {
					state = STRING_ESCAPE;
				} else if (isSpaceOrComma(c)) {
					newValue();
					if (objectMode()) state = FIND_KEY; // object mode
					else state = OBJECT; // array mode
				/*} else if (! isWord(c)) {
					throwException( "found '"+c+"' in non-quoted value ("+currentValue+") to be added to "+objectHierarchy()+" at "+charLocation() );
				*/
				} else {
					currentValue.append(c);
				}

			} else if (state == STRING_ESCAPE) {
				currentValue.append(c);
				state = STRING;
			}
			
			if (printDebug) System.out.println( reverse_state[state]+" "+this+" "+objectMode()+" "+currentKey+" "+currentValue );
			charCount++;
			if (c=='\n') {
				lineCount++;
				charCount=0;
			}
		}
		
		if (keys.size()!=0) {
			System.out.println( keys );
			throwException( "more opening '{' than closing '}' after end of stream at "+charLocation() );
		}
		return this;
	}
	
	public static void main ( String[] args ) throws Exception {
		Tree json = new JSON( true, ( args.length>1 ? Boolean.valueOf(args[1]) : false ) );
		
		String input = FileActions.read(args[0]);
		System.out.println( input );

		json.deserialize( input );
		System.out.println( json );
		
		String output = json.serialize();
		System.out.println( output );
	}

}
