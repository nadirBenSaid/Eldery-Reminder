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

package fr.core.android.ensanotes.async;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import fr.core.android.ensanotes.BaseActivity;
import fr.core.android.ensanotes.ensaNotes;
import fr.core.android.ensanotes.db.DbHelper;
import fr.core.android.ensanotes.models.Note;
import fr.core.android.ensanotes.utils.Constants;
import fr.core.android.ensanotes.utils.ReminderHelper;

import java.util.List;


public class AlarmRestoreOnRebootService extends IntentService {

	public AlarmRestoreOnRebootService() {
		super("AlarmRestoreOnRebootService");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		Log.i(Constants.TAG, "System rebooted: service refreshing reminders");
		Context mContext = getApplicationContext();

		BaseActivity.notifyAppWidgets(mContext);

		List<Note> notes = DbHelper.getInstance().getNotesWithReminderNotFired();
		Log.d(Constants.TAG, "Found " + notes.size() + " reminders");
		for (Note note : notes) {
			ReminderHelper.addReminder(ensaNotes.getAppContext(), note);
		}
	}

}
