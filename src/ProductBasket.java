import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Компонент корзины с сессионным scope
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
class ProductBasket {
    private final Map<UUID, Integer> items;

    public ProductBasket() {
        this.items = new HashMap<>();
    }

    // Метод добавления продукта в корзину
    public void addProduct(UUID productId) {
        items.put(productId, items.getOrDefault(productId, 0) + 1);
    }

    // Метод получения всех продуктов в корзине
    public Map<UUID, Integer> getItems() {
        return Collections.unmodifiableMap(new HashMap<>(items));
    }
}
