/**
 * 
 */
package uk.bl.wa.w3act;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import play.Logger;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author andy
 *
 */
public class CollectionTree implements Serializable {
	
	private static final long serialVersionUID = 3152090496542715791L;
	
	public long id;
	public long parentId;
	public String title;
	public List<CollectionTree> children = new ArrayList<CollectionTree>();
	public boolean publish;
	public String description;
	public List<Target> targets = new ArrayList<Target>();

	protected CollectionTree() {
	}
	
	public CollectionTree( JsonNode json ) {
		this.id = -1;
		this.title = "ROOT";
		for( JsonNode item : json ) {
			children.add( new CollectionTree(item, this.id) );
		}
	}
	
	private CollectionTree( JsonNode json, long parent ) {
		this.parentId = parent;
		try {
		    this.id = Long.parseLong(json.get("key").textValue().replace("\"", ""));
		} catch( Exception e ) {
		    Logger.info("Old string-accessor failed for ID, trying Long...");
		}
		this.id = json.get("key").longValue();
		this.title = json.get("title").textValue();
		if( json.has("children")) {
			for( JsonNode item : json.get("children")) {
				this.children.add( new CollectionTree(item, this.id));
			}
		}
	}
	
	/**
	 * Recursively hunt for a particular point in the tree
	 * 
	 * @param id
	 * @return
	 */
	public CollectionTree find( long id ) {
	    if( this.id == id ) {
	    	return this;
	    } else {
	    	for( CollectionTree child : children ) {
	    		CollectionTree ctid = child.find(id);
	    		if( ctid != null ) {
	    			return ctid;
	    		}
	    	}
	    	return null;
	    }
	}
	
	/**
	 * Recursively hunt for a particular target
	 * 
	 * @param id
	 * @return
	 */
	public Target findTarget( long id ) {
	    for( Target t : this.targets ) {
		if( t.id == id ) {
		    return t;
		}
	    }
	    for( CollectionTree child : children ) {
		Target t = child.findTarget(id);
		if( t != null ) {
		    return t;
		}
	    }
	    return null;
	}
	
	public List<Long> getAllCollectionsIds() {
		return getAllCollectionsIdsRecursive( new ArrayList<Long>());
	}

	private List<Long> getAllCollectionsIdsRecursive(List<Long> list) {
		if( id != -1 )
			list.add(id);
    	for( CollectionTree child : children ) {
    		child.getAllCollectionsIdsRecursive(list);
    	}
    	return list;
	}

	public List<CollectionTree> getAllCollections() {
		return getAllCollectionsRecursive( new ArrayList<CollectionTree>());
	}

	private List<CollectionTree> getAllCollectionsRecursive(List<CollectionTree> list) {
		if( id != -1 )
			list.add(this);
    	for( CollectionTree child : children ) {
    		child.getAllCollectionsRecursive(list);
    	}
    	return list;
	}

	public void addCollectionDetails(JsonNode json) {
		this.title  = json.get("name").textValue();
		this.publish  = json.get("field_publish").booleanValue();
		this.description = json.get("description").textValue();
	}

	public void addTarget(JsonNode target) {
	    this.targets.add(new Target(target));
	}
	
	public Long getNumberOfOpenAccessTargets(boolean recursive) {
	    Set<Long> ids = getNumberOfTargets(recursive, false, new HashSet<Long>());
	    return (long) ids.size();
	}
	
	public Long getNumberOfTargets(boolean recursive) {
	    Set<Long> ids = getNumberOfTargets(recursive, true, new HashSet<Long>());
	    return (long) ids.size();
	}
	
	private Set<Long> getNumberOfTargets(boolean recursive, boolean all, Set<Long> ids) {
	    if( this.targets != null ) {
		for( Target t : this.targets ) {
		    if( t.isOpenAccess || all ) {
			ids.add(t.id);
		    }
		}
	    }
	    if( this.children != null && recursive) {
		for( CollectionTree ct : this.children ) {
		    ids = ct.getNumberOfTargets(recursive, all, ids);
		}
	    }
	    return ids;
	}
	
	public Long getNumberOfCollections(boolean recursive) {
	    return getNumberOfCollections(recursive, 0l);
	}
	
	private Long getNumberOfCollections(boolean recursive, Long num) {
	    if( this.children != null ) {
		num += this.children.size();
		if( recursive ) {
		  for( CollectionTree ct : this.children ) {
		    num = ct.getNumberOfCollections(recursive, num);
		  }
		}
	    }
	    return num;
	}
}
