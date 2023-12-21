package org.kronexilane.DEJA.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación que indica si un CAMPO del objeto POJO es clave primaria
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface DEJAPrimaryKey {
    /**
     * Indica si el campo está definido en la tabla como autonúmerico
     *
     * @return boolean
     */
    boolean autonumeric();
}
