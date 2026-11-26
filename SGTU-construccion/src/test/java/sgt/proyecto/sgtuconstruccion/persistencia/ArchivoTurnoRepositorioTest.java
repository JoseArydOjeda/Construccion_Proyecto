package sgt.proyecto.sgtuconstruccion.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.nio.file.Path;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sgt.proyecto.sgtuconstruccion.modelo.EstadoTurno;
import sgt.proyecto.sgtuconstruccion.modelo.Turno;

class ArchivoTurnoRepositorioTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 23, 18, 55, 0);

    @TempDir
    Path carpeta;

    private Path archivo;
    private ArchivoTurnoRepositorio repo;

    @BeforeEach
    void preparar() {
        archivo = carpeta.resolve("datos").resolve("turnos.csv");
        repo = new ArchivoTurnoRepositorio(archivo);
    }

    private static List<Turno> cuatroEstados() {
        return List.of(
                new Turno("T001", 1, FECHA, EstadoTurno.ATENDIDO, "V01"),
                new Turno("T002", 2, FECHA.plusMinutes(1), EstadoTurno.EN_ATENCION, "V02"),
                new Turno("T003", 3, FECHA.plusMinutes(2), EstadoTurno.EN_ESPERA, null),
                new Turno("T004", 4, FECHA.plusMinutes(3), EstadoTurno.CANCELADO, null));
    }

    @Test
    void sinArchivoDevuelveListaVacia() throws PersistenciaException {
        assertTrue(repo.cargar().isEmpty());
    }

    @Test
    void guardarYCargarConservaTodosLosDatos() throws PersistenciaException {
        List<Turno> originales = cuatroEstados();
        repo.guardar(originales);
        assertEquals(originales, repo.cargar());
    }

    @Test
    void guardarReemplazaElContenidoAnterior() throws PersistenciaException {
        repo.guardar(cuatroEstados());
        List<Turno> nuevos = List.of(new Turno("T009", 9, FECHA, EstadoTurno.EN_ESPERA, null));
        repo.guardar(nuevos);
        assertEquals(nuevos, repo.cargar());
    }

    @Test
    void guardarNoDejaArchivosTemporales() throws PersistenciaException, IOException {
        repo.guardar(cuatroEstados());
        try (Stream<Path> archivos = Files.list(archivo.getParent())) {
            assertEquals(List.of("turnos.csv"),
                    archivos.map(p -> p.getFileName().toString()).collect(Collectors.toList()));
        }
    }

    @Test
    void archivoInvalidoLanzaErrorYNoSeModifica() throws IOException {
        Files.createDirectories(archivo.getParent());
        String basura = "esto no es un csv de turnos\n";
        Files.writeString(archivo, basura, StandardCharsets.UTF_8);

        assertThrows(PersistenciaException.class, () -> repo.cargar());
        assertEquals(basura, Files.readString(archivo, StandardCharsets.UTF_8));
    }

    @Test
    void lineaCorruptaIndicaElNumeroDeLinea() throws IOException {
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, List.of(
                ArchivoTurnoRepositorio.ENCABEZADO,
                "T001,1,2026-11-23T18:55:00,EN_ESPERA,",
                "T002,abc,2026-11-23T18:56:00,EN_ESPERA,"), StandardCharsets.UTF_8);

        PersistenciaException e = assertThrows(PersistenciaException.class, () -> repo.cargar());
        assertTrue(e.getMessage().contains("línea 3"), e.getMessage());
    }

    @Test
    void estadoDesconocidoEsRechazado() throws IOException {
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, List.of(
                ArchivoTurnoRepositorio.ENCABEZADO,
                "T001,1,2026-11-23T18:55:00,FLOTANDO,"), StandardCharsets.UTF_8);
        assertThrows(PersistenciaException.class, () -> repo.cargar());
    }

    @Test
    void rechazaDosTurnosEnAtencionEnLaMismaVentanilla() {
        List<Turno> invalidos = List.of(
                new Turno("T001", 1, FECHA, EstadoTurno.EN_ATENCION, "V01"),
                new Turno("T002", 2, FECHA, EstadoTurno.EN_ATENCION, "V01"));
        assertThrows(PersistenciaException.class, () -> repo.guardar(invalidos));
    }

    @Test
    void rechazaIdsOSecuenciasRepetidas() {
        assertThrows(PersistenciaException.class, () -> repo.guardar(List.of(
                new Turno("T001", 1, FECHA, EstadoTurno.EN_ESPERA, null),
                new Turno("T001", 2, FECHA, EstadoTurno.EN_ESPERA, null))));
        assertThrows(PersistenciaException.class, () -> repo.guardar(List.of(
                new Turno("T001", 1, FECHA, EstadoTurno.EN_ESPERA, null),
                new Turno("T002", 1, FECHA, EstadoTurno.EN_ESPERA, null))));
    }

    @Test
    void guardadoInvalidoNoAlteraLosDatosExistentes() throws PersistenciaException {
        List<Turno> originales = cuatroEstados();
        repo.guardar(originales);

        List<Turno> invalidos = List.of(
                new Turno("T001", 1, FECHA, EstadoTurno.EN_ATENCION, "V01"),
                new Turno("T002", 2, FECHA, EstadoTurno.EN_ATENCION, "V01"));
        assertThrows(PersistenciaException.class, () -> repo.guardar(invalidos));

        assertEquals(originales, repo.cargar());
    }

    @Test
    void fallaDeEscrituraLanzaErrorYConservaElOriginal() throws PersistenciaException, IOException {
        List<Turno> originales = cuatroEstados();
        repo.guardar(originales);

        // Un archivo en el lugar de la carpeta hace imposible crear el temporal en otra ruta.
        Path bloqueo = carpeta.resolve("bloqueo");
        Files.writeString(bloqueo, "x");
        ArchivoTurnoRepositorio imposible =
                new ArchivoTurnoRepositorio(bloqueo.resolve("turnos.csv"));
        assertThrows(PersistenciaException.class, () -> imposible.guardar(originales));

        assertEquals(originales, repo.cargar());
    }
}
