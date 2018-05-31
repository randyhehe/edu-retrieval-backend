import java.util.ArrayList;
import java.util.List;

public class ListEntries {
    public int totalResults;
    public List<WebEntry> entries;

    public ListEntries(int totalResults) {
        this.totalResults = totalResults;
        entries = new ArrayList<>();
    }
}
