package co.javeriana.dw.biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.javeriana.dw.biblioteca.entity.Libro;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    List<Libro> findAllByOrderByTituloAsc();

    boolean existsByIsbn(String isbn);
}
