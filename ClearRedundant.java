package creek;

import java.util.*;

public class ClearRedundant extends SimpleTable {

	private ClearRedundant () {} // prohibit blank instantiation
	
	public ClearRedundant ( Table table ) {
		this( table, "" );
	}

	public ClearRedundant ( Table table, String blank ) {
		super( table );
		
		// loop through items
		for (int row=0; row<rowCount(); row++) {
			for (int col=0; col<colCount(row); col++) {
				String topItem = item(row,col);
				
				// if an item is not blank, then blank downward while items are the same as topItem
				if (topItem != null && ! topItem.equals(blank)) {
					for (int area=row+1; area<rowCount(); area++) {
						try {
							if (topItem.equals(item(area,col))) data().get(area).set(col, blank);
							else break;
						} catch (Exception e) {
							break;
						}
					}
				}
				
			}
		}
		
							
	}
	
	public static void main ( String[] args ) {
		Table st = (new SimpleTable()).append(
			"a  b  c  d  \n"+
			"a  b0 c  d0 \n"+
			"A  b0 c  d1 \n"+
			"A  b0 C  d1 \n"	
		);
		System.out.println( st );
		ClearRedundant cr = new ClearRedundant( st, "-" );
		System.out.println( cr );
	}
	
}
