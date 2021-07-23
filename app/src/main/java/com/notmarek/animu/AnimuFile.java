package com.notmarek.filepicker;



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
        String[] path = this.path.split("/");
        String parentPath = "";
        for (int i = 0; i < path.length - 1; i++) {
            parentPath = parentPath + "/" + path[i];
        }
        return new AnimuFile(parentPath);
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
