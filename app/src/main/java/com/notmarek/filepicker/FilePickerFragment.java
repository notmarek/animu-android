/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.notmarek.filepicker;

import com.notmarek.animu.AnimuApi;
import com.notmarek.animu.AnimuFile;
import com.notmarek.mpv.R;

import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import android.preference.PreferenceManager;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ExecutionException;


/**
 * An implementation of the picker which allows you to select a file from the internal/external
 * storage (SD-card) on a device.
 */


public class FilePickerFragment extends AbstractFilePickerFragment<AnimuFile> {

    protected static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    protected boolean showHiddenItems = false;
    protected FileFilter filterPredicate = null;
    private AnimuFile mRequestedPath = null;

    public FilePickerFragment() {
    }

    /**
     * This method is used to dictate whether hidden files and folders should be shown or not
     *
     * @param showHiddenItems whether hidden items should be shown or not
     */
    public void showHiddenItems(boolean showHiddenItems){
        this.showHiddenItems = showHiddenItems;
    }

    /**
     * Returns if hidden items are shown or not
     *
     * @return true if hidden items are shown, otherwise false
     */

    public boolean areHiddenItemsShown(){
        return showHiddenItems;
    }

    /**
     * This method is used to set the filter that determines the files to be shown
     *
     * @param predicate filter implementation or null
     */
    public void setFilterPredicate(@Nullable FileFilter predicate) {
        this.filterPredicate = predicate;
        refresh(mCurrentPath);
    }

    /**
     * Returns the filter that determines the files to be shown
     *
     * @return filter implementation or null
     */
    public @Nullable FileFilter getFilterPredicate() {
        return this.filterPredicate;
    }

    /**
     * @return true if app has been granted permission to write to the SD-card.
     */
    @Override
    protected boolean hasPermission(@NonNull AnimuFile path) {
        return true;
    }

    /**
     * Request permission to write to the SD-card.
     */
    @Override
    protected void handlePermission(@NonNull AnimuFile path) {
//         Should we show an explanation?
//        if (shouldShowRequestPermissionRationale(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//             Explain to the user why we need permission
//        }

        return;
    }

    /**
     * This the method that gets notified when permission is granted/denied. By default,
     * a granted request will result in a refresh of the list.
     *
     * @param requestCode  the code you requested
     * @param permissions  array of permissions you requested. empty if process was cancelled.
     * @param grantResults results for requests. empty if process was cancelled.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // If arrays are empty, then process was cancelled
        if (permissions.length == 0) {
            // Treat this as a cancel press
            if (mListener != null) {
                mListener.onCancelled();
            }
        } else { // if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                // Do refresh
                if (mRequestedPath != null) {
                    refresh(mRequestedPath);
                }
            } else {
                Toast.makeText(getContext(), R.string.nnf_permission_external_write_denied,
                        Toast.LENGTH_SHORT).show();
                // Treat this as a cancel press
                if (mListener != null) {
                    mListener.onCancelled();
                }
            }
        }
    }

    /**
     * Return true if the path is a directory and not a file.
     *
     * @param path either a file or directory
     * @return true if path is a directory, false if file
     */
    @Override
    public boolean isDir(@NonNull final AnimuFile path) {
        return path.isDirectory();
    }

    /**
     * @param path either a file or directory
     * @return filename of path
     */
    @NonNull
    @Override
    public String getName(@NonNull AnimuFile path) {
        return path.getName();
    }

    /**
     * Return the path to the parent directory. Should return the root if
     * from is root.
     *
     * @param from either a file or directory
     * @return the parent directory
     */
    @NonNull
    @Override
    public AnimuFile getParent(@NonNull final AnimuFile from) {
        if (from.getPath().equals(getRoot().getPath())) {
            // Already at root, we can't go higher
            return from;
        } else if (from.getParentFile() != null) {
            return from.getParentFile();
        } else {
            return from;
        }
    }

    @NonNull
    @NotNull
    @Override
    public String getFullPath(@NonNull @NotNull AnimuFile path) {
        return path.getPath();
    }

    /**
     * Convert the path to the type used.
     *
     * @param path either a file or directory
     * @return File representation of the string path
     */
    @NonNull
    @Override
    public AnimuFile getPath(@NonNull final String path) {
        return new AnimuFile(path);
    }

    /**
     * @param path either a file or directory
     * @return the full path to the file
     */
//    @NonNull
//    @Override
//    public String getFullPath(@NonNull final File path) {
//        return path.getPath();
//    }

    /**
     * Get the root path.
     *
     * @return the highest allowed path, which is "/" by default
     */
    @NonNull
    @Override
    public AnimuFile getRoot() {
        return new AnimuFile("/");
    }

    /**
     * Convert the path to a URI for the return intent
     *
     * @param file either a file or directory
     * @return a Uri
     */
    @NonNull
    @Override
    public Uri toUri(@NonNull final AnimuFile file) {
        return Uri.parse(file.getPath());
    }

    /**
     * Get a loader that lists the Files in the current path,
     * and monitors changes.
     */
    @NonNull
    @Override
    public Loader<SortedList<AnimuFile>> getLoader() {
        return new AsyncTaskLoader<SortedList<AnimuFile>>(getActivity()) {


            @Override
            public SortedList<AnimuFile> loadInBackground() {
                AnimuFile[] listFiles = new AnimuFile[0];
                try {
                    JSONArray jar = new AnimuApi(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("animu_token", "kek")).getFilesFromPath(mCurrentPath.getPath());
                    listFiles = new AnimuFile[jar.length()];
                    for (int i=0; i < jar.length(); i++)
                    {
                        try {
                            JSONObject oneObject = jar.getJSONObject(i);
                            if (mCurrentPath.getPath() == "/") {
                                if (oneObject.getString("type").contains("directory")) {
                                    listFiles[i] = new AnimuFile("/" + oneObject.getString("name"), oneObject.getString("anime"), true);
                                } else {
                                    listFiles[i] = new AnimuFile(oneObject.getString("name"), oneObject.getString("anime"), false);
                                }
                            } else {

                                if (oneObject.getString("type").contains("directory")) {
                                    listFiles[i] = new AnimuFile(mCurrentPath.getPath() + "/" + oneObject.getString("name"), oneObject.getString("anime"), true);
                                } else {
                                    listFiles[i] = new AnimuFile(oneObject.getString("name"), oneObject.getString("anime"), false);
                                }
                            }
                        } catch (JSONException e) {
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final int initCap = listFiles == null ? 0 : listFiles.length;

                SortedList<AnimuFile> files = new SortedList<>(AnimuFile.class, new SortedListAdapterCallback<AnimuFile>(getDummyAdapter()) {
                    @Override
                    public int compare(AnimuFile o1, AnimuFile o2) {
                        return -1;
                    }

                    @Override
                    public boolean areContentsTheSame(AnimuFile oldItem, AnimuFile newItem) {
                        return false;
                    }

                    @Override
                    public boolean areItemsTheSame(AnimuFile item1, AnimuFile item2) {
                        return false;
                    }
                }, initCap);




                files.beginBatchedUpdates();
                if (listFiles != null) {
                    for (AnimuFile f : listFiles) {
                        if (f.isHidden() && !areHiddenItemsShown())
                            continue;

                        files.add(f);
                    }
                }
                files.endBatchedUpdates();

                return files;
            }

            /**
             * Handles a request to start the Loader.
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                // handle if directory does not exist. Fall back to root.
                if (mCurrentPath == null || !mCurrentPath.isDirectory()) {
                    mCurrentPath = getRoot();
                }

                // Start watching for changes
//                fileObserver = new FileObserver(mCurrentPath.getPath(),
//                        FileObserver.CREATE |
//                                FileObserver.DELETE
//                                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO
//                ) {
//
//                    @Override
//                    public void onEvent(int event, String path) {
//                        // Reload
//                        onContentChanged();
//                    }
//                };
//                fileObserver.startWatching();

                forceLoad();
            }

            /**
             * Handles a request to completely reset the Loader.
             */
            @Override
            protected void onReset() {
                super.onReset();

                // Stop watching
//                if (fileObserver != null) {
//                    fileObserver.stopWatching();
//                    fileObserver = null;
//                }
            }
        };
    }

    /**
     * Compare two files to determine their relative sort order. This follows the usual
     * comparison interface. Override to determine your own custom sort order.
     * <p/>
     * Default behaviour is to place directories before files, but sort them alphabetically
     * otherwise.
     *
     * @param lhs File on the "left-hand side"
     * @param rhs File on the "right-hand side"
     * @return -1 if if lhs should be placed before rhs, 0 if they are equal,
     * and 1 if rhs should be placed before lhs
     */
    protected int compareFiles(@NonNull File lhs, @NonNull File rhs) {
        if (lhs.isDirectory() && !rhs.isDirectory()) {
            return -1;
        } else if (rhs.isDirectory() && !lhs.isDirectory()) {
            return 1;
        } else {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }
}
