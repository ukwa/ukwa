package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import play.*;
import play.data.Form;
import play.mvc.*;
import uk.bl.wa.w3act.CollectionTree;
import uk.bl.wa.w3act.CollectionsDataSource;
import uk.bl.wa.w3act.SolrCollectionsDataSource;
import uk.bl.wa.w3act.Target;
import uk.bl.wa.w3act.W3ACTCache;
import uk.bl.wa.w3act.forms.SearchForm;
import uk.bl.wa.w3act.view.CollectionTreeView;
import views.html.*;

public class Application extends Controller {

    public static CollectionsDataSource collectionsDataSource = new /*W3ACTCache()*/ SolrCollectionsDataSource();


    public Result viewTarget(Long id) {
        Target t = collectionsDataSource.getTarget(id);
        if(t == null) {
            return notFound("No target with ID " + id);
        }
        return ok(target.render(t));
    }

    public Result switchLang_cy_GB() {
        ctx().changeLang("cy-GB");
        return redirect(controllers.routes.Application.index());
    }

    public Result switchLang_en_GB() {
        ctx().changeLang("en-GB");
        return redirect(controllers.routes.Application.index());
    }

    private List<CollectionTree> getAllCollections() {
        List<CollectionTree> top = new ArrayList<CollectionTree>();
        for(CollectionTree ct : collectionsDataSource.getCollections().values()) {
            top.add(ct);
        }
        // And sort by title:
        Collections.sort(top, new Comparator<CollectionTree>() {
            public int compare(CollectionTree o1, CollectionTree o2) {
                return (o1.title != null ? o1.title : "").compareToIgnoreCase(o2.title != null ? o2.title : "");
            }
        });
        return top;
    }

    public Result viewCollections() {
        return ok(collections.render(getAllCollections(), false));
    }

    public Result viewAllCollections() {
        return ok(collections.render(getAllCollections(), true));
    }

    private CollectionTree findCollectionById(Long id) {
        CollectionTree found = null;
        for(CollectionTree top : collectionsDataSource.getCollections().values()) {
            if(found == null) {
                found = top.find(id);
            }
        }
        return found;
    }

    public Result submitCollectionView(Long id) {
        Form<SearchForm> formData = Form.form(SearchForm.class).bindFromRequest();
        if(formData.hasErrors()) {
            Logger.error("FORM ERRORS" + formData.globalErrors());
            flash("error", "Please correct errors above.");
            CollectionTree ct = findCollectionById(id);
            CollectionTreeView ctv = new CollectionTreeView(ct, 1, 20, "");
            return badRequest(collection.render(ctv, formData));
        }
        SearchForm s = formData.get();
        long sid = s.getCollectionId();
        int page = 1;
        int pageSize = 20;
        String filter = s.getFilter();
        return redirect(controllers.routes.Application.viewCollection(id, page, pageSize, filter));
    }

    public Result viewCollection(Long id, int page, int pageSize, String filter) {
        CollectionTree ct = findCollectionById(id);
        if(ct == null) {
            return notFound("No collection with ID " + id);
        }
        CollectionTreeView ctv = new CollectionTreeView(ct, page, pageSize, filter);

        // Search form:
        SearchForm search = new SearchForm();
        search.setCollectionId(id);
        Logger.info("Set collectionId: " + search.getCollectionId());
        search.setFilter(filter);
        Form<SearchForm> searchForm = Form.form(SearchForm.class).fill(search);
        //
        return ok(collection.render(ctv, searchForm));
    }

    public Result index() {
        return ok(index.render(""));
    }

}
