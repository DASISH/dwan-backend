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

import eu.dasish.annotation.schema.Principal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import java.util.Random;
/**
 *
 * @author olhsha
 */
public class Helpers {

    //exception messages
    //final static public String INVALID_BODY_EXCEPTION = "Invalide annotation body: both, text and xml options, are null.";
    final static String hexa = "ABCDEabcde";
    final static int hexan= hexa.length();
    
    public static String replace(String text, Map<String, ?> pairs) {
        StringBuilder result = new StringBuilder(text);
        for (String old : pairs.keySet()) {
            if (old != null) {
                if (!old.equals("")) {
                    replaceString(result, old, pairs.get(old));
                }
            }
        }
        return result.toString();
    }

    public static StringBuilder replaceString(StringBuilder source, String oldFragment, Object newObject) {
        if (oldFragment != null) {
            int lengthOld = oldFragment.length();
            String newFragment;
            if (newObject != null) {
                if (newObject instanceof Integer) {
                    newFragment = ((Integer) newObject).toString();
                } else {
                    if (newObject instanceof String) {
                        newFragment = (String) newObject;
                    } else {
                        newFragment = newObject.toString();
                    }
                }
            } else {
                newFragment = " ";
            }
            int lengthNew = newFragment.length();
            int indexOf = source.indexOf(oldFragment);
            while (indexOf > 0) {
                source.delete(indexOf, indexOf + lengthOld);
                source.insert(indexOf, newFragment);
                indexOf = source.indexOf(oldFragment, indexOf + lengthNew);
            }
        }
        return source;
    }

    public static Element stringToElement(String string) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder dbf = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(string.getBytes("UTF-16"));
        Document doc = dbf.parse(is);
        return doc.getDocumentElement();

    }

    public static String elementToString(Element element) {
        Document document = element.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        String result = serializer.writeToString(element);
        return result;
    }

    public static String welcomeString(String baseUri, String remoteID) {
        String welcome = "<!DOCTYPE html><body>"
                + "You are logged in as "
                + remoteID + ".<br>"
                + "<h3>Welcome to DASISH Webannotator (DWAN)</h3><br>"
                + "<a href=\"" + baseUri + "\"> To DWAN REST overview page</a>"
                + "</body>";
        return welcome;
    }

    public static Principal createPrincipalElement(String name, String e_mail) {
        Principal result = new Principal();
        result.setDisplayName(name);
        result.setEMail(e_mail);
        return result;
    }

    public static String hashPswd(String pswd, int strength, String salt) {
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(strength);
        return encoder.encodePassword(pswd, salt);
    }
    
    public static UUID generateUUID(){ 
        UUID result = UUID.randomUUID();
        char[] chars = result.toString().toCharArray();
        if (chars[0] >= 'a'  && chars[0] <='z') {
            return result;
        } else {
            Random r = new Random();
            chars[0] = hexa.charAt(r.nextInt(hexan));
            result = UUID.fromString(new String(chars));
            return result;
        }       
    }
}
