package org.kronexilane.DEJA.Connector;

import java.sql.ResultSet;

/**
 * Abstracción de las operaciones estándar de Motor SQL de SGBDR.
 *
 * @author SERGIO
 */
public abstract class ConnectorDataSource {

    /**
     * Conecta a la base de datos
     *
     * @throws ConnectorException Captura de errores.
     */
    public abstract void connect() throws ConnectorException; // Conexión

    /**
     * Ejecuta un sentencia SQL sin devolución de resultados.
     *
     * @param sqlSentence sentencia SQL a ejecutar.
     * @return nº de filas afectadas.
     * @throws ConnectorException Captura de errores.
     */
    public abstract int execute(String sqlSentence) throws ConnectorException; // Sentencias DML

    /**
     * Ejecuta una sentencia de consulta SQL devolviendo un conjunto
     * de registros.
     *
     * @param sql Sentencia a ejecutar.
     * @return ResultSet Objeto con los registros devueltos.
     * @throws ConnectorException Captura de errores.
     */
    public abstract ResultSet executeQuery(String sql) throws ConnectorException; // Consultas

    /**
     * Desconecta de la base de datos
     *
     * @throws ConnectorException Captura de errores.
     */
    public abstract void disconnect() throws ConnectorException; //Desconexión


    /**
     * Activa el LOG por pantalla.
     *
     * @param value TRUE/FALSE para activar desactivar.
     */
    public abstract void setLog(boolean value);

}
