package uk.co.altras.altrasGooglePay;



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
import com.google.android.gms.wallet.WalletConstants;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Optional;
public class CheckoutActivity extends AppCompatActivity {
    

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    public static final int PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;

    private PaymentsClient paymentsClient;

    /**
     * Initialize the Google Pay API on creation of the activity
     *
     * @see Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the mock information for our item in the UI.



        // Initialize a Google Pay API client for an environment suitable for testing.
        // It's recommended to create the PaymentsClient object inside of the onCreate method.
        // requestPayment();
        // possiblyShowGooglePayButton();
    }


    public   static boolean initGooglePay() {
        Wallet.WalletOptions walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        this.paymentsClient =  Wallet.getPaymentsClient(this, walletOptions);
        return true ;
    }

    // @Override
//     public void onActivityResult(int requestCode, int resultCode, Intent data) {
//         super.onActivityResult(requestCode, resultCode, data);
//         switch (requestCode) {
//             // value passed in AutoResolveHelper
//             case LOAD_PAYMENT_DATA_REQUEST_CODE:
//                 switch (resultCode) {

//                     case Activity.RESULT_OK:
//                         PaymentData paymentData = PaymentData.getFromIntent(data);
//                         handlePaymentSuccess(paymentData);
//                         break;

//                     case Activity.RESULT_CANCELED:
//                         // The user cancelled the payment attempt
//                         break;

//                     case AutoResolveHelper.RESULT_ERROR:
//                         Status status = AutoResolveHelper.getStatusFromIntent(data);
//                         handleError(status.getStatusCode());
//                         break;
//                 }

//                 // Re-enables the Google Pay payment button.
// //                googlePayButton.setClickable(true);
//         }
//     }


    public  static void canUseGooglePay(JSONObject isReadyToPayRequest,  CallbackContext callbackContext  ) {

        final Optional<JSONObject> isReadyToPayJson = Optional.of(isReadyToPayRequest);
        if (!isReadyToPayJson.isPresent()) {
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this,
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            callbackContext.success(task.getResult());

                            // setGooglePayAvailable(task.getResult());
                        } else {
                            // Log.w("isReadyToPay failed", task.getException());
                            callbackContext.error("isReadyToPay failed", task.getException());

                        }
                    }
                });
    }

}
