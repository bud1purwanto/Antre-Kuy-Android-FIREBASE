package com.bud1purwanto.smartqueue.Activity.Public;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bud1purwanto.smartqueue.Activity.User.Beranda;
import com.bud1purwanto.smartqueue.Controller.RequestHandler;
import com.bud1purwanto.smartqueue.Controller.SQLiteHandler;
import com.bud1purwanto.smartqueue.Controller.SessionManager;
import com.bud1purwanto.smartqueue.Models.mUser;
import com.bud1purwanto.smartqueue.R;
import com.bud1purwanto.smartqueue.Server.Konfigurasi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = RegisterUser.class.getSimpleName();
    private int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private Uri downloadUrl, filePath, fileUri;
    private SharedPreferences permissionStatus;
    private Bitmap bitmap;
    private String filePat = null;
    private boolean sentToSettings = false;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private CircularImageView gambarUpload, popupfoto;
    private TextView toLogin, txtClose, popupnama, popupttl, popupagama, popupstatus, popuppekerjaan;
    private Button btnUpload, btnRegister, btnPopupRegister, btnCamera, btnGaleri;
    private EditText nik, inputEmail, inputPassword;
    private String inputNIK, getNama, getTempat, getTanggal, getAlamat, getAgama, getStatus, getPekerjaan;
    private Dialog myDialog, dialogUpload;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        myDialog = new Dialog(this);
        dialogUpload = new Dialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("user");
        storageReference = FirebaseStorage.getInstance().getReference();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        session = new SessionManager(getApplicationContext());
        db = new SQLiteHandler(getApplicationContext());

        if (session.isLoggedIn()) {
            Intent intent = new Intent(RegisterUser.this, Beranda.class);
            startActivity(intent);
            finish();

        }

        gambarUpload = (CircularImageView)findViewById(R.id.imgProfil);
        gambarUpload.setBorderColor(getResources().getColor(R.color.white));
        gambarUpload.setBorderWidth(10);
        gambarUpload.addShadow();
        gambarUpload.setShadowRadius(15);
        gambarUpload.setShadowColor(Color.WHITE);

        toLogin = findViewById(R.id.toLogin);
        btnUpload = findViewById(R.id.btnUpload);
        btnRegister = findViewById(R.id.btnRegister);
        nik = findViewById(R.id.nik);

        toLogin.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        gambarUpload.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == toLogin){
            startActivity(new Intent(getApplicationContext(), LoginUser.class));
            finish();
        }
        if (view == btnUpload || view == gambarUpload){
            ShowPopupUpload();
        }
        if (view == btnRegister){
            inputNIK = nik.getText().toString();
            if (bitmap == null){
                Toast.makeText(getApplicationContext(),"Isi Foto Profil Anda", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(inputNIK)){
                nik.setError(getString(R.string.error_field_required));
            } else {
                cekTerdaftar(inputNIK);
            }
        }
    }

    public void ShowPopupUpload() {
        dialogUpload.setContentView(R.layout.popup_choice);

        btnCamera =  dialogUpload.findViewById(R.id.btnCamera);
        btnGaleri =  dialogUpload.findViewById(R.id.btnGaleri);


        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                askPermission();
                captureImage();
            }
        });

        btnGaleri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });


        dialogUpload.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogUpload.show();
    }

    public void cekTerdaftar(String nik){
        pDialog.setTitle("Validasi NIK");
        pDialog.setMessage("Silahkan Tunggu");
        showDialog();
        databaseReference.orderByChild("nik").equalTo(nik)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUser data = dataSnapshot.getValue(mUser.class);
                        if (data != null){
                            myDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Anda sudah mendaftar! Login Sekarang", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginUser.class));
                            finish();
                            hideDialog();
                        } else if (data == null){
                            getDataNIK();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getDataNIK(){
        class getDataNIK extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog.setTitle("Mencari Data NIK");
                pDialog.setMessage("Silahkan Tunggu");
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                pDialog.dismiss();
                showDataNIK(s);
                if (getNama == null || getNama == "null"){
                    Toast.makeText(getApplicationContext(), "NIK anda salah!", Toast.LENGTH_SHORT).show();
                } else if (getNama != null || getNama != "null"){
                    ShowPopupRegister();
                } else {
                    Toast.makeText(getApplicationContext(), "Periksa koneksi internet anda!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String s = rh.sendGetRequestParam(Konfigurasi.URL_Detail, inputNIK);
                return s;
            }
        }
        getDataNIK ge = new getDataNIK();
        ge.execute();
    }

    private void showDataNIK(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray result = jsonObject.getJSONArray("result");
            JSONObject c = result.getJSONObject(0);
            getNama = c.getString(Konfigurasi.KEY_NAMA);
            getTempat = c.getString(Konfigurasi.KEY_TEMPAT);
            getTanggal = c.getString(Konfigurasi.KEY_TANGGAL);
            getAlamat = c.getString(Konfigurasi.KEY_ALAMAT);
            getAgama = c.getString(Konfigurasi.KEY_AGAMA);
            getStatus = c.getString(Konfigurasi.KEY_STATUS);
            getPekerjaan = c.getString(Konfigurasi.KEY_PEKERJAAN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void ShowPopupRegister() {
        myDialog.setContentView(R.layout.popup_register);

        txtClose =  myDialog.findViewById(R.id.txtclose);
        btnPopupRegister =  myDialog.findViewById(R.id.btnpopupregister);
        popupfoto = myDialog.findViewById(R.id.popupfoto);
        popupnama = myDialog.findViewById(R.id.popupnama);
        popupttl = myDialog.findViewById(R.id.popupttl);
        popupagama = myDialog.findViewById(R.id.popupagama);
        popupstatus =  myDialog.findViewById(R.id.popupstatus);
        popuppekerjaan = myDialog.findViewById(R.id.popuppekerjaan);

        inputEmail = myDialog.findViewById(R.id.popupemail);
        inputPassword = myDialog.findViewById(R.id.popuppassword);

        if (bitmap != null) {
            popupfoto.setImageBitmap(bitmap);
        }

        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(getTanggal);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        str = outputFormat.format(date);


        popupnama.setText(getNama);
        popupttl.setText(getTempat+", "+str);
        popupagama.setText(getAgama);
        popupstatus.setText(getStatus);
        popuppekerjaan.setText(getPekerjaan);

        btnPopupRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError(getString(R.string.error_field_required));
                    focusView = inputEmail;
                    cancel = true;
                } else if (TextUtils.isEmpty(password)) {
                    inputPassword.setError(getString(R.string.error_field_required));
                    focusView = inputPassword;
                    cancel = true;
                } else if (!isPasswordValid(password)) {
                    inputPassword.setError(getString(R.string.error_invalid_password));
                    focusView = inputPassword;
                    cancel = true;
                } else if (!isEmailValid(email)) {
                    inputEmail.setError(getString(R.string.error_invalid_email));
                    focusView = inputEmail;
                    cancel = true;
                } else if (cancel) {
                    focusView.requestFocus();
                } else if (!email.isEmpty() && !password.isEmpty()) {
                    registerUser(inputNIK, getNama, getTempat, getTanggal, getAlamat, getAgama,
                            getStatus, getPekerjaan, inputEmail.getText().toString(), inputPassword.getText().toString());
                }
            }
        });

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onKlik) {
                myDialog.dismiss();
            }
        });

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    private void registerUser(final String nik, final String nama, final String tempat_lahir, final String tanggal_lahir,
                              final String alamat, final String agama , final String status, final String pekerjaan,
                              final String email, final String password){
        pDialog.setMessage("Menyiapkan Akun Anda ...");
        showDialog();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            hideDialog();
                            uploadFoto(nik, nama, tempat_lahir, tanggal_lahir, alamat, agama, status, pekerjaan, email);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }
                    }
                });

    }

    private void uploadFoto(final String nik, final String nama, final String tempat_lahir, final String tanggal_lahir,
                            final String alamat, final String agama , final String status, final String pekerjaan,
                            final String email){
        StorageReference riversRef = storageReference.child("user/"+nik+".jpg");
        showDialog();

        riversRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pDialog.dismiss();
                        downloadUrl = taskSnapshot.getDownloadUrl();
                        Toast.makeText(getApplicationContext(), "Registrasi Sukses!", Toast.LENGTH_SHORT).show();
                        hideDialog();
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        mUser userInfo = new mUser(nik, nama, tempat_lahir, tanggal_lahir, alamat,
                                agama, status, pekerjaan, email, downloadUrl.toString(), "0", user.getUid());
//                            databaseReference.child(user.getUid()).setValue(userInfo);
                        databaseReference.child(nik).setValue(userInfo);

                        Intent intent = new Intent(RegisterUser.this, LoginUser.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        pDialog.setMessage(((int) progress) + "% Mendaftarkan Akun Anda ...");
                    }
                });

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (bitmap != null){
            btnUpload.setText("EDIT FOTO");
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        dialogUpload.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                gambarUpload.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                filePat = fileUri.getPath();

                gambarUpload.setVisibility(View.VISIBLE);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;

                bitmap = BitmapFactory.decodeFile(filePat, options);
                filePath = getImageUri(getApplicationContext(), bitmap);
                gambarUpload.setImageBitmap(bitmap);

            } else if (resultCode == RESULT_CANCELED) {
                gambarUpload.setImageBitmap(bitmap);
                Toast.makeText(getApplicationContext(),
                        "Batal", Toast.LENGTH_SHORT)
                        .show();

            } else {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void askPermission(){
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        if (ActivityCompat.checkSelfPermission(RegisterUser.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterUser.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterUser.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(RegisterUser.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterUser.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(RegisterUser.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,true);
            editor.commit();


        } else {
            proceedAfterPermission();
        }
    }

    private void proceedAfterPermission() {
//        Toast.makeText(getBaseContext(), "", Toast.LENGTH_LONG).show();
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        dialogUpload.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "upload");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + "upload directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}