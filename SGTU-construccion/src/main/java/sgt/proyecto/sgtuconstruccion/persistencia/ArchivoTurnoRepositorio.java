package sgt.proyecto.sgtuconstruccion.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sgt.proyecto.sgtuconstruccion.modelo.EstadoTurno;
import sgt.proyecto.sgtuconstruccion.modelo.Turno;

/**
 * Guarda los turnos en un archivo CSV local (RD-01), sin dependencias externas.
 *
 * <p>Formato (UTF-8, una línea por turno, con encabezado):
 * <pre>
 * id,secuencia,fechaRegistro,estado,ventanilla
 * T001,1,2026-11-23T18:55,EN_ATENCION,V02
 * T002,2,2026-11-23T18:56:10,EN_ESPERA,
 * </pre>
 *
 * <p>Garantías:
 * <ul>
 *   <li>Guardado atómico: se escribe a un archivo temporal y luego se reemplaza el original,
 *       así nunca queda un archivo a medias (RD-02).</li>
 *   <li>Si el archivo existe pero es ilegible o inválido, {@link #cargar()} lanza error y
 *       no modifica el archivo (RD-03).</li>
 * </ul>
 */
public class ArchivoTurnoRepositorio implements TurnoRepositorio {

    static final String ENCABEZADO = "id,secuencia,fechaRegistro,estado,ventanilla";
    private static final int COLUMNAS = 5;

    private final Path archivo;

    /** Usa el archivo indicado; se crea (con sus carpetas) al guardar por primera vez. */
    public ArchivoTurnoRepositorio(Path archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("La ruta del archivo es obligatoria");
        }
        this.archivo = archivo;
    }

    /** Ruta por defecto: {@code data/turnos.csv}, relativa a donde se ejecuta la aplicación. */
    public static Path rutaPorDefecto() {
        return Path.of("data", "turnos.csv");
    }

    @Override
    public List<Turno> cargar() throws PersistenciaException {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        List<String> lineas;
        try {
            lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo de datos " + archivo
                    + ". No se modificó: " + e.getMessage(), e);
        }
        if (lineas.isEmpty() || !ENCABEZADO.equals(lineas.get(0).stripTrailing())) {
            throw new PersistenciaException("El archivo " + archivo
                    + " no tiene el formato esperado (falta el encabezado). No se modificó.");
        }
        List<Turno> turnos = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue;
            }
            turnos.add(leerLinea(linea, i + 1));
        }
        validarConsistencia(turnos);
        return turnos;
    }

    @Override
    public void guardar(List<Turno> turnos) throws PersistenciaException {
        if (turnos == null) {
            throw new PersistenciaException("La lista de turnos es obligatoria");
        }
        validarConsistencia(turnos);

        List<String> lineas = new ArrayList<>();
        lineas.add(ENCABEZADO);
        for (Turno t : turnos) {
            lineas.add(String.join(",",
                    t.getId(),
                    Long.toString(t.getSecuencia()),
                    t.getFechaRegistro().toString(),
                    t.getEstado().name(),
                    t.getVentanilla() == null ? "" : t.getVentanilla()));
        }

        Path temporal = null;
        try {
            Path carpeta = archivo.toAbsolutePath().getParent();
            Files.createDirectories(carpeta);
            temporal = Files.createTempFile(carpeta, "turnos-", ".tmp");
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            reemplazar(temporal, archivo);
        } catch (IOException e) {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException ignorada) {
                    // El error relevante es el original.
                }
            }
            throw new PersistenciaException("No se pudieron guardar los datos en " + archivo
                    + ": " + e.getMessage(), e);
        }
    }

    private static void reemplazar(Path origen, Path destino) throws IOException {
        try {
            Files.move(origen, destino,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Turno leerLinea(String linea, int numeroLinea) throws PersistenciaException {
        String[] campos = linea.split(",", -1);
        if (campos.length != COLUMNAS) {
            throw errorEnLinea(numeroLinea, "se esperaban " + COLUMNAS + " columnas");
        }
        try {
            long secuencia = Long.parseLong(campos[1].trim());
            LocalDateTime fecha = LocalDateTime.parse(campos[2].trim());
            EstadoTurno estado = EstadoTurno.valueOf(campos[3].trim());
            String ventanilla = campos[4].isBlank() ? null : campos[4].trim();
            return new Turno(campos[0].trim(), secuencia, fecha, estado, ventanilla);
        } catch (NumberFormatException e) {
            throw errorEnLinea(numeroLinea, "la secuencia no es un número");
        } catch (DateTimeParseException e) {
            throw errorEnLinea(numeroLinea, "la fecha no tiene formato válido");
        } catch (IllegalArgumentException e) {
            throw errorEnLinea(numeroLinea, e.getMessage());
        }
    }

    private PersistenciaException errorEnLinea(int numeroLinea, String detalle) {
        return new PersistenciaException("Datos inválidos en " + archivo + ", línea "
                + numeroLinea + ": " + detalle + ". El archivo no se modificó.");
    }

    /** Revisa unicidad de ID y secuencia (RF-02) y una sola atención por ventanilla (RN-04). */
    private void validarConsistencia(List<Turno> turnos) throws PersistenciaException {
        Set<String> ids = new HashSet<>();
        Set<Long> secuencias = new HashSet<>();
        Set<String> ventanillasOcupadas = new HashSet<>();
        for (Turno t : turnos) {
            if (!ids.add(t.getId())) {
                throw new PersistenciaException("ID de turno repetido: " + t.getId());
            }
            if (!secuencias.add(t.getSecuencia())) {
                throw new PersistenciaException("Secuencia repetida: " + t.getSecuencia()
                        + " (turno " + t.getId() + ")");
            }
            if (t.getEstado() == EstadoTurno.EN_ATENCION
                    && !ventanillasOcupadas.add(t.getVentanilla())) {
                throw new PersistenciaException("La ventanilla " + t.getVentanilla()
                        + " tiene más de un turno en atención (turno " + t.getId() + ")");
            }
        }
    }
}
