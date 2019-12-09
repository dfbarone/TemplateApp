package com.example.dbarone.templateapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.example.dbarone.templateapp.R.styleable;

public class NavigationView extends ConstraintLayout {

  private static final String TAG = NavigationView.class.getSimpleName();

  // Default Constants
  private static final int COMPACT_MODE_WIDTH = 56;
  private static final int EXPANDED_MODE_WIDTH = COMPACT_MODE_WIDTH * 5;
  private static final int COMPACT_MODE_THRESHOLD_WIDTH = 480;
  private static final int EXPANDED_MODE_THRESHOLD_WIDTH = 840;

  // State variables
  // onMeasure state variable
  private PaneDisplayModeEnum mLastMeasurePaneDisplayMode = null;
  // LeftMinimal if open is true
  private boolean isActive = false;

  // Properties
  private PaneDisplayModeEnum mPaneDisplayMode = PaneDisplayModeEnum.AUTO;
  private int mCompactModeThreashholdWidth = COMPACT_MODE_THRESHOLD_WIDTH;
  private int mExpandedModeThreadholdWidth = EXPANDED_MODE_THRESHOLD_WIDTH;
  private int mBackButtonVisibility = View.VISIBLE;

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

    // default background to LTGRAY if no color is set.
    mColorDrawable = (ColorDrawable) getBackground();
    if (mColorDrawable == null) {
      mColorDrawable = new ColorDrawable(Color.parseColor("#f5f5f5"));
      setBackground(mColorDrawable);
    }

    if (attrs != null) {
      TypedArray a =
          this.getContext().obtainStyledAttributes(attrs, styleable.NavigationView_Layout);
      int N = a.getIndexCount();

      for (int i = 0; i < N; ++i) {
        int attr = a.getIndex(i);
        if (attr == styleable.NavigationView_Layout_paneDisplayMode) {
          int val = a.getInt(attr, 4);
          setPaneDisplayMode(PaneDisplayModeEnum.fromInt(val));
        } else if (attr == styleable.NavigationView_Layout_compactModeThresholdWidth) {
          mCompactModeThreashholdWidth = a.getInt(attr, COMPACT_MODE_THRESHOLD_WIDTH);
        } else if (attr == styleable.NavigationView_Layout_expandedModeThresholdWidth) {
          mExpandedModeThreadholdWidth = a.getInt(attr, EXPANDED_MODE_THRESHOLD_WIDTH);
        } else if (attr == styleable.NavigationView_Layout_backButtonVisibility) {
          int val = a.getInt(attr, 0);
          mBackButtonVisibility = View.VISIBLE;
          if (val == View.INVISIBLE) {
            mBackButtonVisibility = val;
          } else if (val == View.GONE) {
            mBackButtonVisibility = val;
          }
        }
      }

      a.recycle();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    int widthSizeInDp = (int) DisplayUtils.pxToDp(getContext(), widthSize);
    int heightSizeInDp = (int) DisplayUtils.pxToDp(getContext(), heightSize);

    int width;
    int height;
    if (widthMode == MeasureSpec.EXACTLY) {
      Log.d(TAG, "w MeasureSpec.EXACTLY");

      // We may not be getting parent width/height. So we need to try to used cached information to determine height
      // else defer to heightMeasureSpec
      Rect newSize = mLastMeasurePaneDisplayMode != null ? calculateSizeByPaneDisplayMode(
          mLastMeasurePaneDisplayMode, widthSize, heightSize) : null;

      //Must be this size
      width = widthSize;

      //Measure Height
      if (heightMode == MeasureSpec.EXACTLY) {
        Log.d(TAG, "h MeasureSpec.EXACTLY");
        //Must be this size
        height = heightSize;
      } else if (heightMode == MeasureSpec.AT_MOST) {
        Log.d(TAG, "h MeasureSpec.AT_MOST");
        //Can't be bigger than...
        height = newSize != null ? Math.min(newSize.height(), heightSize) : heightSize;
      } else {
        Log.d(TAG, "h MeasureSpec.UNSPECIFIED");
        //Be whatever you want
        height = newSize != null ? newSize.height() : heightSize;
      }
    } else if (widthMode == MeasureSpec.AT_MOST) {
      Log.d(TAG, "w MeasureSpec.AT_MOST");

      mLastMeasurePaneDisplayMode = calculatePaneDisplayMode(widthSizeInDp, heightSizeInDp);
      Rect newSize =
          calculateSizeByPaneDisplayMode(mLastMeasurePaneDisplayMode, widthSize, heightSize);

      //Can't be bigger than...
      width = Math.min(newSize.width(), widthSize);

      //Measure Height
      if (heightMode == MeasureSpec.EXACTLY) {
        Log.d(TAG, "h MeasureSpec.EXACTLY");
        //Must be this size
        height = heightSize;
      } else if (heightMode == MeasureSpec.AT_MOST) {
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
          calculateSizeByPaneDisplayMode(mLastMeasurePaneDisplayMode, widthSize, heightSize);

      //Be whatever you want
      width = newSize.width();

      //Measure Height
      if (heightMode == MeasureSpec.EXACTLY) {
        Log.d(TAG, "h MeasureSpec.EXACTLY");
        //Must be this size
        height = heightSize;
      } else if (heightMode == MeasureSpec.AT_MOST) {
        Log.d(TAG, "h MeasureSpec.AT_MOST");
        //Can't be bigger than...
        height = Math.min(newSize.height(), heightSize);
      } else {
        Log.d(TAG, "h MeasureSpec.UNSPECIFIED");
        //Be whatever you want
        height = newSize.height();
      }
    }

    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }

  @Override
  public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);
    onNavigationViewStateChange(width, height, mLastMeasurePaneDisplayMode);
  }

  protected void onNavigationViewStateChange(int width, int height, PaneDisplayModeEnum mode) {
    // TODO: do possible layout initializations.
    Log.d(TAG, String.format("onNavigationViewStateChange size %sx%s", width, height));

    if (mode == PaneDisplayModeEnum.LEFTMINIMAL) {
      setBackground(new ColorDrawable(Color.TRANSPARENT));
    } else {
      setBackground(mColorDrawable);
    }
  }

  protected PaneDisplayModeEnum calculatePaneDisplayMode(int parentWidthInDp,
      int parentHeightInDp) {
    PaneDisplayModeEnum state;
    if (mPaneDisplayMode == PaneDisplayModeEnum.LEFT) {
      state = mPaneDisplayMode;
    } else if (mPaneDisplayMode == PaneDisplayModeEnum.LEFTCOMPACT) {
      state = mPaneDisplayMode;
    } else if (mPaneDisplayMode == PaneDisplayModeEnum.LEFTMINIMAL) {
      state = isActive ? PaneDisplayModeEnum.LEFTCOMPACT : PaneDisplayModeEnum.LEFTMINIMAL;
    } else {
      // Auto Minimal
      if (parentWidthInDp > mCompactModeThreashholdWidth
          && parentWidthInDp <= mExpandedModeThreadholdWidth) {
        state = PaneDisplayModeEnum.LEFTCOMPACT;
      } else if (parentWidthInDp > mExpandedModeThreadholdWidth) {
        state = PaneDisplayModeEnum.LEFT;
      } else {
        state = isActive ? PaneDisplayModeEnum.LEFTCOMPACT : PaneDisplayModeEnum.LEFTMINIMAL;
      }
    }
    Log.d(TAG, "calculatePaneDisplayMode " + state.toString() + " " + parentWidthInDp + " " + parentHeightInDp);
    return state;
  }

  protected Rect calculateSizeByPaneDisplayMode(PaneDisplayModeEnum mode, int parentWidth,
      int parentHeight) {
    int newWidth;
    int newHeight;
    if (mode == PaneDisplayModeEnum.LEFT) {
      newWidth = (int) DisplayUtils.dpToPx(getContext(), EXPANDED_MODE_WIDTH);
      newHeight = parentHeight;
      //setBackgroundColor(mColorDrawable.getColor());
    } else if (mode == PaneDisplayModeEnum.LEFTCOMPACT) {
      newWidth = (int) DisplayUtils.dpToPx(getContext(), COMPACT_MODE_WIDTH);
      newHeight = parentHeight;
      //setBackgroundColor(mColorDrawable.getColor());
    } else {
      newWidth = (int) DisplayUtils.dpToPx(getContext(), COMPACT_MODE_WIDTH);
      newHeight = (int) DisplayUtils.dpToPx(getContext(), COMPACT_MODE_WIDTH * 2);
      //setBackgroundColor(Color.TRANSPARENT);
    }
    return new Rect(0, 0, newWidth, newHeight);
  }

  /*
   * Getters/Setters
   */
  public PaneDisplayModeEnum getPaneDisplayMode() {
    return mPaneDisplayMode;
  }

  public void setPaneDisplayMode(
      PaneDisplayModeEnum mPaneDisplayMode) {
    this.mPaneDisplayMode = mPaneDisplayMode;
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

  /*
   */
  public enum PaneDisplayModeEnum {
    TOP(0),
    LEFT(1),
    LEFTCOMPACT(2),
    LEFTMINIMAL(3),
    AUTO(4);

    int val;

    PaneDisplayModeEnum(int val) {
      this.val = val;
    }

    public int getValue() {
      return this.val;
    }

    public static PaneDisplayModeEnum fromInt(int val) {
      for (PaneDisplayModeEnum i : PaneDisplayModeEnum.values()) {
        if (i.getValue() == val) {
          return i;
        }
      }
      return PaneDisplayModeEnum.AUTO;
    }
  }
}
