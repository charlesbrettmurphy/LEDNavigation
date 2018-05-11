package brett.lednavigation;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by brett on 3/27/2018.
 * This class is responsible for all calls made by the application to the hue bridge.
 * 1. the constructor takes the calling activity context so that the information gets passed through the appropriate interface.
 * This class takes up to 3 variable length arguments passed from .execute()
 * 1. the URL being accessed,
 * 2. the type of http call (PUT POST GET DELETE),
 * 3. and the json if its PUT or POST
 */
public class BridgeCall extends AsyncTask<String, Void, String> {

    private HttpURLConnection urlConnection;

    private onBridgeResponseListener listener;

    public interface onBridgeResponseListener {
        void onBridgeResponse(String json);
    }

    public BridgeCall(onBridgeResponseListener listener) {
        this.listener = listener;
    }

    //TODO: Finish implementing interfaces for all bridge calls so empty constructor can be removed.
    public BridgeCall() {
    }


    @Override
    protected String doInBackground(String... params) {
        URL url;
        InputStream in;
        String response;
        int FAST = 1000;
        int LONG = 5000;
        String json = "";
        String httpCallType = params[1];
        Log.d("Method", httpCallType);
        if (params.length == 3) { // check if a json was passed or not, because its possible to call with only a url and httpCallType
            json = params[2]; //
        }
        try {

            url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            if (httpCallType.equals("GET")) {
                urlConnection.setDoOutput(false);
            } else {
                urlConnection.setDoOutput(true); //if we are sending a json
            }
            urlConnection.setDoInput(true);
            if (url.toString().contains("internetservices")) //setting fast timeout for quickTestGateway Connection
                urlConnection.setConnectTimeout(FAST);
            else
                urlConnection.setConnectTimeout(LONG);  //Otherwise normal timeout
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod(httpCallType);
            if (httpCallType.equals("GET")) {
                urlConnection.connect();
            }
            if (params.length == 3) { //only need an output stream if method is PUT or POST
                urlConnection.setDoOutput(true);
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                Log.d("URL in BridgeCall:", url.toString());
                Log.d("JSON in BridgeCall:", json);
                writer.write(json);
                writer.close();
                outputStream.close();
            }
            in = new BufferedInputStream(urlConnection.getInputStream());
            String encoding = urlConnection.getContentEncoding();
            response = IOUtils.toString(in, encoding);
            Log.d("Http Response", response);
        } catch (UnsupportedEncodingException e) {
            Log.d("EncodingNotSupported", e.toString());
            response = "Encoding Not Supported-OutputStreamwriter UTF-8 " + e.toString();
        } catch (MalformedURLException e) {
            Log.d("MalformedURLException", e.toString());
            response = "MalformedURLException in URL " + e.toString();
        } catch (IllegalStateException e) {
            Log.d("IllegalStateException", e.toString());
            response = "IllegalStateException in urlConnection Object " + e.toString();

        } catch (NullPointerException e) {
            Log.d("NullPointerException", e.toString());
            response = "NullPointerException in urlConnection Object " + e.toString();

        } catch (ProtocolException e) {
            Log.d("ProtcolException", e.toString());
            response = "ProtocolException Protocol not supported " + e.toString();
        } catch (IOException e) {
            Log.d("IOException", e.toString());
            response = "IOException in write function " + e.toString();

        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return response;
    }

    protected void onPostExecute(String json) {
        if (listener != null) {
            listener.onBridgeResponse(json);
        }

    }

}


