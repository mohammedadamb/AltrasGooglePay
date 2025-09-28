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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

import java.util.Optional;

public class AltrasGooglePay extends CordovaPlugin {
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private static final String TAG = "AltrasGooglePay";

    private PaymentsClient paymentsClient;
    private CallbackContext currentPaymentCallbackContext;
    private boolean isPaymentInProgress = false;
    private long lastPaymentRequestTime = 0;
    private static final long MIN_PAYMENT_INTERVAL = 3000; // 3 seconds cooldown

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova.setActivityResultCallback(this);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "Executing action: " + action);
        
        if (action.equals("initGooglePay")) {
            String mode = args.getString(0); 
            this.initGooglePay(callbackContext, mode);
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
        if (action.equals("resetPaymentState")) {
            this.resetPaymentState(callbackContext);
            return true;
        }
        return false;
    }

    private void initGooglePay(CallbackContext callbackContext, String mode) {
        try {
            Wallet.WalletOptions walletOptions;
            if ("PRODUCTION".equals(mode)) {
                walletOptions = new Wallet.WalletOptions.Builder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
                    .build();
            } else {
                walletOptions = new Wallet.WalletOptions.Builder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .build();
            }
            
            this.paymentsClient = Wallet.getPaymentsClient(this.cordova.getActivity(), walletOptions);
            callbackContext.success("Google Pay initialized successfully in " + mode + " mode");
            
        } catch (Exception e) {
            Log.e(TAG, "Init Google Pay error: " + e.getMessage());
            callbackContext.error("Failed to initialize Google Pay: " + e.getMessage());
        }
    }

    private void canUseGooglePay(JSONObject isReadyToPayRequest, CallbackContext callbackContext) {
        try {
            final Optional<JSONObject> isReadyToPayJson = Optional.of(isReadyToPayRequest);
            if (!isReadyToPayJson.isPresent()) {
                callbackContext.error("Invalid request body");
                return;
            }

            IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
            Task<Boolean> task = this.paymentsClient.isReadyToPay(request);
            
            task.addOnCompleteListener(this.cordova.getActivity(), new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    try {
                        if (task.isSuccessful()) {
                            boolean isReady = task.getResult();
                            JSONObject response = new JSONObject();
                            response.put("status", 0);
                            response.put("canUseGooglePay", isReady);
                            response.put("message", isReady ? "Google Pay is available" : "Google Pay not available");
                            callbackContext.success(response);
                        } else {
                            Log.w(TAG, "isReadyToPay failed", task.getException());
                            callbackContext.error("Google Pay check failed: " + task.getException().getMessage());
                        }
                    } catch (Exception e) {
                        callbackContext.error("Error processing Google Pay availability: " + e.getMessage());
                    }
                }
            });
            
        } catch (Exception e) {
            callbackContext.error("Error in canUseGooglePay: " + e.getMessage());
        }
    }

    private void requestPayment(JSONObject paymentDataRequest, CallbackContext callbackContext) {
        Log.d(TAG, "requestPayment called");
        
        // Check if payment is already in progress
        if (isPaymentInProgress) {
            callbackContext.error("Payment already in progress. Please wait.");
            return;
        }
        
        // Check cooldown period
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPaymentRequestTime < MIN_PAYMENT_INTERVAL) {
            long waitTime = (MIN_PAYMENT_INTERVAL - (currentTime - lastPaymentRequestTime)) / 1000;
            callbackContext.error("Please wait " + waitTime + " seconds before another payment");
            return;
        }

        try {
            Optional<JSONObject> paymentDataRequestJson = Optional.of(paymentDataRequest);
            if (!paymentDataRequestJson.isPresent()) {
                callbackContext.error("Invalid payment data request");
                return;
            }

            PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
            if (request == null) {
                callbackContext.error("Failed to create payment request");
                return;
            }

            // Set payment state
            this.currentPaymentCallbackContext = callbackContext;
            this.isPaymentInProgress = true;
            this.lastPaymentRequestTime = currentTime;

            // Execute on UI thread
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Task<PaymentData> task = paymentsClient.loadPaymentData(request);
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
    });                    } catch (Exception e) {
                        Log.e(TAG, "Error starting payment: " + e.getMessage());
                        resetPaymentState();
                        currentPaymentCallbackContext.error("Failed to start payment: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in requestPayment: " + e.getMessage());
            resetPaymentState();
            callbackContext.error("Payment request failed: " + e.getMessage());
        }
    }

    private void resetPaymentState(CallbackContext callbackContext) {
        resetPaymentState();
        callbackContext.success("Payment state reset successfully");
    }

    private void resetPaymentState() {
        Log.d(TAG, "Resetting payment state");
        this.isPaymentInProgress = false;
        this.currentPaymentCallbackContext = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (requestCode != LOAD_PAYMENT_DATA_REQUEST_CODE) {
            return;
        }

        // Always reset payment state when we get any response
        boolean wasPaymentInProgress = isPaymentInProgress;
        resetPaymentState();

        if (currentPaymentCallbackContext == null) {
            Log.w(TAG, "No callback context for payment result");
            return;
        }

        try {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null) {
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        if (paymentData != null) {
                            handlePaymentSuccess(paymentData);
                        } else {
                            currentPaymentCallbackContext.error("No payment data received");
                        }
                    } else {
                        currentPaymentCallbackContext.error("No data received from payment");
                    }
                    break;

                case Activity.RESULT_CANCELED:
                    JSONObject cancelResponse = new JSONObject();
                    cancelResponse.put("status", 21);
                    cancelResponse.put("message", "Customer cancelled the payment");
                    currentPaymentCallbackContext.success(cancelResponse);
                    break;

                case AutoResolveHelper.RESULT_ERROR:
                    if (data != null) {
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        JSONObject errorResponse = new JSONObject();
                        errorResponse.put("status", 11);
                        errorResponse.put("message", "Payment error: " + (status != null ? status.getStatusMessage() : "Unknown error"));
                        errorResponse.put("statusCode", status != null ? status.getStatusCode() : -1);
                        currentPaymentCallbackContext.error(errorResponse);
                    } else {
                        currentPaymentCallbackContext.error("Payment failed with unknown error");
                    }
                    break;

                default:
                    currentPaymentCallbackContext.error("Unknown payment result: " + resultCode);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON error in onActivityResult: " + e.getMessage());
            currentPaymentCallbackContext.error("Error processing payment result: " + e.getMessage());
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        try {
            String paymentInfo = paymentData.toJson();
            if (paymentInfo == null) {
                currentPaymentCallbackContext.error("No payment information received");
                return;
            }

            JSONObject paymentMethodData = new JSONObject(paymentInfo);
            JSONObject response = new JSONObject();
            response.put("status", 0);
            response.put("message", "Payment successful");
            response.put("paymentData", paymentMethodData);
            
            currentPaymentCallbackContext.success(response);
            
        } catch (JSONException e) {
            Log.e(TAG, "JSON error in handlePaymentSuccess: " + e.getMessage());
            currentPaymentCallbackContext.error("Error processing payment data: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resetPaymentState();
        Log.d(TAG, "Plugin destroyed, state reset");
    }
}