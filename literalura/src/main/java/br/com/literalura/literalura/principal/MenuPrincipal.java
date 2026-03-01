package br.com.literalura.literalura.principal;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.literalura.literalura.exception.LivroNaoEncontradoException;
import br.com.literalura.literalura.model.Autor;
import br.com.literalura.literalura.model.Livro;
import br.com.literalura.literalura.service.AutorService;
import br.com.literalura.literalura.service.LivroService;

@Component
public class MenuPrincipal implements CommandLineRunner {
  @Autowired
  private LivroService livroService;
  @Autowired
  private AutorService autorService;

  private Scanner scanner = new Scanner(System.in);

  @Override
  public void run(String... args) throws Exception {
    int opcao = -1;
    while (opcao != 0) {
      exibirMenu();
      opcao = lerOpcao();
      processarOpcao(opcao);
    }
  }

  private void exibirMenu() {
    System.out.println("Escolha uma opção");
    System.out.println("""
        -------------------------------------------
        1 - Buscar livro pelo título
        2 - Listar livros registrados
        3 - Listar autores registrados
        4 - Listar autores vivos em um ano
        5 - Listar livros por idioma
        6 - Buscar autor por nome (Banco de Dados)
        7 - Top 10 livros mais baixados
        8 - Exibir estatísticas de downloads
        0 - Sair
        -------------------------------------------
        """);
  }

  private int lerOpcao() {
    try {
      return Integer.parseInt(scanner.nextLine());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private void processarOpcao(int opcao) {
    switch (opcao) {
      case 1:
        buscarLivro();
        break;
      case 2:
        listarLivros();
        break;
      case 3:
        listarAutores();
        break;
      case 4:
        listarAutoresVivos();
        break;
      case 5:
        listarLivrosPorIdioma();
        break;
      case 6:
        buscarAutorPorNome();
        break;
      case 7:
        listarTop10();
        break;
      case 8:
        exibirEstatisticas();
        break;
      case 0:
        System.out.println("Saindo...");
        break;
      default:
        System.out.println("Opção inválida");
    }
    System.out.println();
  }

  private void buscarLivro() {
    System.out.print("Digite o título do livro: ");
    String titulo = scanner.nextLine();

    if (titulo == null || titulo.isBlank()) {
        System.out.println("O título não pode estar vazio.");
        return;
    }
   
    try {
        Livro livro = livroService.buscarESalvarLivroPorTitulo(titulo.trim());
        
        System.out.println("\n-----------------------------");
        System.out.println("Livro encontrado e salvo:");
        System.out.println("Título: " + livro.getTitulo());
        System.out.println("Autor: " + livro.getAutor().getNome());
        System.out.println("Idioma: " + livro.getIdioma());
        System.out.println("Downloads: " + livro.getNumeroDownloads());
        System.out.println("-----------------------------\n");
        
    } catch (LivroNaoEncontradoException e) {
        System.out.println("Aviso: " + e.getMessage());
    }
  }

  private void listarLivros() {
    List<Livro> livros = livroService.listarTodos();
    if (livros.isEmpty()) {
      System.out.println("Nenhum livro registrado.");
    } else {
      livros.forEach(l -> System.out.println("Título: " + l.getTitulo() + " | Autor: " + l.getAutor().getNome()));
    }
  }

  private void listarAutores() {
    List<Autor> autores = autorService.listarTodos();
    if (autores.isEmpty()) {
      System.out.println("Nenhum autor registrado.");
    } else {
      autores.forEach(a -> {
        System.out.println("Autor: " + a.getNome() +
            " (" + a.getAnoNascimento() + " - " + (a.getAnoFalecimento() != null ? a.getAnoFalecimento() : "") + ")");
        
        
        String titulosLivros = a.getLivros().stream()
                                .map(Livro::getTitulo)
                                .collect(Collectors.joining(", "));
                                
        System.out.println("Livros: " + (titulosLivros.isEmpty() ? "Nenhum livro vinculado" : titulosLivros));
        System.out.println();
      });
    }
}

  private void listarAutoresVivos() {
    System.out.print("Digite o ano: ");
    try {
        int ano = Integer.parseInt(scanner.nextLine());
        List<Autor> autores = autorService.listarAutoresVivosNoAno(ano);
        if (autores.isEmpty()) {
            System.out.println("Nenhum autor vivo encontrado nesse ano.");
        } else {
            autores.forEach(a -> System.out.println("Autor: " + a.getNome()));
        }
    } catch (NumberFormatException e) {
        System.out.println("Ano inválido. Digite apenas números.");
    }
  }

  private void listarLivrosPorIdioma() {
    System.out.println("""
        Idiomas disponíveis:
        es - Espanhol
        en - Inglês
        fr - Francês
        pt - Português
        """);
    System.out.print("Digite a sigla do idioma: ");
    String idioma = scanner.nextLine().toLowerCase();
    List<Livro> livros = livroService.listarPorIdioma(idioma);
    if (livros.isEmpty()) {
      System.out.println("Nenhum livro nesse idioma.");
    } else {
      livros.forEach(l -> System.out.println("Título: " + l.getTitulo() + " | Autor: " + l.getAutor().getNome()));
    }
  }

  private void buscarAutorPorNome() {
      System.out.print("Digite o nome do autor: ");
      String nome = scanner.nextLine();
      List<Autor> autores = autorService.buscarAutorPorNome(nome);
      if (autores.isEmpty()) {
          System.out.println("Autor não encontrado no banco.");
      } else {
          autores.forEach(a -> System.out.println("Autor: " + a.getNome() + " | Nascimento: " + a.getAnoNascimento()));
      }
  }

  private void listarTop10() {
      List<Livro> top10 = livroService.obterTop10();
      System.out.println("\nRANKING TOP 10:");
      top10.forEach(l -> System.out.println(l.getNumeroDownloads() + " downloads - " + l.getTitulo()));
  }

  private void exibirEstatisticas() {
      livroService.exibirEstatisticas();
  }
}
