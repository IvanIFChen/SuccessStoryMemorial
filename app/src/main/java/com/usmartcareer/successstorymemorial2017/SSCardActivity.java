package com.usmartcareer.successstorymemorial2017;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.usmartcareer.successstorymemorial2017.model.Story;
import com.usmartcareer.successstorymemorial2017.sqlite.DBHelper;

import java.util.ArrayList;
import java.util.Random;

import static com.usmartcareer.successstorymemorial2017.sqlite.DBConstants.*;

public class SSCardActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener {
    private static final String TAG = "SSCardActivity";
    private Handler mHandler = new Handler();
    private static boolean mShowingBack = false;
    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private static int index = 0;
    boolean isFav = false;
    public static String PACKAGE_NAME;
    private DBHelper dbHelper;
    private static ArrayList<Story> storyList = new ArrayList<Story>();
    private static final String PREFS_NAME = "SAVED_VALUES";
    public static final int MAX_LINE_COUNT = 19;

    private static CustomTracker tracker = new CustomTracker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sscard);

        tracker.initialize(this, this, "SSCard_Main");

        // restore saved preferences, restoring saved index value
        SharedPreferences settings = getSharedPreferences(this.PREFS_NAME, 0);
        this.index = settings.getInt("savedIndex", index);

        if (STORY_SIZE == 0 || storyList.size() == 0) {
            // fetch data from db and store to an array list.
            dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selectQuery =  "SELECT * FROM " + TABLE_NAME_STORY_LIST;
            Cursor cursor = db.rawQuery(selectQuery, null);
            STORY_SIZE = cursor.getCount();

            for (int i = 0; i < STORY_SIZE; i++) {
                Story story = dbHelper.getStory(i);
                storyList.add(story);
                Log.d("=====story list adding:", story.getTitle());
            }
            db.close();
        }

        // Hides action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // fragment restore from saved state stuff
        if (savedInstanceState == null) {
            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.mainContainer, new CardFrontFragment())
                    .commit();

        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }
        getFragmentManager().addOnBackStackChangedListener(this);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        // layout for touch listener
        FrameLayout myLayout =
                (FrameLayout)findViewById(R.id.mainContainer);
        // touch listener
        myLayout.setOnTouchListener(
                new FrameLayout.OnTouchListener() {
//                    @Override
                    public boolean onTouch(View v,
                                           MotionEvent m) {
                        return handleTouch(m);
                    }
                }
        );

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                handleShakeEvent(count);
            }
        });
    }

    // This method is invoked when shake is detected
    private void handleShakeEvent(int count) {
        Toast.makeText(this, "Random", Toast.LENGTH_SHORT).show();

        // send action to Analytics
        tracker.sendAnalyticAction("Shake");

        // randomize an index within the story list size.
        Random r = new Random();
        index = r.nextInt(storyList.size());

        // refresh the fragment
        Fragment frg = null;
        frg = getFragmentManager().findFragmentById(R.id.mainContainer);
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
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
//               Log.d("ACTION_DOWN X: ", Integer.toString((int)downX));
//               Log.d("ACTION_DOWN Y: ", Integer.toString((int)downY));
                return true;
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();
//               Log.d("ACTION_UP X: ", Integer.toString((int)upX));
//               Log.d("ACTION_UP Y: ", Integer.toString((int)upY));

                float deltaX = downX - upX;
                float deltaY = downY - upY;
//               Log.d("CHANGE_X X: ", Integer.toString((int) deltaX));
//               Log.d("CHANGE_Y Y: ", Integer.toString((int) deltaY));

                // click?
                if ((Math.abs(deltaX) + Math.abs(deltaY)) <= 50) {
//                    Log.d("Clicked", " ");
                    onTouchClick();
                    return true;
                }
                // swipe horizontal?
                if(Math.abs(deltaX) > MIN_DISTANCE && Math.abs(deltaY) < MIN_DISTANCE){
//                    Log.d("Swipe Horizontal"," ");
                    // left or right
                    if(deltaX < 0) { onLeftToRightSwipe(); return true; }
                    if(deltaX > 0) { onRightToLeftSwipe(); return true; }
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

    public void onTouchClick() {
//        Log.d("Clicked!", " ");

        // send action to Analytics
        tracker.sendAnalyticAction("Flip Card");

        flipCard();

    }
    public void onRightToLeftSwipe() {
//        Log.d("RightToLeftSwipe!"," ");

        // send action to Analytics
        tracker.sendAnalyticAction("Next Card");

        index = (index + 1) % STORY_SIZE;

        // refresh the fragment
        Fragment frg = null;
        frg = getFragmentManager().findFragmentById(R.id.mainContainer);
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

    public void onLeftToRightSwipe(){
//        Log.d( "LeftToRightSwipe!"," ");

        // send action to Analytics
        tracker.sendAnalyticAction("Prev Card");

        // index subtract 1 but limit to story list size.
        index = (index - 1) % STORY_SIZE;
        if (index < 0)
            index += storyList.size();

        // refresh the fragment
        Fragment frg = null;
        frg = getFragmentManager().findFragmentById(R.id.mainContainer);
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

    public void onTopToBottomSwipe(){
//        Log.d( "onTopToBottomSwipe!"," ");
        // send action to Analytics
        tracker.sendAnalyticAction("Show Total Available Cards");

        Toast.makeText(this, "總共有" + Integer.toString(STORY_SIZE) + "則故事", Toast.LENGTH_SHORT).show();
    }

    public void onBottomToTopSwipe(){
        // send action to Analytics
        tracker.sendAnalyticAction("Enter Special");

        Intent intent = new Intent(this, SpecialCardActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);

        // When the back stack changes, invalidate the options menu (action bar).
        invalidateOptionsMenu();
    }

    // start page flip when this is invoked
    private void flipCard() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.

        mShowingBack = true;

        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.

        getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing
                // rotations when switching to the back of the card, as well as animator
                // resources representing rotations when flipping back to the front (e.g. when
                // the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a fragment
                // representing the next page (indicated by the just-incremented currentPage
                // variable).
                .replace(R.id.mainContainer, new CardBackFragment())

                // Add this transaction to the back stack, allowing users to press Back
                // to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit();

        // Defer an invalidation of the options menu (on modern devices, the action bar). This
        // can't be done immediately because the transaction may not yet be committed. Commits
        // are asynchronous in that they are posted to the main thread's message loop.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    // A fragment representing the front of the card.
    public static class CardFrontFragment extends Fragment {
        private TextView textViewFrontTitle;
        private TextView textViewFrontCategory;
        private TextView textViewCompanyName;

        public CardFrontFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_card_front, container, false);

            // setting up views
            textViewFrontTitle = (TextView) view.findViewById(R.id.textFrontTitle);
            textViewFrontCategory = (TextView) view.findViewById(R.id.textFrontCategory);
            textViewCompanyName = (TextView) view.findViewById(R.id.textCompanyTitle);

            textViewFrontTitle.setText(storyList.get(index).getTitle());
            textViewFrontCategory.setText(storyList.get(index).getCategory());
            textViewCompanyName.setText(R.string.company_title); // never change

            if (!mShowingBack){
                tracker.sendAnalyticScreen("Card_Front~" + index);
            }
            mShowingBack = false;

            return view;
        }
    }

    // A fragment representing the back of the card.
    public static class CardBackFragment extends Fragment {
        private TextView textViewBackTitle;
        private TextView textViewBackContent;
        private TextView textViewCompanyName;

        public CardBackFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_card_back, container, false);

            // setting up views
            textViewBackTitle = (TextView) view.findViewById(R.id.textBackTitle);
            textViewBackContent = (TextView) view.findViewById(R.id.textBackContent);
            textViewCompanyName = (TextView) view.findViewById(R.id.textCompanyTitle);

            textViewBackTitle.setText(storyList.get(index).getTitle());
            textViewBackContent.setText(storyList.get(index).getContent());
            textViewCompanyName.setText(R.string.company_title); // never change

            // get line count
            textViewBackContent.post(new Runnable() {
                @Override
                public void run() {
                    int lineCount = textViewBackContent.getLineCount();
                    // if over 19 lines, make text view scrollable
                    if (lineCount > MAX_LINE_COUNT)
                        textViewBackContent.setMovementMethod(new ScrollingMovementMethod());
                }
            });

            if (mShowingBack) {
                tracker.sendAnalyticScreen("Card_Back~" + index);
            }

            return view;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);

        // restore saved preferences, restoring saved index value
        SharedPreferences settings = getSharedPreferences(this.PREFS_NAME, 0);
        this.index = settings.getInt("savedIndex", index);

    }

    @Override
    public void onPause() {
        super.onPause();

        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);

        // save the current index value to a shared preference
        SharedPreferences settings = getSharedPreferences(this.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("savedIndex", this.index);

        // Commit the edits
        editor.commit();
    }
}
