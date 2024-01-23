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
		"STRING_ESCAPE"
	};

	// used by FSM
	private static final boolean OBJECT_MODE = true;
	private static final boolean ARRAY_MODE = false;
	private List<Boolean> modes = new ArrayList<Boolean>();
	private List<String> keys = new ArrayList<>();
	private StringBuilder keyUnderConstruction = null;
	private StringBuilder valueUnderConstruction = null;
	private int charCount;
	private int lineCount;
	private boolean printDebug;
	private boolean trailingComma = false;

	// Leniency:
	private int leniency;
	// 3: as relaxed as possible, 2: just the essentials, 1: very rigorous, 0: overly pedantic
	private static final int RELAXED = 3;
	private static final int CAREFUL = 2;
	private static final int RIGOROUS = 1;
	private static final int PEDANTIC = 0;
	
	// Seriousness:
	// 3: caution label, 2: important note, 1: just FYI
	private static final int CAUTION = 3;
	private static final int NOTE = 2;
	private static final int INFO = 1;
	private static final String[] reverse_seriousness = {
		"",
		"INFO",
		"NOTE",
		"CAUTION"
	};
	
	// normal constructors
	
	public JSON ( int leniency, boolean printDebug ) {
		this.leniency = leniency;
		this.printDebug = printDebug;
	}
	
	public JSON () {
		this( RELAXED, false );
	}

	public Tree create () {
		return new JSON();
	}
	
	// constructors that can throw Exception

	public JSON ( String serial, int leniency, boolean printDebug ) throws Exception {
		this( leniency, printDebug );
		deserialize( serial );
	}
	
	public JSON ( String serial ) throws Exception {
		this( serial, RELAXED, false );
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
			throw new Exception( reverse_seriousness[seriousness]+": "+message );
		}
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
	
	private String charLocation () {
		return "line "+lineCount+" character "+charCount;
	}
	
	// FSM

	public Tree deserialize ( String serial ) throws Exception {
		charCount = 1;
		lineCount = 1;
		int state = VALUE;
		//currentMode( OBJECT_MODE );
		
		for (Character c : serial.toCharArray()) {
			if (printDebug) System.out.print( c+": "+reverse_state[state]+" -> " );

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
					trailingComma = false;
					newValue();
					if (currentMode()==OBJECT_MODE) state = FIND_KEY;
					else state = VALUE; // array mode
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
			}
			
			if (printDebug) System.out.println( reverse_state[state]+" str="+this+" keys="+keys+" modes="+modes+" key="+keyUnderConstruction+" val="+valueUnderConstruction+" comma="+trailingComma );
			charCount++;
			if (c=='\n') {
				lineCount++;
				charCount=0;
			}
		}
		
		if (keys.size()!=0) {
			throwException( NOTE, "found more openings ('{' or '[') than closings ('}' or ']') after end of stream at "+charLocation() );
		}
		return this;
	}
	
	
	////////// serialize() //////////
	
	private void indent ( StringBuilder sb, int length ) {
		for (int i=0; i<length; i++) sb.append( "\t" );
	}
	
	private void serialize ( Tree branch, StringBuilder json, int i ) {
		boolean integerKeys = branch.integerKeys();
		if (integerKeys) json.append("[");
		else json.append("{");
		String comma = "";
		for (Map.Entry<String,Tree> entry : branch.map().entrySet()) {
			json.append(comma).append("\n");
			indent( json, i+1 );
			if (! integerKeys) json.append("\"").append( entry.getKey() ).append("\": ");
			Tree subBranch = entry.getValue();
			if (subBranch.size()==0) {
				String value = subBranch.value().replace( "\\", "\\\\" ).replace( "\"", "\\\"" );
				if (value==null) json.append("null");
				else if (value.equals("true") || value.equals("false") || !Regex.exists( value, "[^\\d\\.]" )) json.append( value );
				else json.append("\"").append( value ).append("\"");
			} else {
				serialize( subBranch, json, i+1 );
			}
			comma = ",";
		}
		json.append("\n");
		indent( json, i );
		if (integerKeys) json.append("]");
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
		Tree json = new JSON(
			FileActions.read(args[0]),
			( args.length>1 ? Integer.parseInt(args[1]) : 3 ),
			( args.length>2 ? Boolean.valueOf(args[2]) : false )
		);
		System.out.println(
			json.serialize()
		);
	}

}
