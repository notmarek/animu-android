package com.notmarek.animu;


import android.net.Uri;

import java.util.regex.Pattern;

public class AnimuFile implements Comparable<AnimuFile> {

    private final String path;
    private final boolean isDir;
    private final String name;
    private final boolean search;

    public AnimuFile(String path) {
        this.path = path;
        this.isDir = false;
        this.name = "";
        this.search = false;
    }

    public AnimuFile(String path, boolean isDir) {
        this.path = path;
        this.isDir = isDir;
        this.name = "";
        this.search = false;
    }

    public AnimuFile(String path, String name, boolean isDir) {
        this.path = path;
        this.name = name;
        this.isDir = isDir;
        this.search = false;
    }

    public AnimuFile(boolean search, String searchTerm) {
        this.path = "/Search";
        this.name = searchTerm;
        this.isDir = true;
        this.search = search;
    }

    public boolean isSearch() { return this.search; }

    public boolean isDirectory() {
        return this.isDir;
    }

    public String getFolderName() {
        String[] splitPath = this.path.split("/");
        if (splitPath.length > 0) {
            return splitPath[splitPath.length - 1];
        } else {
            return "Animu";
        }
    }

    public String getName() {
        if (this.name.length() > 0) {
            return this.name;
        } else {
            return this.getFolderName();
        }
    }

    public String getExtension() {
        if (isDirectory()) {
            return "";
        } else {
            String[] splitPath = this.path.split(Pattern.quote("."));
            return splitPath[splitPath.length - 1];
        }
    }

    public String getPath() {
        return this.path;
    }

    public boolean isRoot() {
        return this.path == "/";
    }

    public AnimuFile getParentFile() {
        Uri uri = Uri.parse(this.path);
        String lastPathSeg = uri.getLastPathSegment();
        String parentPath = this.path.replace("/"+lastPathSeg, "");
        if (parentPath.length() == 0) {
            parentPath = "/";
        }
        return new AnimuFile(parentPath, true);
    }

    public AnimuFile[] listFiles() {
        return new AnimuFile[3];
    }

    public boolean isHidden() {
        return false;
    }

    public int compareTo(AnimuFile animuFile) {
        return 0;
    }
}
