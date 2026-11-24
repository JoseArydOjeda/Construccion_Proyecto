package sgt.proyecto.sgtuconstruccion.modelos;

import java.time.LocalDateTime;

public final class ErrorRegistrado {
    private final LocalDateTime fechaHora;
    private final String mensaje;
    private final String detalle;

    public ErrorRegistrado(LocalDateTime fechaHora, String mensaje, String detalle) {
        this.fechaHora = fechaHora;
        this.mensaje = mensaje;
        this.detalle = detalle;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getDetalle() {
        return detalle;
    }
}
