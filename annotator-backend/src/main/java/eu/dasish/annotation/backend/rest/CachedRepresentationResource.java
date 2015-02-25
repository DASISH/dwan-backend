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

import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import eu.dasish.annotation.backend.BackendConstants;
import eu.dasish.annotation.backend.ForbiddenException;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.dao.ILambda;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A REST class for GETting, POSTing and PUTting cached representations. 
 * DELETing cached representation is provided by {@link TargetRsource} class.
 * Every REST method in the case of successful completion produces an object of the declared output type
 * (a JAXB-element or a message string) or sends a HTTP-error with the corresponding diagnostics otherwise.
 * @author olhsha
 */
@Component
@Path("/cached")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class CachedRepresentationResource extends ResourceResource {
   
   
    /**
     * 
     * @param externalId the external UUID of a cached representation.
     * @return a {@link CachedRepresentationInfo} object representing the metadata for the cached representation with the "externalId".
     * @throws IOException if sending an error fails.
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/metadata")
    @Transactional(readOnly = true)
    public JAXBElement<CachedRepresentationInfo> getCachedRepresentationInfo(@PathParam("cachedid") String externalId) throws IOException {
        Map params = new HashMap();
        try {
            CachedRepresentationInfo result = (CachedRepresentationInfo) (new RequestWrappers(this)).wrapRequestResource(params, new GetCachedRepresentationInfo(), Resource.CACHED_REPRESENTATION, Access.READ, externalId);
            if (result != null) {
                return (new ObjectFactory()).createCachedRepresentationInfo(result);
            } else {
                return (new ObjectFactory()).createCachedRepresentationInfo(new CachedRepresentationInfo());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return new ObjectFactory().createCachedRepresentationInfo(new CachedRepresentationInfo());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createCachedRepresentationInfo(new CachedRepresentationInfo());
        }
    }

    private class GetCachedRepresentationInfo implements ILambda<Map, CachedRepresentationInfo> {

        @Override
        public CachedRepresentationInfo apply(Map params) throws NotInDataBaseException {
            Number cachedID = (Number) params.get("internalID");
            return dbDispatcher.getCachedRepresentationInfo(cachedID);
        }
    }

    /**
     * 
     * @param externalId the external UUID of a cached representation.
     * @return the image-blob if the cached-representation's blob is an image file.
     * @throws IOException if logging an error fails (should be changed to sending httpResponse error message).
     */
    @GET
    @Produces({"image/jpeg", "image/png"})
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/content")
    @Transactional(readOnly = true)
    public BufferedImage getCachedRepresentationContent(@PathParam("cachedid") String externalId) throws IOException {
        Map params = new HashMap();
        try {
            InputStream result = (InputStream) (new RequestWrappers(this)).wrapRequestResource(params, new GetCachedRepresentationInputStream(), Resource.CACHED_REPRESENTATION, Access.READ, externalId);
            if (result != null) {
                ImageIO.setUseCache(false);
                try {
                    BufferedImage retVal = ImageIO.read(result);
                    return retVal;
                } catch (IOException e1) {
                    loggerServer.info(e1.toString());

                    return null;
                }
            } else {
                loggerServer.info(" The cached representation with the id " + externalId + " has null blob.");
                return null;
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return null;
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return null;
        }

    }

    /**
     * 
     * @param externalId the external UUID of a cached representation.
     * @return the cached-representation's blob.
     * @throws IOException is sending an error fails.
     */
    @GET
    //@Produces({"text/plain", "text/html", "text/xml", "application/zip", "image/png", "image/jpg"})
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/stream")
    @Transactional(readOnly = true)
    public InputStream getCachedRepresentationContentStream(@PathParam("cachedid") String externalId) throws IOException {
        Map params = new HashMap();
        try {
            return (InputStream) (new RequestWrappers(this)).wrapRequestResource(params, new GetCachedRepresentationInputStream(), Resource.CACHED_REPRESENTATION, Access.READ, externalId);
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return null;
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return null;
        }
    }

    private class GetCachedRepresentationInputStream implements ILambda<Map, InputStream> {

        @Override
        public InputStream apply(Map params) throws NotInDataBaseException {
            Number cachedID = (Number) params.get("internalID");
            return dbDispatcher.getCachedRepresentationBlob(cachedID);
        }
    }

    /**
     * 
     * @param cachedIdentifier the external UUID of a cached representation.
     * @param multiPart a {@link MultiPart} object containing two parts: a {@link CachedRepresentationInfo} object for the cached-representation's metadata, and its blob.
     * @return a message about how many rows in "cached_representation" table have been updated; "1" if updated, and "0" otherwisee.
     * @throws IOException if sending an error fails.
     */
    @PUT
    @Consumes("multipart/mixed")
    @Produces(MediaType.APPLICATION_XML)
    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/stream")
    public String updateCachedBlob(@PathParam("cachedid") String cachedIdentifier,
            MultiPart multiPart) throws IOException {
        Map params = new HashMap();
        BodyPartEntity bpe = (BodyPartEntity) multiPart.getBodyParts().get(0).getEntity();
        params.put("stream", bpe.getInputStream());
        try {
            Integer result = (Integer) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateCachedBlob(), Resource.CACHED_REPRESENTATION, Access.WRITE, cachedIdentifier);
            if (result != null) {
                return result + "rows are updated";
            } else {
                return "Nothing is updated. ";
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return e1.getMessage();
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return e2.getMessage();
        }

    }

//    @PUT
//    @Consumes("text/plain")
//    @Produces(MediaType.APPLICATION_XML)
//    @Path("{cachedid: " + BackendConstants.regExpIdentifier + "}/path/{isurl}")
//    public String updateCachedBlobFromFile(@PathParam("cachedid") String cachedIdentifier,
//            @PathParam("isurl") String isURL, String blobPath) throws IOException {
//        Map params = new HashMap();
//        InputStream input;
//
//        if (isURL.equals("URL")) {
//            URL blob = new URL(blobPath);
//            input = blob.openStream();
//        } else {
//            input = new FileInputStream(blobPath);
//        }
//
//        params.put("stream", input);
//        try {
//            Integer result = (Integer) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateCachedBlob(), Resource.CACHED_REPRESENTATION, Access.WRITE, cachedIdentifier);
//            input.close();
//            if (result != null) {
//                return result + "rows are updated";
//            } else {
//                return "Nothing is updated. ";
//            }
//        } catch (NotInDataBaseException e1) {
//            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
//            return e1.getMessage();
//        } catch (ForbiddenException e2) {
//            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
//            return e2.getMessage();
//        }
//
//    }
//
    private class UpdateCachedBlob implements ILambda<Map, Integer> {

        @Override
        public Integer apply(Map params) throws NotInDataBaseException {
            Number cachedID = (Number) params.get("internalID");
            InputStream stream = (InputStream) params.get("stream");
            try {
                return dbDispatcher.updateCachedBlob(cachedID, stream);
            } catch (IOException e) {
                loggerServer.info(e.toString());
                return 0;
            }
        }
    }

    /**
     * 
     * @param cachedInfo a {@link CachedRepresentationInfo} object representing the new metadata.
     * @return a message about how many rows in "cached_representation" table have been updated: "1" if updated, "0" otherwise.
     * @throws IOException if sending an error fails.
     */
    @PUT
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("metadata")
    public String updateCachedMetadata(CachedRepresentationInfo cachedInfo) throws IOException {
        Map params = new HashMap();
        params.put("info", cachedInfo);
        try {
            Integer result = (Integer) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateCachedMetadata(), Resource.CACHED_REPRESENTATION, Access.ALL, cachedInfo.getId());
            if (result != null) {
                return result + "rows are updated";
            } else {
                return "Nothing is updated. ";
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return e1.getMessage();
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return e2.getMessage();
        }
    }

    private class UpdateCachedMetadata implements ILambda<Map, Integer> {

        @Override
        public Integer apply(Map params) throws NotInDataBaseException {
            CachedRepresentationInfo cachedInfo = (CachedRepresentationInfo) params.get("info");
            return dbDispatcher.updateCachedMetada(cachedInfo);
        }
    }
}
