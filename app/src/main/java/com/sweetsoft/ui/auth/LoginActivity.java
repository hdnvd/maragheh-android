package com.sweetsoft.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.Session;
import com.sweetsoft.data.model.AuthResponse;
import com.sweetsoft.data.model.OtpResponse;
import com.sweetsoft.databinding.ActivityLoginBinding;
import com.sweetsoft.ui.common.BaseActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.ui.photos.UploadPhotoActivity;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.Ui;
import com.sweetsoft.util.SystemBars;

/** Phone number + SMS one-time code (sent via Kavenegar on the server). */
public class LoginActivity extends BaseActivity {
    public static final String EXTRA_NEXT_UPLOAD = "next_upload";

    private ActivityLoginBinding b;
    private String phone;
    private boolean needsName;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, getString(R.string.title_login));
        SystemBars.padBottom(b.getRoot());

        b.sendCode.setOnClickListener(v -> requestCode());
        b.phone.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_DONE) {
                requestCode();
                return true;
            }
            return false;
        });
        b.verify.setOnClickListener(v -> verify());
        b.code.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_DONE) {
                verify();
                return true;
            }
            return false;
        });
        b.resend.setOnClickListener(v -> requestCode());
        b.editPhone.setOnClickListener(v -> showPhoneStep());
    }

    /** Normalizes 09xxxxxxxxx / +989xxxxxxxxx / 9xxxxxxxxx to 09xxxxxxxxx. */
    private static String normalize(String raw) {
        String p = Fa.toLatinDigits(raw).replaceAll("[^0-9+]", "");
        if (p.startsWith("+98")) p = "0" + p.substring(3);
        else if (p.startsWith("0098")) p = "0" + p.substring(4);
        else if (p.startsWith("9") && p.length() == 10) p = "0" + p;
        return p.matches("^09\\d{9}$") ? p : null;
    }

    private void requestCode() {
        String input = b.phone.getText() != null ? b.phone.getText().toString() : "";
        String p = normalize(input);
        if (p == null) {
            b.phoneLayout.setError(getString(R.string.phone_invalid));
            return;
        }
        b.phoneLayout.setError(null);
        phone = p;
        setBusy(true);
        track(ApiClient.get().requestOtp(phone)).enqueue(new Callback<OtpResponse>() {
            @Override
            public void onSuccess(OtpResponse body) {
                setBusy(false);
                needsName = body == null || !body.isRegistered;
                showCodeStep(body != null && body.resendIn > 0 ? body.resendIn : 120);
            }

            @Override
            public void onError(String message, int code) {
                setBusy(false);
                Ui.snack(b.getRoot(), message);
            }
        });
    }

    private void showCodeStep(int resendSeconds) {
        b.stepPhone.setVisibility(View.GONE);
        b.stepCode.setVisibility(View.VISIBLE);
        b.codeInfo.setText(getString(R.string.code_sent_to, Fa.digits(phone)));
        b.nameLayout.setVisibility(needsName ? View.VISIBLE : View.GONE);
        b.code.setText("");
        (needsName ? b.name : b.code).requestFocus();
        startTimer(resendSeconds);
    }

    private void showPhoneStep() {
        if (timer != null) timer.cancel();
        b.stepCode.setVisibility(View.GONE);
        b.stepPhone.setVisibility(View.VISIBLE);
    }

    private void startTimer(int seconds) {
        if (timer != null) timer.cancel();
        b.resend.setEnabled(false);
        timer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long ms) {
                b.resend.setText(getString(R.string.resend_in, Fa.digits(ms / 1000)));
            }

            @Override
            public void onFinish() {
                b.resend.setEnabled(true);
                b.resend.setText(R.string.resend);
            }
        }.start();
    }

    private void verify() {
        String code = Fa.toLatinDigits(b.code.getText() != null ? b.code.getText().toString().trim() : "");
        String name = b.name.getText() != null ? b.name.getText().toString().trim() : "";
        if (needsName && name.length() < 2) {
            b.nameLayout.setError(getString(R.string.name_required));
            return;
        }
        b.nameLayout.setError(null);
        if (code.length() < 4) {
            b.codeLayout.setError(getString(R.string.code_hint));
            return;
        }
        b.codeLayout.setError(null);
        setBusy(true);
        track(ApiClient.get().verifyOtp(phone, code, needsName ? name : null)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse body) {
                setBusy(false);
                if (body == null || body.token == null) {
                    Ui.snack(b.getRoot(), getString(R.string.error_network));
                    return;
                }
                Session.save(body.token, body.user);
                if (getIntent().getBooleanExtra(EXTRA_NEXT_UPLOAD, false)) {
                    startActivity(new Intent(LoginActivity.this, UploadPhotoActivity.class));
                }
                finish();
            }

            @Override
            public void onError(String message, int code) {
                setBusy(false);
                b.codeLayout.setError(message);
            }
        });
    }

    private void setBusy(boolean busy) {
        b.progress.setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
        b.sendCode.setEnabled(!busy);
        b.verify.setEnabled(!busy);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
