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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Log;
import de.greenrobot.event.EventBus;
import fr.core.android.ensanotes.MainActivity;
import fr.core.android.ensanotes.R;
import fr.core.android.ensanotes.SettingsActivity;
import fr.core.android.ensanotes.GmailActivity;
import fr.core.android.ensanotes.async.bus.NavigationUpdatedEvent;
import fr.core.android.ensanotes.db.DbHelper;
import fr.core.android.ensanotes.models.Category;
import fr.core.android.ensanotes.models.ONStyle;
import fr.core.android.ensanotes.models.adapters.NavDrawerCategoryAdapter;
import fr.core.android.ensanotes.models.views.NonScrollableListView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Arrays;

public class CategoryMenuTask extends AsyncTask<Void, Void, List<Category>> {

    private final WeakReference<Fragment> mFragmentWeakReference;
    private final MainActivity mainActivity;
    private NonScrollableListView mDrawerCategoriesList;
    private View settingsView;
    private View settingsViewCat;
    private View gmailView;
    private View gmailViewCat;

    private View contactView;
    private View contactViewCat;


    private NonScrollableListView mDrawerList;


    public CategoryMenuTask(Fragment mFragment) {
        mFragmentWeakReference = new WeakReference<>(mFragment);
        this.mainActivity = (MainActivity) mFragment.getActivity();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDrawerList = (NonScrollableListView) mainActivity.findViewById(R.id.drawer_nav_list);
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        settingsView = mainActivity.findViewById(R.id.settings_view);
        gmailView = mainActivity.findViewById(R.id.gmail_view);
        // Settings view when categories are available
        mDrawerCategoriesList = (NonScrollableListView) mainActivity.findViewById(R.id.drawer_tag_list);
        if (mDrawerCategoriesList.getAdapter() == null && mDrawerCategoriesList.getFooterViewsCount() == 0) {
            gmailViewCat = inflater.inflate(R.layout.drawer_category_list_gmail, null);
            mDrawerCategoriesList.addFooterView(gmailViewCat);

            contactViewCat = inflater.inflate(R.layout.drawer_category_list_contact, null);
            mDrawerCategoriesList.addFooterView(contactViewCat);

            settingsViewCat = inflater.inflate(R.layout.drawer_category_list_footer, null);
            mDrawerCategoriesList.addFooterView(settingsViewCat);
        } else {
            gmailViewCat = mDrawerCategoriesList.getChildAt(mDrawerCategoriesList.getChildCount() - 3);
            contactViewCat = mDrawerCategoriesList.getChildAt(mDrawerCategoriesList.getChildCount() - 2);
            settingsViewCat = mDrawerCategoriesList.getChildAt(mDrawerCategoriesList.getChildCount() - 1);
        }

    }


    @Override
    protected List<Category> doInBackground(Void... params) {
        if (isAlive()) {
            return buildCategoryMenu();
        } else {
            cancel(true);
            return null;
        }
    }


    @Override
    protected void onPostExecute(final List<Category> categories) {
        if (isAlive()) {
            mDrawerCategoriesList.setAdapter(new NavDrawerCategoryAdapter(mainActivity, categories,
                    mainActivity.getNavigationTmp()));
            if (categories.size() == 0) {
                setWidgetVisibility(settingsViewCat, false);
                setWidgetVisibility(settingsView, true);
                setWidgetVisibility(gmailViewCat, false);
                setWidgetVisibility(gmailView, true);
                setWidgetVisibility(contactViewCat, false);
                setWidgetVisibility(contactView, true);
            } else {
                setWidgetVisibility(settingsViewCat, true);
                setWidgetVisibility(settingsView, false);
                setWidgetVisibility(gmailViewCat, true);
                setWidgetVisibility(gmailView, false);
                setWidgetVisibility(contactViewCat, true);
                setWidgetVisibility(contactView, false);
            }
            mDrawerCategoriesList.justifyListViewHeightBasedOnChildren();
        }
    }


    private void setWidgetVisibility(View view, boolean visible) {
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }


    private boolean StaticExceptions(String nav_tab) {

        String[] mNavigationException = mainActivity.getResources().getStringArray(R.array.navigation_list_static);
        boolean skippable = Arrays.asList(mNavigationException).contains(nav_tab);
        String bool_skippable = Boolean.toString(skippable);
        String mNavigationArray_strx = Arrays.toString(mNavigationException);
        Log.e("IneedNoPermission", "StaticExceptions 1 : " + bool_skippable + " : " + mNavigationArray_strx);

        return skippable;

    }


    private boolean isAlive() {
        return mFragmentWeakReference.get() != null
                && mFragmentWeakReference.get().isAdded()
                && mFragmentWeakReference.get().getActivity() != null
                && !mFragmentWeakReference.get().getActivity().isFinishing();
    }


    private List<Category> buildCategoryMenu() {
        // Retrieves data to fill tags list
        List<Category> categories = DbHelper.getInstance().getCategories();

        View settings = categories.isEmpty() ? settingsView : settingsViewCat;
        if (settings == null) return categories;
//        Fonts.overrideTextSize(mainActivity,
//                mainActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS),
//                settings);
        settings.setOnClickListener(v -> {
            Log.d("Short x1", "75475412");
			Intent settingsIntent = new Intent(mainActivity, SettingsActivity.class);
			mainActivity.startActivity(settingsIntent);
		});


        View gmailEvent = categories.isEmpty() ? gmailView : gmailViewCat;
        if (gmailEvent == null) return categories;
//        Fonts.overrideTextSize(mainActivity,
//                mainActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS),
//                settings);
        gmailEvent.setOnClickListener(v -> {
            Log.d("Short x12", "123452345");
            Intent settingsIntent2 = new Intent(mainActivity, GmailActivity.class);
            mainActivity.startActivity(settingsIntent2);
        });


        View contactEvent = categories.isEmpty() ? contactView : contactViewCat;
        if (contactEvent == null) return categories;
        
        contactEvent.setOnClickListener(v -> {
            Log.d("Short NULL x01", "67587643");
            try {
                Intent i = mainActivity.getPackageManager().getLaunchIntentForPackage("com.google.android.contacts");
                mainActivity.startActivity(i);
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
        });


        // Sets click events
        mDrawerCategoriesList.setOnItemClickListener((arg0, arg1, position, arg3) -> {

			Object item = mDrawerCategoriesList.getAdapter().getItem(position);
			if (mainActivity.updateNavigation(String.valueOf(((Category) item).getId()))) {
                Log.d("mamak 9e7ba", String.valueOf(((Category) item).getId()));
                mDrawerCategoriesList.setItemChecked(position, true);
                // Forces redraw
                if (mDrawerList != null) {
                    mDrawerList.setItemChecked(0, false);
                    EventBus.getDefault().post(new NavigationUpdatedEvent(mDrawerCategoriesList.getItemAtPosition
                            (position)));
                }
			}
		});

        // Sets long click events
        mDrawerCategoriesList.setOnItemLongClickListener((arg0, view, position, arg3) -> {
			if (mDrawerCategoriesList.getAdapter() != null) {
				Object item = mDrawerCategoriesList.getAdapter().getItem(position);
				// Ensuring that clicked item is not the ListView header
                Log.d("Man on a mission : ", String.valueOf(((Category) item).getName()));
				if (item != null && !StaticExceptions(String.valueOf(((Category) item).getName()))) {
					mainActivity.editTag((Category) item);
				}
			} else {
				mainActivity.showMessage(R.string.category_deleted, ONStyle.ALERT);
			}
			return true;
		});

        return categories;
    }

}
