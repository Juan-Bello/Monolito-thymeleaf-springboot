package co.javeriana.dw.biblioteca.service;

import co.javeriana.dw.biblioteca.entity.Libro;
import co.javeriana.dw.biblioteca.exception.NegocioException;
import co.javeriana.dw.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<Libro> listar() {
        return libroRepository.findAllByOrderByTituloAsc();
    }

    @Transactional(readOnly = true)
    public List<Libro> listarConDisponibilidad() {
        return listar().stream().filter(l -> l.getCantidadDisponible() > 0).toList();
    }

    @Transactional
    public Libro crear(Libro libro) {
        if (libroRepository.existsByIsbn(libro.getIsbn())) {
            throw new NegocioException("Ya existe un libro con el ISBN " + libro.getIsbn());
        }
        // al registrar un libro, todos sus ejemplares quedan disponibles
        libro.setCantidadDisponible(libro.getCantidadTotal());
        return libroRepository.save(libro);
    }
}
