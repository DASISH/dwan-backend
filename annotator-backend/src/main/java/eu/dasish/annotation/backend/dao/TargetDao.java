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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author olhsha
 */
public interface TargetDao extends ResourceDao {

    /**
     * GETTERS 
     *
     */
    /**
     *
     * @param inernalID
     * @return the Target with the intrenal Id "internalID".
     */
    public Target getTarget(Number internalID);

    /**
     *
     * @param targets
     * @return the list of TargetInfo objects corresponding to the Targets with
     * the internalIds from the list "Targets".
     */
    public List<TargetInfo> getTargetInfos(List<Number> targets);

    /**
     *
     * @param targetID
     * @return the list of pairs (cached_representation_id, fragment_descriptor)
     * for the target with the internal ID "targetID".
     */
    public Map<Number, String> getCachedRepresentationFragmentPairs(Number targetID);

    /**
     *
     * @param subword
     * @return the list of Target ID's which link-fields contain "subword" as a
     * substring.
     */
    public List<Number> getTargetsReferringTo(String subword);

    /**
     *
     * @param targetID
     * @return The link (uri) to the source to which the target refers
     */
    public String getLink(Number targetID);

    /**
     *
     * @param link
     * @return the list of Target ID's which link-fields is exactly "link"
     */
    public List<Number> getTargetsForLink(String link);

    boolean cachedIsInUse(Number cachedID);

    public List<Number> retrieveTargetIDs(Number annotationID);

    /**
     * ADDERS
     *
     */
    /**
     *
     * @param target: the Target-object of the Target to be added to "Target"
     * table.
     * @return the internal ID of the just added Target or null if it has not
     * been added.
     */
    public Number addTarget(Target target)  throws NotInDataBaseException;

    /**
     *
     * @param TargetID
     * @param cachedID
     * @return # added rows to the table "Targets_cached_representations".
     * Should be "1" if the pair (TargetID, cachedID) has been added.
     * @throws SQLException
     */
    public int addTargetCachedRepresentation(Number TargetID, Number cachedID, String fragmentDescription);

    /**
     * DELETERS
     *
     */
    /**
     *
     * @param internalId
     * @return # deleted rows in "Target" table. Should be "1" if the Target has
     * been deleted.
     */
    public int deleteTarget(Number internalID);

    /**
     *
     * @param TargetID
     * @return # deleted rows in the table "Targets_cached_representation" when
     * deleting the pair (TargetID, chachedID)
     * @throws SQLException
     */
    public int deleteTargetCachedRepresentation(Number TargetID, Number chachedID);
}
