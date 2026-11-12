## Диаграмма зависимостей классов проекта `QRScanner`

Ниже представлена визуальная схема того, **какой класс из какого модуля вызывает какой класс в каком модуле и через какие основные методы**.  
Диаграмма отражает только **классы проекта**, без Android/Jetpack/Room-инфраструктуры и внешних библиотек.

> **Точка входа приложения:**  
> - На уровне Android в `AndroidManifest.xml` точкой запуска является `MainActivity` (имеет интент‑фильтр `MAIN/LAUNCHER`).  
> - На уровне инициализации модуля — класс `QRScannerApplication`, который создаётся системой раньше всех Activity и поднимает базу данных.

```mermaid
flowchart TD

    %% =======================
    %% ПАКЕТЫ / МОДУЛИ
    %% =======================

    subgraph app["com.example.qrscanner"]
        QRScannerApplication
    end

    subgraph models["com.example.qrscanner.models"]
        QRCode
    end

    subgraph local["com.example.qrscanner.data.local"]
        QRCodeDatabase
        QRCodeDao
        DateConverter
    end

    subgraph entities["com.example.qrscanner.data.local.entities"]
        QRCodeEntity
    end

    subgraph repo["com.example.qrscanner.data.repositories"]
        QRCodeRepository
    end

    subgraph remote["com.example.qrscanner.data.remote"]
        ExcelExportService
        OneCExportService
    end

    subgraph ui_activities["com.example.qrscanner.ui.activities"]
        MainActivity
        ScanActivity
        InventoryActivity
        ListScannedActivity
        SendActivity
        JsonInventoryActivity
    end

    subgraph ui_fragments["com.example.qrscanner.ui.fragments"]
        ExcelSendFragment
        OneCFragment
        ManualInventoryFragment
        ApprovedFormatFragment
    end

    subgraph ui_viewmodels["com.example.qrscanner.ui.viewmodels"]
        MainViewModel
        ScanViewModel
        InventoryViewModel
        ListScannedViewModel
        SendViewModel
    end

    subgraph ui_adapters["com.example.qrscanner.ui.adapters"]
        ScannedCodesAdapter
    end

    subgraph utils["com.example.qrscanner.utils"]
        QRCodeScanner
        CameraHelper
        DebugLogger
    end


    %% =======================
    %% ИНИЦИАЛИЗАЦИЯ БД
    %% =======================

    QRScannerApplication -->|getInstance(Context)| QRCodeDatabase
    QRCodeRepository -->|getInstance(Application)| QRCodeDatabase
    QRCodeDatabase -->|qrCodeDao()| QRCodeDao

    QRCodeDatabase -.@Database(entities={...}).-> QRCodeEntity
    QRCodeDao -.CRUD методы: getAllCodes(), getCodeById(), insert(), update(), delete(), deleteById(), deleteAll().-> QRCodeEntity
    QRCodeEntity -.@TypeConverters(...).-> DateConverter


    %% =======================
    %% REPOSITORY ↔ ENTITY / MODEL
    %% =======================

    QRCodeRepository -->|"insert(QRCode)"| QRCodeDao
    QRCodeRepository -->|"delete(QRCodeEntity), deleteById(long)"| QRCodeDao
    QRCodeRepository -->|"InsertQRCodeAsyncTask.doInBackground(), DeleteQRCodeAsyncTask.doInBackground(), DeleteByIdAsyncTask.doInBackground()"| QRCodeDao

    QRCodeRepository -->|"new QRCodeEntity(...)"| QRCodeEntity
    QRCodeRepository -->|"addSessionCode(QRCode), getSessionCodes()"| QRCode


    %% =======================
    %% VIEWMODEL ↔ REPOSITORY / MODEL / UTILS
    %% =======================

    MainViewModel -->|"new QRCodeRepository(Application), getAllCodes()"| QRCodeRepository
    MainViewModel -->|LiveData<List<QRCodeEntity>>| QRCodeEntity

    ScanViewModel -->|"new QRCodeRepository(Application), insert(QRCode), addSessionCode(QRCode), getRepository()"| QRCodeRepository
    ScanViewModel -->|"new QRCode(String), getScannedCode().setValue(QRCode)"| QRCode

    InventoryViewModel -->|"DebugLogger.d(...), DebugLogger.e(...)"| DebugLogger

    ListScannedViewModel -->|"new QRCodeRepository(Application), getSessionCodes(), removeSessionCode(int), clearSessionCodes()"| QRCodeRepository
    ListScannedViewModel -->|MutableLiveData<List<QRCode>>| QRCode

    SendViewModel -->|"new QRCodeRepository(Application), getSessionCodes()"| QRCodeRepository
    SendViewModel -->|List<QRCode>| QRCode


    %% =======================
    %% ACTIVITIES ↔ VIEWMODELS / FRAGMENTS / ADAPTER / SERVICES / UTILS
    %% =======================

    %% MainActivity
    MainActivity -->|"ViewModelProvider.get(MainViewModel.class)"| MainViewModel
    MainActivity -->|"new Intent(..., ScanActivity.class)"| ScanActivity
    MainActivity -->|"new Intent(..., InventoryActivity.class)"| InventoryActivity
    MainActivity -->|"new Intent(..., SendActivity.class)"| SendActivity

    %% ScanActivity
    ScanActivity -->|"ViewModelProvider.get(ScanViewModel.class)"| ScanViewModel
    ScanActivity -->|"getScannedCode().observe(...), getErrorMessage().observe(...), setBatchMode(...), onQRCodeScanned(String)"| ScanViewModel
    ScanActivity -->|"new CameraHelper(Context), hasFlash(), toggleFlash(), isFlashOn(), release()"| CameraHelper
    ScanActivity -->|"DebugLogger.d(TAG, ...)"| DebugLogger
    ScanActivity -->|"new Intent(..., ListScannedActivity.class)"| ListScannedActivity

    %% InventoryActivity
    InventoryActivity -->|"ViewModelProvider.get(InventoryViewModel.class)"| InventoryViewModel
    InventoryActivity -->|"new Intent(..., JsonInventoryActivity.class)"| JsonInventoryActivity
    InventoryActivity -->|"new ManualInventoryFragment()"| ManualInventoryFragment
    InventoryActivity -->|"new ApprovedFormatFragment()"| ApprovedFormatFragment

    %% ListScannedActivity
    ListScannedActivity -->|"ViewModelProvider.get(ListScannedViewModel.class), getSessionCodes().observe(...), loadSessionCodes()"| ListScannedViewModel
    ListScannedActivity -->|"new ScannedCodesAdapter(...)"| ScannedCodesAdapter
    ListScannedActivity -->|List<QRCode> из ViewModel| QRCode
    ListScannedActivity -->|"new Intent(..., ScanActivity.class)"| ScanActivity

    %% SendActivity
    SendActivity -->|"ViewModelProvider.get(SendViewModel.class)"| SendViewModel
    SendActivity -->|List<QRCode> из ViewModel| QRCode
    SendActivity -->|"new ExcelSendFragment()"| ExcelSendFragment
    SendActivity -->|"new OneCFragment()"| OneCFragment

    %% JsonInventoryActivity
    JsonInventoryActivity -->|"ViewModelProvider.get(InventoryViewModel.class), getInventoryData().observe(...), getErrorMessage().observe(...), loadInventoryFromJson(String)"| InventoryViewModel
    JsonInventoryActivity -->|"DebugLogger.d(...), DebugLogger.e(...)"| DebugLogger


    %% =======================
    %% FRAGMENTS ↔ VIEWMODEL / SERVICES / MODEL
    %% =======================

    ExcelSendFragment -->|"ViewModelProvider(requireActivity()).get(SendViewModel.class), getSessionCodes()"| SendViewModel
    ExcelSendFragment -->|List<QRCode>| QRCode
    ExcelSendFragment -->|"new ExcelExportService(Context), exportToExcel(List<QRCode>)"| ExcelExportService

    OneCFragment -->|"ViewModelProvider(requireActivity()).get(SendViewModel.class), getSessionCodes()"| SendViewModel
    OneCFragment -->|List<QRCode>| QRCode
    OneCFragment -->|"new OneCExportService(Context), exportTo1C(List<QRCode>)"| OneCExportService

    ManualInventoryFragment -->|"ViewModelProvider(requireActivity()).get(InventoryViewModel.class), addManualInventoryItem(...)"| InventoryViewModel

    %% ApprovedFormatFragment не вызывает проектные классы явно


    %% =======================
    %% ADAPTERS
    %% =======================

    ScannedCodesAdapter -->|List<QRCode>, setCodes(List<QRCode>), onBindViewHolder()| QRCode


    %% =======================
    %% UTILS / SERVICES ↔ MODEL / LOGGER
    %% =======================

    QRCodeScanner -->|"DebugLogger.d(...), DebugLogger.e(...)"| DebugLogger
    CameraHelper -->|"DebugLogger.d(...), DebugLogger.e(...)"| DebugLogger

    ExcelExportService -->|exportToExcel(List<QRCode>)| QRCode
    ExcelExportService -->|"DebugLogger.i(...), DebugLogger.e(...)"| DebugLogger

    OneCExportService -->|exportTo1C(List<QRCode>)| QRCode
    OneCExportService -->|"DebugLogger.i(...), DebugLogger.e(...)"| DebugLogger
```

### Как читать диаграмму

- **Узел** — это конкретный **класс** в своём **пакете/модуле** (подписан в прямоугольнике `subgraph`).
- **Стрелка `A -->|method(...)| B`** означает:  
  **класс `A` вызывает класс `B` через указанные методы или использует его типы** (например, `List<QRCode>`).
- **Пунктирные стрелки** (`-. ... .->`) показывают **аннотационные / метаданные‑зависимости** (Room: `@Database`, `@TypeConverters`) — это не прямой вызов метода, но важная связь по данным.
- Диаграмма читается **сверху вниз**:
  - вверху лежит слой **инициализации и данных** (`QRScannerApplication`, `QRCodeDatabase`, `QRCodeRepository`);
  - ниже — **ViewModel‑слой**;
  - ещё ниже — **Activity/Fragment‑слой**, которые уже взаимодействуют с пользователем и дергают ViewModel’и и сервисы.

#### Где искать точку старта (поток запуска)

1. **Запуск приложения (Android):**
   - Система Android создаёт экземпляр `QRScannerApplication` и вызывает его, поэтому на диаграмме с него начинаются стрелки к `QRCodeDatabase`.
2. **Стартовый экран:**
   - В `AndroidManifest.xml` стартовой (`LAUNCHER`) объявлена `MainActivity` — именно она открывается первой для пользователя.
   - На диаграмме `MainActivity` находится в блоке `com.example.qrscanner.ui.activities` и:
     - получает `MainViewModel` (`MainActivity --> MainViewModel`),
     - по кнопкам навигации создаёт `Intent` на `ScanActivity`, `InventoryActivity`, `SendActivity`.
3. **Дальнейший поток:**
   - `ScanActivity` через `ScanViewModel` и `QRCodeRepository` идёт к БД и модели `QRCode`.
   - `InventoryActivity` ведёт к фрагментам инвентаризации и `InventoryViewModel`.
   - `SendActivity` ведёт к фрагментам `ExcelSendFragment` / `OneCFragment`, которые используют экспортные сервисы.

Таким образом, **если вы хотите “проследить запуск приложения”**:
1. Начните с узлов **`QRScannerApplication`** и **`MainActivity`**.  
2. От `MainActivity` по стрелкам смотрите, в какие Activity вы переходите (сканирование, инвентаризация, отправка).  
3. От каждой Activity следите за стрелками вниз к её ViewModel, затем к репозиторию и БД — так вы увидите полный путь данных от экрана до хранения/экспорта.

Эту диаграмму можно открыть в любом просмотрщике Markdown, поддерживающем Mermaid (например, в GitHub, VS Code/Cursor с расширением Mermaid), чтобы интерактивно изучать архитектуру и зависимости проекта.

