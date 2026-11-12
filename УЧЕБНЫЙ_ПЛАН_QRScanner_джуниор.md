# QRScanner как учебный проект — от создания приложения до сборки APK

**Дата:** 12.11.2026
**Для кого:** новичок, который знает основы Java и хочет пройти полный путь Android-разработки на реальном проекте.
**Что используем:** этот проект, ТЗ, `PROJECT_ROADMAP.md`, отчёт по ТЗ и `CLASS_DEPENDENCIES.md`.
**Формат:** 12 модулей, каждый — теория + практика + критерий готовности. Можно идти последовательно или брать отдельные модули.

---

## 0. Кратко: чему можно научиться на этом проекте

| Навык | Что именно освоите |
|---|---|
| Android-фундамент | Activity, Fragment, жизненный цикл, Intents, Manifest, ресурсы |
| UI | XML-разметка, LinearLayout/ConstraintLayout, Toolbar, Material, RecyclerView, адаптеры |
| Камера | Runtime permissions, ZXing, `DecoratedBarcodeView`, фонарик, зум, смена камеры |
| Данные | Room, Entity/DAO/Database, TypeConverters, LiveData, SQLite |
| Архитектура | MVVM, ViewModel, Repository, single source of truth, слои |
| Асинхронность | Почему нельзя в UI-потоке, Executor, Handler, AsyncTask как legacy, ANR |
| Списки | RecyclerView, ViewHolder, DiffUtil, удаление, пустые состояния |
| Файлы | Внутреннее/внешнее хранилище, SAF, ContentResolver, FileProvider, `ACTION_SEND` |
| Форматы | JSON, Gson/org.json, UTF-8, CSV, Excel/Apache POI |
| Инвентаризация | Формы, валидация, фрагменты, общее состояние, выбор форматов |
| Качество | Logcat, отладчик, Layout Inspector, Database Inspector, JUnit, Espresso, lint |
| Релиз | Gradle, signing, R8/ProGuard, APK/AAB, CI, версионирование |
| Инженерия | Анализ ТЗ, чтение чужого кода, оценка, декомпозиция, техдолг, документация |

**Главная ценность проекта:** он не «идеальный пример», а **реальный Legacy-код с ошибками**. На нём можно научиться не только писать, но и:
- читать чужой код;
- находить причины багов;
- отличать рабочий каркас от готового продукта;
- планировать исправления;
- доводить приложение до релиза.

---

## 1. Что нужно знать до старта

**Обязательно:**

- Java: классы, интерфейсы, наследование, коллекции (`List`, `Map`), исключения, generics на базовом уровне.
- Git: `clone`, `add`, `commit`, `branch`, `merge`.
- Понимание, что такое HTTP/JSON/файл — на уровне пользователя.

**Желательно:**

- SQL: `SELECT`, `INSERT`, `DELETE`, `WHERE`, `ORDER BY`.
- Основы ООП: инкапсуляция, композиция, зависимости.
- Английский на уровне чтения документации.

**Инструменты:**

- Android Studio (актуальная версия).
- JDK 17 (требование AGP 8.x).
- Android SDK 34.
- Эмулятор или реальное устройство.
- Git.
- По желанию: Postman, DB Browser for SQLite.

**Время:**

- 8 недель по 10–12 часов в неделю — интенсив.
- 12 недель по 6–8 часов в неделю — спокойный темп.
- Только исправление проекта без «с нуля» — 3–4 недели.

---

## 2. Две траектории обучения

### Траектория A. «Повторить с нуля»

Вы создаёте похожее приложение **QRScanner Learning** с нуля: пустой проект → экраны → камера → БД → список → экспорт → релиз.

**Плюсы:** вы проходите весь путь создания, лучше понимаете, откуда берутся файлы и классы.
**Минусы:** больше времени; можно не дойти до архитектуры.

### Траектория B. «Изучать и исправлять готовый проект»

Вы берёте этот репозиторий, читаете ТЗ, отчёт и `PROJECT_ROADMAP.md`, исправляете ошибки по этапам.

**Плюсы:** быстрее видите реальные проблемы; учитесь читать чужой код и работать с техдолгом.
**Минусы:** можно «застрять» в чужой архитектуре, не поняв, как строить с нуля.

### Рекомендуемый гибрид

1. **60% времени** — траектория A: создаёте свои модули с нуля.
2. **40% времени** — траектория B: сравниваете с этим проектом и исправляете его.
3. После каждого модуля задаёте вопрос: «Как это сделано в QRScanner? Что здесь плохо и почему?»

Так вы получаете и навык создания, и навык работы с реальным кодом.

---

## 3. Карта тем: что где изучать

| Область | Темы | Файлы проекта | Уровень |
|---|---|---|---|
| Gradle и сборка | `build.gradle`, зависимости, build types, APK | `build.gradle`, `app/build.gradle`, `settings.gradle` | 1 |
| Manifest | Activity, permissions, FileProvider, theme | `AndroidManifest.xml` | 1 |
| UI | Layouts, ресурсы, темы, Toolbar, Material | `res/layout`, `res/values` | 1 |
| Activity | Жизненный цикл, Intents, back stack | `MainActivity`, `ScanActivity`, `SendActivity` | 1 |
| Fragment | `FragmentManager`, back stack, `requireActivity()` | `ManualInventoryFragment`, `ExcelSendFragment` | 2 |
| Permissions | Runtime permissions, отказ, `onRequestPermissionsResult` | `ScanActivity.java` | 2 |
| Камера | ZXing, `DecoratedBarcodeView`, `BarcodeCallback` | `ScanActivity.java`, `activity_scan.xml` | 2 |
| Room | Entity, DAO, Database, TypeConverters, LiveData | `data/local/*` | 2 |
| MVVM | ViewModel, LiveData, Repository, состояние | `ui/viewmodels/*`, `QRCodeRepository` | 2 |
| Асинхронность | UI-поток, Executor, Handler, ANR | `QRCodeRepository`, `ExcelExportService` | 2 |
| RecyclerView | Adapter, ViewHolder, DiffUtil, click | `ScannedCodesAdapter`, `ListScannedActivity` | 2 |
| JSON | Парсинг, числа, объекты, ошибки, UTF-8 | `InventoryViewModel`, `JsonInventoryActivity` | 2 |
| Файлы | SAF, `ContentResolver`, `FileProvider`, `ACTION_SEND` | `JsonInventoryActivity`, `ExcelExportService` | 3 |
| Excel | CSV → Apache POI, `SXSSFWorkbook`, ячейки | `ExcelExportService` | 3 |
| Тестирование | JUnit, Espresso, тестовые двойники | нет тестов — добавите сами | 3 |
| Релиз | Signing, R8, AAB, CI | `app/build.gradle`, `proguard-rules.pro` | 3 |
| Инженерия | ТЗ → код → тест → релиз, оценка, техдолг | `README.MD`, ТЗ, `PROJECT_ROADMAP.md` | 3 |

---

## 4. Пошаговый план: 12 модулей

### Модуль 0. Инструменты и первая сборка (2–4 часа)

**Цель:** понять, как проект превращается в APK.

**Теория:**

- Android Studio, SDK, JDK, Gradle.
- Debug vs Release.
- Что такое APK/AAB.
- `adb`, Logcat, эмулятор/устройство.

**Практика:**

- [ ] Установить Android Studio и JDK 17.
- [ ] Открыть проект, дождаться Gradle Sync.
- [ ] Собрать: `gradlew.bat assembleDebug`.
- [ ] Установить: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- [ ] Запустить и посмотреть Logcat.
- [ ] Прочитать `app/build.gradle` и `AndroidManifest.xml`.
- [ ] Объяснить своими словами, что такое `minSdk`, `targetSdk`, `compileSdk`.

**Критерий готовности:** APK собирается и запускается; вы понимаете, где лежит готовый файл и как посмотреть логи.

**Что изучаете:** Gradle, build variants, APK, adb, Logcat.

---

### Модуль 1. Создание проекта и структура (3–4 часа)

**Цель:** создать свой проект с нуля и понять его структуру.

**Теория:**

- `File → New → New Project → Empty Views Activity` (Java).
- Package name, `minSdk 26`, `compileSdk 34`.
- Структура `app/src/main/java`, `res`, `AndroidManifest.xml`.
- Ресурсы: `strings.xml`, `colors.xml`, `themes.xml`.
- R-класс.

**Практика:**

- [ ] Создать проект `QRScannerLearning`.
- [ ] Добавить зависимости постепенно:
  - `appcompat`, `material`, `constraintlayout`;
  - `recyclerview`;
  - `lifecycle-viewmodel`, `lifecycle-livedata`;
  - `room-runtime`, `room-compiler`;
  - `zxing-android-embedded`, `zxing-core`;
  - `gson`.
  - Apache POI — **позже**, на модуле 7.
- [ ] Создать пакеты: `ui.activities`, `ui.fragments`, `ui.viewmodels`, `ui.adapters`, `data.local`, `data.repositories`, `data.remote`, `models`, `utils`.
- [ ] Создать `MainActivity` с тремя кнопками.
- [ ] Собрать APK.

**Критерий готовности:** приложение открывается, на экране 3 кнопки, проект собирается.

**Что изучаете:** структура Android-проекта, Gradle-зависимости, ресурсы, R-класс.

---

### Модуль 2. UI, ресурсы, Activity, Intents (6–8 часов)

**Цель:** сделать все экраны и навигацию.

**Теория:**

- `Activity` и жизненный цикл: `onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`.
- Layouts: `LinearLayout`, `RelativeLayout`, `ConstraintLayout`, `ScrollView`.
- `TextView`, `Button`, `EditText`, `Toolbar`.
- `Intent`: явный и неявный, `startActivity`, `finish`, back stack.
- Ресурсы и локализация.
- Ориентация и сохранение состояния.

**Практика:**

- [ ] Создать пустые экраны: `ScanActivity`, `ListScannedActivity`, `InventoryActivity`, `JsonInventoryActivity`, `SendActivity`.
- [ ] Создать layout для каждого.
- [ ] Настроить переходы из главного меню.
- [ ] Добавить Toolbar и кнопку «Назад».
- [ ] Проверить поворот экрана.
- [ ] Вынести все строки в `strings.xml`.

**Критерий готовности:** работают все переходы по схеме ТЗ; поворот не приводит к падению; нет жёстко заданных строк в layout.

**Что изучаете:** Activity lifecycle, Intents, layouts, ресурсы, навигация.

---

### Модуль 3. Камера, разрешения, ZXing (8–10 часов)

**Цель:** научиться работать с камерой и runtime permissions.

**Теория:**

- `CAMERA` permission, `checkSelfPermission`, `requestPermissions`.
- `onRequestPermissionsResult`, отказ и повторный запрос.
- ZXing: `DecoratedBarcodeView`, `decodeContinuous`, `BarcodeCallback`.
- `onResume`/`onPause` камеры.
- Фонарик, зум, переключение камеры.
- Дедупликация сканов.

**Практика:**

- [ ] Добавить `CAMERA` в манифест.
- [ ] Реализовать запрос разрешения.
- [ ] Подключить `DecoratedBarcodeView`.
- [ ] Сканировать один код и показывать результат.
- [ ] Реализовать пакетный режим.
- [ ] Добавить дебаунс (не добавлять один код дважды).
- [ ] Исправить двойной `resume()`.
- [ ] Добавить фонарик через ZXing.
- [ ] Добавить зум.
- [ ] Реализовать переключение камеры или убрать кнопку.

**Критерий готовности:** одиночный и пакетный режимы работают; отказ в разрешении обработан; один код в кадре не дублируется; камера освобождается при выходе.

**Что изучаете:** permissions, camera lifecycle, ZXing, edge cases.

---

### Модуль 4. Room и работа с данными (8–10 часов)

**Цель:** сохранять данные локально и читать их.

**Теория:**

- SQLite и Room.
- `@Entity`, `@PrimaryKey`, `@Dao`, `@Database`.
- `@Insert`, `@Query`, `@Delete`, `@Update`.
- `LiveData` из Room.
- `@TypeConverters` для `Date`.
- Синглтон базы.
- Почему нельзя работать с БД в UI-потоке.
- `Executor`, `AsyncTask` как устаревший подход.

**Практика:**

- [ ] Создать `QRCodeEntity`.
- [ ] Создать `QRCodeDao` с CRUD.
- [ ] Создать `QRCodeDatabase` с синглтоном.
- [ ] Создать `DateConverter`.
- [ ] Вставить скан в БД.
- [ ] Прочитать список через `LiveData`.
- [ ] Проверить, что данные сохраняются после перезапуска.
- [ ] Посмотреть сгенерированный `QRCodeDao_Impl`.

**Критерий готовности:** скан сохраняется, список читается из БД, данные не теряются после перезапуска; вы понимаете, зачем нужен DAO.

**Что изучаете:** Room, SQL, LiveData, потоки, синглтоны.

---

### Модуль 5. MVVM, ViewModel, Repository (8–10 часов)

**Цель:** разделить UI и данные, понять, почему в проекте «пустой список».

**Теория:**

- MVVM: View → ViewModel → Model/Repository.
- `ViewModel`, `ViewModelProvider`, `AndroidViewModel`.
- `LiveData`, `MutableLiveData`, наблюдатели.
- Repository как единый источник данных.
- Single source of truth.
- Маппинг Entity ↔ Model.
- Почему `new QRCodeRepository()` в каждом ViewModel ломает сессию.

**Практика:**

- [ ] Создать `QRCode` (модель) и `QRCodeEntity`.
- [ ] Создать `QRCodeRepository`.
- [ ] Сделать репозиторий синглтоном.
- [ ] Подключить `ScanViewModel`, `ListScannedViewModel`, `SendViewModel` к одному репозиторию.
- [ ] Исправить: одиночный скан добавляется в сессию.
- [ ] Проверить цепочку: скан → список → отправка.

**Критерий готовности:** список видит коды из сканера; отправка видит те же коды; вы можете объяснить, почему старый код показывал пустой список.

**Что изучаете:** MVVM, ViewModel, LiveData, Repository, состояние приложения.

---

### Модуль 6. RecyclerView и список (6–8 часов)

**Цель:** сделать качественный список с удалением и пустым состоянием.

**Теория:**

- `RecyclerView`, `LayoutManager`, `Adapter`, `ViewHolder`.
- `onCreateViewHolder`, `onBindViewHolder`, `getItemCount`.
- `DiffUtil` / `ListAdapter`.
- Клик и удаление.
- Пустое состояние.
- Доступность: `contentDescription`.

**Практика:**

- [ ] Создать `item_scanned_code.xml`.
- [ ] Создать `ScannedCodesAdapter`.
- [ ] Подключить `RecyclerView`.
- [ ] Реализовать удаление.
- [ ] Показать пустое состояние.
- [ ] (Продвинуто) Перейти на `DiffUtil`.

**Критерий готовности:** список отображает коды; удаление работает; пустой список показывает понятное сообщение; нет `notifyDataSetChanged()` в продвинутой версии.

**Что изучаете:** RecyclerView, адаптеры, списки, UX.

---

### Модуль 7. JSON, файлы, экспорт (10–12 часов)

**Цель:** научиться читать файлы, парсить JSON и выгружать данные.

**Теория:**

- Внутреннее и внешнее хранилище: `getFilesDir()`, `getExternalFilesDir()`.
- SAF: `ACTION_GET_CONTENT`, `OpenDocument`, `ContentResolver`.
- `InputStreamReader` и UTF-8.
- JSON: `org.json`, Gson, массивы, объекты, числа, ошибки.
- CSV — простой формат для старта.
- Apache POI: `XSSFWorkbook`, `SXSSFWorkbook`, ячейки, даты.
- `FileProvider`, `ACTION_SEND`, MIME-типы.
- Фоновый экспорт: `Executor`, `Handler`, прогресс, ANR.

**Практика:**

- [ ] Прочитать JSON-файл через SAF.
- [ ] Распарсить массив и объект `{items:[...]}`.
- [ ] Обработать числа и битый JSON.
- [ ] Показать список загруженных элементов.
- [ ] Экспортировать в CSV — как упражнение.
- [ ] Экспортировать в Excel через POI.
- [ ] Убрать `autoSizeColumn()` и задать ширины.
- [ ] Добавить `FileProvider` и `ACTION_SEND`.
- [ ] Выполнить экспорт в фоне.

**Критерий готовности:** JSON загружается, Excel/CSV создаются, файл открывается и отправляется, интерфейс не зависает.

**Что изучаете:** файлы, JSON, Excel, шаринг, фоновые задачи, обработка ошибок.

---

### Модуль 8. Инвентаризация, формы, фрагменты (8–10 часов)

**Цель:** собрать полноценный сценарий инвентаризации.

**Теория:**

- `Fragment`, `FragmentManager`, `addToBackStack`.
- `TextInputLayout`, `TextInputEditText`.
- Валидация формы.
- `Dialog`, `Snackbar`, `Toast`.
- Общее состояние между экранами.
- Выбор элемента списка.

**Практика:**

- [ ] Создать `ManualInventoryFragment`.
- [ ] Создать `ApprovedFormatFragment`.
- [ ] Реализовать `RecyclerView.Adapter` для форматов.
- [ ] Хранить выбранный формат.
- [ ] Показывать добавленные позиции.
- [ ] Реализовать удаление/редактирование.
- [ ] Сделать общий `InventoryRepository`.

**Критерий готовности:** JSON-инвентаризация, ручной ввод и выбор формата работают; данные не теряются при переходе между экранами.

**Что изучаете:** фрагменты, формы, валидация, общее состояние.

---

### Модуль 9. Отладка и тесты (8–10 часов)

**Цель:** научиться находить ошибки и защищать код тестами.

**Теория:**

- Logcat, точки останова, Debugger.
- Layout Inspector, Database Inspector.
- StrictMode, ANR, чтение stack trace.
- JUnit, assertions, тестовые двойники.
- Espresso — базовые UI-тесты.
- Lint, code style, рефакторинг.

**Практика:**

- [ ] Найти ошибку сессии через отладчик.
- [ ] Посмотреть БД через Database Inspector.
- [ ] Включить StrictMode и найти операции в UI-потоке.
- [ ] Написать unit-тесты для сессии.
- [ ] Написать unit-тесты для JSON-парсера.
- [ ] Написать простой Espresso-тест навигации.
- [ ] Запустить lint и исправить 3 предупреждения.

**Критерий готовности:** есть тесты, которые падают при регрессии; вы умеете читать stack trace и находить причину.

**Что изучаете:** отладка, тестирование, качество кода.

---

### Модуль 10. Release, R8, signing, CI (6–8 часов)

**Цель:** собрать подписанный релиз и автоматизировать проверку.

**Теория:**

- `versionCode`/`versionName`.
- Keystore, `signingConfigs`, переменные окружения.
- R8/ProGuard, keep-правила.
- `assembleRelease`, AAB.
- Установка release APK.
- GitHub Actions, кэш Gradle.

**Практика:**

- [ ] Сгенерировать keystore.
- [ ] Настроить `signingConfigs` без хранения паролей в git.
- [ ] Включить `minifyEnabled true`.
- [ ] Добавить keep-правила для Gson/Room/POI.
- [ ] Собрать `assembleRelease`.
- [ ] Установить и проверить.
- [ ] Настроить CI: `lint`, `test`, `assembleDebug`.

**Критерий готовности:** подписанный APK устанавливается; CI зелёный; вы понимаете, зачем нужен R8.

**Что изучаете:** релиз, безопасность, CI, автоматизация.

---

### Модуль 11. Что дальше (постоянно)

После Java/XML Views переходите к современному стеку:

- **Kotlin** — синтаксис, null-safety, coroutines.
- **Jetpack Compose** — декларативный UI.
- **Hilt** — dependency injection.
- **Coroutines/Flow** — асинхронность.
- **Retrofit/OkHttp** — сеть.
- **Paging 3** — большие списки.
- **WorkManager** — фоновые задачи.
- **Play Console** — публикация, внутреннее тестирование.
- **Crashlytics/Sentry** — мониторинг падений.
- **MVI/модуляризация** — архитектура больших приложений.
- **Безопасность**: EncryptedSharedPreferences, HTTPS, secrets.

**Практика:** переписать один экран (например, список) на Kotlin + Compose и сравнить с Java/XML.

---

## 5. Упражнения на этом проекте: от простого к сложному

| № | Упражнение | Тема | Файлы | Уровень |
|---|---|---|---|---|
| 1 | Собрать и установить APK | Gradle, adb | `app/build.gradle` | 1 |
| 2 | Объяснить каждое разрешение в манифесте | Manifest, permissions | `AndroidManifest.xml` | 1 |
| 3 | Добавить новый экран и переход | Activity, Intent, layout | `MainActivity`, `res/layout` | 1 |
| 4 | Вынести строки в `strings.xml` | Ресурсы | `ScanActivity`, layouts | 1 |
| 5 | Исправить пустой список после сканирования | MVVM, Repository, singleton | `QRCodeRepository`, ViewModels | 2 |
| 6 | Добавить одиночный скан в сессию | Состояние | `ScanViewModel` | 2 |
| 7 | Убрать дубли при сканировании | Camera lifecycle | `ScanActivity` | 2 |
| 8 | Убрать двойной `resume()` | Lifecycle | `ScanActivity` | 2 |
| 9 | Удалять запись из БД | Room, threading | `ListScannedViewModel`, `QRCodeRepository` | 2 |
| 10 | Показать список JSON-инвентаризации | JSON, RecyclerView | `JsonInventoryActivity`, `InventoryViewModel` | 2 |
| 11 | Починить парсинг чисел и объекта | JSON, ошибки | `InventoryViewModel` | 2 |
| 12 | Подключить адаптер форматов | RecyclerView | `ApprovedFormatFragment` | 2 |
| 13 | Показать ручные позиции | Fragments, state | `ManualInventoryFragment` | 2 |
| 14 | Вынести экспорт в фон | Executor, ANR | `SendActivity`, `ExcelExportService` | 3 |
| 15 | Добавить шаринг файла | FileProvider, Intent | `ExcelExportService`, manifest | 3 |
| 16 | Добавить зум и смену камеры | Camera API | `ScanActivity`, `activity_scan.xml` | 3 |
| 17 | Написать тесты на сессию и JSON | JUnit | новый `src/test` | 3 |
| 18 | Собрать подписанный release | Signing, R8 | `app/build.gradle`, keystore | 3 |

**Совет:** не делайте все упражнения сразу. Один модуль — 1–3 упражнения. После каждого — коммит.

---

## 6. Плохие практики в этом проекте: что понять и не повторять

Это отдельная ценность проекта: здесь есть типичные ошибки, которые полезно увидеть «вживую».

| Антипаттерн | Почему плохо | Как правильно |
|---|---|---|
| `new QRCodeRepository()` в каждом ViewModel | Состояние не разделяется; список и отправка пустые | Singleton/Application-scoped репозиторий или `ScanSession` |
| `AsyncTask` | Устарел, сложно тестировать, утечки | `ExecutorService`, Room + `LiveData`, coroutines |
| Экспорт в UI-потоке | ANR, зависание интерфейса | Фоновый executor + результат в UI |
| `Toast` внутри сервиса | Слой данных зависит от UI | Возвращать результат, показывать в Activity/Fragment |
| Жёстко заданные строки | Нет локализации, сложно менять | `strings.xml` |
| Лишние разрешения | Приватность, недоверие пользователя | Только необходимые |
| `allowBackup=true` для БД с кодами | Данные уходят в бэкап | `false` или правила исключения |
| Нет тестов | Регрессии не ловятся | JUnit + Espresso |
| `app/build` в git, нет wrapper | Репозиторий раздувается, сборка не воспроизводится | `.gitignore`, Gradle Wrapper |
| Мёртвый код (`QRCodeScanner`) | Путает, создаёт иллюзию функциональности | Удалить или использовать |
| `autoSizeColumn()` в POI на Android | `java.awt` отсутствует | Фиксированные ширины, `SXSSFWorkbook` |
| `id` всегда 0 | Некорректные выгрузки | Возвращать `id` из `insert` |

**Задание:** выберите 3 антипаттерна, объясните причину и исправьте. Это лучшее упражнение на «чтение кода».

---

## 7. График обучения

### Вариант «интенсив» — 8 недель по 10–12 часов

| Неделя | Модули | Результат |
|---|---|---|
| 1 | 0–1 | Проект создан, APK собирается |
| 2 | 2 | Все экраны и навигация |
| 3 | 3 | Камера, разрешения, сканирование |
| 4 | 4 | Room, сохранение данных |
| 5 | 5–6 | MVVM, исправленная сессия, список |
| 6 | 7 | JSON, файлы, экспорт |
| 7 | 8–9 | Инвентаризация, отладка, тесты |
| 8 | 10 | Release, CI, документация |

### Вариант «спокойный» — 12 недель по 6–8 часов

| Неделя | Модули |
|---|---|
| 1 | 0 |
| 2 | 1 |
| 3 | 2 |
| 4 | 3 (начало) |
| 5 | 3 (завершение) |
| 6 | 4 |
| 7 | 5 |
| 8 | 6 |
| 9 | 7 (начало) |
| 10 | 7 (завершение) |
| 11 | 8–9 |
| 12 | 10 + итоговый проект |

---

## 8. Как проверять себя

### Контрольные вопросы

- [ ] Чем `Activity` отличается от `Fragment`?
- [ ] Что происходит при повороте экрана?
- [ ] Как работает runtime permission и что делать при отказе?
- [ ] Почему нельзя обращаться к БД в UI-потоке?
- [ ] Что такое `LiveData` и чем она удобна?
- [ ] Почему в проекте список был пустым? Как исправить?
- [ ] Чем `Entity` отличается от модели?
- [ ] Как работает `RecyclerView` и зачем `ViewHolder`?
- [ ] Как безопасно отдать файл другому приложению?
- [ ] Чем внутреннее хранилище отличается от внешнего?
- [ ] Что такое ANR и как его избежать?
- [ ] Зачем нужен R8 и signing config?
- [ ] Как собрать release APK?

### Практический экзамен

Сделайте **самостоятельно**, не копируя код:

1. Добавьте экран «История» с данными из Room.
2. Добавьте поиск по значению.
3. Добавьте удаление записи.
4. Напишите unit-тест на удаление.
5. Соберите подписанный release APK.

Если получилось — вы освоили основной цикл Android-разработки.

### Портфолио

- [ ] Исправленное приложение.
- [ ] `README` с описанием и скриншотами.
- [ ] Тесты.
- [ ] Подписанный APK.
- [ ] CI-бейдж.
- [ ] Краткий отчёт: что было сломано, что исправлено, чему научились.

---

## 9. Ресурсы

**Android:**

- Основы: https://developer.android.com/guide/components/fundamentals
- Activity lifecycle: https://developer.android.com/guide/components/activities
- Permissions: https://developer.android.com/training/permissions/requesting
- Room: https://developer.android.com/training/data-storage/room
- ViewModel/LiveData: https://developer.android.com/topic/libraries/architecture/viewmodel
- RecyclerView: https://developer.android.com/guide/topics/ui/layout/recyclerview
- Хранилище: https://developer.android.com/training/data-storage
- FileProvider: https://developer.android.com/reference/androidx/core/content/FileProvider
- Тестирование: https://developer.android.com/training/testing
- Сборка из командной строки: https://developer.android.com/studio/build/building-cmdline

**Библиотеки:**

- ZXing Android Embedded: https://github.com/journeyapps/zxing-android-embedded
- Apache POI: https://poi.apache.org/
- Gson: https://github.com/google/gson
- Material Components: https://m3.material.io/

**Java и SQL:**

- Java: https://dev.java/learn/
- SQL: https://sqlbolt.com/

**Современный стек (после этого проекта):**

- Kotlin: https://kotlinlang.org/docs/home.html
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Hilt: https://developer.android.com/training/dependency-injection/hilt-android
- Coroutines: https://kotlinlang.org/docs/coroutines-overview.html

**Git и CI:**

- Git: https://git-scm.com/book/ru/v2
- GitHub Actions: https://docs.github.com/actions

---

## 10. Ограничения этого проекта как учебного

- Это **Java + XML Views**, а не современный Kotlin + Compose. Полезно для понимания legacy, но после обучения переходите на Kotlin/Compose.
- В проекте есть **антипаттерны** (см. раздел 6). Не копируйте их в свой код.
- Бизнес-логика инвентаризации и 1С **не завершена**; проект — каркас, а не эталон.
- **Apache POI** на Android — тяжёлое и рискованное решение; для обучения начните с CSV.
- **Нет тестов и CI** — вам придётся добавить их самостоятельно.
- **Формат 1С** не согласован; для учебных целей достаточно JSON.
- Проект не заменяет чтение официальной документации и практику на своих мини-проектах.

---

## 11. Итог: что вы будете уметь после курса

- Создавать Android-проект с нуля и собирать APK.
- Делать экраны, навигацию и адаптивный UI.
- Работать с разрешениями и камерой.
- Сохранять данные в Room и читать их через LiveData.
- Строить MVVM-архитектуру с Repository.
- Делать списки на RecyclerView.
- Читать и записывать файлы, парсить JSON, экспортировать в CSV/Excel.
- Делиться файлами через FileProvider.
- Отлаживать приложение и писать тесты.
- Собирать подписанный release и настраивать CI.
- Читать чужой код, находить баги и планировать исправления.
- Понимать, чем «рабочий каркас» отличается от готового продукта.

**Главный совет:** не пытайтесь пройти все 12 модулей за неделю. Один модуль — один небольшой результат. После каждого модуля делайте коммит и короткую запись в учебный журнал: «что сделал, что сломалось, почему, как исправил». Именно эти записи через месяц станут вашим портфолио и доказательством навыков.
