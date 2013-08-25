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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class DataDetailActivity extends FragmentActivity implements
		AbstractDataDetailFragment.OnItemActionListener {
	public static final String EXTRA_EDIT = "edit";

	public static final int EXTRA_EDIT_REFUELING = 0;
	public static final int EXTRA_EDIT_OTHER = 1;
	public static final int EXTRA_EDIT_CAR = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_detail);

		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			Fragment fragment;
			int edit = getIntent()
					.getIntExtra(EXTRA_EDIT, EXTRA_EDIT_REFUELING);
			if (edit == EXTRA_EDIT_REFUELING) {
				fragment = new DataDetailRefuelingFragment();
			} else if (edit == EXTRA_EDIT_OTHER) {
				fragment = new DataDetailOtherFragment();
			} else {
				fragment = new DataDetailCarFragment();
			}

			fragment.setArguments(getIntent().getExtras());

			getSupportFragmentManager().beginTransaction()
					.add(R.id.detail, fragment).commit();
		}
	}

	@Override
	public void onItemSaved() {
		setResult(RESULT_OK);
		finish();
	}

	@Override
	public void onItemCanceled() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onItemDeleted() {
		setResult(RESULT_OK);
		finish();
	}
}
