package com.coral.calculator;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Where the films are. Their pictures (.mp4) and voice tracks (.m4a) are in the "films" asset pack (films/src/main/assets/films), not in res/raw, so that the base module
 * stays under Play's limit. A film is still found by its name (run_film_idol_tall_h_ja, run_clip_07), and the players still ask for it by a number: a resource
 * number if one is in res/raw (the paper-cut scene), and otherwise a negative number made here for the file in the pack.
 *
 * id(context, name): 0 if there is no such file, so that the callers' "is it there" checks stay as they were.
 * open(context, id): the file, opened as an AssetFileDescriptor: straight from the pack if it is stored uncompressed (the .mp4 and .m4a are), and otherwise copied
 * once to the cache and opened from there.
 */
final class FilmFiles {
    private static final List<String> PATHS = new ArrayList<>();
    private static Set<String> names;

    private FilmFiles() { }

    static synchronized int id(Context context, String name) {
        int resource = context.getResources().getIdentifier(name, "raw", context.getPackageName());
        if (resource != 0) return resource;
        if (names == null) {
            names = new HashSet<>();
            try {
                String[] listed = context.getAssets().list("films");
                if (listed != null) for (String listing : listed) names.add(listing);
            } catch (IOException ignored) {
                // none found: nothing is there
            }
        }
        for (String extension : new String[] {".mp4", ".m4a"}) {
            if (!names.contains(name + extension)) continue;
            String path = "films/" + name + extension;
            int at = PATHS.indexOf(path);
            if (at < 0) {
                PATHS.add(path);
                at = PATHS.size() - 1;
            }
            return -2 - at;
        }
        return 0;
    }

    static AssetFileDescriptor open(Context context, int id) throws IOException {
        if (id > 0) return context.getResources().openRawResourceFd(id);
        String path;
        synchronized (FilmFiles.class) {
            int at = -2 - id;
            if (at < 0 || at >= PATHS.size()) throw new FileNotFoundException("film " + id);
            path = PATHS.get(at);
        }
        try {
            return context.getAssets().openFd(path);
        } catch (FileNotFoundException compressed) {
            File file = new File(context.getCacheDir(), path.replace('/', '_'));
            if (!file.isFile() || file.length() == 0) {
                try (InputStream in = context.getAssets().open(path); OutputStream out = new FileOutputStream(file)) {
                    byte[] buffer = new byte[64 * 1024];
                    int n;
                    while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                }
            }
            return new AssetFileDescriptor(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), 0, file.length());
        }
    }
}
