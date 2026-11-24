package sgt.proyecto.sgtuconstruccion.modelo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Turno del sistema. Modelo mínimo pensado para la persistencia; la lógica de negocio
 * (FIFO, transiciones de estado) vive en otra capa y puede ampliar esta clase.
 *
 * <p>Es inmutable: para cambiar de estado se usa {@link #conEstado}.
 */
public final class Turno {

    private static final Pattern ID_VALIDO = Pattern.compile("[A-Za-z0-9_-]{1,20}");
    private static final Pattern VENTANILLA_VALIDA = Pattern.compile("V0[1-3]");

    private final String id;
    private final long secuencia;
    private final LocalDateTime fechaRegistro;
    private final EstadoTurno estado;
    private final String ventanilla;

    /**
     * @param ventanilla {@code V01}, {@code V02} o {@code V03}; obligatoria en
     *                   EN_ATENCION y ATENDIDO, y debe ser {@code null} en EN_ESPERA y CANCELADO
     * @throws IllegalArgumentException si algún dato es inválido o inconsistente
     */
    public Turno(String id, long secuencia, LocalDateTime fechaRegistro,
                 EstadoTurno estado, String ventanilla) {
        if (id == null || !ID_VALIDO.matcher(id).matches()) {
            throw new IllegalArgumentException(
                    "ID de turno inválido (1 a 20 caracteres: letras, números, '-' o '_'): " + id);
        }
        if (secuencia <= 0) {
            throw new IllegalArgumentException("La secuencia debe ser positiva: " + secuencia);
        }
        if (fechaRegistro == null) {
            throw new IllegalArgumentException("La fecha de registro es obligatoria (turno " + id + ")");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio (turno " + id + ")");
        }
        boolean requiereVentanilla = estado == EstadoTurno.EN_ATENCION || estado == EstadoTurno.ATENDIDO;
        if (requiereVentanilla && ventanilla == null) {
            throw new IllegalArgumentException(
                    "El turno " + id + " en estado " + estado + " requiere ventanilla");
        }
        if (!requiereVentanilla && ventanilla != null) {
            throw new IllegalArgumentException(
                    "El turno " + id + " en estado " + estado + " no puede tener ventanilla");
        }
        if (ventanilla != null && !VENTANILLA_VALIDA.matcher(ventanilla).matches()) {
            throw new IllegalArgumentException(
                    "Ventanilla inválida para el turno " + id + ": " + ventanilla);
        }
        this.id = id;
        this.secuencia = secuencia;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
        this.ventanilla = ventanilla;
    }

    /** Devuelve una copia del turno con otro estado y ventanilla. */
    public Turno conEstado(EstadoTurno nuevoEstado, String nuevaVentanilla) {
        return new Turno(id, secuencia, fechaRegistro, nuevoEstado, nuevaVentanilla);
    }

    public String getId() { return id; }
    public long getSecuencia() { return secuencia; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public EstadoTurno getEstado() { return estado; }
    /** @return la ventanilla asignada o {@code null} si no tiene. */
    public String getVentanilla() { return ventanilla; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Turno)) return false;
        Turno otro = (Turno) o;
        return secuencia == otro.secuencia
                && id.equals(otro.id)
                && fechaRegistro.equals(otro.fechaRegistro)
                && estado == otro.estado
                && Objects.equals(ventanilla, otro.ventanilla);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, secuencia, fechaRegistro, estado, ventanilla);
    }

    @Override
    public String toString() {
        return "Turno{" + id + ", sec=" + secuencia + ", " + fechaRegistro
                + ", " + estado + ", " + (ventanilla == null ? "-" : ventanilla) + "}";
    }
}
