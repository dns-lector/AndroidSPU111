package itstep.learning.spu111;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class RatesActivity extends AppCompatActivity {
    private final static String nbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private LinearLayout ratesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rates);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ratesContainer = findViewById( R.id.rates_ll_container );
        ratesContainer.post( () -> new Thread( this::loadRates ).start() );
    }

    private void loadRates() {
        try {
            URL url = new URL( nbuUrl );   // аналогічно до File об'єкт не робить нічого, окрім створення
            InputStream urlStream = url.openStream();
            String jsonString  = readAsString( urlStream );
            runOnUiThread( () -> showRates( jsonString ) );
            urlStream.close();
        }
        catch( MalformedURLException ex ) {
            Log.d( "loadRates", "MalformedURLException: " + ex.getMessage() );
        }
        catch( IOException ex ) {
            Log.d( "loadRates", "IOException: " + ex.getMessage() );
        }
        catch( android.os.NetworkOnMainThreadException ex ) {
            Log.d( "loadRates", "NetworkOnMainThreadException: " + ex.getMessage() );
        }
        catch( SecurityException ex ) {
            Log.d( "loadRates", "SecurityException: " + ex.getMessage() );
        }
    }

    private void showRates( String jsonString ) {
        JSONArray rates;
        try {
            rates = new JSONArray( jsonString );
            for( int i = 0; i < rates.length(); i++ ) {
                JSONObject rate = rates.getJSONObject( i );

                TextView tv = new TextView( RatesActivity.this );
                tv.setText( rate.getString( "txt" ) );
                tv.setBackground( AppCompatResources.getDrawable(
                        getApplicationContext(),
                        R.drawable.calc_btn_equal ) );
                tv.setPadding( 10, 5, 10, 5);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins( 10, 5, 10, 5 );

                tv.setLayoutParams( layoutParams );
                ratesContainer.addView( tv );

            }
        }
        catch( JSONException ex ) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private String readAsString( InputStream inputStream ) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while( ( len = inputStream.read( buffer ) ) > 0 ) {
            byteBuilder.write( buffer, 0, len );
        }
        return byteBuilder.toString();
    }
}
/*
Робота з мережею Інтернет
Задача: одержати курси валют з АРІ банку та відобразити їх
https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json

Підключення може супроводжуватись наступними проблемами:
- android.os.NetworkOnMainThreadException - в Андроїд підключення до мережі не може
    здійснюватись у тому ж потоці, в якому працює UI
- java.lang.SecurityException: Permission denied (missing INTERNET permission?)
    для доступу до Інтернет необхідно заявити в маніфесті
    <uses-permission android:name="android.permission.INTERNET"/>
- Can't toast on a thread that has not called Looper.prepare()
    запущені в іншому потоці, ніж UI, дії не можуть мати доступ до UI,
    у т.ч. Тостів. Запуск таких дій слід делегувати UI потоку

Д.З. Завершити роботу з проєктом "2048"
 */
