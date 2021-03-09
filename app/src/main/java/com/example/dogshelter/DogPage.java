package com.example.dogshelter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DogPage extends AppCompatActivity {

    ImageView dogImage;
    Button btnCamera;

    TextView vId;
    TextView vName;
    TextView vBreed;
    TextView vDOB;

    Button btnEdit;
    Button btnDelete;
    Button btnChange;
    Button btnClose;

    Button btnDateEdit;
    TextView textDateEdit;
    Calendar dateAndTime = Calendar.getInstance();

    EditText eName;
    EditText eBreed;

    LinearLayout layoutEdit;

    String pathImage = null;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_page);

        Bundle arguments = getIntent().getExtras();
        String strId = arguments.get("id_dog").toString();
        Log.d("mLog", "id собаки = "+strId);

        dogImage = (ImageView) findViewById(R.id.pImage);

        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        btnDateEdit = (Button) findViewById(R.id.btnDateEdit);
        btnDateEdit.setOnClickListener(dateShow);

        textDateEdit = (TextView) findViewById(R.id.textDateEdit);
        textDateEdit.setText(new SimpleDateFormat("yyyy.MM.dd").format(new Date()));

        vId = (TextView) findViewById(R.id.pId);
        vName = (TextView) findViewById(R.id.pName);
        vBreed = (TextView) findViewById(R.id.pBreed);
        vDOB = (TextView) findViewById(R.id.pDate);

        eName = (EditText) findViewById(R.id.editName);
        eBreed = (EditText) findViewById(R.id.editBreed);

        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnChange = (Button) findViewById(R.id.btnChange);
        btnClose = (Button) findViewById(R.id.btnClose);

        btnEdit.setOnClickListener(edit);
        btnClose.setOnClickListener(close);
        btnChange.setOnClickListener(change);
        btnDelete.setOnClickListener(delete);

        layoutEdit = (LinearLayout) findViewById(R.id.layoutEdit);

        dbHelper = new DBHelper(this, DBHelper.DATABASE_NAME, DBHelper.DATABASE_VERSION);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String selection = null;
        String[] selectionArgs = null;

        selection = DBHelper.KEY_ID+" = ?";
        selectionArgs = new String[] { strId };

        Cursor cursor = database.query(DBHelper.TABLE_DOGS, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {

            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME);
                int breedIndex = cursor.getColumnIndex(DBHelper.KEY_BREED);
                int dateIndex = cursor.getColumnIndex(DBHelper.KEY_DOB);
                int linkIndex = cursor.getColumnIndex(DBHelper.KEY_LINK);

                vId.setText(strId);
                vName.setText(cursor.getString(nameIndex));
                eName.setText(cursor.getString(nameIndex));
                vBreed.setText(cursor.getString(breedIndex));
                eBreed.setText(cursor.getString(breedIndex));
                vDOB.setText(cursor.getString(dateIndex));
                textDateEdit.setText(cursor.getString(dateIndex));
                pathImage = cursor.getString(linkIndex);
            }
            cursor.close();
        } else
            Log.d("mLog", "Cursor is null");
        if(pathImage != null){
            dogImage.setImageBitmap(ImagePickUpUtil.getBitmap(pathImage));
        }
        else{
            dogImage.setImageResource(R.drawable.dog_default);
        }
        Log.d("mLog", "Путь к картинки во время входа:"+pathImage);
        dbHelper.close();
    }

    View.OnClickListener edit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            layoutEdit.setVisibility(View.VISIBLE);
        }
    };

    View.OnClickListener close = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            layoutEdit.setVisibility(View.GONE);
        }
    };

    View.OnClickListener change = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String id = vId.getText().toString();
            String name = eName.getText().toString();
            String breed = eBreed.getText().toString();
            String date = textDateEdit.getText().toString();

            vName.setText(name);
            vBreed.setText(breed);
            vDOB.setText(date);


            Log.d("mLog", "Id="+id+", Name="+name+", Breed="+breed+", Date="+date+". Фото:"+pathImage);

            ContentValues cv = new ContentValues();
            cv.put(DBHelper.KEY_NAME, name);
            cv.put(DBHelper.KEY_BREED, breed);
            cv.put(DBHelper.KEY_DOB, date);
            cv.put(DBHelper.KEY_LINK, pathImage);
            // обновляем по id
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int updCount = db.update(DBHelper.TABLE_DOGS, cv, DBHelper.KEY_ID+" = ?",
                    new String[] { id });

            Log.d("mLog", "Вот это вернула функция обновления: "+updCount);
            layoutEdit.setVisibility(View.GONE);
        }
    };

    View.OnClickListener delete = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String id = vId.getText().toString();

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int updCount = db.delete(DBHelper.TABLE_DOGS, DBHelper.KEY_ID+" = ?",
                    new String[] { id });

            finish();
            Log.d("mLog", "Здесь будет удаление");
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
        textDateEdit.setText(new SimpleDateFormat("yyyy.MM.dd").format(dateAndTime.getTime()));
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
        new DatePickerDialog(DogPage.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
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
                dogImage.setImageBitmap(bitmap);

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
                dogImage.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pathImage = imgPath;
        Log.d("mLog", "Путь фотки:"+imgPath);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
