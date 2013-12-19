/*
 * Copyright (C) 2013 DASISH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.dasish.annotation.backend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

/**
 *
 * @author olhsha
 */
public class Helpers {

    //exception messages
    final static public String INVALID_BODY_EXCEPTION = "Invalide annotation body: both, text and xml options, are null.";
   
    public static String replace(String text, Map<String, String> pairs) {
        String result = (new StringBuilder(text)).toString();
        for (String tempTarget : pairs.keySet()) {
            result = result.replaceAll(tempTarget, pairs.get(tempTarget));
        }
        return result;
    }

    public static Element stringToElement(String string) {
        try {
            DocumentBuilder dbf = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            try {
                try {
                    InputStream is = new ByteArrayInputStream(string.getBytes("UTF-16"));
                    Document doc = dbf.parse(is);
                    return doc.getDocumentElement();
                } catch (SAXException saxException) {
                    System.out.println(saxException);
                }
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        } catch (ParserConfigurationException parserException) {
            System.out.println(parserException);
        }
        return null;
    }

    public static String elementToString(Element element) {
        Document document = element.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        String result = serializer.writeToString(element);
        return result;
    }
}
