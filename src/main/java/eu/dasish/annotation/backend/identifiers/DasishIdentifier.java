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
package eu.dasish.annotation.backend.identifiers;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author olhsha
 */
public class DasishIdentifier {
    
    @XmlElement(required = true)
    final private UUID _identifier;
    
    
    private int _hashParameterOne;
    private int _hashParameterTwo;
    
       
    public DasishIdentifier(UUID identifier) {
        _identifier = identifier;
    }
    
    public DasishIdentifier(String identifier) {
        if (identifier == null) {
            _identifier=null;
        } else {
               _identifier=UUID.fromString(identifier);
        }
    }

    public DasishIdentifier() {
        _identifier = UUID.randomUUID();
    }

    public UUID getUUID() {
        return _identifier;
    }
    
    
    @Override
    public String toString(){
       return (this.getUUID()== null ? null : this.getUUID().toString());
    }
    
    protected void setHashParameters(int parameterOne, int parameterTwo){
        _hashParameterOne=parameterOne;
        _hashParameterTwo=parameterTwo;
    }

    @Override
    public int hashCode() {
        int hash = _hashParameterOne;
        hash = _hashParameterTwo * hash + (_identifier != null ? _identifier.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DasishIdentifier other = (DasishIdentifier) obj;
        /*if ((_identifier == null) ? (other.getUUID() != null) : !_identifier.equals(other.getUUID())) {
            return false;
        }*/
        if (_identifier == null) {
            return (other.getUUID() == null); 
        }
        else {
            return _identifier.equals(other.getUUID());
        }
    }
     
}
