package com.example.dogshelter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String ALL_BEERS = "Все породы";

    Button btnAdd, btnActivity, btnDate, btnShow, btnClose;
    TextView tvDate;
    Calendar dateAndTime = Calendar.getInstance();

    LinearLayout addLayout;

    Button btn_0_6m, btn_6_12m, btn_1_2g, btn_2_4g, btn_4_6g, btn_more_6g;

    EditText etName, etBreed;
    Spinner spinner;

    DBHelper dbHelper;

    Intent intent;

    String[] listForSpinner;

    int dateForSort = 0;
    String breedForSort = ALL_BEERS;

    ImageView imgDog;
    Button addImgDog;


    @Override
    protected void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

        int permissionStatus3 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionStatus2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(add);

        btnDate = (Button) findViewById(R.id.btnDate);
        btnDate.setOnClickListener(dateShow);

        addLayout = (LinearLayout) findViewById(R.id.layoutAdd);
        addLayout.setVisibility(View.GONE);
        btnShow = (Button) findViewById(R.id.btnShowLayout);
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLayout.setVisibility(View.VISIBLE);
            }
        });

        imgDog = (ImageView) findViewById(R.id.imageDog);
        addImgDog = (Button) findViewById(R.id.addImgDog);
        addImgDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        btnClose = (Button) findViewById(R.id.btnCloseAdd);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLayout.setVisibility(View.GONE);
            }
        });

        tvDate = (TextView) findViewById(R.id.textDate);
        tvDate.setText(new SimpleDateFormat("yyyy.MM.dd").format(new Date()));

        btn_0_6m = (Button) findViewById(R.id.btn_0_6_m);
        btn_6_12m = (Button) findViewById(R.id.btn_6_12_m);
        btn_1_2g = (Button) findViewById(R.id.btn_1_2_g);
        btn_2_4g = (Button) findViewById(R.id.btn_2_4_g);
        btn_4_6g = (Button) findViewById(R.id.btn_4_6_g);
        btn_more_6g = (Button) findViewById(R.id.btn_more_6_g);

        btn_0_6m.setOnClickListener(this);
        btn_6_12m.setOnClickListener(this);
        btn_1_2g.setOnClickListener(this);
        btn_2_4g.setOnClickListener(this);
        btn_4_6g.setOnClickListener(this);
        btn_more_6g.setOnClickListener(this);

        btnActivity = (Button) findViewById(R.id.btnActivity);

        etName = (EditText) findViewById(R.id.etName);
        etBreed = (EditText) findViewById(R.id.etBreed);


        dbHelper = new DBHelper(this, DBHelper.DATABASE_NAME, DBHelper.DATABASE_VERSION);

        // Получаем список для spinner
        String[] test = new String[0];
        listForSpinner = getListBreeds(dbHelper).toArray(test);

        // Находим spinner и подключаем адаптер
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listForSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(itemSelectedListener);

        // Создадим связь со вторым активити
        intent = new Intent(this, SecondActivity.class);

        btnActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Переход на новое активити
                    intent.putExtra("date", dateForSort);
                    intent.putExtra("breed", breedForSort);
                    startActivity(intent);
                }
            });
    }

    private List<String> getListBreeds(DBHelper dbh){
        List<String> result = new ArrayList<>();
        result.add(ALL_BEERS);
        SQLiteDatabase db = dbh.getReadableDatabase();

        Cursor cursor = db.query(true, DBHelper.TABLE_DOGS, new String[]{DBHelper.KEY_BREED}, null, null, DBHelper.KEY_BREED, null, null, null);

        while (cursor.moveToNext()){
            String breed = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.KEY_BREED));
            result.add(breed);
        }

        Log.d("mLog", "Результат: "+result.toString());

        return result;
    }

    private View.OnClickListener add = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String name = etName.getText().toString();
            String breed = etBreed.getText().toString();
            String date = tvDate.getText().toString();


            SQLiteDatabase database = dbHelper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();

            contentValues.put(DBHelper.KEY_NAME, name);
            contentValues.put(DBHelper.KEY_BREED, breed);
            contentValues.put(DBHelper.KEY_DOB, date);
            contentValues.put(DBHelper.KEY_LINK, imgPath);

            database.insert(DBHelper.TABLE_DOGS, null, contentValues);

            dbHelper.close();
            addLayout.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener dateShow = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setDate(view);
        }
    };

    // установка начальных даты и времени
    private void setInitialDateTime() {
        tvDate.setText(new SimpleDateFormat("yyyy.MM.dd").format(dateAndTime.getTime()));
    }

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
        }
    };

    // отображаем диалоговое окно для выбора даты
    public void setDate(View v) {
        new DatePickerDialog(MainActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            breedForSort = (String) adapterView.getItemAtPosition(i);
            Log.d("mLog", "Выбран элемент из списка: "+breedForSort);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };



    @Override
    public void onClick(View view) {
        final int cActive = 0xFF1111FF;
        final int cNoActive = 0xFFCCCCFF;

        switch (view.getId()){
            case R.id.btn_0_6_m:
                //btn_0_6m.setBackgroundColor(cNoActive);
                btn_6_12m.setBackgroundColor(cNoActive);
                btn_1_2g.setBackgroundColor(cNoActive);
                btn_2_4g.setBackgroundColor(cNoActive);
                btn_4_6g.setBackgroundColor(cNoActive);
                btn_more_6g.setBackgroundColor(cNoActive);

                if (dateForSort != 1) {
                    view.setBackgroundColor(cActive);
                    dateForSort = 1;
                }
                else {
                    view.setBackgroundColor(cNoActive);
                    dateForSort = 0;
                }

                break;

            case R.id.btn_6_12_m:
                btn_0_6m.setBackgroundColor(cNoActive);
                //btn_6_12m.setBackgroundColor(cNoActive);
                btn_1_2g.setBackgroundColor(cNoActive);
                btn_2_4g.setBackgroundColor(cNoActive);
                btn_4_6g.setBackgroundColor(cNoActive);
                btn_more_6g.setBackgroundColor(cNoActive);

                if (dateForSort != 2) {
                    view.setBackgroundColor(cActive);
                    dateForSort = 2;
                }
                else {
                    view.setBackgroundColor(cNoActive);
                    dateForSort = 0;
                }
                break;

            case R.id.btn_1_2_g:
                btn_0_6m.setBackgroundColor(cNoActive);
                btn_6_12m.setBackgroundColor(cNoActive);
               // btn_1_2g.setBackgroundColor(cNoActive);
                btn_2_4g.setBackgroundColor(cNoActive);
                btn_4_6g.setBackgroundColor(cNoActive);
                btn_more_6g.setBackgroundColor(cNoActive);

                if (dateForSort != 3) {
                    view.setBackgroundColor(cActive);
                    dateForSort = 3;
                }
                else {
                    view.setBackgroundColor(cNoActive);
                    dateForSort = 0;
                }
                break;

            case R.id.btn_2_4_g:
                btn_0_6m.setBackgroundColor(cNoActive);
                btn_6_12m.setBackgroundColor(cNoActive);
                btn_1_2g.setBackgroundColor(cNoActive);
                //btn_2_4g.setBackgroundColor(cNoActive);
                btn_4_6g.setBackgroundColor(cNoActive);
                btn_more_6g.setBackgroundColor(cNoActive);

                if (dateForSort != 4) {
                    view.setBackgroundColor(cActive);
                    dateForSort = 4;
                }
                else {
                    view.setBackgroundColor(cNoActive);
                    dateForSort = 0;
                }
                break;

            case R.id.btn_4_6_g:
                btn_0_6m.setBackgroundColor(cNoActive);
                btn_6_12m.setBackgroundColor(cNoActive);
                btn_1_2g.setBackgroundColor(cNoActive);
                btn_2_4g.setBackgroundColor(cNoActive);
               // btn_4_6g.setBackgroundColor(cNoActive);
                btn_more_6g.setBackgroundColor(cNoActive);

                if (dateForSort != 5) {
                    view.setBackgroundColor(cActive);
                    dateForSort = 5;
                }
                else {
                    view.setBackgroundColor(cNoActive);
                    dateForSort = 0;
                }
                break;

            case R.id.btn_more_6_g:
                btn_0_6m.setBackgroundColor(cNoActive);
                btn_6_12m.setBackgroundColor(cNoActive);
                btn_1_2g.setBackgroundColor(cNoActive);
                btn_2_4g.setBackgroundColor(cNoActive);
                btn_4_6g.setBackgroundColor(cNoActive);
               // btn_more_6g.setBackgroundColor(cNoActive);

                if (dateForSort != 6) {
                    view.setBackgroundColor(cActive);
                    dateForSort = 6;
                }
                else {
                    view.setBackgroundColor(cNoActive);
                    dateForSort = 0;
                }
                break;
        }
    }
    private Bitmap bitmap;
    private File destination = null;
    private InputStream inputStreamImg;
    private String imgPath = null;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;

    private void selectImage() {
        try {
            PackageManager pm = getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else
                Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inputStreamImg = null;
        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                Uri selectedImage = data.getData();
                bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

                Log.e("Activity", "Pick from Camera::>>> ");

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                destination = new File(Environment.getExternalStorageDirectory() + "/" +
                        "Android/data/com.example.dogshelter/files" , "IMG_" + timeStamp + ".jpg");
                FileOutputStream fo;
                //getString(R.string.app_name)
                Log.d("mLog", "Какая-то штука:"+Environment.getExternalStorageDirectory());
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imgPath = destination.getAbsolutePath();
                imgDog.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            Uri selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                Log.e("Activity", "Pick from Gallery::>>> ");

                imgPath = getRealPathFromURI(selectedImage);
                destination = new File(imgPath.toString());
                imgDog.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("mLog", "Путь фотки:"+imgPath);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_IMAGE && data!=null){
            Uri selectedImage = data.getData();
            // create file
            Cursor c = getContentResolver().query(selectedImage,null,null,null,null);
            c.moveToFirst();
            dogImage.setImageBitmap(ImagePickUpUtil.getBitmap(ImagePickUpUtil.getRealPathFromURI(this, selectedImage)));
            pathImage = ImagePickUpUtil.getRealPathFromURI(this, selectedImage);
        }
        else if(requestCode == MAKE_IMAGE && data!=null){
            Uri selectedImage = data.getData();
            // create file
            Cursor c = getContentResolver().query(selectedImage,null,null,null,null);
            c.moveToFirst();
            dogImage.setImageBitmap(ImagePickUpUtil.getBitmap(ImagePickUpUtil.getRealPathFromURI(this, selectedImage)));
            pathImage = ImagePickUpUtil.getRealPathFromURI(this, selectedImage);
        }
    }*/
}

