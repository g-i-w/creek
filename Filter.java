package creek;

import java.util.*;

public interface Filter extends Table {

	public Filter first ( int rows );
	public Filter first ( int column, String lastVal );
	
	public Filter last ( int column, String firstVal );

	public Filter range ( int column, String minVal, String maxVal );
	public String min ( int column );
	public String max ( int column );
	
	public Filter set ( int column, Set<String> valSet );
	
	public Filter search ( int column, String partialVal );
}
