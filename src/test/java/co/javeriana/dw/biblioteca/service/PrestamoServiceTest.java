package co.javeriana.dw.biblioteca.service;

import co.javeriana.dw.biblioteca.entity.EstadoPrestamo;
import co.javeriana.dw.biblioteca.entity.Estudiante;
import co.javeriana.dw.biblioteca.entity.Libro;
import co.javeriana.dw.biblioteca.entity.Prestamo;
import co.javeriana.dw.biblioteca.exception.NegocioException;
import co.javeriana.dw.biblioteca.repository.EstudianteRepository;
import co.javeriana.dw.biblioteca.repository.LibroRepository;
import co.javeriana.dw.biblioteca.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private LibroRepository libroRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private PrestamoService prestamoService;

    private Libro libro;
    private Estudiante estudiante;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(prestamoService, "diasPlazo", 14);

        libro = new Libro();
        libro.setId(1L);
        libro.setTitulo("Clean Code");
        libro.setAutor("Robert C. Martin");
        libro.setIsbn("9780132350884");
        libro.setCantidadTotal(3);
        libro.setCantidadDisponible(2);

        estudiante = new Estudiante();
        estudiante.setId(1L);
        estudiante.setNombre("Ana Torres");
        estudiante.setCodigo("20211001");
        estudiante.setCorreo("ana@uni.edu");
    }

    @Test
    void solicitarPrestamo_descuentoEjemplarYCalculaFechaLimite() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(prestamoRepository.existsByEstudianteAndEstado(estudiante, EstadoPrestamo.VENCIDO)).thenReturn(false);
        when(prestamoRepository.existsByEstudianteAndEstadoAndFechaLimiteBefore(eq(estudiante), eq(EstadoPrestamo.ACTIVO), any(LocalDate.class))).thenReturn(false);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo prestamo = prestamoService.solicitarPrestamo(1L, 1L);

        assertEquals(1, libro.getCantidadDisponible());
        assertEquals(EstadoPrestamo.ACTIVO, prestamo.getEstado());
        assertEquals(LocalDate.now(), prestamo.getFechaPrestamo());
        assertEquals(LocalDate.now().plusDays(14), prestamo.getFechaLimite());
        verify(prestamoRepository).save(any(Prestamo.class));
    }

    @Test
    void solicitarPrestamo_sinEjemplares_lanzaNegocioException() {
        libro.setCantidadDisponible(0);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));

        assertThrows(NegocioException.class, () -> prestamoService.solicitarPrestamo(1L, 1L));
        verify(prestamoRepository, never()).save(any(Prestamo.class));
    }

    @Test
    void solicitarPrestamo_estudianteConVencidos_lanzaNegocioException() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(prestamoRepository.existsByEstudianteAndEstado(estudiante, EstadoPrestamo.VENCIDO)).thenReturn(true);

        assertThrows(NegocioException.class, () -> prestamoService.solicitarPrestamo(1L, 1L));
        verify(prestamoRepository, never()).save(any(Prestamo.class));
    }

    @Test
    void solicitarPrestamo_activoVencidoPorFecha_lanzaNegocioException() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(prestamoRepository.existsByEstudianteAndEstado(estudiante, EstadoPrestamo.VENCIDO)).thenReturn(false);
        when(prestamoRepository.existsByEstudianteAndEstadoAndFechaLimiteBefore(eq(estudiante), eq(EstadoPrestamo.ACTIVO), any(LocalDate.class))).thenReturn(true);

        assertThrows(NegocioException.class, () -> prestamoService.solicitarPrestamo(1L, 1L));
    }

    @Test
    void solicitarPrestamo_estudianteYaTieneCopiaDelMismoLibro_lanzaNegocioException() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(prestamoRepository.existsByEstudianteAndEstado(estudiante, EstadoPrestamo.VENCIDO)).thenReturn(false);
        when(prestamoRepository.existsByEstudianteAndEstadoAndFechaLimiteBefore(eq(estudiante), eq(EstadoPrestamo.ACTIVO), any(LocalDate.class))).thenReturn(false);
        when(prestamoRepository.existsByEstudianteAndLibroAndEstadoIn(eq(estudiante), eq(libro), any())).thenReturn(true);

        assertThrows(NegocioException.class, () -> prestamoService.solicitarPrestamo(1L, 1L));
        verify(prestamoRepository, never()).save(any(Prestamo.class));
    }

    @Test
    void solicitarPrestamo_trasDevolverCopiaDelMismoLibro_sePermite() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(prestamoRepository.existsByEstudianteAndEstado(estudiante, EstadoPrestamo.VENCIDO)).thenReturn(false);
        when(prestamoRepository.existsByEstudianteAndEstadoAndFechaLimiteBefore(eq(estudiante), eq(EstadoPrestamo.ACTIVO), any(LocalDate.class))).thenReturn(false);
        when(prestamoRepository.existsByEstudianteAndLibroAndEstadoIn(eq(estudiante), eq(libro), any())).thenReturn(false);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo prestamo = prestamoService.solicitarPrestamo(1L, 1L);

        assertEquals(EstadoPrestamo.ACTIVO, prestamo.getEstado());
        verify(prestamoRepository).save(any(Prestamo.class));
    }

    @Test
    void devolver_aTiempo_restauraDisponibilidadSinMora() {
        libro.setCantidadDisponible(1);
        Prestamo prestamo = new Prestamo(libro, estudiante, LocalDate.now().minusDays(5), 14);
        prestamo.setId(1L);
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo resultado = prestamoService.devolver(1L);

        assertEquals(EstadoPrestamo.DEVUELTO, resultado.getEstado());
        assertEquals(0, resultado.getMoraDias());
        assertEquals(LocalDate.now(), resultado.getFechaDevolucion());
        assertEquals(2, libro.getCantidadDisponible());
        verify(prestamoRepository).save(prestamo);
    }

    @Test
    void devolver_conMora_calculaDias() {
        Prestamo prestamo = new Prestamo(libro, estudiante, LocalDate.now().minusDays(10), 7);
        prestamo.setId(1L);
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prestamo resultado = prestamoService.devolver(1L);

        assertEquals(3, resultado.getMoraDias());
        assertEquals(EstadoPrestamo.DEVUELTO, resultado.getEstado());
    }

    @Test
    void devolver_yaDevuelto_lanzaNegocioException() {
        Prestamo prestamo = new Prestamo(libro, estudiante, LocalDate.now().minusDays(5), 14);
        prestamo.setId(1L);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));

        assertThrows(NegocioException.class, () -> prestamoService.devolver(1L));
        verify(prestamoRepository, never()).save(any(Prestamo.class));
    }

    @Test
    void devolver_noSuperaCantidadTotal() {
        libro.setCantidadTotal(2);
        libro.setCantidadDisponible(2);
        Prestamo prestamo = new Prestamo(libro, estudiante, LocalDate.now().minusDays(5), 14);
        prestamo.setId(1L);
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        prestamoService.devolver(1L);

        assertEquals(2, libro.getCantidadDisponible()); // nunca pasa del total registrado
    }

    @Test
    void marcarVencidos_actualizaSoloActivosVencidos() {
        Prestamo vencido = new Prestamo(libro, estudiante, LocalDate.now().minusDays(20), 14);
        vencido.setId(1L);
        Prestamo alDia = new Prestamo(libro, estudiante, LocalDate.now(), 14);
        alDia.setId(2L);
        when(prestamoRepository.findByEstadoAndFechaLimiteBefore(EstadoPrestamo.ACTIVO, LocalDate.now()))
                .thenReturn(List.of(vencido));

        int marcados = prestamoService.marcarVencidos();

        assertEquals(1, marcados);
        assertEquals(EstadoPrestamo.VENCIDO, vencido.getEstado());
        assertEquals(EstadoPrestamo.ACTIVO, alDia.getEstado());
    }
}
