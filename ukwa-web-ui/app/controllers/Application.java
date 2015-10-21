package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import play.*;
import play.i18n.Lang;
import play.mvc.*;
import uk.bl.wa.w3act.CollectionTree;
import uk.bl.wa.w3act.Target;
import uk.bl.wa.w3act.W3ACTCache;
import views.html.*;

public class Application extends Controller {
	
	public static W3ACTCache w3act = new W3ACTCache();
	
	
	public Result viewTarget(Long id) {
		Target t = w3act.targets.get(id);
		if( t == null ) {
			return notFound("No target with ID "+id);
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
		for( CollectionTree ct : w3act.collections.values() ) {
			top.add(ct);
		}
		// And sort by title:
		Collections.sort(top, new Comparator<CollectionTree>(){
			   public int compare(CollectionTree o1, CollectionTree o2){
			      return o1.title.compareToIgnoreCase(o2.title);
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
	    for( CollectionTree top : w3act.collections.values()) {
		if( found == null) {
		    found = top.find(id);
		}
	    }
	    return found;
	}
	
	public Result viewCollection(Long id) {
		CollectionTree ct = findCollectionById(id);
		if( ct == null ) {
			return notFound("No collection with ID "+id);
		}
		return ok(collection.render(ct));
	}

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

}
