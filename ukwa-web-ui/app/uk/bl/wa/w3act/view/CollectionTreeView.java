/**
 * 
 */
package uk.bl.wa.w3act.view;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import uk.bl.wa.w3act.CollectionTree;
import uk.bl.wa.w3act.Target;

/**
 * @author andy
 *
 */
public class CollectionTreeView extends CollectionTree {
    
    public CollectionTree ct;
    public String filter;
    public int page;
    public int offset;
    public int pageSize;
    public int totalPages;
    public int totalTargets;
    public int pageStart;
    public int pageEnd;
    public int pagerStart;
    public int pagerEnd;
    public int pagerMax = 10;
    public boolean morePagesAbove;
    public boolean morePagesBelow;
    
    private static final long serialVersionUID = -9156803844993709903L;

    public CollectionTreeView( CollectionTree ct, int page, int pageSize, String filter ) {
	this.ct = ct;
	this.filter = filter;
	this.page = page;
	this.pageSize = pageSize;
	this.totalPages = 1;
	this.totalTargets = ct.targets.size();
	this.pageStart = 1;
	this.pageEnd = this.totalTargets;
	this.morePagesBelow = false;
	this.morePagesAbove = false;
	
	// Filtering:
	
	// Paging:
	pageIt();
    }
    
    private void pageIt() {
	// Set the page total
	this.totalPages = 1 + totalTargets/pageSize;
	// Limit targets to those on the current page:
	if( ct.targets.size() > pageSize ) {
	    List<Target> tp = new ArrayList<Target>();
	    for( int i = (page-1)*pageSize; 
		    i < page*pageSize && i < totalTargets; i++ ) {
			tp.add(ct.targets.get(i));
            }
	    ct.targets = tp;
	    // Fix page start-end:
	    this.pageStart = (page-1)*pageSize + 1;
	    this.pageEnd = page*pageSize;
	    if( this.pageEnd > totalTargets) pageEnd = totalTargets;
	}
	// Reduce range of pages viewed when number is large.
	this.pagerStart = 1;
	this.pagerEnd = this.totalPages;
	if( this.totalPages > pagerMax ) {
	    this.pagerStart = page - pagerMax/2;
	    if( pagerStart <= 1 ) {
		pagerStart = 1;
		morePagesBelow = false;
	    } else {
		morePagesBelow = true;
	    }
	    this.pagerEnd = page + pagerMax/2;
	    if( pagerEnd >= totalPages) {
		pagerEnd = totalPages;
		morePagesAbove = false;
	    } else {
		morePagesAbove = true;
	    }
	}
    }

}
