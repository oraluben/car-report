/*
 * Copyright 2012 Jan Kühle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.kuehle.carreport.gui;

import me.kuehle.carreport.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class DataListActivity extends FragmentActivity implements
		AbstractDataDetailFragment.OnItemActionListener,
		DataListFragment.Callback {
	private DataListFragment mList;
	private boolean mTwoPane;

	private OnBackStackChangedListener mOnBackStackChangeListener = new OnBackStackChangedListener() {
		private int count = getFragmentManager().getBackStackEntryCount();

		@Override
		public void onBackStackChanged() {
			FragmentManager fm = getSupportFragmentManager();
			if (fm.getBackStackEntryCount() < count) {
				Fragment fragment = fm.findFragmentById(R.id.detail);
				if (fragment != null) {
					fm.beginTransaction()
							.remove(fragment)
							.setTransition(
									FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
							.commit();
				}

				setNoEntryMessageVisible(true);
				mList.setCurrentPosition(ListView.INVALID_POSITION);
			}
			count = fm.getBackStackEntryCount();
		}
	};

	@Override
	public void itemCanceled() {
		onItemClosed();
		mList.setCurrentPosition(ListView.INVALID_POSITION);
	}

	@Override
	public void itemDeleted() {
		onItemClosed();
		mList.updateLists();
	}

	@Override
	public void itemSaved() {
		onItemClosed();
		mList.updateLists();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			mList.updateLists();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_list);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mList = (DataListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.list);
		if (findViewById(R.id.detail) != null) {
			mTwoPane = true;
			mList.setActivateOnItemClick(true);
		}

		getSupportFragmentManager().addOnBackStackChangedListener(
				mOnBackStackChangeListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_data, menu);
		return true;
	}

	@Override
	public void onItemClosed() {
		getFragmentManager().popBackStack();
	}

	@Override
	public void onItemSelected(int edit, long carId, long id) {
		if (mTwoPane) {
			setNoEntryMessageVisible(false);

			AbstractDataDetailFragment fragment;
			if (edit == DataDetailActivity.EXTRA_EDIT_REFUELING) {
				fragment = DataDetailRefuelingFragment.newInstance(id);
			} else {
				fragment = DataDetailOtherFragment.newInstance(id);
			}

			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction().replace(R.id.detail,
					fragment);
			if (fm.findFragmentById(R.id.detail) == null) {
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.addToBackStack(null);
			}
			ft.commit();
		} else {
			startDetailActivity(edit, carId, id);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_refueling:
			startDetailActivity(DataDetailActivity.EXTRA_EDIT_REFUELING, mList
					.getCurrentCar().getId(),
					AbstractDataDetailFragment.EXTRA_ID_DEFAULT);
			return true;
		case R.id.menu_add_other:
			startDetailActivity(DataDetailActivity.EXTRA_EDIT_OTHER, mList
					.getCurrentCar().getId(),
					AbstractDataDetailFragment.EXTRA_ID_DEFAULT);
			return true;
		case android.R.id.home:
			if (mList.isItemActivated()) {
				itemCanceled();
			} else {
				finish();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onTabChanged(int edit) {
		TextView v = (TextView) findViewById(R.id.txt_no_entry_selected);
		if (v != null) {
			int id = edit == DataDetailActivity.EXTRA_EDIT_REFUELING ? R.drawable.ic_data_detail_refueling
					: R.drawable.ic_data_detail_other;
			v.setCompoundDrawablesWithIntrinsicBounds(0, id, 0, 0);
		}
	}

	private void setNoEntryMessageVisible(boolean visible) {
		TextView v = (TextView) findViewById(R.id.txt_no_entry_selected);
		if (v != null) {
			v.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	private void startDetailActivity(int edit, long carId, long id) {
		Intent detailIntent = new Intent(this, DataDetailActivity.class);
		detailIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		detailIntent.putExtra(DataDetailActivity.EXTRA_EDIT, edit);
		detailIntent.putExtra(AbstractDataDetailFragment.EXTRA_CAR_ID, carId);
		detailIntent.putExtra(AbstractDataDetailFragment.EXTRA_ID, id);
		startActivityForResult(detailIntent, 0);
	}
}