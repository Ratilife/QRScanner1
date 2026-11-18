package com.example.qrscanner.ui.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.data.remote.ExcelExportService;
import com.example.qrscanner.data.remote.OneCExportService;
import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.ui.fragments.ExcelSendFragment;
import com.example.qrscanner.ui.fragments.OneCFragment;
import com.example.qrscanner.ui.viewmodels.SendViewModel;

import java.util.List;

/**
 * SendActivity — экран отправки/экспорта отсканированных кодов
 * (третий экран приложения, открывается кнопкой «ОТПРАВИТЬ» из меню).
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ПЕРЕХОДЕ ИЗ MainActivity (нажали «ОТПРАВИТЬ»):
 *   MainActivity.onPause()      ← меню теряет фокус (до создания SendActivity!)
 *   SendActivity.onCreate()     ← натягиваем разметку, находим кнопки,
 *                                 инициализируем сервисы экспорта, настраиваем
 *                                 Toolbar, вешаем слушатели
 *   SendActivity.onStart()      ← экран отправки стал виден
 *   SendActivity.onResume()     ← экран принимает нажатия
 *   MainActivity.onStop()       ← меню полностью скрыто под SendActivity
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ВОЗВРАТЕ ПО «НАЗАД» (finish() из Toolbar):
 *   SendActivity.onPause()      ← экран теряет фокус
 *   MainActivity.onRestart()    ← меню «воскресает» из onStop()
 *   MainActivity.onStart()
 *   MainActivity.onResume()
 *   SendActivity.onStop()
 *   SendActivity.onDestroy()    ← SendActivity уничтожена
 *
 * ВАЖНО: у MainActivity при возврате НЕ вызывается onCreate() — она
 * лежала в памяти остановленной (как и при возврате из ScanActivity).
 * А SendActivity после finish() уничтожается полностью: при следующем
 * нажатии «ОТПРАВИТЬ» она создастся с нуля, с onCreate().
 *
 * ОСОБЕННОСТЬ: этот экран использует ФРАГМЕНТЫ (ExcelSendFragment,
 * OneCFragment). При показе фрагмента SendActivity НЕ уходит в onPause()
 * — фрагмент живёт внутри той же активности. Поэтому переход
 * «Activity → фрагмент» не гоняет жизненный цикл активности.
 */

public class SendActivity extends AppCompatActivity {
    // ViewModel переживает пересоздание активности (например, поворот экрана).
    private SendViewModel viewModel;
    // Две кнопки экрана: экспорт в Excel и экспорт в 1С.
    private Button btnExcel, btnOneC;
    // Сервисы экспорта — создаются в onCreate(), используются по нажатию.
    private ExcelExportService excelExportService;
    private OneCExportService oneCExportService;

    /**
     * onCreate() — вызывается ОДИН РАЗ при создании активности
     * (в нашем сценарии — при каждом нажатии «ОТПРАВИТЬ», т.к. после
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

        // Шаг 1: натягиваем разметку activity_send
        // (Toolbar + две кнопки: Excel и 1С).
        setContentView(R.layout.activity_send);

        // Шаг 2: получаем ViewModel экрана отправки. При повороте экрана
        // ViewModelProvider вернёт ТУ ЖЕ ViewModel (данные не потеряются).
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(SendViewModel.class);

        // Шаг 3: создаём сервисы экспорта. Они нужны только когда
        // пользователь нажмёт кнопку — но создаём заранее в onCreate(),
        // чтобы не создавать их каждый раз в слушателе.
        excelExportService = new ExcelExportService(this);
        oneCExportService = new OneCExportService(this);
        // Шаг 4: настраиваем Toolbar (верхнюю панель) как ActionBar.
        // Это даёт кнопку «стрелка назад» в углу экрана.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Включаем отображение стрелки «назад» в Toolbar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Обработчик нажатия на стрелку «назад» → finish().
        // Далее система: onPause() → (MainActivity оживает) →
        // onStop() → onDestroy(). Тот же путь, что и у ScanActivity
        // по кнопке «НАЗАД».
        toolbar.setNavigationOnClickListener(v -> finish());

        // Шаг 5: находим две кнопки по id.
        btnExcel = findViewById(R.id.btnExcel);
        btnOneC = findViewById(R.id.btnOneC);

        // Шаг 6: вешаем слушатели нажатия.
        // ВНИМАНИЕ: код внутри лямбд СЕЙЧАС НЕ ВЫПОЛНЯЕТСЯ —
        // он выполнится только когда пользователь тапнет по кнопке.

        // «ЭКСПОРТ В EXCEL»:
        //   - если в сессии уже есть отсканированные коды → сразу экспортируем;
        //   - если кодов нет → показываем ФРАГМЕНТ с дополнительными опциями.
        // ВАЖНО: показ фрагмента НЕ уводит SendActivity в onPause() —
        // фрагмент живёт внутри той же активности (в android.R.id.content).
        btnExcel.setOnClickListener(v -> {
            List<QRCode> codes = viewModel.getSessionCodes();
            if (codes != null && !codes.isEmpty()) {
                // Есть коды — экспортируем сразу.
                excelExportService.exportToExcel(codes);
            } else {
                // Кодов нет — показываем фрагмент с опциями.
                // addToBackStack(null) позволяет вернуться назад
                // системной кнопкой «назад» (фрагмент закроется,
                // SendActivity останется).
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new ExcelSendFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        // «ЭКСПОРТ В 1С» — та же логика, но для 1С.
        // При показе фрагмента SendActivity НЕ уходит в onPause().
        btnOneC.setOnClickListener(v -> {
            List<QRCode> codes = viewModel.getSessionCodes();
            if (codes != null && !codes.isEmpty()) {
                // Есть коды — экспортируем сразу.
                oneCExportService.exportTo1C(codes);
            } else {
                // Кодов нет — показываем фрагмент с опциями.
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new OneCFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        // На этом onCreate() завершён: экран подготовлен, но ещё не виден.
        // Далее система вызовет onStart() → onResume().
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
     * Именно с этого момента слушатели кнопок Excel/1С и стрелки Toolbar
     * могут сработать.
     *
     * При возврате из фрагмента (системная «назад») SendActivity
     * НЕ проходит через onRestart/onStart/onResume — фрагмент просто
     * убирается из контейнера, активность остаётся в onResume().
     *
     * При возврате из MainActivity (после finish() этой активности)
     * SendActivity уже уничтожена — onCreate() при следующем заходе
     * вызовется заново.
     */
    // @Override
    // protected void onResume() { super.onResume(); ... }

    /**
     * onPause() — активность теряет фокус (пользователь уходит в другой
     * экран, свернул приложение или на экране появился диалог).
     *
     * ВЫЗЫВАЕТСЯ ПЕРВЫМ при переходе, ДО onCreate() новой активности.
     * Здесь нельзя делать тяжёлых операций — иначе новый экран
     * не начнёт создаваться, и пользователь увидит лаг перехода.
     *
     * ВАЖНО: показ фрагмента (ExcelSendFragment / OneCFragment)
     * НЕ вызывает onPause() — фрагмент живёт внутри активности.
     */
    // @Override
    // protected void onPause() { super.onPause(); ... }

    /**
     * onStop() — активность полностью скрыта (например, под другой
     * активностью или когда пользователь свернул приложение).
     * Вызывается ПОСЛЕ того, как новая активность уже работает.
     *
     * Из этого состояния активность может вернуться через onRestart().
     * В нашем сценарии SendActivity после finish() не возвращается —
     * она идёт дальше в onDestroy().
     */
    // @Override
    // protected void onStop() { super.onStop(); ... }

    /**
     * onRestart() — вызывается при «воскрешении» активности из onStop()
     * (например, если бы SendActivity оставалась в памяти, как MainActivity).
     *
     * В нашем сценарии при возврате по «НАЗАД» SendActivity уничтожается
     * (finish() → onDestroy()), поэтому onRestart() для неё не вызывается.
     */
    // @Override
    // protected void onRestart() { super.onRestart(); ... }

    /**
     * onDestroy() — активность уничтожается.
     * Вызывается после finish() (стрелка «назад» в Toolbar) или когда
     * система убивает активность ради памяти.
     *
     * ВАЖНО: onDestroy() может НЕ вызваться, если система убила процесс —
     * поэтому критичные ресурсы (камера, датчики и т.п.) освобождают
     * в onPause(). Здесь у SendActivity критичных ресурсов нет:
     * excelExportService и oneCExportService — обычные Java-объекты,
     * их сборщик мусора уберёт сам.
     */
    // @Override
    // protected void onDestroy() { super.onDestroy(); ... }
}

