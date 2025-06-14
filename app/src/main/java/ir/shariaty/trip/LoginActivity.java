package ir.shariaty.trip;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonGotoSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGotoSignUp = findViewById(R.id.buttonGotoSignUp);

        buttonLogin.setOnClickListener(v -> loginUser());

        buttonGotoSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "ایمیل و رمز عبور را وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("test@mail.com".equals(email) && "123456".equals(password)) {
            Toast.makeText(this, "ورود موفقیت‌آمیز بود", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "ایمیل یا رمز اشتباه است", Toast.LENGTH_SHORT).show();
        }
    }
}