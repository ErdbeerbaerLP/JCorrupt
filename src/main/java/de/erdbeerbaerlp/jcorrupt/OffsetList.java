package de.erdbeerbaerlp.jcorrupt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;


public class OffsetList extends ArrayList<OffsetList.ProtectedOffsetArea>
{
    
    public static final OffsetList EMPTY = new OffsetList();
    private static final Gson g = new GsonBuilder().create();
    static final File jsonFile = new File(defaultDirectory() + "/JCorrupt/ProtectedFileAreas.json");
    
    private OffsetList() {
    }
    public OffsetList(final String extension, final long fileLength) throws FileNotFoundException {
        System.out.println(extension);
        final JsonObject o = g.fromJson(new FileReader(jsonFile), JsonObject.class);
        if (o.has(extension) && o.get(extension).isJsonArray()) {
            final JsonArray a = o.getAsJsonArray(extension);
            for (int i = 0 ; i < a.size() ; i++) {
                if (a.get(i).isJsonObject()) {
                    final JsonObject ob = a.get(i).getAsJsonObject();
                    final long maxSecuredArea = convertByteString(ob.get("end").getAsString(), fileLength);
                    final long minSecuredArea = convertByteString(ob.get("start").getAsString(), fileLength);
                    this.add(new ProtectedOffsetArea(minSecuredArea, maxSecuredArea));
                }
            }
        }
    }
    
    private static String defaultDirectory() {
        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")) return System.getenv("APPDATA");
        else if (OS.contains("MAC")) return System.getProperty("user.home") + "/Library/Application " + "Support";
        else if (OS.contains("NUX")) return System.getProperty("user.home");
        return System.getProperty("user.dir");
    }
    
    private static long convertByteString(final String str, final long fileLength) {
        if (str.startsWith("-0x")) {
            return fileLength - Long.decode(str.replace("-", ""));
        }
        else {
            return Long.decode(str);
        }
    }
    
    @Override
    public String toString() {
        String out = "OffsetList: [ ";
        for (final ProtectedOffsetArea a : this) {
            out = out + a.toString() + " ";
        }
        out = out + "]";
        return out;
    }
    
    public boolean isProtected(final long offset) {
        if (!Conf.protectHeaders || this.isEmpty()) return false;
        for (final ProtectedOffsetArea po : this) {
            if (po.isInArea(offset)) return true;
        }
        return false;
    }
    
    static class ProtectedOffsetArea
    {
        final long start, end;
        
        ProtectedOffsetArea(long start, long end) {
            this.start = start;
            this.end = end;
        }
        
        public boolean isInArea(final long offset) {
            return offset >= start && offset <= end;
        }
        
        @Override
        public String toString() {
            return "0x" + Long.toHexString(start) + "-0x" + Long.toHexString(end);
        }
    }
}
