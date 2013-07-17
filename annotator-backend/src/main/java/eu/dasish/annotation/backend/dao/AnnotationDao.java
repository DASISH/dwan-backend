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
package eu.dasish.annotation.backend.dao;

import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Annotations;
import eu.dasish.annotation.schema.ResourceREF;
import java.util.List;

/**
 * Created on : Jun 27, 2013, 10:34:13 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */

// TODO: Getting Target Sources from Body and testing must be added!!!


public interface AnnotationDao extends ResourceDao{
    
     // Returns the list of annotation Id-s  for the notebook id.
    List<Number> getAnnotationIDs(Number notebookID);
    
     // Returns the list of annotation info-s  for the notebook id.
    List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs);    
     
     // Returns the list of annotation info-s  for the notebook id.
    List<AnnotationInfo> getAnnotationInfosOfNotebook(Number notebookID);    
    
     // Returns the list of annotations Id-s  for the notebook id.
    List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs);    
    
     // Returns the list of annotations Id-s  for the notebook id.
    List<ResourceREF> getAnnotationREFsOfNotebook(Number notebookID);
    
    // Returns the annotations object for the notebook id.
    Annotations getAnnotations(Number notebookID);
    
   
}
