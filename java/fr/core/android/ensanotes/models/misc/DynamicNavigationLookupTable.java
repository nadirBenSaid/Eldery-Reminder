/*
 * Copyright (C) 2018 Bensaid Nadir (Bensaid.nadir@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.core.android.ensanotes.models.misc;


import android.util.Log;
import de.greenrobot.event.EventBus;
import fr.core.android.ensanotes.async.bus.DynamicNavigationReadyEvent;
import fr.core.android.ensanotes.async.bus.NotesUpdatedEvent;
import fr.core.android.ensanotes.db.DbHelper;
import fr.core.android.ensanotes.models.Note;
import fr.core.android.ensanotes.utils.Constants;

import java.util.List;


public class DynamicNavigationLookupTable {

	private static DynamicNavigationLookupTable instance;
	int archived;
	int trashed;
	int uncategorized;
	int reminders;


	private DynamicNavigationLookupTable() {
		EventBus.getDefault().register(this);
		update();
	}


	public static DynamicNavigationLookupTable getInstance() {
		if (instance == null) {
			instance = new DynamicNavigationLookupTable();
		}
		return instance;
	}


	public void update() {
		((Runnable) () -> {
			archived = trashed = uncategorized = reminders = 0;
			List<Note> notes = DbHelper.getInstance().getAllNotes(false);
			for (int i = 0; i < notes.size(); i++) {
				if (notes.get(i).isTrashed()) trashed++;
				else if (notes.get(i).isArchived()) archived++;
				else if (notes.get(i).getAlarm() != null) reminders++;
				if (notes.get(i).getCategory() == null || notes.get(i).getCategory().getId().equals(0L)) {
					uncategorized++;
				}
			}
			EventBus.getDefault().post(new DynamicNavigationReadyEvent());
			Log.d(Constants.TAG, "Dynamic menu finished counting items");
		}).run();
	}


	public void onEventAsync(NotesUpdatedEvent event) {
		update();
	}


	public int getArchived() {
		return archived;
	}


	public int getTrashed() {
		return trashed;
	}


	public int getReminders() {
		return reminders;
	}


	public int getUncategorized() {
		return uncategorized;
	}

}
