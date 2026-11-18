package com.example.qrscanner.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.ui.viewmodels.MainViewModel;

/**
 * MainActivity — стартовая активность приложения (меню).
 *
 * ЖИЗНЕННЫЙ ЦИКЛ (что происходит при запуске приложения):
 * 1. onCreate()  — система создаёт активность: натягивается разметка,
 *                  находятся кнопки, вешаются слушатели. Экран ещё НЕ виден.
 * 2. onStart()   — экран появился на дисплее, но нажатия ещё не принимает.
 * 3. onResume()  — экран принимает нажатия. MainActivity РАБОТАЕТ.
 *
 * ВАЖНО: слушатели в onCreate() только ПОВЕШЕНЫ, но не выполнены —
 * они ждут нажатия пользователя.
 */

public class MainActivity extends AppCompatActivity {
    // TODO - viewModel - Мертвый код
    // ViewModel переживает пересоздание активности (например, поворот экрана).
    private MainViewModel viewModel;
    // Ссылки на три кнопки главного меню.
    private Button btnRead, btnInventory, btnSend;

    /**
     * onCreate() — вызывается ОДИН РАЗ за жизнь активности (при создании).
     * Здесь готовим всё необходимое: разметку, ссылки на View, слушатели.
     *
     * @param savedInstanceState — сохранённое состояние при пересоздании
     *                             (например, после поворота экрана).
     *                             При первом запуске = null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // super.onCreate(savedInstanceState) — ОБЯЗАТЕЛЬНЫЙ вызов родительской
        // реализации (AppCompatActivity → FragmentActivity → ComponentActivity →
        // Activity). Своими силами мы этот метод не пишем — он часть фреймворка.
        //
        // ЧТО ДЕЛАЕТСЯ ВНУТРИ (упрощённо):
        // 1. Восстанавливает внутреннее состояние Activity после пересоздания:
        //    - если активность пересоздаётся (поворот экрана, смена темы/языка),
        //      система передаёт savedInstanceState — снимок состояния, который
        //      Activity сохранила в onSaveInstanceState();
        //    - фреймворк восстанавливает из него служебные вещи: иерархию View,
        //      состояние виджетов (текст в EditText, позиции скролла и т.п.),
        //      сохранённые через onSaveInstanceState данные.
        //    При ПЕРВОМ запуске savedInstanceState == null — восстанавливать нечего.
        // 2. Инициализирует внутренние механизмы Activity:
        //    - создаёт/восстанавливает ViewModelStore (хранилище ViewModel'ей) —
        //      именно поэтому ViewModel «переживает» пересоздание активности:
        //      при повороте система кладёт снимок состояния + ViewModelStore,
        //      а super.onCreate() их подхватывает;
        //    - готовит FragmentManager (для AppCompatActivity/FragmentActivity);
        //    - настраивает окно (Window), тему, диспетчеризацию жизненного цикла;
        //    - регистрирует активность в системе (ActivityThread, Application).
        // 3. Помечает активность как «создаваемую» и запускает цепочку
        //    колбэков жизненного цикла (LifecycleRegistry) —
        //    после этого активность перейдёт в состояние CREATED.
        //
        // ПОЧЕМУ БЕЗ НЕГО НЕЛЬЗЯ:
        // - Пропустишь super.onCreate() → получишь RuntimeException
        //   ("SuperNotCalledException: Activity ... did not call through to
        //   super.onCreate()") — фреймворк специально проверяет этот вызов.
        // - Даже если бы проверки не было, активность осталась бы
        //   «недонастроенной»: не было бы восстановления состояния,
        //   ViewModelStore, FragmentManager и т.д.
        //
        // ПОЧЕМУ ИМЕННО ПЕРВОЙ СТРОКОЙ:
        // Родительская реализация должна подготовить инфраструктуру ДО того,
        // как мы начнём ей пользоваться (setContentView, findViewById,
        // ViewModelProvider, getSupportFragmentManager и т.п.).
        // Поэтому super.onCreate() идёт в самом начале onCreate().
        super.onCreate(savedInstanceState);
        // Шаг 1: натягиваем разметку activity_main —
        // тот самый белый экран с тремя синими кнопками.
        setContentView(R.layout.activity_main);
        // Шаг 2: получаем ViewModel, привязанную к этой активности.
        // ViewModelProvider создаёт её один раз и переиспользует
        // при пересоздании активности.
        //TODO - формирование переменной viewModel это мертвый код
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(MainViewModel.class);
        // Шаг 3: находим три кнопки в разметке по их id.
        btnRead = findViewById(R.id.btnRead);               // «ПРОЧИТАТЬ»
        btnInventory = findViewById(R.id.btnInventory);     // «ИНВЕНТАРИЗАЦИЯ»
        btnSend = findViewById(R.id.btnSend);               // «ОТПРАВИТЬ»
        // Шаг 4: вешаем слушатели нажатия.
        // ВНИМАНИЕ: код внутри лямбд СЕЙЧАС НЕ ВЫПОЛНЯЕТСЯ —
        // он выполнится только когда пользователь тапнет по кнопке.

        // «ПРОЧИТАТЬ» → переход к сканеру ScanActivity.
        // Сработает по нажатию: создастся Intent («открой ScanActivity»)
        // и вызовется startActivity(intent).
        // Далее система сама вызовет:
        //   MainActivity.onPause()  → ScanActivity.onCreate()
        //   → ScanActivity.onStart() → ScanActivity.onResume()
        //   → MainActivity.onStop()
        btnRead.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanActivity.class);
            startActivity(intent);
        });
        // «ИНВЕНТАРИЗАЦИЯ» → переход к InventoryActivity.
        btnInventory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            startActivity(intent);
        });
        // «ОТПРАВИТЬ» → переход к SendActivity.
        btnSend.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SendActivity.class);
            startActivity(intent);
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
     * Именно с этого момента слушатели кнопок могут сработать.
     * При возврате из ScanActivity (кнопка «НАЗАД» / finish())
     * MainActivity пройдёт короткий путь: onRestart() → onStart() → onResume().
     * onCreate() при этом НЕ вызывается — активность не создаётся заново,
     * она лежала в памяти остановленной (в onStop()).
     */
    // @Override
    // protected void onResume() { super.onResume(); ... }

    /**
     * onPause() — активность теряет фокус (пользователь уходит на другой экран).
     * Вызывается ПЕРВЫМ при переходе, ДО onCreate() новой активности.
     * Здесь нельзя делать тяжёлых операций — иначе новый экран
     * не начнёт создаваться, и пользователь увидит лаг перехода.
     */
    // @Override
    // protected void onPause() { super.onPause(); ... }

    /**
     * onStop() — активность полностью скрыта (например, под ScanActivity).
     * Вызывается ПОСЛЕ того, как новая активность уже работает и рисуется
     * (иначе между экранами была бы чёрная вспышка).
     * Из этого состояния активность может вернуться через onRestart().
     */
    // @Override
    // protected void onStop() { super.onStop(); ... }

    /**
     * onRestart() — вызывается при «воскрешении» активности из onStop()
     * (например, возврат из ScanActivity по «НАЗАД»).
     * Только после этого пойдут onStart() → onResume().
     */
    // @Override
    // protected void onRestart() { super.onRestart(); ... }

    /**
     * onDestroy() — активность уничтожается.
     * Может НЕ вызваться, если система убила процесс ради памяти.
     * Для MainActivity при обычном переходе в сканер и обратно
     * onDestroy() не вызывается — меню остаётся в памяти остановленным.
     */
    // @Override
    // protected void onDestroy() { super.onDestroy(); ... }


}

