package creek;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;

public class MergeHTML {

	private File file;

	private StringBuilder head;
	private StringBuilder body;
	private String bodyExtras = "";
	private StringBuilder style;
	private StringBuilder script;
	
	
	public MergeHTML () {
		this.head = new StringBuilder();
		this.body = new StringBuilder();
		this.style = new StringBuilder();
		this.script = new StringBuilder();
	}
	
	public MergeHTML ( String head, String body, String style, String script ) {
		this();
		this.head.append(head);
		this.body.append(body);
		this.style.append(style);
		this.script.append(script);
	}

	public MergeHTML ( File file ) throws Exception {
		this();
		this.file = file;
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
	
	
	
	public MergeHTML merge () throws Exception {
		return merge( file );
	}
	
	private String extractTag ( StringBuilder category, String type, String wholeFile ) throws Exception {
		// tag data
		List<String> tagDataArray = Regex.groups( wholeFile, "<"+type+">([\\s\\S]+?)</"+type+">" );
		String tagData = String.join( "\n", tagDataArray ); 
		category.append( tagData );
		
		// source files
		List<String> srcList = Regex.groups( wholeFile, "<"+type+" +?src=['\"]([^'\"]+)['\"]>.*?</"+type+">" );
		if (srcList.size()>0) System.out.println( "<"+type+"> source files in "+file.getPath()+": "+srcList );
		for (String src : srcList) {
			merge( new File( file.getParentFile(), src ) );
		}
		
		return wholeFile.replaceAll( "<"+type+"[\\s\\S]+?"+type+">", "" );
	}
	
	public MergeHTML merge ( File file ) throws Exception {
		String wholeFile = FileActions.read( file );
		String ext = FileActions.extension(file);
		
		wholeFile = wholeFile.replaceAll( "<!--[\\s\\S]+?-->", "" );
		
		if (ext.equals("htm") || FileActions.extension(file).equals("html")) {
			wholeFile = extractTag( style, "style", wholeFile );
			wholeFile = extractTag( script, "script", wholeFile );
			
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
	
	public MergeHTML merge ( MergeHTML html ) {
		if (html.body().length()>0) body.append( "\n<!-- from " ).append( html.path() ).append( " -->\n" ).append( html.body() );
		if (html.script().length()>0) script.append( "\n// from " ).append( html.path() ).append( ":\n" ).append( html.script() );
		return this;
	}
	
	public static void merge ( String[] paths ) throws Exception {
		MergeHTML html = new MergeHTML( new File(paths[0]) ).merge();
		for (int i=1; i<paths.length; i++) {
			html.merge( new MergeHTML( new File(paths[i]) ) );
		}
		FileActions.write( FileActions.addSuffix( paths[0], "-merged" ), html.toString(), "UTF-8", false );
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
		merge( args );
	}

}
