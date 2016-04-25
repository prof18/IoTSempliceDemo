package it.prof.iotsemplicedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public final static String BLUE_LED_UUID = "ee0c1012-8786-40ba-ab96-99b91ac981d8";
    public final static String RED_LED_UUID = "ee0c1013-8786-40ba-ab96-99b91ac981d8";

    private EditText time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupButton();

    }

    private void setupButton() {
        Button button_open_gate = (Button) findViewById(R.id.button_led_blue);
        button_open_gate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent blueOn = new Intent(MainActivity.this, DeviceScanActivity.class);
                blueOn.putExtra("uuidExtra", BLUE_LED_UUID);
                blueOn.putExtra("timeExtra", 10);
                startActivity(blueOn);

                /*Intent openGate = new Intent(MainActivity.this, DeviceScanActivity.class);
                openGate.putExtra("COLOR", 0);  // 0 is red
                startActivity(openGate);*/
            }
        });

        Button button_lights_on = (Button) findViewById(R.id.button_led_red);
        button_lights_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
                Intent redOn = new Intent(MainActivity.this, DeviceScanActivity.class);
                redOn.putExtra("uuidExtra", RED_LED_UUID);
                redOn.putExtra("timeExtra", 10);
                startActivity(redOn);

            }
        });


    }
}
