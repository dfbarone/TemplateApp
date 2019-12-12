package com.dfbarone.android.navigationview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.dfbarone.android.navigationview.DisplayUtils;
import com.dfbarone.android.navigationview.R.styleable;

public class NavigationView extends ConstraintLayout {

    private static final String TAG = NavigationView.class.getSimpleName();

    public interface NavigationViewStateChangeListener {
        void onNavigationViewStateChange(int width, int height, PaneDisplayMode mode);
    }

    // Default Constants
    private static final int COMPACT_MODE_WIDTH = 60;
    private static final int EXPANDED_MODE_WIDTH = COMPACT_MODE_WIDTH * 5;
    private static final int COMPACT_MODE_THRESHOLD_WIDTH = 480;
    private static final int EXPANDED_MODE_THRESHOLD_WIDTH = 840;

    // State variables
    // onMeasure state variable
    int mLastMeasureWidth = -1;
    int mLastMeasureHeight = -1;
    private PaneDisplayMode mLastMeasurePaneDisplayMode = null;
    Rect mSize = new Rect();
    // LeftMinimal if open is true
    private boolean mIsActive = false;

    // Properties
    private PaneDisplayMode mPaneDisplayMode = PaneDisplayMode.AUTO;
    private int mCompactModeWidth = COMPACT_MODE_WIDTH;
    private int mExpandedModeWidth = EXPANDED_MODE_WIDTH;
    private int mCompactModeThreashholdWidth = COMPACT_MODE_THRESHOLD_WIDTH;
    private int mExpandedModeThreadholdWidth = EXPANDED_MODE_THRESHOLD_WIDTH;

    // Listeners
    private NavigationViewStateChangeListener mNavigationViewStateChangeListener = null;

    ColorDrawable mColorDrawable;

    public NavigationView(Context context) {
        super(context);
        this.init((AttributeSet) null);
    }

    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(attrs);
    }

    public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (attrs != null) {
            TypedArray a =
                    this.getContext()
                            .obtainStyledAttributes(attrs, styleable.NavigationView_Layout);
            int N = a.getIndexCount();

            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);
                if (attr == styleable.NavigationView_Layout_paneDisplayMode) {
                    int val = a.getInt(attr, 4);
                    setPaneDisplayMode(PaneDisplayMode.fromInt(val));
                } else if (attr == styleable.NavigationView_Layout_compactModeThresholdWidth) {
                    mCompactModeThreashholdWidth = (int)DisplayUtils.pxToDp(getContext(), a.getDimensionPixelSize(attr, COMPACT_MODE_THRESHOLD_WIDTH));
                } else if (attr == styleable.NavigationView_Layout_expandedModeThresholdWidth) {
                    mExpandedModeThreadholdWidth = (int)DisplayUtils.pxToDp(getContext(), a.getDimensionPixelSize(attr, EXPANDED_MODE_THRESHOLD_WIDTH));
                } else if (attr == styleable.NavigationView_Layout_compactModeWidth) {
                    mCompactModeWidth = (int)DisplayUtils.pxToDp(getContext(), a.getDimensionPixelSize(attr, COMPACT_MODE_THRESHOLD_WIDTH));
                    mExpandedModeWidth = mCompactModeWidth * 5;
                } else if (attr == styleable.NavigationView_Layout_expandedModeWidth) {
                    mExpandedModeWidth = (int)DisplayUtils.pxToDp(getContext(), a.getDimensionPixelSize(attr, EXPANDED_MODE_THRESHOLD_WIDTH));
                }
            }

            a.recycle();
        }

        // default background to LTGRAY if no color is set.
        mColorDrawable = (ColorDrawable) getBackground();
        if (mColorDrawable == null) {
            mColorDrawable = new ColorDrawable(Color.LTGRAY);
            setBackground(mColorDrawable);
        }

        //
        setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                setActive(!isActive());
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);

        int widthSizeInDp = (int) DisplayUtils.pxToDp(getContext(), widthSize);
        int heightSizeInDp = (int) DisplayUtils.pxToDp(getContext(), heightSize);

        int width;
        int height;
        if (widthMode == View.MeasureSpec.EXACTLY) {
            Log.d(TAG, "w MeasureSpec.EXACTLY");

            // We may not be getting parent width/height. So we need to try to used cached information to determine height
            // else defer to heightMeasureSpec
            Rect newSize = mLastMeasurePaneDisplayMode != null ? calculateSizeByPaneDisplayMode(
                    mLastMeasurePaneDisplayMode, widthSize, heightSize) : null;

            //Must be this size
            width = widthSize;

            //Measure Height
            if (heightMode == View.MeasureSpec.EXACTLY) {
                Log.d(TAG, "h MeasureSpec.EXACTLY");
                //Must be this size
                height = heightSize;
            } else if (heightMode == View.MeasureSpec.AT_MOST) {
                Log.d(TAG, "h MeasureSpec.AT_MOST");
                //Can't be bigger than...
                height = newSize != null ? Math.min(newSize.height(), heightSize) : heightSize;
            } else {
                Log.d(TAG, "h MeasureSpec.UNSPECIFIED");
                //Be whatever you want
                height = newSize != null ? newSize.height() : heightSize;
            }
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            Log.d(TAG, "w MeasureSpec.AT_MOST");

            mLastMeasurePaneDisplayMode = calculatePaneDisplayMode(widthSizeInDp, heightSizeInDp);
            Rect newSize =
                    calculateSizeByPaneDisplayMode(mLastMeasurePaneDisplayMode, widthSize,
                            heightSize);

            //Can't be bigger than...
            width = Math.min(newSize.width(), widthSize);

            //Measure Height
            if (heightMode == View.MeasureSpec.EXACTLY) {
                Log.d(TAG, "h MeasureSpec.EXACTLY");
                //Must be this size
                height = heightSize;
            } else if (heightMode == View.MeasureSpec.AT_MOST) {
                Log.d(TAG, "h MeasureSpec.AT_MOST");
                //Can't be bigger than...
                height = Math.min(newSize.height(), heightSize);
            } else {
                Log.d(TAG, "h MeasureSpec.UNSPECIFIED");
                //Be whatever you want
                height = newSize.height();
            }
        } else {
            Log.d(TAG, "w MeasureSpec.UNSPECIFIED");

            mLastMeasurePaneDisplayMode = calculatePaneDisplayMode(widthSizeInDp, heightSizeInDp);
            Rect newSize =
                    calculateSizeByPaneDisplayMode(mLastMeasurePaneDisplayMode, widthSize,
                            heightSize);

            //Be whatever you want
            width = newSize.width();

            //Measure Height
            if (heightMode == View.MeasureSpec.EXACTLY) {
                Log.d(TAG, "h MeasureSpec.EXACTLY");
                //Must be this size
                height = heightSize;
            } else if (heightMode == View.MeasureSpec.AT_MOST) {
                Log.d(TAG, "h MeasureSpec.AT_MOST");
                //Can't be bigger than...
                height = Math.min(newSize.height(), heightSize);
            } else {
                Log.d(TAG, "h MeasureSpec.UNSPECIFIED");
                //Be whatever you want
                height = newSize.height();
            }
        }

        super.onMeasure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

        mLastMeasureWidth = width;
        mLastMeasureHeight = height;

        getLocalVisibleRect(mSize);
        if (!mSize.isEmpty()) {
            onNavigationViewStateChange(mLastMeasureWidth, mLastMeasureHeight,
                    mLastMeasurePaneDisplayMode);
        }
    }

    protected void onNavigationViewStateChange(int width, int height, PaneDisplayMode mode) {
        // TODO: do possible layout initializations.
        Log.d(TAG, String.format("onNavigationViewStateChange size %sx%s", width, height));

        if (mode == PaneDisplayMode.LEFTMINIMAL) {
            setBackground(new ColorDrawable(Color.TRANSPARENT));
        } else {
            setBackground(mColorDrawable);
        }

        // TODO: If you want to hide/show NavigationView when LeftMinimal handle here.
        if (mNavigationViewStateChangeListener != null) {
            mNavigationViewStateChangeListener.onNavigationViewStateChange(width, height, mode);
        }
    }

    protected PaneDisplayMode calculatePaneDisplayMode(int parentWidthInDp,
            int parentHeightInDp) {
        PaneDisplayMode state;
        if (mPaneDisplayMode == PaneDisplayMode.TOP ||
                mPaneDisplayMode == PaneDisplayMode.LEFT ||
                mPaneDisplayMode == PaneDisplayMode.LEFTCOMPACT) {
            state = mPaneDisplayMode;
        } else if (mPaneDisplayMode == PaneDisplayMode.LEFTMINIMAL) {
            state = isActive() ? PaneDisplayMode.LEFTCOMPACT : PaneDisplayMode.LEFTMINIMAL;
        } else {
            // Auto Minimal
            if (parentWidthInDp > mCompactModeThreashholdWidth
                    && parentWidthInDp <= mExpandedModeThreadholdWidth) {
                state = PaneDisplayMode.LEFTCOMPACT;
            } else if (parentWidthInDp > mExpandedModeThreadholdWidth) {
                state = PaneDisplayMode.LEFT;
            } else {
                state = isActive() ? PaneDisplayMode.LEFTCOMPACT
                        : PaneDisplayMode.LEFTMINIMAL;
            }
        }
        Log.d(TAG, "calculatePaneDisplayMode " + state.toString() + " " + parentWidthInDp + " " + parentHeightInDp);
        return state;
    }

    protected Rect calculateSizeByPaneDisplayMode(PaneDisplayMode mode, int parentWidth,
            int parentHeight) {
        int newWidth;
        int newHeight;
        if (mode == PaneDisplayMode.TOP) {
            newWidth = parentWidth;
            newHeight = (int) DisplayUtils.dpToPx(getContext(), getCompactModeWidth());
        } else if (mode == PaneDisplayMode.LEFT) {
            newWidth = (int) DisplayUtils.dpToPx(getContext(), getExpandedModeWidth());
            newHeight = parentHeight;
        } else if (mode == PaneDisplayMode.LEFTMINIMAL) {
            newWidth = (int) DisplayUtils.dpToPx(getContext(), getCompactModeWidth());
            newHeight = (int) DisplayUtils.dpToPx(getContext(), getCompactModeWidth() * 2);
        } else {
            newWidth = (int) DisplayUtils.dpToPx(getContext(), getCompactModeWidth());
            newHeight = parentHeight;
        }
        return new Rect(0, 0, newWidth, newHeight);
    }

    /*
     * Getters/Setters
     */
    public PaneDisplayMode getPaneDisplayMode() {
        return mPaneDisplayMode;
    }

    public void setPaneDisplayMode(
            PaneDisplayMode mPaneDisplayMode) {
        this.mPaneDisplayMode = mPaneDisplayMode;
    }

    public int getCompactModeWidth() {
        return mCompactModeWidth;
    }

    public void setCompactModeWidth(int mCompactModeWidth) {
        this.mCompactModeWidth = mCompactModeWidth;
    }

    public int getExpandedModeWidth() {
        return mExpandedModeWidth;
    }

    public void setExpandedModeWidth(int mExpandedModeWidth) {
        this.mExpandedModeWidth = mExpandedModeWidth;
    }

    public int getCompactModeThreashholdWidth() {
        return mCompactModeThreashholdWidth;
    }

    public void setCompactModeThreashholdWidth(int mCompactModeThreashholdWidth) {
        this.mCompactModeThreashholdWidth = mCompactModeThreashholdWidth;
    }

    public int getExpandedModeThreadholdWidth() {
        return mExpandedModeThreadholdWidth;
    }

    public void setExpandedModeThreadholdWidth(int mExpandedModeThreadholdWidth) {
        this.mExpandedModeThreadholdWidth = mExpandedModeThreadholdWidth;
    }

    public NavigationViewStateChangeListener getNavigationViewStateChangeListener() {
        return mNavigationViewStateChangeListener;
    }

    public void setNavigationViewStateChangeListener(
            NavigationViewStateChangeListener listener) {
        this.mNavigationViewStateChangeListener = listener;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setActive(boolean active) {
        if (mIsActive != active) {
            mIsActive = active;
            if (isModeDynamic()) {
                requestLayout();
            }
        }
    }

    protected boolean isModeDynamic() {
        if (mPaneDisplayMode == PaneDisplayMode.LEFTMINIMAL) {
            return true;
        } else if (mPaneDisplayMode == PaneDisplayMode.AUTO) {
            return mLastMeasurePaneDisplayMode == PaneDisplayMode.LEFTMINIMAL ||
                    mLastMeasurePaneDisplayMode == PaneDisplayMode.LEFTCOMPACT;
        }
        return false;
    }

    /**
     * All possible NavigationView modes
     */
    public enum PaneDisplayMode {
        TOP(0),
        LEFT(1),
        LEFTCOMPACT(2),
        LEFTMINIMAL(3),
        AUTO(4);

        int val;

        PaneDisplayMode(int val) {
            this.val = val;
        }

        public int getValue() {
            return this.val;
        }

        public static PaneDisplayMode fromInt(int val) {
            for (PaneDisplayMode i : PaneDisplayMode.values()) {
                if (i.getValue() == val) {
                    return i;
                }
            }
            return PaneDisplayMode.AUTO;
        }
    }
}
