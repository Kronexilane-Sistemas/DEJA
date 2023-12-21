package org.kronexilane.DEJA.DAO;

import java.util.List;

import org.kronexilane.DEJA.Connector.Connector;

/**
 * Implementa las operaciones DAO sobre un objeto de tipo T y clave primaria de tipo K.
 *
 * @param <T> Tipo de entidad (Objeto POJO)
 * @param <K> Tipo de la clave primaria
 * @author SERGIO
 */
public interface DAOInterface<T, K> {
    // -- Funcione de actualización
    void Save(T entity) throws DAOException; //Salva un ejemplar de la clase T

    int Update(T entity) throws DAOException; // Actualiza un ejemplar de la clase T

    int Delete(T entity) throws DAOException; // Borrar un ejemplar de la clase T

    // -- Funciones de Carga

    // -- Usa de filtro los campos de una entidad POJO
    T LoadByEntity(T entity) throws DAOException; // Carga un ejemplar de la clase T

    T LoadByPK(K primaryKey);

    List<T> LoadByQuery(String query) throws DAOException;

    // -- Carga Listas completas --
    List<T> LoadList(T entity,
                     String[] Relational,
                     String BooleanTemplate) throws DAOException; // Carga una lista con un filtro dado por T

    List<T> LoadList() throws DAOException; // Carga una lista con un filtro dado por T

    boolean isEmpty() throws DAOException;

    void setLog(boolean value); // Activa/Desactiva el log

    Connector Connector();
}
