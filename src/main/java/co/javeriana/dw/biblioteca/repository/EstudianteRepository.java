package co.javeriana.dw.biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.javeriana.dw.biblioteca.entity.Estudiante;

import java.util.List;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    List<Estudiante> findAllByOrderByNombreAsc();

    boolean existsByCodigo(String codigo);
}
