package uk.co.altras.altrasGooglePay;

import org.apache.cordova.CordovaPlugin;
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

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0); 
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("initGooglePay")) {
            // String message = args.getString(0); 
            this.initGooglePay(this, callbackContext);
            return true;
        }
        if (action.equals("canUseGooglePay")) {
            JSONObject isReadyToPayRequest = args.getJSONObject(0);
            this.canUseGooglePay(isReadyToPayRequest, callbackContext);
            return true;
        }
        return false;
    }

    private  void initGooglePay( CallbackContext callbackContext) {
        if(CheckoutActivity.initGooglePay()) {
            callbackContext.success("init successfully");
        } else {
            callbackContext.error("init successfully");
        }

    }



    private void canUseGooglePay(JSONObject isReadyToPayRequest, CallbackContext callbackContext ) {
       CheckoutActivity.canUseGooglePay(isReadyToPayRequest, callbackContext);
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
