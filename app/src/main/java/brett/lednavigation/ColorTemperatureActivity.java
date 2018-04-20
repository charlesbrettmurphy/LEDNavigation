package brett.lednavigation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class ColorTemperatureActivity extends AppCompatActivity {
    private TextView colorTemp;
    private SeekBar colorTempSeek;
    private SeekBar brightSeek;
    private boolean mqtt = false;
    private Button submit;
    private Button rgb;

    private String temperature = "2000";
    int planckValue=127;
    int brightValue=127;
    int miredTemperature;
    int max = 6535;
    int[] rgbToColor;
    String bridgebuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_temperature);
        colorTemp = findViewById(R.id.colorTemp);
        colorTempSeek = findViewById(R.id.colorTempSeek);
        brightSeek = findViewById(R.id.brightSeek);
        submit = findViewById(R.id.submit);
        rgb = findViewById(R.id.rgb);
        brightSeek.setProgress(127);
        colorTempSeek.setProgress(127);
        bridgebuilder = getIntent().getStringExtra("bridgeBuilderURL");


        colorTempSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                planckValue = (max - (progress*13));
                miredTemperature = progress+153; //this is the format the hue API requires;
                rgbToColor = colorTempToRGB();
                colorTemp.setTextColor(Color.rgb(rgbToColor[0],rgbToColor[1],rgbToColor[2]));

                colorTemp.setText("Color Temp " + Integer.toString(planckValue) + "K" + " Brightness: " + brightValue);

            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               setBrightnessAndTemperature();
                colorTemp.setTextColor(Color.rgb(rgbToColor[0],rgbToColor[1],rgbToColor[2]));
                colorTemp.setText("Color Temp " + Integer.toString(planckValue) + "K" + " Brightness: " + brightValue);

            }
        });

        rgb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        brightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightValue = i;
                colorTemp.setText("Color Temp " + Integer.toString(planckValue) + "K" + " Brightness: " + brightValue);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setBrightnessAndTemperature();


            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                planckValue = max - (colorTempSeek.getProgress()*13);
                colorTemp.setText("Color Set: " + Integer.toString(planckValue) + "K" + " Brightness: " + brightValue);
                Log.i("BridgebuilderURL ", bridgebuilder);
                BridgeCall bridgeCall = new BridgeCall();
                BuildJSON buildJSON = new BuildJSON();

                String json = buildJSON.setColorTemperatureAndBrightness(miredTemperature, brightValue).toString();
                bridgeCall.execute(bridgebuilder, "PUT", json);
                }
        });



    }

    public void setBrightnessAndTemperature() {
        BridgeCall bridgeCall = new BridgeCall();
        BuildJSON buildJSON = new BuildJSON();
        bridgebuilder.concat("/state");
        String json = buildJSON.setColorTemperatureAndBrightness(miredTemperature, brightValue).toString();
        bridgeCall.execute(bridgebuilder, "PUT", json);
        }
/*an algorithm for extrapolating color temperatures to RGB for temperatures between 6600K
to 1900k. The reds can be tinkered with 210-250 gives reasonable reproduction for my S7 edge*/
    public int[] colorTempToRGB(){
       double temp =  planckValue/100;
       int[]rgb = new int[3];
       double red, green, blue;
           red= 230;
           green= temp;
           green = 99.4708025861* Math.log(green) - 161.1195681661;
           blue = temp-10;
           blue = 138.5177312231 * Math.log(blue) - 305.0447927307;
           green = clamp(green);
           blue = clamp(blue);
           rgb[0]= (int)(red);
           rgb[1]= (int)(green);
           rgb[2]=(int)(blue);
           return rgb;
    }
    public double clamp(double x){
        if(x<0){return 0;}
        if(x>255){return 255;}
        return x;

    }
    }



