package brett.lednavigation;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class ColorTemperatureActivity extends AppCompatActivity {
    private TextView brightnessTextView;
    private TextView colorTempTextView;
    private SeekBar colorTempSeek;
    private SeekBar brightSeek;
    private boolean mqtt = false;
    private Button submit;
    private Button rgb;
    private int START_COLOR=0xfff9f7a8;
    private int END_COLOR=0x00000000;
    View backgroundView;
    int planckValue=127;
    int brightValue=127;
    int miredTemperature;
    int max = 6500;
    int[] rgbToColor;
    String bridgebuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_temperature);
        brightnessTextView = findViewById(R.id.brightnessTextView);
        colorTempSeek = findViewById(R.id.colorTempSeek);
        brightSeek = findViewById(R.id.brightSeek);
        rgb = findViewById(R.id.rgb);
        colorTempTextView = findViewById(R.id.colorTempTextView);
        brightSeek.setProgress(127);
        colorTempSeek.setProgress(127);
        bridgebuilder = getIntent().getStringExtra("bridgeBuilderURL");
        backgroundView = getWindow().peekDecorView();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);


        //initial ui state
        planckValue = (max-(127*13));
        rgbToColor= colorTempToRGB();
        updateUI(rgbToColor);


        colorTempSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                planckValue = (int) Math.round(max - (progress*12.9));
                miredTemperature = progress+153; //this is the format the hue API requires;
                rgbToColor = colorTempToRGB();
                updateUI(rgbToColor);



            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               setBrightnessAndTemperature();
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
                updateUI(rgbToColor);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setBrightnessAndTemperature();


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
    private void updateUI(int [] rgb) {


        int colorInt = Color.rgb(rgb[0],rgb[1],rgb[2]);
        brightnessTextView.setTextColor(colorInt);
        colorTempTextView.setTextColor(colorInt);
        brightnessTextView.setText(" Brightness: " + brightValue);
        colorTempTextView.setText("Temperature: " + planckValue+ "K");
        // String hexColor = String.format( "%02x%02x%02x%02x",255, color[0], color[1], color[2]).toUpperCase(); Useful Keep
        // int decimalColor = (int) Long.parseLong(hexColor, 16); Useful keep
        int[] gradientColor = new int[9];
        // Sets the parameters for the Gradient, could spend forever tweaking these */
        gradientColor[0] = START_COLOR;
        gradientColor[1] = colorInt;
        gradientColor[2] = colorInt;
        gradientColor[3] = colorInt;
        gradientColor[4] = START_COLOR;
        gradientColor[5] = colorInt;
        gradientColor[6] = END_COLOR;
        gradientColor[7] = END_COLOR;
        gradientColor[8] = END_COLOR;
        GradientDrawable background;
        background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, rgb);
        background.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        // adjusts the radius of the animation according to how bright it is
        background.setGradientRadius(returnGradientRadius(brightValue));
        background.setGradientCenter(1, 0);
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColors(gradientColor);
        backgroundView.setBackground(background);
    }

    private int returnGradientRadius(int brightValue) {
        int radius = 400;
        int increment = brightValue * 6;
        return (radius + increment);


    }
    }



