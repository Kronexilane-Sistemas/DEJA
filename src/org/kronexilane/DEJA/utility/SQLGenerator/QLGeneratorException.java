package org.kronexilane.DEJA.utility.SQLGenerator;

/**
 * Excepción generada cuando hay algún problema de analísis del
 * generador dinámico de sentencias de base de datos.
 *
 * @author SERGIO
 */
public class QLGeneratorException extends Exception {
    private final Throwable _source;

    public QLGeneratorException(String message, Throwable source) {
        super(message);
        _source = source;
    }

    @Override
    public String getLocalizedMessage() {
        String InfoException = "";
        if (_source != null) {
            InfoException = _source.toString();
        }
        return InfoException;
    }

}
