package creek;

public interface SortTable extends Table {

	public SortTable sort ( int column );
	public SortTable sortReverse ( int column );

	public SortTable last ( int column, String thisAndFollowing );
	
	public SortTable slice ( int column, String thisAndFollowing, String approachingThisLimit );
	
}
