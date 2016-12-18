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

package analyser;

import com.panayotis.gnuplot.GNUPlotException;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.FileDataSet;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;
import helper.Helper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JTextArea;

public class Plotter {

    //items can be AppLog, PowerLog, MetricsLog or SimLog.
    //full path is completed within this plot method
    public static File plot(ArrayList<String> items, String label, JTextArea txtOutput) {
        ImageTerminal png = new ImageTerminal();
        File pngFile = new File("results/plot/Plot_" + System.currentTimeMillis() + ".png");
        try {
            pngFile.createNewFile();
            png.processOutput(new FileInputStream(pngFile));
            JavaPlot p = new JavaPlot();
            p.setTerminal(png);
            PlotStyle myPlotStyle = new PlotStyle();
            myPlotStyle.setStyle(Style.LINES);
            myPlotStyle.setLineWidth(1);
            items.stream().forEach((item) -> {
                if (item.equals("PowerLog") || item.equals("CPULog") || item.equals("MemLog")) {
                    try {
                        File file = new File("results/" + item + ".dat");
                        DataSetPlot dataSetPlot = new DataSetPlot(new FileDataSet(file));
                        dataSetPlot.setPlotStyle(myPlotStyle); //plotstyle
                        dataSetPlot.setTitle(item);
                        p.addPlot(dataSetPlot);
                    } catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                        Helper.display(txtOutput, "An error occurred: " + ex.getMessage());
                    }
                }
            });
            if (items.contains("AppLog")) {
                BufferedReader br;
                File file = new File("results/MarkerLog.dat");
                br = new BufferedReader(new FileReader(file));
                String strLine;
                br.readLine(); //read firs line of comment
                //first load all in hashmap
                while ((strLine = br.readLine()) != null) { //read from second line
                    String[] split = strLine.split(" ");

                    //double[][] plot = {{x,y}, {x,y}}; //use 10
                    double[][] plot = {{Double.parseDouble(split[1]), 10}, {Double.parseDouble(split[2]), 10}};
                    DataSetPlot s = new DataSetPlot(plot);
                    s.setTitle(split[0]);
                    p.addPlot(s);
                }
            }
            if (items.contains("SimLog")) {
                BufferedReader br;
                File file = new File("results/SimLog.dat");
                br = new BufferedReader(new FileReader(file));
                String strLine;
                br.readLine(); //read firs line of comment
                //first load all in hashmap
                while ((strLine = br.readLine()) != null) { //read from second line
                    String[] split = strLine.split(" ");
                    //use 15
                    double[][] plot = {{Double.parseDouble(split[1]), 15}};
                    DataSetPlot s = new DataSetPlot(plot);
                    s.setTitle(split[0]);
                    p.addPlot(s);
                }
            }
            p.setTitle(label);
            p.plot();
            ImageIO.write(png.getImage(), "png", pngFile);
            Helper.display(txtOutput, "Plot file generated.");
        } catch (IOException | GNUPlotException ex) {
            Helper.display(txtOutput, "An error occurred: " + ex.getMessage());
        }
        return pngFile;
    }
}
