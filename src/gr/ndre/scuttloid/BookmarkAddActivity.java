package gr.ndre.scuttloid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class BookmarkAddActivity extends Activity implements OnClickListener, ScuttleAPI.CreateCallback {

	/**
	 * The bookmark content this activity is editing.
	 */
	private BookmarkContent.Item item;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmark_add);
		
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Setup the Privacy (status) spinner.
		Spinner spinner = (Spinner) findViewById(R.id.status);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.status_options,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		// Handle when the user presses the save button.
		Button btnSave = (Button)findViewById(R.id.save_button);
		btnSave.setOnClickListener(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		EditText field_url = (EditText)findViewById(R.id.url);
		EditText field_title = (EditText)findViewById(R.id.title);
		String url = field_url.getText().toString();
		String title = field_title.getText().toString();
		String description = ((EditText)findViewById(R.id.description)).getText().toString(); 
		String tags = ((EditText)findViewById(R.id.tags)).getText().toString();
		String status = String.valueOf(((Spinner)findViewById(R.id.status)).getSelectedItemPosition());
		
		boolean error = false;
		if (title.trim().equals("")) {
			field_title.setError(getString(R.string.error_titlerequired));
			error = true;
		}
		if (url.trim().equals("")) {
			field_url.setError(getString(R.string.error_urlrequired));
			error = true;
		}
		if (!error) {
			item = new BookmarkContent.Item();
			String fixed_url = URLUtil.guessUrl(url);
			if (fixed_url.endsWith("/")) {
				fixed_url = fixed_url.substring(0, fixed_url.length() - 1);
			}
			item.url = fixed_url;
			item.title = title;
			item.description = description;
			item.tags = tags;
			item.status = status;
			
			// Save the bookmark
			ScuttleAPI api = new ScuttleAPI(this.getGlobalPreferences(), this);
			api.createBookmark(item);
		}
	}
	
	protected SharedPreferences getGlobalPreferences() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
		return preferences;
	}

	@Override
	public void onAPIError(String message) {
	    AlertDialog alert = new AlertDialog.Builder(this).create();
	    alert.setMessage(message);  
	    alert.show();
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void onBookmarkCreated() {
		BookmarkContent.getShared().addItemToTop(item);
		Toast.makeText(this, getString(R.string.bookmark_created), Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	public void onBookmarkExists() {
		Toast.makeText(this, getString(R.string.error_bookmarkexists), Toast.LENGTH_SHORT).show();
		
		Integer position = BookmarkContent.getShared().getPosition(item.url);
		if (position != -1) {
			Intent intent = new Intent(this, BookmarkEditActivity.class);
			intent.putExtra(BookmarkDetailActivity.ARG_ITEM_POS, position);
			startActivity(intent);
			finish();
		}
	}

}