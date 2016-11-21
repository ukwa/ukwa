package uk.bl.wa.w3act;

import controllers.Application;
import controllers.routes;
import play.api.mvc.Request;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import scala.collection.immutable.VectorBuilder;
import uk.gov.hmrc.play.breadcrumb.model.Breadcrumb;
import uk.gov.hmrc.play.breadcrumb.model.BreadcrumbItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BreadcrumbFactory extends Action.Simple implements uk.gov.hmrc.play.breadcrumb.factory.BreadcrumbFactory {
    private static final String collectionUrlIdRegex = ".*/collection/([0-9]+)(/|\\z)";
    private static final Pattern collectionUrlIdPattern;

    static {
        collectionUrlIdPattern = Pattern.compile(collectionUrlIdRegex);
    }

    // Add the breadcrumb data to the web context
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        ctx.args.put("breadcrumb", buildBreadcrumb(ctx.request()._underlyingRequest()));
        return delegate.call(ctx);
    }

    @Override
    public Breadcrumb buildBreadcrumb(Request<?> request) {
        Breadcrumb breadcrumb = null;
        Matcher matcher = collectionUrlIdPattern.matcher(request.path());

        // Currently builds a breadcrumb for only collection views
        if(matcher.find()){
            Long collectionId = Long.parseLong(matcher.group(1));
            VectorBuilder<BreadcrumbItem> vectorBuilder= new VectorBuilder<BreadcrumbItem>();

            // Add the root of all collections as a breadcrumb item.
            vectorBuilder.$plus$eq(new BreadcrumbItem("Collections", routes.Application.viewAllCollections().url()));

            // Add the current collection ancestors
            Application.findCollectionById(collectionId).getAllAncestors()
                    .stream()
                    .map(collectionTree -> new BreadcrumbItem(collectionTree.title,
                            routes.Application.viewCollection(collectionTree.id, 1, 20, new String("")).url()))
                    .forEachOrdered((breadcrumbItem) -> vectorBuilder.$plus$eq(breadcrumbItem));

            // HACK - need to add a fake item at the end of the breadcrumb, as the control outputs the last item as
            // text only, rather than a link, which breaks principles used in the CSS
            vectorBuilder.$plus$eq(new BreadcrumbItem("", routes.Application.index().url()));

            breadcrumb = new Breadcrumb(vectorBuilder.result());
        }

        return breadcrumb;
    }
}
