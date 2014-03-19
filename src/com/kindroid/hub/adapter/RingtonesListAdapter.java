package com.kindroid.hub.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.kindroid.hub.R;
import com.kindroid.hub.ui.category.ringtone.RingtoneItemData;
import com.kindroid.hub.ui.category.ringtone.RingtoneManager;
import com.kindroid.hub.ui.category.ringtone.RingtoneMessageListItem;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.ViewType;

public class RingtonesListAdapter extends CursorAdapter {

	private static final String TAG = "RingtonesListAdapter";
	private Context mContext;
	private RingtoneManager mRingtoneManager;
	private RingsDataOnContentChangedListener mRingsDataOnContentChangedListener;
	private ViewType mViewType;
	private Handler mListItemHandler;

//	private HashMap<Long, RingtoneMessageListItem> mIdToRingtoneMessageListItem
//            = new HashMap<Long, RingtoneMessageListItem>();
	
	public static interface RingsDataOnContentChangedListener {
		public void onContentChanged();
	};
	
	public RingtonesListAdapter(Context context, RingtoneManager ringtoneManager, ViewType viewType) {
		super(context, null, true);
		mContext = context;
		mRingtoneManager = ringtoneManager;
		mViewType = viewType;
	}
	
	public void setOnContentChangedListsner(RingsDataOnContentChangedListener onContentChangeListsner) {
		mRingsDataOnContentChangedListener = onContentChangeListsner;
	}
	
	public void setListItemHandler(Handler handler) {
	        mListItemHandler = handler;
	}
	
	public void setViewType(ViewType viewType) {
		mViewType = viewType;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (view instanceof RingtoneMessageListItem) {
			long rawId = cursor.getLong(RingtoneManager.HISTORY_RAWID_INDEX);
			RingtoneItemData ringtoneItemData = mRingtoneManager.getRingItemDataFromCache(mViewType, rawId, cursor);
			RingtoneMessageListItem rmi = (RingtoneMessageListItem) view;
			rmi.setListItemHandler(mListItemHandler);
			rmi.bind(ringtoneItemData, mRingtoneManager, mViewType);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		return layoutInflater.inflate(R.layout.ring_preview_item, parent, false);
	}
	
	@Override
	protected void onContentChanged() {
		if (mRingsDataOnContentChangedListener != null) {
			mRingsDataOnContentChangedListener.onContentChanged();
		}
		Log.v(TAG, "onContentChanged()");
		
		super.onContentChanged();
		notifyDataSetChanged();
	}
	
	
//	public void notifyDwonloadProgressChanged(long rawId, int progress) {
//        RingtoneMessageListItem rmi = mIdToRingtoneMessageListItem.get(rawId);
//        if (rmi != null) {
//            rmi.updateSeekbarProgress(progress);
//        }
//    }
}