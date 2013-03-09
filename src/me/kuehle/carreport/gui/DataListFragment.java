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

import java.util.ArrayList;

import me.kuehle.carreport.Preferences;
import me.kuehle.carreport.R;
import me.kuehle.carreport.db.AbstractItem;
import me.kuehle.carreport.db.Car;
import me.kuehle.carreport.db.OtherCost;
import me.kuehle.carreport.db.Refueling;
import me.kuehle.carreport.util.RecurrenceInterval;
import me.kuehle.carreport.util.gui.MessageDialogFragment;
import me.kuehle.carreport.util.gui.MessageDialogFragment.MessageDialogFragmentListener;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class DataListFragment extends Fragment implements
		MessageDialogFragmentListener {
	public abstract class AbstractTabHelper {
		protected AbstractItem[] mItems;
		protected ListView mListView;

		public AbstractTabHelper(ListView listView) {
			mListView = listView;
			mListView.setAdapter(new DataListAdapter());
		}

		protected abstract int getAlertDeleteManyMessage();

		protected abstract int getIndicator();

		protected abstract String getTag();

		protected abstract int getExtraEdit();

		protected abstract int getView();

		protected abstract void updateItems(Car car);

		protected abstract void updateListAdapter();
	}

	public interface Callback {
		public abstract void onItemClosed();

		public abstract void onItemSelected(int edit, int carId, int id);

		public abstract void onTabChanged(int edit);
	}

	public class OtherCostsTabHelper extends AbstractTabHelper {
		public OtherCostsTabHelper(ListView listView) {
			super(listView);
		}

		@Override
		protected int getAlertDeleteManyMessage() {
			return R.string.alert_delete_others_message;
		}

		@Override
		protected int getIndicator() {
			return R.string.tab_indicator_other;
		}

		@Override
		protected String getTag() {
			return "otherCosts";
		}

		@Override
		protected int getView() {
			return R.id.tab_other_costs;
		}

		@Override
		protected void updateItems(Car car) {
			mItems = OtherCost.getAllForCar(car, false);
		}

		@Override
		protected void updateListAdapter() {
			ArrayList<SparseArray<String>> data = new ArrayList<SparseArray<String>>();

			Preferences prefs = new Preferences(getActivity());
			java.text.DateFormat dateFmt = DateFormat
					.getDateFormat(getActivity());
			String[] repIntervals = getResources().getStringArray(
					R.array.repeat_intervals);
			for (int i = 0; i < mItems.length; i++) {
				OtherCost other = (OtherCost) mItems[i];
				SparseArray<String> dataItem = new SparseArray<String>();
				dataItem.put(R.id.title, other.getTitle());
				dataItem.put(R.id.date, dateFmt.format(other.getDate()));
				if (other.getMileage() > -1) {
					dataItem.put(
							R.id.data1,
							String.format("%d %s", other.getMileage(),
									prefs.getUnitDistance()));
				}
				dataItem.put(
						R.id.data2,
						String.format("%.2f %s", other.getPrice(),
								prefs.getUnitCurrency()));
				dataItem.put(R.id.data3, repIntervals[other.getRecurrence()
						.getInterval().getValue()]);
				if (!other.getRecurrence().getInterval()
						.equals(RecurrenceInterval.ONCE)) {
					int recurrences = other.getRecurrence()
							.getRecurrencesSince(other.getDate());
					dataItem.put(
							R.id.data2_calculated,
							String.format("%.2f %s", other.getPrice()
									* recurrences, prefs.getUnitCurrency()));
					dataItem.put(R.id.data3_calculated,
							String.format("x%d", recurrences));
				}
				data.add(dataItem);
			}

			((DataListAdapter) mListView.getAdapter()).setData(data);
		}

		@Override
		protected int getExtraEdit() {
			return DataDetailActivity.EXTRA_EDIT_OTHER;
		}
	}

	public class RefuelingsTabHelper extends AbstractTabHelper {
		public RefuelingsTabHelper(ListView listView) {
			super(listView);
		}

		@Override
		protected int getAlertDeleteManyMessage() {
			return R.string.alert_delete_refuelings_message;
		}

		@Override
		protected int getIndicator() {
			return R.string.tab_indicator_refuelings;
		}

		@Override
		protected String getTag() {
			return "refuelings";
		}

		@Override
		protected int getView() {
			return R.id.tab_refuelings;
		}

		@Override
		protected void updateItems(Car car) {
			mItems = Refueling.getAllForCar(car, false);
		}

		@Override
		protected void updateListAdapter() {
			ArrayList<SparseArray<String>> data = new ArrayList<SparseArray<String>>();

			Preferences prefs = new Preferences(getActivity());
			java.text.DateFormat dateFmt = DateFormat
					.getDateFormat(getActivity());
			for (int i = 0; i < mItems.length; i++) {
				Refueling refueling = (Refueling) mItems[i];
				SparseArray<String> dataItem = new SparseArray<String>();

				dataItem.put(R.id.title,
						getString(R.string.edit_title_refueling));
				if (refueling.getFuelType() != null) {
					dataItem.put(R.id.subtitle, refueling.getFuelType()
							.getName());
				}
				dataItem.put(R.id.date, dateFmt.format(refueling.getDate()));

				dataItem.put(
						R.id.data1,
						String.format("%d %s", refueling.getMileage(),
								prefs.getUnitDistance()));
				if (i + 1 < mItems.length) {
					Refueling nextRefueling = (Refueling) mItems[i + 1];
					dataItem.put(
							R.id.data1_calculated,
							String.format("+ %d %s", refueling.getMileage()
									- nextRefueling.getMileage(),
									prefs.getUnitDistance()));
				}

				dataItem.put(
						R.id.data2,
						String.format("%.2f %s", refueling.getPrice(),
								prefs.getUnitCurrency()));
				dataItem.put(
						R.id.data2_calculated,
						String.format("%.3f %s/%s", refueling.getPrice()
								/ refueling.getVolume(),
								prefs.getUnitCurrency(), prefs.getUnitVolume()));

				dataItem.put(
						R.id.data3,
						String.format("%.2f %s", refueling.getVolume(),
								prefs.getUnitVolume()));
				if (refueling.isPartial()) {
					dataItem.put(R.id.data3_calculated,
							getString(R.string.label_partial));
				} else if (i + 1 < mItems.length) {
					float diffVolume = refueling.getVolume();
					for (int j = i + 1; j < mItems.length; j++) {
						Refueling nextRefueling = (Refueling) mItems[j];
						if (nextRefueling.isPartial()) {
							diffVolume += nextRefueling.getVolume();
						} else {
							int diffMileage = refueling.getMileage()
									- nextRefueling.getMileage();
							dataItem.put(
									R.id.data3_calculated,
									String.format("%.2f %s/100%s", diffVolume
											/ diffMileage * 100,
											prefs.getUnitVolume(),
											prefs.getUnitDistance()));
							break;
						}
					}
				}

				data.add(dataItem);
			}

			((DataListAdapter) mListView.getAdapter()).setData(data);
		}

		@Override
		protected int getExtraEdit() {
			return DataDetailActivity.EXTRA_EDIT_REFUELING;
		}
	}

	public class DataListAdapter extends BaseAdapter {
		private ArrayList<SparseArray<String>> data = new ArrayList<SparseArray<String>>();
		private int[] fields = { R.id.title, R.id.subtitle, R.id.date,
				R.id.data1, R.id.data1_calculated, R.id.data2,
				R.id.data2_calculated, R.id.data3, R.id.data3_calculated };

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.list_item_data, parent, false);
			}

			SparseArray<String> item = data.get(position);
			for (int field : fields) {
				TextView textView = (TextView) convertView.findViewById(field);
				String value = item.get(field);
				if (value != null) {
					textView.setText(value);
					textView.setVisibility(View.VISIBLE);
				} else if (field == R.id.subtitle) {
					textView.setVisibility(View.GONE);
				} else {
					textView.setVisibility(View.INVISIBLE);
				}
			}

			return convertView;
		}

		public void setData(ArrayList<SparseArray<String>> data) {
			this.data = data;
		}
	}

	private static final String STATE_CURRENT_ITEM = "current_position";
	private static final String STATE_CURRENT_CAR = "current_car";
	private static final String STATE_CURRENT_TAB = "current_tab";
	private static final int DELETE_REQUEST_CODE = 0;

	private Car[] mCars;
	private Car mCurrentCar = null;

	private TabHost mTabHost;
	private AbstractTabHelper[] mTabs;
	private AbstractTabHelper mCurrentTab;

	private int mCurrentItem = ListView.INVALID_POSITION;
	private boolean mActivateOnClick = false;
	private ActionMode mActionMode = null;
	private boolean dontStartActionMode = false;

	private Callback mCallback;

	private OnTabChangeListener mOnTabChangeListener = new OnTabChangeListener() {
		@Override
		public void onTabChanged(String tabId) {
			setCurrentPosition(ListView.INVALID_POSITION);
			mCallback.onItemClosed();
			mCurrentTab = mTabs[mTabHost.getCurrentTab()];
			mCallback.onTabChanged(mCurrentTab.getExtraEdit());
		}
	};

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			setCurrentPosition(position);

			int edit = mCurrentTab.getExtraEdit();
			int itemId = mCurrentTab.mItems[mCurrentItem].getId();
			mCallback.onItemSelected(edit, mCurrentCar.getId(), itemId);
		}
	};

	private MultiChoiceModeListener mMultiChoiceModeListener = new MultiChoiceModeListener() {
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_delete:
				String message = String.format(
						getString(mCurrentTab.getAlertDeleteManyMessage()),
						mCurrentTab.mListView.getCheckedItemCount());
				MessageDialogFragment.newInstance(DataListFragment.this,
						DELETE_REQUEST_CODE, R.string.alert_delete_title,
						message, android.R.string.yes, android.R.string.no)
						.show(getFragmentManager(), null);
				return true;
			default:
				return false;
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			if (dontStartActionMode) {
				dontStartActionMode = false;
				return false;
			}

			mCallback.onItemClosed();
			mCurrentItem = ListView.INVALID_POSITION;
			mCurrentTab.mListView.setOnItemClickListener(null);

			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.view_data_cab, menu);

			mActionMode = mode;
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mCurrentTab.mListView.setOnItemClickListener(mOnItemClickListener);
			mActionMode = null;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			int count = mCurrentTab.mListView.getCheckedItemCount();
			mode.setTitle(String.format(getString(R.string.cab_title_selected),
					count));
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	};

	private OnNavigationListener mListNavigationCallback = new OnNavigationListener() {
		@Override
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			mCurrentCar = mCars[itemPosition];
			mCallback.onItemClosed();
			updateLists();
			return true;
		}
	};

	private void addTab(String tag, int indicatorId, int contentId) {
		TabSpec tabSpec = mTabHost.newTabSpec(tag);
		tabSpec.setIndicator(getString(indicatorId));
		tabSpec.setContent(contentId);
		mTabHost.addTab(tabSpec);
	}

	public Car getCurrentCar() {
		return mCurrentCar;
	}

	public boolean isItemActivated() {
		return mCurrentTab.mListView.getCheckedItemCount() > 0;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (Callback) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ViewDataListListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_dropdown_item);
		mCars = Car.getAll();
		for (Car car : mCars) {
			adapter.add(car.getName());
		}
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(adapter, mListNavigationCallback);

		// Set selected car from config instance of preferences
		Preferences prefs = new Preferences(getActivity());
		if (savedInstanceState != null) {
			selectCarById(savedInstanceState.getInt(STATE_CURRENT_CAR,
					prefs.getDefaultCar()));
		} else {
			selectCarById(prefs.getDefaultCar());
		}

		mTabHost = (TabHost) inflater.inflate(R.layout.fragment_data_list,
				container, false);

		mTabs = new AbstractTabHelper[2];
		mTabs[0] = new RefuelingsTabHelper(
				(ListView) mTabHost.findViewById(R.id.tab_refuelings));
		mTabs[1] = new OtherCostsTabHelper(
				(ListView) mTabHost.findViewById(R.id.tab_other_costs));

		mTabHost.setup();
		for (AbstractTabHelper tab : mTabs) {
			addTab(tab.getTag(), tab.getIndicator(), tab.getView());

			tab.mListView.setOnItemClickListener(mOnItemClickListener);
			tab.mListView.setMultiChoiceModeListener(mMultiChoiceModeListener);
			tab.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		}

		int position = ListView.INVALID_POSITION;
		if (savedInstanceState != null) {
			mTabHost.setCurrentTab(savedInstanceState.getInt(STATE_CURRENT_TAB,
					0));
			position = savedInstanceState.getInt(STATE_CURRENT_ITEM,
					ListView.INVALID_POSITION);
		}
		mCurrentTab = mTabs[mTabHost.getCurrentTab()];
		setCurrentPosition(position);

		mTabHost.setOnTabChangedListener(mOnTabChangeListener);
		updateLists();

		return mTabHost;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}

	@Override
	public void onDialogNegativeClick(int requestCode) {

	}

	@Override
	public void onDialogPositiveClick(int requestCode) {
		if (requestCode == DELETE_REQUEST_CODE) {
			SparseBooleanArray selected = mCurrentTab.mListView
					.getCheckedItemPositions();
			for (int i = 0; i < mCurrentTab.mItems.length; i++) {
				if (selected.get(i)) {
					mCurrentTab.mItems[i].delete();
				}
			}
			mActionMode.finish();
			mCurrentTab.updateItems(mCurrentCar);
			mCurrentTab.updateListAdapter();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_CURRENT_CAR, mCurrentCar.getId());
		outState.putInt(STATE_CURRENT_TAB, mTabHost.getCurrentTab());
		outState.putInt(STATE_CURRENT_ITEM, mCurrentItem);
	}

	private void selectCarById(int id) {
		ActionBar actionBar = getActivity().getActionBar();
		for (int pos = 0; pos < mCars.length; pos++) {
			if (mCars[pos].getId() == id) {
				mCurrentCar = mCars[pos];
				actionBar.setSelectedNavigationItem(pos);
			}
		}
	}

	public void setActivateOnItemClick(boolean activate) {
		mActivateOnClick = activate;
	}

	public void setCurrentPosition(int position) {
		if (mActionMode != null) {
			mActionMode.finish();
		}

		mCurrentTab.mListView.setItemChecked(mCurrentItem, false);
		if (position != ListView.INVALID_POSITION && mActivateOnClick) {
			dontStartActionMode = true;
			mCurrentTab.mListView.setItemChecked(position, true);
		}
		mCurrentItem = position;
	}

	public void updateLists() {
		setCurrentPosition(ListView.INVALID_POSITION);
		for (AbstractTabHelper tab : mTabs) {
			tab.updateItems(mCurrentCar);
			tab.updateListAdapter();
		}
	}
}
