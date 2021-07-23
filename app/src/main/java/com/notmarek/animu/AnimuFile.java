package com.notmarek.animu;


import android.net.Uri;

public class AnimuFile implements Comparable<AnimuFile> {

    private final String path;
    private final boolean isDir;
    public AnimuFile(String path) {
        this.path = path;
        this.isDir = false;
    }

    public AnimuFile(String path, boolean isDir) {
        this.path = path;
        this.isDir = isDir;
    }

    public boolean isDirectory() {
        return this.isDir;
    }

    public String getName() {
        String[] splitPath = this.path.split("/");
        if (splitPath.length > 0) {
            return splitPath[splitPath.length - 1];
        } else {
            return "Animu";
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
        System.out.println("niggrehjj"+parentPath);
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
