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

import eu.dasish.annotation.schema.AnnotationBody;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author olhsha
 */
public class Helpers {

    public static XMLGregorianCalendar setXMLGregorianCalendar(Timestamp timeStamp) throws DatatypeConfigurationException {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(timeStamp);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
    }
    
        
    // TODO: change when serialization mechanism for bodies is fixed
    public static String serializeBody(AnnotationBody body) {
        return body.getAny().get(0).toString();
    }

    // TODO: change when serialization mechanism for bodies is fixed
    public static AnnotationBody deserializeBody(String bodyXml) {
        AnnotationBody result = new AnnotationBody();
        result.getAny().add(bodyXml);
        return result;
    }
    
     
    public static String replace(String text, Map<String, String> pairs) {
        String result = (new StringBuilder(text)).toString();
        for (String tempSource : pairs.keySet()) {
            result = result.replaceAll(tempSource, pairs.get(tempSource));
        }
        return result;
    }
    
   
}
