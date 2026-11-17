package com.example.qrscanner.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.ui.viewmodels.ScanViewModel;
import com.example.qrscanner.utils.CameraHelper;
import com.example.qrscanner.utils.DebugLogger;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
// TODO -  в ScanActivity ошибочные сценарии не логируются вообще.  Отказ в разрешении камеры (строка 160) и недоступный фонарик (строка 71) показывают только Toast — пользователь увидел и забыл, а в Logcat пусто

/**
 * ScanActivity — экран сканера QR-кодов (второй экран приложения).
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ПЕРЕХОДЕ ИЗ MainActivity (нажали «ПРОЧИТАТЬ»):
 *   MainActivity.onPause()      ← меню теряет фокус (до создания сканера!)
 *   ScanActivity.onCreate()     ← натягиваем разметку, находим кнопки,
 *                                 инициализируем barcodeView, запускаем
 *                                 decodeContinuous, проверяем разрешение камеры
 *   ScanActivity.onStart()      ← экран сканера стал виден
 *   ScanActivity.onResume()     ← разрешение есть → barcodeView.resume()
 *                                 → камера включилась, красная линия бегает
 *   MainActivity.onStop()       ← меню полностью скрыто под сканером
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ВОЗВРАТЕ ПО «НАЗАД» (finish()):
 *   ScanActivity.onPause()      ← barcodeView.pause() → камера отпущена
 *   MainActivity.onRestart()    ← меню «воскресает» из onStop()
 *   MainActivity.onStart()
 *   MainActivity.onResume()
 *   ScanActivity.onStop()
 *   ScanActivity.onDestroy()    ← cameraHelper.release() → сканер уничтожен
 *
 * ВАЖНО: у MainActivity при возврате НЕ вызывается onCreate() — она
 * лежала в памяти остановленной. А ScanActivity после finish() уничтожена
 * полностью: при следующем нажатии «ПРОЧИТАТЬ» она создастся с нуля.
 */

public class ScanActivity extends AppCompatActivity {
    // Код запроса разрешения камеры — чтобы в onRequestPermissionsResult
    // понять, на какой именно запрос пришёл ответ.
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    // Тег для логов Logcat (используется в DebugLogger.d).
    private static final String TAG = "ScanActivity";
    // ViewModel переживает пересоздание активности (например, поворот экрана).
    private ScanViewModel viewModel;
    // Видоискатель камеры из библиотеки journeyapps (ZXing).
    private DecoratedBarcodeView barcodeView;
    // Кнопки экрана сканера.
    private Button btnBack, btnListCodes, btnFlashlight, btnCameraSwitch;
    private Button btnNormalMode, btnBatchMode;
    // Хелпер для работы с фонариком камеры.
    private CameraHelper cameraHelper;
    // Текущий режим: false — одиночное сканирование,
    // true — пакетный режим (сканируем много кодов подряд).
    private boolean isBatchMode = false;

    /**
     * onCreate() — вызывается ОДИН РАЗ при создании активности
     * (в нашем сценарии — при каждом нажатии «ПРОЧИТАТЬ», т.к. после
     * «НАЗАД» активность уничтожается и создаётся заново).
     *
     * @param savedInstanceState — снимок состояния при пересоздании
     *                             (например, после поворота экрана);
     *                             при первом запуске = null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Обязательный вызов родителя: восстанавливает состояние,
        // ViewModelStore, FragmentManager, настраивает окно/тему.
        super.onCreate(savedInstanceState);
        // Шаг 1: натягиваем разметку activity_scan (видоискатель + 6 кнопок).
        setContentView(R.layout.activity_scan);
        // Шаг 2: получаем ViewModel сканера. При повороте экрана
        // ViewModelProvider вернёт ТУ ЖЕ ViewModel (данные не потеряются).
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(ScanViewModel.class);
        // Шаг 3: создаём CameraHelper для управления фонариком.
        cameraHelper = new CameraHelper(this);
        // Шаг 4: находим View, вешаем слушатели, запускаем decodeContinuous.
        // ВАЖНО: слушатели только ПОВЕШЕНЫ — код внутри лямбд
        // выполнится по нажатию, а не сейчас.
        initViews();
        // Шаг 5: подписываемся на LiveData ViewModel (отсканированный код,
        // ошибки). Observer сработает, когда ViewModel опубликует данные.
        setupObservers();
        // Шаг 6: проверяем разрешение камеры.
        //   - разрешение есть → startScanning() → barcodeView.resume()
        //   - разрешения нет → показываем системный диалог
        //     «Разрешить доступ к камере?».
        // ВАЖНО: диалог разрешения НЕ закрывает экран полностью —
        // активность уходит только в onPause() (без onStop()).
        // После ответа «Разрешить» сразу вызовется onResume().
        checkCameraPermission();
        // На этом onCreate() завершён: экран подготовлен, но ещё не виден.
        // Далее система вызовет onStart() → onResume().

    }
    /**
     * Находит все View в разметке и вешает слушатели.
     * Вызывается из onCreate().
     */
    private void initViews() {
        // Находим видоискатель и все 6 кнопок по id.
        barcodeView = findViewById(R.id.barcodeView);
        btnBack = findViewById(R.id.btnBack);
        btnListCodes = findViewById(R.id.btnListCodes);
        btnFlashlight = findViewById(R.id.btnFlashlight);
        btnCameraSwitch = findViewById(R.id.btnCameraSwitch);
        btnNormalMode = findViewById(R.id.btnNormalMode);
        btnBatchMode = findViewById(R.id.btnBatchMode);

        // «НАЗАД» → finish() — команда «уничтожить эту активность».
        // Далее система: onPause() → (MainActivity оживает) →
        // onStop() → onDestroy(). См. шаг 2 сценария.
        btnBack.setOnClickListener(v -> finish());

        // «СПИСОК КОДОВ» → переход к ListScannedActivity.
        // ScanActivity уйдёт в onPause() → onStop(), но НЕ будет уничтожена
        // (останется в памяти, как MainActivity при переходе в сканер).
        btnListCodes.setOnClickListener(v -> {
            Intent intent = new Intent(ScanActivity.this, ListScannedActivity.class);
            startActivity(intent);  // Отправили
        });

        // «ФОНАРИК» → включаем/выключаем вспышку.
        btnFlashlight.setOnClickListener(v -> {
            if (cameraHelper.hasFlash(this)) {
                cameraHelper.toggleFlash(this);
                btnFlashlight.setText(cameraHelper.isFlashOn() ? "Выкл. фонарик" : "Вкл. фонарик");
            } else {
                // TODO: здесь только Toast — в Logcat ничего не пишется.
                //  Ошибочный сценарий «фонарик недоступен» не логируется.
                Toast.makeText(this, "Фонарик недоступен", Toast.LENGTH_SHORT).show();
            }
        });
        // «ПЕРЕКЛЮЧЕНИЕ КАМЕРЫ» → пока заглушка.
        btnCameraSwitch.setOnClickListener(v -> {
            // Switch camera functionality would be implemented here
            Toast.makeText(this, "Переключение камеры", Toast.LENGTH_SHORT).show();
        });
        // «ОБЫЧНЫЙ РЕЖИМ» → одиночное сканирование (после первого кода
        // переходим в список).
        btnNormalMode.setOnClickListener(v -> {
            isBatchMode = false;
            viewModel.setBatchMode(false);
            updateModeButtons();
        });
        // «ПАКЕТНЫЙ РЕЖИМ» → сканируем много кодов подряд, показываем Toast
        // и продолжаем сканировать.
        btnBatchMode.setOnClickListener(v -> {
            isBatchMode = true;
            viewModel.setBatchMode(true);
            updateModeButtons();
        });
        // Запускаем НЕПРЕРЫВНОЕ декодирование: камера постоянно смотрит
        // в кадр и при обнаружении QR-кода вызывает barcodeResult().
        // ВАЖНО: этот вызов лишь ПОДПИСЫВАЕТСЯ на результаты —
        // фактически камера включится в onResume() через barcodeView.resume().
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Пришёл распознанный QR-код.
                if (result.getText() != null) {
                    handleScanResult(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                // Не используем: точки-подсказки, где может быть код.
            }
        });
    }
    /**
     * Подсвечивает активную кнопку режима (альфа 1.0) и приглушает вторую
     * (альфа 0.5).
     */
    private void updateModeButtons() {
        if (isBatchMode) {
            btnBatchMode.setAlpha(1.0f);
            btnNormalMode.setAlpha(0.5f);
        } else {
            btnNormalMode.setAlpha(1.0f);
            btnBatchMode.setAlpha(0.5f);
        }
    }
    /**
     * Подписка на LiveData ViewModel. Вызывается из onCreate().
     * Observer'ы срабатывают только когда активность в состоянии
     * STARTED/RESUMED (LiveData учитывает жизненный цикл).
     */
    private void setupObservers() {
        // Наблюдаем за отсканированным кодом.
        viewModel.getScannedCode().observe(this, qrCode -> {
            if (qrCode != null) {
                if (!isBatchMode) {
                    // Одиночный режим — переходим в список кодов.
                    // ScanActivity уйдёт в onPause() → onStop().
                    Intent intent = new Intent(ScanActivity.this, ListScannedActivity.class);
                    startActivity(intent);
                } else {
                    // Пакетный режим — показываем Toast и продолжаем сканировать.
                    Toast.makeText(this, "QR-код добавлен: " + qrCode.getValue(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Наблюдаем за ошибками ViewModel.
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Обработка одного распознанного QR-кода.
     * Вызывается из barcodeResult() — т.е. в момент, когда камера
     * увидела код (активность в onResume()).
     */
    private void handleScanResult(String qrValue) {
        DebugLogger.d(TAG, "QR Code scanned: " + qrValue);
        viewModel.onQRCodeScanned(qrValue);
    }
    /**
     * Проверка разрешения камеры. Вызывается из onCreate().
     */
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Разрешения нет — показываем СИСТЕМНЫЙ диалог.
            // ВАЖНО: диалог не закрывает экран полностью:
            //   ScanActivity → onPause() (БЕЗ onStop()!)
            // После ответа пользователя система вызовет
            // onRequestPermissionsResult(), а затем onResume().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            // Разрешение уже есть — сразу запускаем сканирование.
            startScanning();
        }
    }
    /**
     * Результат системного диалога разрешения камеры.
     * Вызывается системой, когда пользователь ответил на диалог.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Пользователь разрешил — запускаем камеру.
                // Далее система вызовет onResume() → barcodeView.resume().
                startScanning();
            } else {
                // Пользователь запретил — показываем Toast и закрываем экран.
                // TODO: здесь только Toast — в Logcat ничего не пишется.
                //  Ошибочный сценарий «отказ в разрешении» не логируется.
                Toast.makeText(this, "Разрешение на камеру необходимо для сканирования", Toast.LENGTH_LONG).show();
                finish();  // → onPause() → onStop() → onDestroy()
            }
        }
    }
    /**
     * Запускает сканирование: barcodeView.resume() включает камеру.
     * Вызывается либо из checkCameraPermission() (если разрешение уже есть
     * при создании), либо из onRequestPermissionsResult() (после разрешения).
     *
     * ВНИМАНИЕ: здесь возможен двойной вызов resume() —
     * и отсюда, и из onResume(). Это избыточно, но библиотека обычно
     * справляется с повторным resume().
     */
    private void startScanning() {
        barcodeView.resume();
    }
    /**
     * onResume() — активность на переднем плане, принимает нажатия.
     * Здесь включаем камеру (ресурс), если разрешение есть.
     *
     * ПОЧЕМУ РЕСУРС БЕРЁТСЯ В onResume(), А НЕ В onCreate():
     * onCreate() может вызываться, когда экран ещё не виден (например,
     * при пересоздании). Камеру нужно включать только когда пользователь
     * реально смотрит на экран. А отдавать — в onPause(), чтобы не
     * держать камеру, когда экран уходит в фон.
     *
     * Этот метод вызывается также при возврате из диалога разрешения
     * (короткая ветка: onPause → onResume, без onStop/onRestart).
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        }
    }
    /**
     * onPause() — активность теряет фокус (пользователь уходит
     * в другой экран или свернул приложение).
     *
     * ВЫЗЫВАЕТСЯ ПЕРВЫМ при переходе, ДО onCreate() новой активности.
     * Здесь ОБЯЗАТЕЛЬНО отдаём ресурс камеры: barcodeView.pause()
     * останавливает сканирование и отпускает камеру.
     *
     * Почему в onPause(), а не в onStop(): onPause() гарантированно
     * вызывается при любом уходе с экрана, а onStop() может не вызваться,
     * если активность частично видна (например, под диалогом).
     */
    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
    /**
     * onDestroy() — активность уничтожается.
     * Вызывается после finish() (кнопка «НАЗАД» или отказ в разрешении)
     * или когда система убивает активность ради памяти.
     *
     * ВАЖНО: onDestroy() может НЕ вызваться, если система убила процесс —
     * поэтому освобождать критические ресурсы нужно в onPause().
     * Здесь освобождаем CameraHelper (фонарик).
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraHelper != null) {
            cameraHelper.release();
        }
    }
}

