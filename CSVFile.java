package creek;

import java.io.*;
import java.util.concurrent.atomic.*;

public class CSVFile extends CSV {

	private boolean initialized = false;
	private File file;
	private AtomicBoolean writeLock;
	
	public CSVFile ( String path ) {
		this( new File(path) );
	}
	
	public CSV ( File file ) {
		this( file, ",", "\\" );
	}

	public CSV ( File file, String comma, String escape ) {
		super( comma, escape );
		initialized = false;
		this.file = file;
		writeLock = new AtomicBoolean(false);
		input(
			new String(
				Files.readAllBytes( file.toPath(), Charset.defaultCharset() )
			)
		);
		initialized = true;
	}
	
	public void addRow () {
		if (initialized) {
			while(true) {
				if ( writeLock.compareAndSet( false, true ) ) break;
				Thread.sleep(1);
			}
			try {
				Files.write(file.toPath(), lastRow().getBytes(), StandardOpenOption.APPEND);
				super.addRow();
			} catch (Exception e) {
				addRowException();
			}
		} else {
			super.addRow();
		}
	}
	
	public void addRowException () {
		e.printStackTrace();
	}
	
}
