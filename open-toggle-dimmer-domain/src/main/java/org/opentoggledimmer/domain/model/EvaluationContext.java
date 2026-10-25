package org.opentoggledimmer.domain.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Value object: контекст вычисления флага.
 * Value object: the flag evaluation context.
 *
 * <hr>
 *
 * <p>Неизменяемый набор полей, используемых для вычисления активации и
 * stickiness. Все поля опциональны. Текстовые значения хранятся как строки;
 * числовые/датовые/semver-операторы конвертируют строку на лету.
 * An immutable set of fields used to evaluate activation and stickiness. All
 * fields are optional. Text values are stored as strings; numeric/date/semver
 * operators convert on the fly.</p>
 *
 * <p>Класс <b>не</b> вызывает {@code now()}: если {@code currentTime} не задан,
 * его подставляет application-слой через порт {@link org.opentoggledimmer.domain.port.Clock}.
 * The class does <b>not</b> call {@code now()}: if {@code currentTime} is not
 * set, the application layer supplies it via the
 * {@link org.opentoggledimmer.domain.port.Clock} port.</p>
 *
 * @param appName       имя приложения / application name
 * @param environment   окружение / environment
 * @param userId        идентификатор пользователя / user id
 * @param sessionId     идентификатор сессии / session id
 * @param remoteAddress IP-адрес клиента / client IP address
 * @param currentTime   момент вычисления / evaluation instant
 * @param properties    произвольные поля / arbitrary fields
 */
public record EvaluationContext(
        String appName,
        String environment,
        String userId,
        String sessionId,
        String remoteAddress,
        OffsetDateTime currentTime,
        Map<String, String> properties) {

    /**
     * Компактный конструктор: нормализует {@code properties} в неизменяемую копию.
     * Compact constructor: normalizes {@code properties} into an immutable copy.
     */
    public EvaluationContext {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    /**
     * Создаёт пустой контекст.
     * Creates an empty context.
     *
     * @return пустой контекст / an empty context
     */
    public static EvaluationContext empty() {
        return new EvaluationContext(null, null, null, null, null, null, Map.of());
    }

    /**
     * Возвращает значение стандартного поля или поля из {@code properties}.
     * Returns the value of a standard field or of a {@code properties} entry.
     *
     * <hr>
     *
     * <p>Имена {@code userId}, {@code sessionId}, {@code remoteAddress},
     * {@code appName}, {@code environment} и {@code currentTime} берутся из
     * соответствующих полей; любое другое имя — из {@code properties}.
     * The names {@code userId}, {@code sessionId}, {@code remoteAddress},
     * {@code appName}, {@code environment} and {@code currentTime} are read from
     * the corresponding fields; any other name comes from {@code properties}.</p>
     *
     * @param name имя поля контекста / the context field name
     * @return значение, если найдено / the value if found
     */
    public Optional<String> get(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return switch (name) {
            case "userId" -> Optional.ofNullable(userId);
            case "sessionId" -> Optional.ofNullable(sessionId);
            case "remoteAddress" -> Optional.ofNullable(remoteAddress);
            case "appName" -> Optional.ofNullable(appName);
            case "environment" -> Optional.ofNullable(environment);
            case "currentTime" -> Optional.ofNullable(currentTime).map(OffsetDateTime::toString);
            default -> Optional.ofNullable(properties.get(name));
        };
    }

    /**
     * Создаёт копию контекста с заданным {@code currentTime}.
     * Creates a copy of the context with the given {@code currentTime}.
     *
     * @param time момент времени / the instant
     * @return новый контекст / a new context
     */
    public EvaluationContext withCurrentTime(OffsetDateTime time) {
        return new EvaluationContext(appName, environment, userId, sessionId, remoteAddress, time, properties);
    }
}