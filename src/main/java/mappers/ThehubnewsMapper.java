package mappers;

import entity.ThehubnewsPage;

import java.util.ArrayList;


public interface ThehubnewsMapper {
    void addThehubnewsContent(ThehubnewsPage page);
    void addThehubnewsList(ThehubnewsPage page);
    ArrayList<ThehubnewsPage> getPagelink(int limit);
}
