package br.com.literalura.literalura.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DTO para capturar múltiplos formatos da API Gutendex, especialmente imagens.
 * 
 * Abordagem robusta:
 * - Usa @JsonAnySetter para capturar todos os campos dinâmicos
 * - Prioriza image/jpeg, com fallback para image/png
 * - Trata valores null/vazio com segurança
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormatsDTO {
    private static final Logger log = LoggerFactory.getLogger(FormatsDTO.class);
    
    private String imagemJpeg;
    private String imagemPng;
    
    public FormatsDTO() {
    }
    
    public FormatsDTO(String imagemJpeg) {
        this.imagemJpeg = imagemJpeg;
    }
    
    @JsonAnySetter
    public void capturarFormato(String chave, Object valor) {
        if (valor == null) {
            return;
        }
        
        String valorStr = valor.toString();
        
        // Captura image/jpeg (prioridade 1)
        if ("image/jpeg".equals(chave) && (this.imagemJpeg == null || this.imagemJpeg.isBlank())) {
            this.imagemJpeg = valorStr;
            log.debug("✅ image/jpeg capturado com sucesso");
        } 
        // Fallback para image/png se jpeg não estiver disponível
        else if ("image/png".equals(chave) && (this.imagemJpeg == null || this.imagemJpeg.isBlank())) {
            this.imagemPng = valorStr;
            this.imagemJpeg = valorStr; // Usa png como fallback para jpeg
            log.debug("✅ image/png capturado como fallback para jpeg");
        }
    }
    
    public String imagemJpeg() {
        return imagemJpeg;
    }
    
    public void setImagemJpeg(String imagemJpeg) {
        this.imagemJpeg = imagemJpeg;
    }
    
    @Override
    public String toString() {
        return "FormatsDTO{imagemJpeg='" + (imagemJpeg != null ? "✅" : "❌") + "'}";
    }
}
