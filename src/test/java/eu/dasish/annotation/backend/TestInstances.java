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
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.NewSourceInfo;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.SourceInfo;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class TestInstances {
    
    final private Annotation _annotationOne;
    final private Annotation _annotationToAdd;
    final private Annotation _annotationToAddNewSource;
    
    public TestInstances(){
        _annotationOne = makeAnnotationOne();
        _annotationToAdd = makeAnnotationToAdd();
        _annotationToAddNewSource = makeAnnotationToAddNewSource();        
    }
    
    
    private Annotation makeAnnotationOne(){
        Annotation result = makeAnnotation(TestBackendConstants._TEST_ANNOT_2_BODY, TestBackendConstants._TEST_ANNOT_2_HEADLINE, TestBackendConstants._TEST_ANNOT_2_OWNER);
        return result;
    }
    
    private Annotation makeAnnotationToAdd(){
       Annotation result = makeAnnotation(TestBackendConstants._TEST_ANNOT_TO_ADD_BODY, TestBackendConstants._TEST_ANNOT_TO_ADD_HEADLINE, 5);
       
       SourceInfo sourceInfo =  new SourceInfo();
       sourceInfo.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
       sourceInfo.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
       sourceInfo.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID); 
       
       NewOrExistingSourceInfo noeSourceInfo =  new NewOrExistingSourceInfo();
       noeSourceInfo.setSource(sourceInfo);
       NewOrExistingSourceInfos noeSourceInfos =  new NewOrExistingSourceInfos();
       noeSourceInfos.getTarget().add(noeSourceInfo);
       result.setTargetSources(noeSourceInfos);
       
       return result;
    }
    
    private Annotation makeAnnotationToAddNewSource(){
       Annotation result = makeAnnotation(TestBackendConstants._TEST_ANNOT_TO_ADD_NEW_SOURCE_BODY, TestBackendConstants._TEST_ANNOT_TO_ADD_NEW_SOURCE_HEADLINE, 5);
       
       NewSourceInfo newSourceInfo =  new NewSourceInfo();
       newSourceInfo.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
       newSourceInfo.setId(TestBackendConstants._TEST_TEMP_SOURCE_ID);
       // TODO: so far, the version is the external version id generated when a version is added
       // because for now the version is used to keep external id of the version, not is human-friendly headline
       // fix it by adding external Id to the version
       newSourceInfo.setVersion(null); 
       
       NewOrExistingSourceInfo noeSourceInfo =  new NewOrExistingSourceInfo();
       noeSourceInfo.setNewSource(newSourceInfo);
       NewOrExistingSourceInfos noeSourceInfos =  new NewOrExistingSourceInfos();
       noeSourceInfos.getTarget().add(noeSourceInfo);
       result.setTargetSources(noeSourceInfos);
       
       return result;
    }
    
    
    // so far tests only adding annot with existing sources!!!
    // TODO: add non-existing sources
    private Annotation makeAnnotation(String bodyTxt, String headline, int ownerId){
        Annotation result = new Annotation();
        AnnotationBody body = new AnnotationBody();
        List<Object> bodyContent = body.getAny();
        bodyContent.add(bodyTxt);        
        result.setBody(body);
        result.setHeadline(headline);
        ResourceREF owner = new ResourceREF();
        owner.setRef(String.valueOf(ownerId));
        result.setOwner(owner); 
        
        result.setTimeStamp(null); 
        result.setURI(null);
        result.setTargetSources(null);
        result.setURI(null);
        
       return result;
    }
    
    //private 
    
    
    public Annotation getAnnotationOne(){
        return _annotationOne;
    }
    
    public Annotation getAnnotationToAdd(){
        return _annotationToAdd;
    }
    
   public Annotation getAnnotationToAddNewSource(){
        return _annotationToAddNewSource;
    }
    
}
