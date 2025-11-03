import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Сервис работы с корзиной
@Service
class BasketService {
    private final ProductBasket productBasket;
    private final StorageService storageService;

    public BasketService(ProductBasket productBasket, StorageService storageService) {
        this.productBasket = productBasket;
        this.storageService = storageService;
    }

    // Метод добавления товара в корзину по ID - теперь выбрасывает NoSuchProductException
    public void addProductToBasket(UUID productId) {
        Optional<Product> product = storageService.getProductById(productId);
        if (product.isEmpty()) {
            throw new NoSuchProductException("Продукт с ID '" + productId + "' не найден в каталоге");
        }
        productBasket.addProduct(productId);
    }

    // Метод получения корзины пользователя
    public UserBasket getUserBasket() {
        Map<UUID, Integer> basketItems = productBasket.getItems();

        // Преобразуем Map в список BasketItem с помощью StreamAPI
        List<BasketItem> items = basketItems.entrySet().stream()
                .map(entry -> {
                    UUID productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Product product = storageService.getProductById(productId)
                            .orElseThrow(() -> new NoSuchProductException("Продукт с ID '" + productId + "' не найден в каталоге"));
                    return new BasketItem(product, quantity);
                })
                .collect(Collectors.toList());

        return new UserBasket(items);
    }
}
