import java.util.*;

// Сервис хранения
@Service
class StorageService {
    private final Map<UUID, Product> products;
    private final Map<UUID, Article> articles;

    public StorageService() {
        this.products = new HashMap<>();
        this.articles = new HashMap<>();
        initializeTestData();
    }

    private void initializeTestData() {
        // Добавляем тестовые продукты
        UUID smartphoneId = UUID.randomUUID();
        UUID laptopId = UUID.randomUUID();
        UUID headphonesId = UUID.randomUUID();

        products.put(smartphoneId, new Product(smartphoneId, "Смартфон Samsung Galaxy", 50000.0));
        products.put(laptopId, new Product(laptopId, "Ноутбук игровой ASUS", 120000.0));
        products.put(headphonesId, new Product(headphonesId, "Наушники беспроводные Sony", 15000.0));
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Планшет Apple iPad", 45000.0));
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Смартфон Apple iPhone", 80000.0));
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Телевизор LG OLED", 150000.0));

        // Добавляем тестовые статьи
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Обзор нового смартфона Samsung",
                "Полный обзор флагманского смартфона Samsung..."));
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Лучшие игровые ноутбуки 2024",
                "Топ-10 игровых ноутбуков этого года..."));
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Сравнение беспроводных наушников",
                "Детальное сравнение популярных моделей..."));
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Новости технологий",
                "Свежие новости из мира технологий..."));
    }

    public Collection<Product> getAllProducts() {
        return Collections.unmodifiableCollection(products.values());
    }

    public Collection<Article> getAllArticles() {
        return Collections.unmodifiableCollection(articles.values());
    }

    public Collection<Searchable> getAllSearchableItems() {
        List<Searchable> allItems = new ArrayList<>();
        allItems.addAll(products.values());
        allItems.addAll(articles.values());
        return Collections.unmodifiableCollection(allItems);
    }

    // Новый метод для получения продукта по ID
    public Optional<Product> getProductById(UUID id) {
        return Optional.ofNullable(products.get(id));
    }
}
