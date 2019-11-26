package de.erdbeerbaerlp.jcorrupt;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Random;


public class Corruptor implements AutoCloseable
{
    final File source, destination;
    final long length;
    final String extension;
    final FileInputStream is;
    final FileOutputStream os;
    private OffsetList ol;
    byte[] buffer = new byte[4096];
    private long currentOffset = 0;
    
    Corruptor(String src, String dest) throws FileNotFoundException, FileAlreadyExistsException, AccessDeniedException {
        this.source = new File(src);
        if (!this.source.canRead()) throw new AccessDeniedException(source.getAbsolutePath());
        this.destination = new File(dest);
        //if (!this.destination.canWrite()) throw new AccessDeniedException(destination.getAbsolutePath());
        if (!Files.isWritable(destination.getParentFile().toPath()) && ((destination.exists() && Files.isWritable(destination.toPath())) || !destination.exists())) {
            throw new AccessDeniedException(destination.getAbsolutePath());
        }
        this.length = source.length();
        this.extension = JCorruptWindow.getFileExtension(source);
        try {
            ol = new OffsetList(extension, length);
            System.out.println(ol.toString());
        } catch (FileNotFoundException e) {
            ol = OffsetList.EMPTY;
        }
        if (this.source.getAbsolutePath().equals(this.destination.getAbsolutePath())) throw new FileAlreadyExistsException(dest);
        is = new FileInputStream(source);
        os = new FileOutputStream(destination);
    }
    
    public void startRandomByteCorruption() {
        currentOffset = 0;
        final Thread t = new Thread(() -> {
            final Random ra = new Random(System.currentTimeMillis());
            while (currentOffset < source.length()) {
                try {
                    int r;
                    while ((r = is.read(buffer)) >= 0) {
                        for (int i = 0 ; i < buffer.length ; i++) {
                            currentOffset++;
                            if (Conf.rate == 0 || ra.nextInt(Conf.rate) == 0) {
                                if (!isProtectedOffset()) buffer[i] = (byte) ra.nextInt(255);
                            }
                        }
                        os.write(buffer, 0, r);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.setName("Corruption Thread");
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
        while (t.isAlive()) {
            JCorrupt.win.setCorruptedStatus(currentOffset, source.length(), "Random Byte Corruption:");
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        JCorrupt.win.setCorruptedStatus(0, -1, "Random Byte Corruption done");
    }
    
    private boolean isProtectedOffset() {
        return ol.isProtected(currentOffset);
    }
    
    @Override
    public void close() throws Exception {
        is.close();
        os.close();
    }
}
