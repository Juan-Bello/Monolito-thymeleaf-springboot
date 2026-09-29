package co.javeriana.dw.biblioteca.controller;

/** Backing object del formulario de solicitud de préstamo (Thymeleaf). */
public class PrestamoForm {

    private Long libroId;
    private Long estudianteId;

    public PrestamoForm() {
    }

    public PrestamoForm(Long libroId, Long estudianteId) {
        this.libroId = libroId;
        this.estudianteId = estudianteId;
    }

    public Long getLibroId() {
        return libroId;
    }

    public void setLibroId(Long libroId) {
        this.libroId = libroId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }
}
