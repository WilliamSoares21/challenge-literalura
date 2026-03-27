package br.com.literalura.literalura.model;

import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "livros")
public class Livro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String titulo;

  private String idioma;
  private Integer numeroDownloads;

  @ManyToOne
  @JoinColumn(name = "autor_id")
  private Autor autor;

  @Column(name = "curiosidade", columnDefinition = "TEXT")
  private String curiosidade;

  @Column(name = "imagem")
  private String imagem;

  @Column(name = "resumo", columnDefinition = "TEXT")
  private String resumo;

  @Column(name = "genero")
  private String genero;

  @Column(name = "data_ultima_atualizacao")
  private LocalDateTime dataUltimaAtualizacao;

  public Livro() {
  }

  public Livro(String titulo, String curiosidade) {
    this.titulo = titulo;
    this.curiosidade = curiosidade;
  }

  public String getCuriosidade() {
    return curiosidade;
  }

  public void setCuriosidade(String curiosidade) {
    this.curiosidade = curiosidade;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getIdioma() {
    return idioma;
  }

  public void setIdioma(String idioma) {
    this.idioma = idioma;
  }

  public Integer getNumeroDownloads() {
    return numeroDownloads;
  }

  public void setNumeroDownloads(Integer numeroDownloads) {
    this.numeroDownloads = numeroDownloads;
  }

  public Autor getAutor() {
    return autor;
  }

  public void setAutor(Autor autor) {
    this.autor = autor;
  }

  public String getImagem() {
    return imagem;
  }

  public void setImagem(String imagem) {
    this.imagem = imagem;
  }

  public String getResumo() {
    return resumo;
  }

  public void setResumo(String resumo) {
    this.resumo = resumo;
  }

  public String getGenero() {
    return genero;
  }

  public void setGenero(String genero) {
    this.genero = genero;
  }

  public LocalDateTime getDataUltimaAtualizacao() {
    return dataUltimaAtualizacao;
  }

  public void setDataUltimaAtualizacao(LocalDateTime dataUltimaAtualizacao) {
    this.dataUltimaAtualizacao = dataUltimaAtualizacao;
  }
}
