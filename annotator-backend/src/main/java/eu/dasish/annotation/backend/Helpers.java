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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
/**
 *
 * @author olhsha
 */
public class Helpers {
    
    //exception messages
    final static public String INVALID_BODY_EXCEPTION = "Invalide annotation body: both, text and xml options, are null.";
    

    public static XMLGregorianCalendar setXMLGregorianCalendar(Timestamp timeStamp) throws DatatypeConfigurationException {
        //DateTimeZone jdtz = DateTimeZone.forTimeZone(xmlGC.getTimeZone(xmlGC.getTimezone()));
        //DateTime jdt = new DateTime(xmlGC.getYear(), xmlGC.getMonth(), xmlGC.getDay(), xmlGC.getHour(), xmlGC.getMinute(), xmlGC.getSecond(), jdtz);
            
        
        GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(timeStamp.getTime());
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    }
    
        
    public static Timestamp retrieveTimeStamp(XMLGregorianCalendar xmlGC) {
        //DateTimeZone jdtz = DateTimeZone.forTimeZone(xmlGC.getTimeZone(xmlGC.getTimezone()));
        //DateTime jdt = new DateTime(xmlGC.getYear(), xmlGC.getMonth(), xmlGC.getDay(), xmlGC.getHour(), xmlGC.getMinute(), xmlGC.getSecond(), jdtz);
        GregorianCalendar gc = new GregorianCalendar(xmlGC.getTimeZone(xmlGC.getTimezone()));
        int test = xmlGC.getTimezone();
        TimeZone tz  = xmlGC.getTimeZone(test);
        gc.set(xmlGC.getYear(), xmlGC.getMonth(), xmlGC.getDay(), xmlGC.getHour(), xmlGC.getMinute(), xmlGC.getSecond());
        Timestamp result = new Timestamp(gc.getTimeInMillis());
        TimeZone tz2 = gc.getTimeZone();
        int hr = result.getHours();
        int offset = result.getTimezoneOffset();
        return result;
    }
  
    
     
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
                    Document doc = dbf.parse(string);
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
        DOMImplementationLS domImplLS = (DOMImplementationLS) document
                .getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        String result = serializer.writeToString(element);
        return result;
    }
}
