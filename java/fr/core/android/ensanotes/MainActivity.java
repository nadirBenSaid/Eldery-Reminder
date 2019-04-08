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

package fr.core.android.ensanotes;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;



import android.support.v7.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import edu.emory.mathcs.backport.java.util.Arrays;
import fr.core.android.ensanotes.async.UpdateWidgetsTask;
import fr.core.android.ensanotes.async.UpdaterTask;
import fr.core.android.ensanotes.async.bus.PasswordRemovedEvent;
import fr.core.android.ensanotes.async.bus.SwitchFragmentEvent;
import fr.core.android.ensanotes.async.notes.NoteProcessorDelete;
import fr.core.android.ensanotes.db.DbHelper;
import fr.core.android.ensanotes.helpers.NotesHelper;
import fr.core.android.ensanotes.intro.IntroActivity;
import fr.core.android.ensanotes.models.Attachment;
import fr.core.android.ensanotes.models.Category;
import fr.core.android.ensanotes.models.Note;
import fr.core.android.ensanotes.models.ONStyle;
import fr.core.android.ensanotes.utils.Constants;
import fr.core.android.ensanotes.utils.PasswordHelper;
import fr.core.android.ensanotes.utils.SystemHelper;


public class MainActivity extends BaseActivity implements OnDateSetListener, OnTimeSetListener {

    @BindView(R.id.crouton_handle) ViewGroup croutonViewContainer;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;

    public final String FRAGMENT_DRAWER_TAG = "fragment_drawer";
    public final String FRAGMENT_LIST_TAG = "fragment_list";
    public final String FRAGMENT_DETAIL_TAG = "fragment_detail";
    public final String FRAGMENT_SKETCH_TAG = "fragment_sketch";
    private static boolean isPasswordAccepted = false;
    private FragmentManager mFragmentManager;
    public Uri sketchUri;
    Category category;
    ArrayList<String> tutorialList = new ArrayList<String>();

    private final static String URL = "http://muslimsalat.com/london/daily.json?key=e651c61f9837ee1244225330ba55a8e0&jsoncallback=";
    private int selectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setTheme(R.style.ensaNotesTheme_ApiSpec);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
		EventBus.getDefault().register(this);

        initUI();

        new FetchDataTask().execute(URL);

		if (IntroActivity.mustRun()) {
			startActivity(new Intent(this.getApplicationContext(), IntroActivity.class));
		}

        new UpdaterTask(this).execute();

        long[] cats_id = {2863245995L, 11453115051L, 11452115051L, 11451115051L, 11453125051L};
        bootCategories(cats_id[0], getString(R.string.medication), null);
        bootCategories(cats_id[1], getString(R.string.prayer), null);
        //bootPrayers();
    }

    private class FetchDataTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {

            InputStream inputStream = null;
            String result= null;
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(params[0]);

            try {

                HttpResponse response = client.execute(httpGet);
                inputStream = response.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null){
                    result = convertInputStreamToString(inputStream);
                    Log.i("App", "Data received:" +result);

                }
                else
                    result = "Failed to fetch data";

                return result;

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String dataFetched) {
            //parse the JSON data and then display
            parseJSON(dataFetched);
        }


        private String convertInputStreamToString(InputStream inputStream) throws IOException{
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }

        private void parseJSON(String data){
            long[] prayerids = {2463245990L, 11453115251L, 11452315058L, 13451115051L, 17453125051L, 17453125059L};
            try{
                JSONObject jsonMainNode = new JSONObject(data);
                bootPrayers(jsonMainNode, getString(R.string.fajr), prayerids[0]);
                bootPrayers(jsonMainNode, getString(R.string.shurooq), prayerids[1]);
                bootPrayers(jsonMainNode, getString(R.string.dhuhr), prayerids[2]);
                bootPrayers(jsonMainNode, getString(R.string.asr), prayerids[3]);
                bootPrayers(jsonMainNode, getString(R.string.maghrib), prayerids[4]);
                bootPrayers(jsonMainNode, getString(R.string.isha), prayerids[5]);
            }catch(Exception e){
                Log.i("JsonAppx", "Error parsing data " +e.getMessage());

            }
        }
    }

    public void bootCategories(Long id, String cat_name, String cat_desc) {
        //Random random_obj = new Random();
        //Long id = random_obj.nextLong();
        category = new Category();
        category.setId(id);
        category.setName(cat_name);
        category.setDescription(cat_desc);
        category.setColor(String.valueOf(getRandomPaletteColor()));
        // Saved to DB and new id or update result catched
        DbHelper db = DbHelper.getInstance();
        category = db.updateCategory(category);
    }

    public void bootPrayers(JSONObject jsonMainNode, String prayertitle, Long prayer_id) {

        Note note = new Note();
        note.set_id(prayer_id);
        note.setTitle(prayertitle);
        note.setContent("");
        long categoryId = 11453115051L;
        Category category = new Category(categoryId, "",
                "", "");
        note.setCategory(category);
        try {
            String useless = jsonMainNode.getJSONArray("items").getJSONObject(0).getString("date_for") + " " + jsonMainNode.getJSONArray("items").getJSONObject(0).getString(prayertitle);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d hh:mm a");
            Date parsedDate = dateFormat.parse(useless);
            Timestamp ts2 = new java.sql.Timestamp(parsedDate.getTime());
            //Timestamp ts2 = Timestamp.valueOf("2019-01-02 04:09:00.000");
            long tsTime2 = ts2.getTime();
            note.setAlarm(tsTime2);
            Log.d("PrayerString ", String.valueOf(tsTime2));
        } catch(Exception e) {
            Log.e("Error PrayerString ", "Error on receiving reminder", e);
        }

        DbHelper.getInstance().updateNote(note, true);

    }


    private int getRandomPaletteColor() {
        int[] paletteArray = getResources().getIntArray(R.array.material_colors);
        return paletteArray[new Random().nextInt((paletteArray.length))];
    }


	@Override
	protected void onResume() {
		super.onResume();
		if (isPasswordAccepted) {
			init();
		} else {
			checkPassword();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		EventBus.getDefault().unregister(this);
	}


	private void initUI() {
        Log.d(Constants.TAG, "fuck off 1");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


	/**
	 * This method starts the bootstrap chain.
	 */
	private void checkPassword() {
		if (prefs.getString(Constants.PREF_PASSWORD, null) != null
				&& prefs.getBoolean("settings_password_access", false)) {
            PasswordHelper.requestPassword(this, passwordConfirmed -> {
                switch (passwordConfirmed) {
                    case SUCCEED:
                        init();
                        break;
                    case FAIL:
                        finish();
                        break;
                    case RESTORE:
                        PasswordHelper.resetPassword(this);
                }
			});
        } else {
            init();
		}
	}


	public void onEvent(PasswordRemovedEvent passwordRemovedEvent) {
		showMessage(R.string.password_successfully_removed, ONStyle.ALERT);
		init();
	}


	private void init() {
        isPasswordAccepted = true;

		getFragmentManagerInstance();

		NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManagerInstance()
                .findFragmentById(R.id.navigation_drawer);
        if (mNavigationDrawerFragment == null) {
            FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
            fragmentTransaction.replace(R.id.navigation_drawer, new NavigationDrawerFragment(), 
                    FRAGMENT_DRAWER_TAG).commit();
        }

        if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_LIST_TAG) == null) {
            FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, new ListFragment(), FRAGMENT_LIST_TAG).commit();
        }

        handleIntents();
    }

	private FragmentManager getFragmentManagerInstance() {
		if (mFragmentManager == null) {
			mFragmentManager = getSupportFragmentManager();
		}
		return mFragmentManager;
	}

	@Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction() == null) {
            intent.setAction(Constants.ACTION_START_APP);
        }
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntents();
        Log.d(Constants.TAG, "onNewIntent");
    }


    public MenuItem getSearchMenuItem() {
        Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            return ((ListFragment) f).getSearchMenuItem();
        } else {
            return null;
        }
    }


    public void editTag(Category tag) {
        Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            ((ListFragment) f).editCategory(tag);
        }
    }


    public void initNotesList(Intent intent) {
        Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            ((ListFragment) f).toggleSearchLabel(false);
            ((ListFragment) f).initNotesList(intent);
        }
    }


    public void commitPending() {
        Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            ((ListFragment) f).commitPending();
        }
    }


    /**
     * Checks if allocated fragment is of the required type and then returns it or returns null
     */
    private Fragment checkFragmentInstance(int id, Object instanceClass) {
        Fragment result = null;
		Fragment fragment = getFragmentManagerInstance().findFragmentById(id);
		if (fragment!= null && instanceClass.equals(fragment.getClass())) {
			result = fragment;
		}
        return result;
    }


    /*
     * (non-Javadoc)
     * @see android.support.v7.app.ActionBarActivity#onBackPressed()
     *
     * Overrides the onBackPressed behavior for the attached fragments
     */
    public void onBackPressed() {

        // SketchFragment
		Fragment f = checkFragmentInstance(R.id.fragment_container, SketchFragment.class);
        if (f != null) {
            ((SketchFragment) f).save();

            // Removes forced portrait orientation for this fragment
            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

            getFragmentManagerInstance().popBackStack();
            return;
        }

        // DetailFragment
        f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
        if (f != null) {
            ((DetailFragment) f).goBack = true;
            ((DetailFragment) f).saveAndExit((DetailFragment) f);
            return;
        }

        // ListFragment
        f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            // Before exiting from app the navigation drawer is opened
            if (prefs.getBoolean("settings_navdrawer_on_exit", false) && getDrawerLayout() != null && 
                    !getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
                getDrawerLayout().openDrawer(GravityCompat.START);
            } else if (!prefs.getBoolean("settings_navdrawer_on_exit", false) && getDrawerLayout() != null && 
                    getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
                getDrawerLayout().closeDrawer(GravityCompat.START);
            } else {
                if (!((ListFragment)f).closeFab()) {
					isPasswordAccepted = false;
					super.onBackPressed();
                }
            }
            return;
        }
        super.onBackPressed();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("navigationTmp", navigationTmp);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Crouton.cancelAllCroutons();
    }


    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }


    public ActionBarDrawerToggle getDrawerToggle() {
        if (getFragmentManagerInstance().findFragmentById(R.id.navigation_drawer) != null) {
            return ((NavigationDrawerFragment) getFragmentManagerInstance().findFragmentById(R.id.navigation_drawer)).mDrawerToggle;
        } else {
            return null;
        }
    }


    /**
     * Finishes multiselection mode started by ListFragment
     */
    public void finishActionMode() {
        ListFragment fragment = (ListFragment) getFragmentManagerInstance().findFragmentByTag(FRAGMENT_LIST_TAG);
        if (fragment != null) {
            fragment.finishActionMode();
        }
    }


    Toolbar getToolbar() {
        return this.toolbar;
    }


    private void handleIntents() {
        Intent i = getIntent();
        Log.d(Constants.TAG, "fuck off 0");
        if (i.getAction() == null) return;

        if (Constants.ACTION_RESTART_APP.equals(i.getAction())) {
            SystemHelper.restartApp(getApplicationContext(), MainActivity.class);
        }

        if (receivedIntent(i)) {
            Note note = i.getParcelableExtra(Constants.INTENT_NOTE);
            if (note == null) {
                note = DbHelper.getInstance().getNote(i.getIntExtra(Constants.INTENT_KEY, 0));
            }
            // Checks if the same note is already opened to avoid to open again
            if (note != null && noteAlreadyOpened(note)) {
                return;
            }
            // Empty note instantiation
            if (note == null) {
                note = new Note();
            }
            switchToDetail(note);
            return;
        }

        if (Constants.ACTION_SEND_AND_EXIT.equals(i.getAction())) {
            Log.d(Constants.TAG, "fuck off");
            saveAndExit(i);
            return;
        }

        // Tag search
        if (Intent.ACTION_VIEW.equals(i.getAction())) {
            Log.d(Constants.TAG, "fuck off");
            switchToList();
            return;
        }

        // Home launcher shortcut widget
        if (Constants.ACTION_SHORTCUT_WIDGET.equals(i.getAction())) {
            Log.d(Constants.TAG, "fuck off");
            switchToDetail(new Note());
            return;
        }
    }


    /**
     * Used to perform a quick text-only note saving (eg. Tasker+Pushbullet)
     */
    private void saveAndExit(Intent i) {
        Note note = new Note();
        note.setTitle(i.getStringExtra(Intent.EXTRA_SUBJECT));
        note.setContent(i.getStringExtra(Intent.EXTRA_TEXT));
        DbHelper.getInstance().updateNote(note, true);
        showToast(getString(R.string.note_updated), Toast.LENGTH_SHORT);
        finish();
    }


    private boolean receivedIntent(Intent i) {
        return Constants.ACTION_SHORTCUT.equals(i.getAction())
                || Constants.ACTION_NOTIFICATION_CLICK.equals(i.getAction())
                || Constants.ACTION_WIDGET.equals(i.getAction())
                || Constants.ACTION_WIDGET_TAKE_PHOTO.equals(i.getAction())
                || ((Intent.ACTION_SEND.equals(i.getAction())
                || Intent.ACTION_SEND_MULTIPLE.equals(i.getAction())
                || Constants.INTENT_GOOGLE_NOW.equals(i.getAction()))
                && i.getType() != null)
                || i.getAction().contains(Constants.ACTION_NOTIFICATION_CLICK);
    }


    private boolean noteAlreadyOpened(Note note) {
        DetailFragment detailFragment = (DetailFragment) getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG);
        return detailFragment != null && NotesHelper.haveSameId(note, detailFragment.getCurrentNote());
    }


    public void switchToList() {
        Log.d(Constants.TAG, "fuck off 1x");
        FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
        animateTransition(transaction, TRANSITION_HORIZONTAL);
        ListFragment mListFragment = new ListFragment();
        transaction.replace(R.id.fragment_container, mListFragment, FRAGMENT_LIST_TAG).addToBackStack
                (FRAGMENT_DETAIL_TAG).commitAllowingStateLoss();
        if (getDrawerToggle() != null) {
            getDrawerToggle().setDrawerIndicatorEnabled(false);
        }
		getFragmentManagerInstance().getFragments();
        EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.PARENT));
    }


    public void switchToDetail(Note note) {
        Log.d(Constants.TAG, "SwitchFragmentEvent x001");
        FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
        animateTransition(transaction, TRANSITION_HORIZONTAL);
        DetailFragment mDetailFragment = new DetailFragment();
        Bundle b = new Bundle();
        b.putParcelable(Constants.INTENT_NOTE, note);
        mDetailFragment.setArguments(b);
        if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG) == null) {
            transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
                    .addToBackStack(FRAGMENT_LIST_TAG)
                    .commitAllowingStateLoss();
        } else {
			getFragmentManagerInstance().popBackStackImmediate();
            transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
                    .addToBackStack(FRAGMENT_DETAIL_TAG)
                    .commitAllowingStateLoss();
        }
    }


    /**
     * Notes sharing
     */
    public void shareNote(Note note) {

        String titleText = note.getTitle();

        String contentText = titleText
                + System.getProperty("line.separator")
                + note.getContent();


        Intent shareIntent = new Intent();
        // Prepare sharing intent with only text
        if (note.getAttachmentsList().size() == 0) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            // Intent with single image attachment
        } else if (note.getAttachmentsList().size() == 1) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType(note.getAttachmentsList().get(0).getMime_type());
            shareIntent.putExtra(Intent.EXTRA_STREAM, note.getAttachmentsList().get(0).getUri());

            // Intent with multiple images
        } else if (note.getAttachmentsList().size() > 1) {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> uris = new ArrayList<>();
            // A check to decide the mime type of attachments to share is done here
            HashMap<String, Boolean> mimeTypes = new HashMap<>();
            for (Attachment attachment : note.getAttachmentsList()) {
                uris.add(attachment.getUri());
                mimeTypes.put(attachment.getMime_type(), true);
            }
            // If many mime types are present a general type is assigned to intent
            if (mimeTypes.size() > 1) {
                shareIntent.setType("*/*");
            } else {
                shareIntent.setType((String) mimeTypes.keySet().toArray()[0]);
            }

            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, titleText);
        shareIntent.putExtra(Intent.EXTRA_TEXT, contentText);

        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_message_chooser)));
    }


    /**
     * Single note permanent deletion
     *
     * @param note Note to be deleted
     */
    public void deleteNote(Note note) {
        new NoteProcessorDelete(Arrays.asList(new Note[]{note})).process();
        BaseActivity.notifyAppWidgets(this);
        Log.d(Constants.TAG, "Deleted permanently note with id '" + note.get_id() + "'");
    }


    public void updateWidgets() {
        new UpdateWidgetsTask(getApplicationContext())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void showMessage(int messageId, Style style) {
        showMessage(getString(messageId), style);
    }


    public void showMessage(String message, Style style) {
        // ViewGroup used to show Crouton keeping compatibility with the new Toolbar
		runOnUiThread(() -> Crouton.makeText(this, message, style, croutonViewContainer).show());
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        DetailFragment f = (DetailFragment) getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG);
        if (f != null && f.isAdded()) {
            f.onTimeSetListener.onTimeSet(view, hourOfDay, minute);
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        DetailFragment f = (DetailFragment) getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG);
        if (f != null && f.isAdded() && f.onDateSetListener != null) {
            f.onDateSetListener.onDateSet(view, year, monthOfYear, dayOfMonth);
        }
    }
}
