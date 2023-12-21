package org.kronexilane.DEJA.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DEJAEntity - Anotación que indica que un objeto POJO es una entidad DEJA
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DEJAEntity {
    /**
     * Propiedad que indica el nombre de la tabla asignada a la entidad POJO
     *
     * @return String
     */
    String name();
}
