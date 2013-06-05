package com.ctrlb.draggablelist;

import java.util.HashMap;

/**
 * Interface used to allow the data to be returned as a {@link HashMap}.
 * @author philip brown
 *
 */

public interface GenericDataProvider {
    
    /**
     * 
     * @param position
     * @return the data as a HashMap at the given position
     */
    public HashMap<String,String> getItem(int position);  
    
    /**
     * 
     * @return the number of items in the data set
     */
    public int getCount();
    
    /**
     *
     * @param position
     * @return the id (primary key) for the data at the given position
     */
    
    public long getItemId(int position);

}
