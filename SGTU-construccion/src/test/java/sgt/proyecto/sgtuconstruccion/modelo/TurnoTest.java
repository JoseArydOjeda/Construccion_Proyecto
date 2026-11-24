package sgt.proyecto.sgtuconstruccion.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TurnoTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 23, 18, 55);

    @Test
    void rechazaIdConComaOSaltoDeLinea() {
        assertThrows(IllegalArgumentException.class,
                () -> new Turno("T,1", 1, FECHA, EstadoTurno.EN_ESPERA, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Turno("T1\nX", 1, FECHA, EstadoTurno.EN_ESPERA, null));
    }

    @Test
    void rechazaVentanillaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> new Turno("T1", 1, FECHA, EstadoTurno.EN_ATENCION, "V04"));
    }

    @Test
    void enAtencionRequiereVentanillaYEnEsperaNoPuedeTenerla() {
        assertThrows(IllegalArgumentException.class,
                () -> new Turno("T1", 1, FECHA, EstadoTurno.EN_ATENCION, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Turno("T1", 1, FECHA, EstadoTurno.EN_ESPERA, "V01"));
    }

    @Test
    void rechazaSecuenciaNoPositiva() {
        assertThrows(IllegalArgumentException.class,
                () -> new Turno("T1", 0, FECHA, EstadoTurno.EN_ESPERA, null));
    }

    @Test
    void conEstadoDevuelveCopiaSinAlterarElOriginal() {
        Turno espera = new Turno("T1", 1, FECHA, EstadoTurno.EN_ESPERA, null);
        Turno atencion = espera.conEstado(EstadoTurno.EN_ATENCION, "V02");
        assertEquals(EstadoTurno.EN_ESPERA, espera.getEstado());
        assertEquals(EstadoTurno.EN_ATENCION, atencion.getEstado());
        assertEquals("V02", atencion.getVentanilla());
    }
}
