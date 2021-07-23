package com.notmarek.mpv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.notmarek.animu.AnimuFile;
import com.notmarek.filepicker.FilePickerFragment;

public class MPVFilePickerFragment extends FilePickerFragment {

    private AnimuFile rootPath = new AnimuFile("/");

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {}

    @Override
    public void onClickCheckable(@NonNull View v, @NonNull FileViewHolder vh) {

        mListener.onFilePicked(vh.file);
    }

    @Override
    public boolean onLongClickCheckable(@NonNull View v, @NonNull DirViewHolder vh) {
        mListener.onDirPicked(vh.file);
        return true;
    }

    @NonNull
    @Override
    public AnimuFile getRoot() {
        return rootPath;
    }

    public void setRoot(@NonNull AnimuFile path) {
        rootPath = path;
    }

    public boolean isBackTop() {
        return mCurrentPath.isRoot();
    }

    private @NonNull String makeRelative(@NonNull String path) {
        String head = getRoot().toString();
        if (path.equals(head))
            return "";
        if (!head.endsWith("/"))
            head += "/";
        return path.startsWith(head) ? path.substring(head.length()) : path;
    }

    @Override
    public void onChangePath(AnimuFile file) {
        ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (file != null && bar != null)
            bar.setSubtitle(makeRelative(file.getPath()));
    }
}
