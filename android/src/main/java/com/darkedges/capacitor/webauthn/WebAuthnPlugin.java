package com.darkedges.capacitor.webauthn;

import android.os.CancellationSignal;
import android.util.Log;

import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreatePublicKeyCredentialRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetPublicKeyCredentialOption;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.CreateCredentialCancellationException;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.CreateCredentialInterruptedException;
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException;
import androidx.credentials.exceptions.CreateCredentialUnknownException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONException;


@CapacitorPlugin(name = "WebAuthn")
public class WebAuthnPlugin extends Plugin {
    private final WebAuthn implementation = new WebAuthn();

    @Override
    public void load() {
        super.load();
        // Use activity context (not application context) for CredentialManager.
        // Some credential providers inspect the context type for UI hosting decisions.
        implementation.setCredentialManager(CredentialManager.create(getActivity()));
    }

    @PluginMethod
    public void isWebAuthnAvailable(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.isWebAuthnAvailable());
        call.resolve(ret);
    }

    @PluginMethod
    public void isWebAuthnAutoFillAvailable(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.isWebAuthnAutoFillAvailable());
        call.resolve(ret);
    }

    @PluginMethod
    public void startAuthentication(PluginCall call) {
        // Keep the call alive across activity lifecycle changes.
        // When CredentialSelectorActivity opens, MainActivity pauses.
        // Without this, the PluginCall reference is invalidated and the
        // onResult/onError callbacks silently fail to deliver results.
        call.setKeepAlive(true);

        String requestJson = call.getData().toString();

        // Only request passkey credentials (no password option).
        // Including GetPasswordOption causes issues when Google account
        // auth tokens are stale (BadAuthentication), which can block
        // the entire credential flow including passkeys.
        GetPublicKeyCredentialOption getPublicKeyCredentialOption =
                new GetPublicKeyCredentialOption(requestJson, null);
        GetCredentialRequest getCredRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(getPublicKeyCredentialOption)
                .build();
        CancellationSignal cancellationSignal = null;
        implementation.getCredentialManager().getCredentialAsync(
                getActivity(),
                getCredRequest,
                cancellationSignal,
                getContext().getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof PublicKeyCredential) {
                            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
                            fidoAuthenticateToServer(call, responseJson);
                        } else {
                            handlePasskeyError(call, "Unexpected type of credential", credential.getClass().getName());
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        handlePasskeyError(call, "Sign in failed with exception", e.getMessage());
                    }
                }
        );
    }

    @PluginMethod
    public void startRegistration(PluginCall call) {
        // Keep the call alive across activity lifecycle changes.
        call.setKeepAlive(true);

        String requestJson = call.getData().toString();
        CreatePublicKeyCredentialRequest createPublicKeyCredentialRequest =
                new CreatePublicKeyCredentialRequest(requestJson, null, false);
        CancellationSignal cancellationSignal = null;
        implementation.getCredentialManager().createCredentialAsync(getActivity(), createPublicKeyCredentialRequest, cancellationSignal, getContext().getMainExecutor(),
                new CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>() {
                    @Override
                    public void onResult(CreateCredentialResponse result) {
                        handleSuccessfulCreatePasskeyResult(call, result);
                    }

                    @Override
                    public void onError(CreateCredentialException e) {
                        if (e instanceof CreatePublicKeyCredentialDomException) {
                            handlePasskeyError(call, "CreatePublicKeyCredentialDomException", ((CreatePublicKeyCredentialDomException)e).getMessage());
                        } else if (e instanceof CreateCredentialCancellationException) {
                            handlePasskeyError(call, "CreateCredentialCancellationException", ((CreateCredentialCancellationException)e).getMessage());
                        } else if (e instanceof CreateCredentialInterruptedException) {
                            handlePasskeyError(call, "CreateCredentialInterruptedException", ((CreateCredentialInterruptedException)e).getMessage());
                        } else if (e instanceof CreateCredentialProviderConfigurationException) {
                            handlePasskeyError(call, "CreateCredentialProviderConfigurationException", ((CreateCredentialProviderConfigurationException)e).getMessage());
                        } else if (e instanceof CreateCredentialUnknownException) {
                            handlePasskeyError(call, "CreateCredentialUnknownException", ((CreateCredentialUnknownException)e).getMessage());
                        } else {
                            handlePasskeyError(call, "Unexpected exception type", e.getMessage());
                        }
                    }
                });
    }

    private void fidoAuthenticateToServer(PluginCall call, String responseJson) {
        try {
            JSObject ret = new JSObject(responseJson);
            call.resolve(ret);
        } catch (JSONException e) {
            call.reject("Failed to parse webauthn response", e);
        }
    }

    private void handlePasskeyError(PluginCall call, String type, String e) {
        JSObject ret = new JSObject();
        ret.put(type, e);
        call.reject(e, ret);
    }

    private void handleSuccessfulCreatePasskeyResult(PluginCall call, CreateCredentialResponse createCredentialResponse) {
        try {
            JSObject ret = new JSObject(createCredentialResponse.getData().getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON"));
            call.resolve(ret);
        } catch (JSONException e) {
            call.reject("Failed to parse registration response", e);
        }
    }
}
