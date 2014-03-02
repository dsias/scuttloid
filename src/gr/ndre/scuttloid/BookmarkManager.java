/**
 * Scuttloid - Semantic Scuttle Android Client
 * Copyright (C) 2013 Alexandre Gravel-Raymond
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gr.ndre.scuttloid;

import android.content.Context;
import android.content.SharedPreferences;

import gr.ndre.scuttloid.database.DatabaseConnection;

/**
 * Manages loading of bookmarks, locally and remote
 */
public class BookmarkManager implements ScuttleAPI.Callback, ScuttleAPI.CreateCallback, ScuttleAPI.BookmarksCallback, ScuttleAPI.DeleteCallback, ScuttleAPI.UpdateCallback {

    private ScuttleAPI scuttleAPI;
    private DatabaseConnection database;

    protected String url;
    protected String password;

    protected Callback callback;

    /**
     * Constructor injecting mandatory preferences
     */
    public BookmarkManager(SharedPreferences preferences, Callback manager_callback) {
        this.callback = manager_callback;
        this.scuttleAPI = new ScuttleAPI(preferences, this);
        this.database = new DatabaseConnection( callback.getContext() );
    }

    /**
     * Get bookmarks
     * TODO: load local bookmarks if available and up to date
     * TODO: maybe return local bookmarks immediately and remote bookmarks when received
     */
    public void getBookmarks() {
        scuttleAPI.getBookmarks();
    }

    /**
     * Received Bookmarks
     * TODO: store bookmarks locally
     * TODO: maybe, instead of callbacks, use listeners, that are called, when the bookmark content changes
     */
    @Override
    public void onBookmarksReceived(BookmarkContent bookmarks) {
        // return to callback
        ( (BookmarksCallback)callback ).onBookmarksReceived(bookmarks);

        // store bookmarks locally
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        database.setBookmarks(BookmarkContent.getShared());
                    }
                }
        );
    }

    /**
     * Create bookmark
     */
    public void createBookmark(BookmarkContent.Item item) {
        scuttleAPI.createBookmark(item);
    }

    /**
     * bookmark created
     * TODO: update local storage after creating bookmark
     */
    @Override
    public void onBookmarkCreated() {
        ( (CreateCallback)callback ).onBookmarkCreated();
    }

    /**
     * bookmark already exists, let the callback handle it
     */
    @Override
    public void onBookmarkExists() {
        ( (CreateCallback)callback ).onBookmarkExists();
    }

    /**
     * Update bookmark
     */
    public void updateBookmark(BookmarkContent.Item item) {
        scuttleAPI.updateBookmark(item);
    }

    /**
     *  Bookmark updated
     *  TODO: update local storage after updating bookmark
     */
    @Override
    public void onBookmarkUpdated() {
        ( (UpdateCallback)callback ).onBookmarkUpdated();
    }

    /**
     * Delete bookmark
     */
    public void deleteBookmark(BookmarkContent.Item item) {
        scuttleAPI.deleteBookmark(item);
    }

    /**
     * Bookmark deleted
     * TODO: update local storage after deleting bookmark
     */
    @Override
    public void onBookmarkDeleted() {
        ( (DeleteCallback)callback ).onBookmarkDeleted();
    }

    /**
     * Forward Errors to callback
     * TODO: maybe some errors should be handled here.
     */
    @Override
    public void onAPIError(String message) {
        this.callback.onManagerError(message);
    }

    /**
     * get the context from the callback
     */
    @Override
    public Context getContext() {
        return callback.getContext();
    }

    // Callback Interfaces

    public interface Callback {
        void onManagerError(String message);
        Context getContext();
    }

    public interface BookmarksCallback extends Callback {
        void onBookmarksReceived(BookmarkContent bookmarks);
    }

    public interface UpdateCallback extends Callback {
        void onBookmarkUpdated();
    }

    public interface CreateCallback extends Callback {
        void onBookmarkCreated();
        void onBookmarkExists();
    }

    public interface DeleteCallback extends Callback {
        void onBookmarkDeleted();
    }

}
