package uk.bl.wa.w3act;

public class Stats {
    private String lastUpdatedString;
    private long totalTargets;
    private long totalCollections;
    private long totalTopCollections;

    public Stats(String lastUpdatedString, long totalTargets, long totalCollections, long totalTopCollections){
        this.lastUpdatedString = lastUpdatedString;
        this.totalTargets = totalTargets;
        this.totalCollections = totalCollections;
        this.totalTopCollections = totalTopCollections;
    }

    public String getLastUpdatedString() {
        return lastUpdatedString;
    }

    public long getTotalTargets() {
        return totalTargets;
    }

    public long getTotalCollections() {
        return totalCollections;
    }

    public long getTotalTopCollections() {
        return totalTopCollections;
    }
}
