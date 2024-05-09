package creek;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;

public class HTML {

	private File file;

	private StringBuilder head;
	private StringBuilder body;
	private String bodyExtras = "";
	private StringBuilder style;
	private StringBuilder script;
	
	
	public HTML () {
		this.head = new StringBuilder();
		this.body = new StringBuilder();
		this.style = new StringBuilder();
		this.script = new StringBuilder();
	}
	
	public HTML ( String head, String body, String style, String script ) {
		this();
		this.head.append(head);
		this.body.append(body);
		this.style.append(style);
		this.script.append(script);
	}

	public HTML ( File file ) throws Exception {
		this();
		this.file = file;
		append( file );
	}
	
	public File file () {
		return file;
	}
	
	public String path () {
		return file.getPath();
	}
	
	public String head () {
		return head.toString();
	}
	
	public String body () {
		return body.toString();
	}
	
	public String style () {
		return style.toString();
	}
	
	public String script () {
		return script.toString();
	}
	
	
	private String specialTag ( StringBuilder category, String type, String wholeFile ) throws Exception {
		// embedded
		String embed = String.join( "\n", Regex.groups( wholeFile, "<"+type+">([\\s\\S]+?)</"+type+">" ) );
		category.append( embed );
		
		// source files
		List<String> srcList = Regex.groups( wholeFile, "<"+type+" +?src=['\"]([^'\"]+)['\"]>.*?</"+type+">" );
		if (srcList.size()>0) System.out.println( "<"+type+"> source files in "+file.getPath()+": "+srcList );
		for (String src : srcList) {
			append( new File( file.getParentFile(), src ) );
		}
		
		return wholeFile.replaceAll( "<"+type+"[\\s\\S]+?"+type+">", "" );
	}
	
	public HTML append ( File file ) throws Exception {
		String wholeFile = FileActions.read( file );
		String ext = FileActions.extension(file);
		
		wholeFile = wholeFile.replaceAll( "<!--[\\s\\S]+?-->", "" );
		
		if (ext.equals("htm") || FileActions.extension(file).equals("html")) {
			wholeFile = specialTag( style, "style", wholeFile );
			wholeFile = specialTag( script, "script", wholeFile );
			
			head.append( String.join( "\n", Regex.groups( wholeFile, "<head[^>]*>([\\s\\S]+?)</head>" ) ) );
			bodyExtras += String.join( " ", Regex.groups( wholeFile, "<body([^>]*)>" ) );
			body.append( String.join( "\n", Regex.groups( wholeFile, "<body[^>]*>([\\s\\S]+?)</body>" ) ) );
		} else if (ext.equals("css")) {
			style.append( "\n" ).append( wholeFile );
		} else if (ext.equals("js")) {
			script.append( "\n" ).append( wholeFile );
		}
		return this;
	}
	
	public HTML append ( HTML html ) {
		if (html.body().length()>0) body.append( "\n<!-- from " ).append( html.path() ).append( " -->\n" ).append( html.body() );
		if (html.script().length()>0) script.append( "\n// from " ).append( html.path() ).append( ":\n" ).append( html.script() );
		return this;
	}
	
	public String toString () {
		return (new StringBuilder())
			.append( "<html>\n<head>\n" )
			.append( head )
			.append( "\n<style>\n" )
			.append( style )
			.append( "\n</style>\n</head>\n<body" ).append( bodyExtras ).append( ">\n" )
			.append( body )
			.append( "\n<script>\n" )
			.append( script )
			.append( "\n</script>\n</body>\n</html>\n" )
			.toString()
		;
	}
	
	
	public static void main ( String[] args ) throws Exception {
		HTML html = new HTML( new File(args[0]) );
		for (int i=1; i<args.length; i++) html.append( new HTML( new File(args[i]) ) );
		FileActions.write( FileActions.addSuffix( args[0], "-merged" ), html.toString(), "UTF-8", false );
	}

}
