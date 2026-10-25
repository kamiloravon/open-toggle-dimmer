package org.opentoggledimmer.domain.exception;

/**
 * Базовое доменное исключение: нарушение инварианта домена.
 * Base domain exception: a domain invariant was violated.
 *
 * <hr>
 *
 * <p>Все исключения домена наследуют этот тип, чтобы внешние слои могли
 * отличать ошибки бизнес-правил от технических ошибок инфраструктуры.
 * All domain exceptions extend this type so outer layers can distinguish
 * business-rule violations from infrastructure failures.</p>
 */
public class DomainException extends RuntimeException {

    /**
     * Создаёт доменное исключение с сообщением.
     * Creates a domain exception with a message.
     *
     * @param message описание нарушения / description of the violation
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Создаёт доменное исключение с сообщением и причиной.
     * Creates a domain exception with a message and a cause.
     *
     * @param message описание нарушения / description of the violation
     * @param cause   причина / underlying cause
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
