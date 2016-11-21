package uk.bl.wa.w3act;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SolrCollectionsDataSource implements CollectionsDataSource {
    private SolrClient solrClient;

    public void init(SolrClient solrClient){
        this.solrClient = solrClient;
    }

    @Override
    public
    Target getTarget(long id){
        try {
            Optional<Target> target = solrClient.query(new SolrQuery(String.format("type:target AND id:%1d", id))).getResults()
                    .stream()
                    .map(SolrCollectionsDataSource::solrDocumentToTarget)
                    .findFirst();

            return target.isPresent() ? target.get() : null;
        }
        catch(SolrServerException e) {
            // TODO
            e.printStackTrace();
        }
        catch(IOException e) {
            // TODO
            e.printStackTrace();
        }

        return null;
    }

    protected static Target solrDocumentToTarget(SolrDocument solrDocument){
        Target target = new Target();
        target.id = Long.parseLong((String)solrDocument.getFieldValue("id"));
        target.title = (String)solrDocument.getFieldValue("title");
        target.description = (String)solrDocument.getFieldValue("description");
        target.startDate = (Date)solrDocument.getFieldValue("startDate");
        target.endDate = (Date)solrDocument.getFieldValue("endDate");
        target.language = (String)solrDocument.getFieldValue("language");
        target.primaryUrl = new TargetUrl((String)solrDocument.getFieldValue("url"), true);
        target.additionalUrls = solrDocument.getFieldValues("additionalUrl") != null ? solrDocument.getFieldValues("additionalUrl")
                .stream()
                .map(url -> new TargetUrl((String)url, false))
                .collect(Collectors.toList())
            : new ArrayList<>();
        target.isOpenAccess = solrDocument.getFieldValues("licenses") != null && solrDocument.getFieldValues("licenses").size() > 0;

        return target;
    }

    protected static CollectionTree solrDocumentToCollectionTree(SolrDocument solrDocument){
        CollectionTree collectionTree = new CollectionTree();
        collectionTree.id = Long.parseLong((String)solrDocument.getFieldValue("id"));
        collectionTree.parentId = solrDocument.getFieldValue("parentId") == null ? 0 : (Long)(solrDocument.getFieldValue("parentId"));
        collectionTree.title = (String)solrDocument.getFieldValue("name");
        collectionTree.publish = true; // Only publishable collections will be in the Solr index
        collectionTree.description = (String)solrDocument.getFieldValue("description");
        return collectionTree;
    }

    @Override
    public Stats getStats() {
        long totalTargets = 0;
        long totalCollections = 0;
        long totalTopCollections = 0;

        try {
            totalTargets = solrClient.query(new SolrQuery("type:target")
                    .setRows(0))
                    .getResults()
                    .getNumFound();

            totalCollections = solrClient.query(new SolrQuery("type:collection")
                    .setRows(0))
                    .getResults()
                    .getNumFound();

            totalTopCollections = solrClient.query(new SolrQuery("type:collection AND -parent:[\"\" TO *]")
                    .setRows(0))
                    .getResults()
                    .getNumFound();

            return new Stats(
                    new ISO8601DateFormat().format(new Date()),
                    totalTargets,
                    totalCollections,
                    totalTopCollections
            );
        }
        catch(SolrServerException e) {
            // TODO
            e.printStackTrace();
        }
        catch(IOException e) {
            // TODO
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<Long, CollectionTree> getCollections() {
        try {
            List<CollectionTree> collections =
                    solrClient.query(new SolrQuery("type:collection").setRows(99999999)).getResults()
                    .stream()
                    .map(SolrCollectionsDataSource::solrDocumentToCollectionTree)
                    .collect(Collectors.toList());

            return collections
                    .stream()
                    // Add child collections and targets
                    .map(collectionTree ->  {
                        collectionTree.children = collections
                                .stream()
                                .filter(ct -> ct.parentId == collectionTree.id)
                                .map(ct -> {ct.parent = collectionTree; return ct;})
                                .collect(Collectors.toList());
                        collectionTree.targets = getTargets(collectionTree.id);
                        return collectionTree;
                    })
                    .collect(Collectors.toMap(CollectionTree::getId, ct -> ct));
        }
        catch(SolrServerException e) {
            // TODO
            e.printStackTrace();
        }
        catch(IOException e) {
            // TODO
            e.printStackTrace();
        }

        return null;
    }

    protected List<Target> getTargets(long collectionId) {
        try {
            return solrClient.query(new SolrQuery(String.format("type:target AND parentId:%1d", collectionId)).setRows(99999999))
                    .getResults()
                    .stream()
                    .map(SolrCollectionsDataSource::solrDocumentToTarget)
                    .collect(Collectors.toList());
        }
        catch(SolrServerException e) {
            // TODO
            e.printStackTrace();
        }
        catch(IOException e) {
            // TODO
            e.printStackTrace();
        }

        return null;
    }
}
