package brett.lednavigation;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * This Activity contains the two sliders to adjust the lights brightness and mired color temperature
 * and the necessary UI animation. It is called from  {@link LEDControllerActivity} and is passed the
 * same resource url.
 **/
//TODO: consolidate some of the calculation methods into a separate class with some of LEDController calcs
//TODO: configure getInitialLightState and clamp to closest colorTemperature on ActivityLoad
public class ColorTemperatureActivity extends AppCompatActivity {
    private TextView brightnessTextView;
    private TextView colorTempTextView;
    View backgroundView;
    int colorTempValue;
    int brightValue = 127;
    int miredTemperature;
    int max = 6500;
    int[] rgbToColor;
    String bridgeBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_temperature);
        brightnessTextView = findViewById(R.id.brightnessTextView);
        SeekBar colorTempSeek = findViewById(R.id.colorTempSeek);
        SeekBar brightSeek = findViewById(R.id.brightSeek);
        Button rgbButton = findViewById(R.id.rgb);
        colorTempTextView = findViewById(R.id.colorTempTextView);
        brightSeek.setProgress(127);
        colorTempSeek.setProgress(127);
        bridgeBuilder = getIntent().getStringExtra("bridgeBuilderURL");
        backgroundView = getWindow().peekDecorView();
        //for troubleshooting display anomalies across devices.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //initial ui state
        colorTempValue = (int) Math.round(max - (127 * 12.9));
        rgbToColor = colorTempToRGB();
        updateUI(rgbToColor);

        //SeekBar for Color Temperature
        colorTempSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                colorTempValue = (int) Math.round(max - (progress * 12.9));
                miredTemperature = progress + 153; //this is the format the hue API requires;
                rgbToColor = colorTempToRGB();
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

        rgbButton.setOnClickListener(new View.OnClickListener() {
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
        String json = buildJSON.setColorTemperatureAndBrightness(miredTemperature, brightValue).toString();
        bridgeCall.execute(bridgeBuilder, "PUT", json);
    }

    /*an algorithm for extrapolating color temperatures to RGB for temperatures between 6600K
    to 1900k. The reds can be tinkered with 210-250 gives reasonable reproduction for my S7 edge*/
    public int[] colorTempToRGB() {
        double temp = colorTempValue / 100;
        int[] rgb = new int[3];
        double red, green, blue;
        red = 230;
        green = temp;
        green = 99.4708025861 * Math.log(green) - 161.1195681661;
        blue = temp - 10;
        blue = 138.5177312231 * Math.log(blue) - 305.0447927307;
        green = clamp(green);
        blue = clamp(blue);
        rgb[0] = (int) (red);
        rgb[1] = (int) (green);
        rgb[2] = (int) (blue);
        return rgb;
    }

    public double clamp(double x) {
        if (x < 0) {
            return 0;
        }
        if (x > 255) {
            return 255;
        }
        return x;
    }

    private void updateUI(int[] rgb) {
        int START_COLOR = 0xfff9f7a8;
        int END_COLOR = 0x00000000;
        int colorInt = Color.rgb(rgb[0], rgb[1], rgb[2]);
        brightnessTextView.setTextColor(colorInt);
        colorTempTextView.setTextColor(colorInt);
        brightnessTextView.setText(" Brightness: ".concat(Integer.toString(brightValue)));
        colorTempTextView.setText("Temperature: ".concat(Integer.toString(colorTempValue) + "K"));
        int[] gradientColor = new int[9];
        // Sets the parameters for the Gradient
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



