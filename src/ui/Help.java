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

public class Help extends javax.swing.JFrame {

    /**
     * Creates new form Help
     */
    public Help() {
        initComponents();
        Helper.defaults(Help.this, null);

    }

    private String header() {
        return "<html>"
                + "<style>td.b{border-right: 1px solid #000;} td.d{border-bottom: 1px solid #000;}</style>"
                + "<table>"
                + "<tr>"
                + "<td class='d'></td>"
                + "<td class='d'>Beftigre is a tool designed for evaluation of mobile cloud applications. "
                + "The tool is based on concepts from behaviour-driven development and full-tier evaluation.<br/><br/>"
                + "Samuel Chinenyeze<br/>"
                + "© 2016<br/><br/>"
                + "</td>"
                + "</tr>";
    }

    private String sMonitorHelp() {
        return "<tr>"
                + "<td class='b' valign='top'><b>Server Monitor</b></td>"
                + "<td>"
                + "Server Monitor makes use of <i>PerfMon Server Agent</i>.<br/>"
                + "Using Beftigre you can setup, start, stop or cleanup the serveragent on your EC2 server instance.<br/>"
                + "<u>Connection Parameters</u>: To perform any of the four mentioned operations, you require the Pem File, Host IP, Port number and User of your EC2 instance."
                + "The default Port is 22 and default User is ubuntu, but this can be changed where appropriate.<br/>"
                + "<u>Start</u>: button is used to launch the ServerAgent on default port 4444 of your EC2 instance. Processes started are for TCP and UDP connection.<br/>"
                + "<u>Stop</u>: button is used to stop the ServerAgent monitor processes.<br/>"
                + "<u>Setup</u>: button is used to download and install the ServerAgent into your EC2 instance. Setup is required the first time you plan to run the monitor, after one time setup you can subsequently use the start and stop buttons.<br/>"
                + "<u>Cleanup</u>: button is used to clean/delete all the ServerAgent files from you EC2 instance. This can be done if you intend to re-setup/re-install the monitor, or are done testing.<br/>"
                + "<u>Output Terminal</u>: displays log of all operations.<br/>"
                + "Shortcut Keys:<br/>"
                + "Alt+I: Install setup files<br/>"
                + "Alt+C: Cleanup files<br/>"
                + "Alt+S: Simulation Settings<br/>"
                + "Enter: Start server monitors<br/>"
                + "Alt+Enter: Stop server monitors<br/>"
                + "Alt+O: Offloader<br/>"
                + "</td>"
                + "</tr>";
    }

    private String mCollectorHelp() {
        return "<tr>"
                + "<td class='b' valign='top'><b>Metrics Collector</b></td>"
                + "<td>"
                + "Metrics Collector makes use of <i>PerfMon Metrics Collector</i>.<br/>"
                + "Using Beftigre you can configure and start a test. The test launched with Beftigre is used to perform random resource state simulation on the server.<br/>"
                + "<u>JMeter setup</u>: To perform the simulation test, Tou require jmeter setup on your system. Using 3 easy steps:"
                + "<ul style='margin:0px 0px 0px 20px; list-style-type: circle;'>"
                + "<li>Download (and extract) Apache JMeter binaries (zip or tgz) from http://jmeter.apache.org/download_jmeter.cgi</li>"
                + "<li>Copy/overwrite the provided <i>jmeter.properties</i> and <i>saveservice.properties</i> to \"[YOUR_JMETER_HOME]/bin\"</li>"
                + "<li>Under Jmeter Dir select [YOUR_JMETER_HOME] as your directory</li>"
                + "</ul>"
                + "<u>Edit .jmx Test Plan</u>: button is used to setup the test plan. Open the test plan and update lines 12, 17, 22 and 27 with your server IP address, port number, duration of metrics recording, and path (i.e. path to a dummy page on the server) respectively. "
                + "N.B. simulation path is the relative url to the simulation process on your server instance."
                + "And, port number is the port used to access the simulation process on your server instance.<br/>"
                + "<u>Start</u>: button is used to launch the test with metrics collector which collects monitor data (for CPU and Memory server resources) on default port 4444 from the Server Monitor process.<br/>"
                + "The test will automattically end when done (i.e. using the recommended default Test Plan settings).<br/>"
                + "<u>Output Terminal</u>: displays log of all operations.<br/>"
                + "Shortcut Keys:<br/>"
                + "Alt+T: Edit .jmx Test Plan<br/>"
                + "Alt+M: Start metrics collector<br/>"
                + "</td>"
                + "</tr>";
    }

    private String ftAnalyserHelp() {
        return "<tr>"
                + "<td class='b' valign='top'><b>Full-tier Analyser</b></td>"
                + "<td>"
                + "Full-tier Analyser makes use of <i>JavaPlot which is based on GNUPlot</i> and custom library built for PowerTutor log analysis.<br/>"
                + "Using Beftigre you can generate a results sunnary and graph for mobile-cloud evaluation.<br/>"
                + "<u>App Log</u>: This is the instumentation log file generated by the Duo class of P2I API on your mobile. Format of log file is AppLog_[timestamp].log<br/>"
                + "<u>Power Log</u>: This is the power log file generated by the Power Monitor of P2I API on your mobile. Format of log file is PowerLog_[timestamp].log<br/>"
                + "<u>Sim Log (mobile)</u>: This is the simulation log file generated by Sim class of P2I API on your mobile. Format of log file is SimLog_[timestamp].log<br/>"
                + "<u>Sim Log (server)</u>: This is the simulation log file generated by the Simulation process on your server.<br/>"
                + "<u>Resource Log</u>: This is the resource log file generated by the Perfmon Metrics Collector.<br/>"
                + "<u>Graph</u>: button is used to generate graph for the log files for full-tier evaluation.<br/>"
                + "<u>Results</u>: button is used to generate results for the log files for full-tier evaluation.<br/>"
                + "<u>Output Terminal</u>: displays log of all operations.<br/>"
                + "Shortcut Keys:<br/>"
                + "Alt+L: Select Logs<br/>"
                + "Alt+E: Extract Results<br/>"
                + "Alt+P: Plot<br/>"
                + "</td>"
                + "</tr>";
    }

    private String spacer() {
        return "<tr>"
                + "<td class='b' valign='top'></td>"
                + "<td>"
                + "</td>"
                + "</tr>";
    }

    private String footer() {
        return "</table>"
                + "</html>";
    }

    public void order() {
        setViewNotifier("displaying");
        String text = header() + sMonitorHelp() + spacer() + mCollectorHelp() + spacer() + ftAnalyserHelp() + footer();
        paneHelp.setContentType("text/html");
        paneHelp.setText(text);
        paneHelp.setCaretPosition(0);
    }

    //viewNotifier to ensure one help is opened at a time.
    public String getViewNotifier() {
        return viewNotifier;
    }

    public final void setViewNotifier(String viewNotifier) {
        this.viewNotifier = viewNotifier;
    }

    @Override
    public void dispose() {
        setViewNotifier(null);
        super.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        paneHelp = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Beftigre: Help");

        paneHelp.setEditable(false);
        paneHelp.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(paneHelp);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Help.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Help().setVisible(true);
        });
    }

    private String viewNotifier;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JEditorPane paneHelp;
    // End of variables declaration//GEN-END:variables
}
