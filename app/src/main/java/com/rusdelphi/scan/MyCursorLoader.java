package com.rusdelphi.scan;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

public class MyCursorLoader extends CursorLoader {

	DbAdapter mDb;

	public MyCursorLoader(Context context, DbAdapter db) {
		super(context);
		this.mDb = db;
	}

	@Override
	public Cursor loadInBackground() {

		return mDb.getAllNews();
	}

}