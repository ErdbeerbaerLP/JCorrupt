/*
 * Created by JFormDesigner on Thu Nov 07 19:05:32 CET 2019
 */

package de.erdbeerbaerlp.jcorrupt;

import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.nio.file.*;


/**
 * @author unknown
 */
public class JCorruptWindow extends JFrame
{
    
    final Thread usageThread;
    final ConfigDialog cfg = new ConfigDialog(this);
    //HDD Stuff
    
    
    long usableHDD = -1;
    long maxHDD = -1;
    long usedHDD = -1;
    long neededSize = -1;
    
    //------
    public JCorruptWindow() {
        initComponents();
        setSize(615, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        emulatorField.setEnabled(false);
        buttonEmu.setEnabled(false);
        usageThread = new Thread(() -> {
            while (true) {
                final Runtime runtime = Runtime.getRuntime();
                long maxMemory = runtime.maxMemory();
                long usedMem = runtime.totalMemory() - runtime.freeMemory();
                
                
                lblRam.setText("RAM: " + formatStorage(usedMem) + " / " + formatStorage(maxMemory));
                final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                lblCPU.setText("CPU: " + Math.round(osBean.getSystemCpuLoad() * 100) + "%");
                
                
                if (isValidPath(destFileField.getText())) {
                    final Path p = Paths.get(destFileField.getText()).getRoot();
                    if (p != null) {
                        final File destFile = new File(p.toString());
                        maxHDD = destFile.getTotalSpace();
                        usableHDD = destFile.getUsableSpace();
                        if (maxHDD < usableHDD) usedHDD = maxHDD;
                        else usedHDD = maxHDD - usableHDD;
                        neededSize = new File(sourceFileField.getText()).length();
                    }
                }
                lblHDD.setText("<html>" + (neededSize > usableHDD ? "<font color='red'>" : "") + "HDD" + ": " + (isValidPath(destFileField.getText()) ? (formatStorage(usedHDD) + " / " + formatStorage(
                        maxHDD)) + " Used</html>" : "?/?</html>"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        });
        usageThread.start();
    }
    
    public static boolean isValidPath(String path) {
        if (path.isEmpty()) return false;
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }
    
    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;
    
    private String formatStorage(final long value) {
        final long[] dividers = new long[]{T, G, M, K, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
        if (value < 1) return "?";
        String result = null;
        for (int i = 0 ; i < dividers.length ; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }
    
    private static String format(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return String.format("%.1f %s", result, unit);
    }
    
    private void button1ActionPerformed(ActionEvent e) {
        final FileDialog d = new FileDialog(this, "Select File to Corrupt", FileDialog.LOAD);
        d.setVisible(true);
        final String filename = d.getFile();
        if (filename == null || filename.isEmpty()) return;
        sourceFileField.setText((d.getDirectory() != null ? d.getDirectory() : "") + filename);
        generateDest();
    }
    
    private void checkBox1StateChanged(ChangeEvent e) {
        final JCheckBox box = (JCheckBox) e.getSource();
        emulatorField.setEnabled(box.isSelected());
        buttonEmu.setEnabled(box.isSelected());
    }
    
    private void buttonDestFileActionPerformed(ActionEvent e) {
        final FileDialog d = new FileDialog(this, "Select location to save corrupted file to", FileDialog.SAVE);
        d.setVisible(true);
        if (d.getFile() != null && !d.getFile().isEmpty()) destFileField.setText((d.getDirectory() != null ? d.getDirectory() : "") + d.getFile());
    }
    
    private void buttonEmuActionPerformed(ActionEvent e) {
        final FileDialog d = new FileDialog(this, "Select emulator", FileDialog.LOAD);
        d.setVisible(true);
        if (!d.getFile().isEmpty()) {
            emulatorField.setText((d.getDirectory() != null ? d.getDirectory() : "") + d.getFile() + " %file%");
        }
    }
    
    private void showErrorMessage(final String title, final String message) {
        final Runnable runnable = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation");
        if (runnable != null) runnable.run();
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        this.toFront(); //For some reason the window minimizes when pressing OK
    }
    
    public static String getFileExtension(File path) {
        String extension = "";
        
        int i = path.getAbsolutePath().lastIndexOf('.');
        int p = Math.max(path.getAbsolutePath().lastIndexOf('/'), path.getAbsolutePath().lastIndexOf('\\'));
        
        if (i > p) {
            extension = path.getAbsolutePath().substring(i + 1);
        }
        return extension;
    }

    private void buttonStartActionPerformed(ActionEvent ev) {
        if (sourceFileField.getText().isEmpty()) {
            showErrorMessage("Error", "Can not corrupt nothing...");
            return;
        }
        try {
            final Corruptor c = new Corruptor(sourceFileField.getText(), destFileField.getText());
            if (usableHDD > -1 && c.length >= usableHDD) {
                showErrorMessage("Not enough Space!", "The target drive has not enough space! Choose a different drive or clean it up.");
                return;
            }
            this.setEnabled(false);
            final Thread t = new Thread(() -> {
                c.startRandomByteCorruption();
                try {
                    c.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Runtime.getRuntime().gc();
                if (startEmulatorBox.isSelected()) try {
                    final Process p = Runtime.getRuntime().exec(emulatorField.getText().replace("%file%", "\"" + destFileField.getText() + "\""));
                    p.waitFor();
                    progressBar1.setValue(0);
                    progressBar1.setMaximum(1);
                    this.requestFocus();
                    this.toFront();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            
                this.setEnabled(true);
            
            });
            t.setName("Corruptor Thread");
            t.setDaemon(true);
            t.start();
        } catch (FileNotFoundException e) {
            showErrorMessage("Missing File", "Can not find the source file");
        } catch (FileAlreadyExistsException e) {
            showErrorMessage("Src = Dst", "Source and destination are the same");
        } catch (AccessDeniedException e) {
            if (e.getFile().equals(new File(destFileField.getText()).getAbsolutePath())) {
                showErrorMessage("Write Error!", "Can not write to Destination!");
            }
            else {
                showErrorMessage("Read Error!", "Can not read from Source!");
            }
        }
    }
    
    public void setCorruptedStatus(long current, long max, String prefix) {
        setCorruptedStatus((int) (((double) current / (double) max) * 1000000));
        if (max == -1) {
            label3.setText(prefix);
        }
        else {
            label3.setText(prefix + " " + formatStorage(current) + "/" + formatStorage(max) + "  corrupted");
        }
    }
    
    private void thisWindowClosing(WindowEvent e) {
        this.usageThread.interrupt();
    }
    
    private void configButtonActionPerformed(ActionEvent e) {
        cfg.setVisible(true);
    }
    
    private void btnGenerateLocationActionPerformed(ActionEvent e) {
        generateDest();
    }
    
    private void setCorruptedStatus(int current) {
        progressBar1.setValue(current);
        progressBar1.setMaximum(1000000);
        
    }
    
    private void generateDest() {
        if (!sourceFileField.getText().isEmpty()) {
            final File srcFile = new File(sourceFileField.getText());
            if (!srcFile.exists()) return;
            destFileField.setText(srcFile.getParentFile().getAbsolutePath() + File.separator + srcFile.getName().replace("." + getFileExtension(srcFile), "") + ".corrupted." + getFileExtension(srcFile));
        }
    }
    
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JTextField sourceFileField;
    private JButton buttonSrcFile;
    private JLabel label1;
    private JTextField destFileField;
    private JButton buttonDestFile;
    private JLabel label2;
    private JButton buttonStart;
    private JTextField emulatorField;
    private JButton buttonEmu;
    private JCheckBox startEmulatorBox;
    private JProgressBar progressBar1;
    private JLabel label3;
    private JButton configButton;
    private JLabel lblRam;
    private JLabel lblCPU;
    private JLabel lblHDD;
    private JButton btnGenerateLocation;
    
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        this.sourceFileField = new JTextField();
        this.buttonSrcFile = new JButton();
        this.label1 = new JLabel();
        this.destFileField = new JTextField();
        this.buttonDestFile = new JButton();
        this.label2 = new JLabel();
        this.buttonStart = new JButton();
        this.emulatorField = new JTextField();
        this.buttonEmu = new JButton();
        this.startEmulatorBox = new JCheckBox();
        this.progressBar1 = new JProgressBar();
        this.label3 = new JLabel();
        this.configButton = new JButton();
        this.lblRam = new JLabel();
        this.lblCPU = new JLabel();
        this.lblHDD = new JLabel();
        this.btnGenerateLocation = new JButton();

        //======== this ========
        setTitle("JCorrupt");
        setName("this");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thisWindowClosing(e);
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- sourceFileField ----
        this.sourceFileField.setName("sourceFileField");
        contentPane.add(this.sourceFileField);
        this.sourceFileField.setBounds(10, 45, 250, 30);

        //---- buttonSrcFile ----
        this.buttonSrcFile.setText("Browse...");
        this.buttonSrcFile.setName("buttonSrcFile");
        this.buttonSrcFile.addActionListener(e -> button1ActionPerformed(e));
        contentPane.add(this.buttonSrcFile);
        this.buttonSrcFile.setBounds(260, 45, 120, 29);

        //---- label1 ----
        this.label1.setText("File to Corrupt:");
        this.label1.setName("label1");
        contentPane.add(this.label1);
        this.label1.setBounds(15, 25, 200, this.label1.getPreferredSize().height);

        //---- destFileField ----
        this.destFileField.setName("destFileField");
        contentPane.add(this.destFileField);
        this.destFileField.setBounds(10, 105, 250, 30);

        //---- buttonDestFile ----
        this.buttonDestFile.setText("Browse...");
        this.buttonDestFile.setName("buttonDestFile");
        this.buttonDestFile.addActionListener(e -> buttonDestFileActionPerformed(e));
        contentPane.add(this.buttonDestFile);
        this.buttonDestFile.setBounds(260, 105, 120, 29);

        //---- label2 ----
        this.label2.setText("Corrupted file location");
        this.label2.setName("label2");
        contentPane.add(this.label2);
        this.label2.setBounds(15, 85, 200, 16);

        //---- buttonStart ----
        this.buttonStart.setText("Start Corruption");
        this.buttonStart.setName("buttonStart");
        this.buttonStart.addActionListener(e -> buttonStartActionPerformed(e));
        contentPane.add(this.buttonStart);
        this.buttonStart.setBounds(new Rectangle(new Point(205, 255), this.buttonStart.getPreferredSize()));

        //---- emulatorField ----
        this.emulatorField.setName("emulatorField");
        contentPane.add(this.emulatorField);
        this.emulatorField.setBounds(10, 190, 250, 30);

        //---- buttonEmu ----
        this.buttonEmu.setText("Browse...");
        this.buttonEmu.setName("buttonEmu");
        this.buttonEmu.addActionListener(e -> buttonEmuActionPerformed(e));
        contentPane.add(this.buttonEmu);
        this.buttonEmu.setBounds(260, 190, 120, 29);

        //---- startEmulatorBox ----
        this.startEmulatorBox.setText("Launch emulator/program after corruption (works with cmd commands)");
        this.startEmulatorBox.setName("startEmulatorBox");
        this.startEmulatorBox.addChangeListener(e -> checkBox1StateChanged(e));
        contentPane.add(this.startEmulatorBox);
        this.startEmulatorBox.setBounds(new Rectangle(new Point(20, 165), this.startEmulatorBox.getPreferredSize()));

        //---- progressBar1 ----
        this.progressBar1.setName("progressBar1");
        contentPane.add(this.progressBar1);
        this.progressBar1.setBounds(40, 285, 540, 20);

        //---- label3 ----
        this.label3.setText("0/0 bytes corrupted");
        this.label3.setHorizontalAlignment(SwingConstants.CENTER);
        this.label3.setName("label3");
        contentPane.add(this.label3);
        this.label3.setBounds(40, 305, 540, this.label3.getPreferredSize().height);

        //---- configButton ----
        this.configButton.setText("Config");
        this.configButton.setName("configButton");
        this.configButton.addActionListener(e -> configButtonActionPerformed(e));
        contentPane.add(this.configButton);
        this.configButton.setBounds(new Rectangle(new Point(390, 255), this.configButton.getPreferredSize()));

        //---- lblRam ----
        this.lblRam.setText("RAM: ?/?");
        this.lblRam.setName("lblRam");
        contentPane.add(this.lblRam);
        this.lblRam.setBounds(395, 15, 165, this.lblRam.getPreferredSize().height);

        //---- lblCPU ----
        this.lblCPU.setText("CPU: ?%");
        this.lblCPU.setName("lblCPU");
        contentPane.add(this.lblCPU);
        this.lblCPU.setBounds(395, 40, 165, 16);

        //---- lblHDD ----
        this.lblHDD.setText("HDD: ?/?");
        this.lblHDD.setName("lblHDD");
        contentPane.add(this.lblHDD);
        this.lblHDD.setBounds(395, 65, 215, 16);

        //---- btnGenerateLocation ----
        this.btnGenerateLocation.setText("Generate");
        this.btnGenerateLocation.setName("btnGenerateLocation");
        this.btnGenerateLocation.addActionListener(e -> btnGenerateLocationActionPerformed(e));
        contentPane.add(this.btnGenerateLocation);
        this.btnGenerateLocation.setBounds(385, 105, this.btnGenerateLocation.getPreferredSize().width, 29);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
