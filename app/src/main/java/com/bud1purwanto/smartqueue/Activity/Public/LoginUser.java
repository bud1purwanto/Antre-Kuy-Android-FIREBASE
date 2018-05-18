package com.bud1purwanto.smartqueue.Activity.Public;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bud1purwanto.smartqueue.Activity.User.Beranda;
import com.bud1purwanto.smartqueue.Controller.SQLiteHandler;
import com.bud1purwanto.smartqueue.Controller.SessionManager;
import com.bud1purwanto.smartqueue.Models.mUser;
import com.bud1purwanto.smartqueue.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LoginUser extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = LoginUser.class.getSimpleName();
    private Button btnLogin;
    private TextView toRegister;
    private AutoCompleteTextView inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private SharedPreferences loginPref;
    private static final String PREF_NAME = TAG+"_PREF";
    private ArrayAdapter<String> adapter;
    private List<String> listEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("user");
        loginPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        toRegister = findViewById(R.id.toRegister);

        listEmail = new ArrayList<String>();
        listEmail.add(loginPref.getString("EMAIL", null));
        adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, listEmail);
        inputEmail.setAdapter(adapter);

        String storedEmail = loginPref.getString("EMAIL","");
        inputEmail.setText(storedEmail);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn() && firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginUser.this, Beranda.class);
            startActivity(intent);
            finish();
        }

        toRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == toRegister){
            startActivity(new Intent(getApplicationContext(), RegisterUser.class));
            finish();
        }
        if(view == btnLogin){
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
                cekLogin(email, password);
            }
        }
    }

    private void cekLogin(final String email, String password) {
        pDialog.setMessage("Memproses Login ...");
        showDialog();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            session.setLogin(true);
                            SharedPreferences.Editor editor = loginPref.edit();
                            editor.putString("EMAIL", email);
                            editor.apply();
                            listEmail.add(loginPref.getString("EMAIL", email));

                            Intent intent = new Intent(LoginUser.this,
                                    Beranda.class);
                            hideDialog();
                            startActivity(intent);
                            finish();

                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot user : dataSnapshot.getChildren()){
                                        mUser data = user.getValue(mUser.class);
                                        FirebaseUser u = firebaseAuth.getCurrentUser();
                                        if(u.getUid().equals(data.getUid())){
                                            db.addUser(data.getNik(), data.getNama(), data.getTempat_lahir(),
                                                    data.getTanggal_lahir(), data.getAlamat(), data.getAgama(),
                                                    data.getStatus(), data.getPekerjaan(), data.getEmail(),
                                                    data.getFoto(), data.getAntre(), u.getUid());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            hideDialog();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Email atau password salah", Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }
                    }
                });

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
