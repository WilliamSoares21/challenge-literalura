package br.com.literalura.literalura;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.literalura.literalura.dto.FormatsDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste unitário para validar a deserialização do FormatsDTO
 * especialmente para o campo "image/jpeg" da API Gutendex.
 */
public class FormatsDeserializationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testDeserializeImageJpeg() throws Exception {
        String json = """
            {
                "text/html": "https://www.gutenberg.org/ebooks/67740.html.images",
                "image/jpeg": "https://www.gutenberg.org/cache/epub/67740/pg67740.cover.medium.jpg",
                "text/plain": "https://www.gutenberg.org/ebooks/67740.txt.utf-8"
            }
            """;

        FormatsDTO formats = objectMapper.readValue(json, FormatsDTO.class);
        
        assertNotNull(formats.imagemJpeg(), "imagemJpeg não deveria ser nulo");
        assertTrue(formats.imagemJpeg().contains("pg67740.cover.medium.jpg"), 
            "imagemJpeg deveria conter a URL da imagem");
        System.out.println("✅ Imagem capturada: " + formats.imagemJpeg());
    }

    @Test
    public void testDeserializeWithoutImageJpegButWithPng() throws Exception {
        String json = """
            {
                "text/html": "https://www.gutenberg.org/ebooks/123.html.images",
                "image/png": "https://www.gutenberg.org/cache/epub/123/pg123.cover.png",
                "text/plain": "https://www.gutenberg.org/ebooks/123.txt.utf-8"
            }
            """;

        FormatsDTO formats = objectMapper.readValue(json, FormatsDTO.class);
        
        assertNotNull(formats.imagemJpeg(), "imagemJpeg não deveria ser nulo (fallback para png)");
        assertTrue(formats.imagemJpeg().contains("pg123.cover.png"), 
            "imagemJpeg deveria conter a URL do png como fallback");
        System.out.println("✅ Imagem PNG (fallback) capturada: " + formats.imagemJpeg());
    }

    @Test
    public void testDeserializeNoImage() throws Exception {
        String json = """
            {
                "text/html": "https://www.gutenberg.org/ebooks/456.html.images",
                "text/plain": "https://www.gutenberg.org/ebooks/456.txt.utf-8"
            }
            """;

        FormatsDTO formats = objectMapper.readValue(json, FormatsDTO.class);
        
        assertNull(formats.imagemJpeg(), "imagemJpeg deveria ser nulo quando não houver imagem");
        System.out.println("✅ Nenhuma imagem encontrada (comportamento esperado)");
    }
}
