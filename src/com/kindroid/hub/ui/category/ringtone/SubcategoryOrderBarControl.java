package com.kindroid.hub.ui.category.ringtone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kindroid.hub.R;

/**
 * 这个类用来控制频道Tab动作条的状态变化，包括字体颜色和背景的改变。使用方式   Xml布局文件
 *<pre>
 * <SubcategoryOrderBarControl>
 *    <LinearLayout android:id="@+id/preview_order_bar_id">//必须听过这个id,可以修改，参见onFinishInflate()
 *        <YourView/>//tab1
 *        ..........
 *        <YourView/>//tabN
 *    </LinearLayout>
 * </SubcategoryOrderBarControl>
 *</pre>
 */
public class SubcategoryOrderBarControl extends FrameLayout implements View.OnClickListener {
	private final String TAG = getClass().getSimpleName();
	
	private OnTabClickListener mOnMenuClickListener;
	private View mCurrentSelection;
	private LinearLayout mChildLinearLayout;
	private Context mContext;
	private ImageView mSelectionIndicator;
	private int mTabTextDefaultColor, mTabTextPressedColor;
	private Drawable mSelectedViewBackground;
	private int mAnimationStartLeft = 0;
	
	public static interface OnTabClickListener {
		public void onTabClickListener(View menuButton);
	}

	public SubcategoryOrderBarControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mTabTextDefaultColor = mContext.getResources().getColor(R.color.black);
		mTabTextPressedColor = mContext.getResources().getColor(R.color.white);
		mSelectedViewBackground = mContext.getResources().getDrawable(R.drawable.tab_button02);
	}
	
	public SubcategoryOrderBarControl(Context context) {
		super(context);
		throw new RuntimeException("the class only can be used in xml configs, you can't new it programmingly");
	}
	
	@Override
	protected void onFinishInflate() {
		mChildLinearLayout = (LinearLayout) findViewById(R.id.preview_order_bar_id);
		int childCount = mChildLinearLayout.getChildCount();
		Log.v(TAG, "onFinishInflate child count " + childCount);
		if (childCount >0) {
			View child= null;
			for (int i=0; i < childCount; i++) {
				child = mChildLinearLayout.getChildAt(i);
				child.setOnClickListener(this);
				child.setTag(i);
			}
		} else {
			throw new RuntimeException(TAG + " must contain more than one child.");
		}
	}

	public void initMenuSelection(int selection) {
		mCurrentSelection = mChildLinearLayout.getChildAt(selection);
//		Log.v(TAG, "initMenuSelection child count " + mChildLinearLayout.getChildCount());
		
		mSelectionIndicator = new ImageView(mContext);
		mSelectionIndicator.setBackgroundResource(R.drawable.tab_button01);
		mSelectionIndicator.setScaleType(ImageView.ScaleType.CENTER);
//		Log.v(TAG, "initMenuSelection line w/h " + mCurrentSelection.getMeasuredWidth() + "/" + (int) (1*scale+0.5f));
//		Log.v(TAG, "initMenuSelection layout w/h " + getMeasuredWidth() + "/" + getMeasuredHeight());
		//-mCurrentSelection.getPaddingLeft()-mCurrentSelection.getPaddingRight()
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mCurrentSelection.getMeasuredWidth(),mCurrentSelection.getMeasuredHeight());
//		layoutParams.leftMargin = mCurrentSelection.getPaddingLeft();
		mSelectionIndicator.setLayoutParams(layoutParams);

		mSelectionIndicator.setVisibility(View.VISIBLE);
//		Log.v(TAG, "mCurrentSelection.getPaddingLeft() " + mCurrentSelection.getPaddingLeft());
		addView(mSelectionIndicator);
		
		((TextView) mCurrentSelection).setTextColor(mTabTextPressedColor);
	}
	
	public void initOrderSelection(int selection) {
		TextView view = (TextView) mChildLinearLayout.getChildAt(selection);
		view.setTextColor(mTabTextPressedColor);
		view.performClick();
	}
	
	/**
	 * 
	 * @param child
	 */
	private void changeSelection(View child) {
		((TextView) child).setTextColor(mTabTextPressedColor);
		((TextView) child).setBackgroundDrawable(mSelectedViewBackground);
//		int toXDelta = child.getLeft();
//		TranslateAnimation transAnimation = new TranslateAnimation(mAnimationStartLeft, toXDelta, 0, 0);
//		transAnimation.setDuration(400);
//		transAnimation.setStartOffset(0);
//		transAnimation.setFillAfter(true);
//		mSelectionIndicator.startAnimation(transAnimation);
		mCurrentSelection = child;
//		mAnimationStartLeft = mCurrentSelection.getLeft() ;
		
	}
	
	public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
		mOnMenuClickListener = onTabClickListener;
	}

	@Override
	public void onClick(View v) {
		Log.v(TAG, "onClick");
		if (mCurrentSelection == null || mCurrentSelection !=v) {
			if (mOnMenuClickListener!=null) {
				mOnMenuClickListener.onTabClickListener(v);
			}
			if (mCurrentSelection != null) {
				((TextView) mCurrentSelection).setTextColor(mTabTextDefaultColor);
				((TextView) mCurrentSelection).setBackgroundDrawable(null);
			}
			changeSelection(v);
		}
	}
}
