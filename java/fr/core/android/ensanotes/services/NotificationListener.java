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

package fr.core.android.ensanotes.services;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import de.greenrobot.event.EventBus;
import fr.core.android.ensanotes.ensaNotes;
import fr.core.android.ensanotes.async.bus.NotificationRemovedEvent;
import fr.core.android.ensanotes.db.DbHelper;
import fr.core.android.ensanotes.models.Note;
import fr.core.android.ensanotes.utils.Constants;
import fr.core.android.ensanotes.utils.date.DateUtils;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {


	@Override
	public void onCreate() {
		super.onCreate();
		EventBus.getDefault().register(this);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}


	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		Log.d(Constants.TAG, "Notification posted for note: " + sbn.getId());
	}


	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		if (sbn.getPackageName().equals(getPackageName())) {
			EventBus.getDefault().post(new NotificationRemovedEvent(sbn));
			Log.d(Constants.TAG, "Notification removed for note: " + sbn.getId());
		}
	}


	public void onEventAsync(NotificationRemovedEvent event) {
		Long nodeId = Long.valueOf(event.statusBarNotification.getTag());
		Note note = DbHelper.getInstance().getNote(nodeId);
		if (!DateUtils.isFuture(note.getAlarm())) {
			DbHelper.getInstance().setReminderFired(nodeId, true);
		}
	}


	public static boolean isRunning() {

		ContentResolver contentResolver = ensaNotes.getAppContext().getContentResolver();
		String enabledNotificationListeners = Settings.Secure.getString(contentResolver,
				"enabled_notification_listeners");
		return enabledNotificationListeners != null && enabledNotificationListeners.contains(NotificationListener
				.class.getSimpleName());
	}

}
