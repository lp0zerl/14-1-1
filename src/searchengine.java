class SearchEngine {
    private final Searchable[] elements;
    private int size = 0;

    public SearchEngine(int capacity) {
        elements = new Searchable[capacity];
    }

    public void add(Searchable s) {
        if (size < elements.length) {
            elements[size++] = s;
        }
    }