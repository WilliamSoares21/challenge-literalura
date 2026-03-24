package br.com.literalura.literalura.security;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Rate Limiter simples em memória.
 * 
 * Implementa Token Bucket algorithm para limitar requisições por IP.
 * Para produção, ainda preciso estudar como usar Redis ou Spring Cloud RateLimiter.
 * 
 * Limite: 20 requisições por 5 minutos por IP
 */
@Component
public class RateLimiter {

  private static final int MAX_REQUESTS = 20;
  private static final long TIME_WINDOW_MS = 5 * 60 * 1000; // 5 minutos

  private final Map<String, RequestTracker> trackers = new ConcurrentHashMap<>();

  /**
   * Verifica se a requisição de um IP deve ser permitida.
   * 
   * @param clientIp IP do cliente
   * @return true se permitida, false se excedeu rate limit
   */
  public boolean allowRequest(String clientIp) {
    if (clientIp == null || clientIp.isBlank()) {
      clientIp = "unknown";
    }

    // Remove IPs antigos do tracker
    long now = System.currentTimeMillis();
    trackers.entrySet().removeIf(entry -> {
      long lastRequest = entry.getValue().lastRequestTime;
      return now - lastRequest > TIME_WINDOW_MS;
    });

    // Obtém ou cria tracker para este IP
    RequestTracker tracker = trackers.computeIfAbsent(clientIp, k -> new RequestTracker());

    // Verifica janela de tempo
    if (now - tracker.windowStartTime > TIME_WINDOW_MS) {
      // Nova janela
      tracker.requestCount = 0;
      tracker.windowStartTime = now;
    }

    // Incrementa contador
    tracker.requestCount++;
    tracker.lastRequestTime = now;

    // Retorna se está dentro do limite
    return tracker.requestCount <= MAX_REQUESTS;
  }

  /**
   * Retorna informações do limite para um IP.
   * 
   * @param clientIp IP do cliente
   * @return Mapa com requestCount e remainingRequests
   */
  public Map<String, Integer> getStatus(String clientIp) {
    RequestTracker tracker = trackers.getOrDefault(clientIp, new RequestTracker());
    
    long now = System.currentTimeMillis();
    if (now - tracker.windowStartTime > TIME_WINDOW_MS) {
      tracker.requestCount = 0;
      tracker.windowStartTime = now;
    }

    Map<String, Integer> status = new HashMap<>();
    status.put("requestCount", tracker.requestCount);
    status.put("remainingRequests", Math.max(0, MAX_REQUESTS - tracker.requestCount));
    
    return status;
  }

  /**
   * Classe privada para rastrear requisições por IP.
   */
  private static class RequestTracker {
    int requestCount = 0;
    long windowStartTime = System.currentTimeMillis();
    long lastRequestTime = System.currentTimeMillis();
  }
}
