package br.com.literalura.literalura.infra.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deserializador customizado para o campo "summaries" do LivroDTO.
 * 
 * Converte tanto lista de strings quanto Map de summaries baseado em keys
 * em uma lista de strings simples.
 */
public class SummariesDeserializer extends JsonDeserializer<List<String>> {
    
    private static final Logger log = LoggerFactory.getLogger(SummariesDeserializer.class);
    
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        JsonNode node = p.getCodec().readTree(p);
        List<String> resultado = new ArrayList<>();
        
        // Se for null, retorna lista vazia
        if (node == null || node.isNull()) {
            return resultado;
        }
        
        // Se for um array de strings, retorna diretamente
        if (node.isArray()) {
            node.forEach(item -> {
                if (item.isTextual()) {
                    resultado.add(item.asText());
                }
            });
            log.debug("✅ summaries deserializado como array");
            return resultado;
        }
        
        // Se for um objeto (Map), extrai os valores como texto
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String valor = entry.getValue().asText();
                if (valor != null && !valor.isBlank()) {
                    resultado.add(valor);
                }
            });
            log.debug("✅ summaries deserializado como map com {} itens", resultado.size());
            return resultado;
        }
        
        // Se for string simples, adiciona à lista
        if (node.isTextual()) {
            String valor = node.asText();
            if (valor != null && !valor.isBlank()) {
                resultado.add(valor);
            }
            log.debug("✅ summaries deserializado como string única");
            return resultado;
        }
        
        log.debug("⚠️ summaries tipo desconhecido, retornando lista vazia");
        return resultado;
    }
}
