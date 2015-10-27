/**
 * 
 */
package uk.bl.wa.w3act.forms;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author andy
 *
 */
public class SearchForm {
    
    public enum SearchSort {
	    BY_NAME(1),
	    OLDEST_FIRST(2),
	    NEWEST_FIRST(3);

	    public final int id;

	    SearchSort(int id) {
	      this.id = id;
	    }
	    
	    public String getId() {
		return ""+id;
	    }

	    public static Map<String, String> options(){
	        LinkedHashMap<String, String> vals = new LinkedHashMap<String, String>();
	        for (SearchSort cType: SearchSort.values()) {
	            vals.put(cType.getId(), cType.name());
	        }
	        return vals;
	    }
    }
    
    private Long collectionId;
    private String filter;
    
    public Long getCollectionId() {
        return collectionId;
    }
    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }
    public String getFilter() {
        return filter;
    }
    public void setFilter(String filter) {
        this.filter = filter;
    }

}
