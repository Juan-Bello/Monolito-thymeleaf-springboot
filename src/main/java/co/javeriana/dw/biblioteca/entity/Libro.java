package co.javeriana.dw.biblioteca.entity;

import co.javeriana.dw.biblioteca.exception.NegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Column(nullable = false)
    private String autor;

    @NotBlank(message = "El ISBN es obligatorio")
    @Column(nullable = false, unique = true)
    private String isbn;

    @Min(value = 1, message = "Debe registrar al menos un ejemplar")
    @Column(nullable = false)
    private int cantidadTotal;

    @Column(nullable = false)
    private int cantidadDisponible;

    public Libro() {
    }

    /** Descuenta un ejemplar disponible (regla de negocio). */
    public void prestar() {
        if (cantidadDisponible <= 0) {
            throw new NegocioException("No quedan ejemplares disponibles de «" + titulo + "»");
        }
        cantidadDisponible--;
    }

    /** Devuelve un ejemplar al inventario, sin pasar del total registrado. */
    public void devolver() {
        cantidadDisponible = Math.min(cantidadTotal, cantidadDisponible + 1);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public void setCantidadTotal(int cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }
}
