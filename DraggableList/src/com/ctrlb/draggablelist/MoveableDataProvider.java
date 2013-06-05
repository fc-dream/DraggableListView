package com.ctrlb.draggablelist;


/**
 * Interface used to allow the data to be moved in the underlying data set.
 * @author phil
 *
 */

public interface MoveableDataProvider extends GenericDataProvider {
    
    /**
     * This method will be invoked to move the data.
     * @param from position in the data set the data is moved from
     * @param to position in the data set the data is moved to
     */
    public void move(int from, int to);

}
