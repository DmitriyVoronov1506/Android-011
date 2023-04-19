package step.learning.course;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private final int N = 4 ;
    private final int[][] cells = new int[N][N];  // значения в ячейках поля

    private final int[][] saves = new int[N][N]; // предыдущий ход
    private final TextView[][] tvCells = new TextView[N][N];   // ссылки на ячеки поля
    private final Random random = new Random();

    private final String BEST_SCORE_FILENAME = "best_score.txt";

    private int score;
    private int bestScore;
    private int saveScore;
    private boolean continuePlaying;
    private TextView tvScore;
    private TextView tvBestScore;

    private Animation spawnAnimation;
    private Animation collapseAnimation;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvScore = findViewById(R.id.game_tv_score);
        tvBestScore = findViewById(R.id.game_tv_best_score);
        tvScore.setText(getString(R.string.game_score, "69.6k"));
        tvBestScore.setText(getString(R.string.game_best_score, "69.6k"));

        spawnAnimation = AnimationUtils.loadAnimation(this, R.anim.cell_spawn );
        spawnAnimation.reset();

        collapseAnimation = AnimationUtils.loadAnimation(this, R.anim.cell_collapse );
        collapseAnimation.reset();

        for(int i = 0; i < N; ++i) {
            for(int j = 0; j < N; ++j) {
                tvCells[i][j] = findViewById(     // R.id.game_cell_12
                        getResources().getIdentifier(
                                "game_cell_" + i + j,
                                "id",
                                getPackageName()
                        )
                ) ;
            }
        }

        findViewById( R.id.game_field ).setOnTouchListener(
                new OnSwipeTouchListener(GameActivity.this){
                    @Override
                    public void onSwipeRight() {

                        //saveField() ;  // [0204] -> [0024] -> no move -> [undo]
                        if(canMoveRight(cells)) {

                            saveField();
                            moveRight();
                            spawnCell(2);
                            showField();
                        }
                        else {
                            Toast.makeText(
                                            GameActivity.this,
                                            "No Right Move",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onSwipeLeft() {

                        if (canMoveLeft(cells)) {

                            saveField();
                            moveLeft();
                            spawnCell(1);
                            showField();
                        }
                        else {
                            Toast.makeText(
                                            GameActivity.this,
                                            "No Left Move",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onSwipeTop() {

                        if (canMoveTop(cells)) {

                            saveField();
                            moveTop();
                            spawnCell(1);
                            showField();
                        }
                        else {
                            Toast.makeText(
                                            GameActivity.this,
                                            "No Top Move",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onSwipeBottom() {

                        if (canMoveBottom(cells)) {

                            saveField();
                            moveBottom();
                            spawnCell(1);
                            showField();
                        }
                        else {
                            Toast.makeText(
                                            GameActivity.this,
                                            "No Bottom Move",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
        );

        findViewById(R.id.game_new).setOnClickListener(this::newGame);
        findViewById(R.id.game_undo).setOnClickListener(this::undoMoveClick);
        newGame(null) ;
    }

    private void newGame(View view) {

        for(int i = 0; i < N; ++i) {

            for(int j = 0; j < N; ++j) {

                cells[i][j] = 0;
            }
        }

        continuePlaying = false;

        score = 0 ;

        loadBestScore() ;
        tvBestScore.setText(getString(R.string.game_best_score, String.valueOf(bestScore)));

        spawnCell(2);
        saveField();
        showField();
    }

    private boolean canMoveRight(int[][] cells) {

        boolean canMove = false;

        for (int[] r : cells) {

            for (int i = r.length - 2; i >= 0; i--) {

                if (r[i] != 0) {

                    if (r[i+1] == 0 || r[i+1] == r[i]) {

                        canMove= true;
                        break;
                    }
                }
            }
            if (canMove) {
                break;
            }
        }
        return canMove;
    }

    public boolean canMoveLeft(int[][] cells) {

        boolean canMove = false;

        for (int[] r : cells) {

            for (int i = 1; i < r.length; i++) {

                if (r[i] != 0) {

                    if (r[i-1] == 0 || r[i-1] == r[i]) {

                        canMove = true;
                        break;
                    }
                }
            }
            if (canMove) {
                break;
            }
        }
        return canMove;
    }

    public boolean canMoveTop(int[][] cells) {

        boolean canMove = false;

        for (int i = 0; i < cells[0].length; i++) {

            for (int r = 1; r < cells.length; r++) {

                if (cells[r][i] != 0) {

                    if (cells[r-1][i] == 0 || cells[r-1][i] == cells[r][i]) {

                        canMove = true;
                        break;
                    }
                }
            }
            if (canMove) {
                break;
            }
        }
        return canMove;
    }
    public boolean canMoveBottom(int[][] cells) {

        boolean canMove = false;

        for (int i = 0; i < cells[0].length; i++) {

            for (int r = cells.length - 2; r >= 0; r--) {

                if (cells[r][i] != 0) {

                    if (cells[r+1][i] == 0 || cells[r+1][i] == cells[r][i]) {

                        canMove = true;
                        break;
                    }
                }
            }
            if (canMove) {
                break;
            }
        }
        return canMove;
    }

    @SuppressLint("DiscouragedApi")
    private void showField() {

        Resources resources = getResources();
        String packageName = getPackageName();

        for(int i = 0; i < N; ++i) {

            for(int j = 0; j < N; ++j) {

                tvCells[i][j].setText( String.valueOf(cells[i][j]));

                tvCells[i][j].setTextAppearance(    // R.style.GameCell_16
                        resources.getIdentifier(
                                "GameCell_" + cells[i][j],
                                "style",
                                packageName
                        )
                );

                // setTextAppearance не "подтягивает" фоновый цвет
                tvCells[i][j].setBackgroundColor(
                        resources.getColor(
                                resources.getIdentifier(
                                        "game_bg_" + cells[i][j],
                                        "color",
                                        packageName
                                ),
                                getTheme()
                        )
                );
            }
        }

        tvScore.setText(getString(R.string.game_score, String.valueOf(score)));

        if( score > bestScore ) {

            bestScore = score;
            saveBestScore();
            tvBestScore.setText(getString(R.string.game_best_score, String.valueOf(bestScore)));
        }

        if( score >= 8 && ! continuePlaying ) {  // !!
            showWinMessage() ;
        }
    }

    private void showWinMessage() {
        new AlertDialog.Builder( this, com.google.android.material.R.style.Base_Theme_Material3_Dark_Dialog ) // com.google.android.material.R.style.Base_Theme_Material3_Dark )
                .setTitle( R.string.game_win_dialog_title )
                .setMessage( R.string.game_win_dialog_message )
                .setIcon( android.R.drawable.btn_star )
                .setCancelable( false )
                .setPositiveButton( R.string.game_yes_dialog_button, ( dialog, button ) -> continuePlaying = true )
                .setNegativeButton( R.string.game_exit_dialog_button,  ( dialog, button ) -> finish() )
                .setNeutralButton( R.string.game_new_dialog_button, ( dialog, button ) -> newGame( null ) )
                .show() ;
    }

    private boolean spawnCell(int number) {

        // собираем данные о пустых ячейках
        List<Coord> coordinates = new ArrayList<>();

        for(int i = 0; i < N; ++i) {

            for(int j = 0; j < N; ++j) {

                if(cells[i][j] == 0) {
                    coordinates.add(new Coord(i, j));
                }
            }
        }
        // проверяем есть ли пустые ячейки
        int cnt = coordinates.size();
        if(cnt < number) return false;

        for (int i = 0; i < number; ++i) {

            int randIndex = random.nextInt(cnt);

            int x = coordinates.get(randIndex).getX();
            int y = coordinates.get(randIndex).getY();

            cells[x][y] = random.nextInt(10) == 0 ? 4 : 2;
            tvCells[x][y].startAnimation(spawnAnimation);
        }
        return true ;
    }

    private boolean moveRight() {

        boolean isMoved = false;    // [0002]->[0002], [0200]->[0002], [2020]->[0022]->[0004]

        for(int i = 0; i < N; ++i) {

            boolean wasReplace;

            do {
                wasReplace = false;
                for (int j = N - 1; j > 0; --j) {
                    if (cells[i][j] == 0          // текущая ячейка 0
                            && cells[i][j - 1] != 0) {
                        // а перед ней - не 0
                        cells[i][j] = cells[i][j - 1];
                        cells[i][j - 1] = 0;

                        wasReplace = true;
                        isMoved = true;
                    }
                }
            } while (wasReplace);

            // collapse
            for (int j = N - 1; j > 0; --j) {
                // [2202] -> [0222] -> [0204] -> [0024]
                if (cells[i][j] == cells[i][j - 1] && cells[i][j] != 0) {
                    // соседние ячейки равны  [2222]
                    score += cells[i][j] + cells[i][j - 1] ;   // счет = сумма всех объединенных ячеек
                    cells[i][j] *= -2 ;  // [2224]; "-" - признак для анимации
                    cells[i][j - 1] = 0 ;   // [2204]

                    isMoved = true ;
                }
            }  // [0404]  при коллапсе может понадобиться дополнительное смещение
            for (int j = N - 1; j > 0; --j) {

                if (cells[i][j] == 0 && cells[i][j - 1] != 0) {

                    cells[i][j] = cells[i][j - 1];
                    cells[i][j - 1] = 0;
                }
            }
            for (int j = N - 1; j > 0; --j) {

                if( cells[i][j] < 0 ) {  // надо включить анимацию

                    cells[i][j] = -cells[i][j] ;
                    tvCells[i][j].startAnimation( collapseAnimation ) ;
                }
            } // [0044]
        }
        return isMoved ;
    }

    private boolean moveLeft() {

        boolean isMoved = false ;

        for( int i = 0; i < N; ++i ) {

            boolean wasReplace;

            do {
                wasReplace = false;

                for (int j = 0; j < N - 1; ++j) {

                    if (cells[i][j] == 0 && cells[i][j + 1] != 0) {

                        cells[i][j] = cells[i][j + 1];
                        cells[i][j + 1] = 0;

                        wasReplace = true;
                        isMoved = true;
                    }
                }
            } while (wasReplace);


            for (int j = 0; j < N - 1; ++j) {

                if (cells[i][j] == cells[i][j + 1] && cells[i][j] != 0) {

                    score += cells[i][j] + cells[i][j + 1];
                    cells[i][j] *= -2;
                    cells[i][j + 1] = 0;

                    isMoved = true;
                }
            }

            for (int j = 0; j < N - 1; ++j) {

                if (cells[i][j] == 0 && cells[i][j + 1] != 0) {

                    cells[i][j] = cells[i][j + 1];
                    cells[i][j + 1] = 0;
                }
            }

            for (int j = 0; j < N - 1; ++j) {

                if (cells[i][j] < 0) {

                    cells[i][j] = -cells[i][j];
                    tvCells[i][j].startAnimation(collapseAnimation);
                }
            }
        }
        return isMoved;
    }
    private boolean moveTop() {

        boolean isMoved = false;

        for (int j = 0; j < N; ++j) {

            boolean wasReplace;

            do {

                wasReplace = false;

                for (int i = 0; i < N - 1; ++i) {

                    if (cells[i][j] == 0 && cells[i + 1][j] != 0) {

                        cells[i][j] = cells[i + 1][j];
                        cells[i + 1][j] = 0;

                        wasReplace = true;
                        isMoved = true;
                    }
                }
            } while (wasReplace);

            for (int i = 0; i < N - 1; ++i) {

                if (cells[i][j] == cells[i + 1][j] && cells[i][j] != 0) {

                    score += cells[i][j] + cells[i + 1][j];
                    cells[i][j] *= -2;
                    cells[i + 1][j] = 0;

                    isMoved = true;
                }
            }

            for (int i = 0; i < N - 1; ++i) {

                if (cells[i][j] == 0 && cells[i + 1][j] != 0) {

                    cells[i][j] = cells[i + 1][j];
                    cells[i + 1][j] = 0;
                }
            }

            for (int i = 0; i < N - 1; ++i) {

                if (cells[i][j] < 0) {

                    cells[i][j] = -cells[i][j];
                    tvCells[i][j].startAnimation(collapseAnimation);
                }
            }
        }
        return isMoved;
    }
    private boolean moveBottom() {

        boolean isMoved = false;

        for (int j = 0; j < N; ++j) {

            boolean wasReplace;

            do {

                wasReplace = false;

                for (int i = N - 1; i > 0; --i) {

                    if (cells[i][j] == 0 && cells[i - 1][j] != 0) {

                        cells[i][j] = cells[i - 1][j];
                        cells[i - 1][j] = 0;

                        wasReplace = true;
                        isMoved = true;
                    }
                }
            } while (wasReplace);


            for (int i = N - 1; i > 0; --i) {

                if (cells[i][j] == cells[i - 1][j] && cells[i][j] != 0) {

                    score += cells[i][j] + cells[i - 1][j];
                    cells[i][j] *= -2;
                    cells[i - 1][j] = 0;

                    isMoved = true;
                }
            }

            for (int i = N - 1; i > 0; --i) {

                if (cells[i][j] == 0 && cells[i - 1][j] != 0) {

                    cells[i][j] = cells[i - 1][j];
                    cells[i - 1][j] = 0;
                }
            }

            for (int i = N - 1; i > 0; --i) {

                if (cells[i][j] < 0) {

                    cells[i][j] = -cells[i][j];
                    tvCells[i][j].startAnimation(collapseAnimation);
                }
            }
        }
        return isMoved;
    }
    private void saveField() {

        for (int i = 0; i < N; i++) {
            System.arraycopy(cells[i], 0, saves[i], 0, N);
        }
    }
    private void undoMove() {
        for (int i = 0; i < N; i++) {
            System.arraycopy(saves[i], 0, cells[i], 0, N);
        }
        score = saveScore ;
    }
    private void undoMoveClick( View view ) {
        undoMove() ;
        showField() ;
    }

    private void saveBestScore() {

        try(FileOutputStream fileStream = openFileOutput(BEST_SCORE_FILENAME, Context.MODE_PRIVATE);

            DataOutputStream writer = new DataOutputStream(fileStream)) {
                writer.writeInt(bestScore);
                writer.flush();
            }

        catch( IOException ex ) {
            Log.d("saveBestScore", ex.getMessage());
        }
    }
    private void loadBestScore() {
        try( FileInputStream fileInputStream = openFileInput( BEST_SCORE_FILENAME );
             DataInputStream reader = new DataInputStream( fileInputStream ) ) {
            bestScore = reader.readInt() ;
        }
        catch( IOException ex ) {
            Log.d( "loadBestScore", ex.getMessage() ) ;
            bestScore = 0 ;
        }
    }


    private static class Coord {
        private final int x ;
        private final int y ;

        public Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}

/*
Анимации
1. Создаем ресурсную папку res/anim (ПКМ - папка - anim)
2. Создаем ресурсный файл cell_spawn.xml, корневой элемент alpha (см файл)
3. Создаем переменную типа Animation spawnAnimation
4. Загружаем анимацию
    spawnAnimation = AnimationUtils.loadAnimation( this, R.anim.cell_spawn ) ;
    spawnAnimation.reset() ;
5. Проигрываем анимацию (на любом View)
    tvCells[x][y].startAnimation( spawnAnimation ) ;
Виды анимаций
alpha - прозрачность
rotate - вращение
translate - перемещение
scale - размер(масштаб)
 */