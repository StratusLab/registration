<html>

  <#include "header.ftl">

  <body>
  
    <p>
      You can update your profile by submitting this form with new values. 
    </p>

    <form action="./?method=put" enctype="application/x-www-form-urlencoded" method="POST">
      <table>
        <tbody>
          <tr>
            <td>Username</td>
            <td><input type="text" name="uid" size="40" value="${properties['uid']}" readonly="true"></td>
          </tr>
          <tr>
            <td>Email Address</td>
            <td><input type="text" name="mail" size="40" value="${properties['mail']}"></td>
          </tr>
          <tr>
            <td>Given Name(s)</td>
            <td><input type="text" name="givenName" size="40" value="${properties['givenName']}"></td>
          </tr>
          <tr>
            <td>Family Name</td>
            <td><input type="text" name="sn" size="40" value="${properties['sn']}"></td>
          </tr>
          <tr>
            <td>Password</td>
            <td><input type="password" name="userPassword" size="40"></td>
          </tr>
          <tr>
            <td>Password (again)</td>
            <td><input type="password" name="userPasswordCheck" size="40"></td>
          </tr>
          <tr>
            <td><input type="submit" value="update"></td><td></td>
          </tr>
        </tbody>
      </table>
    </form>
    
  </body>
</html>
