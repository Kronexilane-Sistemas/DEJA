
package org.kronexilane.DEJA.DAO;

import org.kronexilane.DEJA.Annotations.DEJAEntity;
import org.kronexilane.DEJA.Annotations.DEJAPrimaryKey;
import org.kronexilane.DEJA.Connector.Connector;
import org.kronexilane.DEJA.Connector.ConnectorFactory;
import org.kronexilane.DEJA.Connector.ConnectorDataSource;
import org.kronexilane.DEJA.utility.SQLGenerator.QLGeneratorException;
import org.kronexilane.DEJA.Connector.ConnectorException;

import org.kronexilane.DEJA.utility.SQLGenerator.QLGenerator;
import org.kronexilane.DEJA.utility.SQLGenerator.QLGeneratorDATA;
import org.kronexilane.DEJA.utility.SQLGenerator.SQLGenerator;

import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Permite implementar un DAO de un tipo de objeto T (POJO) y tipo de clave primaria K.
 *
 * @param <T> Tipo de clase entidad.
 * @param <K> Tipo de la clave primaria.
 * @author SERGIO
 */
public abstract class DAO<T, K> implements DAOInterface<T, K> {

    // Devuelve aqui el tipo T (POJO/TABLA/ENTIDAD)

    // Generador de sentencias SQL
    QLGenerator generador;

    // Resultado de la última operación de Load o FindByPK
    T POJOLastFindOperation;

    private String _pkField = "";
    private String _entityName = "";

    // Motor SQL que utiliza el DAO
    protected ConnectorDataSource engine;

    // activa-desactiva el log por pantalla
    private boolean log = false;


    // Consumer Lambda para impresión sencilla
    Consumer<Object> outC = System.out::println;


    // Mapeador de RESULTSet JDBC a ENTITY POJO
    private Function<ResultSet, T> rsToEntity = null;


    /**
     * CONSTRUCTOR
     * ------------
     * Carga valores de configuración iniciales en el constructor
     * 1º El nombre de la tabla dado por el POJO Entity y la notación @DEJAEntity
     * 2º El nombre de la clave dado por el POJO Entity y la notación @DEJAPrimaryKey
     * 3º El método de MAPEADO de resultSet a Entity
     * 4º El Motor Engine o Connector a usar
     * 5º Establece el generado de sentencias SQL
     */
    public DAO() {
        try {
            // La clase POJO Deja debe estar anotada con el nombre de la clase
            Class<T> POJO = this.getClassPOJO();

            // 1º Lee la  anotacion que indica que tabla es
            DEJAEntity tableName = POJO.getAnnotation(DEJAEntity.class);

            // Tabla de la base de datos
            String _tablename;
            if (tableName != null) {
                _tablename = tableName.name();
                this._entityName = _tablename;
            } else {
                throw new DAOException("La clase '" + POJO.getSimpleName() + "' no es una entidad DAO DEJA.", null);
            }

            // 2º Establece la PRIMARY KEY
            for (Field field : POJO.getDeclaredFields()) {
                if (field.isAnnotationPresent(DEJAPrimaryKey.class)) {
                    this._pkField = field.getName();
                }
            }
            // 3º Estable el método de MAPEO de resultSet (JDBC) a entity (POJO)
            this.rsToEntity = ResultSetToPOJOEntity();
            // 4º Estable el conector con la base de datos
            this.engine = Connector();
            // 5º Establece el generador de sentencias SQL
            this.generador = new SQLGenerator(new QLGeneratorDATA(_tablename, _pkField));
        } catch (DAOException e) {
            outC.accept(e.getMessage());
        }

    }

    /* ------------------------------------------------
    /*  FUNCIONES DE ACTUALIZACIÓN, CREACIÓN Y BORRADO
    /* ------------------------------------------------

    /**
     * Función DAO:
     * Guarda un ejemplar de una entidad.
     *
     * @param entity ejemplar de entidad a guardar.
     * @throws DAOException Captura de posibles errores.
     */
    @Override
    public void Save(T entity) throws DAOException {


        int rowA;

        try {
            String SQL_INSERT = generador.getInsert(entity);
            if (log) outC.accept("SQL de save():" + SQL_INSERT);
            engine.connect();
            rowA = engine.execute(SQL_INSERT);
            engine.disconnect();
        } catch (QLGeneratorException | ConnectorException ex) {
            throw new DAOException("Error en Save():" + ex.getMessage(), ex);
        }
    }

    /**
     * Función DAO: LOAD
     * Carga un único ejemplar de la entidad en un objeto T.
     * utilizando un objeto de la entidad T como filtro en la forma
     * en que todos los campos NO NULOS se utilicen en la búsqueda
     * en la forma campo1 and campo2 and campo3.
     * Si no lo encuentra devuelve null.
     *
     * @param entity Ejemplar de entidad filtro.
     * @return Entidad resultante, resultado completo de la búsqueda.
     * @throws DAOException Captura de posibles errores.
     */
    @Override
    public T LoadByEntity(T entity) throws DAOException {
        T newEntity = null;
        try {
            String SQL_SELECT = generador.getSelect(entity);
            if (log) outC.accept("SQL de Load():" + SQL_SELECT);
            engine.connect();
            ResultSet rs = engine.executeQuery(SQL_SELECT);
            if (rs != null && rs.next()) {
                newEntity = rsToEntity.apply(rs);
                // Busca el método clone
                Optional<Method> CloneMethod = Arrays.stream(newEntity.getClass().getMethods()).filter(e -> e.getName().equalsIgnoreCase("CLONE")).findFirst();
                if (CloneMethod.isPresent()) {
                    try {
                        POJOLastFindOperation = (T) CloneMethod.get().invoke(newEntity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
                //this.setUpdatablePOJO(newEntity);
                rs.close();
            }
            engine.disconnect();
        } catch (ConnectorException | SQLException | QLGeneratorException ex) {
            throw new DAOException("Error en Load():" + ex.getMessage(), ex);
        }
        return (newEntity);
    }

    @Override
    public T LoadByPK(K primaryKey) {
        T result = null;
        Class<T> entity = this.getClassPOJO();
        Field[] fields = entity.getDeclaredFields();
        for (Field field : fields) {
            DEJAPrimaryKey pk = field.getAnnotation(DEJAPrimaryKey.class);
            if (pk != null) {
                try {
                    T filter = (T) entity.newInstance();
                    String method = "set".concat(capitalize(field.getName()));
                    filter.getClass().getMethod(method, primaryKey.getClass()).invoke(filter, (K) primaryKey);
                    result = this.LoadByEntity(filter);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException | DAOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        return result;
    }

    @Override
    public List<T> LoadByQuery(String query) throws DAOException {
        List<T> myList = null;
        T newEntity;
        String SQL_SELECT = query;
        // En esta función solo se admiten sentencias select
        if (!query.toLowerCase().contains("select")) {
            throw new DAOException("En LoadByQuery() sólo se admiten sentencias select.", null);
        }
        try {
            if (log) outC.accept("SQL de LoadByQuery:" + SQL_SELECT);
            engine.connect();
            ResultSet rs = engine.executeQuery(SQL_SELECT);
            if (rs != null) {
                myList = new LinkedList<>();
                while (rs.next()) {
                    newEntity = this.rsToEntity.apply(rs);
                    myList.add(newEntity);
                }
                rs.close();
            }
            engine.disconnect();
        } catch (ConnectorException | SQLException ex) {
            throw new DAOException("Error en LoadByQuery():" + ex.getMessage(), ex);
        }
        return (myList);
    }

    /**
     * Función DAO:
     * Carga una Lista de objetos pasándole información de filtro.
     * Si se le pasa una matriz de operadores relacionales, estos
     * se sustituirán en el orden de aparición de los
     * atributos en la entidad, además creará la sentencia con la
     * plantilla que se le pase.
     *
     * @param entity          Ejemplar de entidad filtradora.
     * @param Relational      Array de operadores relacionales.
     * @param BooleanTemplate Plantilla de sentencia de filtro.
     * @return Lista de entidades (null si no encontró nada).
     * @throws DAOException Captura de posibles errores.
     */
    @Override
    public List<T> LoadList(T entity,
                            String[] Relational,
                            String BooleanTemplate) throws DAOException {
        List<T> myList = null;
        T newEntity;
        String SQL_SELECT;
        try {
            if (Relational != null)
                SQL_SELECT = generador.getSelect(entity, Relational, BooleanTemplate);
            else
                SQL_SELECT = generador.getSelect(entity);
            if (log) outC.accept("SQL de LoadList():" + SQL_SELECT);
            engine.connect();
            ResultSet rs = engine.executeQuery(SQL_SELECT);
            if (rs != null) {
                myList = new LinkedList<>();
                while (rs.next()) {
                    newEntity = this.rsToEntity.apply(rs);
                    myList.add(newEntity);
                }
                rs.close();
            }
            engine.disconnect();
        } catch (QLGeneratorException | ConnectorException | SQLException ex) {
            ex.printStackTrace();
            throw new DAOException("Error en LoadList():" + ex.getMessage(), ex);

        }
        return (myList);
    }

    /**
     * Función DAO:
     * Carga una lista con todos los ejemplares de la entidad.
     *
     * @return Lista de los ejemplares.
     * @throws DAOException Captura de posibles errores.
     */
    @Override
    public List<T> LoadList() throws DAOException {
        return LoadList(null, null, null);
    }

    @Override
    public boolean isEmpty() throws DAOException {
        boolean empty = true;
        try {
            String SQL = "select count(*) from " + this._entityName;
            engine.connect();
            ResultSet res = engine.executeQuery(SQL);
            if (res.next()) {

                empty = res.getInt(1) == 0;
            }
            engine.disconnect();
        } catch (ConnectorException | SQLException e) {
            System.out.println(e.getMessage());
        }
        return empty;
    }

    /**
     * Función DAO:
     * Actualiza una entidad T,si la entidad de tipo T se estableció como 'cloneable'
     * y se implementó el método Clone, podrán ejecutarse actualizaciones
     * sobre objetos precargados con todos los datos de la entidad, ya que
     * cálcula las diferencias a actualizar.
     *
     * @param entity Ejemplar de la entidad que se va a actualizar.
     * @return Devuelve el nº de filas afectadas.
     * @throws DAOException Captura de posibles errores.
     */
    @Override
    public int Update(T entity) throws DAOException {
        String SQL_UPDATE;
        int result = 0;

        try {
            setUpdatablePOJO(entity);
            SQL_UPDATE = generador.getUpdate(POJOLastFindOperation);

            if (SQL_UPDATE.equalsIgnoreCase("")) {
                throw new DAOException("No hay diferencias. Nada para actualizar.", null);
            }

            if (log) outC.accept("SQL de Update():" + SQL_UPDATE);
            engine.connect();
            result = engine.execute(SQL_UPDATE);
            this.POJOLastFindOperation = null;
            engine.disconnect();
        } catch (ConnectorException | QLGeneratorException ex) {
            throw new DAOException("Error en Update():" + ex.getMessage(), ex);
        }
        return (result);
    }

    /**
     * Función DAO:
     * Borra un ejemplar de una entidad.
     *
     * @param entity Entidad a borrar
     * @return Devuelve el nº de filas afectadas.
     * @throws DAOException Captura de posibles errores.
     */
    @Override
    public int Delete(T entity) throws DAOException {
        String SQL_DELETE;
        int result;
        try {
            SQL_DELETE = generador.getDelete(entity);
            if (log) outC.accept("SQL de Delete():" + SQL_DELETE);
            engine.connect();
            result = engine.execute(SQL_DELETE);
            engine.disconnect();
        } catch (ConnectorException | QLGeneratorException ex) {
            throw new DAOException("Error en Delete():" + ex.getMessage(), ex);
        }
        return (result);
    }


    /**
     * Activa el LOG de consola por pantalla.
     *
     * @param value activa o desactiva la función.
     */
    @Override
    public void setLog(boolean value) {
        log = value;
        engine.setLog(value);

    }

    @Override
    public Connector Connector() {
        return (ConnectorFactory.getInstance().getDBEngine());
    }



    /* ------------------------------------------------------------------
     *  Métodos privados de utilidad de Gestión interna de la clase DAO
     * -----------------------------------------------------------------*/


    /**
     * Devuelve el nombre de la clave primaria de la tabla
     * asociada al DAO.
     *
     * @return String con el nombre del campo clave primaria.
     */
    private String getPrimaryKey() {
        return this._pkField;
    }

    /**
     * Devuelve la referencia al CLASS del POJO dado por
     * el parámetro genérico T.
     * Permite leer sus anotaciones y establecer características
     * de forma genérica (Nombre de tabla, clave primaria...) para
     * cualquier entidad POJO de tipo T.
     *
     * @return Tipo de Clase T
     */
    private Class<T> getClassPOJO() {
        // La clase POJO Deja debe estar anotada con el nombre de la clase
        Class<T> ret = null;
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType param = (ParameterizedType) type;
            Type[] typeArguments = param.getActualTypeArguments();
            ret = (Class<T>) typeArguments[0];

        }
        return ret;
    }

    /**
     * Recibe un ResultSet de JDBC y lo procesa
     * creando una instancia de entidad POJO de tipo T
     * y rellenándola con los datos devueltos
     *
     * @return T (Tipo POJO/ENTIDAD DEJA)
     * Utiliza intensamente la REFLECTION.
     */
    private Function<ResultSet, T> ResultSetToPOJOEntity() {
        // Obtiene la clase real del POJO/ENTIDAD DEJA
        // para poderla instanciar
        Class<T> ret = getClassPOJO();
        return res -> {
            // Crea una instancia de POJO del tipo T
            T newEntity = null;
            try {
                newEntity = ret.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                System.out.println(e.getMessage());
            }
            Field[] fields;
            if (newEntity != null) {
                fields = newEntity.getClass().getDeclaredFields();
                /**
                 * Obtiene la lista de columnas de la consulta
                 * y las mapea en la entidad POJO.
                 * Los métodos de "Seteo" tienen que ser de la forma
                 * setCAMPO (CAMPO=campo de la tabla capitalizado).
                 */
                try {
                    int cols = res.getMetaData().getColumnCount();
                    String method = null;
                    for (int i = 1; i < cols + 1; i++) {
                        method = this.CreateMethodName(res.getMetaData().getColumnName(i));
                        Method[] methods = newEntity.getClass().getMethods();
                        String finalMethod = method;
                        Stream<Method> executeMethod = Arrays.stream(methods).filter(e -> e.getName().equalsIgnoreCase(finalMethod));
                        Optional<Method> SETmethod = executeMethod.findFirst();
                        if (SETmethod.isPresent()) {
                            try {
                                SETmethod.get().invoke(newEntity, res.getObject(res.getMetaData().getColumnName(i)));
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            } catch (SQLException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                } catch (SQLException e) {
                    outC.accept(e.getMessage());
                }

            }
            return newEntity;
        };
    }

    /**
     * Crea un POJO actualizable con las diferencias
     * basándose en el último resultado de la operación
     * LOAD y del valor actual que le pasamos en a
     *
     * @param a Valor actual del POJO y del cual vamos a obtener diferencias.
     */
    private void setUpdatablePOJO(T a) {

        // Sale si no hay último resultado;
        T b = POJOLastFindOperation;
        if (POJOLastFindOperation != null) {
            String PrimaryKey = getPrimaryKey();
            Stream<Field> campos = Arrays.
                    stream(a.getClass().
                            getDeclaredFields()).filter(c -> !c.getName().equalsIgnoreCase(PrimaryKey));

            campos.forEach(aField -> {
                try {
                    Field bField = b.getClass().getDeclaredField(aField.getName());
                    Object valA = a.getClass().getDeclaredMethod(generador.getQLGeneratorInfo().getGET_PREFIX() + bField.getName()).invoke(a);
                    Object valB = b.getClass().getDeclaredMethod(generador.getQLGeneratorInfo().getGET_PREFIX() + bField.getName()).invoke(b);
                    if (valA.equals(valB)) {
                        Object[] values = {null};
                        Object invoke = b.getClass().getDeclaredMethod(generador.getQLGeneratorInfo().getSET_PREFIX() + bField.getName(), bField.getType()).invoke(b, values);
                    } else {
                        Object[] values = {valA};
                        Object invoke = b.getClass().getDeclaredMethod(generador.getQLGeneratorInfo().getSET_PREFIX() + bField.getName(), bField.getType()).invoke(b, values);
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | NoSuchMethodException |
                         IllegalAccessException | InvocationTargetException ignored) {

                }
            });
        } else {
            POJOLastFindOperation = a;
        }
    }

    /**
     * Crea un estándar de nombre de método para la inializaciçon
     * de la entidad POJO (Todos los métodos set deben ser de la forma
     * general: setCAMPO que genera cualquier entorno ide JAVA.
     *
     * @return String
     * @Param field Nombre del campo para crear su método set
     */
    private String CreateMethodName(String field) {
        String ret = "set";
        return ret.concat(capitalize(field));
    }

    /**
     * Capitaliza una cadena
     *
     * @return String
     * @Param inputString (Cadena a capitalizar)
     */
    private String capitalize(String inputString) {

        // get the first character of the inputString
        char firstLetter = inputString.charAt(0);

        // convert it to an UpperCase letter
        char capitalFirstLetter = Character.toUpperCase(firstLetter);

        // return the output string by updating
        //the first char of the input string
        return inputString.replace(inputString.charAt(0), capitalFirstLetter);
    }
}
