package com.kindroid.hub.ui.category.ringtone;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.kindroid.hub.R;
import com.kindroid.hub.adapter.RingtonesListAdapter;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.proto.RingOrWallPaperProtoc.RingOrWallPaper;
import com.kindroid.hub.provider.RingtoneProvider;
import com.kindroid.hub.ui.category.ringtone.DownloadManager.ViewType;

public class RingSearch extends Activity implements OnItemClickListener, OnClickListener{

	private static final String TAG ="RingSearch";
	private TextView mTitleTextView, mLoadingText;
	private ProgressBar mLoadingProgressBar;
	private ImageView mSearchBtn, mBackBtn;
	private EditText mSearchInput;
	private ListView mSearchHistoryList, mSearchResultList;
	private RingtonesListAdapter mResultAdapter;
	
	private static final int LIST_FOOTER_MODE_NONE = 0;
    private static final int LIST_FOOTER_MODE_REFRESH = 1;
    private static final int LIST_FOOTER_MODE_MORE = 2;
    private int mListFooterMode;
    
    private String HISTROY_SPLIT = "/";
    private static final int MAX_HISTROY_AUTO_COMPLETE = 20;

    protected LinkedList<String> mFilterSearchHistory;
    protected LinkedList<String> mSearchHistroy;
//    protected ListView mHistoryList;
    protected String mLastSearch;
    protected String mPreferenceKey;
    protected SearchHistroyAdapter mSearchHistoryAdapter;
    private List<RingOrWallPaper> mSearchListData;
    
    private RingtoneManager mRingtoneManager;
    private Handler mHandler;
    
    private int mPageIndex = 0;
    private static final int MAX_REQUEST_COUNT = 10;
    private boolean isSearchKeyChanged = false;
    private QueryTask mQueryTask;
    private LoadTask mLoadTask;
    private ContentResolver mCr;
    private String SELECTION = HistoryDBHelper.TYPE + "=" + HistoryDBHelper.SEARCH_TYPE;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		mCr = getContentResolver();
		mRingtoneManager = RingtoneManager.newInstance(this, mHandler);
		setContentView(R.layout.ring_search);
		initSearchBarControl();
		initSearchResultViews();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.ACTION_DOWN) {
			mCr.delete(RingtoneProvider.PREVIEW_CONTENT_URL, SELECTION, null);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}



	private void initSearchBarControl() {
		mSearchHistroy = new LinkedList<String>();
		mFilterSearchHistory = new LinkedList<String>();
		initializeSearchHistroy();
		mTitleTextView = (TextView) findViewById(R.id.frame_title);
		mTitleTextView.setText(R.string.msg_search_title);
		mSearchBtn = (ImageView) findViewById(R.id.search_action);
		mSearchBtn.setOnClickListener(this);
		mSearchInput = (EditText) findViewById(R.id.search_input);
		mSearchInput.addTextChangedListener(new MyWatcher());
		
		mBackBtn = (ImageView) findViewById(R.id.icon_back);
		mBackBtn.setOnClickListener(this);
		ImageView searchBtn = (ImageView) findViewById(R.id.icon_search);
		searchBtn.setVisibility(View.GONE);
		mSearchHistoryList = (ListView) findViewById(R.id.search_history_view);
		mSearchHistoryList.setOnItemClickListener(new OnHistoryItemClick());
		mSearchHistoryAdapter = new SearchHistroyAdapter();
		mSearchHistoryList.setAdapter(mSearchHistoryAdapter);
		
	}
	
	private void initSearchResultViews() {
		mSearchResultList  = (ListView) findViewById(R.id.search_result_view);
		mSearchResultList.setOnItemClickListener(this);

		mResultAdapter = new RingtonesListAdapter(this, mRingtoneManager, ViewType.PREVIEW);
		mResultAdapter.setListItemHandler(mHandler);
		LayoutInflater inflater = LayoutInflater.from(this);
		View mFooterView = inflater.inflate(R.layout.ringtone_listview_loading, mSearchResultList, false);
		mLoadingProgressBar = (ProgressBar) mFooterView.findViewById(R.id.preview_loading_progressbar);
		mLoadingText = (TextView) mFooterView.findViewById(R.id.preview_loading_text);
		mSearchResultList.addFooterView(mFooterView);
		mSearchResultList.setAdapter(mResultAdapter);
		mSearchResultList.setVisibility(View.GONE);
	}

	private void clearSearchHistory() {
	    mSearchHistroy.clear();
	    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RingSearch.this).edit();
	    editor.remove(mPreferenceKey);
	    editor.commit();
	    updateAdapter(null);
	}

	private void initializeSearchHistroy() {
	    SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(RingSearch.this);
	    String histories = prefer.getString(mPreferenceKey, null);
	    if (histories == null)
	        return;
	    String[] historyArray = histories.split("/");
	    if (historyArray == null)
	        return;
	    for (String str:historyArray) {
	    	mSearchHistroy.add(str);
	    	mFilterSearchHistory.add(str);
	    }
	}

	private void saveSearchHistroy() {
	    if (mSearchHistroy.isEmpty())
	        return;
	    StringBuilder sb = new StringBuilder();
	    String history = null;
	    int count = 0;
	    Iterator<String> searchHistoryIterator = mSearchHistroy.iterator();
	    while(searchHistoryIterator.hasNext()) {
	    	history = searchHistoryIterator.next();
	    	sb.append(history).append("/");
	    	count ++;
	    	if (count > MAX_HISTROY_AUTO_COMPLETE) {
	    		sb.deleteCharAt(sb.length()-1);
	    		break;
	    	}
	    }
	    String histories = sb.toString();
	    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
	    editor.putString(mPreferenceKey, histories);
	    editor.commit();
	}
	
	private void updateAdapter(String search) {
	    mFilterSearchHistory.clear();
	    if (TextUtils.isEmpty(search)) {
	        mFilterSearchHistory.addAll(mSearchHistroy);
	    } else {
	    	 Iterator<String> searchHistoryIterator = mSearchHistroy.iterator();
	 	     while (searchHistoryIterator.hasNext()) {
	 	        String history = searchHistoryIterator.next();
	 	        if (history.toLowerCase().indexOf(search) == -1) {
	 	             continue;
	 	        } else {
	 	             mFilterSearchHistory.add(history);
	 	        }
	 	    }
	    }
	    mSearchHistoryAdapter.notifyDataSetChanged();
	}

	private void updateSearchHistory(String search) {
		//if current query has existed, don't add it into history.
	    if (TextUtils.isEmpty(search.trim()) || mSearchHistroy.contains(search))
	        return;
	    //add the last query into the head of history list.
	    mSearchHistroy.add(0, search);
	    updateAdapter(search);
	    saveSearchHistroy();
	}
	
	private void searchAction(String key) {
		mSearchHistoryList.setVisibility(View.GONE);
		mSearchResultList.setVisibility(View.VISIBLE);
		String keyLowerCase = key.toLowerCase();
		if (keyLowerCase.equals(mLastSearch)) {
			return ;
		} else {
			mLastSearch = keyLowerCase;
			mPageIndex = 0;
			isSearchKeyChanged = true;
		}
		if (isSearchKeyChanged) {
			updateSearchHistory(keyLowerCase);
		    new QueryTask().execute(keyLowerCase);
		}
	}
	
	private class SearchHistroyAdapter extends BaseAdapter {
	    private final LayoutInflater mInflater;

	    public SearchHistroyAdapter() {
	    	mInflater = LayoutInflater.from(RingSearch.this);
	    }

	    @Override
	    public int getCount() {
	      int count = mFilterSearchHistory.size();
	      if (TextUtils.isEmpty(mSearchInput.getText().toString().trim()) && !mSearchHistroy.isEmpty())
	            return count + 1;
	      return count;
	    }

	    public Object getItem(int position) {
	       return null;
	    }

	    public long getItemId(int position) {
	       return 0L;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup viewGroup) {
	       if (convertView == null) {
	    	   convertView = mInflater.inflate(R.layout.search_history_item_view, viewGroup, false);
	       }
	       convertView.setVisibility(View.VISIBLE);
	       int i = mFilterSearchHistory.size();
	       if (position < i) {
	    	  ((TextView) convertView).setText(mFilterSearchHistory.get(position));
	       } else {
	    	  ((TextView) convertView).setText(R.string.search_history_clear);
	          if (!TextUtils.isEmpty(mSearchInput.getText().toString())) {
	        	  convertView.setVisibility(View.GONE);
	          }
	       }
	       return convertView;
	    }
	}
	
	private void startQuery() {
		if (mQueryTask != null) {
			mQueryTask.cancel(true);
		}
		mQueryTask = new QueryTask();
		mQueryTask.execute();
	}
	
	private void loadMessages() {
		if(mLoadTask != null) mLoadTask.cancel(true);
		mLoadTask = new LoadTask();
		mLoadTask.execute();
	}
	
	private class OnHistoryItemClick
        implements AdapterView.OnItemClickListener {
        private OnHistoryItemClick() { }

        public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
        		int position, long id) {
            int i = mFilterSearchHistory.size();
            if (position >= i) {
                clearSearchHistory();
                return;
            }
            String searchText = mFilterSearchHistory.get(position);
            mSearchInput.setText(searchText);
            mSearchInput.setSelection(mSearchInput.length());
            searchAction(searchText);
        }
    }
	
	class QueryTask extends AsyncTask<String, Void, Integer> {

		private List<RingOrWallPaper> ringtoneStructure;
		private static final int SUCCESS = 1;
		private static final int FAILED = 0;
		//represents no more messages and not needs to refresh list.
		private static final int EOF_EMPTY = 2;
		//represents no more messages but needs to refresh list.
		private static final int EOF = 3;
		private static final int CANCEL = 4;
		private int descriptionResID;
		private Drawable indicator;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoadingText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			mLoadingText.setText(R.string.msg_loading);
			mLoadingProgressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Integer doInBackground(String... keys) {
			if (keys.length == 0) {
				Log.v(TAG, "no query key");
				return CANCEL;
			} else {
				ringtoneStructure = DataService.getSearchListData(true, keys[0], mPageIndex, MAX_REQUEST_COUNT);
			}
			if (ringtoneStructure == null) {
				descriptionResID = R.string.report_no_network_error;
			    indicator = getResources().getDrawable(R.drawable.indicator_error);
				return FAILED;
			}
			int loadMsgsCount = ringtoneStructure.size();
			if (loadMsgsCount == 0) {
				descriptionResID = R.string.msg_search_no_match;
			    indicator = getResources().getDrawable(R.drawable.refresh);
				return EOF_EMPTY;
			}
			//insert data into history table in ring.db
			if (isCancelled()) return CANCEL;
			
			//删除之前的搜索数据
            if (isSearchKeyChanged) {
            	mCr.delete(RingtoneProvider.PREVIEW_CONTENT_URL, SELECTION, null);
            }
            mPageIndex ++;
			ContentValues values = new ContentValues();
			for (RingOrWallPaper ring:ringtoneStructure) {
				if (isCancelled()) return CANCEL;
				mRingtoneManager.populateContentValues(values, ring, HistoryDBHelper.SEARCH_TYPE);
				if (isCancelled()) return CANCEL;
				mCr.insert(RingtoneProvider.PREVIEW_CONTENT_URL, values);
			}
			if (loadMsgsCount < MAX_REQUEST_COUNT) {
				descriptionResID = R.string.more;
			    indicator = getResources().getDrawable(R.drawable.refresh);
				return EOF;
			} else {
				descriptionResID = R.string.msg_loading;
			    indicator = null;
				return SUCCESS;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			mLoadingText.setCompoundDrawablesWithIntrinsicBounds(indicator, null, null, null);
			mLoadingText.setText(descriptionResID);
			if (result == SUCCESS) {
				mLoadingProgressBar.setVisibility(View.VISIBLE);
				loadMessages();
			} else if (result == EOF) {
				loadMessages();
				mLoadingProgressBar.setVisibility(View.GONE);
			} else if (result == EOF_EMPTY) {
				mLoadingProgressBar.setVisibility(View.GONE);
				Log.v(TAG, "reach EOF with empty message");
			} else if (result == FAILED) {
				mLoadingProgressBar.setVisibility(View.GONE);
			} 
			mQueryTask = null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mQueryTask = null;
		}
		
		
	}
	
	class LoadTask extends AsyncTask<String, Void, Cursor> {

		@Override
		protected Cursor doInBackground(String... keys) {
			return mRingtoneManager.queryHistory(SELECTION, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			mResultAdapter.changeCursor(result);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mLoadTask = null;
		}
		
		
	}
	
	class MyWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			mSearchHistoryList.setVisibility(View.VISIBLE);
			mSearchResultList.setVisibility(View.GONE);

			if (TextUtils.isEmpty(s.toString())) {
				updateAdapter(null);
			} else {
				updateAdapter(s.toString().toLowerCase());
			}
		}
	}
	
	class ResultOnScrollListener implements AbsListView.OnScrollListener {
		private int lastItem;

		@Override
		public void onScroll(AbsListView paramAbsListView, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastItem = firstVisibleItem + visibleItemCount - 1;
		}

		@Override
		public void onScrollStateChanged(AbsListView listView, int scrollState) {
			if (mResultAdapter != null && lastItem == mResultAdapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				startQuery();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position < mResultAdapter.getCount()) {
			RingtoneMessageListItem itemView = (RingtoneMessageListItem) view;
			Intent intent = RingtoneDetailsActivity.actionStartIntent(this, 
					itemView.getRingId(), position, HistoryDBHelper.SEARCH_TYPE, mPageIndex);
			startActivity(intent);
		}
		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.search_action:
			String searchText = mSearchInput.getText().toString();
			if (!TextUtils.isEmpty(searchText))
				searchAction(searchText);
			break;
		case R.id.icon_back:
			finish();
			break;
		}
		
	}
}
