/**
 * Copyright (C) 2013 DASISH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package eu.dasish.annotation.backend;

import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.TargetInfoList;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 *
 * @author olhsha
 */
public class TestInstances {
    
    final private Annotation _annotationOne;
    final private Annotation _annotationToAdd;
    
    public TestInstances(String baseURI){
        _annotationOne = makeAnnotationOne(baseURI);
        _annotationToAdd = makeAnnotationToAdd(baseURI);    
    }
    
    
    private Annotation makeAnnotationOne(String baseURI){
        Annotation result = makeAnnotation(baseURI, "<html><body>some html 1</body></html>", "text/html", "Sagrada Famiglia", "00000000-0000-0000-0000-000000000111");
        try {
        result.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z"));
        } catch (DatatypeConfigurationException dce) {
            System.out.println("wrongly-formatted test timestamp "+"2013-08-12T09:25:00.383000Z");
            result.setLastModified(null);
        }
        return result;
    }
    
    private Annotation makeAnnotationToAdd(String baseURI){
       Annotation result = makeAnnotation(baseURI, "<html><body>some html 1</body></html>", "text/plain", "Annotation to add to test DAO", "00000000-0000-0000-0000-000000000111");
       
       TargetInfo TargetInfo =  new TargetInfo();
       TargetInfo.setLink("http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia");
       TargetInfo.setRef("00000000-0000-0000-0000-000000000031");
       TargetInfo.setVersion("version 1.0"); 
       
       TargetInfoList targetInfos =  new TargetInfoList();
       targetInfos.getTargetInfo().add(TargetInfo);
       result.setTargets(targetInfos);
       
       return result;
    }
    

    private Annotation makeAnnotation(String baseURI, String bodyTxt, String bodyMimeType, String headline, String ownerID){
        Annotation result = new Annotation();
        AnnotationBody body = new AnnotationBody();
        result.setBody(body);
        TextBody textBody = new TextBody();
        body.setTextBody(textBody);
        textBody.setMimeType(bodyMimeType);
        textBody.setBody(bodyTxt);
       
        result.setHeadline(headline);
        result.setOwnerRef(baseURI+"users/"+ownerID); 
        
        result.setLastModified(null); 
        result.setURI(null);
        result.setTargets(null);
        result.setURI(null);
        
       return result;
    }
    
    
    public Annotation getAnnotationOne(){
        return _annotationOne;
    }
    
    public Annotation getAnnotationToAdd(){
        return _annotationToAdd;
    }
    
    
}
