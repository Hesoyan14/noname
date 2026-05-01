package itz.silentcore.feature.autobuy.items;

import java.util.HashMap;
import java.util.Map;

public class DefaultPrices {
    private static final Map<String, Integer> defaultPrices = new HashMap<>();

    static {
        // Базовые ресурсы
        defaultPrices.put("Алмаз", 1500);
        defaultPrices.put("Алмазный блок", 11500);
        defaultPrices.put("Незеритовый слиток", 50000);
        defaultPrices.put("Незеритовый блок", 50000);
        defaultPrices.put("Незеритовое улучшение", 50000);
        defaultPrices.put("Золотой слиток", 1500);
        defaultPrices.put("Блок золота", 1500);
        defaultPrices.put("Железный слиток", 1000);
        defaultPrices.put("Железный блок", 1000);
        
        // Еда
        defaultPrices.put("Золотое яблоко", 1000);
        defaultPrices.put("Зачарованное золотое яблоко", 60000);
        defaultPrices.put("Яблоко", 1000);
        defaultPrices.put("Торт", 10000);
        
        // Особые предметы
        defaultPrices.put("Тотем бессмертия", 1000);
        defaultPrices.put("Элитры", 100000);
        defaultPrices.put("Эндер жемчуг", 1000);
        defaultPrices.put("Трезубец", 1000);
        defaultPrices.put("Булава", 100000);
        defaultPrices.put("Мешок", 10000);
        
        // Блоки и материалы
        defaultPrices.put("Обсидиан", 1000);
        defaultPrices.put("Динамит", 1000);
        defaultPrices.put("Маяк", 1500);
        defaultPrices.put("Спавнер", 1000000);
        defaultPrices.put("Шалкеровый ящик", 1000);
        
        // Опыт и зелья
        defaultPrices.put("Пузырёк опыта", 1500);
        defaultPrices.put("Звезда Незера", 1000);
        
        // Разное
        defaultPrices.put("Бирка", 1000);
        defaultPrices.put("Порох", 1000);
        defaultPrices.put("Палка ифрита", 1000);
        defaultPrices.put("Фейерверк", 5000);
        
        // Головы
        defaultPrices.put("Голова скелета", 1000);
        defaultPrices.put("Голова зомби", 1000);
        defaultPrices.put("Голова крипера", 1000);
        defaultPrices.put("Голова визер-скелета", 1000);
        defaultPrices.put("Голова пиглина", 1000);
        defaultPrices.put("Голова дракона", 1000);
        
        // Яйца спавна
        defaultPrices.put("Яйцо зомби-жителя", 100000);
        defaultPrices.put("Яйцо жителя", 200000);
        defaultPrices.put("Яйцо вихря", 200000);
        
        // Руды
        defaultPrices.put("Алмазная руда", 1000);
        defaultPrices.put("Изумрудная руда", 1000);
        
        // Новые предметы 1.21
        defaultPrices.put("Ключ испытаний", 1000);
        defaultPrices.put("Зловещий ключ испытаний", 1000);
        defaultPrices.put("Заряд ветра", 1790);
        defaultPrices.put("Стержень вихря", 1790);
    }

    public static int getPrice(String displayName) {
        return defaultPrices.getOrDefault(displayName, 5000);
    }
}
