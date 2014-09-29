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
package eu.dasish.annotation.backend.rest;

import eu.dasish.annotation.backend.rest.AnnotationResource;
import java.io.IOException;

/**
 *
 * @author olhsha
 */
public class GetThread implements Runnable {

    private Thread getThread;
    private String threadName;
    private AnnotationResource annotationResource;
    private String annotationId;
    
    public GetThread(AnnotationResource aResource, String aId) {
        annotationResource = aResource;
        annotationId = aId;
        threadName = "getannotation:"+aId;
    }

    public void run() {
        try {
            annotationResource.getAnnotation(annotationId);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void start() {
        if (getThread == null) {
            getThread = new Thread(this, threadName);
            getThread.start();
        }
    }
}
