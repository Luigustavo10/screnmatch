package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar séries por nome
                    5 - Buscar serie por Ator
                    6 - Buscar os top 5 episodios
                    7 - Buscar Serie por genero
                    8 - Filtrar Series
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSerieporNome();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                case 7:
                    buscarSeriePorGenero();
                    break;
                case 8:
                    busacarSeriePorQuantidadeTemporadasAndAvaliacao();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void busacarSeriePorQuantidadeTemporadasAndAvaliacao() {
        System.out.println("Digite A quantidade de temporadas:");
        var temporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Digite a avaliacao minima:");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> serieTemporadas = repositorio.findByTotalTemporadasLessThanEqualAndAvaliacaoIsGreaterThanEqual(temporadas, avaliacao);
        serieTemporadas.forEach(s -> System.out.println(s.getTitulo()+" Temporadas:  "+s.getTotalTemporadas()+" Avaliação: "+s.getAvaliacao()));

    }

    private void buscarSeriePorGenero() {
        System.out.println("Qual o genero para a busca:");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortuguese(nomeGenero);
        List<Serie> seriePorGenero = repositorio.findByGenero(categoria);
        System.out.println("Series da categoria " + nomeGenero);
        seriePorGenero.forEach(s -> System.out.println(s.getTitulo()));
    }

    private void buscarTop5Series() {
        List<Serie> melhoresSeries = repositorio.findTop5ByOrderByAvaliacaoDesc();
        melhoresSeries.forEach(s -> System.out.println(s.getTitulo() + "  Avaliação: " + s.getAvaliacao()));


    }

    private void buscarSeriePorAtor() {
        System.out.println("Qual o nome para a busca:");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliação a partir de qual valor:");
        var valorAvaliacao = leitura.nextDouble();
        List<Serie> atorEncontrado = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, valorAvaliacao);
        System.out.println("Series em que { " + nomeAtor + "} trabalhou:");
        atorEncontrado.forEach(s -> System.out.println(s.getTitulo() + " Avaliação" + s.getAvaliacao() + " Atores: " + s.getAtores()));

    }

    private void buscarSerieporNome() {
        System.out.println("Digite o nome da serie:");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> serieEncontrada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        if (serieEncontrada.isPresent()) {
            System.out.println(serieEncontrada.get());
        } else {
            System.out.println("Serie Nao encontrada");
        }

    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma serie pelo nome");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }

    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }
}
