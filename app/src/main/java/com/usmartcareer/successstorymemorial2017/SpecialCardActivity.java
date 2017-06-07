package com.usmartcareer.successstorymemorial2017;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.usmartcareer.successstorymemorial2017.model.User;
import com.usmartcareer.successstorymemorial2017.sqlite.DBHelper;

import java.util.ArrayList;

import static com.usmartcareer.successstorymemorial2017.sqlite.DBConstants.USER_SIZE;
import static com.usmartcareer.successstorymemorial2017.sqlite.DBConstants.TABLE_NAME_USER_LIST;


public class SpecialCardActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener{
    private boolean mShowingBack = false;
    private Handler mHandler = new Handler();
    private static ArrayList<User> userList = new ArrayList<User>();
    private DBHelper dbHelper;
    private static CustomTracker tracker = new CustomTracker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_card);

        tracker.initialize(this, this, "SSCard_Special");

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
                    .add(R.id.mainContainerSpecial, new CardFrontFragment())
                    .commit();
        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }
        getFragmentManager().addOnBackStackChangedListener(this);

        // layout for touch listener
        FrameLayout myLayout =
                (FrameLayout)findViewById(R.id.mainContainerSpecial);
        // touch listener
        myLayout.setOnTouchListener(
                new FrameLayout.OnTouchListener() {
                    //                    @Override
                    public boolean onTouch(View v, MotionEvent m) {
                        return handleTouch(m);
                    }
                }
        );
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

    public void onTouchClick() {
        tracker.sendAnalyticAction("Flip Card");
        flipCard();
    }
    public void onRightToLeftSwipe() { }

    public void onLeftToRightSwipe(){ }

    public void onTopToBottomSwipe(){
        tracker.sendAnalyticAction("Exit: Swipe down");
        Intent intent = new Intent(this, SSCardActivity.class);
        this.finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void onBottomToTopSwipe(){
        tracker.sendAnalyticAction("Exit: Swipe up");
        Intent intent = new Intent(this, SSCardActivity.class);
        this.finish();
        startActivity(intent);
        overridePendingTransition(0, 0);}

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
                .replace(R.id.mainContainerSpecial, new CardBackFragment())

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
            View view = inflater.inflate(R.layout.fragment_special_card_front, container, false);
            tracker.sendAnalyticScreen("Special_Card_Front~");
            return view;
        }
    }

    // A fragment representing the back of the card.
    public static class CardBackFragment extends Fragment implements View.OnClickListener {
        View mView;
        public CardBackFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mView = inflater.inflate(R.layout.fragment_special_card_back, container, false);

            mView.findViewById(R.id.buttonConfirm).setOnClickListener(this);
            mView.findViewById(R.id.buttonGuest).setOnClickListener(this);

            for (User u : userList) {
                Log.d("User", u.getName());
                Log.d("Content", u.getContent());
                Log.d("Number", Integer.toString(u.getNumber()));
            }
            tracker.sendAnalyticScreen("Special_Card_Back~");
            return mView;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonConfirm:
                    EditText nameText = (EditText) mView.findViewById(R.id.nameText);
                    String userName = nameText.getText().toString();
                    boolean userValid = false;

                    for (User u : userList) {
                        Log.d("checking", Integer.toString(u.getNumber()));
                        if (userName.equals(u.getName())){
                            tracker.sendAnalyticAction("Confirm: Correct username");
                            userValid = true;
                            int n = u.getNumber();
                            SpecialCardContentActivity.mUserNumber = n;
                            // intent to special card content activity with user's special content
                            Intent intent = new Intent(getActivity(), SpecialCardContentActivity.class);
                            getActivity().finish();
                            startActivity(intent);
                            getActivity().overridePendingTransition(0, 0);
                            break;
                        }
                    }
                    if (!userValid) {
                        tracker.sendAnalyticAction("Confirm: Wrong Username");
                        Toast.makeText(getActivity(), "再試一次", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.buttonGuest:
                    tracker.sendAnalyticAction("Guest: As guest");

                    // login as
                    SpecialCardContentActivity.mUserNumber = 0;
                    // intent to special card content activity with user's special content
                    Intent intent = new Intent(getActivity(), SpecialCardContentActivity.class);
                    getActivity().finish();
                    startActivity(intent);
                    getActivity().overridePendingTransition(0, 0);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        tracker.sendAnalyticAction("Exit: Back Press");
        Intent intent = new Intent(this, SSCardActivity.class);
        this.finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
