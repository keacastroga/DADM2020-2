package co.edu.unal.tictactoe;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

public class BoardView extends View {

    public static final int GRID_WIDTH = 10;
    private Paint mPaint;

    private Bitmap mHumanBitmap;
    private Bitmap mComputerBitmap;

    private TicTacToeGame mGame;

    private SharedPreferences mPrefs;

    public void setGame(TicTacToeGame game){
        mGame = game;
    }

    public BoardView(Context context){
        super(context);
        initialize();
    }
    public BoardView(Context context, AttributeSet attrs){
        super(context, attrs);
        initialize();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        initialize();
    }

    public void initialize(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHumanBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.x_img);
        mComputerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.o_img);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public int getBoardCellWidth(){
        return getWidth() / 3;
    }

    public int getBoardCellHeight(){
        return getHeight() / 3;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int boardWidth = getWidth();
        int boardHeight = getHeight();

        String colorPref = mPrefs.getString("board_color", "#FF000000");
        mPaint.setColor(Color.parseColor(colorPref));
        mPaint.setStrokeWidth((GRID_WIDTH));

        int cellWidth = boardWidth / 3;
        canvas.drawLine(cellWidth, 0, cellWidth, boardHeight, mPaint);
        canvas.drawLine(cellWidth * 2, 0, cellWidth * 2, boardHeight, mPaint);

        int cellHeight = boardHeight / 3;
        canvas.drawLine(0, cellHeight, boardWidth, cellHeight, mPaint);
        canvas.drawLine(0, cellHeight * 2, boardWidth, cellHeight * 2, mPaint);

        for (int i = 0; i < TicTacToeGame.BOARD_SIZE; i++){
            int col = i % 3;
            int row = i / 3;

            int left = cellWidth * col;
            int top = cellHeight * row;
            int right = left + cellWidth;
            int bottom = top + cellHeight;

            if (mGame != null && mGame.getBoardOccupant(i) == TicTacToeGame.HUMAN_PLAYER){
                canvas.drawBitmap(mHumanBitmap,
                        null,
                        new Rect(left+10,top+10,right-10,bottom-10),
                        null);
            }
            else if (mGame != null && mGame.getBoardOccupant(i) == TicTacToeGame.COMPUTER_PLAYER){
                canvas.drawBitmap(mComputerBitmap,
                        null,
                        new Rect(left+10,top+10,right-10,bottom-10),
                        null);
            }

        }

    }
}
