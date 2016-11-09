/**
 *
 */
package uk.bl.wa.w3act.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import uk.bl.wa.w3act.CollectionTree;
import uk.bl.wa.w3act.Target;

/**
 * @author andy
 */
public class CollectionTreeView {

    public CollectionTree ct;
    public List<Target> targets;
    public String filter;
    public int page;
    public int offset;
    public int pageSize;
    public int totalPages;
    public int totalTargets;
    public int numFilteredTargets;
    public int pageStart;
    public int pageEnd;
    public int pagerStart;
    public int pagerEnd;
    public int pagerMax = 10;
    public boolean morePagesAbove;
    public boolean morePagesBelow;

    private static final long serialVersionUID = -9156803844993709903L;

    public CollectionTreeView(CollectionTree ct, int page, int pageSize, String filter) {
        this.ct = ct;
        this.filter = filter;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = 1;
        this.totalTargets = ct.getTargets().size();
        this.pageStart = 1;
        this.pageEnd = this.totalTargets;
        this.morePagesBelow = false;
        this.morePagesAbove = false;

        // Filtering:
        filterIt();

        sortIt();

        // Paging:
        pageIt();
    }

    private void filterIt() {
        if(filter == null || "".equals(filter)) {
            targets = ct.getTargets();
        }
        else {
            targets = ct.getTargets(true, Pattern.compile(".*" + Pattern.quote(filter) + ".*", Pattern.CASE_INSENSITIVE));
        }

        numFilteredTargets = targets.size();
    }

    private void sortIt(){
        targets.sort((t1, t2) -> t1.title.compareTo(t2.title));
    }

    private void pageIt() {
        // Set the page total
        this.totalPages = 1 + targets.size() / pageSize;

        // Fix page start-end:
        this.pageStart = (page - 1) * pageSize + 1;
        this.pageEnd = page * pageSize;
        if(this.pageEnd > targets.size()) {
            pageEnd = targets.size();
        }

        // Limit targets to those on the current page:
        if(targets.size() > pageSize) {
            List<Target> tp = new ArrayList<Target>();
            for(int i = (page - 1) * pageSize;
                i < page * pageSize && i < targets.size(); i++) {
                tp.add(targets.get(i));
            }

            targets = tp;
        }

        // Reduce range of pages viewed when number is large.
        this.pagerStart = 1;
        this.pagerEnd = this.totalPages;
        if(this.totalPages > pagerMax) {
            this.pagerStart = page - pagerMax / 2;
            if(pagerStart <= 1) {
                pagerStart = 1;
                morePagesBelow = false;
            }
            else {
                morePagesBelow = true;
            }
            this.pagerEnd = page + pagerMax / 2;
            if(pagerEnd >= totalPages) {
                pagerEnd = totalPages;
                morePagesAbove = false;
            }
            else {
                morePagesAbove = true;
            }
        }
    }

}
