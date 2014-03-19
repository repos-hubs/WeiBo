package com.kindroid.hub.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.kindroid.hub.ui.category.ringtone.HistoryDBHelper;

public class RingtoneProvider extends ContentProvider {

    private static final String TAG = "RingtoneProvider";

    public static final String RING_AUTHORITY = "com.kindroid.hub.provider.RingtoneProvider";
    public static final Uri PREVIEW_CONTENT_URL = Uri.parse("content://" + RING_AUTHORITY + "/preview");
    public static final Uri DOWNLOAD_CONTENT_URL = Uri.parse("content://" + RING_AUTHORITY + "/dwonload");
    private static final int PREVIEW = 1;
    private static final int PREVIEW_ITEM = 2;
    private static final int DOWNLOAD = 3;
    private static final int DOWNLOAD_ITEM = 4;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        UriMatcher matcher = sURIMatcher;
        matcher.addURI(RING_AUTHORITY, "preview", PREVIEW);
        matcher.addURI(RING_AUTHORITY, "preview/#", PREVIEW_ITEM);
        matcher.addURI(RING_AUTHORITY, "dwonload", DOWNLOAD);
        matcher.addURI(RING_AUTHORITY, "dwonload/#", DOWNLOAD_ITEM);
    }

    private SQLiteDatabase mDatabase;

    public synchronized SQLiteDatabase getDatabase(Context context) {
        // Always return the cached database, if we've got one
        if (mDatabase != null) {
            return mDatabase;
        }
        HistoryDBHelper helper = new HistoryDBHelper(context);
        mDatabase = helper.getWritableDatabase();
        if (mDatabase != null) {
            mDatabase.setLockingEnabled(true);
        }
        return mDatabase;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sURIMatcher.match(uri);
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        int result = -1;
        switch (match) {
        case DOWNLOAD:// delete downloading data according to selection
        	result = db.delete(HistoryDBHelper.DOWNLOAD_TABLE_NAME, selection, selectionArgs);
        	break;
        case DOWNLOAD_ITEM:// delete a downloading data with specific id
        	String rawId = uri.getPathSegments().get(1);
        	result = db.delete(HistoryDBHelper.DOWNLOAD_TABLE_NAME, whereWithId(rawId, selection), selectionArgs);
        	break;
        case PREVIEW:// delete preview data
        	result = db.delete(HistoryDBHelper.HISTORY_TABLE_NAME, selection, selectionArgs);
        	break;
        }
        if (result <= 0) getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sURIMatcher.match(uri);
        Context context = getContext();
        // See the comment at delete(), above
        SQLiteDatabase db = getDatabase(context);
        Uri resultUri = null;
        long resultId = 0;
        switch(match) {
        case PREVIEW:
        	resultId = db.insert(HistoryDBHelper.HISTORY_TABLE_NAME, null, values);
        	resultUri = ContentUris.withAppendedId(uri, resultId);
    	    break;
        case DOWNLOAD:
        	resultId = db.insert(HistoryDBHelper.DOWNLOAD_TABLE_NAME, null, values);
        	resultUri = ContentUris.withAppendedId(uri, resultId);
        	break;
        }
        // Notify with the base uri, not the new uri (nobody is watching a new record)
        if (resultUri != null)getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Cursor c = null;
        String rawId = null;
        int match = sURIMatcher.match(uri);
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        switch(match) {
        case PREVIEW:
        	c = db.query(HistoryDBHelper.HISTORY_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        	break;
        case DOWNLOAD:
        	c = db.query(HistoryDBHelper.DOWNLOAD_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        	break;
        case DOWNLOAD_ITEM:
        	rawId = uri.getPathSegments().get(1);
        	c = db.query(HistoryDBHelper.DOWNLOAD_TABLE_NAME, projection, whereWithId(rawId, selection), selectionArgs, null, null, sortOrder);
        	break;
        case PREVIEW_ITEM: {
        	rawId = uri.getPathSegments().get(1);
        	c = db.query(HistoryDBHelper.HISTORY_TABLE_NAME, projection, whereWithId(rawId, selection), selectionArgs, null, null, sortOrder);
        	break; }
        }
        if ((c != null) && !isTemporary()) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    private String whereWithId(String id, String selection) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(HistoryDBHelper.RAW_ID).append("=");
        sb.append(id);
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = sURIMatcher.match(uri);
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        int result = 0;
        String rawId = null;
        switch(match) {
        case PREVIEW:
        	result = db.update(HistoryDBHelper.HISTORY_TABLE_NAME, values, selection, selectionArgs);
        	break;
        case PREVIEW_ITEM:
        	rawId = uri.getPathSegments().get(1);
        	result = db.update(HistoryDBHelper.HISTORY_TABLE_NAME, values, whereWithId(rawId, selection), selectionArgs);
        	break;
        case DOWNLOAD:
        	result = db.update(HistoryDBHelper.DOWNLOAD_TABLE_NAME, values, selection, selectionArgs);
        	break;
        case DOWNLOAD_ITEM:
        	rawId = uri.getPathSegments().get(1);
        	result = db.update(HistoryDBHelper.DOWNLOAD_TABLE_NAME, values, whereWithId(rawId, selection), selectionArgs);
        	break;
        }
        if (result != 0) getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

	@Override
	public String getType(Uri paramUri) {
		// TODO Auto-generated method stub
		return null;
	}
}
