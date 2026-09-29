package co.javeriana.dw.biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.javeriana.dw.biblioteca.entity.EstadoPrestamo;
import co.javeriana.dw.biblioteca.entity.Estudiante;
import co.javeriana.dw.biblioteca.entity.Libro;
import co.javeriana.dw.biblioteca.entity.Prestamo;

import java.time.LocalDate;
import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findAllByOrderByFechaPrestamoDesc();

    boolean existsByEstudianteAndEstado(Estudiante estudiante, EstadoPrestamo estado);

    boolean existsByEstudianteAndLibroAndEstadoIn(Estudiante estudiante, Libro libro, List<EstadoPrestamo> estados);

    boolean existsByEstudianteAndEstadoAndFechaLimiteBefore(Estudiante estudiante, EstadoPrestamo estado, LocalDate fechaLimite);

    List<Prestamo> findByEstadoAndFechaLimiteBefore(EstadoPrestamo estado, LocalDate fechaLimite);
}
