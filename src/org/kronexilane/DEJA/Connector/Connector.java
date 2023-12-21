/**
 * Connector
 * Conector especializado de BASE DE DATOS para SQLFACTORY
 * Debe extenderse de esta clase cada origen de datos donde
 * se sobreescribirán los métdos:
 * getSQLToGetRowLastID(): DEVUELVE la SQL que proporciona el IDENTTY (Autonumerico)
 * último usado.
 * getParameters(): Devuelve un objeto ConnectorParam que devuelve los parámetros
 * de la conexión de la base de datos
 */
package org.kronexilane.DEJA.Connector;

import java.sql.*;
import java.util.function.Consumer;

/**
 * CONNECTOR
 * Proporciona las funciones básicas de acceso a bajo nivel del SGBDR
 */
public abstract class Connector extends ConnectorDataSource {

    private Connection conn;
    private Statement st;
    private ResultSet rs;
    protected ConnectorParam myParam;

    Consumer<Object> outC = System.out::println; // Salida por consola de LOG


    String DB_URL = "";
    String DB_DRIVER = "";
    String DB_USER = "";
    String DB_PASSWORD = "";

    private boolean log = false;


    /**
     * Carga los parámetros iniciales establecidos en la rutina getParameters()
     * de la clase derivada y los mensajes particulares que traducen mensajes
     * originales del SGBDR.
     */
    public Connector() {
        LoadParameters();
    }       
 
    /*-----------------------------------
     *  FUNCIONALIDAD PÚBLICA IMPLEMENTADA
     -------------------------------------*/

    /**
     * Conecta a la base de datos
     * @throws ConnectorException Captura de posibles errores.
     */
    @Override
    public void connect() throws ConnectorException {

        try {

            // Controlador necesario
            Class.forName(DB_DRIVER);

            // Localización de la DB
            conn = DriverManager.
                    getConnection(DB_URL, DB_USER, DB_PASSWORD);


            // Con el objeto conexión creo un Statement
            st = conn.createStatement();
            if (log) outC.accept("Conectado OK:" + DB_URL);


        } catch (ClassNotFoundException | SQLException ex) {
            throw new ConnectorException("Fallo en connect():" + ex.getMessage(), ex);
        }
    }

    /**
     * Ejecuta un consulta SQL devolviendo el nº de filas afectadas.
     * @param sqlSentence Sentencia SQL.
     * @return Nº de filas afectadas.
     * @throws ConnectorException Captura los posibles errores.
     */
    @Override
    public int execute(String sqlSentence) throws ConnectorException {
        int resp;
        try {
            resp = st.executeUpdate(sqlSentence);
        } catch (NullPointerException | SQLException ex) {
            throw new ConnectorException("Fallo en execute():" + ex.getMessage(), ex);
        }
        return resp;
    }

    /**
     * Ejecuta una consulta SQL devolviendo un ResultSet con el resultado
     * de dicha consulta. ResultSet es null, si la consulta no devuelve nada
     * y aparte de esto, lanza excepción
     * @param sql Sentencia SQL a ejecutar.
     * @return ResultSet con el resultado de la consulta.
     * @throws ConnectorException Captura posibles errores.
     */
    @Override
    public ResultSet executeQuery(String sql) throws ConnectorException {
        try {
            rs = st.executeQuery(sql);
        } catch (SQLException ex) {
            throw new ConnectorException("Fallo en el método executeQuery():" + ex.getMessage(), ex);
        }
        return rs;
    }

    /**
     * Desconecta de la base de datos.
     * @throws ConnectorException Captura de posibles errores.
     */
    @Override
    public void disconnect() throws ConnectorException {
        try {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }
            if (log) outC.accept("Desconectado OK:" + DB_URL);
        } catch (SQLException ex) {
            throw new ConnectorException("Fallo en el método disconnect():" + ex.getMessage(), ex);
        }
    }


    /**
     * Establece el log por pantalla.
     * @param value True/False activando o desactivando esta opción.
     */
    @Override
    public void setLog(boolean value) {
        log = value;
    }


    /*-------------------
     *  MÉTODOS ABSTRACTOS
     ---------------------*/

    /**
     * Retorna un objeto ConnectorParam como los parámetros
     * de conexión.
     * @return Objeto ConnectorParam con los parámetros cargados.
     */
    public abstract ConnectorParam getParameters();


    /**----------------------------
     *  MÉTODOS PRIVADOS DE SOPORTE
     ------------------------------*/
    // Carga de parámetros
    private void LoadParameters() {
        myParam = getParameters();
        if (myParam != null) {
            this.DB_DRIVER = myParam.getDriver();
            this.DB_URL = myParam.getUrl();
            this.DB_USER = myParam.getUser();
            this.DB_PASSWORD = myParam.getPassword();
        }
    }
}
