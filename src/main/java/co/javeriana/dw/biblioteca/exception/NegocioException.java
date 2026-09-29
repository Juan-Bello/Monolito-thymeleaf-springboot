package co.javeriana.dw.biblioteca.exception;

/**
 * Violación de una regla de negocio (disponibilidad, mora, registros duplicados).
 * El controlador la captura y la muestra en la vista.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
