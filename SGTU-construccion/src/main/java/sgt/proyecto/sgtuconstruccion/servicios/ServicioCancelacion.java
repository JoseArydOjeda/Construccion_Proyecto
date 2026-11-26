package sgt.proyecto.sgtuconstruccion.servicios;

import sgt.proyecto.sgtuconstruccion.modelos.Turno;
import java.io.IOException;
import java.util.List;

/** Caso de uso para cancelar turnos mediante el servicio de turnos. */
public class ServicioCancelacion {
    private final ServicioTurnos servicioTurnos;

    public ServicioCancelacion(ServicioTurnos servicioTurnos) {
        if (servicioTurnos == null) throw new IllegalArgumentException("El servicio de turnos es obligatorio");
        this.servicioTurnos = servicioTurnos;
    }

    public void cancelar(Turno turno) throws IOException {
        servicioTurnos.cancelarTurno(turno);
    }

    public List<Turno> consultarCancelados() {
        return servicioTurnos.consultarCancelados();
    }
}
