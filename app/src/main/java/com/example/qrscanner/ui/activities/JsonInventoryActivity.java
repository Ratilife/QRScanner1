package com.example.qrscanner.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.ui.viewmodels.InventoryViewModel;
import com.example.qrscanner.utils.DebugLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * JsonInventoryActivity — экран загрузки инвентаризации из JSON-файла.
 * Открывается из InventoryActivity кнопкой «НА ОСНОВЕ JSON»
 * (переход Activity → Activity через startActivity).
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ПЕРЕХОДЕ ИЗ InventoryActivity:
 *   InventoryActivity.onPause()          ← предыдущий экран теряет фокус
 *   JsonInventoryActivity.onCreate()     ← натягиваем разметку, получаем
 *                                          ViewModel, настраиваем Toolbar,
 *                                          находим кнопку и TextView,
 *                                          вешаем слушатели, подписываемся
 *                                          на LiveData
 *   JsonInventoryActivity.onStart()      ← экран стал виден
 *   JsonInventoryActivity.onResume()     ← экран принимает нажатия
 *   InventoryActivity.onStop()           ← предыдущий экран полностью скрыт
 *                                          (но НЕ уничтожен — лежит в памяти)
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ВОЗВРАТЕ ПО «НАЗАД» (finish() из Toolbar):
 *   JsonInventoryActivity.onPause()      ← экран теряет фокус
 *   InventoryActivity.onRestart()        ← предыдущий экран «воскресает»
 *                                          из onStop() (без onCreate!)
 *   InventoryActivity.onStart()
 *   InventoryActivity.onResume()
 *   JsonInventoryActivity.onStop()
 *   JsonInventoryActivity.onDestroy()    ← JsonInventoryActivity уничтожена
 *
 * ВАЖНО (диалог выбора файла — «короткая» ветка жизненного цикла):
 * При нажатии «ВЫБРАТЬ JSON» запускается системный диалог выбора файла
 * (startActivityForResult). Это НЕ полноэкранная активность —
 * JsonInventoryActivity уходит только в onPause() (БЕЗ onStop()).
 * После выбора файла система вызовет:
 *   JsonInventoryActivity.onActivityResult()  ← получаем Uri выбранного файла
 *   JsonInventoryActivity.onResume()          ← возвращаемся к экрану
 * Без onStop/onRestart/onStart! Это та же «короткая» ветка, что и
 * диалог разрешения камеры в ScanActivity.
 *
 * ОСОБЕННОСТЬ: этот экран не использует фрагменты (в отличие от
 * InventoryActivity и SendActivity) и не держит критичных ресурсов
 * (в отличие от ScanActivity с камерой). Поэтому переопределены только
 * onCreate(), onActivityResult() — остальные методы жизненного цикла
 * достаточно базовой реализации AppCompatActivity.
 */
public class JsonInventoryActivity extends AppCompatActivity {
    // Код запроса при выборе JSON-файла — чтобы в onActivityResult
    // понять, на какой именно запрос пришёл ответ.
    private static final int REQUEST_CODE_PICK_JSON = 101;
    // Тег для логов Logcat (используется в DebugLogger).
    private static final String TAG = "JsonInventoryActivity";
    // ViewModel инвентаризации — общая с InventoryActivity (тот же класс
    // InventoryViewModel). Переживает пересоздание активности.
    private InventoryViewModel viewModel;
    // Кнопка «ВЫБРАТЬ JSON» и TextView для отображения результата загрузки.
    private Button btnSelectJson;
    private TextView tvJsonContent;

    /**
     * onCreate() — вызывается ОДИН РАЗ при создании активности
     * (в нашем сценарии — при каждом нажатии «НА ОСНОВЕ JSON»
     * в InventoryActivity, т.к. после «НАЗАД» активность уничтожается).
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

        // Шаг 1: натягиваем разметку activity_json_inventory
        // (Toolbar + кнопка «ВЫБРАТЬ JSON» + TextView).
        setContentView(R.layout.activity_json_inventory);

        // Шаг 2: получаем ViewModel. При повороте экрана ViewModelProvider
        // вернёт ТУ ЖЕ ViewModel (данные не потеряются).
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(InventoryViewModel.class);

        // Шаг 3: настраиваем Toolbar (верхнюю панель) как ActionBar —
        // со стрелкой «назад» в углу.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Стрелка «назад» → finish().
        // Далее система: onPause() → (InventoryActivity оживает
        // коротким путём onRestart → onStart → onResume) →
        // onStop() → onDestroy().
        toolbar.setNavigationOnClickListener(v -> finish());

        // Шаг 4: находим кнопку и TextView по id.
        btnSelectJson = findViewById(R.id.btnSelectJson);
        tvJsonContent = findViewById(R.id.tvJsonContent);

        // Шаг 5: вешаем слушатель нажатия на кнопку.
        // ВНИМАНИЕ: код внутри лямбды СЕЙЧАС НЕ ВЫПОЛНЯЕТСЯ —
        // он выполнится только когда пользователь тапнет по кнопке.
        btnSelectJson.setOnClickListener(v -> {
            // Запускаем СИСТЕМНЫЙ диалог выбора файла через
            // startActivityForResult (а не startActivity — нам нужен
            // результат: Uri выбранного файла).
            // ВАЖНО: диалог не закрывает экран полностью —
            // JsonInventoryActivity уходит только в onPause() (БЕЗ onStop()!).
            // После выбора файла система вызовет onActivityResult(),
            // а затем onResume().
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/json");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Выберите JSON файл"), REQUEST_CODE_PICK_JSON);
        });
        // Шаг 6: подписываемся на LiveData ViewModel (данные инвентаризации,
        // ошибки). Observer'ы срабатывают только когда активность
        // в состоянии STARTED/RESUMED (LiveData учитывает жизненный цикл).
        observeViewModel();

        // На этом onCreate() завершён: экран подготовлен, но ещё не виден.
        // Далее система вызовет onStart() → onResume().
    }

    /**
     * Подписка на LiveData ViewModel. Вызывается из onCreate().
     */
    private void observeViewModel() {
        // Наблюдаем за данными инвентаризации.
        // Сработает, когда ViewModel.loadInventoryFromJson() опубликует
        // результат (после успешного чтения файла в onActivityResult).
        viewModel.getInventoryData().observe(this, data -> {
            if (data != null && !data.isEmpty()) {
                // Данные загружены — показываем количество элементов
                // в TextView и Toast об успехе.
                tvJsonContent.setText("Загружено элементов: " + data.size());
                tvJsonContent.setVisibility(android.view.View.VISIBLE);
                Toast.makeText(this, "JSON файл успешно загружен", Toast.LENGTH_SHORT).show();
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
     * onActivityResult() — результат работы системного диалога выбора файла.
     * Вызывается системой, когда пользователь выбрал файл (или отменил выбор).
     *
     * ВАЖНО: этот метод вызывается МЕЖДУ onPause() и onResume() —
     * в «короткой» ветке жизненного цикла (без onStop/onRestart/onStart).
     * См. диаграмму в шапке класса.
     *
     * @param requestCode — код запроса (сверяем с REQUEST_CODE_PICK_JSON)
     * @param resultCode  — RESULT_OK, если файл выбран, иначе RESULT_CANCELED
     * @param data        — Intent с Uri выбранного файла
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Проверяем, что это ответ на НАШ запрос и файл действительно выбран.
        if (requestCode == REQUEST_CODE_PICK_JSON && resultCode == RESULT_OK && data != null) {
            // Получаем Uri выбранного файла.
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    // Открываем поток чтения файла через ContentResolver
                    // (работает для любого источника: файловая система,
                    // облако, Downloads и т.п.).
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        // Читаем файл построчно в StringBuilder.
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        reader.close();
                        inputStream.close();

                        // Получили содержимое JSON — отправляем его в ViewModel
                        // на разбор. Результат придёт через LiveData
                        // в observeViewModel().
                        String jsonContent = stringBuilder.toString();
                        viewModel.loadInventoryFromJson(jsonContent);
                        DebugLogger.d(TAG, "JSON file loaded successfully");
                    }
                } catch (Exception e) {
                    // Ошибка чтения — логируем и показываем Toast.
                    DebugLogger.e(TAG, "Error reading JSON file", e);
                    Toast.makeText(this, "Ошибка при чтении файла", Toast.LENGTH_SHORT).show();
                }
            }
        }
        // После возврата из onActivityResult() система вызовет onResume() —
        // JsonInventoryActivity снова станет активной и примет нажатия.
    }
    /**
     * onStart() — экран стал ВИДЕН пользователю.
     * Здесь пока нажатия ещё не обрабатываются (это делает onResume()).
     * В этой активности переопределять не нужно — хватает базовой логики.
     */
    // @Override
    // protected void onStart() { super.onStart(); ... }

    /**
     * onResume() — экран принимает нажатия (активность на переднем плане).
     * Именно с этого момента слушатель кнопки «ВЫБРАТЬ JSON» и стрелки
     * Toolbar могут сработать.
     *
     * Вызывается также после закрытия системного диалога выбора файла
     * (короткая ветка: onPause → [onActivityResult] → onResume,
     * без onStop/onRestart/onStart).
     */
    // @Override
    // protected void onResume() { super.onResume(); ... }

    /**
     * onPause() — активность теряет фокус (пользователь открыл системный
     * диалог выбора файла, ушёл в другое приложение, свернул).
     *
     * ВЫЗЫВАЕТСЯ ПЕРВЫМ при переходе, ДО onCreate() новой активности
     * (или до показа диалога). Здесь нельзя делать тяжёлых операций —
     * иначе новый экран не начнёт создаваться, и пользователь увидит лаг.
     *
     * ВАЖНО: при показе системного диалога выбора файла активность уходит
     * ТОЛЬКО в onPause() — без onStop(). Это отличает диалог от
     * полноэкранной активности.
     */
    // @Override
    // protected void onPause() { super.onPause(); ... }

    /**
     * onStop() — активность полностью скрыта (например, когда пользователь
     * свернул приложение). Вызывается ПОСЛЕ того, как новая активность
     * уже работает.
     *
     * Из этого состояния активность может вернуться через onRestart().
     * Но в нашем сценарии при возврате по «НАЗАД» JsonInventoryActivity
     * уничтожается (finish() → onDestroy()), поэтому onRestart() для неё
     * не вызывается.
     */
    // @Override
    // protected void onStop() { super.onStop(); ... }

    /**
     * onRestart() — вызывается при «воскрешении» активности из onStop()
     * (например, если бы JsonInventoryActivity оставалась в памяти).
     * В нашем сценарии не вызывается — после finish() активность
     * уничтожается.
     */
    // @Override
    // protected void onRestart() { super.onRestart(); ... }

    /**
     * onDestroy() — активность уничтожается.
     * Вызывается после finish() (стрелка «назад» в Toolbar) или когда
     * система убивает активность ради памяти.
     *
     * ВАЖНО: onDestroy() может НЕ вызваться, если система убила процесс —
     * поэтому критичные ресурсы освобождают в onPause(). У
     * JsonInventoryActivity критичных ресурсов нет: потоки InputStream
     * закрываются сразу после чтения в onActivityResult(),
     * ViewModel уничтожится вместе с ViewModelStore при окончательном
     * завершении активности.
     */
    // @Override
    // protected void onDestroy() { super.onDestroy(); ... }

}

