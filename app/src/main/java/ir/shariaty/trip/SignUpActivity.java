package ir.shariaty.trip;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonSignUp;
    private SignInButton buttonGoogleSignIn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore firestore;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        editTextName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("103272119870-i811n331dqam0e98c3cstuh4vlgt25mp.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonSignUp.setOnClickListener(v -> signUpUser());
        buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void signUpUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "همه فیلدها را پر کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "ایمیل معتبر وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "رمز عبور باید حداقل 6 کاراکتر باشد", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "رمز عبور و تکرار آن یکسان نیستند", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            saveUserInFirestore(user.getUid(), name, email, () -> {
                                                Toast.makeText(SignUpActivity.this, "ثبت‌ نام موفق", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                                                finish();
                                            });
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "خطا در ذخیره نام", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "خطا در ثبت‌ نام", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> startActivityForResult(signInIntent, RC_SIGN_IN));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "خطا در ثبت‌ نام با گوگل", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserInFirestore(user.getUid(), user.getDisplayName(), user.getEmail(), () -> {
                                Toast.makeText(SignUpActivity.this, "ثبت ‌نام با گوگل موفق", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                                finish();
                            });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "خطا در احراز هویت", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserInFirestore(String uid, String name, String email, Runnable onSuccess) {
        if (email == null) return;

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("name", name);
        userMap.put("email", email);

        firestore.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "کاربر ذخیره شد");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> Log.w("Firestore", "خطا در ذخیره کاربر", e));
    }
}
