package com.notmarek.mpv

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.notmarek.animu.AnimuApi
import com.notmarek.animu.AnimuFile
import com.notmarek.filepicker.AbstractFilePickerFragment
import com.notmarek.mpv.config.SettingsActivity
import java.io.FileFilter
import java.net.URLEncoder

class MainActivity : AppCompatActivity(), AbstractFilePickerFragment.OnFilePickedListener {

    private var fragment: MPVFilePickerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the system UI to act as if the nav bar is hidden, so that we can
        // draw behind it. STABLE flag is historically recommended but was
        // deprecated in API level 30, so probably not strictly necessary, but
        // cargo-culting is fun.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val updateInfo = AnimuApi("").updateInfo()
        if (BuildConfig.VERSION_CODE != updateInfo.getInt("version_code")) {
            with(AlertDialog.Builder(this)) {
                setTitle("Update available")
                setPositiveButton("Download") { dialog, _ ->
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.getString("apk_url")))
                    startActivity(browserIntent)
                }
                setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                    dialog.cancel()
                }
                show()
            }
        }

        val data: Uri? = intent?.data;
        if (data != null) {
            if (data.toString().contains("animu.notmarek")) {
                val url: String = data.toString().replace("animu://", "https://")
                this.playFile(url)
            } else if (data.toString().contains("save.token")) {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("animu_token", data.toString().replace("animu://save.token/", ""))
                    .commit()
            }
        }

        fragment =
            supportFragmentManager.findFragmentById(R.id.file_picker_fragment) as MPVFilePickerFragment

        // With the app acting as if the navbar is hidden, we need to
        // account for it outselves. We want the recycler to directly
        // take the system UI padding so that we can tell it to draw
        // into the padded area while still respecting the padding for
        // input.
        val layout: RelativeLayout = findViewById(R.id.main_layout)
        val recycler: RecyclerView = layout.findViewById(android.R.id.list)
        recycler.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
            insets
        }

        supportActionBar?.setTitle(R.string.mpv_activity)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (sharedPrefs.getBoolean("${localClassName}_filter_state", false)) {
            (fragment as MPVFilePickerFragment).filterPredicate = MEDIA_FILE_FILTER
        }

        // TODO: rework or remove this setting
        val defaultPathStr = sharedPrefs.getString(
            "default_file_manager_path",
            Environment.getExternalStorageDirectory().path
        )
        val defaultPath = AnimuFile("/")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // check that the preferred path is inside a storage volume
//            val vols = Utils.getStorageVolumes(this)
//            val vol = vols.find { defaultPath.startsWith(it.path) }
//            if (vol == null) {
//                // looks like it wasn't
//                Log.w(TAG, "default path set to $defaultPath but no such storage volume")
//                with (fragment as MPVFilePickerFragment) {
////                    root = vols.first().path
//                    root = AnimuFile("/");
//                    goToDir(AnimuFile("/"))
////                    goToDir(vols.first().path)
//                }
//            } else {
            with(fragment as MPVFilePickerFragment) {
//                    root = vol.path
//                    goToDir(defaultPath)
                root = AnimuFile("/");
                goToDir(AnimuFile("/"))

//                }
            }
        } else {
            // Old device: go to preferred path but don't restrict root
            (fragment as MPVFilePickerFragment).goToDir(defaultPath)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_request -> {
                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT

                with(AlertDialog.Builder(this)) {
                    setTitle(R.string.action_request)
                    setView(input)

                    setPositiveButton("Submit magnet") { dialog, _ ->
                        val result = AnimuApi(PreferenceManager.getDefaultSharedPreferences(context).getString("animu_token", "kek")).requestTorrent(input.text.toString())
                        dialog.dismiss()
                        Toast.makeText(context, result.getString("data"), Toast.LENGTH_SHORT).show()
                    }
                    setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    show()
                }
            }
            R.id.action_search -> {
                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT

                with(AlertDialog.Builder(this)) {
                    setTitle(R.string.action_search)
                    setView(input)

                    setPositiveButton(R.string.action_search) { dialog, _ ->
                        (fragment as MPVFilePickerFragment).goToDir(AnimuFile(true, input.text.toString()))

                    }
                    setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    show()
                }
            }
            R.id.action_open_url -> {
                // https://stackoverflow.com/questions/10903754/#answer-10904665
                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI

                with(AlertDialog.Builder(this)) {
                    setTitle(R.string.action_open_url)
                    setView(input)
                    setPositiveButton(R.string.dialog_ok) { dialog, _ ->
                        playFile(input.text.toString())
                    }
                    setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    show()
                }
            }
            R.id.action_settings -> {
                val i = Intent(this, SettingsActivity::class.java)
                startActivity(i)
                return true
            }
        }
        return false
    }

    private fun playFile(filepath: String) {
        val i = Intent(this, MPVActivity::class.java)
        i.putExtra("filepath", filepath)
        startActivity(i)
    }

    override fun onFilePicked(file: AnimuFile) {
        val token =
            PreferenceManager.getDefaultSharedPreferences(this).getString("animu_token", "");
        if (file.path.startsWith("http://") || file.path.startsWith("https://")) {
            playFile(file.path)
        } else {
            playFile(
                "https://animu.notmarek.com/1qweww45/" + file.path + "?t=" + URLEncoder.encode(
                    token
                )
            )
        }
    }

    override fun onDirPicked(dir: AnimuFile) {
        // mpv will play directories as playlist of all contained files
//        playFile(dir.absolutePath)
    }

    override fun onCancelled() {
    }

    override fun onBackPressed() {
        if (fragment!!.isBackTop) {
            super.onBackPressed()
        } else {
            fragment!!.goUp()
        }
    }

    companion object {
        private const val TAG = "mpv"

        private val MEDIA_FILE_FILTER = FileFilter { file ->
            if (file.isDirectory) {
                val contents: Array<String> = file.list() ?: arrayOf()
                // filter hidden files due to stuff like ".thumbnails"
                contents.filterNot { it.startsWith('.') }.any()
            } else {
                Utils.MEDIA_EXTENSIONS.contains(file.extension.toLowerCase())
            }
        }
    }
}
