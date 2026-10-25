package org.opentoggledimmer.domain.exception;

/**
 * Исключение: значение не прошло доменную валидацию.
 * Exception: a value failed domain validation.
 *
 * <hr>
 *
 * <p>Используется value objects при создании через фабричные методы, когда
 * передано недопустимое значение (пустая строка, выход за диапазон и т.п.).
 * Used by value objects in factory methods when an invalid value is passed
 * (empty string, out of range, etc.).</p>
 */
public final class ValidationException extends DomainException {

    /**
     * Создаёт исключение валидации.
     * Creates a validation exception.
     *
     * @param message описание нарушения / description of the violation
     */
    public ValidationException(String message) {
        super(message);
    }
}
