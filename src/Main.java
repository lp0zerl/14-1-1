// SkyshopApplication.java

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

// ==================== ИСКЛЮЧЕНИЯ И ОБРАБОТКА ОШИБОК ====================

// ==================== МОДЕЛИ ====================

// ==================== КОРЗИНА ====================

// Класс UserBasket
class UserBasket {
    private final List<BasketItem> items;
    private final double total;

    public UserBasket(List<BasketItem> items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        // Подсчет общей стоимости с помощью StreamAPI
        this.total = items.stream()
                .mapToDouble(BasketItem::getTotalPrice)
                .sum();
    }

    public List<BasketItem> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }
}

// ==================== СЕРВИСЫ ====================

// ==================== КОНТРОЛЛЕР ====================

