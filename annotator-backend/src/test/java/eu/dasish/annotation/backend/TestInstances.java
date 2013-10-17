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

/**
 *
 * @author olhsha
 */
public class TestInstances {
    
    final private Annotation _annotationOne;
    final private Annotation _annotationToAdd;
    
    public TestInstances(){
        _annotationOne = makeAnnotationOne();
        _annotationToAdd = makeAnnotationToAdd();    
    }
    
    
    private Annotation makeAnnotationOne(){
        Annotation result = makeAnnotation(TestBackendConstants._TEST_ANNOT_2_BODY, TestBackendConstants._TEST_BODY_MIMETYPE_HTML, TestBackendConstants._TEST_ANNOT_2_HEADLINE, TestBackendConstants._TEST_ANNOT_2_OWNER);
        return result;
    }
    
    private Annotation makeAnnotationToAdd(){
       Annotation result = makeAnnotation(TestBackendConstants._TEST_ANNOT_TO_ADD_BODY, TestBackendConstants._TEST_BODY_MIMETYPE_TEXT, TestBackendConstants._TEST_ANNOT_TO_ADD_HEADLINE, 5);
       
       TargetInfo TargetInfo =  new TargetInfo();
       TargetInfo.setLink(TestBackendConstants._TEST_Target_1_LINK);
       TargetInfo.setRef(TestBackendConstants._TEST_Target_1_EXT_ID);
       TargetInfo.setVersion(TestBackendConstants._TEST_Target_1_VERSION); 
       
       TargetInfoList targetInfos =  new TargetInfoList();
       targetInfos.getTargetInfo().add(TargetInfo);
       result.setTargets(targetInfos);
       
       return result;
    }
    

    private Annotation makeAnnotation(String bodyTxt, String bodyMimeType, String headline, int ownerId){
        Annotation result = new Annotation();
        AnnotationBody body = new AnnotationBody();
        result.setBody(body);
        TextBody textBody = new TextBody();
        body.setTextBody(textBody);
        textBody.setMimeType(bodyMimeType);
        textBody.setValue(bodyTxt);
       
        result.setHeadline(headline);
        result.setOwnerRef(String.valueOf(ownerId)); 
        
        result.setTimeStamp(null); 
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
