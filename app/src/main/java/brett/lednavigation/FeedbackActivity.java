package brett.lednavigation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Simple Activity to launch email client to send feedback
 **/
public class FeedbackActivity extends AppCompatActivity {

    String[] EMAIL_ADDRESSES = {"brett@sowilodesign.com"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        final EditText contactEmailEditText = findViewById(R.id.contactEmailEditText);
        final Spinner subjectSpinner = findViewById(R.id.subjectSpinner);
        final EditText messageBodyEditText = findViewById(R.id.messageBodyEditText);
        Button sendFeedbackButton = findViewById(R.id.sendFeedBackButton);
        String[] subjectStrings = {"About Sowilo LED Strips", "About Sowilo Android App", "About Sowilo Website", "Other"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, subjectStrings);
        subjectAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);

        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, EMAIL_ADDRESSES);
                emailIntent.putExtra(Intent.EXTRA_CC, contactEmailEditText.getText());
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjectSpinner.getSelectedItem().toString());
                emailIntent.putExtra(Intent.EXTRA_TEXT, messageBodyEditText.getText());
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    finish();
                    Log.i("Email Send", "Finished");
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(FeedbackActivity.this, "No email client found", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(FeedbackActivity.this, "Pick your email client", Toast.LENGTH_LONG).show();
            }
        });

    }
}

