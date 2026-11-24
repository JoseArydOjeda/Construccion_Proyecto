package sgt.proyecto.sgtuconstruccion.persistencia;

import java.util.List;
import sgt.proyecto.sgtuconstruccion.modelo.Turno;

/**
 * Contrato de persistencia que usa la lógica de negocio.
 * La lógica no necesita saber si los datos viven en un archivo o en otro medio.
 */
public interface TurnoRepositorio {

    /**
     * Carga todos los turnos guardados.
     *
     * @return la lista de turnos (vacía si aún no hay datos guardados)
     * @throws PersistenciaException si los datos no se pueden leer o son inválidos
     */
    List<Turno> cargar() throws PersistenciaException;

    /**
     * Reemplaza todo lo guardado por la lista indicada. Es todo o nada: si falla,
     * los datos anteriores quedan intactos y se lanza la excepción (RD-02).
     *
     * @throws PersistenciaException si la lista es inconsistente o no se pudo escribir
     */
    void guardar(List<Turno> turnos) throws PersistenciaException;
}
