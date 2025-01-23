package creek;

import java.util.*;
import java.time.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

public interface FileTree extends Tree {

	// file/dir/sub-file object
	
	public File file ();
	
	public void toDirectory ();
	
	default File file ( String subFile ) {
		toDirectory();
		return new File( file(), subFile );
	}
	
	// timestamp from file
	
	default FileTime fileTime () throws Exception {
		return Files.getLastModifiedTime( file().toPath() );
	}
	
	default LocalDateTime localDateTime () throws Exception {
		return LocalDateTime.ofInstant( fileTime().toInstant(), ZoneId.systemDefault() );
	}
	
	default LocalDate localDate () throws Exception {
		return LocalDate.ofInstant( fileTime().toInstant(), ZoneId.systemDefault() );
	}
	
	// timestamp from sub-files
	
	default FileTime fileTime ( String subFile ) throws Exception {
		return Files.getLastModifiedTime( file( subFile ).toPath() );
	}
	
	default LocalDateTime localDateTime ( String subFile ) throws Exception {
		return LocalDateTime.ofInstant( fileTime( subFile ).toInstant(), ZoneId.systemDefault() );
	}
	
	default LocalDate localDate ( String subFile ) throws Exception {
		return LocalDate.ofInstant( fileTime( subFile ).toInstant(), ZoneId.systemDefault() );
	}
	
	// read/write byte[] data
	
	default byte[] read ( String subFile ) throws Exception {
		return FileActions.readBytes( file( subFile ) );
	}
	
	default void write ( String subFile, byte[] data ) throws Exception {
		FileActions.write( file( subFile ), data, false );
	}
	
	default void append ( String subFile, byte[] data ) throws Exception {
		FileActions.write( file( subFile ), data, true );
	}
	
}
