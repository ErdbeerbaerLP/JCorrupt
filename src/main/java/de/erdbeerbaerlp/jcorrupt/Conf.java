package de.erdbeerbaerlp.jcorrupt;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class Conf
{
    static int rate = 100;
    static boolean protectHeaders = true;
    
    public static void initJson(final String jsonUrl) {
        try {
            final HttpsURLConnection ghUrl = (HttpsURLConnection) new URL(jsonUrl).openConnection();
            ghUrl.connect();
            final InputStream is = ghUrl.getInputStream();
            final File tmpFile = new File(OffsetList.jsonFile.getAbsolutePath() + ".tmp");
            if (!tmpFile.getParentFile().exists()) tmpFile.getParentFile().mkdirs();
            final FileOutputStream os = new FileOutputStream(tmpFile);
            byte[] buf = new byte[is.available()];
            is.read(buf);
            os.write(buf);
            is.close();
            os.close();
            ghUrl.disconnect();
            if (tmpFile.exists() && !tmpFile.isDirectory() && tmpFile.length() > 0) {
                OffsetList.jsonFile.delete();
                tmpFile.renameTo(OffsetList.jsonFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }
}
