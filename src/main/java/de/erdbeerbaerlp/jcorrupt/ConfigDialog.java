/*
 * Created by JFormDesigner on Thu Nov 14 19:16:46 CET 2019
 */

package de.erdbeerbaerlp.jcorrupt;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;


/**
 * @author unknown
 */
public class ConfigDialog extends JDialog {
    public ConfigDialog(Window owner) {
        super(owner);
        initComponents();
        this.setSize(455, 320);
    }
    
    private void saveBtnActionPerformed(ActionEvent e) {
        try {
            spinner1.commitEdit();
        } catch (ParseException ignored) {
        }
        Conf.rate = (int) spinner1.getValue();
        Conf.protectHeaders = checkBox1.isSelected();
        this.setVisible(false);
    }
    
    @Override
    public void setVisible(boolean b) {
        if(b){
            spinner1.setValue(Conf.rate);
            checkBox1.setSelected(Conf.protectHeaders);
        }
        super.setVisible(b);
    }
    private void cancelBtnActionPerformed(ActionEvent e) {
        this.setVisible(false);
    }

    private void spinner1StateChanged(ChangeEvent e) {
        if((int)spinner1.getValue() >= Integer.MAX_VALUE) spinner1.setValue(Integer.MAX_VALUE-1);
        if((int)spinner1.getValue() < 0) spinner1.setValue(0);
    }
    
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        this.label2 = new JLabel();
        this.label3 = new JLabel();
        this.saveBtn = new JButton();
        this.cancelBtn = new JButton();
        this.spinner1 = new JSpinner();
        this.checkBox1 = new JCheckBox();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("JCorrupt Config");
        setName("this");
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- label2 ----
        this.label2.setText("CorruptionChance");
        this.label2.setName("label2");
        contentPane.add(this.label2);
        this.label2.setBounds(new Rectangle(new Point(80, 80), this.label2.getPreferredSize()));

        //---- label3 ----
        this.label3.setText("(Higher numbers = less likely, set 0 to corrupt every byte)");
        this.label3.setName("label3");
        contentPane.add(this.label3);
        this.label3.setBounds(new Rectangle(new Point(55, 110), this.label3.getPreferredSize()));

        //---- saveBtn ----
        this.saveBtn.setText("USE");
        this.saveBtn.setName("saveBtn");
        this.saveBtn.addActionListener(e -> saveBtnActionPerformed(e));
        contentPane.add(this.saveBtn);
        this.saveBtn.setBounds(new Rectangle(new Point(155, 245), this.saveBtn.getPreferredSize()));

        //---- cancelBtn ----
        this.cancelBtn.setText("CANCEL");
        this.cancelBtn.setName("cancelBtn");
        this.cancelBtn.addActionListener(e -> cancelBtnActionPerformed(e));
        contentPane.add(this.cancelBtn);
        this.cancelBtn.setBounds(new Rectangle(new Point(240, 245), this.cancelBtn.getPreferredSize()));

        //---- spinner1 ----
        this.spinner1.setName("spinner1");
        this.spinner1.addChangeListener(e -> spinner1StateChanged(e));
        contentPane.add(this.spinner1);
        this.spinner1.setBounds(190, 80, 75, this.spinner1.getPreferredSize().height);

        //---- checkBox1 ----
        this.checkBox1.setText("Prevent overwriting important stuff (like headers / footers)");
        this.checkBox1.setSelected(true);
        this.checkBox1.setName("checkBox1");
        contentPane.add(this.checkBox1);
        this.checkBox1.setBounds(new Rectangle(new Point(50, 155), this.checkBox1.getPreferredSize()));

        contentPane.setPreferredSize(new Dimension(455, 320));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JLabel label2;
    private JLabel label3;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JSpinner spinner1;
    private JCheckBox checkBox1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
