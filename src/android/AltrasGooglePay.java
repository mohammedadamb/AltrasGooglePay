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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.PendingIntent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

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
    private CallbackContext mCallbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        this.cordovaInterface = cordova;
        cordova.setActivityResultCallback (this);

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.mCallbackContext = callbackContext ;
        
        if (action.equals("initGooglePay")) {
            String mode = args.getString(0); 
            this.initGooglePay( callbackContext, mode);
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

    //  Handle potential conflict from calling loadPaymentData.
//   ActivityResultLauncher<IntentSenderRequest> resolvePaymentForResult = registerForActivityResult(
//       new ActivityResultContracts.StartIntentSenderForResult(),
//       result -> {
//         switch (result.getResultCode()) {
//           case Activity.RESULT_OK:
//             Intent resultData = result.getData();
//             if (resultData != null) {
//               PaymentData paymentData = PaymentData.getFromIntent(result.getData());
//               if (paymentData != null) {
//                 handlePaymentSuccess(paymentData);
//               }
//             }
//             break;

//           case Activity.RESULT_CANCELED:
//             // The user cancelled the payment attempt
//             break;
//         }
//       });
    private  void initGooglePay( CallbackContext callbackContext, String mode) {
        // Intent myIntent = new Intent(CheckoutActivity.this, Katra_home.class);
        // startActivity(myIntent);
         Wallet.WalletOptions walletOptions = null ;
        // if(mode == "PRODUCTION"){
        //     walletOptions =
        //         new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION).build();
        // } else {
        //     walletOptions =
        //         new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        // }
        walletOptions =
                new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION).build();
        this.paymentsClient =  Wallet.getPaymentsClient(this.cordovaInterface.getActivity(), walletOptions);
        callbackContext.success("init successfully" + mode);



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
                            callbackContext.error("isReadyToPay failed : " + task.getException());

                        }
                    }
                });
    //    CheckoutActivity.canUseGooglePay(this.cordovaInterface.getActivity(), isReadyToPayRequest, callbackContext);
    }


//     private void requestPayment(JSONObject paymentDataRequest, CallbackContext callbackContext) {

//         // Disables the button to prevent multiple clicks.
// //        googlePayButton.setClickable(false);

//         // The price provided to the API should include taxes and shipping.
//         // This price is not displayed to the user.
// //        try {
//             ;
//             // long priceCents = 56.3;

//             Optional<JSONObject> paymentDataRequestJson = Optional.of(paymentDataRequest);
//             if (!paymentDataRequestJson.isPresent()) {
//                 this.mCallbackContext.error("invalid request body");
//                 return;
//             }

//             PaymentDataRequest request =
//                     PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());

//             // Since loadPaymentData may show the UI asking the user to select a payment method, we use
//             // AutoResolveHelper to wait for the user interacting with it. Once completed,
//             // onActivityResult will be called with the result.
//             if (request != null) {
//                 AutoResolveHelper.resolveTask(
//                         this.paymentsClient.loadPaymentData(request),
//                         this.cordovaInterface.getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
//             }

//             this.mCallbackContext.error("invalid request body 2");

//     }

    private void requestPayment(JSONObject paymentDataRequest, CallbackContext callbackContext) {

        // Disables the button to prevent multiple clicks.
//        googlePayButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
//        try {
            ;
            // long priceCents = 56.3;

            Optional<JSONObject> paymentDataRequestJson = Optional.of(paymentDataRequest);
            if (!paymentDataRequestJson.isPresent()) {
                this.mCallbackContext.error("invalid request body");
                return;
            }

            PaymentDataRequest request =
                    PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
                    if (request == null) {
                        this.mCallbackContext.error("invalid request body 2");
                        return; 
                    }
            // Since loadPaymentData may show the UI asking the user to select a payment method, we use
            // AutoResolveHelper to wait for the user interacting with it. Once completed,
            // onActivityResult will be called with the result.
            final Task<PaymentData> task = this.paymentsClient.loadPaymentData(request);
            //  if (request != null) {
            //     AutoResolveHelper.resolveTask(
            //             this.paymentsClient.loadPaymentData(request),
            //             this.cordovaInterface.getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
            // }
             task.addOnCompleteListener(completedTask -> {
        if (completedTask.isSuccessful()) {
        handlePaymentSuccess(completedTask.getResult());
      } else {
        Exception exception = completedTask.getException();
        // this.mCallbackContext.error(exception.getMessage());

        if (exception instanceof ResolvableApiException) {
          PendingIntent resolution = ((ResolvableApiException) exception).getResolution();
        //   resolvePaymentForResult.launch(new IntentSenderRequest.Builder(resolution).build());
         AutoResolveHelper.resolveTask(
                    task,
                    this.cordovaInterface.getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
            // this.mCallbackContext.error(exception.getMessage());

        } else if (exception instanceof ApiException) {
          ApiException apiException = (ApiException) exception;
            this.mCallbackContext.error("api error 12");

        //   handleError(apiException.getStatusCode(), apiException.getMessage());

        } else {
        this.mCallbackContext.error("api error");
        //   handleError(CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
        //       " exception when trying to deliver the task result to an activity!");
        }
      }

      // Re-enables the Google Pay payment button.
    });
            // if (request != null) {
            //     AutoResolveHelper.resolveTask(
            //             this.paymentsClient.loadPaymentData(request),
            //             this.cordovaInterface.getActivity(), LOAD_PAYMENT_DATA_REQUEST_CODE);
            // }


//        } catch (JSONException e) {
//            throw new RuntimeException("The price cannot be deserialized from the JSON object.");
//        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode  Result code returned by the Google Pay API.
     * @param data        Intent from the Google Pay API containing payment or error data.
     * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
     * from an Activity</a>
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JSONObject response = new JSONObject() ;
                // this.mCallbackContext.error("getting response ");
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
            try {
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;

                    case Activity.RESULT_CANCELED:
                     response.put("status", 21).put("message", "customer canceled the payment");
                    this.mCallbackContext.success(response);

                        // The user cancelled the payment attempt
                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        // handleError(status.getStatusCode());
                         response.put("status", 11).put("message", "customer canceled the payment 2");
                        this.mCallbackContext.error(response);
                        break;
                }

        } catch (JSONException e) {
            throw new RuntimeException("Json Exception");
        }

                // Re-enables the Google Pay payment button.
//                googlePayButton.setClickable(true);
        }
    }


    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see <a href="https://developers.google.com/pay/api/android/reference/
     * object#PaymentData">PaymentData</a>
     */
    private void handlePaymentSuccess(PaymentData paymentData) {

        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        final String paymentInfo = paymentData.toJson();
        //  Toast.makeText(
        //             this.cordovaInterface.getActivity(), paymentInfo,
        //             Toast.LENGTH_LONG).show();
        if (paymentInfo == null) {
            return;
        }

        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo);
            JSONObject response = new JSONObject().put("status", 0).put("paymentData", paymentMethodData);
            this.mCallbackContext.success(response);
            // JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            // final JSONObject tokenizationData = paymentMethodData.getJSONObject("tokenizationData");
            // final String token = tokenizationData.getString("token");
            // final JSONObject info = paymentMethodData.getJSONObject("info");
            // final String billingName = info.getJSONObject("billingAddress").getString("name");
            // Toast.makeText(
            //         this.cordovaInterface.getActivity(), "Successfully received payment data",
            //         Toast.LENGTH_LONG).show();

            // Logging token string.
            // Log.d("Google Pay token: ", token);

        } catch (JSONException e) {
            throw new RuntimeException("Json Exception");
        }
    }

}
