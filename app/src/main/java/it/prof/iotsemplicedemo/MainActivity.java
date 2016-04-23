package it.prof.iotsemplicedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupButton();

    }

    private void setupButton() {
        Button button_open_gate = (Button) findViewById(R.id.button_open_gate);
        button_open_gate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openScan = new Intent(MainActivity.this, DeviceScanActivity.class);
                startActivity(openScan);

                /*Intent openGate = new Intent(MainActivity.this, DeviceScanActivity.class);
                openGate.putExtra("COLOR", 0);  // 0 is red
                startActivity(openGate);*/
            }
        });

        Button button_lights_on = (Button) findViewById(R.id.button_lights_on);
        button_lights_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();

            }
        });


    }
}
