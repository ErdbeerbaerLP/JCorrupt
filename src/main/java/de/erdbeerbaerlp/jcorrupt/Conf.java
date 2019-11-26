package de.erdbeerbaerlp.jcorrupt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class Conf
{
    static int rate = 100;
    static boolean protectHeaders = true;
    private static JsonObject o;
    
    public static void initJson() {
    }
    
    public static boolean isProtectedOffset(long currentOffset, final String ending, final long fileLength) {
        if (!protectHeaders) return false;
        if (o.has(ending) && o.get(ending).isJsonArray()) {
            final JsonArray a = o.getAsJsonArray(ending);
            for (int i = 0 ; i < a.size() ; i++) {
                if (a.get(i).isJsonObject()) {
                    final JsonObject ob = a.get(i).getAsJsonObject();
                    final long maxSecuredArea = convertByteString(ob.get("end").getAsString(), fileLength);
                    final long minSecuredArea = convertByteString(ob.get("start").getAsString(), fileLength);
                    if (currentOffset <= maxSecuredArea && currentOffset >= minSecuredArea) {
                        System.out.println(currentOffset + " protected");
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static long convertByteString(final String str, final long fileLength) {
        if (str.startsWith("-0x")) {
            return fileLength - Long.decode(str.replace("-", ""));
        }
        else {
            return Long.decode(str);
        }
    }
}
