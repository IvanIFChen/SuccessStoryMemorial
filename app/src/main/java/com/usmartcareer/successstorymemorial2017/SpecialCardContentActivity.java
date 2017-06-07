package com.usmartcareer.successstorymemorial2017;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.usmartcareer.successstorymemorial2017.model.User;
import com.usmartcareer.successstorymemorial2017.sqlite.DBHelper;

import java.util.ArrayList;

import static com.usmartcareer.successstorymemorial2017.sqlite.DBConstants.TABLE_NAME_USER_LIST;
import static com.usmartcareer.successstorymemorial2017.sqlite.DBConstants.USER_SIZE;
import static com.usmartcareer.successstorymemorial2017.SSCardActivity.MAX_LINE_COUNT;

/**
 * Created by Tunabutter on 6/2/2016.
 */
public class SpecialCardContentActivity extends AppCompatActivity implements View.OnClickListener {
    public static int mUserNumber;
    private static User mUser;
    private static ArrayList<User> userList = new ArrayList<User>();
    private DBHelper dbHelper;
    private static CustomTracker tracker = new CustomTracker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_card_content);

        tracker.initialize(this, this, "SSCard_SpecialContent");

        tracker.sendAnalyticScreen("Special_Card_Story");

        // check if userList is already initialized
        if (USER_SIZE == 0 || userList.size() == 0) {
            // fetch data from db and store to an array list.
            dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selectQuery =  "SELECT * FROM " + TABLE_NAME_USER_LIST;
            Cursor cursor = db.rawQuery(selectQuery, null);
            USER_SIZE = cursor.getCount();

            for (int i = 0; i < USER_SIZE; i++) {
                User user = dbHelper.getUser(i);
                userList.add(user);
                Log.d("=====story list adding:", user.getName());
            }
            db.close();
        }

        // find based on mUserNumber and initialize mUser.
        for (int i = 0; i < userList.size(); i++) {
            User u = userList.get(i);
            if (u.getNumber() == this.mUserNumber) {
                this.mUser = u;
                break;
            }
        }

        // Hides action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // touch listener
        findViewById(android.R.id.content).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return handleTouch(motionEvent);
                    }
                }
        );

        // on click listeners
        findViewById(R.id.buttonSpecialContentConfirm).setOnClickListener(this);
//        findViewById(R.id.buttonSpecialContentGuest).setOnClickListener(this);

        // set hint according to current user.
        TextView textHint = (TextView) findViewById(R.id.textHint);
        textHint.setText(mUser.getHint());

        Toast.makeText(this, "歡迎" + mUser.getName() + "，請輸入通關密語", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonSpecialContentConfirm:
                EditText textKey = (EditText) findViewById(R.id.textKey);
                String userInputKey = textKey.getText().toString();

                if (userInputKey.equals(mUser.getKey())) {
                    // set question linear layout to GONE, and make content text view visible.
                    findViewById(R.id.specialContentQuestionLayout).setVisibility(View.GONE);
                    findViewById(R.id.textBackSpecialContent).setVisibility(View.VISIBLE);
                    // setting content text view to show default content for guest users.
                    final TextView contentView = (TextView) findViewById(R.id.textBackSpecialContent);
                    TextView titleView = (TextView) findViewById(R.id.textBackSpecialTitle);
                    String userContentText = mUser.getContent();
                    String userTitleText = mUser.getTitle();
                    contentView.setText(userContentText);
                    titleView.setText(userTitleText);

                    // get line count
                    contentView.post(new Runnable() {
                        @Override
                        public void run() {
                            int lineCount = contentView.getLineCount();
                            // if over 19 lines, make text view scrollable
                            if (lineCount > MAX_LINE_COUNT)
                                contentView.setMovementMethod(new ScrollingMovementMethod());
                        }
                    });
                    tracker.sendAnalyticAction("Confirm: Correct Password");
                    // display password success message, "displaying unique message".
                    Toast.makeText(this, "通關密語正確，顯示專屬訊息。", Toast.LENGTH_SHORT).show();
                }
                else {
                    tracker.sendAnalyticAction("Confirm: Wrong Password");
                    Toast.makeText(this, "再試一次", Toast.LENGTH_SHORT).show();
                }
                break;
            /*
            case R.id.buttonSpecialContentGuest:
                // set question linear layout to GONE, and make content text view visible.
                findViewById(R.id.specialContentQuestionLayout).setVisibility(View.GONE);
                findViewById(R.id.textBackSpecialContent).setVisibility(View.VISIBLE);
                // setting content text view to show default content for guest users.
                TextView contentView = (TextView) findViewById(R.id.textBackSpecialContent);
                TextView titleView = (TextView) findViewById(R.id.textBackSpecialTitle);
                // get default user's content (index 0 is always the default content);
                User defaultUser = userList.get(0);
                String defaultContentText = defaultUser.getContent();
                String defaultTitleText = defaultUser.getTitle();
                contentView.setText(defaultContentText);
                titleView.setText(defaultTitleText);
                // display guest message.
                Toast.makeText(this, "未輸入通關密語，顯示Guest訊息。", Toast.LENGTH_SHORT).show();
                break;
            */
        }
    }

    float downX, downY, upX, upY;
    static final int MIN_DISTANCE = 100;

    // 由右向左滑動(下一張)/由左向右滑動(上一張)，滑動是下一張，按下(按鈕)是翻面
    // mWest 下一張, mEast 上一張
    public boolean handleTouch(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                Log.d("Action", "DOWN");
                return true;
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();
                Log.d("Action", "UP");

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                // click?
                if ((Math.abs(deltaX) + Math.abs(deltaY)) <= 50) {
//                    Log.d("Clicked", " ");
                    Log.d("Action", "CLICK");
                    onTouchClick();
                    return true;
                }
                // swipe horizontal?
                if(Math.abs(deltaX) > MIN_DISTANCE && Math.abs(deltaY) < MIN_DISTANCE){
//                    Log.d("Swipe Horizontal"," ");
                    // left or right
                    if(deltaX < 0) { onLeftToRightSwipe(); return false; }
                    if(deltaX > 0) { onRightToLeftSwipe(); return false; }
                } else {
//                    Log.i( "H Swipe was only " + Math.abs(deltaX), " long, need at least " + MIN_DISTANCE);
                }

                // swipe vertical?
                if(Math.abs(deltaY) > MIN_DISTANCE && Math.abs(deltaX) < MIN_DISTANCE){
//                    Log.d("Swipe Vertical"," ");
                    // top or down
                    if(deltaY < 0) { onTopToBottomSwipe(); return false; }
                    if(deltaY > 0) { onBottomToTopSwipe(); return false; }

                } else {
//                    Log.i( "V Swipe was only " + Math.abs(deltaX), " long, need at least " + MIN_DISTANCE);
                }
//               return true;
            }
        }
        return false;
    }

    public void onTouchClick() { }
    public void onRightToLeftSwipe() { }
    public void onLeftToRightSwipe(){ }
    public void onTopToBottomSwipe(){
        tracker.sendAnalyticAction("Exit: Swipe Down");
        this.intentBack();
    }
    public void onBottomToTopSwipe(){
        tracker.sendAnalyticAction("Exit: Swipe Up");
        this.intentBack();
    }

    @Override
    public void onBackPressed() {
        this.intentBack();
    }

    // go back to special card activity
    public void intentBack() {
        tracker.sendAnalyticAction("Exit: Back Press");

        // set userNumber to default
        this.mUserNumber = 0;
        // go back to special card activity
        Intent intent = new Intent(this, SpecialCardActivity.class);
        this.finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
