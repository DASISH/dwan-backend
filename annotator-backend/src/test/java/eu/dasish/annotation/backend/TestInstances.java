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
import eu.dasish.annotation.schema.ResourceREF;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class TestInstances {
    
    final private Annotation _annotationOne;
    
    public TestInstances(){
        _annotationOne = makeAnnotationOne();
    }
    
    
    private Annotation makeAnnotationOne(){
        // add the other fields whengetAnnotation is completed
        Annotation result = new Annotation();
        AnnotationBody body = new AnnotationBody();
        List<Object> bodyContent = body.getAny();
        bodyContent.add(TestBackendConstants._TEST_ANNOT_1_BODY);        
        result.setBody(body);
        result.setHeadline(TestBackendConstants._TEST_ANNOT_1_HEADLINE);
        ResourceREF owner = new ResourceREF();
        owner.setRef(String.valueOf(TestBackendConstants._TEST_ANNOT_1_OWNER));
        result.setOwner(owner);
        return result;
    }
    
    public Annotation getAnnotationOne(){
        return _annotationOne;
    }
}
