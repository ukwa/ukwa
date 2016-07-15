package uk.bl.wa.w3act;

import java.util.Map;
import java.util.Optional;

public interface CollectionsDataSource {
    Stats getStats();
    Map<Long, CollectionTree> getCollections();
    Target getTarget(long id);
}
