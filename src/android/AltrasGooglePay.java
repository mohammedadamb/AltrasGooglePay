package uk.co.altras.altrasGooglePay;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Optional;

import uk.co.altras.altrasGooglePay.CheckoutActivity;

/**
 * This class echoes a string called from JavaScript.
 */
public class AltrasGooglePay extends CordovaPlugin {
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    private PaymentsClient paymentsClient;
    private CordovaInterface cordovaInterface;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        this.cordovaInterface = cordova;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0); 
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("initGooglePay")) {
            // String message = args.getString(0); 
            this.initGooglePay( callbackContext);
            return true;
        }
        if (action.equals("canUseGooglePay")) {
            JSONObject isReadyToPayRequest = args.getJSONObject(0);
            this.canUseGooglePay(isReadyToPayRequest, callbackContext);
            return true;
        }
        if (action.equals("requestPayment")) {
            JSONObject paymentDataRequest = args.getJSONObject(0);
            this.requestPayment(paymentDataRequest, callbackContext);
            return true;
        }
        return false;
    }

    private  void initGooglePay( CallbackContext callbackContext) {
        // Intent myIntent = new Intent(CheckoutActivity.this, Katra_home.class);
        // startActivity(myIntent);

        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        this.paymentsClient =  Wallet.getPaymentsClient(this.cordovaInterface.getActivity(), walletOptions);
        callbackContext.success("init successfully");



        // if(CheckoutActivity.initGooglePay(this.cordovaInterface.getActivity())) {
        //     callbackContext.success("init successfully");
        // } else {
        //     callbackContext.error("init successfully");
        // }

    }



    private void canUseGooglePay(JSONObject isReadyToPayRequest, CallbackContext callbackContext ) {

        final Optional<JSONObject> isReadyToPayJson = Optional.of(isReadyToPayRequest);
        if (!isReadyToPayJson.isPresent()) {
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        Task<Boolean> task = this.paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this.cordovaInterface.getActivity(),
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task)   {
                        if (task.isSuccessful()) {
                            try {
                            callbackContext.success(new JSONObject().put("Status", 0).put("message", "can use google pay"));
                            } catch (JSONException ex) {
                                callbackContext.success("Json Exception");

                            }
                            // setGooglePayAvailable(task.getResult());
                        } else {
                            // Log.w("isReadyToPay failed", task.getException());
                            callbackContext.error("isReadyToPay failed");

                        }
                    }
                });
    //    CheckoutActivity.canUseGooglePay(this.cordovaInterface.getActivity(), isReadyToPayRequest, callbackContext);
    }


    private void requestPayment(JSONObject paymentDataRequest, CallbackContext callbackContext) {

        // Disables the button to prevent multiple clicks.
//        googlePayButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
//        try {
            ;
            long priceCents = 56.3;

            Optional<JSONObject> paymentDataRequestJson = Optional.of(paymentDataRequest);
            if (!paymentDataRequestJson.isPresent()) {
                return;
            }

            PaymentDataRequest request =
                    PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());

            // Since loadPaymentData may show the UI asking the user to select a payment method, we use
            // AutoResolveHelper to wait for the user interacting with it. Once completed,
            // onActivityResult will be called with the result.
            if (request != null) {
                AutoResolveHelper.resolveTask(
                        this.paymentsClient.loadPaymentData(request),
                        this.cordovaInterface.getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
            }

//        } catch (JSONException e) {
//            throw new RuntimeException("The price cannot be deserialized from the JSON object.");
//        }
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
