# 🎮 GameFit — Геймифицированный фитнес-трекер для геймеров

**GameFit** — это консольное приложение на языке Kotlin, призванное мотивировать игроков (в частности, игроков в Dota 2) заниматься спортом. Программа анализирует результаты сыгранных матчей (показатели KDA и исход игры), начисляет штрафные баллы за неудачи или слабую игру и конвертирует эти штрафы в персонализированный план физических упражнений с учётом накопленной усталости.

---

## 📑 Оглавление
1. [Концепция и механика](#-концепция-и-механика)
2. [Архитектура проекта](#-архитектура-проекта)
3. [Подробное описание модулей и файлов](#-подробное-описание-модулей-и-файлов)
   - [Модели данных (`model`)](#1-модели-данных-gamefitmodel)
   - [Передача данных DTO (`dto`)](#2-передача-данных-gamefitdto)
   - [Источники данных (`provider`)](#3-источники-данных-gamefitprovider)
   - [Хранилище данных (`repository`)](#4-хранилище-данных-gamefitrepository)
   - [Бизнес-логика и сервисы (`service`)](#5-бизнес-логика-и-сервисы-gamefitservice)
   - [Пользовательский интерфейс (`ui`)](#6-пользовательский-интерфейс-gamefitui)
   - [Конфигурация проекта](#7-конфигурация-проекта)
4. [Алгоритмы и формулы](#-алгоритмы-и-формулы)
   - [Расчёт KDA и штрафов](#расчёт-kda-и-штрафов)
   - [Динамический коэффициент нагрузки и распределение упражнений](#динамический-коэффициент-нагрузки-и-распределение-упражнений)
5. [Стек технологий и зависимости](#-стек-технологий-и-зависимости)
6. [Сборка и запуск](#-сборка-и-запуск)

---

## 💡 Концепция и механика

Идея GameFit строится на простой связке: **сыграл плохо или проиграл — сделай физические упражнения**:
1. Игрок указывает свой **SteamID** (поддерживаются форматы SteamID32 и SteamID64).
2. Приложение запрашивает последние матчи через публичное API **OpenDota** (или позволяет ввести результат вручную).
3. Новые матчи сохраняются в локальную базу данных **SQLite**, исключая повторный учёт.
4. По каждому матчу высчитывается показатель **KDA** и сравнивается с целевым значением игрока (`kdaTarget`).
5. На основе победы/поражения и качества KDA начисляются **штрафные баллы**.
6. Приложение формирует **план тренировки** (приседания, пресс, отжимания), распределяя повторения так, чтобы отработать накопленный штраф. При этом учитывается адаптация и усталость: чем больше очков уже набрано по конкретному упражнению, тем выше его коэффициент нагрузки и ниже приоритет при выборе.
7. После выполнения упражнений штраф списывается, а прогресс фиксируется в профиле.

---

## 🏗 Архитектура проекта

Структура директорий проекта:

```text
GameFit/
├── build.gradle.kts                       # Конфигурация сборки Gradle
├── settings.gradle.kts                    # Настройки проекта Gradle
├── gamefit.db                             # База данных SQLite (создаётся автоматически)
└── src/
    └── gamefit/
        ├── model/                         # Доменные сущности
        │   ├── Match.kt                   # Модель матча
        │   ├── KdaPerformance.kt          # Перечисление категорий KDA
        │   ├── Exercise.kt                # Модель упражнения с формулой нагрузки
        │   ├── PenaltyResult.kt           # Результат расчёта штрафа за матч
        │   └── UserAccount.kt             # Профиль пользователя и баланс штрафов
        ├── dto/                           # Data Transfer Objects
        │   └── DotaMatchResponse.kt       # Модель ответа API OpenDota
        ├── provider/                      # Получение матчей (сеть / ручной ввод)
        │   ├── MatchProvider.kt           # Интерфейс провайдера и ручной ввод
        │   ├── OpenDotaMatchProvider.kt   # Запросы к OpenDota через Ktor
        │   └── GetPLayerId.kt             # Консольный ввод SteamID и лимита матчей
        ├── repository/                    # Работа с базой данных
        │   ├── MatchRepository.kt         # Интерфейс репозитория матчей
        │   ├── SqliteMatchRepository.kt   # Реализация SQLite репозитория матчей
        │   ├── UserRepository.kt          # Интерфейс репозитория профилей
        │   └── SqliteUserRepository.kt    # Реализация SQLite репозитория профилей
        ├── service/                       # Бизнес-логика приложения
        │   ├── PenaltyCalculator.kt       # Логика начисления штрафных очков
        │   ├── ExercisePlanner.kt         # Алгоритм планирования тренировки
        │   └── MatchProcessingService.kt  # Оркестрация обработки матчей
        └── ui/                            # Консольный интерфейс
            ├── ConsolePrinter.kt          # Форматированный вывод в консоль
            └── Main.kt                    # Точка входа в программу и главное меню
```

---

## 📦 Подробное описание модулей и файлов

### 1. Модели данных (`gamefit.model`)

#### [`Match.kt`](src/gamefit/model/Match.kt)
Доменный класс, описывающий сыгранный матч.
- **Поля:**
  - `id: Long` — уникальный идентификатор матча.
  - `kills: Int` — количество убийств.
  - `deaths: Int` — количество смертей.
  - `assists: Int` — количество помощи.
  - `matchWon: Boolean` — победа (`true`) или поражение (`false`).
- **Свойства и функции:**
  - `kda: Double` — вычисляемое свойство KDA по формуле `(kills + assists) / max(deaths, 1)`. Использование `deaths.coerceAtLeast(1)` предотвращает деление на ноль при игре без смертей.

#### [`KdaPerformance.kt`](src/gamefit/model/KdaPerformance.kt)
Перечисление (`enum class`), определяющее категорию качества игры:
- `GOOD` — KDA выше или равен целевому значению.
- `OK` — KDA выше 1.0, но ниже целевого значения.
- `BAD` — KDA 1.0 или ниже.

#### [`Exercise.kt`](src/gamefit/model/Exercise.kt)
Модель физического упражнения.
- **Поля:**
  - `name: String` — название упражнения (например, "Squats", "Abs", "Push-ups").
  - `points: Int` — сколько штрафных очков списывает одно повторение упражнения.
  - `baseLoadCoefficient: Double` — базовый коэффициент физической нагрузки.
- **Функции:**
  - `calculateLoadCoefficient(donePoints: Int): Double` — рассчитывает текущий динамический коэффициент нагрузки с учётом уже выполненного объёма работы:
    $$\text{loadCoefficient} = \text{baseLoadCoefficient} \times \left(1 + \frac{\text{donePoints}}{100.0}\right)$$

#### [`PenaltyResult.kt`](src/gamefit/model/PenaltyResult.kt)
Неизменяемый класс данных, содержащий полный отчёт об оценке матча:
- `kda: Double` — фактический KDA в матче.
- `target: Double` — целевой KDA, с которым производилось сравнение.
- `kdaResult: KdaPerformance` — качественная оценка KDA (`GOOD`, `OK`, `BAD`).
- `matchWon: Boolean` — завершился ли матч победой.
- `penaltyPoints: Int` — итоговое количество начисленных штрафных очков.

#### [`UserAccount.kt`](src/gamefit/model/UserAccount.kt)
Основной класс пользователя, управляющий его состоянием и тренировочным процессом.
- **Поля:**
  - `username: String` — имя пользователя.
  - `penalty: Int` — текущий баланс штрафных баллов (доступен только для чтения снаружи).
  - `kdaTarget: Double` — персональная цель по KDA (по умолчанию `2.0`).
  - `currentPlan: Map<Exercise, Int>` — назначенный план тренировки (упражнение $\to$ число повторений).
  - `matches: MutableList<Match>` — история проведённых матчей.
  - `exerciseProgress: MutableMap<String, Int>` — прогресс по каждому упражнению (название $\to$ суммарно отработанные баллы).
- **Функции:**
  - `getProgress(exerciseName: String): Int` — возвращает суммарно отработанные баллы по выбранному упражнению.
  - `getAllProgress(): Map<String, Int>` — возвращает копию карты прогресса по всем упражнениям.
  - `assignPlan(plan: Map<Exercise, Int>): Boolean` — устанавливает текущий тренировочный план, если есть штрафы (`penalty > 0`). Возвращает `false`, если штрафа нет.
  - `addPenalty(penaltyValue: Int)` — увеличивает баланс штрафных очков на `penaltyValue`.
  - `completeWork(): Map<Exercise, Int>` — фиксирует выполнение текущего плана: списывает штрафные очки, переносит выполненные баллы в `exerciseProgress`, сбрасывает план и возвращает отчёт о выполненной работе.
  - `addMatch(match: Match)` — добавляет матч в историю пользователя.
  - `getMatches(): List<Match>` — возвращает неизменяемый список матчей пользователя.
  - `changeTarget(target: Double): Boolean` — изменяет целевой KDA, если переданное значение $> 0$.

---

### 2. Передача данных (`gamefit.dto`)

#### [`DotaMatchResponse.kt`](src/gamefit/dto/DotaMatchResponse.kt)
DTO-класс с аннотациями `kotlinx.serialization` для десериализации JSON-ответов от API OpenDota (`/players/{account_id}/recentMatches`).
- **Поля:**
  - `matchId: Long` (`@SerialName("match_id")`)
  - `playerSlot: Int` (`@SerialName("player_slot")`)
  - `radiantWin: Boolean` (`@SerialName("radiant_win")`)
  - `kills: Int`
  - `deaths: Int`
  - `assists: Int`
- **Функции:**
  - `toMatch(): Match` — преобразует сетевой DTO в доменную модель `Match`. В Dota 2 слоты игроков с 0 по 127 принадлежат команде Radiant, а со 128 по 255 — Dire. Функция определяет победу игрока: `isWin = (playerSlot < 128) == radiantWin`.

---

### 3. Источники данных (`gamefit.provider`)

#### [`MatchProvider.kt`](src/gamefit/provider/MatchProvider.kt)
- **Интерфейс `MatchProvider`**:
  - `fun getMatches(): List<Match>` — контракт на получение списка матчей.
- **Класс `ManualMatchProvider`**:
  - Реализация `MatchProvider` для ручного ввода результатов матча через консоль.
  - Принимает строку вида `"10 2 5 win"` (убийства, смерти, ассисты, исход).
  - Генерирует уникальный `id` на основе `System.currentTimeMillis()`.
  - Метод `parseMatchResult(value: String): Boolean?` парсит победу/поражение по строковым эквивалентам (`win`/`w`/`true`/`1` против `loss`/`l`/`false`/`0`).

#### [`GetPLayerId.kt`](src/gamefit/provider/GetPLayerId.kt)
Вспомогательные консольные функции:
- `getPlayerId(): Int`:
  - Запрашивает у пользователя формат SteamID (1 — SteamID32, 2 — SteamID64).
  - При выборе SteamID64 конвертирует его в SteamID32 путём вычитания базы `76561197960265728L`.
- `getPlayerLim(): Int`:
  - Запрашивает желаемое количество недавних матчей для импорта (до 20).

#### [`OpenDotaMatchProvider.kt`](src/gamefit/provider/OpenDotaMatchProvider.kt)
Реализация `MatchProvider`, выполняющая HTTP-запросы к REST API OpenDota.
- **Конструктор:**
  - `accountId: Long` — идентификатор игрока в Dota 2.
  - `limit: Int` — максимальное число запрашиваемых последних матчей.
  - `client: HttpClient` — Ktor HTTP-клиент (по умолчанию `CIO`).
- **Функции:**
  - `getMatches(): List<Match>` — выполняет блокирующий вызов (`runBlocking`) к `https://api.opendota.com/api/players/$accountId/recentMatches`, забирает `limit` записей и трансформирует их в `Match`. В случае ошибки выводит сообщение и возвращает пустой список.
  - `createDefaultClient(): HttpClient` — настраивает `HttpClient(CIO)` с плагином `ContentNegotiation` и JSON-сериализатором (`ignoreUnknownKeys = true`, `coerceInputValues = true`).

---

### 4. Хранилище данных (`gamefit.repository`)

#### [`MatchRepository.kt`](src/gamefit/repository/MatchRepository.kt)
Интерфейс репозитория для постоянного хранения матчей и предотвращения дублирования:
- `fun isProceed(id: Long): Boolean` — проверяет, был ли матч с данным `id` уже обработан ранее.
- `fun save(match: Match)` — сохраняет матч в хранилище.
- `fun getAll(): List<Match>` — возвращает все сохранённые матчи.
- `fun clear()` — очищает хранилище.

#### [`SqliteMatchRepository.kt`](src/gamefit/repository/SqliteMatchRepository.kt)
Полноценная реализация `MatchRepository` на базе локальной базы данных **SQLite** с использованием JDBC драйвера `org.xerial:sqlite-jdbc`.
- **Инициализация (`init`)**:
  - Создаёт таблицу `matches` (если не существует):
    ```sql
    CREATE TABLE IF NOT EXISTS matches (
        id INTEGER PRIMARY KEY,
        kills INTEGER NOT NULL,
        deaths INTEGER NOT NULL,
        assists INTEGER NOT NULL,
        match_won INTEGER NOT NULL
    );
    ```
- **Функции:**
  - `isProceed(matchId: Long): Boolean` — проверяет наличие записи по `SELECT 1 FROM matches WHERE id = ? LIMIT 1`.
  - `save(match: Match)` — выполняет `INSERT OR IGNORE INTO matches (id, kills, deaths, assists, match_won) VALUES (?, ?, ?, ?, ?)`.
  - `getAll(): List<Match>` — считывает все строки из таблицы и восстанавливает список объектов `Match`.
  - `clear()` — удаляет все записи (`DELETE FROM matches`).
  - `close()` — корректно закрывает соединение с БД.

#### [`UserRepository.kt`](src/gamefit/repository/UserRepository.kt)
Интерфейс репозитория для постоянного хранения профиля игрока:
- `fun getOrCreateUser(username: String): UserAccount` — загружает существующий профиль из БД или создаёт новый по умолчанию.
- `fun saveUser(user: UserAccount)` — сохраняет текущий баланс штрафов, цель KDA и прогресс выполнения упражнений.
- `fun clear()` — очищает сохранённые профили при общем сбросе БД.

#### [`SqliteUserRepository.kt`](src/gamefit/repository/SqliteUserRepository.kt)
SQLite-реализация репозитория профилей с таблицами `users` (штрафы и целевой KDA) и `user_progress` (прогресс выполненных очков по каждому упражнению). Предотвращает потерю штрафов и усталости между перезапусками приложения.

---

### 5. Бизнес-логика и сервисы (`gamefit.service`)

#### [`PenaltyCalculator.kt`](src/gamefit/service/PenaltyCalculator.kt)
Сервис расчёта штрафа за матч на основе соотношения KDA к цели (`target`) и исхода игры.
- **Функция:**
  - `calculatePenalty(match: Match, target: Double): PenaltyResult`
  - Определяет градацию `KdaPerformance`:
    - `currKda >= target` $\to$ `GOOD`
    - `currKda > 1.0` $\to$ `OK`
    - иначе $\to$ `BAD`
  - Начисляет штрафные баллы в соответствии с матрицей:
    - `GOOD`: Победа = 0, Поражение = 15
    - `OK`: Победа = 15, Поражение = 30
    - `BAD`: Победа = 30, Поражение = 40

#### [`ExercisePlanner.kt`](src/gamefit/service/ExercisePlanner.kt)
Сервис умного составления плана тренировки.
- **Конструктор:**
  - `exercises: List<Exercise>` — список доступных упражнений.
- **Функции:**
  - `calculatePriority(exercise: Exercise, userDonePoints: Int): Double` — вычисляет текущую привлекательность упражнения:
    $$\text{Priority} = \frac{\text{exercise.points}}{\text{exercise.calculateLoadCoefficient(userDonePoints)}}$$
    Чем выше отдача упражнения в очках и чем меньше пользователь выполнял его ранее, тем выше приоритет.
  - `planExercise(remainingPoints: Int, user: UserAccount): Map<Exercise, Int>` — алгоритм круговой тренировки (Circuit Training):
    - Распределяет нагрузку небольшими сетами (по 5 повторений).
    - Динамически симулирует рост усталости после каждого сета, снижая приоритет уже добавленного упражнения и подключая упражнения на другие группы мышц.
    - Обеспечивает сбалансированный комплекс на все группы мышц без зависших остатков.

#### [`MatchProcessingService.kt`](src/gamefit/service/MatchProcessingService.kt)
Сервисный слой, объединяющий получение матчей, работу с БД, начисление штрафов и обновление пользователя.
- **Зависимости:** `PenaltyCalculator`, `MatchRepository`.
- **Функции:**
  - `processMatches(account: UserAccount, matches: List<Match>): List<PenaltyResult>`:
    - Проходит по списку матчей.
    - Проверяет `repository.isProceed(match.id)` — если матч уже был обработан, он пропускается.
    - Сохраняет новый матч в базу через `repository.save(match)` и добавляет в историю аккаунта `account.addMatch(match)`.
    - Рассчитывает штраф через `calculator.calculatePenalty(match, account.kdaTarget)`.
    - Добавляет штраф к балансу пользователя `account.addPenalty(...)`.
    - Возвращает список результатов для отображения пользователю.

---

### 6. Пользовательский интерфейс (`gamefit.ui`)

#### [`ConsolePrinter.kt`](src/gamefit/ui/ConsolePrinter.kt)
Набор чистых функций для форматированного вывода данных в терминал:
- `printPenaltyResult(result: PenaltyResult)` — выводит подробную карточку матча (KDA, целевой KDA, оценка, WIN/LOSS, штраф).
- `printMatchHistory(matches: List<Match>)` — выводит список сыгранных матчей.
- `printAssignedPlan(plan: Map<Exercise, Int>)` — выводит назначенный план упражнений и число повторений.
- `printCompletedWork(plan: Map<Exercise, Int>)` — выводит отчёт о выполненной тренировке.
- `printNoPenalty()`, `printNoAssignedPlan()`, `printInvalidTarget()`, `printTargetChanged(target)`, `printPenalty(penalty)` — служебные информационные сообщения.

#### [`Main.kt`](src/gamefit/ui/Main.kt)
Главная точка входа в консольное приложение (`fun main()`):
- Инициализирует репозиторий SQLite (`SqliteMatchRepository`), провайдер матчей (`OpenDotaMatchProvider`), сервисы и профиль игрока `UserAccount("John")`.
- Задаёт базовый каталог упражнений:
  - `Squats` (Приседания): 1 очко за повторение, базовый коэфф. 0.5
  - `Abs` (Пресс): 2 очка за повторение, базовый коэфф. 1.0
  - `Push-ups` (Отжимания): 3 очка за повторение, базовый коэфф. 1.5
- Запускает бесконечный интерактивный цикл с меню:
  - `1. Import match` — загрузка новых матчей из OpenDota и расчёт штрафов.
  - `2. Show penalty` — отображение текущего баланса штрафных баллов.
  - `3. Complete penalty` — отметка о выполнении тренировки и списание штрафа.
  - `4. assign plan` — генерация и назначение плана тренировки под текущий штраф.
  - `5. show match history` — просмотр истории матчей в текущей сессии.
  - `6. change KDA target` — изменение целевого KDA игрока.
  - `7. reset DataBase` — полная очистка локальной базы данных.
  - `0. exit` — выход из приложения.

---

### 7. Конфигурация проекта

#### [`build.gradle.kts`](build.gradle.kts)
Файл сборки Gradle Kotlin DSL.
- Плагины: `kotlin("jvm")`, `kotlin("plugin.serialization")`, `application`.
- Зависимости:
  - `io.ktor:ktor-client-core:3.6.0` — ядро HTTP-клиента.
  - `io.ktor:ktor-client-cio:3.6.0` — асинхронный движок CIO.
  - `io.ktor:ktor-client-content-negotiation:3.6.0` — согласование контента (JSON).
  - `io.ktor:ktor-serialization-kotlinx-json:3.6.0` — сериализатор JSON.
  - `org.xerial:sqlite-jdbc:3.45.1.0` — драйвер SQLite для работы с БД.
- Исходный код настроен на директорию `src`.

---

## 📐 Алгоритмы и формулы

### Расчёт KDA и штрафов

1. **KDA:**
   $$\text{KDA} = \frac{\text{Kills} + \text{Assists}}{\max(\text{Deaths}, 1)}$$

2. **Градация качества KDA:**
   - $\text{KDA} \ge \text{Target} \implies \text{GOOD}$
   - $1.0 < \text{KDA} < \text{Target} \implies \text{OK}$
   - $\text{KDA} \le 1.0 \implies \text{BAD}$

3. **Матрица начисления штрафных очков:**

| Оценка KDA | Победа (Win) | Поражение (Loss) |
| :--- | :---: | :---: |
| **GOOD** | 0 очков | 15 очков |
| **OK** | 15 очков | 30 очков |
| **BAD** | 30 очков | 40 очков |

---

### Динамический коэффициент нагрузки и распределение упражнений

1. **Коэффициент нагрузки с учётом усталости:**
   $$K_{\text{load}}(E) = K_{\text{base}}(E) \times \left(1 + \frac{\text{Progress}(E)}{100}\right)$$
   *Пример:* если игрок уже выполнил приседаний на 100 очков, его коэффициент нагрузки для приседаний удваивается.

2. **Приоритет выбора упражнения в планировщике:**
   $$\text{Priority}(E) = \frac{\text{Points}(E)}{K_{\text{load}}(E)}$$
   Планировщик отдаёт предпочтение упражнениям, которые приносят больше очков за повторение при наименьшем текущем утомлении, тем самым сбалансированно распределяя нагрузку по разным группам мышц.

---

## 🛠 Стек технологий и зависимости

| Компонент | Технология / Библиотека | Назначение |
| :--- | :--- | :--- |
| **Язык** | Kotlin (JVM) 2.4+ | Основной язык разработки |
| **Сборщик** | Gradle (Kotlin DSL) | Сборка и управление зависимостями |
| **Сеть** | Ktor Client 3.6.0 (CIO) | Асинхронные HTTP-запросы к OpenDota API |
| **Сериализация** | kotlinx.serialization 2.4+ | Десериализация ответов JSON |
| **База данных** | SQLite JDBC 3.45.1.0 | Локальное хранение матчей и дедупликация |

---

## 🚀 Сборка и запуск

### Требования
- Установленная Java JDK 17 или выше.

### Сборка проекта
В корневой папке проекта выполните:
```bash
./gradlew build
```

### Запуск приложения
```bash
./gradlew run --console=plain
```
При запуске программа запросит:
1. Формат SteamID (SteamID32 или SteamID64).
2. Сам SteamID игрока (открытый профиль Dota 2 с включённой опцией «Общедоступная история матчей»).
3. Лимит последних матчей для первичного импорта.

После этого откроется интерактивное консольное меню с выбором действий.
