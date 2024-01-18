package creek;

public class JSON extends AbstractTree {

	// states
	private static final int OBJECT_ARRAY_KEY = 0;
	private static final int OBJECT = 1;
	private static final int KEY = 2;
	private static final int OBJECT_ARRAY_VALUE = 3;
	private static final int VALUE = 4;
	private static final int VALUE_ESCAPE = 5;
	private static final int ARRAY = 6;
	private static final int ARRAY_VALUE = 7;
	private static final int ARRAY_VALUE_ESCAPE = 8;
	
	/*private static final String[] reverse_state = {
		"LINE_START_STATE","VALUE_STATE","SPACE_STATE","ESCAPE_STATE","LINE_END_STATE","QUOTE_VALUE_STATE","QUOTE_END_STATE"
	};*/


	public Tree create ( String key ) {
		add( key, new JSON() );
	}
	
	public Tree deserialize ( String serial ) {
		List<String> objects = new ArrayList<>();
		StringBuilder currentKey = null;
		StringBuilder currentValue = null;
		int state = OBJECT_ARRAY_KEY;
		
		for (Character c : serial.toCharArray()) {
		
			if (state == OBJECT_ARRAY_KEY) {
				if (c == '{') {
					state = OBJECT;
					objects.add( currentKey.toString() );
				} else if (c == '[') {
					state = ARRAY;
				} else if (c == '"') {
					state = KEY;
				}

			} else if (state == OBJECT) {
				if (c == '"') {
					state = KEY;
					currentKey = new StringBuilder();
				} else if (c == '}') {
					state = OBJECT_ARRAY_KEY;
					objects.remove( objects.size()-1 );
				}
			} else if (state == KEY) {
				if (c == '"') {
					state = OBJECT_ARRAY_VALUE;
				} else {
					currentKey.append(c);
				}

			} else if (state == OBJECT_ARRAY_VALUE) {
				if (c == '{') {
					state = OBJECT;
					objects.add( currentKey.toString() );
					currentKey = null;
				} else if (c == '[') {
					state = ARRAY;
				} else if (c == '"') {
					state = VALUE;
					currentValue = new StringBuilder();
				}

			} else if (state == VALUE) {
				if (c == '\\') {
					state = VALUE_ESCAPE;
				} else if (c == '"') {
					state = OBJECT;
					auto( objects ).add( currentKey.toString(), currentValue.toString() );
				} else {
					currentValue.append(c);
				}

			} else if (state == VALUE_ESCAPE) {
				currentValue.append(c);
				state = VALUE;
				
			} else if (state == ARRAY) {
				if (c == '"') {
					state = ARRAY_VALUE;
					currentValue = new StringBuilder();
				}
				
			} else if (state == ARRAY_VALUE) {
				if (c == '\\') {
					state = ARRAY_VALUE_ESCAPE;
				} else if (c == '"') {
					state = ARRAY;
					auto( objects ).add( currentValue.toString() );
				} else {
					currentKey.append(c);
				}
			} else if (state == ARRAY_VALUE_ESCAPE) {
				currentKey.append(c);
				state = ARRAY_VALUE;
			}
		}
		
		return this;
	}

}
