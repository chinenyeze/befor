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

package collector;

import helper.Helper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.perfmon.PerfMonCollector;
import kg.apc.jmeter.vizualizers.PerfMonGui;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

/**
 * This is a PerfmonMetricsCollector listener
 * This class requires PerfMon to be installed in JMeter, via plugins manager - already setup.
 */
public class PerfmonCollector {

    //begin recording: SwingWorker for bg processing of long thread
    public static void start(String jmeterHome, String host, String logFile, File testPlan, JTextArea txtOutput) {
        SwingWorker<String, String> worker;
        worker = new SwingWorker<String, String>() {

            @Override
            protected String doInBackground() throws Exception {
                // JMeter Engine
                StandardJMeterEngine jmeter = new StandardJMeterEngine();

                // Initialize Properties, home, logging & locale
                JMeterUtils.loadJMeterProperties(jmeterHome + "\\bin\\jmeter.properties");
                JMeterUtils.setJMeterHome(jmeterHome);
                JMeterUtils.initLogging();  //comment out for logging at DEBUG level
                JMeterUtils.initLocale();

                try {
                    // Initialize JMeter SaveService
                    SaveService.loadProperties();
                } catch (IOException ex) {
                    publish("Error: saveservice.properties file not found.");
                }

                // Load existing .jmx Test Plan
                HashTree testPlanTree = null;
                try {
                    testPlanTree = SaveService.loadTree(testPlan);
                } catch (IOException ex) {
                    publish("Error: invalid test plan.");
                }

                Summariser summer = null;
                //use appropriate name instead of summary
                String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
                //JMeterUtils.getPropDefault("summariser.log", true);
                if (summariserName.length() > 0) {
                    summer = new Summariser(summariserName);
                }

                PowerTableModel dataModel = new PowerTableModel(PerfMonGui.columnIdentifiers, PerfMonGui.columnClasses);
                dataModel.addRow(new String[]{host, "4444", "CPU", ""});
                dataModel.addRow(new String[]{host, "4444", "Memory", ""});

                //use permon collector
                PerfMonCollector perf = new PerfMonCollector();
                perf.setFilename(logFile);
                perf.setData(JMeterPluginsUtils.tableModelRowsToCollectionProperty(dataModel, PerfMonCollector.DATA_PROPERTY));
                testPlanTree.add(testPlanTree.getArray()[0], perf);

                // Run JMeter Test
                jmeter.configure(testPlanTree);
                publish("JMeter test started for Metrics collector.");
                jmeter.run();
                return "JMeter test finished for Metrics collector.";
            }

            // Can safely update the GUI from this method.
            @Override
            protected void done() {
                String status;
                try {
                    // Retrieve the return value of doInBackground.
                    status = get();
                    Helper.display(txtOutput, status);
                } catch (InterruptedException e) {
                    Helper.display(txtOutput, "Error: process was interrupted.");
                } catch (ExecutionException e) {
                    Helper.display(txtOutput, "Error: problem with Jmeter test setup.");
                }
            }

            // Can safely update the GUI
            @Override
            protected void process(List<String> chunks) {
                // Here we receive the values that we publish().
                // They may come grouped in chunks.
                String mostRecentValue = chunks.get(chunks.size() - 1);
                Helper.display(txtOutput, mostRecentValue);
            }
        };

        worker.execute();
    }

}
