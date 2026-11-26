package sgt.proyecto.sgtuconstruccion.servicios;

import sgt.proyecto.sgtuconstruccion.modelos.Turno;
import sgt.proyecto.sgtuconstruccion.modelos.Ventanilla;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/** Administra los turnos pendientes, las tres ventanillas y los archivos CSV. */
public class ServicioTurnos {
    private final Queue<Turno> cola = new ArrayDeque<>();
    private final List<Turno> terminados = new ArrayList<>();
    private final ServicioVentanillas servicioVentanillas;
    private final Path archivoPendientes;
    private final Path archivoTerminados;
    private final Path archivoCancelados;
    private int siguienteId = 1;
    private int siguienteSecuencia = 1;
    private final Random random = new Random();
    private final Set<Integer> atencionesIniciadas = new HashSet<>();
    private final List<Turno> cancelados = new ArrayList<>();

    public ServicioTurnos(Path directorioDatos) throws IOException {
        this(directorioDatos, new ServicioVentanillas());
    }

    public ServicioTurnos(Path directorioDatos, ServicioVentanillas servicioVentanillas) throws IOException {
        if (directorioDatos == null) {
            throw new IllegalArgumentException("El directorio de datos es obligatorio");
        }
        if (servicioVentanillas == null) {
            throw new IllegalArgumentException("El servicio de ventanillas es obligatorio");
        }
        this.servicioVentanillas = servicioVentanillas;
        Files.createDirectories(directorioDatos);
        archivoPendientes = directorioDatos.resolve("turnos_pendientes.csv");
        archivoTerminados = directorioDatos.resolve("turnos_terminados.csv");
        archivoCancelados = directorioDatos.resolve("turnos_cancelados.csv");
        guardarArchivos();
    }

    public synchronized Turno registrarTurno() throws IOException {
        Turno turno = new Turno();
        turno.setId(siguienteId++);
        turno.setSecuencia(siguienteSecuencia++);
        turno.setFechaHoraRegistro(LocalDateTime.now());
        turno.setEstado("EN_ESPERA");
        cola.add(turno);
        guardarArchivos();
        return turno;
    }

    /** Asigna el turno más antiguo en espera a la ventanilla elegida. */
    public synchronized Turno asignarTurno(int ventanillaId) throws IOException {
        servicioVentanillas.validarDisponible(ventanillaId);
        Turno turno = siguienteSinAsignar();
        if (turno == null) return null;
        servicioVentanillas.asignar(turno, ventanillaId);
        guardarArchivos();
        iniciarAtenciones();
        return turno;
    }

    /** Inicia la atención de los turnos asignados; cada atención dura de 1 a 400 segundos. */
    public void iniciarAtenciones() {
        for (Ventanilla v : consultarVentanillas()) {
            Turno asignado = v.getTurnoActual();
            synchronized (this) {
                if (asignado != null && atencionesIniciadas.add(asignado.getId())) {
                    int segundos = random.nextInt(400) + 1;
                    programarFinalizacion(asignado, segundos);
                }
            }
        }
    }

    void programarFinalizacion(Turno turno, int segundos) {
        Thread hilo = new Thread(() -> {
            try {
                Thread.sleep(segundos * 1000L);
                finalizarTurno(turno);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException("No se pudo guardar el turno terminado", e);
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    public synchronized void finalizarTurno(Turno turno) throws IOException {
        if (turno == null || !cola.contains(turno) || turno.getVentanilla() == null) return;
        if (turno.getVentanilla().getTurnoActual() != turno) return;
        cola.remove(turno);
        atencionesIniciadas.remove(turno.getId());
        turno.setEstado("TERMINADO");
        terminados.add(turno);
        servicioVentanillas.liberar(turno);
        guardarArchivos();
    }

    /** Cancela un turno en espera o en atención y deja libre su ventanilla. */
    public synchronized void cancelarTurno(Turno turno) throws IOException {
        if (turno == null || !cola.contains(turno)) {
            throw new IllegalArgumentException("El turno no está registrado o ya fue cerrado");
        }
        cola.remove(turno);
        atencionesIniciadas.remove(turno.getId());
        servicioVentanillas.liberar(turno);
        turno.setEstado("CANCELADO");
        cancelados.add(turno);
        guardarArchivos();
    }

    public synchronized List<Turno> consultarCola() {
        return Collections.unmodifiableList(new ArrayList<>(cola));
    }

    public synchronized List<Ventanilla> consultarVentanillas() {
        return servicioVentanillas.consultarVentanillas();
    }

    public synchronized List<Turno> consultarTerminados() {
        return Collections.unmodifiableList(new ArrayList<>(terminados));
    }

    public synchronized List<Turno> consultarCancelados() {
        return Collections.unmodifiableList(new ArrayList<>(cancelados));
    }

    private Turno siguienteSinAsignar() {
        for (Turno turno : cola) {
            if (turno.getVentanilla() == null) return turno;
        }
        return null;
    }

    private void guardarArchivos() throws IOException {
        escribir(archivoPendientes, cola);
        escribir(archivoTerminados, terminados);
        escribir(archivoCancelados, cancelados);
    }

    private void escribir(Path archivo, Iterable<Turno> turnos) throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add("id,secuencia,fechaHoraRegistro,estado,ventanillaId");
        for (Turno t : turnos) {
            int ventanillaId = t.getVentanilla() == null ? 0 : t.getVentanilla().getId();
            lineas.add(t.getId() + "," + t.getSecuencia() + "," + t.getFechaHoraRegistro() + "," + t.getEstado() + "," + ventanillaId);
        }
        Files.write(archivo, lineas, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
