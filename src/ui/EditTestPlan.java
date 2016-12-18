/*
 * Copyright (c) 2016 Samuel Chinenyeze <sjchinenyeze@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ui;

import helper.Helper;
import collector.JmxReader;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Used text editor (not NetBeans) to create and adapt .Form file for 
 * RSyntaxTextArea lib to allow NetBeans to open design view.
 */
public class EditTestPlan extends JFrame {

    private final File testPlan = new File("files/TestPlan.jmx");

    public EditTestPlan() {
        initComponents();
        Helper.defaults(EditTestPlan.this, null);
        getRootPane().setDefaultButton(btnSave); //for default enter button
        try {
            FileReader fr = new FileReader(testPlan);
            try (BufferedReader br = new BufferedReader(fr)) {
                textArea.read(br, null);
            }
            textArea.requestFocus();
        } catch (Exception ex) {
            Helper.display(txtOutput, "Error with file.");
        }
    }

    private void btnSaveActionPerformed(ActionEvent evt) {
        try {
            new JmxReader(textArea.getText()).getServer();  //test that the server call is valid
            //save file
            FileWriter fileWritter = new FileWriter(testPlan, false);
            try (BufferedWriter bufferWritter = new BufferedWriter(fileWritter)) {
                bufferWritter.write(textArea.getText());
            }
            Helper.display(txtOutput, "Test Plan successfully saved.");
            this.dispose();
        } catch (Exception e) {
            Helper.display(txtOutput, "Error with Test Plan format, file not saved.");
        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        textArea.setCodeFoldingEnabled(true);
        textArea.setBackground(new Color(255, 255, 204));

        scrollPane = new RTextScrollPane(textArea);
        btnSave = new JButton();
        btnSave.setText("Save and Close");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Beftigre: Test Plan Editor");

        btnSave.addActionListener(this::btnSaveActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 138, Short.MAX_VALUE)
                        .addComponent(btnSave))
                .addComponent(scrollPane)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnSave)))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EditTestPlan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> {
            new EditTestPlan().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public JTextArea txtOutput;
    private JButton btnSave;
    private RTextScrollPane scrollPane;
    private RSyntaxTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
