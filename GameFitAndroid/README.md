# GameFit Android

<!-- Документация Android-модуля GameFit. -->

Это Android-версия проекта GameFit.

## Структура проекта

```
GameFitAndroid/
├── app/                          # Android-модуль приложения
│   ├── build.gradle.kts         # Gradle-конфигурация модуля
│   ├── src/main/
│   │   ├── AndroidManifest.xml  # Манифест приложения
│   │   ├── java/com/gamefit/android/
│   │   │   └── MainActivity.kt  # Главный экран
│   │   └── res/                 # Ресурсы (layouts, strings)
├── build.gradle.kts             # Корневой Gradle
└── settings.gradle.kts          # Настройки проекта
```

## Как открыть проект

1. Запустите Android Studio
2. Выберите "Open" или "Open an Existing Project"
3. Укажите папку `GameFitAndroid`
4. Дождитесь синхронизации Gradle

## Требования

- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 34

## Текущий статус

Реализована переносимая Kotlin-бизнес-логика: модели матча, пользователя и упражнений,
расчёт штрафа и планировщик тренировок. Кнопки ручного ввода матча, назначения и
завершения тренировки, просмотра истории, изменения KDA и сброса данных работают
с локальным состоянием приложения. Подключены импорт матчей из OpenDota через Ktor и
локальная Room-база данных.

Следующие шаги:
- Добавить синхронизацию локальной базы с backend
- Перенести backend-хранилище на MySQL/PostgreSQL при появлении серверной части
