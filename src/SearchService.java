import java.util.Collection;
import java.util.stream.Collectors;

// Сервис поиска
@Service
class SearchService {
    private final StorageService storageService;

    public SearchService(StorageService storageService) {
        this.storageService = storageService;
    }

    public Collection<SearchResult> search(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return storageService.getAllSearchableItems().stream()
                    .map(SearchResult::fromSearchable)
                    .collect(Collectors.toList());
        }

        String lowerPattern = pattern.toLowerCase();

        return storageService.getAllSearchableItems().stream()
                .filter(item -> item.getName().toLowerCase().contains(lowerPattern))
                .map(SearchResult::fromSearchable)
                .collect(Collectors.toList());
    }
}
