package org.kronexilane.DEJA.utility.SQLGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Stream;


/**
 * Implementación de un generador dinámico
 * de sentencias SQL a partir de un objeto POJO.
 *
 * @author SERGIO
 */
public class SQLGenerator extends QLGenerator {

    // Constante de separador de cadenas en SQL
    private final String SQL_STRING_FIELD_DELIMITER = "'";
    private final String SQL_FIELD_DELIMITER = ",";

    /*
        Necesario para el procesado
        de algunas sentencias
     */
    private int iCount;
    private LinkedList<String> grupos = null;

    /**
     * Constructor de la clase QLGenerator
     *
     * @param QLSetup Objeto de configuración
     */
    public SQLGenerator(QLGeneratorDATA QLSetup) {
        super(QLSetup);
        this.iCount = 0;
    }

    /**
     * Funciones  que llamarán las propiedades externas
     * de la clase base y que devuelven las sentencias
     * sql creadas. Estos métodos deben sobreescribirse
     * y devolver como String la sentencia SQL creada.
     */

    // Sentencia Select Simple
    @Override
    String CreateSimpleSelect() throws QLGeneratorException {
        String generated;
        try {
            if (entity != null) {
                Class<?> cls = entity.getClass();
                Stream<Field> CamposFiltrados = Arrays.stream(cls.getDeclaredFields());

                StringBuilder consBuilder = new StringBuilder();

                String andOperator = " and ";

                CamposFiltrados.forEach(obj -> {
                    String separator;
                    try {
                        Object value = cls.getDeclaredMethod(GET_PREFIX + Capitalize(obj.getName())).invoke(entity);
                        if (value != null) {

                            // Configura el delimitador de campo en función del tipo
                            if (obj.getType().getSimpleName().equalsIgnoreCase("string")) {
                                separator = this.SQL_STRING_FIELD_DELIMITER;
                            } else {
                                separator = "";
                            }


                            if (obj.getType().getSimpleName().equalsIgnoreCase("string"))
                                separator = this.SQL_STRING_FIELD_DELIMITER;
                            String CampoB = obj.getName() + "=" + setupFieldSelect(separator, value.toString());
                            consBuilder.append(CampoB).append(andOperator);

                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                             NoSuchMethodException | SecurityException ex) {
                        /*
                            Salta aquí si no se ha podido ejecutar algún método
                            por haberse cambiado el nombre del método get del POJO
                            sin haber hecho corresponder el nombre del campo private
                            correspondiente. Ignora ese dato, pero trata de construir
                            el resto de la SQL.

                        */
                        throw new RuntimeException("Error en la construcción de la sentencia SELECT:" + ex.getMessage());

                    }
                });
                if (consBuilder.length() != 0) {
                    consBuilder.delete(consBuilder.length() - andOperator.length(), consBuilder.length());
                    generated = "Select * from " + super.DB_TABLE_NAME + " where 1=1 and " + consBuilder;
                } else {
                    generated = "Select * from " + super.DB_TABLE_NAME;
                }
            } else {
                generated = "Select * from " + super.DB_TABLE_NAME;
            }
        } catch (IllegalArgumentException ex) {
            throw new QLGeneratorException("Error de generación de la sentencia SELECT.", ex);
        }
        if (log) System.out.println("Generada: " + generated);
        return (generated);
    }

    /**
     * Crea una sentencia select avanzada con las siguientes características
     * 1º Utiliza una matriz de operadores relacionales, cuyo orden y expresividad
     * la determina el orden en que dichos campos esten declarados en el POJO.
     * Es decir si en el pojo tenemos declarado.
     * Integer ID
     * String Nombre
     * Integer EDAD
     * Double Sueldo
     * <p>
     * Los operadores los tenemos que construir teniendo en cuenta
     * que la select se construye tomando el orden de los campos declarados
     * en el mismo orden del pojo
     * En el ejemplo expuesto:
     * La matriz de operadores "like",op1,op2 utilizará ese orden natural:
     * Nombre like XXX , EDAD op1 XXX, Sueldo op2 XXXX
     *
     * @param RelationalOperators Operadores Relacionales de cada grupo
     * @param booleanTemplate     Plantilla de organización boolean
     * @return Sentencia SQL avanzada y formateada
     */
    @Override
    String CreateAdvancedSelect(String[] RelationalOperators,
                                String booleanTemplate)
            throws QLGeneratorException {
        String generated;
        String resFinal;
        String iniSQL;

        try {
            if (entity != null) {
                grupos = new LinkedList<>();

                Class<?> cls;
                cls = entity.getClass();
                Stream<Field> CamposFiltrados = Arrays.stream(cls.getDeclaredFields());

                StringBuilder consBuilder = new StringBuilder();

                String andOperator = " and ";

                CamposFiltrados.forEach(obj -> {
                    String separator;

                    try {
                        Object value = cls.getDeclaredMethod(GET_PREFIX + Capitalize(obj.getName())).invoke(entity);
                        if (value != null) {

                            // Configura el delimitador de campo en función del tipo
                            if (obj.getType().getSimpleName().equalsIgnoreCase("string")) {
                                separator = this.SQL_STRING_FIELD_DELIMITER;
                            } else {
                                separator = "";
                            }

                            if (obj.getType().getSimpleName().equalsIgnoreCase("string"))
                                separator = this.SQL_STRING_FIELD_DELIMITER;
                            String CampoB = obj.getName() + " " + RelationalOperators[iCount] + " " + setupFieldSelect(separator, value.toString());
                            grupos.add(CampoB);
                            consBuilder.append(CampoB).append(andOperator);
                            iCount++;
                        }
                    } catch (java.lang.ArrayIndexOutOfBoundsException | IllegalAccessException |
                             IllegalArgumentException | InvocationTargetException | NoSuchMethodException |
                             SecurityException ex) {
                        /*
                            Salta aquí si no se ha podido ejecutar algún método
                            por haberse cambiado el nombre del método get del POJO
                            sin haber hecho corresponder el nombre del campo private
                            correspondiente. Ignora ese dato, pero trata de construir
                            el resto de la SQL.

                        */
                        iCount = 0;
                        throw new RuntimeException("Error en la construcción de la sentencia SELECT:" + ex.getMessage());
                    }

                });

                iCount = 0;
                iniSQL = "Select * from " + super.DB_TABLE_NAME + " where 1=1 and ";

                if (consBuilder.length() != 0) {
                    consBuilder.delete(consBuilder.length() - andOperator.length(), consBuilder.length());
                    generated = iniSQL + consBuilder;
                } else {
                    generated = "Select * from " + super.DB_TABLE_NAME;
                }

                if (booleanTemplate != null) {
                    resFinal = booleanTemplate.toLowerCase();
                    if (resFinal.contains("%initsentence%")) {
                        resFinal = resFinal.replaceFirst("%initsentence%", iniSQL);
                    } else {
                        throw new QLGeneratorException("Falta %initsentence% al principio de la plantilla de la sentencia SELECT generada por getAdvancedSelect().", null);
                    }
                    iCount = 1;
                    for (String grupo : grupos) {
                        resFinal = resFinal.replace("@".concat(String.valueOf(iCount)), grupo);
                        iCount++;
                    }
                } else {
                    resFinal = generated;
                }

                generated = resFinal;
            } else {
                generated = "Select * from " + super.DB_TABLE_NAME;
            }

        } catch (IllegalArgumentException ex) {
            iCount = 0;
            throw new QLGeneratorException("Error de generación en la sentencia SELECT Avanzada.", ex);
        }
        grupos = null;
        if (log) System.out.println("Generada: " + generated);
        iCount = 0;
        return (generated);
    }

    /**
     * ---------------------------------------------
     * CREACIÓN DE LA INSERT
     * ----------------------------------------------
     */

    @Override
    String CreateInsert(Boolean IDAutonumeric) throws QLGeneratorException {
        StringBuilder generated = new StringBuilder("insert into " + super.DB_TABLE_NAME);
        try {
            if (entity != null) {
                Class<?> cls = entity.getClass();
                Stream<Field> campos = Arrays.stream(cls.getDeclaredFields());
                Stream<Field> CamposFiltrados;
                if (IDAutonumeric) {
                    CamposFiltrados = campos.filter(obj -> !obj.getName().equalsIgnoreCase(this.PK_FIELD));
                } else {
                    CamposFiltrados = campos;
                }

                StringBuilder listaCampos = new StringBuilder();
                StringBuilder listaValores = new StringBuilder();

                listaCampos.append("(");
                listaValores.append("values (");

                CamposFiltrados.forEach(obj -> {
                    String separator;
                    String FieldName;
                    try {
                        Object value = cls.getDeclaredMethod(GET_PREFIX + Capitalize(obj.getName())).invoke(entity);
                        if (value != null) {
                            FieldName = obj.getName();
                            listaCampos.append(FieldName).append(SQL_FIELD_DELIMITER);

                            // Configura el delimitador de campo en función del tipo
                            if (obj.getType().getSimpleName().equalsIgnoreCase("string")) {
                                separator = this.SQL_STRING_FIELD_DELIMITER;
                            } else {
                                separator = "";
                            }

                            setupFieldInsertUpdate(listaValores, separator, value.toString());

                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                             NoSuchMethodException | SecurityException ex) {
                       /*
                            Salta aquí si no se ha podido ejecutar algún método
                            por haberse cambiado el nombre del método get del POJO
                            sin haber hecho corresponder el nombre del campo private
                            correspondiente. Ignora ese dato, pero trata de construir
                            el resto de la SQL.
                            Proporciona información si procede.
                        */
                        throw new RuntimeException("Error en la construcción de la sentencia INSERT:" + ex.getMessage());
                    }
                });
                if (listaCampos.length() - 1 == 0)
                    throw new
                            QLGeneratorException("Error de generación en la sentencia INSERT", null);

                listaCampos.delete(listaCampos.length() - 1, listaCampos.length()).append(")");
                listaValores.delete(listaValores.length() - 1, listaValores.length()).append(")");
                generated.append(" ").append(listaCampos).append(" ").append(listaValores);
            }
        } catch (IllegalArgumentException ex) {
            throw new QLGeneratorException("Error de generación en la sentencia INSERT", ex);
        }
        if (log) System.out.println("Generada: " + generated);
        return (generated.toString());
    }


    /**
     * ---------------------------------------------
     * CREACIÓN DE LA DELETE
     * ----------------------------------------------
     */
    @Override
    String CreateDelete() throws QLGeneratorException {
        StringBuilder generated = new StringBuilder();
        String PKSeparator = "";
        String getPKMethod;
        try {
            if (entity != null) {

                generated.append("delete from ").append(super.DB_TABLE_NAME);
                String whereCondition = " Where " + PK_FIELD + "=";
                Class<?> cls = entity.getClass();
                Object iPKValue;
                getPKMethod = GET_PREFIX + Capitalize(PK_FIELD);

                // Creación de la claúsula Where
                iPKValue = cls.getDeclaredMethod(getPKMethod).invoke(entity);
                // Salta excepción si el POJO que vamos a actualizar
                // no tiene establecido un valor de PK
                if (iPKValue == null)
                    throw new QLGeneratorException("No se puede construir una sentencia DELETE sin una clave primaria.", null);

                if (cls.getDeclaredField(PK_FIELD).getType().getSimpleName().equalsIgnoreCase("string"))
                    PKSeparator = SQL_STRING_FIELD_DELIMITER;
                whereCondition = whereCondition + PKSeparator + iPKValue + PKSeparator;

                generated.append(whereCondition);

            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException |
                 NoSuchFieldException ex) {
            throw new QLGeneratorException("Error de generación en la sentencia DELETE.", ex);
        }
        if (log) System.out.println("Generada: " + generated);
        return (generated.toString());
    }

    /**
     * ---------------------------------------------
     * CREACIÓN DE LA UPDATE
     * ----------------------------------------------
     */
    @Override
    String CreateUpdate() throws QLGeneratorException {
        StringBuilder generated = new StringBuilder();
        String getPKMethod;


        try {
            if (entity != null) {

                String PKSeparator = "";
                generated.append("update ").append(super.DB_TABLE_NAME).append(" set");
                String whereCondition = " Where " + PK_FIELD + "=";
                Class<?> cls = entity.getClass();
                Object iPKValue;
                getPKMethod = GET_PREFIX + Capitalize(PK_FIELD);

                Stream<Field> campos = Arrays.stream(cls.getDeclaredFields());

                // Creación de la claúsula Where
                iPKValue = cls.getDeclaredMethod(getPKMethod).invoke(entity);

                // Salta excepción si el POJO que vamos a actualizar
                // no tiene establecido un valor de PK
                if (iPKValue == null)
                    throw new QLGeneratorException("No se puede actualizar un objeto sin tener este un valor en la clave primaria.", null);

                if (cls.getDeclaredField(PK_FIELD).getType().getSimpleName().equalsIgnoreCase("string"))
                    PKSeparator = SQL_STRING_FIELD_DELIMITER;

                whereCondition = whereCondition + PKSeparator + iPKValue + PKSeparator;

                Stream<Field> CamposFiltrados = campos.filter(obj -> !obj.getName().equalsIgnoreCase(this.PK_FIELD));

                StringBuilder listaCampos = new StringBuilder();


                CamposFiltrados.forEach(obj -> {
                    String separator;
                    String FieldName;
                    try {
                        Object value = cls.getDeclaredMethod(GET_PREFIX + Capitalize(obj.getName())).invoke(entity);
                        if (value != null) {
                            FieldName = obj.getName();
                            listaCampos.append(FieldName).append("=");
                            // Configura el delimitador de campo en función del tipo
                            if (obj.getType().getSimpleName().equalsIgnoreCase("string")) {
                                separator = this.SQL_STRING_FIELD_DELIMITER;
                            } else {
                                separator = "";
                            }
                            setupFieldInsertUpdate(listaCampos, separator, value.toString());

                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                             NoSuchMethodException | SecurityException ex) {
                         /*
                            Salta aquí si no se ha podido ejecutar algún método
                            por haberse cambiado el nombre del método get del POJO
                            sin haber hecho corresponder el nombre del campo private
                            correspondiente. Ignora ese dato, pero trata de construir
                            el resto de la SQL.
                            Proporciona información si procede.
                        */
                        throw new RuntimeException("Error en la construcción de la sentencia UPDATE:" + ex.getMessage());
                    }
                });

                if (!listaCampos.toString().isEmpty()) {
                    listaCampos.delete(listaCampos.length() - 1, listaCampos.length());
                    generated.append(" ").append(listaCampos);
                    generated.append(whereCondition);
                } else {
                    generated.delete(0, generated.length());
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException |
                 NoSuchFieldException ex) {
            throw new QLGeneratorException("Error de generación en la sentencia UPDATE.", ex);
        }
        if (log) System.out.println("Generada: " + generated);
        return (generated.toString());
    }


    /**
     * Capitalización de una cadena de caracteres
     *
     * @param cad Cadena de entrada
     * @return Devuelve la cadena Capitalizada
     */
    private static String Capitalize(String cad) {
        String firstLetter = cad.substring(0, 1);
        cad = cad.replaceFirst(firstLetter, firstLetter.toUpperCase());
        return (cad);
    }


    /**
     * Función específica particularización del separador de campos
     * a la hora de montar parte de la sql INSER/UPDATE sobre el valor de campo
     *
     * @param ValueList StringBuilder sobre el que se construye la lista
     * @param Separator Separador admitido
     * @param Value     Valor del campo obtenido por reflexión
     */

    private void setupFieldInsertUpdate(StringBuilder ValueList,
                                        String Separator,
                                        String Value) {
        ValueList.append(Separator).append(Value).append(Separator).append(SQL_FIELD_DELIMITER);

    }

    /**
     * Función específica particularización del separador de campos
     * a la hora de montar parte de la sql Select sobre el valor de campo
     *
     * @param Separator Separador admitido
     * @param Value     Valor del campo obtenido por reflexión
     */

    private String setupFieldSelect(String Separator,
                                    String Value) {

        return (Separator.concat(Value).concat(Separator));
    }


}