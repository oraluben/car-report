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

package me.kuehle.carreport.db;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class Car extends AbstractItem {
	private String name;
	private int color;
	private Date suspended;

	public Car(int id) {
		synchronized (Helper.dbLock) {
			SQLiteDatabase db = Helper.getInstance().getReadableDatabase();
			Cursor cursor = db.query(CarTable.NAME, CarTable.ALL_COLUMNS,
					BaseColumns._ID + "=?",
					new String[] { String.valueOf(id) }, null, null, null);
			if (cursor.getCount() != 1) {
				cursor.close();
				throw new IllegalArgumentException(
						"A car with this ID does not exist!");
			} else {
				cursor.moveToFirst();
				this.id = id;
				this.name = cursor.getString(1);
				this.color = cursor.getInt(2);
				if (cursor.isNull(3)) {
					this.suspended = null;
				} else {
					this.suspended = new Date(cursor.getLong(3));
				}
				cursor.close();
			}
		}
	}

	private Car(int id, String name, int color, Date suspended) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.suspended = suspended;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public Date getSuspended() {
		return this.suspended;
	}

	public boolean isSuspended() {
		return this.suspended != null;
	}

	public void setSuspended(Date suspended) {
		this.suspended = suspended;
	}

	public void delete() {
		if (getCount() == 1) {
			throw new RuntimeException("The last car cannot be deleted!");
		} else if (!isDeleted()) {
			synchronized (Helper.dbLock) {
				SQLiteDatabase db = Helper.getInstance().getWritableDatabase();
				db.delete(CarTable.NAME, BaseColumns._ID + "=?",
						new String[] { String.valueOf(id) });
			}
			Helper.getInstance().dataChanged();
			deleted = true;
		}
	}

	public void save() {
		if (!isDeleted()) {
			ContentValues values = new ContentValues();
			values.put(CarTable.COL_NAME, name);
			values.put(CarTable.COL_COLOR, color);
			values.put(CarTable.COL_SUSPENDED, suspended == null ? null
					: suspended.getTime());

			synchronized (Helper.dbLock) {
				SQLiteDatabase db = Helper.getInstance().getWritableDatabase();
				db.update(CarTable.NAME, values, BaseColumns._ID + "=?",
						new String[] { String.valueOf(id) });
			}
			Helper.getInstance().dataChanged();
		}
	}

	public static Car create(String name, int color, Date suspended) {
		return create(-1, name, color, suspended);
	}

	public static Car create(int id, String name, int color, Date suspended) {
		ContentValues values = new ContentValues();
		if (id != -1) {
			values.put(BaseColumns._ID, id);
		}
		values.put(CarTable.COL_NAME, name);
		values.put(CarTable.COL_COLOR, color);
		values.put(CarTable.COL_SUSPENDED,
				suspended == null ? null : suspended.getTime());

		synchronized (Helper.dbLock) {
			SQLiteDatabase db = Helper.getInstance().getWritableDatabase();
			id = (int) db.insert(CarTable.NAME, null, values);
		}

		if (id == -1) {
			throw new IllegalArgumentException(
					"A car with this ID does already exist!");
		}

		Helper.getInstance().dataChanged();
		return new Car(id, name, color, suspended);
	}

	public static Car[] getAll() {
		ArrayList<Car> cars = new ArrayList<Car>();

		synchronized (Helper.dbLock) {
			SQLiteDatabase db = Helper.getInstance().getReadableDatabase();
			Cursor cursor = db.query(CarTable.NAME, CarTable.ALL_COLUMNS, null,
					null, null, null, CarTable.COL_SUSPENDED + " ASC");

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Date suspended = null;
				if (!cursor.isNull(3)) {
					suspended = new Date(cursor.getLong(3));
				}
				cars.add(new Car(cursor.getInt(0), cursor.getString(1), cursor
						.getInt(2), suspended));
				cursor.moveToNext();
			}
			cursor.close();
		}

		return cars.toArray(new Car[cars.size()]);
	}

	public static int getCount() {
		int count;
		synchronized (Helper.dbLock) {
			SQLiteDatabase db = Helper.getInstance().getReadableDatabase();
			Cursor cursor = db.rawQuery(
					"SELECT count(*) FROM " + CarTable.NAME, null);
			cursor.moveToFirst();
			count = cursor.getInt(0);
			cursor.close();
		}
		return count;
	}
}
