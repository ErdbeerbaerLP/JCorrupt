package de.erdbeerbaerlp.jcorrupt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class Corruptor
{
    
    private static final ArrayList<Thread> threadList = new ArrayList<>();
    private static int bytes = 0;
    private byte[] RAM = null;
    
    final File source;
    
    Corruptor(String src) {
        this.source = new File(src);
    }
    
    public boolean isLargeFile() {
        return this.source.length() > Integer.MAX_VALUE - 20;
    }
    
    public boolean fileExists() {
        return source.exists() && source.isFile();
    }
    
    public void loadFile() {
        try {
            JCorrupt.win.setCorruptedStatus(0, -1, "Loading file to memory...");
            RAM = new byte[(int) source.length()];
            final FileInputStream is = new FileInputStream(source);
            is.read(RAM);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public long advancedFileLoad(FileInputStream is) {
        JCorrupt.win.setCorruptedStatus(0, -1, "Loading file part to memory...");
        try {
            System.out.println(is.available());
            RAM = new byte[Math.min(is.available(), Integer.MAX_VALUE - 20)];
            return is.read(RAM);
        } catch (IOException | OutOfMemoryError e) {
            JCorrupt.win.setCorruptedStatus(0, -1, "Unable to load file: " + e.getMessage());
        }
        return -1;
    }
    
    public void startRandomByteCorruption() {
        if (RAM == null || RAM.length == 0) loadFile();
        bytes = 0;
        final Random r = new Random(System.currentTimeMillis());
        final Thread t = new Thread(() -> {
            for (int i = 0 ; i < RAM.length ; i++) {
                bytes++;
                if (Conf.rate == 0 || r.nextInt(Conf.rate) == 0) {
                    RAM[i] = (byte) r.nextInt(255);
                }
            }
        });
        t.setDaemon(true);
        t.setName("Corruption Thread");
        t.start();
        do {
            JCorrupt.win.setCorruptedStatus(bytes, (int) RAM.length, "Random Byte Corruption:");
        } while (t.isAlive());
        threadList.clear();
        //while(bytes < a*threads){win.setCorruptedStatus(bytes, (int) sourceFile.length());}
        JCorrupt.win.setCorruptedStatus(0, -1, "Random Byte Corruption done");
    }
    
    public void advancedSaveToFile(FileOutputStream os) {
        JCorrupt.win.setCorruptedStatus(0, -1, "Saving to file...");
        try {
            os.write(RAM);
            os.flush();
            RAM = null;
            JCorrupt.win.setCorruptedStatus(0, -1, "Successfully saved to file!");
        } catch (IOException e) {
            JCorrupt.win.setCorruptedStatus(0, -1, e.getMessage());
        }
    }
    
    public void saveToFile(String dest) {
        try {
            JCorrupt.win.setCorruptedStatus(0, -1, "Saving to file...");
            final File destFile = new File(dest);
            final FileOutputStream os = new FileOutputStream(destFile);
            //for(long i=0;i<400L;i++) os.write(new byte[10000000]);  //MAKE BIG FILE!
            os.write(RAM);
            os.close();
            JCorrupt.win.setCorruptedStatus(0, -1, "Successfully saved to file!");
        } catch (IOException e) {
            JCorrupt.win.setCorruptedStatus(0, -1, e.getMessage());
        }
        this.RAM = null;
    }
    
    
}
