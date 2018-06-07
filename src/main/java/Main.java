import static spark.Spark.*;

import com.google.gson.Gson;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;
import org.apache.lucene.store.*;

import java.io.File;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static final String PROJECT_ROOT = new File("").getAbsolutePath();
    static final String INDEX_FILE = PROJECT_ROOT + File.separator + "indexFile";

    public static void main(String[] args) {
        get("/search", (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");

            String queryString = request.queryParams("query");
            int start = Integer.parseInt(request.queryParams("start"));

            Analyzer analyzer = new StandardAnalyzer();
            Directory directory = FSDirectory.open(Paths.get(INDEX_FILE));

            DirectoryReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            String[] fields = {"title", "body"};
            Map<String, Float> boosts = new HashMap<>();
            boosts.put(fields[0], 1.0f);
            boosts.put(fields[1], 0.5f);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer, boosts);
            Query query = parser.parse(queryString);
            int topHitCount = start + 10;
            LRUQueryCache queryCache = new LRUQueryCache(1000, 1073741824);
            indexSearcher.setQueryCache(queryCache);

            ScoreDoc[] hits = indexSearcher.search(query, Integer.MAX_VALUE).scoreDocs;

            ListEntries list = new ListEntries(hits.length);
            for (int rank = start; rank < hits.length && rank < start + 10; rank++) {
                Document hitDoc = indexSearcher.doc(hits[rank].doc);
                list.entries.add(new WebEntry(hitDoc.get("url"), hitDoc.get("title"), rank + 1));
            }
            indexReader.close();
            directory.close();

            Gson gson = new Gson();
            System.out.println(gson.toJson(list));
            return gson.toJson(list);
        });
    }
}
