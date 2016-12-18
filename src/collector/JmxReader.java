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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JmxReader {

    private final String strTestPlan;   // for validating test plan editing
    private final File testPlan;        // for test plan file

    public JmxReader(File testPlan) {
        this.testPlan = testPlan;
        this.strTestPlan = null;
    }

    public JmxReader(String strTestPlan) {
        this.strTestPlan = strTestPlan;
        this.testPlan = null;
    }

    public String getServer() {
        Node cp = readjmx();
        Node epServer = getNodeList("elementProp", cp.getChildNodes()).get(0);
        Node spServer = getNodeList("stringProp", epServer.getChildNodes()).get(1);
        return getNodeValue(spServer);
    }

    private InputStream stream() throws FileNotFoundException {
        if (strTestPlan != null) {
            return new ByteArrayInputStream(strTestPlan.getBytes(Charset.defaultCharset()));
        } else if (testPlan != null) {
            return new FileInputStream(testPlan.getPath());
        }
        return null;
    }

    /**
     * Method to read .jmx test plan.
     *
     * @return
     */
    public Node readjmx() {
        Node cp = null;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = stream();

            Document doc = builder.parse(stream);
            NodeList root = doc.getChildNodes();

            Node jtp = getNode("jmeterTestPlan", root);

            Node ht = getNodeList("hashTree", jtp.getChildNodes()).get(0);
            Node tp = getNodeList("TestPlan", ht.getChildNodes()).get(0);
            Node ep = getNodeList("elementProp", tp.getChildNodes()).get(0);
            cp = getNodeList("collectionProp", ep.getChildNodes()).get(0);

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(JmxReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cp;
    }

    /**
     * Method to get nodes.
     *
     * @param tagName
     * @param nodes
     * @return
     */
    public Node getNode(String tagName, NodeList nodes) {
        for (int x = 0; x < nodes.getLength(); x++) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Method to get node lists.
     *
     * @param tagName
     * @param nodes
     * @return
     */
    public List<Node> getNodeList(String tagName, NodeList nodes) {
        List<Node> lst = new ArrayList<>();
        for (int x = 0; x < nodes.getLength(); x++) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                lst.add(node);
            }
        }
        return lst;
    }

    /**
     * Method to get value from a node.
     *
     * @param node
     * @return
     */
    public String getNodeValue(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int x = 0; x < childNodes.getLength(); x++) {
            Node data = childNodes.item(x);
            if (data.getNodeType() == Node.TEXT_NODE) {
                return data.getNodeValue();
            }
        }
        return "";
    }
}
