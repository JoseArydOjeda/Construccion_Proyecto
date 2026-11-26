package sgt.proyecto.sgtuconstruccion.servicios;

import sgt.proyecto.sgtuconstruccion.modelos.Turno;
import sgt.proyecto.sgtuconstruccion.modelos.Ventanilla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Administra el estado y la asignación manual de las ventanillas. */
public class ServicioVentanillas {
    private final List<Ventanilla> ventanillas = new ArrayList<>();

    public ServicioVentanillas() {
        for (int id = 1; id <= 3; id++) {
            Ventanilla ventanilla = new Ventanilla();
            ventanilla.setId(id);
            ventanilla.setEstado("LIBRE");
            ventanillas.add(ventanilla);
        }
    }

    public synchronized void asignar(Turno turno, int ventanillaId) {
        if (turno == null) {
            throw new IllegalArgumentException("El turno es obligatorio");
        }
        validarDisponible(ventanillaId);
        Ventanilla ventanilla = buscar(ventanillaId);
        if (turno.getVentanilla() != null || !"EN_ESPERA".equals(turno.getEstado())) {
            throw new IllegalStateException("El turno no está disponible para asignación");
        }

        turno.setVentanilla(ventanilla);
        turno.setEstado("EN_ATENCION");
        ventanilla.setTurnoActual(turno);
        ventanilla.setEstado("OCUPADA");
    }

    public synchronized void validarDisponible(int ventanillaId) {
        Ventanilla ventanilla = buscar(ventanillaId);
        if (ventanilla == null) {
            throw new IllegalArgumentException("No existe la ventanilla " + ventanillaId);
        }
        if (!"LIBRE".equals(ventanilla.getEstado())) {
            throw new IllegalStateException("La ventanilla " + ventanillaId + " está ocupada");
        }
    }

    public synchronized void liberar(Turno turno) {
        if (turno == null) {
            return;
        }
        Ventanilla ventanilla = turno.getVentanilla();
        if (ventanilla != null && ventanilla.getTurnoActual() == turno) {
            ventanilla.setTurnoActual(null);
            ventanilla.setEstado("LIBRE");
        }
    }

    public synchronized List<Ventanilla> consultarVentanillas() {
        return Collections.unmodifiableList(new ArrayList<>(ventanillas));
    }

    private Ventanilla buscar(int id) {
        for (Ventanilla ventanilla : ventanillas) {
            if (ventanilla.getId() == id) {
                return ventanilla;
            }
        }
        return null;
    }
}
