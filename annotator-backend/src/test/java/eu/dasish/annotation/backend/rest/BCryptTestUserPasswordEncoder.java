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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/**
 *
 * @author olhsha
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml"})
public class BCryptTestUserPasswordEncoder {
    
    final   Map<String, String> userPassword= new HashMap<String, String>();
    private Map<String, String> passwordHash= new HashMap<String, String>();
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    
    public BCryptTestUserPasswordEncoder(){
        userPassword.put("olha", "olha"+"password");
        userPassword.put("stephanie", "stephanie"+"password");
        userPassword.put("olof", "olof"+"password");
        userPassword.put("twan", "twan"+"password");
        userPassword.put("peter", "peter"+"password");
        userPassword.put("przemek", "przemek"+"password");
        userPassword.put("daan", "daan"+"password");
        userPassword.put("menzo", "menzo"+"password");
        userPassword.put("eric", "eric"+"password");
    }
    
    
    @Test
    public void generateHashes(){
        System.out.println("Passwords -- hashes:");
        for (String user:userPassword.keySet()){
            String hash = passwordEncoder.encode(userPassword.get(user));
            System.out.println(userPassword.get(user)+" -- "+hash);
            passwordHash.put(userPassword.get(user), hash);
        }
    }
}
