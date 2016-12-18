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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class FileCreator {

    private static final File FILES = new File("files");
    private static final File LOGS = new File("logs");
    private static final File RESULTSPLOT = new File("results/plot");

    private static final File RAND = new File("files/rand");
    private static final File SLOW = new File("files/slow");
    private static final File PLAN = new File("files/TestPlan.jmx");
    private static final File SIGAR = new File("files/sigar.zip");

    private static final File SETTINGS = new File("files/settings.befor");

    public static void loadSettings(JTextField txtPemFile, JTextField txtHostIP, JTextField txtPort, JTextField txtUser,
            JTextField txtJmeterDir, JTextField txtBLPort, JTextField txtCMPort, JTextArea txtOutput) {
        if (SETTINGS.exists()) {
            BufferedReader in;
            String str, error = "";
            String pemFile = "", hostIP = "", port = "", user = "", jmeterDir = "", blPort = "", cmPort = "";
            try {
                in = new BufferedReader(new FileReader(SETTINGS));
                while ((str = in.readLine()) != null) {
                    switch (str.split(";")[0]) {
                        case "PemFile":
                            pemFile = str.split(";")[1];
                            break;
                        case "HostIP":
                            hostIP = str.split(";")[1];
                            break;
                        case "Port":
                            port = str.split(";")[1];
                            break;
                        case "User":
                            user = str.split(";")[1];
                            break;
                        case "JmeterDir":
                            jmeterDir = str.split(";")[1];
                            break;
                        case "BLPort":
                            blPort = str.split(";")[1];
                            break;
                        case "CMPort":
                            cmPort = str.split(";")[1];
                            break;
                        default:
                            error = "";
                            break;
                    }
                }
                //validate entries
                error += validateFile(pemFile, "Pem File");
                error += validateString(hostIP, 8, "Host IP");
                error += validateInt(port, "Port");
                error += validateString(user, 4, "User");
                error += validateFile(jmeterDir, "Jmeter Dir");
                error += validateInt(blPort, "BL Port");
                error += validateInt(cmPort, "CM Port");
                if (error.isEmpty()) {
                    txtPemFile.setText(pemFile);
                    txtHostIP.setText(hostIP);
                    txtPort.setText(port);
                    txtUser.setText(user);
                    txtJmeterDir.setText(jmeterDir);
                    txtBLPort.setText(blPort);
                    txtCMPort.setText(cmPort);
                    Helper.display(txtOutput, "Settings file loaded successfully.");
                } else {
                    Helper.display(txtOutput, error);
                }
            } catch (FileNotFoundException ex) {
                Helper.display(txtOutput, "Error: " + ex.getMessage());
            } catch (IOException ex) {
                Helper.display(txtOutput, "Error: " + ex.getMessage());
            }
        }
    }

    public static String validateString(String str, int length, String errId) {
        String error = "";
        if (str.length() < length) {
            error += "\nSettings error: " + errId + " was not located.";
        }
        return error;
    }

    public static String validateFile(String file, String errId) {
        String error = "";
        if (!new File(file).exists()) {
            error += "\nSettings error: " + errId + " was not located.";
        }
        return error;
    }

    public static String validateInt(String num, String errId) {
        String error = "";
        try {
            Integer.parseInt(num);
        } catch (NumberFormatException ex) {
            error += "\nSettings error: " + errId + " was not identified.";
        }
        return error;
    }

    public static void start(JTextArea txtOutput) {
        String out = "";
        if (!LOGS.exists()) {
            if (LOGS.mkdir()) {
                out += "logs directory is created. ";
            } else {
                out += "Failed to create logs directory. ";
            }
        } else {
            out += "logs directory found. ";
        }

        if (!RESULTSPLOT.isDirectory()) {
            if (RESULTSPLOT.mkdirs()) {
                out += "results\\plot directory is created. ";
            } else {
                out += "Failed to create results\\plot directory. ";
            }
        } else {
            out += "results\\plot directory found. ";
        }

        if (!FILES.exists()) {
            if (FILES.mkdir()) {
                out += "files directory is created. ";
                //out+=create(RAND);
                out += create(SLOW);
                out += create(PLAN);
                out += create(SIGAR);
            } else {
                out += "Failed to create files directory. ";
            }
        } else {
            out += "files directory found. ";
            //out+=create(RAND);
            out += create(SLOW);
            out += create(PLAN);
            out += create(SIGAR);
        }
        if (txtOutput == null) {
            System.out.println(out + "\n");
        } else {
            Helper.display(txtOutput, out);
        }
    }

    private static String create(File file) {
        String out = "";
        try {
            if (file.createNewFile()) {
                if (file == RAND) {
                    extract("rand", RAND);
                } else if (file == SLOW) {
                    extract("slow", SLOW);
                } else if (file == PLAN) {
                    extract("TestPlan.jmx", PLAN);
                } else if (file == SIGAR) {
                    extract("sigar.zip", SIGAR);
                }
                out += file + " created. ";
            } else {
                out += file + " already exist. ";
            }
        } catch (IOException ex) {
            out += "IOException " + ex.getMessage();
        }
        return out;
    }

    public static void extract(String srcFile, File destFile) {
        OutputStream outStream;
        try (InputStream in = FileCreator.class.getResourceAsStream(srcFile)) {
            outStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            outStream.close();
        } catch (FileNotFoundException ex) {
            System.err.print("FileNotFoundException " + ex.getMessage());
        } catch (IOException ex) {
            System.err.print("IOException " + ex.getMessage());
        }
    }
}
