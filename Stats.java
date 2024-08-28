package creek;

import java.util.*;

public class Stats {

	public static void displayMemory () {
		Runtime rt = Runtime.getRuntime();
		long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
		System.out.println( "Memory: "+usedMB+"MB" );
	}

}
