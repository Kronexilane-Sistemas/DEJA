package org.kronexilane.DEJA.utility.SQLGenerator;

import org.kronexilane.DEJA.Annotations.DEJAPrimaryKey;

import java.lang.reflect.Field;

/**
 * Permite crear generadores de sentencias de tratamiento
 * de bases de datos.
 *
 * @author SERGIO
 */
public abstract class QLGenerator {

    /**
     * Clase que contiene información para el generador
     * con métodos get para recuperarla cuando se necesite
     * Esta información es:
     * 1. Tabla
     * 2. Clave Primaria
     * 3. Prefijo de los métodos GET
     * 4. Prefijo de los métodos SET
     */
    private final QLGeneratorDATA QLGeneratorInfo;

    Object entity; // Objeto POJO que trataremos
    boolean log; // Logueo


    // Accesibles desde las clases derivadas
    String DB_TABLE_NAME;    // Tabla
    String PK_FIELD;    // Clave primaria
    String GET_PREFIX;  // Prefijo Get
    String SET_PREFIX;  // Prefijo Set

    /**
     * Constructor
     *
     * @param QLSetup Objeto QLGeneratorData con la informació requerida
     */
    public QLGenerator(QLGeneratorDATA QLSetup) {
        if (QLSetup == null)
            throw new java.lang.NullPointerException("Es necesario pasar un objeto QLGeneratorData válido.");
        if (QLSetup.getPK_FIELD().equalsIgnoreCase("") || QLSetup.getDB_TABLE_NAME().equalsIgnoreCase(""))
            throw new IllegalArgumentException("Falta el nombre de la tabla y/o clave primaria en el objeto de configuración de QLGenerator");
        this.DB_TABLE_NAME = QLSetup.getDB_TABLE_NAME();
        this.PK_FIELD = QLSetup.getPK_FIELD();
        this.GET_PREFIX = QLSetup.getGET_PREFIX();
        this.SET_PREFIX = QLSetup.getSET_PREFIX();
        this.QLGeneratorInfo = QLSetup;
    }

    /**
     * Devuelve el QLGeneratorDATA de información
     * de creación de las sentencias QL.
     * para obtener datos para otras funciones
     * que utilicen los prefijos get/set en uso
     * por técnicas de reflection.
     *
     * @return Un Objeto QLGenerator válido.
     */
    public QLGeneratorDATA getQLGeneratorInfo() {
        return QLGeneratorInfo;
    }


    /**
     * Devuelve una sentencia Select Simple.
     * Select * from [TABLA] Where 1=1 and campo1=xxx and campo2=xxx etc ...
     *
     * @param entity Entidad de la que obtener la SQL
     * @return Devuelve la sentencia 'Select ...'
     * @throws QLGeneratorException Excepción para el generador de sentencias SQL.
     */
    public String getSelect(Object entity) throws QLGeneratorException {
        this.entity = entity;
        return CreateSimpleSelect();
    }

    /**
     * Select con Filtrado avanzado.
     *
     * @param entity              Entidad de la que obtener la SQL
     * @param RelationalOperators Array de Operadores relacionales en orden
     * @param BooleanTemplate     Plantilla de la sentencia con operadores booleanos y paréntesis
     * @return Devuelve la sentencia 'Select' avanzada con filtrado
     * @throws QLGeneratorException Excepción para el generador de sentencias SQL.
     */
    public String getSelect(Object entity,
                            String[] RelationalOperators,
                            String BooleanTemplate) throws QLGeneratorException {
        this.entity = entity;
        String resSelect;
        if (RelationalOperators == null) {
            resSelect = CreateSimpleSelect();
        } else {
            resSelect = CreateAdvancedSelect(RelationalOperators, BooleanTemplate);
        }
        return resSelect;
    }

    /**
     * Devuelve la sentencia Insert.
     *
     * @param entity Entidad de la que obtener la SQL
     * @return Devuelve la sentencia 'Insert ...'
     * @throws QLGeneratorException Excepción para el generador de sentencias SQL.
     */
    public String getInsert(Object entity) throws QLGeneratorException {
        this.entity = entity;
        boolean autonumeric = false;
        for (Field field : this.entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(DEJAPrimaryKey.class)) {
                DEJAPrimaryKey dejaPrimaryKey = field.getAnnotation(DEJAPrimaryKey.class);
                autonumeric = dejaPrimaryKey.autonumeric();
            }
        }
        return CreateInsert(autonumeric);
    }

    /**
     * Devuelve la sentencia Delete
     *
     * @param entity Entidad de la que obtener la SQL
     * @return Devuelve la sentencia 'Delete ...'
     * @throws QLGeneratorException Excepción si hay errores.
     */
    public String getDelete(Object entity) throws QLGeneratorException {
        this.entity = entity;
        return CreateDelete();
    }

    /**
     * Devuelve la sentencia Update.
     *
     * @param entity Entidad de la que obtener la SQL
     * @return Devuelve la sentencia 'Update ...'
     * @throws QLGeneratorException Excepción para el generador de sentencias SQL.
     */
    public String getUpdate(Object entity) throws QLGeneratorException {
        this.entity = entity;
        return CreateUpdate();
    }


    /*
     * Procedimientos que las clases derivadas
     * deben sobreescribir para la creación de las
     * sentencias y que serán llamados por los métodos get(Sentencia)
     * públicos.
     */


    /**
     * Para implementar la sentencia select SIMPLE
     *
     * @return Select * from [tabla] where c1='' and c2='' and [...].
     * @throws QLGeneratorException Captura errores de procesado.
     */
    abstract String CreateSimpleSelect() throws QLGeneratorException;

    /**
     * Para implementar la sentencia select con filtros.
     *
     * @param RelationalOperators Array de operadores sustitivos del igual.
     * @param booleanTemplate     Plantilla del formato de la sentencia.
     * @return Select filtrada con formato avanzado.
     * @throws QLGeneratorException Captura de posibles errores
     */
    abstract String CreateAdvancedSelect(String[] RelationalOperators,
                                         String booleanTemplate) throws QLGeneratorException;

    /**
     * Implementa la sentencia INSERT INTO.
     *
     * @return Sentencia Inserte procesada.
     * @throws QLGeneratorException Captura posibles errores
     */
    abstract String CreateInsert(Boolean IDAutonumeric) throws QLGeneratorException;

    /**
     * Devuelve la sentencia DELETE.
     *
     * @return Sentencia Delete procesada
     * @throws QLGeneratorException captura de posibles errores.
     */
    abstract String CreateDelete() throws QLGeneratorException;

    /**
     * Devuelve la sentencia UPDATE.
     *
     * @return Senctencia Update procesada.
     * @throws QLGeneratorException captura posibles errores.
     */
    abstract String CreateUpdate() throws QLGeneratorException;
}
