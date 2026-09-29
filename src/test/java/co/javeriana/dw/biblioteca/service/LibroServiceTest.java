package co.javeriana.dw.biblioteca.service;

import co.javeriana.dw.biblioteca.entity.Libro;
import co.javeriana.dw.biblioteca.exception.NegocioException;
import co.javeriana.dw.biblioteca.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroService libroService;

    @Test
    void crear_isbnDuplicado_lanzaNegocioException() {
        Libro libro = new Libro();
        libro.setTitulo("Clean Code");
        libro.setAutor("Robert C. Martin");
        libro.setIsbn("9780132350884");
        libro.setCantidadTotal(2);

        when(libroRepository.existsByIsbn("9780132350884")).thenReturn(true);

        assertThrows(NegocioException.class, () -> libroService.crear(libro));
        verify(libroRepository, never()).save(any(Libro.class));
    }

    @Test
    void crear_isbnNuevo_dejaTodosLosEjemplaresDisponibles() {
        Libro libro = new Libro();
        libro.setTitulo("Clean Code");
        libro.setAutor("Robert C. Martin");
        libro.setIsbn("9780132350884");
        libro.setCantidadTotal(3);

        when(libroRepository.existsByIsbn("9780132350884")).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Libro guardado = libroService.crear(libro);

        assertEquals(3, guardado.getCantidadDisponible());
        verify(libroRepository).save(libro);
    }
}
