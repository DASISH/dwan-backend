<%--
    Copyright (C) 2013 DASISH

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

--%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body onload='document.loginForm.username.focus();'>

        <h3>Login for non-shibboleth users</h3>

            <div id="login-box">
                <form name='loginForm'
                      action="j_spring_security_check" method='POST'>

                    <table>
                        <tr>
                            <td>Login:</td>
                            <td><input type='text' name='username' value=''></td>
                        </tr>
                        <tr>
                            <td>Password:</td>
                            <td><input type='password' name='password' /></td>
                        </tr>
                        <tr>
                            <td colspan='2'><input name="submit" type="submit"
                                                   value="submit" /></td>
                        </tr>
                    </table>

                </form>
            </div>

    </body>
</html>
