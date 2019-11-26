package de.erdbeerbaerlp.jcorrupt;

public class JCorrupt
{
    public static final JCorruptWindow win = new JCorruptWindow();
    
    public static void main(String[] args) {
        win.setVisible(true);
        Conf.initJson();
    }
}
