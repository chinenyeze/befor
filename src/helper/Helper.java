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

package helper;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

public class Helper {

    public static void btnHover(JButton btn) {
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void btnIconAndToLabel(JButton[] btns) {
        for (JButton btn : btns) {
            btn.setFocusPainted(false); //begin label
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setOpaque(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setIcon(new ImageIcon(Helper.class.getResource(btn.getName() + ".png"))); // icon
        }
    }

    public static void btnMnemonicAndToolTip(JFrame frame, JButton a, JButton b, JButton c, JButton e, JButton h, JButton i, JButton l,
            JButton m, JButton o, JButton p, JButton s, JButton t, JButton enter, JButton altEnter) {
        a.setMnemonic(KeyEvent.VK_A);
        b.setMnemonic(KeyEvent.VK_B);
        c.setMnemonic(KeyEvent.VK_C);
        e.setMnemonic(KeyEvent.VK_E);
        h.setMnemonic(KeyEvent.VK_H);
        i.setMnemonic(KeyEvent.VK_I);
        l.setMnemonic(KeyEvent.VK_L);
        m.setMnemonic(KeyEvent.VK_M);
        o.setMnemonic(KeyEvent.VK_O);
        p.setMnemonic(KeyEvent.VK_P);
        s.setMnemonic(KeyEvent.VK_S);
        t.setMnemonic(KeyEvent.VK_T);
        frame.getRootPane().setDefaultButton(enter); //Enter
        altEnter.setMnemonic(KeyEvent.VK_ENTER); //Alt + Enter
        //btnSave, btnHelp, btnBin tooltips
        a.setToolTipText("Save Settings");
        b.setToolTipText("Clear Output");
        h.setToolTipText("Help");
    }

    public static void btnToLabel(JButton btn) {
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void defaults(JFrame frame, JPanel[] panels) {
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Helper.class.getResource("iBeftigre.png")));
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setLocationRelativeTo(null);//centralise the frame window
        if (panels != null) {
            for (JPanel panel : panels) {
                if (panel != null) {
                    panel.setBackground(Color.WHITE);
                }
            }
        }
    }

    public static void display(JTextArea txtOutput, String text) {
        if (txtOutput != null) {
            txtOutput.setText(txtOutput.getText() + "\n" + text);
        } else {
            System.out.println(text); //for API
        }
    }

    public static void displayReset(JTextArea txtOutput) {
        txtOutput.setText("Output Terminal\n---------------");
    }

    public static void txtWrap(JTextArea txtOutput) {
        txtOutput.setWrapStyleWord(true); //enable word wrap
        txtOutput.setLineWrap(true); //enable line wrap
    }

    //Analysis helpers----------
    private static boolean inOnePercentRange(double value) {
        return value <= 1 && 1 >= value;
    }

    public static boolean inRange(int check, int range) {
        double from = range - (range * 5 / 100);
        double to = range + (range * 5 / 100);
        return from <= check && check <= to;
    }

    public static boolean inRange(double check, double range) {
        double from = range - (range * 5 / 100);
        double to = range + (range * 5 / 100);
        return from <= check && check <= to;
    }

    public static String isCongruent(double a, double b, double c, double d) {
        ArrayList<Boolean> array = new ArrayList<>();
        array.add(inOnePercentRange(a));
        array.add(inOnePercentRange(b));
        array.add(inOnePercentRange(c));
        array.add(inOnePercentRange(d));
        int count = 0;
        for (boolean value : array) {
            if (value == true) {
                count++;
            }
        }
        if (count >= 3) {
            return "similar system";
        }
        return "different system";
    }

    public static String lessOrMore(double value) {
        if (value > 0) {
            return String.format("%.2f", value) + "% more";
        }
        return String.format("%.2f", Math.abs(value)) + "% less";
    }

    public static double percentageIncrease(int oldvalue, int newvalue) {
        return (double) (newvalue - oldvalue) * 100 / oldvalue;
    }

    public static double percentageIncrease(long oldvalue, long newvalue) {
        return (double) (newvalue - oldvalue) * 100 / oldvalue;
    }

    public static double percentageIncrease(double oldvalue, double newvalue) {
        return (double) (newvalue - oldvalue) * 100 / oldvalue;
    }

    //for file filtering
    public static class ExtensionFileFilter extends FileFilter {

        public static final long KB = 1024;
        public static final long MB = 1024 * KB;
        public static final long GB = 1024 * MB;
        private List<String> extensions;
        private String description, prefix;
        private long max_size;

        public ExtensionFileFilter(String[] exts, String desc) {
            this(exts, desc, "", Long.MAX_VALUE);
        }

        public ExtensionFileFilter(String[] exts, String desc, String prefix, long size) {
            if (exts != null) {
                extensions = new ArrayList<String>();
                for (String ext : exts) {
                    extensions.add(ext.replace(".", "").trim().toLowerCase());
                }
            }
            // No else necessary as null extensions handled below.

            // Using inline if syntax, use input from desc or use
            // a default value.
            // Wrap with an if statement to default as well as
            // avoid NullPointerException when using trim().
            description = (desc != null) ? desc.trim() : "Custom File List";

            // since argument and local variable have same name,
            // we use this keyword to qualify our local variable.
            this.prefix = prefix.trim().toLowerCase();
            max_size = size;
        }

        // Handles which files are allowed by filter.
        @Override
        public boolean accept(File f) {
            // Allow directories to be seen.
            if (f.isDirectory()) {
                return true;
            }

            // exit if no extensions exist.
            if (extensions == null) {
                return false;
            }

            // Allows files with extensions specified to be seen.
            String filename = f.getName().toLowerCase();
            for (String ext : extensions) {
                if (filename.endsWith("." + ext)) {
                    return (filename.startsWith(prefix) && (f.length() <= max_size));
                }
            }

            // Otherwise file is not shown.
            return false;
        }

        // 'Files of Type' description
        @Override
        public String getDescription() {
            return description;
        }

        //Usage:
        /*JFileChooser chooser = new JFileChooser();
        // DOC, RTF, and TXT "article" files under 5MB.
        chooser.addChoosableFileFilter(new ExtensionFileFilter(new String[]{".DOC", ".RTF", ".TXT"}, "Text Documents (*.DOC|RTF|TXT)", "article", 5 * ExtensionFileFilter.MB));
        // CSV files of all size.
        chooser.addChoosableFileFilter(new ExtensionFileFilter(new String[]{".CSV"}, "Comma Delimited File (*.CSV)"));
        // PDF files under 1MB.
        chooser.addChoosableFileFilter(new ExtensionFileFilter(new String[]{".PDF"}, "Portable Document Format (*.PDF)", "", 1 * ExtensionFileFilter.MB));
        // Turn off 'All Files' capability of file chooser, so only our custom filter is used.
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.showSaveDialog(null);*/
    }
}
