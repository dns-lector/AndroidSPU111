package itstep.learning.spu111;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int N = 4;
    private final Random random = new Random();

    private int[][] cells = new int[N][N];
    private TextView[][] tvCells = new TextView[N][N];
    private Animation fadeInAnimation;
    private TextView tvScore;
    private long score;

    @SuppressLint({"DiscouragedApi", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Забезпечення "квадратності" поля - однаковості ширини та висоти
        LinearLayout gameField = findViewById( R.id.game_field );
        tvScore = findViewById( R.id.game_tv_score );
        gameField.post( () -> {
            // onCreate - подія, коли ще активність не "зібрана" і її розміри невідомі.
            // для того щоб дізнатись розміри необхідно створювати відкладені дії
            // З цією метою представлення мають метод post, який створює чергу дій,
            // які виконуються коли представлення стає "вмонтованим"

            // визначаємо актуальні розміри
            int w = this.getWindow().getDecorView().getWidth();  // gameField.width
            int p =  2 * 3;
            // замінюємо параметри на "квадратні"
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(  //
                    w - p,   // квадрат - однакова ширина
                    w - p    // та висота
            );
            layoutParams.setMargins( 5, 10, 5, 10);
            layoutParams.gravity = Gravity.CENTER;
            gameField.setLayoutParams( layoutParams );
        });
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                tvCells[i][j] = findViewById(
                        getResources()   // R
                                .getIdentifier(
                                        ("game_cell_" + i) + j,
                                        "id",  // .id
                                        getPackageName()
                                )

                );
            }
        }
        int val = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                cells[i][j] = val;
                // if(val == 0) val = 2;
                // else val *= 2;
            }
        }
        fadeInAnimation = AnimationUtils.loadAnimation( this, R.anim.fade_in );
        fadeInAnimation.reset();
        gameField.setOnTouchListener(
                new SwipeTouchListener(this) {
                    @Override
                    public void onSwipeBottom() {
                        spawnCell();
                        Toast.makeText(GameActivity.this, "onSwipeBottom", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSwipeLeft() {
                        if( moveLeft() ) {
                            spawnCell();
                        }
                        else {
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        if( moveRight() ) {
                            spawnCell();
                        }
                        else {
                            Toast.makeText(GameActivity.this, "Немає ходу", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeTop() {
                        spawnCell();
                        Toast.makeText(GameActivity.this, "onSwipeTop", Toast.LENGTH_SHORT).show();
                    }
                });

        startGame();
    }

    private void startGame() {
        addScore( -score );
        spawnCell();
    }

    private void addScore( long value ) {
        score += value;
        tvScore.setText(
                getString(                          // заповнення ресурсу-шаблону
                        R.string.game_score_tpl,    // ресурс
                        score                       // дані для підставки (за кількістю % у шаблоні)
                )
        );
    }

    @SuppressLint("DiscouragedApi")
    private void showField() {
        for( int i = 0; i < N; i++ ) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) );
                int val = cells[i][j];
                if(val > 4096) val = 4096;
                tvCells[i][j].setTextAppearance(
                        getResources().getIdentifier(
                                "game_cell_" + val,
                                "style",
                                getPackageName()
                        )
                );
                tvCells[i][j].setBackgroundColor(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        "game_digit_" + val,
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        )
                );
            }
        }
    }

    private boolean moveLeft() {
        // [0 2 0 2] -> [4 0 0 0]
        // [2 0 2 2] -> [4 2 0 0]
        // [2 2 2 2] -> [4 4 0 0]
        // [0 2 0 4] -> [2 4 0 0]
        // [0 0 0 0] -> [0 0 0 0] рух не зараховується
        // [0 0 0 4] -> [4 0 0 0]
        // [2 4 2 4] -> [2 4 2 4] рух не зараховується
        // [4 2 2 0]
        boolean wasMove = false;
        for( int i = 0; i < N; i++ ) {  // цикл по рядках
            // перевіряємо тільки з'єднання
            int pos1 = -1;   // позиція попереднього числа
            for( int j = 0; j < N; j++ ) {
                if( cells[i][j] != 0 ) {
                    if( pos1 != -1 ) {  // раніше було число лівіше
                        if( cells[i][pos1] == cells[i][j] ) {  // з'єднання
                            cells[i][pos1] += cells[i][j];
                            cells[i][j] = 0;
                            addScore( cells[i][pos1] );
                            pos1 = -1;  // число злите, шукаємо нове
                            wasMove = true;
                        }
                        else {  // нове число - замінюємо індекс
                            pos1 = j;
                        }
                    }
                    else {  // перше знайдене число
                        pos1 = j;
                    }
                }
            }

            // пересування
            pos1 = -1;  // позиція нуля
            for( int j = 0; j < N; j++ ) {
                if( cells[i][j] == 0 ) {
                    if( pos1 == -1 ) pos1 = j;
                }
                else {
                    if( pos1 != -1 ) {
                        cells[i][pos1] = cells[i][j];
                        cells[i][j] = 0;
                        pos1++;
                        wasMove = true;
                    }
                }
            }
        }
        return wasMove;
    }

    private boolean moveRight() {
        boolean wasMove = false;
        for( int i = 0; i < N; i++ ) {  // цикл по рядках
            // перевіряємо тільки з'єднання
            int pos1 = -1;   // позиція попереднього числа
            for( int j = N - 1; j >= 0; j-- ) {
                if( cells[i][j] != 0 ) {
                    if( pos1 != -1 ) {  // раніше було число лівіше
                        if( cells[i][pos1] == cells[i][j] ) {  // з'єднання
                            cells[i][pos1] += cells[i][j];
                            cells[i][j] = 0;
                            addScore( cells[i][pos1] );
                            pos1 = -1;  // число злите, шукаємо нове
                            wasMove = true;
                        }
                        else {  // нове число - замінюємо індекс
                            pos1 = j;
                        }
                    }
                    else {  // перше знайдене число
                        pos1 = j;
                    }
                }
            }

            // пересування
            pos1 = -1;  // позиція нуля
            for( int j = N - 1; j >= 0; j-- ) {
                if( cells[i][j] == 0 ) {
                    if( pos1 == -1 ) pos1 = j;
                }
                else {
                    if( pos1 != -1 ) {
                        cells[i][pos1] = cells[i][j];
                        cells[i][j] = 0;
                        pos1--;
                        wasMove = true;
                    }
                }
            }
        }
        return wasMove;
    }


    /**
     * Поява нового числа
     * з імовірністю 0.9 - 2, 0.1 - 4
     */
    private boolean spawnCell() {
        // збираємо координати всіх "порожніх" комірок (зі значенням 0)
        List<Coord> coords = new ArrayList<>();
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                if( cells[i][j] == 0 ) {
                    coords.add( new Coord(i, j) );
                }
            }
        }
        if( coords.isEmpty() ) {  // немає можливості додати число - поле заповнене
            return false;
        }
        // вибираємо випадково одну з них
        int randomIndex = random.nextInt( coords.size() );
        Coord randomCoord = coords.get(randomIndex);

        // задаємо їх значення та анімацію появи
        cells[randomCoord.i][randomCoord.j] = random.nextInt( 10 ) == 0 ? 4 : 2;
        tvCells[randomCoord.i][randomCoord.j].startAnimation( fadeInAnimation );

        // перерисовуємо поле
        showField();
        return true;
    }

    static class Coord {
        int i;
        int j;

        public Coord(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }
}
/*
Д.З.
- Додати анімацію з'єднання комірок на базі scale
    (звернути увагу, що після злиття можливе переміщення - анімацію
    додавати після цього)
- Реалізувати рухи вгору та вниз
- Робота кнопки NEW (нова гра)
 */
