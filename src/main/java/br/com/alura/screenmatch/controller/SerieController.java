package br.com.alura.screenmatch.controller;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/series")

public class SerieController {

    @Autowired
    public SerieService service;

    @GetMapping
    public List<SerieDTO> listar() {
        return service.listarTodasSeries();

    }

    @GetMapping("/top5")
    public List<SerieDTO> listarTopSeries() {
        return service.listarTopSeries();
    }

    @GetMapping("/lancamentos")
    public List<SerieDTO> obterLancamento() {
        return service.obterLancamentos();
    }

    @GetMapping("{id}")
    public SerieDTO obterPorId(@PathVariable Long id) {
        return service.obterPorId(id);
    }

    @GetMapping("/{id}/temporadas/todas")
    public List<EpisodioDTO> obterTodasTemporaas(@PathVariable Long id) {
        return service.obterTodasTemporadas(id);
    }


    @GetMapping("/{id}/temporadas/{numero}")
    public List<EpisodioDTO> obterTodasTemporaas(@PathVariable Long id, @PathVariable Long numero) {
        return service.obterTemporadasPorNumero(id, numero);
    }

    @GetMapping("/{id}/temporadas/top")
    public List<EpisodioDTO> obterTopEpisodios(@PathVariable Long id){
        return service.obterTopEpisodios(id);
    }

    @GetMapping("/categoria/{categoria}")
    public List<SerieDTO> obterPorCategoria(@PathVariable String categoria) {
        return service.obterPorCategoria(categoria);
    }

}
