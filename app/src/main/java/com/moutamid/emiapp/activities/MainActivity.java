package com.moutamid.emiapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.emiapp.R;
import com.moutamid.emiapp.databinding.ActivityMainBinding;
import com.moutamid.emiapp.models.UserModel;
import com.moutamid.emiapp.utils.Constants;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding b;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Constants.auth().getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        b.signUpBtn.setOnClickListener(v -> {
            String email = b.emailEt.getText().toString();
            String password = b.passwordEt.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(this, "Email is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Password is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            progressDialog.show();
            Constants.auth().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                uploadUserData();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        private void uploadUserData() {
                            UserModel userModel = new UserModel();
                            userModel.email = email;
                            userModel.password = password;
                            userModel.uid = Constants.auth().getUid();
                            userModel.isLocked = false;

                            Constants.databaseReference().child(Constants.USERS)
                                    .child(Constants.auth().getUid())
                                    .setValue(userModel)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                Stash.put(Constants.CURRENT_USER_MODEL, userModel);
                                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                finish();
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
        });

        b.loginBtn.setOnClickListener(v -> {
            String email = b.emailEt.getText().toString();
            String password = b.passwordEt.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(this, "Email is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Password is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            progressDialog.show();
            Constants.auth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                getUserData();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        private void getUserData() {
                            Constants.databaseReference().child(Constants.USERS)
                                    .child(Constants.auth().getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            progressDialog.dismiss();
                                            if (snapshot.exists()) {
                                                UserModel userModel = snapshot.getValue(UserModel.class);
                                                Stash.put(Constants.CURRENT_USER_MODEL, userModel);
                                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                finish();
                                                startActivity(intent);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressDialog.dismiss();
                                            Toast.makeText(MainActivity.this, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }


                    });

        });

    }
}