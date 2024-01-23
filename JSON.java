package creek;

import java.util.*;

public class JSON extends AbstractTree {

	// states
	private static final int INIT = 0;
	private static final int FIND_KEY = 1;
	private static final int KEY = 2;
	private static final int KEY_DELIM = 3;
	private static final int VALUE = 4;
	private static final int STRING = 5;
	private static final int STRING_NONQUOTE = 6;
	private static final int STRING_ESCAPE = 7;
	
	private static final String[] reverse_state = {
		"INIT",
		"FIND_KEY",
		"KEY",
		"KEY_DELIM",
		"VALUE",
		"STRING",
		"STRING_NONQUOTE",
		"STRING_ESCAPE"
	};
	
	private static final boolean OBJECT_MODE = true;
	private static final boolean ARRAY_MODE = false;


	private List<String> keys = new ArrayList<>();
	private List<Boolean> modes = new ArrayList<Boolean>();
	private StringBuilder keyUnderConstruction = null;
	private StringBuilder valueUnderConstruction = null;
	private int charCount;
	private int lineCount;
	private int strictness; // 3: as relaxed as possible, 2: just essentials, 1: rigorous, 0: overly pedantic
	private boolean printDebug;
	private boolean trailingComma = false;

	
	public JSON ( int strictness, boolean printDebug ) {
		this.strictness = strictness;
		this.printDebug = printDebug;
	}
	
	public JSON ( String serial ) throws Exception {
		this( 3, false );
		deserialize( serial );
	}

	public JSON () {
		this( 3, false );
	}

	public Tree create () {
		return new JSON();
	}
	
	
	private boolean currentMode () {
		return modes.get(modes.size()-1).booleanValue();
	}
		
	private void currentMode ( boolean mode ) {
		modes.add( mode );
	}
	
		
	private Tree currentBranch () {
		if (keys.size()<1) return this;
		return auto(keys.subList(1,keys.size()));
	}
	

	private void throwException ( int seriousness, String message ) throws Exception {
		if (seriousness > strictness) {
			if (printDebug) System.out.println( serialize() );
			throw new Exception( message );
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
		//if (keys.size()==0) return;
		//if (keys.size()==0) throwException( "more closing '}' than opening '{' at "+charLocation() );
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
	
	private String objectHierarchy () {
		return "\""+String.join(".",keys)+(keyUnderConstruction==null ? "" : "."+keyUnderConstruction)+"\"";
	}
	
	private String charLocation () {
		return "line "+lineCount+" character "+charCount;
	}

	public Tree deserialize ( String serial ) throws Exception {
		charCount = 0;
		lineCount = 1;
		int state = VALUE;
		//currentMode( OBJECT_MODE );
		
		for (Character c : serial.toCharArray()) {
			if (printDebug) System.out.print( c+": "+reverse_state[state]+" -> " );

			if (state == FIND_KEY) {
				if (c == '"') {
					initKey();
					state = KEY;
				} else if (c == '}') {
					popKey();
					if (modes.size()>0 && currentMode()==ARRAY_MODE) state = VALUE;
				}
				
			} else if (state == KEY) {
				if (c == '"') {
					state = KEY_DELIM;
				} else {
					keyUnderConstruction.append(c);
				}

			} else if (state == KEY_DELIM) {
				if (c == ':') {
					state = VALUE;
				} else if (c != ' ') {
					throwException( 3, "non-space character '"+c+"' found before colon at "+charLocation() );
				}

			} else if (state == VALUE) {
				if (c == '{') {
					if (modes.size()>0 && currentMode()==ARRAY_MODE) pushArrayKey();
					else pushObjectKey();
					modes.add( OBJECT_MODE );
					keyUnderConstruction = null;
					state = FIND_KEY;
				} else if (c == '[') {
					if (modes.size()==0) throwException( 1, "JSON spec does not allow arrays at the root level ("+charLocation()+")" );
					if (modes.size()>0 && currentMode()==OBJECT_MODE) pushObjectKey();
					else  pushArrayKey();
					modes.add( ARRAY_MODE );
					keyUnderConstruction = null;
					//keyUnderConstruction = new StringBuilder( currentBranch().integerKey() );
					// no initKey for arrays
				} else if (c == '"') {
					if (modes.size()==0) throwException( 1, "JSON spec does not allow key instantiation at the root level ("+charLocation()+")" );
					initValue();
					state = STRING;
				} else if (isWord(c)) {
					initValue();
					valueUnderConstruction.append(c);
					state = STRING_NONQUOTE;
				} else if (c == ']') {
					popKey();
					if (modes.size()>0 && currentMode()) state = FIND_KEY; // object mode
				}

			} else if (state == STRING) {
				if (c == '\\') {
					state = STRING_ESCAPE;
				} else if (c == '"') {
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
			throwException( 2, "more opening '{' than closing '}' after end of stream at "+charLocation() );
		}
		return this;
	}
	
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
	
	public static void main ( String[] args ) throws Exception {
		Tree json = new JSON( Integer.parseInt(args[2]), ( args.length>1 ? Boolean.valueOf(args[1]) : false ) );
		
		String input = FileActions.read(args[0]);
		System.out.println( input );

		json.deserialize( input );
		System.out.println( json );
		
		String output = json.serialize();
		System.out.println( output );
	}

}
