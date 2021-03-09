package com.example.dogshelter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    // Поля для БД
    DBHelper dbHelper;
    SQLiteDatabase db;

    int dateForSort = 0;
    String breedForSort = MainActivity.ALL_BEERS;

    String sinceDate = "2000.01.01";
    String untilDate = "2010.01.01";

    Calendar currentDate = Calendar.getInstance();

    // Поля для списка
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle arguments = getIntent().getExtras();
        breedForSort = arguments.get("breed").toString();
        dateForSort = arguments.getInt("date");

        switch (dateForSort){
            case 1:
                Calendar since_1 = (Calendar) currentDate.clone();
                Calendar until_1 = (Calendar) currentDate.clone();
                since_1.add(Calendar.MONTH, -6);
                until_1.add(Calendar.MONTH, 0);
                sinceDate = new SimpleDateFormat("yyyy.MM.dd").format(since_1.getTime());
                untilDate = new SimpleDateFormat("yyyy.MM.dd").format(until_1.getTime());
                break;

            case 2:
                Calendar since_2 = (Calendar) currentDate.clone();
                Calendar until_2 = (Calendar) currentDate.clone();
                since_2.add(Calendar.MONTH, -12);
                until_2.add(Calendar.MONTH, -6);
                sinceDate = new SimpleDateFormat("yyyy.MM.dd").format(since_2.getTime());
                untilDate = new SimpleDateFormat("yyyy.MM.dd").format(until_2.getTime());
                break;

            case 3:
                Calendar since_3 = (Calendar) currentDate.clone();
                Calendar until_3 = (Calendar) currentDate.clone();
                since_3.add(Calendar.YEAR, -2);
                until_3.add(Calendar.YEAR, -1);
                sinceDate = new SimpleDateFormat("yyyy.MM.dd").format(since_3.getTime());
                untilDate = new SimpleDateFormat("yyyy.MM.dd").format(until_3.getTime());
                break;

            case 4:
                Calendar since_4 = (Calendar) currentDate.clone();
                Calendar until_4 = (Calendar) currentDate.clone();
                since_4.add(Calendar.YEAR, -4);
                until_4.add(Calendar.YEAR, -2);
                sinceDate = new SimpleDateFormat("yyyy.MM.dd").format(since_4.getTime());
                untilDate = new SimpleDateFormat("yyyy.MM.dd").format(until_4.getTime());
                break;

            case 5:
                Calendar since_5 = (Calendar) currentDate.clone();
                Calendar until_5 = (Calendar) currentDate.clone();
                since_5.add(Calendar.YEAR, -6);
                until_5.add(Calendar.YEAR, -4);
                sinceDate = new SimpleDateFormat("yyyy.MM.dd").format(since_5.getTime());
                untilDate = new SimpleDateFormat("yyyy.MM.dd").format(until_5.getTime());
                break;

            case 6:
                Calendar since_6 = (Calendar) currentDate.clone();
                Calendar until_6 = (Calendar) currentDate.clone();
                since_6.add(Calendar.YEAR, -2020);
                until_6.add(Calendar.YEAR, -6);
                sinceDate = new SimpleDateFormat("yyyy.MM.dd").format(since_6.getTime());
                untilDate = new SimpleDateFormat("yyyy.MM.dd").format(until_6.getTime());
                break;
        }

        String selection = null;
        if(!(breedForSort.equals(MainActivity.ALL_BEERS)) && dateForSort==0 ) {
            selection = DBHelper.KEY_BREED + " = \"" + breedForSort+"\"";
        }
        else if((breedForSort.equals(MainActivity.ALL_BEERS)) && dateForSort!=0 ){
            selection = DBHelper.KEY_DOB + " >= \"" + sinceDate +"\" AND "+DBHelper.KEY_DOB+" <= \""+untilDate+"\"";
        }
        else if(!(breedForSort.equals(MainActivity.ALL_BEERS)) && dateForSort!=0 ){
            selection = DBHelper.KEY_BREED + " = \"" + breedForSort + "\" AND " + DBHelper.KEY_DOB + " >= \"" + sinceDate +"\" AND "+DBHelper.KEY_DOB+" <= \""+untilDate+"\"";
        }

        Log.d("mLog", "Получили породу:"+breedForSort+", и дата под номером:"+dateForSort);

        // Создаем список
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Адаптер создадим после открытия БД

        // Открываем БД
        Log.d("MyTag", "Сейчас попробую открыть БД");
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();

        Log.d("MyTag", "У меня получилось");

        // Чтение из БД
        String[] projection = {
                DBHelper.KEY_ID,
                DBHelper.KEY_NAME,
                DBHelper.KEY_BREED,
                DBHelper.KEY_DOB
        };

//        String sortOrder =
//                FeedEntry.COLUMN_NAME_FIO + " ASC";

        Log.d("MyTag", "Хочу получить курсор");

        Cursor cursor = db.query(
                DBHelper.TABLE_DOGS,
                projection,
                selection,
                null,
                null,
                null,
                null // sortOrder
        );

        List itemIDs = new ArrayList<>();   // ID
        List itemNames = new ArrayList<>();  // full name student
        List itemBreeds = new ArrayList<>();  // full name student
        List itemDates = new ArrayList<>(); // date added

        Log.d("MyTag", "Пробую читать");

        while (cursor.moveToNext()) {
            // Получения ID
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(BaseColumns._ID));
            // Получение имени собаки
            String itemName = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.KEY_NAME));
            // Получение породы собаки
            String itemBreed = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.KEY_BREED));
            // Получение времени добавления
            String itemDate = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.KEY_DOB));

            itemIDs.add(itemId);
            itemNames.add(itemName);
            itemBreeds.add(itemBreed);
            itemDates.add(itemDate);

            Log.d("MyTag", "Id:" + itemId + "; FIO:" + itemName + "; Time:" + itemDate);
        }

        cursor.close();

        mAdapter = new MyAdapter(itemIDs, itemNames, itemBreeds, itemDates, this);
        recyclerView.setAdapter(mAdapter);
    }


}
