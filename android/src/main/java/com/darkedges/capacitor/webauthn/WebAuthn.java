package com.darkedges.capacitor.webauthn;

import android.util.Log;
import androidx.credentials.CredentialManager;

public class WebAuthn {
    private CredentialManager credentialManager;

    public boolean isWebAuthnAvailable() {
        return isAvailable(WebAuthnTypes.WEBAUTHN);
    }
    public boolean isWebAuthnAutoFillAvailable() {
        return isAvailable(WebAuthnTypes.WEBAUTHNAUTOFILL);
    }

    private boolean isAvailable(WebAuthnTypes webAuthnType) {
        // Check if CredentialManager is available (Android 9+ with Google Play Services)
        // CredentialManager requires Android API 28+ and Google Play Services
        boolean val = false;
        if (this.credentialManager != null) {
            switch(webAuthnType) {
                case WEBAUTHN:
                    // WebAuthn is available if CredentialManager is initialized
                    val = true;
                    break;
                case WEBAUTHNAUTOFILL:
                    // AutoFill requires Android 13+ (API 33+)
                    val = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU;
                    break;
            }
        }
        return val;
    }

    public void setCredentialManager(CredentialManager credentialManager) {
        Log.i("setCredentialManager", String.valueOf(credentialManager));
        this.credentialManager=credentialManager;
    }

    public CredentialManager getCredentialManager() {
        return this.credentialManager;
    }
}
