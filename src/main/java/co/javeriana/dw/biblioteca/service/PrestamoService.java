package co.javeriana.dw.biblioteca.service;

import co.javeriana.dw.biblioteca.entity.EstadoPrestamo;
import co.javeriana.dw.biblioteca.entity.Estudiante;
import co.javeriana.dw.biblioteca.entity.Libro;
import co.javeriana.dw.biblioteca.entity.Prestamo;
import co.javeriana.dw.biblioteca.exception.NegocioException;
import co.javeriana.dw.biblioteca.repository.EstudianteRepository;
import co.javeriana.dw.biblioteca.repository.LibroRepository;
import co.javeriana.dw.biblioteca.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private final LibroRepository libroRepository;
    private final EstudianteRepository estudianteRepository;
    private final PrestamoRepository prestamoRepository;

    /** Días de plazo para devolver un ejemplar (configurable vía properties). */
    @Value("${prestamo.dias-plazo:14}")
    private int diasPlazo;

    public PrestamoService(LibroRepository libroRepository, EstudianteRepository estudianteRepository, PrestamoRepository prestamoRepository) {
        this.libroRepository = libroRepository;
        this.estudianteRepository = estudianteRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional(readOnly = true)
    public List<Prestamo> listar() {
        return prestamoRepository.findAllByOrderByFechaPrestamoDesc();
    }

    /**
     * Flujo principal: valida disponibilidad y mora del estudiante,
     * descuenta un ejemplar y calcula la fecha límite de devolución.
     */
    @Transactional
    public Prestamo solicitarPrestamo(Long libroId, Long estudianteId) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new NegocioException("El libro seleccionado no existe"));
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new NegocioException("El estudiante seleccionado no existe"));

        if (libro.getCantidadDisponible() <= 0) {
            throw new NegocioException("No quedan ejemplares disponibles de «" + libro.getTitulo() + "»");
        }

        LocalDate hoy = LocalDate.now();
        boolean tieneVencidos = prestamoRepository.existsByEstudianteAndEstado(estudiante, EstadoPrestamo.VENCIDO)
                || prestamoRepository.existsByEstudianteAndEstadoAndFechaLimiteBefore(estudiante, EstadoPrestamo.ACTIVO, hoy);
        if (tieneVencidos) {
            throw new NegocioException("El estudiante " + estudiante.getNombre() + " tiene préstamos vencidos y no puede solicitar más");
        }

        boolean yaPrestada = prestamoRepository.existsByEstudianteAndLibroAndEstadoIn(
                estudiante, libro, List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO));
        if (yaPrestada) {
            throw new NegocioException("El estudiante ya tiene un ejemplar de «" + libro.getTitulo() + "» prestado sin devolver");
        }

        libro.prestar();
        Prestamo prestamo = new Prestamo(libro, estudiante, hoy, diasPlazo);
        return prestamoRepository.save(prestamo);
    }

    /**
     * Flujo de devolución: calcula la mora según la fecha límite
     * y devuelve el ejemplar al inventario del libro.
     */
    @Transactional
    public Prestamo devolver(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new NegocioException("El préstamo no existe"));

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new NegocioException("Este préstamo ya fue devuelto");
        }

        LocalDate hoy = LocalDate.now();
        prestamo.setFechaDevolucion(hoy);
        prestamo.setMoraDias((int) prestamo.calcularMoraDias(hoy));
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.getLibro().devolver();
        return prestamoRepository.save(prestamo);
    }

    /** Tarea programada: marca como VENCIDO los préstamos activos cuya fecha límite ya pasó. */
    @Scheduled(cron = "${prestamo.cron-vencidos:0 * * * * ?}")
    @Transactional
    public int marcarVencidos() {
        LocalDate hoy = LocalDate.now();
        List<Prestamo> vencidos = prestamoRepository.findByEstadoAndFechaLimiteBefore(EstadoPrestamo.ACTIVO, hoy);
        vencidos.forEach(p -> p.setEstado(EstadoPrestamo.VENCIDO));
        if (!vencidos.isEmpty()) {
            System.out.println("[biblioteca] " + vencidos.size() + " préstamo(s) marcado(s) como vencidos");
        }
        return vencidos.size();
    }
}
