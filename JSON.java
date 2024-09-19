package creek;

import java.util.*;

public class JSON extends AbstractTree {

	// states for FSM
	private static final int INIT = 0;
	private static final int FIND_KEY = 1;
	private static final int FIND_KEY_COMMENT = 2;
	private static final int KEY = 3;
	private static final int KEY_UNQUOTED = 4;
	private static final int KEY_DELIM = 5;
	private static final int VALUE = 6;
	private static final int VALUE_COMMENT = 7;
	private static final int STRING = 8;
	private static final int STRING_NONQUOTE = 9;
	private static final int STRING_ESCAPE = 10;
	private static final int CHECK_FOR_UNESCAPED_QUOTE = 11;
	private static final String[] reverse_state = {
		"INIT",
		"FIND_KEY",
		"FIND_KEY_COMMENT",
		"KEY",
		"KEY_UNQUOTED",
		"KEY_DELIM",
		"VALUE",
		"VALUE_COMMENT",
		"STRING",
		"STRING_NONQUOTE",
		"STRING_ESCAPE",
		"CHECK_FOR_UNESCAPED_QUOTE"
	};

	// used by FSM
	private static final boolean OBJECT_MODE = true;
	private static final boolean ARRAY_MODE = false;
	private List<Boolean> modes = new ArrayList<Boolean>();
	private List<String> keys = new ArrayList<>();
	private StringBuilder keyUnderConstruction = null;
	private StringBuilder valueUnderConstruction = null;
	private String currentSerial = null;
	private int charCount;
	private int lineCount;
	private int absoluteCount;
	private boolean trailingComma = false;

	// Leniency:
	private int leniency;
	// 3: as relaxed as possible, 2: just the essentials, 1: very rigorous, 0: overly pedantic
	public static final int RELAXED = 3;
	public static final int CAREFUL = 2;
	public static final int RIGOROUS = 1;
	public static final int PEDANTIC = 0;
	
	// Seriousness:
	// 3: caution label, 2: important note, 1: just FYI
	public static final int CAUTION = 3;
	public static final int NOTE = 2;
	public static final int INFO = 1;
	private static final String[] reverse_seriousness = {
		"",
		"INFO",
		"NOTE",
		"CAUTION"
	};
	
	// 0: no sorting, 1: maintain original sort order, 2: auto-sort
	private int sortMode;
	public static final int NO_ORDER = 0;
	public static final int RETAIN_ORDER = 1;
	public static final int AUTO_ORDER = 2;
	
	// automatically recognize arrays by consecutive integers starting at 0
	private boolean printArrays;
	
	// print degug state-change information
	private boolean printDebug;

	// allow for unescaped embedded quotes by instead using [ non-space_and_non-ctrl .. comma_or_ctrl ] to deliniate string values
	private boolean unescapedQuotes;
	
	// normal constructors
	
	public JSON ( int sortMode, int leniency, boolean printArrays, boolean printDebug, boolean unescapedQuotes ) {
		this.leniency = leniency;
		this.sortMode = sortMode;
		this.printArrays = printArrays;
		this.printDebug = printDebug;
		this.unescapedQuotes = unescapedQuotes;
	}
	
	public JSON ( int sortMode, int leniency ) {
		this( sortMode, leniency, true, false, false );
	}
	
	public JSON ( int sortMode ) {
		this( sortMode, RELAXED, true, false, false );
	}
	
	public JSON () {
		this( RETAIN_ORDER, RELAXED, true, false, false );
	}

	public Tree create () {
		return new JSON( sortMode, leniency, printArrays, printDebug, unescapedQuotes );
	}
	
	// constructors that can throw Exception

	public JSON ( String serial, int sortMode, int leniency ) throws Exception {
		this( sortMode, leniency, true, false, false );
		deserialize( serial );
	}
	
	public JSON ( String serial, int sortMode ) throws Exception {
		this( serial, sortMode, RELAXED );
	}
	
	public JSON ( String serial ) throws Exception {
		this( serial, RETAIN_ORDER, RELAXED );
	}
	
	////////// map( ) //////////
	
	public Map<String,Tree> map () {
		if (map==null) {
			if      (sortMode == 0) map = new HashMap<>();
			else if (sortMode == 1) map = new LinkedHashMap<>();
			else                    map = new TreeMap<>();
		}
		return map;
	}

	////////// deserialize( ) //////////
	
	private boolean currentMode () {
		return modes.get(modes.size()-1).booleanValue();
	}
		
	private Tree currentBranch () {
		if (keys.size()<1) return this;
		return auto(keys.subList(1,keys.size()));
	}
	
	private void throwException ( int seriousness, String message ) throws Exception {
		if (seriousness > leniency) {
			if (printDebug) System.err.println( serialize() );
			System.err.println( stackState() );
			System.err.println( surroundingText( 100 ) );
			throw new Exception( reverse_seriousness[seriousness]+": "+message );
		}
	}
	
	private String charLocation () {
		return "line "+lineCount+" character "+charCount+" (index "+absoluteCount+" of length "+currentSerial.length()+")";
	}
	
	private String stackState () {
		return "keys="+keys+" modes="+modes+" key="+keyUnderConstruction+" val="+valueUnderConstruction+" comma="+trailingComma;
	}
	
	private String currentSubString ( int start, int end ) {
		if (end > currentSerial.length()) end = currentSerial.length();
		if (start > end-1) start = end-1;
		if (start >= 0) return currentSerial.substring( start, end );
		return "";
	}
	
	private String surroundingText ( int length ) throws Exception {
		return
			currentSubString( absoluteCount-length, absoluteCount )+
			">>>"+currentSubString( absoluteCount, absoluteCount+1 )+"<<<"+
			currentSubString( absoluteCount+1, currentSerial.length() )
		;
	}

	private boolean isWord ( char c ) {
		return (c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || c=='.';
	}
	
	private boolean isSpace ( char c ) {
		return (c==' ' || c=='\t' || c=='\r' || c=='\n');
	}
	
	private boolean isComma ( char c ) {
		return (c==',');
	}
	
	private void pushObjectKey () {
		if (keyUnderConstruction == null) keys.add( null );
		else {
			keys.add( keyUnderConstruction.toString() );
			keyUnderConstruction = null;
		}
	}
	
	private void pushArrayKey () {
		keys.add( currentBranch().integerKey() );
		keyUnderConstruction = null;
	}
	
	private void popKey () throws Exception {
		if (trailingComma) throwException( INFO, "trailing comma inferred before "+charLocation() );
		if (keys.size()==0) {
			throwException( CAUTION, "found more closings ('}' or ']') than openings ('{' or '[') after "+charLocation() );
			return;
		}
		keys.remove( keys.size()-1 );
		modes.remove( modes.size()-1 );
	}
		
	private void newValue () {
		if (currentMode()==OBJECT_MODE) currentBranch().add( keyUnderConstruction.toString(), valueUnderConstruction.toString() );
		else currentBranch().add( valueUnderConstruction.toString() );
		
		valueUnderConstruction = null;
		keyUnderConstruction = null;
	}
	
	private void initKey () {
		keyUnderConstruction = new StringBuilder();
	}
	
	private void initValue () {
		valueUnderConstruction = new StringBuilder();
	}
	
	// FSM

	public Tree deserialize ( String serial ) throws Exception {
		currentSerial = serial;
		charCount = 1;
		lineCount = 1;
		absoluteCount = 0;
		int state = VALUE;
		//currentMode( OBJECT_MODE );
		
		for (Character c : serial.toCharArray()) {
		
			if (printDebug) System.err.print( c+": "+reverse_state[state]+" -> " );

			if (state == FIND_KEY) {
				if (c == '"') {
					initKey();
					state = KEY;
				} else if (isWord(c)) {
					initKey();
					state = KEY_UNQUOTED;
					keyUnderConstruction.append(c);
					throwException( INFO, "original JSON spec does not allow unquoted keys ("+charLocation()+")" );
				} else if (c == '}') {
					popKey();
					if (modes.size()>0 && currentMode()==ARRAY_MODE) state = VALUE;
				} else if (c == ',') {
					trailingComma = true;
				} else if (c == '/') {
					state = FIND_KEY_COMMENT;
				} else if (!isSpace(c)) {
					throwException( CAUTION, "found non-space character '"+c+"' at "+charLocation() );
				}
				
			} else if (state == FIND_KEY_COMMENT) {
				if (c == '\n') {
					state = FIND_KEY;
				}

			} else if (state == KEY) {
				if (c == '"') {
					state = KEY_DELIM;
				} else {
					keyUnderConstruction.append(c);
				}

			} else if (state == KEY_UNQUOTED) {
				if (c == ':') {
					state = VALUE;
				} else if (c == ' ' || !isWord(c)) {
					state = KEY_DELIM;
				} else {
					keyUnderConstruction.append(c);
				}

			} else if (state == KEY_DELIM) {
				if (c == ':') {
					state = VALUE;
				} else if (!isSpace(c)) {
					throwException( CAUTION, "found non-space character '"+c+"' before colon at "+charLocation() );
				}

			} else if (state == VALUE) {
				if (c == '{') {
					trailingComma = false;
					if (modes.size()>0 && currentMode()==ARRAY_MODE) pushArrayKey();
					else pushObjectKey();
					modes.add( OBJECT_MODE );
					keyUnderConstruction = null;
					state = FIND_KEY;
				} else if (c == '[') {
					trailingComma = false;
					if (modes.size()==0) throwException( INFO, "original JSON spec does not allow arrays at the root level ("+charLocation()+")" );
					if (modes.size()>0 && currentMode()==OBJECT_MODE) pushObjectKey();
					else  pushArrayKey();
					modes.add( ARRAY_MODE );
					keyUnderConstruction = null;
					//keyUnderConstruction = new StringBuilder( currentBranch().integerKey() );
					// no initKey for arrays
				} else if (c == '"') {
					if (modes.size()==0) throwException( INFO, "original JSON spec does not allow key instantiation at the root level ("+charLocation()+")" );
					initValue();
					state = STRING;
				} else if (isWord(c)) {
					initValue();
					valueUnderConstruction.append(c);
					state = STRING_NONQUOTE;
				} else if (c == ']') {
					popKey();
					if (modes.size()>0 && currentMode()) state = FIND_KEY; // object mode
				} else if (c == ',') {
					trailingComma = true;
				} else if (c == '/') {
					state = VALUE_COMMENT;
				} else if (!isSpace(c)) {
					throwException( CAUTION, "found non-space character '"+c+"' at "+charLocation() );
				}

			} else if (state == VALUE_COMMENT) {
				if (c == '\n') {
					state = VALUE;
				}

			} else if (state == STRING) {
				if (c == '\\') {
					state = STRING_ESCAPE;
				} else if (c == '"') {
					if (unescapedQuotes) {
						state = CHECK_FOR_UNESCAPED_QUOTE;
					} else {
						trailingComma = false;
						newValue();
						if (currentMode()==OBJECT_MODE) state = FIND_KEY;
						else state = VALUE; // array mode
					}
				} else {
					valueUnderConstruction.append(c);
				}

			} else if (state == STRING_NONQUOTE) {
				if (c == '\\') {
					state = STRING_ESCAPE;
				} else if (isSpace(c) || isComma(c)) {
					newValue();
					if (currentMode()==OBJECT_MODE) state = FIND_KEY;
					else state = VALUE; // array mode
				/*} else if (! isWord(c)) {
					throwException( "found '"+c+"' in non-quoted value ("+valueUnderConstruction+") to be added to "+objectHierarchy()+" at "+charLocation() );
				*/
				} else if (c == ']' || c == '}') {
					newValue();
					popKey();
					if (currentMode()==OBJECT_MODE) state = FIND_KEY;
					else state = VALUE; // array mode
				} else {
					valueUnderConstruction.append(c);
				}

			} else if (state == STRING_ESCAPE) {
				valueUnderConstruction.append(c);
				state = STRING;
			} else if (state == CHECK_FOR_UNESCAPED_QUOTE) {
				if (c == ',' || c == '\n' || c == '\r' || c == '}' || c == ']') {
					if (c != ',') trailingComma = false;
					newValue();
					if (currentMode()==OBJECT_MODE) state = FIND_KEY;
					else state = VALUE; // array mode
				} else if (c == '\\') {
					valueUnderConstruction.append('"');
					state = STRING_ESCAPE;
				} else {
					valueUnderConstruction.append('"');
					valueUnderConstruction.append(c);
					state = STRING;
				}
			}
			
			if (printDebug) System.err.println( reverse_state[state]+" "+stackState() );
			absoluteCount++;
			charCount++;
			if (c=='\n') {
				lineCount++;
				charCount=0;
			}
		}
		
		if (keys.size()!=0) {
			throwException( NOTE, "found more openings ('{' or '[') than closings ('}' or ']') after end of stream at "+charLocation() );
		}
		currentSerial = null;
		return this;
	}
	
	
	////////// serialize() //////////
	
	private void indent ( StringBuilder sb, int length ) {
		for (int i=0; i<length; i++) sb.append( "\t" );
	}
	
	private void serialize ( Tree branch, StringBuilder json, int i ) {
		boolean isArray = (branch.integerKeys() && printArrays);
		if (isArray) json.append("[");
		else json.append("{");
		String comma = "";
		for (Map.Entry<String,Tree> entry : branch.map().entrySet()) {
			json.append(comma).append("\n");
			indent( json, i+1 );
			if (! isArray) json.append("\"").append( entry.getKey() ).append("\": ");
			Tree subBranch = entry.getValue();
			if (subBranch.size()==0) {
				String value = subBranch.value();
				if (value==null) {
					json.append("null");
				} else if (value.equals("")) {
					json.append("\"\"");
				} else if (value.equals("true") || value.equals("false") || !Regex.exists( value, "[^\\d]" )) {
					json.append( value );
				} else {
					json.append("\"").append(
						value.replace( "\\", "\\\\" ).replace( "\"", "\\\"" )
					).append("\"");
				}
			} else {
				serialize( subBranch, json, i+1 );
			}
			comma = ",";
		}
		json.append("\n");
		indent( json, i );
		if (isArray) json.append("]");
		else json.append("}");
	}
	
	public String serialize () {
		StringBuilder json = new StringBuilder();
		serialize( this, json, 0 );
		return json.toString();
	}
	
}
	
	
class ExecCleanJSON {
	
	public static void main ( String[] args ) throws Exception {
		int leniency = ( args.length>1 ? Integer.parseInt(args[1]) : 3 );
		int sortMode = ( args.length>2 ? Integer.valueOf(args[2]) : 2 );
		boolean printArrays = ( args.length>3 ? Boolean.valueOf(args[3]) : true );
		boolean printDebug = ( args.length>4 ? Boolean.valueOf(args[4]) : false );
		boolean unescapedQuotes = ( args.length>5 ? Boolean.valueOf(args[5]) : false );
		Tree json = new JSON( leniency, sortMode, printArrays, printDebug, unescapedQuotes );
		json.deserialize(
			FileActions.read( args[0] )
		);
		System.out.println(
			json.serialize()
		);
	}

}
