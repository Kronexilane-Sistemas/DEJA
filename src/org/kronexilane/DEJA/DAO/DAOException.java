package org.kronexilane.DEJA.DAO;


/**
 * Excepción de alto nivel de DAO
 * que recoge excepciones de más bajo nivel como las
 * de los motores de SQL y el generador de consultas SQL.
 *
 * @author SERGIO
 */
public class DAOException extends Exception {
    private final String _DAOMessage;
    private final Throwable excepSource;


    public DAOException(String DAOMessage, Throwable source) {
        super(DAOMessage);
        _DAOMessage = DAOMessage;
        excepSource = source;
    }

    @Override
    public String getLocalizedMessage() {
        return (excepSource == null ? _DAOMessage : excepSource.toString());
    }


}
