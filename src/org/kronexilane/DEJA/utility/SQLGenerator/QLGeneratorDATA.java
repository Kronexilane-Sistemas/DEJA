package org.kronexilane.DEJA.utility.SQLGenerator;

/**
 * Clase para pasar datos de configuración al generador
 * de lenguaje de interrogación de bases de datos QLGenerator.
 *
 * @author SERGIO
 */
public class QLGeneratorDATA {
    private final String DB_TABLE_NAME; // Tabla
    private final String PK_FIELD; // Clave primaria
    private String GET_PREFIX = "get"; // Prefijo para los métodos get
    private String SET_PREFIX = "set"; // Prefijo para los métodos put

    /**
     * Constructor para pasar al generador QLGenerator
     * el nombre de la tabla y el nombre del campo clave
     *
     * @param DB_TABLE_NAME Nombre de tabla
     * @param PK_FIELD      Campo clave primaria
     */
    public QLGeneratorDATA(String DB_TABLE_NAME,
                           String PK_FIELD) {
        this.DB_TABLE_NAME = DB_TABLE_NAME;
        this.PK_FIELD = PK_FIELD;
    }

    /**
     * Constructor que pasa el nombre de la tabla, el campo clave
     * y los prefijos de los métodos GET y SET que se infieren
     * de las entidades.
     *
     * @param DB_TABLE_NAME Nombre de tabla
     * @param PK_FIELD      Clave primaria
     * @param GET_PREFIX    Prefijo métodos 'GET'
     * @param SET_PREFIX    Prefijo métodos 'SET'
     */
    public QLGeneratorDATA(String DB_TABLE_NAME,
                           String PK_FIELD,
                           String GET_PREFIX,
                           String SET_PREFIX) {
        this.DB_TABLE_NAME = DB_TABLE_NAME;
        this.PK_FIELD = PK_FIELD;
        this.GET_PREFIX = GET_PREFIX;
        this.SET_PREFIX = SET_PREFIX;
    }

    /**
     * Devuelve el nombre de la tabla
     *
     * @return Nombre de la tabla
     */
    public String getDB_TABLE_NAME() {
        return DB_TABLE_NAME;
    }

    /**
     * Devuelve el nombre del campo clave primaria
     *
     * @return Campo clave primaria
     */
    public String getPK_FIELD() {
        return PK_FIELD;
    }

    /**
     * Devuelve el prefijo de los métodos 'GET'.
     *
     * @return Prefijo GET
     */
    public String getGET_PREFIX() {
        return GET_PREFIX;
    }

    /**
     * Devuelve el prefijo de los métodos 'SET'.
     *
     * @return Prefijo SET
     */
    public String getSET_PREFIX() {
        return SET_PREFIX;
    }
}
