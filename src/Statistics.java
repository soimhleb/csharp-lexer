import java.util.*;

public class Statistics {
    private int totalTokens = 0;
    private Map<TokenType, Integer> counts = new EnumMap<>(TokenType.class);

    public void addToken(Token token, String filename) {
        totalTokens++;
        TokenType type = token.getType();
        counts.put(type, counts.getOrDefault(type, 0) + 1);
        // идентификаторы больше не сохраняем
    }

    public void printStatistics() {
        System.out.println("=== Лексическая статистика ===");
        System.out.println("Общее количество лексем: " + totalTokens);
        System.out.println("\nАбсолютная и относительная частота по типам:");
        for (Map.Entry<TokenType, Integer> entry : counts.entrySet()) {
            double percent = (entry.getValue() * 100.0) / totalTokens;
            System.out.printf("  %-20s %6d (%.2f%%)%n", entry.getKey(), entry.getValue(), percent);
        }
    }
}