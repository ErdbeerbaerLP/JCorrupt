package de.erdbeerbaerlp.jcorrupt;

public class JCorrupt
{
    public static final JCorruptWindow win = new JCorruptWindow();
    
    static final String JSON_URL = "https://raw.githubusercontent.com/ErdbeerbaerLP/JCorrupt/master/ProtectedFileAreas.json";
    public static void main(String[] args) {
        win.setVisible(true);
        Conf.initJson(JSON_URL);
    }
}
