/*
 ConnectorFactory Clase Singletron
 Se encarga de proporcionar a todos los objetos DAO
 inicializados con el constructor vacio por defecto
 de un motor ConnectorDataSource apropiado
 */
package org.kronexilane.DEJA.Connector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Devuelve un objeto válido Connector utilizado internamente
 * por las clases derivadas de DAO con el objetivo de conectarse
 * a un origen de datos determinado.
 *
 * @author SERGIO
 */
public class ConnectorFactory {

    // Constructor privado, evita instanciación
    private ConnectorFactory() {
    }

    /**
     * Devuelve la instancia del Singletron
     *
     * @return Devuelve su única instancia.
     */
    public static ConnectorFactory getInstance() {
        return SQLFactoryHolder.INSTANCE;
    }

    private static class SQLFactoryHolder {
        private static final ConnectorFactory INSTANCE = new ConnectorFactory();
    }

    /**
     * Funciones de la clase SingleTron.
     */

    Connector SQLEInstanciaded = null;
    String resourceXMLFile = "DEJADataSources.xml";

    /**
     * Define una entrada del fichero DEJADataSources.xml
     * que contiene el registro de la aplicación de
     * los "drivers" Connector creados
     */
    private static class SQLEngineXMLEntry {
        private final String className; // Contiene Paquete.clase


        public SQLEngineXMLEntry(String className) {
            this.className = className;
            // Contiene clave para encontrarlo
            // Descripción
        }


        public String getClassName() {
            return className;
        }

    }

    /**
     * Obtiene el Connector definido en DEJADataSources.xml
     * como "default", es el usado por los DAO que tienen
     * un constructor por defecto "vacío".
     * Dicho Connector sólo se instancia una vez, ya que
     * es la clase por defecto.
     *
     * @return Una clase Connector válida por defecto que sólo se instancia una vez.
     */
    public Connector getDBEngine() {
        String defaultKey = "default";
        Connector res = null;
        try {
            res = getDBEngineByXMLKey(defaultKey);
        } catch (ConnectorException e) {
            System.out.println(e.getMessage());
        }
        return res;
    }

    /**
     * Retrona una instancia del Connector dado por key y
     * previamente registrado en el archivo XML DEJADataSources.xml
     *
     * @param key Clave XML
     * @return Instancia de Connector
     */
    public Connector getDBEngine(String key) {
        Connector res = null;
        try {
            res = getDBEngineByXMLKey(key);
        } catch (ConnectorException e) {
            System.out.println(e.getMessage());
        }
        return res;
    }

    /**
     * Método privado: Devuelve un Connector previamente registrado en el
     * recurso interno 'DEJADataSources.xml' identificado por una clave classkey
     * y lo instancia
     *
     * @param classKey Clave de identificación de la clase en el archivo XML.
     * @return Un Connector válido
     */
    private Connector getDBEngineByXMLKey(String classKey) throws ConnectorException {
        Class<Connector> claseEngineAI;
        Connector res;

        try {
            // Lee la entrada en el archivo XML interno de la aplicación            
            SQLEngineXMLEntry entrada = ReadSQLEnginesXML(classKey);

            // Si hemos pedido que sólo se instancie una vez
            // Sólo instancia si no ha sido instanciado nunca
            if (SQLEInstanciaded == null) {
                claseEngineAI = (Class<Connector>) Class.forName(entrada.getClassName());
                // Lanza excepción si no es una clase derivada de Connector
                if (claseEngineAI.getSuperclass() != Connector.class) {
                    throw new ConnectorException("'" + entrada.getClassName() + "' no es una clase Connector válida.", null);
                }
                SQLEInstanciaded = (Connector) claseEngineAI.getConstructors()[0].newInstance();

            }
        } catch (ConnectorException | ClassNotFoundException ex) {
            throw new ConnectorException("No hay clase Connector registrada.", ex);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException ex) {
            throw new ConnectorException("Error al cargar la clase Connector registrada:" + ex.getCause().getMessage(), ex.getCause());
        }
        res = SQLEInstanciaded;
        return res;

    }

    /**
     * Lee el archivo de recursos definido en la variable resXML
     * y devuelve un objeto ReadSQLEnginesXML con la entrada y la
     * clave de dicha clase Connector.
     * Salta excepción si hay algún problema con el archivo.
     * Este archivo ha de situarse en el paquete por defecto o
     * raiz del proyecto y se compilará con todo el proyecto y
     * queda como un recurso a nivel interno.
     * Cuando se quiera añadir una clase de Connector para un nuevo
     * SGBDR simplemente se escribe, añade a este archivo y se modifica
     * la clave default y estará disponible gracias a este método
     * por la clave key.
     *
     * @return SQLEngineXMLEntry
     */
    private ConnectorFactory.SQLEngineXMLEntry
    ReadSQLEnginesXML(String SearchClassKey) throws ConnectorException {
        // Recurso en el paquete raíz

        String DefaultEngine;
        String ClassName;
        String ClassKey;
        SQLEngineXMLEntry res = null;
        try {

            InputStream in = getClass().getResourceAsStream("/".concat(resourceXMLFile));
            if (in == null) throw new ConnectorException("Error al abrir el archivo de recursos.", null);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(in);


            NodeList paths = document.getElementsByTagName("engine");

            DefaultEngine = paths.item(0).getParentNode().getAttributes().
                    getNamedItem("default").getNodeValue();

            if (SearchClassKey.equalsIgnoreCase("default"))
                SearchClassKey = DefaultEngine;


            for (int i = 0; i < paths.getLength(); i++) {
                Node path = paths.item(i);
                ClassName = path.getAttributes().getNamedItem("class").getNodeValue();
                ClassKey = path.getAttributes().getNamedItem("key").getNodeValue();

                // Si coincide la clave crea una entrada y sale devolviendola.
                if (ClassKey.equalsIgnoreCase(SearchClassKey)) {
                    res = new SQLEngineXMLEntry(ClassName);
                    break;
                }
            }
            if (res == null)
                throw new ConnectorException("No hay registrado una clase Connector con clave '" + SearchClassKey + "' en '" + resourceXMLFile + "'.", null);
            return res;

        } catch (NullPointerException | ParserConfigurationException | SAXException | IOException |
                 IllegalArgumentException ex) {

            throw new ConnectorException("Error en el archivo de recursos: '" +
                    resourceXMLFile + "', formato interno incorrecto o no se encuentra.", ex);
        }
    }

}
