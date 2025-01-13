package creek;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.function.Function;

public class HTML {
	
	public static String embedTag ( String path, String input, String type, String mime ) throws Exception {
		return Regex.replace(
			input,
			"<"+type+" +?src=['\"]([^'\"]+)['\"]>.*?</"+type+">",
			null,
			new Function<String,String> () {
				public String apply ( String embedPath ) {
					// converts src="some/path" to data URL src="data: ___ "
					try {
						System.out.println( "embedding file '"+embedPath+"'..." );
						String embeddedFile = FileActions.read( new File( new File(path).getParentFile(), embedPath ) );
						return "<"+type+" src=\"data:"+mime+";base64,"+Base64.getEncoder().encodeToString( embeddedFile.getBytes() )+"\"></"+type+">";
					} catch (Exception e) {
						e.printStackTrace();
						return "";
					}
				}
			}
		);
	}
	
	public static void embed ( String inputPath, String outputPath ) throws Exception {
		String text = FileActions.read( inputPath );
		text = embedTag( inputPath, text, "script", "application/javascript" );
		text = embedTag( inputPath, text, "style", "text/css" );
		FileActions.write( new File(outputPath), text, "UTF-8", false );		
	}


	public static void main ( String[] args ) throws Exception {
		embed( args[0], args[1] );
	}

}
