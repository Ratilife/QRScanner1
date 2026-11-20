package com.example.qrscanner.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.R;
import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.ui.adapters.ScannedCodesAdapter;
import com.example.qrscanner.ui.viewmodels.ListScannedViewModel;

import java.util.List;

/**
 * ListScannedActivity — экран списка отсканированных кодов.
 * Открывается из ScanActivity кнопкой «СПИСОК КОДОВ».
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ПЕРЕХОДЕ ИЗ ScanActivity (нажали «СПИСОК КОДОВ»):
 *   ScanActivity.onPause()          ← камера отпущена (barcodeView.pause())
 *   ListScannedActivity.onCreate()  ← натягиваем разметку, получаем ViewModel,
 *                                     настраиваем Toolbar, RecyclerView и
 *                                     адаптер, вешаем слушатели, подписываемся
 *                                     на LiveData
 *   ListScannedActivity.onStart()   ← экран списка стал виден
 *   ListScannedActivity.onResume()  ← экран принимает нажатия +
 *                                     viewModel.loadSessionCodes() —
 *                                     загружаем актуальный список кодов
 *   ScanActivity.onStop()           ← сканер полностью скрыт
 *                                     (НО НЕ уничтожен — лежит в памяти)
 *
 * ЖИЗНЕННЫЙ ЦИКЛ ПРИ ВОЗВРАТЕ ПО «НАЗАД» (finish() из Toolbar или кнопки):
 *   ListScannedActivity.onPause()   ← экран теряет фокус
 *   ScanActivity.onRestart()        ← сканер «воскресает» из onStop()
 *   ScanActivity.onStart()
 *   ScanActivity.onResume()         ← barcodeView.resume() → камера снова
 *                                     включилась (разрешение уже есть)
 *   ListScannedActivity.onStop()
 *   ListScannedActivity.onDestroy() ← ListScannedActivity уничтожена
 *
 * ВАЖНО: у ScanActivity при возврате НЕ вызывается onCreate() — она
 * лежала в памяти остановленной (в onStop()). Возврат идёт коротким
 * путём: onRestart → onStart → onResume. А ListScannedActivity после
 * finish() уничтожается полностью.
 *
 * ОСОБЕННОСТЬ: кнопка «СКАНИРОВАТЬ СНОВА» (btnRescan) запускает НОВЫЙ
 * экземпляр ScanActivity через startActivity — это НЕ возврат к старому
 * сканеру, а создание нового (с onCreate()). Старый ScanActivity так и
 * останется лежать в памяти остановленным под стеком.
 *
 * ОСОБЕННОСТЬ: в onResume() вызывается viewModel.loadSessionCodes() —
 * список перезагружается КАЖДЫЙ раз, когда экран становится активным
 * (в т.ч. после возврата из другого экрана). Это гарантирует
 * актуальность данных (например, если коды добавились в сканере).
 */

public class ListScannedActivity extends AppCompatActivity {
    // ViewModel переживает пересоздание активности (например, поворот экрана).
    private ListScannedViewModel viewModel;

    // RecyclerView для списка кодов и TextView-заглушка «список пуст».
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    // Кнопки: «СКАНИРОВАТЬ СНОВА» и «НАЗАД К СПИСКУ» (finish()).
    private Button btnRescan, btnBackToList;

    // Адаптер для RecyclerView — связывает список QRCode с ViewHolder'ами.
    private ScannedCodesAdapter adapter;

    /**
     * onCreate() — вызывается ОДИН РАЗ при создании активности
     * (каждое нажатие «СПИСОК КОДОВ» в ScanActivity или «СКАНИРОВАТЬ
     * СНОВА» создаёт новый экземпляр).
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

        // Шаг 1: натягиваем разметку activity_list_scanned
        // (Toolbar + RecyclerView + TextView-заглушка + две кнопки).
        setContentView(R.layout.activity_list_scanned);

        // Шаг 2: получаем ViewModel списка. При повороте экрана
        // ViewModelProvider вернёт ТУ ЖЕ ViewModel (данные не потеряются).
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(ListScannedViewModel.class);

        // Шаг 3: настраиваем Toolbar как ActionBar — со стрелкой «назад».
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Стрелка «назад» → finish().
        // Далее система: onPause() → (ScanActivity оживает коротким путём
        // onRestart → onStart → onResume, камера снова включается) →
        // onStop() → onDestroy().
        toolbar.setNavigationOnClickListener(v -> finish());

        // Шаг 4: находим View по id.
        recyclerView = findViewById(R.id.recyclerViewScanned);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnRescan = findViewById(R.id.btnRescan);
        btnBackToList = findViewById(R.id.btnBackToList);

        // Шаг 5: создаём адаптер. Второй аргумент — слушатель удаления:
        // при свайпе/нажатии «удалить» на элементе вызывается
        // viewModel.deleteCode(position) — ViewModel обновит LiveData,
        // observer в observeViewModel() перерисует список.
        adapter = new ScannedCodesAdapter(this, position -> {
            viewModel.deleteCode(position);
        });

        // Шаг 6: настраиваем RecyclerView — вертикальный LinearLayoutManager
        // и наш адаптер.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Шаг 7: вешаем слушатели нажатия на кнопки.
        // ВНИМАНИЕ: код внутри лямбд СЕЙЧАС НЕ ВЫПОЛНЯЕТСЯ —
        // он выполнится только когда пользователь тапнет по кнопке.

        // «СКАНИРОВАТЬ СНОВА» → запускаем НОВЫЙ экземпляр ScanActivity.
        // ВАЖНО: это НЕ возврат к старому сканеру, а создание нового
        // (startActivity → ScanActivity.onCreate() с нуля). Старый
        // ScanActivity останется лежать в памяти остановленным под стеком.
        // Далее система: ListScannedActivity.onPause() →
        // новый ScanActivity.onCreate() → onStart() → onResume() →
        // ListScannedActivity.onStop().
        btnRescan.setOnClickListener(v -> {
            Intent intent = new Intent(ListScannedActivity.this, ScanActivity.class);
            startActivity(intent);
        });

        // «НАЗАД К СПИСКУ» → finish() — уничтожить этот экран и вернуться
        // к предыдущему (ScanActivity или MainActivity — зависит от стека).
        btnBackToList.setOnClickListener(v -> finish());

        // Шаг 8: подписываемся на LiveData ViewModel. Observer сработает,
        // когда ViewModel опубликует список кодов (после loadSessionCodes()
        // в onResume() или после deleteCode()).
        observeViewModel();

        // На этом onCreate() завершён: экран подготовлен, но ещё не виден.
        // Далее система вызовет onStart() → onResume().
    }
    /**
     * Подписка на LiveData ViewModel. Вызывается из onCreate().
     * Observer'ы срабатывают только когда активность в состоянии
     * STARTED/RESUMED (LiveData учитывает жизненный цикл).
     */
    private void observeViewModel() {
        // Наблюдаем за списком отсканированных кодов.
        viewModel.getSessionCodes().observe(this, codes -> {
            if (codes != null && !codes.isEmpty()) {
                // Список не пуст — передаём данные адаптеру,
                // показываем RecyclerView, скрываем заглушку.
                adapter.setCodes(codes);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                // Список пуст — скрываем RecyclerView, показываем заглушку
                // «Список пуст».
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
    /**
     * onResume() — активность на переднем плане, принимает нажатия.
     *
     * ЗДЕСЬ ЖЕ перезагружаем список кодов: viewModel.loadSessionCodes().
     * Это гарантирует актуальность данных КАЖДЫЙ раз, когда экран
     * становится активным:
     *   - при первом входе из ScanActivity;
     *   - при возврате из другого экрана (например, если пользователь
     *     ушёл в сканер и вернулся — новые коды появятся в списке);
     *   - после поворота экрана (активность пересоздалась — onResume
     *     вызовется снова, данные перечитаются).
     *
     * ПОЧЕМУ В onResume(), А НЕ В onCreate():
     * onCreate() вызывается только ОДИН раз при создании активности,
     * а onResume() — каждый раз, когда экран становится активным.
     * Если бы загрузка была в onCreate(), после возврата из сканера
     * список остался бы старым (без новых кодов).
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Перечитываем список кодов сессии — результат придёт через
        // LiveData в observeViewModel().
        viewModel.loadSessionCodes();
    }
    /**
     * onStart() — экран стал ВИДЕН пользователю.
     * Здесь пока нажатия ещё не обрабатываются (это делает onResume()).
     * В этой активности переопределять не нужно — хватает базовой логики.
     */
    // @Override
    // protected void onStart() { super.onStart(); ... }

    /**
     * onPause() — активность теряет фокус (пользователь ушёл в другой
     * экран, свернул приложение или на экране появился диалог).
     *
     * ВЫЗЫВАЕТСЯ ПЕРВЫМ при переходе, ДО onCreate() новой активности.
     * Здесь нельзя делать тяжёлых операций — иначе новый экран
     * не начнёт создаваться, и пользователь увидит лаг перехода.
     *
     * У ListScannedActivity критичных ресурсов нет, поэтому
     * переопределять onPause() не нужно.
     */
    // @Override
    // protected void onPause() { super.onPause(); ... }

    /**
     * onStop() — активность полностью скрыта (например, под ScanActivity
     * после нажатия «СКАНИРОВАТЬ СНОВА» или когда пользователь свернул
     * приложение). Вызывается ПОСЛЕ того, как новая активность уже работает.
     *
     * Из этого состояния активность может вернуться через onRestart().
     * Но в нашем сценарии при возврате по «НАЗАД» ListScannedActivity
     * уничтожается (finish() → onDestroy()), поэтому onRestart() для неё
     * не вызывается.
     */
    // @Override
    // protected void onStop() { super.onStop(); ... }

    /**
     * onRestart() — вызывается при «воскрешении» активности из onStop()
     * (например, если бы ListScannedActivity оставалась в памяти).
     * В нашем сценарии не вызывается — после finish() активность
     * уничтожается.
     */
    // @Override
    // protected void onRestart() { super.onRestart(); ... }

    /**
     * onDestroy() — активность уничтожается.
     * Вызывается после finish() (стрелка «назад» или кнопка «НАЗАД
     * К СПИСКУ») или когда система убивает активность ради памяти.
     *
     * ВАЖНО: onDestroy() может НЕ вызваться, если система убила процесс —
     * поэтому критичные ресурсы освобождают в onPause(). У
     * ListScannedActivity критичных ресурсов нет: адаптер и RecyclerView
     * будут собраны сборщиком мусора вместе с активностью,
     * ViewModel уничтожится вместе с ViewModelStore.
     */
    // @Override
    // protected void onDestroy() { super.onDestroy(); ... }

}

