# open-toggle-dimmer

**Feature flag evaluation engine — включайте, приглушайте и выключайте функциональность без деплоя.**

Библиотека для вычисления feature-флагов: чистая доменная логика, слоистая
архитектура с портами и адаптерами, никаких фреймворков в ядре.

---

## 🤔 Что такое feature flags (и зачем «диммер»)

Представьте светильник с регулятором яркости. Feature flag — это тот же
регулятор, только для функциональности приложения: вы включаете новую кнопку
для 10% пользователей, затем для 50%, затем на полную — и всё это **без
перевыпуска кода**. А если что-то пошло не так, одним движением убавляете
яркость до нуля.

Feature flags позволяют:

- выпускать фичи постепенно (canary / gradual rollout);
- включать функциональность только для нужной аудитории;
- быстро «гасить» проблемную фичу (kill switch) без деплоя;
- проводить A/B-эксперименты на вариантах.

`open-toggle-dimmer` — это **движок принятия решений**: по описанию флагов и
контексту (кто пользователь, из какого он региона, какая версия приложения)
он отвечает на вопрос «включён ли флаг?» и «какой вариант показать?».

## ✨ Что умеет библиотека

- **Вычисление активации по контексту** (`EvaluationContext`): пользователь,
  сессия, IP, приложение, окружение, текущее время, произвольные свойства.
- **Стратегии активации**: gradual rollout, «включено для всех», `userWithId`,
  `remoteAddress` (CIDR), `applicationHostname`, legacy gradual-варианты,
  расширяемые кастомные стратегии.
- **Constraints с полным набором операторов**: `IN`/`NOT_IN`, числовые,
  датовые, строковые, SemVer, регулярные выражения и `IN_CIDR`.
- **Сегменты** — переиспользуемые наборы constraints.
- **Gradual rollout и stickiness** на MurmurHash3: один и тот же пользователь
  стабильно попадает в одну и ту же долю.
- **Варианты** с весами, payload и overrides (для A/B-тестов).
- **Зависимости флагов**: дочерний флаг включается только при нужном
  состоянии (и варианте) родителя.
- **Impression data** — события факта вычисления для аналитики.

## 🚀 Быстрый старт

Минимальный рабочий пример (Java 23+):

```java
// 1. Собираем инфраструктуру (адаптеры портов)
var flags = new InMemoryFeatureFlagRepository();
var segments = new InMemorySegmentRepository();
var clock = new SystemClock();
var random = new ThreadLocalRandomSource();
var evaluation = new FlagEvaluationService(
        new ConstraintEvaluator(),
        new StickinessCalculator(random),
        random,
        new JvmHostnameProvider());
var support = new EvaluationSupport(flags, segments, evaluation, clock);

// 2. Создаём флаг с gradual rollout на 50%
var manage = new ManageFeatureFlagService(flags, new InMemoryEventCollector(), clock);
var name = FeatureName.of("new.checkout");
manage.create(name, ProjectId.defaultProject(), true);
manage.addStrategy(name, new GradualRolloutStrategy(
        RolloutPercentage.of(50), "userId",
        GroupId.forFeature(name), List.of(), List.of(), false, 1));

// 3. Вычисляем для конкретного пользователя
var evaluate = new EvaluateFeatureService(support);
var context = new EvaluationContext(
        "my-app", "production", "user-42", null, null, null, Map.of());
boolean enabled = evaluate.isEnabled(name, context);
System.out.println("new.checkout для user-42: " + enabled);
```

## 🧱 Основные концепции

### EvaluationContext

Информация о текущем запросе. Все поля опциональны; текстовые значения —
строки. Стандартные поля (`userId`, `sessionId`, `remoteAddress`, `appName`,
`environment`, `currentTime`) читаются **только** из соответствующих полей;
произвольные данные кладутся в `properties` (custom context fields).
`currentTime` подставляется application-слоем через порт `Clock`, если не задан.

### Стратегии активации

Правило, привязанное к флагу. Несколько стратегий объединяются через **OR**.
Если стратегий нет — флаг считается активным (эквивалент `enabled`).

| Стратегия | Что делает |
| --- | --- |
| `GradualRolloutStrategy` | Включает для доли пользователей по хешу |
| `DefaultStrategy` | Включает для всех (`true`) |
| `UserIdsStrategy` | Включает для списка userId |
| `RemoteAddressStrategy` | Включает для IP из CIDR-блоков |
| `ApplicationHostnameStrategy` | Включает по хостнейму процесса |
| `LegacyGradualRolloutStrategy` | Старые gradual-варианты |
| `CustomStrategy` | Кастомная (не реализована в ядре → `false`) |

### Constraints и операторы

Constraints внутри стратегии или сегмента объединяются через **AND**.
Полный список операторов:

| Оператор | Смысл |
| --- | --- |
| `IN` / `NOT_IN` | Равенство одному из списка |
| `NUM_EQ`, `NUM_GT`, `NUM_GTE`, `NUM_LT`, `NUM_LTE` | Числовые сравнения |
| `DATE_AFTER`, `DATE_BEFORE` | Сравнение дат (для `currentTime`) |
| `STR_CONTAINS`, `STR_ENDS_WITH`, `STR_STARTS_WITH` | Подстроки |
| `SEMVER_EQ`, `SEMVER_GT`, `SEMVER_GTE`, `SEMVER_LT`, `SEMVER_LTE` | SemVer 2.0.0 |
| `REGEX` | Регулярное выражение |
| `IN_CIDR` | IP входит в CIDR-подсеть |

Флаги `inverted` (инверсия) и `caseInsensitive` (регистр для строковых
операторов) настраиваются на constraint.

### Сегменты

Сегмент — именованный набор constraints, который можно переиспользовать в
разных стратегиях. Пустой сегмент всегда вычисляется как `true`.

### Stickiness и MurmurHash

Для gradual rollout и вариантов значение хешируется как
`"<groupId>:<значение контекста>"` (UTF-8) через MurmurHash3 (x86_32):

- rollout: `seed = 0`, `normalized = hash % 100 + 1`, включение при
  `normalized <= rollout`;
- варианты: `seed = 86028157`, `normalized = hash % (сумма весов) + 1`.

Один и тот же пользователь стабильно попадает в одну и ту же долю. Менять
распределение можно сменой `groupId`.

### Варианты

Варианты дают пользователю разные версии фичи (A/B-тест). Выбор идёт так:
overrides → хеш по весам → первый вариант, чьи накопленные веса покрыли
`target`. Если флаг выключен или вариантов нет — возвращается fallback
`disabled`.

### Зависимости флагов

У флага может быть один родитель. Дочерний активен, если он сам включён и
родитель удовлетворяет зависимости (состояние `enabled`/`false` и, при
необходимости, конкретный вариант по имени).

### Impression data

Если у флага включён `impressionData`, каждое вычисление порождает событие
`IS_ENABLED`/`GET_VARIANT` для аналитики.

## 🏛 Архитектура

Проект — **Maven multi-module**. Слои гексагональной архитектуры разнесены
по модулям, чтобы границы слоёв проверялись на уровне зависимостей Maven:

```
open-toggle-dimmer                 (агрегатор, pom)
├── open-toggle-dimmer-domain      # чистая бизнес-логика (только JDK)
│   └── org.opentoggledimmer.domain
│       ├── model                  # сущности, value objects, стратегии
│       ├── event                  # доменные события
│       ├── service                # доменные сервисы вычисления
│       ├── port                   # доменные порты (Clock, RandomSource, HostnameProvider)
│       └── exception              # доменные исключения
├── open-toggle-dimmer-application # сценарии использования + порты (только JDK)
│   └── org.opentoggledimmer.application
│       ├── port.in                # входящие порты (use cases)
│       ├── port.out               # исходящие порты (репозитории, публикаторы)
│       ├── usecase                # реализации сценариев
│       └── dto                    # DTO
└── open-toggle-dimmer-bootstrap   # адаптеры и точка сборки
    └── org.opentoggledimmer.adapter
        └── out                    # in-memory репозитории, системные адаптеры
```

Зависимости модулей строго в одну сторону:

```
bootstrap → application → domain
```

- `domain` не зависит ни от одного модуля проекта и от фреймворков;
- `application` зависит только от `domain`;
- `bootstrap` содержит адаптеры и собирает всё вместе.

Почему так:

- **ядро изолировано** — его легко тестировать и менять, не трогая
  инфраструктуру;
- **порты и адаптеры** позволяют подменить хранилище или источник
  конфигурации без изменения бизнес-логики;
- **детерминизм**: время, случайность и UUID вынесены в порты, поэтому тесты
  воспроизводимы.

Направление зависимостей и изоляция слоёв дополнительно проверяются
ArchUnit-тестами в модуле `bootstrap`.

## 🛠 Как запускать

Требования: **JDK 23+** (проверено на OpenJDK 23; код совместим с JDK 25)
и Maven.

```bash
mvn test      # только тесты (по всем модулям)
mvn verify    # тесты + проверка покрытия домена (JaCoCo, ≥ 90%)
mvn install   # собрать и установить модули в локальный репозиторий
```

Отчёт о покрытии домена после `mvn verify`:

```
open-toggle-dimmer-domain/target/site/jacoco/index.html
```

Можно запускать команды только для одного модуля, например:

```bash
mvn -pl open-toggle-dimmer-domain test
```

## 🧩 Пример расширения

Добавить свой constraint-оператор можно, расширив `ConstraintOperator` и
обработку в `ConstraintEvaluator` (модуль `domain`). Кастомную стратегию —
реализовав собственный `ActivationStrategy` или зарегистрировав её через
внешний механизм, аналогично раскрытию `customStrategy` во внешних
конфигурациях.

## ❓ FAQ

**Почему флаг выключен, хотя `enabled = true`?**
Проверьте стратегии: они объединяются через OR, и хотя бы одна должна
сработать; также проверьте зависимости от родителя и constraints/сегменты.

**Пользователь «прыгает» между вариантами?**
Убедитесь, что в контексте есть `userId` (или `sessionId`) и `groupId`
стабилен. Без seed-value используется случайность.

**Что если сегмент пустой?**
Пустой сегмент всегда `true` — это поведение движка.

**Почему `NOT_IN` без значения даёт `true`?**
Так движок обрабатывает строковые списки. Для числовых списков поведение
другое — см. комментарии в `ConstraintEvaluator`.

## 📄 Лицензия

Проект распространяется под [MIT License](LICENSE).
