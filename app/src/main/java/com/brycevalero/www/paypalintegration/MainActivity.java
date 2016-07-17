package com.brycevalero.www.paypalintegration;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PAYPAL_REQUEST_CODE = 123;
    private static PayPalConfiguration config;

    private Button buttonPay;
    private EditText editTextAmount;

    private String paymentAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPay = (Button) findViewById(R.id.buttonPay);
        editTextAmount = (EditText) findViewById(R.id.editTextAmount);

        buttonPay.setOnClickListener(this);

        config = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

        startPayPalService();
    }

    @Override
    public void onDestroy(){
        stopPayPalService();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        getPayment();
    }

    public void startPayPalService() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        startService(intent);
    }

    public void stopPayPalService() {
        stopService(new Intent(this, PayPalService.class));
    }

    public void getPayment(){
        paymentAmount = editTextAmount.getText().toString();
        Log.i("getPayment", "paymentAmount: " + paymentAmount);

        PayPalPayment payment = new PayPalPayment(
                new BigDecimal(String.valueOf(paymentAmount)),
                "USD", "Random item",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        Log.i("onActivityResult", "Were Back from Paypal");
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){

                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                if(confirm != null){
                    try{
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.i("paymentDetails", paymentDetails);

                        startActivity(new Intent(this, ConfirmationActivity.class)
                            .putExtra("PaymentDetails", paymentDetails)
                            .putExtra("PaymentAmount", paymentAmount));
                    }
                    catch (JSONException e){
                        Log.e("paymentExample", "error", e);
                    }
                }
                else if(resultCode == Activity.RESULT_CANCELED){
                    Log.i("paymentExample", "User Canceled");
                }
                else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
                    Log.i("paymentExample", "Invalid Payment of configuration");
                }
            }
        }
    }

}
