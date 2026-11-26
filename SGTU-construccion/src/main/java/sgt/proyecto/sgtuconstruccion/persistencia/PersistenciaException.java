package sgt.proyecto.sgtuconstruccion.persistencia;

/** Fallo al leer o guardar los datos persistentes (RD-02, RD-03, RF-12). */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
