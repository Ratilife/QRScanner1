package com.example.qrscanner.ui.activities;  // должна быть именно такой

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.ui.fragments.ApprovedFormatFragment;
import com.example.qrscanner.ui.fragments.ManualInventoryFragment;
import com.example.qrscanner.ui.viewmodels.InventoryViewModel;


/**
 * InventoryActivity — экран инвентаризации (открывается кнопкой
 * «ИНВЕНТАРИЗАЦИЯ» из главного меню MainActivity).
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ПЕРЕХОДЕ ИЗ MainActivity (нажали «ИНВЕНТАРИЗАЦИЯ»):
 *   MainActivity.onPause()          ← меню теряет фокус (до создания InventoryActivity!)
 *   InventoryActivity.onCreate()    ← натягиваем разметку, получаем ViewModel,
 *                                     настраиваем Toolbar, находим кнопки,
 *                                     вешаем слушатели
 *   InventoryActivity.onStart()     ← экран инвентаризации стал виден
 *   InventoryActivity.onResume()    ← экран принимает нажатия
 *   MainActivity.onStop()           ← меню полностью скрыто под InventoryActivity
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ВОЗВРАТЕ ПО «НАЗАД» (finish() из Toolbar):
 *   InventoryActivity.onPause()     ← экран теряет фокус
 *   MainActivity.onRestart()        ← меню «воскресает» из onStop()
 *   MainActivity.onStart()
 *   MainActivity.onResume()
 *   InventoryActivity.onStop()
 *   InventoryActivity.onDestroy()   ← InventoryActivity уничтожена
 *
 * ВАЖНО: у MainActivity при возврате НЕ вызывается onCreate() — она
 * лежала в памяти остановленной (как и при возврате из ScanActivity
 * или SendActivity). А InventoryActivity после finish() уничтожается
 * полностью: при следующем нажатии «ИНВЕНТАРИЗАЦИЯ» она создастся
 * с нуля, с onCreate().
 *
 * ВАЖНО (по контрольному вопросу 2): если InventoryActivity отсутствует
 * в AndroidManifest.xml, то краш произойдёт на шаге 1 таблицы шага 1 —
 * сразу после срабатывания слушателя btnInventory в MainActivity
 * (startActivity выбросит ActivityNotFoundException). onCreate()
 * этой активности даже не начнёт выполняться. Пользователь увидит,
 * как меню на мгновение «замирает» (MainActivity уходит в onPause),
 * а затем приложение закрывается с диалогом «Приложение остановлено».
 *
 * ОСОБЕННОСТЬ: этот экран использует ФРАГМЕНТЫ (ManualInventoryFragment,
 * ApprovedFormatFragment) — как и SendActivity. При показе фрагмента
 * InventoryActivity НЕ уходит в onPause() — фрагмент живёт внутри
 * той же активности (в android.R.id.content).
 */

public class InventoryActivity extends AppCompatActivity {

    // ViewModel переживает пересоздание активности (например, поворот экрана).
    private InventoryViewModel viewModel;

    // Три кнопки экрана инвентаризации:
    //   btnBasedOnJson     — инвентаризация на основе JSON (новая активность)
    //   btnCreateManually  — создать вручную (фрагмент)
    //   btnApprovedFormat  — утверждённый формат (фрагмент)
    private Button btnBasedOnJson, btnCreateManually, btnApprovedFormat;

    /**
     * onCreate() — вызывается ОДИН РАЗ при создании активности
     * (в нашем сценарии — при каждом нажатии «ИНВЕНТАРИЗАЦИЯ»,
     * если активности нет в манифесте — не вызовется вообще, см. выше).
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

        // Шаг 1: натягиваем разметку activity_inventory
        // (Toolbar + три кнопки).
        setContentView(R.layout.activity_inventory);

        // Шаг 2: получаем ViewModel инвентаризации. При повороте экрана
        // ViewModelProvider вернёт ТУ ЖЕ ViewModel (данные не потеряются).
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(InventoryViewModel.class);

        // Шаг 3: настраиваем Toolbar (верхнюю панель) как ActionBar.
        // Это даёт кнопку «стрелка назад» в углу экрана.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Включаем отображение стрелки «назад» в Toolbar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Обработчик нажатия на стрелку «назад» → finish().
        // Далее система: onPause() → (MainActivity оживает) →
        // onStop() → onDestroy(). Тот же путь, что и у SendActivity.
        toolbar.setNavigationOnClickListener(v -> finish());

        // Шаг 4: находим три кнопки по id.
        btnBasedOnJson = findViewById(R.id.btnBasedOnJson);
        btnCreateManually = findViewById(R.id.btnCreateManually);
        btnApprovedFormat = findViewById(R.id.btnApprovedFormat);

        // Шаг 5: вешаем слушатели нажатия.
        // ВНИМАНИЕ: код внутри лямбд СЕЙЧАС НЕ ВЫПОЛНЯЕТСЯ —
        // он выполнится только когда пользователь тапнет по кнопке.

        // «НА ОСНОВЕ JSON» → переход к НОВОЙ АКТИВНОСТИ JsonInventoryActivity.
        // Здесь InventoryActivity уйдёт в onPause() → onStop(), но НЕ будет
        // уничтожена (останется в памяти, как MainActivity при переходе
        // в сканер). При возврате из JsonInventoryActivity она пройдёт
        // короткий путь: onRestart → onStart → onResume (без onCreate).
        btnBasedOnJson.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, JsonInventoryActivity.class);
            startActivity(intent);
        });
        // «СОЗДАТЬ ВРУЧНУЮ» → показываем ФРАГМЕНТ ManualInventoryFragment.
        // ВАЖНО: показ фрагмента НЕ уводит InventoryActivity в onPause() —
        // фрагмент живёт внутри той же активности (в android.R.id.content).
        // addToBackStack(null) позволяет вернуться назад системной кнопкой
        // «назад»: сначала закроется фрагмент, InventoryActivity останется
        // в onResume().
        btnCreateManually.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ManualInventoryFragment())
                    .addToBackStack(null)
                    .commit();
        });
        // «УТВЕРЖДЁННЫЙ ФОРМАТ» → показываем ФРАГМЕНТ ApprovedFormatFragment.
        // Та же логика: активность НЕ уходит в onPause(), фрагмент внутри неё.
        btnApprovedFormat.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ApprovedFormatFragment())
                    .addToBackStack(null)
                    .commit();
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
     * Именно с этого момента слушатели кнопок и стрелки Toolbar могут
     * сработать.
     *
     * При возврате из фрагмента (системная «назад») InventoryActivity
     * НЕ проходит через onRestart/onStart/onResume — фрагмент просто
     * убирается из контейнера, активность остаётся в onResume().
     *
     * При возврате из JsonInventoryActivity InventoryActivity пройдёт
     * короткий путь: onRestart → onStart → onResume (без onCreate),
     * т.к. она лежала в памяти остановленной (в onStop()).
     */
    // @Override
    // protected void onResume() { super.onResume(); ... }

    /**
     * onPause() — активность теряет фокус (пользователь уходит в другую
     * активность JsonInventoryActivity, свернул приложение или на экране
     * появился диалог).
     *
     * ВЫЗЫВАЕТСЯ ПЕРВЫМ при переходе, ДО onCreate() новой активности.
     * Здесь нельзя делать тяжёлых операций — иначе новый экран
     * не начнёт создаваться, и пользователь увидит лаг перехода.
     *
     * ВАЖНО: показ фрагмента (ManualInventoryFragment /
     * ApprovedFormatFragment) НЕ вызывает onPause() — фрагмент живёт
     * внутри активности.
     */
    // @Override
    // protected void onPause() { super.onPause(); ... }

    /**
     * onStop() — активность полностью скрыта (например, под
     * JsonInventoryActivity или когда пользователь свернул приложение).
     * Вызывается ПОСЛЕ того, как новая активность уже работает.
     *
     * Из этого состояния активность может вернуться через onRestart() —
     * это произойдёт, если пользователь вернётся из JsonInventoryActivity.
     * А после finish() (стрелка Toolbar) InventoryActivity идёт дальше
     * в onDestroy().
     */
    // @Override
    // protected void onStop() { super.onStop(); ... }

    /**
     * onRestart() — вызывается при «воскрешении» активности из onStop()
     * (например, возврат из JsonInventoryActivity по «НАЗАД»).
     * Только после этого пойдут onStart() → onResume().
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
     * в onPause(). У InventoryActivity критичных ресурсов нет:
     * viewModel уничтожится вместе с ViewModelStore при окончательном
     * завершении активности.
     */
    // @Override
    // protected void onDestroy() { super.onDestroy(); ... }
}

