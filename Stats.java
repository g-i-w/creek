package creek;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Stats {

	private static AtomicInteger serial = new AtomicInteger();
	
	public static long time () {
		return System.nanoTime();
	}
	
	public static String ms ( long time ) {
		return String.valueOf( (double)time/1000000.0 )+"[ms]";
	}
	
	public static long usedMemory () {
		Runtime rt = Runtime.getRuntime();
		return (rt.totalMemory() - rt.freeMemory());
	}
	
	public static String kiB ( long mem ) {
		return String.valueOf( mem / 1024 )+"[kiB]";
	}
	
	public static void displayMemory () {
		System.err.println( "Memory: "+kiB( usedMemory() ) );
	}
	
	private long initialTime;
	private long mark;
	
	private long initialMemory;
	private long markMemory;
	
	private String name;
	private int nukiBer;
	
	public Stats ( String name ) {
		initialTime = time();
		initialMemory = usedMemory();
		mark = initialTime;
		markMemory = initialMemory;
		nukiBer = serial.getAndIncrement();
		this.name = name;
	}
	
	public Stats () {
		this( null );
	}
	
	public String deltaTime () {
		long now = time();
		long delta = now - mark;
		mark = now;
		return ms( delta );
	}
	
	public String totalTime () {
		long now = time();
		long total = now - initialTime;
		return ms( total );
	}
	
	public String deltaMemory () {
		long now = usedMemory();
		long delta = now - markMemory;
		markMemory = now;
		return kiB( delta );
	}

	public String totalMemory () {
		long now = usedMemory();
		long total = now - initialMemory;
		return kiB( total );	
	}

	public void display ( String message ) {
		if (message!=null) System.err.println( this+" *** "+message+" *** " );
		else  System.err.println( this );
	}
	
	public void display () {
		display( null );
	}
	
	public String toString () {
		return this.getClass().getName()+"-"+nukiBer+( name!=null ? " '"+name+"' | " : " | " )+totalTime()+", delta:"+deltaTime()+" | "+totalMemory()+", delta:"+deltaMemory();
	}

}
