package org.kronexilane.DEJA.Connector;

/**
 * Excepción para errores de motores de ConnectorDataSource y
 * de la clase singletron ConnectorFactory.
 *
 * @author SERGIO
 */
public class ConnectorException extends Exception {
    private final String _SQLEngineMessage;
    private final Throwable excepSource;


    public ConnectorException(String SQLEngineMessage, Throwable source) {
        super(SQLEngineMessage);
        _SQLEngineMessage = SQLEngineMessage;
        excepSource = source;

    }

    @Override
    public String getLocalizedMessage() {
        return (excepSource == null ? _SQLEngineMessage : excepSource.toString());
    }


}
