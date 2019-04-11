package pl.jendrzej.badgebutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class BadgeButton extends RelativeLayout {
    private static final String TAG = "BadgeButton";
    private static final int SIZE_NORMAL = 0;
    private static final int SIZE_MINI = 1;
    private static final int buttonId = 101;
    private static final int badgeId = 102;

    public static final int DIRECTION_UP = 10;
    public static final int DIRECTION_DOWN = 11;
    private float currentRotation = 0;
    private int currentDirection = DIRECTION_UP;
    private boolean isVisible = false;
    private boolean isRotating = false;


    ImageButton button;
    TextSwitcher badge;
    private GradientDrawable backgroundDrawable;
    private GradientDrawable badgeBackgroundDrawable;
    LayoutParams badgeParams;
    Animation textSwitcherIn;
    Animation textSwitcherOut;

    int buttonColor;
    Drawable buttonIcon;
    float buttonElevation;
    int badgeColor;
    int badgeStrokeColor;
    float badgeStrokeWidth;
    int sizeType;
    float sizeDimension;
    int badgeTextColor;

    int badgeCount = 0;
    private static final int BADGE_COUNT_MAX = 99;

    public BadgeButton(Context context) {
        this(context, null);
    }

    public BadgeButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
        TypedArray tp = context.obtainStyledAttributes(attrs, R.styleable.BadgeButton);
        buttonColor = tp.getColor(R.styleable.BadgeButton_bb_color, Color.WHITE);
        buttonIcon = tp.getDrawable(R.styleable.BadgeButton_bb_icon);
        buttonElevation = tp.getDimension(R.styleable.BadgeButton_bb_elevation, 0);
        badgeColor = tp.getColor(R.styleable.BadgeButton_bb_badgeColor, Color.WHITE);
        badgeStrokeColor = tp.getColor(R.styleable.BadgeButton_bb_badgeStrokeColor, Color.BLUE);
        badgeStrokeWidth = tp.getDimension(R.styleable.BadgeButton_bb_badgeStrokeWidth, 0);
        sizeType = tp.getInt(R.styleable.BadgeButton_bb_size, SIZE_NORMAL);
        badgeTextColor = tp.getColor(R.styleable.BadgeButton_bb_badgeTextColor, badgeStrokeColor);
        getSizeDimension(context, sizeType);

        textSwitcherIn = new AlphaAnimation(0f, 1f);
        textSwitcherIn.setDuration(300);
        textSwitcherOut = new AlphaAnimation(1f, 0f);
        textSwitcherOut.setDuration(150);
        tp.recycle();
        init(context);
    }

    private void getSizeDimension(Context context, int sizeType){
        switch (sizeType) {
            case SIZE_NORMAL:
                sizeDimension = context.getResources().getDimension(R.dimen.bb_size_normal);
                break;
            case SIZE_MINI:
                sizeDimension = context.getResources().getDimension(R.dimen.bb_size_mini);
                break;
            default:
                sizeDimension = 0f;
                break;
        }
    }

    private void setButton(Context context) {
        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(buttonColor);
        backgroundDrawable.setShape(GradientDrawable.OVAL);
        button = new ImageButton(context);
        button.setId(buttonId);
        button.setScaleType(ImageView.ScaleType.CENTER);
        button.setImageDrawable(buttonIcon);
        button.setBackground(backgroundDrawable);
        button.setLayoutParams(new LayoutParams((int)sizeDimension, (int)sizeDimension));
    }

    private void setBadge(final Context context) {
        badgeBackgroundDrawable = new GradientDrawable();
        badgeBackgroundDrawable.setColor(badgeColor);
        badgeBackgroundDrawable.setStroke((int)badgeStrokeWidth, badgeStrokeColor);
        badgeBackgroundDrawable.setShape(GradientDrawable.OVAL);
        badge = new TextSwitcher(context);
        badge.setInAnimation(textSwitcherIn);
        badge.setOutAnimation(textSwitcherOut);
        badge.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(context);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeDimension/10);
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
                textView.setTypeface(null, Typeface.BOLD);
                textView.setTextColor(badgeTextColor);
                return textView;
            }
        });
        badge.setId(badgeId);
        badgeParams = new LayoutParams((int)(sizeDimension/2.5), (int)(sizeDimension/2.5));
        badgeParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
        badgeParams.addRule(RelativeLayout.ALIGN_TOP, buttonId);
        badgeParams.addRule(RelativeLayout.ALIGN_LEFT, buttonId);


        badge.setBackground(badgeBackgroundDrawable);
        badge.setLayoutParams(badgeParams);
        badge.setAlpha(0f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setElevation(buttonElevation);
            badge.setElevation(buttonElevation);
        }
    }
    void init(final Context context){

        setMinimumWidth((int)(sizeDimension + 2*buttonElevation));
        setMinimumHeight((int)(sizeDimension + 2*buttonElevation));
        setButton(context);
        setBadge(context);

        button.setClickable(false);
        badge.setClickable(false);
        addView(button);
        addView(badge);

    }

    private void rotateButton(float rotation){
        if(isRotating || currentRotation == rotation) return;
        OvershootInterpolator interpolator = new OvershootInterpolator();
        currentRotation = rotation;
        ViewCompat.animate(button)
                .rotation(currentRotation)
                .withLayer()
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        isRotating = true;
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        isRotating = false;
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        isRotating = false;
                    }
                })
                .setDuration(150)
                .setInterpolator(interpolator)
                .start();
    }

    public void rotateUp(){
        rotateButton(0f);
        currentDirection = DIRECTION_UP;
    }

    public void rotateDown(){
        rotateButton(180f);
        currentDirection = DIRECTION_DOWN;
    }

    public boolean isDirectedUp(){
        return currentDirection == DIRECTION_UP;
    }
    public boolean isDirectedDown(){
        return currentDirection == DIRECTION_DOWN;
    }

    public void hideBadge(){
        ViewCompat.animate(badge)
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setDuration(150)
                .start();
    }

    public void setBadgeCount(int count) {
        badgeCount = count;
        if(badgeCount <= 0) {
            hideBadge();
            badgeCount = 0;
            return;
        }
        showBadge();

        if(isBadgeCountMaxExceeded()) {
            badge.setText(BADGE_COUNT_MAX + "+");
        } else {
            badge.setText(String.valueOf(badgeCount));
        }
    }

    public void incrementBadge(){
        setBadgeCount(++badgeCount);
    }

    public void decrementBadge() {
        setBadgeCount(--badgeCount);
    }

    public void showBadge() {
        ViewCompat.animate(badge)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(300)
                .start();
    }

    public void hide() {
        if(!isVisible()) return;
        isVisible = false;
        ViewCompat.animate(this)
                .alpha(0f)
                .setDuration(150)
                .setInterpolator(new FastOutLinearInInterpolator())
                .start();
    }

    public void show() {
        if(isVisible) return;
        isVisible = true;
        ViewCompat.animate(this)
                .alpha(1f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    public boolean isBadgeVisible(){
        return badge.getAlpha() > 0f;
    }

    public boolean isVisible() {
        return getAlpha() > 0f;
    }

    private boolean isBadgeCountMaxExceeded(){
        return badgeCount > BADGE_COUNT_MAX;
    }
}
