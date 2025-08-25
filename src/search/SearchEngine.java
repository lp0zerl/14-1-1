package search;

public class SearchEngine {
    private final Searchable[] elements;
    private int size = 0;

    public SearchEngine(int capacity) {
        this.elements = new Searchable[capacity];
    }

    public void add(Searchable searchable) {
        if (size < elements.length) {
            elements[size++] = searchable;
        }
    }


    public Searchable[] search(String keyword) {
        Searchable[] results = new Searchable[5];
        int found = 0;
        for (int i = 0; i < size; i++) {
            if (elements[i].getSearchTerm().toLowerCase().contains(keyword.toLowerCase())) {
                results[found++] = elements[i];
                if (found == 5) break;
            }
        }
        return results;
    }
}