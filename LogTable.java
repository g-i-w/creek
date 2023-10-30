package creek;

import java.time.ZonedDateTime;

public interface LogTable extends SortTable {

	public LogTable last ( ZonedDateTime time );
	
	public LogTable slice ( ZonedDateTime start, ZonedDateTime end );
	
}
