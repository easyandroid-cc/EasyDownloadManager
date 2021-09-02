package cc.easyandroid.providers.downloads;

import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;


/**
 * Tools for managing files.  Not for public consumption.
 * @hide
 */
public class FileUtils{


    public static int setPermissions(String path, int mode, int uid, int gid) {
        try {
            Os.chmod(path, mode);
        } catch (ErrnoException e) {
            Log.w("FileUtils", "Failed to chmod(" + path + "): " + e);
            return e.errno;
        }

        if (uid >= 0 || gid >= 0) {
            try {
                Os.chown(path, uid, gid);
            } catch (ErrnoException e) {
                Log.w("FileUtils", "Failed to chown(" + path + "): " + e);
                return e.errno;
            }
        }

        return 0;
    }

}
