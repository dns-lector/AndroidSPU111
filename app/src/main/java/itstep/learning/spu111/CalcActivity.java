package itstep.learning.spu111;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {
    private final int maxDigits = 11;

    private TextView tvHistory;
    private TextView tvResult;
    private String zeroSign;
    private String dotSign;
    private String minusSign;
    private boolean needClearResult;   // стирати при введенні -- після операцій

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable(this);
        setContentView( R.layout.activity_calc );
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calc_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvHistory = findViewById( R.id.calc_tv_history ) ;
        tvResult  = findViewById( R.id.calc_tv_result  ) ;
        zeroSign  = getString( R.string.calc_btn_digit_0 ) ;
        dotSign   = getString( R.string.calc_btn_dot ) ;
        minusSign = getString( R.string.calc_minus_sign ) ;
        findViewById( R.id.calc_btn_c ).setOnClickListener( this::clearClick );
        findViewById( R.id.calc_btn_dot ).setOnClickListener( this::dotClick );
        findViewById( R.id.calc_btn_sign_toggle ).setOnClickListener( this::pmClick );
        findViewById( R.id.calc_btn_backspace ).setOnClickListener( this::backspaceClick );
        findViewById( R.id.calc_btn_square ).setOnClickListener( this::squareClick );
        for (int i = 0; i < 10; i++) {
            findViewById(
                    // R.id.calc_btn_c
                    getResources()   // R
                    .getIdentifier(
                           "calc_btn_digit_" + i,
                            "id",  // .id
                            getPackageName()
                    )
            ).setOnClickListener( this::digitClick );
        }
        if( savedInstanceState == null ) {   // якщо це перший запуск (а не зміна конфігурації)
            this.clearClick(null);
            needClearResult = false;
        }
    }

    private void squareClick( View view ) {
        String res = tvResult.getText().toString();
        res = res
                .replace( zeroSign, "0" )
                .replace( minusSign, "-" )
                .replace( dotSign, "." );
        double x = Double.parseDouble( res ) ;
        needClearResult = true;
        showResult( x * x );
    }

    private void showResult( double x ) {
        if(x >= 1e11 || x <= -1e11) {
            // TODO: перенести до ресурсів
            tvResult.setText( "Переповнення" );
            return;
        }
        String res = x == (int) x
                ? String.valueOf( (int) x )
                : String.valueOf( x ) ;
        res = res
                .replace( "0", zeroSign )
                .replace( "-", minusSign )
                .replace( ".", dotSign );
        int limit = maxDigits;
        if( res.startsWith( minusSign ) ) {
            limit += 1;
        }
        if( res.contains( dotSign ) ) {
            limit += 1;
        }
        if( res.length() > limit ) {
            res = res.substring( 0, limit ) ;
        }
        tvResult.setText( res );
    }

    private void clearClick( View view ) {
        tvHistory.setText( "" );
        tvResult.setText( zeroSign );
    }

    private void digitClick( View view ) {
        String res = needClearResult ? "" : tvResult.getText().toString();
        needClearResult = false;
        if( digitLength( res ) >= maxDigits ) {
            Toast.makeText(this, R.string.calc_msg_max_digits, Toast.LENGTH_SHORT).show();
            return;
        }
        if( zeroSign.equals(res) ) {
            res = "";
        }
        res += ( (Button) view ).getText();
        tvResult.setText( res );
    }

    private void dotClick( View view ) {
        String res = tvResult.getText().toString();
        if( res.contains( dotSign ) ) {
            if( res.endsWith( dotSign ) ) {
                res = res.substring( 0, res.length() - 1 );
            }
            else {
                Toast.makeText(this, R.string.calc_msg_two_dots, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            res += dotSign;
        }
        tvResult.setText( res );
    }

    private void pmClick( View view ) {
        String res = tvResult.getText().toString();
        if( zeroSign.equals( res ) ) {
            Toast.makeText(this, R.string.calc_msg_minus_zero, Toast.LENGTH_SHORT).show();
            return;
        }
        if( res.startsWith( minusSign ) ) {
            res = res.substring( minusSign.length() );
        }
        else {
            res = minusSign + res;
        }
        tvResult.setText( res );
    }

    private void backspaceClick( View view ) {
        String res = tvResult.getText().toString();
        int len = res.length();
        if( len > 1 ) {
            res = res.substring( 0, len - 1 );
            if( minusSign.equals( res ) ) {
                res = zeroSign;
            }
        }
        else {
            res = zeroSign;
        }
        tvResult.setText( res );
    }

    private int digitLength( String input ) {
        // довжина результату у цифрах (без знаку "-" та точки)
        int ret = 0;
        ret = input.length();
        if( input.startsWith( minusSign ) ) {
            ret -= 1;
        }
        if( input.contains( dotSign ) ) {
            ret -= 1;
        }
        return ret;
    }

    /*
    При зміні конфігурації пристрою відбувається переобирання активності,
    через що здійснюється перезапуск і зникають дані, що напрацьовані.
    Для того, щоб їх зберегти, необхідно використати події життєвого циклу
    активності onSaveInstanceState та onRestoreInstanceState
     */

    @Override
    protected void onSaveInstanceState( @NonNull Bundle outState ) {
        super.onSaveInstanceState( outState );
        // outState - Map, що зберігає різні типи даних за принципом ключ-значення
        outState.putCharSequence( "savedResult", tvResult.getText() );
        outState.putBoolean( "needClearResult", needClearResult );
    }

    @Override
    protected void onRestoreInstanceState( @NonNull Bundle savedInstanceState ) {
        super.onRestoreInstanceState( savedInstanceState );
        // savedInstanceState - посилання на outState, що зберігався при onSaveInstanceState
        tvResult.setText( savedInstanceState.getCharSequence( "savedResult" ) );
        needClearResult = savedInstanceState.getBoolean( "needClearResult" );
    }

}

/*
Завершити роботу з проєктом "калькулятор"
 */