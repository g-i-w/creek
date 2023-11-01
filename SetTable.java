package creek;

public interface SetTable extends Table {

	public SetTable set ( int column );
	public SetTable setReverse ( int column );

	public SetTable last ( int column, String thisAndFollowing );
	
	public SetTable slice ( int column, String thisAndFollowing, String approachingThisLimit );
	
}
