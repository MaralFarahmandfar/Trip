package ir.shariaty.trip;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonSignUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        // اتصال به ویوها
        editTextName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        buttonSignUp.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // بررسی خالی بودن فیلدها
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "همه فیلدها را پر کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        // بررسی فرمت ایمیل
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "ایمیل معتبر وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        // بررسی طول رمز عبور
        if (password.length() < 6) {
            Toast.makeText(this, "رمز عبور باید حداقل 6 کاراکتر باشد", Toast.LENGTH_SHORT).show();
            return;
        }

        // بررسی تطابق رمز عبور
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "رمز عبور و تکرار آن یکسان نیستند", Toast.LENGTH_SHORT).show();
            return;
        }

        // ثبت ‌نام کاربر
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // تنظیم نام کاربر در پروفایل
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, "ثبت‌نام موفق بود", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "خطا در ذخیره نام: " + profileTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "خطا در ثبت‌نام: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}