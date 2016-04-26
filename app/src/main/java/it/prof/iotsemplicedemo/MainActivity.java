/*  Copyright (c) 2016 Marco Gomiero
*   
*   Permission is hereby granted, free of charge, to any person obtaining a copy 
*   of this software and associated documentation files (the "Software"), to deal 
*   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
*   copies of the Software, and to permit persons to whom the Software is
*   furnished to do so, subject to the following conditions:
*
*   The above copyright notice and this permission notice shall be included in all 
*   copies or substantial portions of the Software.
*
*   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
*   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
*   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
*   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
*   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
*   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
*   SOFTWARE. 
*/

package it.prof.iotsemplicedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //UUID of the leds
    public final static String BLUE_LED_UUID = "ee0c1012-8786-40ba-ab96-99b91ac981d8";
    public final static String RED_LED_UUID = "ee0c1013-8786-40ba-ab96-99b91ac981d8";

    private EditText time;
    private Button buttonLedBlue;
    private Button buttonLedRed;

    private int seconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupButton();

    }

    private void setupButton() {
        buttonLedBlue = (Button) findViewById(R.id.button_led_blue);
        time = (EditText) findViewById(R.id.edit_time);

        buttonLedBlue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                try {

                    seconds = Integer.valueOf(time.getText().toString());
                    Intent blueOn = new Intent(MainActivity.this, DeviceScanActivity.class);
                    blueOn.putExtra("uuidExtra", BLUE_LED_UUID);
                    blueOn.putExtra("timeExtra", seconds);
                    startActivity(blueOn);

                } catch (NumberFormatException e) {

                    Toast.makeText(MainActivity.this, "You must type a time!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        buttonLedRed = (Button) findViewById(R.id.button_led_red);
        buttonLedRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    seconds = Integer.valueOf(time.getText().toString());
                    Intent redOn = new Intent(MainActivity.this, DeviceScanActivity.class);
                    redOn.putExtra("uuidExtra", RED_LED_UUID);
                    redOn.putExtra("timeExtra", seconds);
                    startActivity(redOn);

                } catch (NumberFormatException e) {

                    Toast.makeText(MainActivity.this, "You must type a time!", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    @Override
    public void onResume() {

        super.onResume();

        time.getText().clear();

    }
}
