package sgt.proyecto.sgtuconstruccion.servicios;

import sgt.proyecto.sgtuconstruccion.modelos.ErrorRegistrado;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Registra retroalimentación de errores para que la aplicación pueda mostrarla o revisarla. */
public class ServicioErrores {
    private final List<ErrorRegistrado> errores = new ArrayList<>();

    public synchronized ErrorRegistrado registrarError(String mensaje, Throwable causa) {
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El mensaje del error no puede estar vacío");
        }
        String detalle = causa == null ? null : causa.getMessage();
        ErrorRegistrado error = new ErrorRegistrado(LocalDateTime.now(), mensaje, detalle);
        errores.add(error);
        return error;
    }

    public synchronized List<ErrorRegistrado> consultarErrores() {
        return Collections.unmodifiableList(new ArrayList<>(errores));
    }
}
