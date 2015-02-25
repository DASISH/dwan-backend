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
     * @param inernalID the internal database id of a target.
     * @return the {@link Target} object representing the target with the internal id "internalID".
     */
    public Target getTarget(Number internalID);

    /**
     *
     * @param targets the list of internal database ids of targets.
     * @return the list of {@link TargetInfo} objects representing the targets with the internal id-s from the list "targets".
     */
    public List<TargetInfo> getTargetInfos(List<Number> targets);

    /**
     *
     * @param targetID the internal database id of a target.
     * @return the list of pairs "cached_representation_id -> fragment_descriptor" for the target with the internal ID "targetID".
     */
    public Map<Number, String> getCachedRepresentationFragmentPairs(Number targetID);

   

    /**
     *
     * @param targetID the internal database id of a target.
     * @return The link (uri) to the source to which the target refers.
     */
    public String getLink(Number targetID);

    /**
     *
     * @param link a link (uri) to a target source.
     * @return the list of internal database target id-s whose link-fields is exactly "link".
     */
    public List<Number> getTargetsForLink(String link);

  /**
   * 
   * @param cachedID the internal database id of a cached representation.
   * @return true iff "cachedID" is connected  to some target in "targets_cached_representations" table.
   */
    boolean cachedIsInUse(Number cachedID);

    /**
     * 
     * @param annotationID the internal database ID of an annotation.
     * @return the list of the internal database id's of the targets of the annotation.
     */
    public List<Number> getTargetIDs(Number annotationID);

    /**
     * ADDERS
     *
     */

   /**
    * 
    * @param target a {@link Target} object representing the target to add.
    * @return the internal database id of the target if it is added to the database.
    * @throws NotInDataBaseException if adding fails.
    */
    public Number addTarget(Target target)  throws NotInDataBaseException;

 
    /**
     * 
     * @param targetID the internal database id of a target.
     * @param cachedID the internal database id of a cached representation.
     * @param fragmentDescription a string representing the location of the target in the cached representation.
     * @return # of updated rows in the table "targets_cached_representations"; should be "1" if the row with (targetID, cachedID) has been updated.
     */
    public int updateTargetCachedRepresentationFragment(Number targetID, Number cachedID, String fragmentDescription);

    /**
     * 
     * @param targetID the internal database id of a target.
     * @param cachedID the internal database id of a cached representation.
     * @param fragmentDescription a string representing the location of the target in the cached representation.
     * @return # of added rows in the table "targets_cached_representations"; should be "1" if the row with (targetID, cachedID, fragmentDescriptor) is added.
     */
    public int addTargetCachedRepresentation(Number targetID, Number cachedID, String fragmentDescription);

    /**
     * DELETERS
     *
     */
    /**
     *
     * @param internalID the internal database id of a target.
     * @return # deleted rows in "target" table. Should be "1" if the target has been removed.
     */
    public int deleteTarget(Number internalID);

/**
 * 
 * @param targetID the internal database id of a target.
 * @param cachedID the internal database id of a cached representation.
 * @return # of deleted rows in the table "targets_cached_representations"; should be "1" if the deletion has taken place,an "0" otherwise". 
 */
    public int deleteTargetCachedRepresentation(Number targetID, Number chachedID);
}
