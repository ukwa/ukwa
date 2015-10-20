/**
 * 
 */
package uk.bl.wa.w3act;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public CollectionTree( JsonNode json ) {
		this.id = -1;
		this.title = "ROOT";
		for( JsonNode item : json ) {
			children.add( new CollectionTree(item, this.id) );
		}
	}
	
	private CollectionTree( JsonNode json, long parent ) {
		this.parentId = parent;
		this.id = Long.parseLong(json.get("key").textValue().replace("\"", ""));
		this.title = json.get("title").textValue();
		Logger.info("Parsing "+title);
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
		this.publish  = json.get("field_publish").booleanValue();
		this.description = json.get("description").textValue();
	}

	public void addTargets(JsonNode targets) {
		for( JsonNode t :targets ) {
			this.targets.add(new Target(t));
		}
		
	}
	
}
