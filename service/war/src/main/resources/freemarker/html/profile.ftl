<html>

  <#include "header.ftl">

  <body>
 
    <h1>User Profile</h1>
    
    <#include "breadcrumbs.ftl">
     
    <p>
      Submit this form with new values to update your account.
    </p>
    <p>
      <strong>Fields marked with an asterisk (*) are mandatory.</strong> 
    </p>

    <form id="form_with_tooltips" 
          action="profile/?method=put" 
          enctype="application/x-www-form-urlencoded" 
          method="POST">

      <table>
        <tbody>
          <tr>
            <td>Username</td>
            <td>
              <input type="text" 
                     name="uid" 
                     size="40"
                     maxlength="20"
                     value="${properties['uid']}" 
                     readonly="true">
            </td>
          </tr>
          <tr>
            <td>Email Address</td>
            <td>
              <input type="text" 
                     name="mail" 
                     size="40" 
                     value="${properties['mail']}" 
                     title="valid email address with domain">
            </td>
          </tr>
          <tr>
            <td>Given Name(s)</td>
            <td>
              <input type="text" 
                     name="givenName" 
                     size="40" 
                     value="${properties['givenName']}" 
                     title="your given name(s), cannot be empty">
            </td>
          </tr>
          <tr>
            <td>Family Name</td>
            <td>
              <input type="text" 
                     name="sn" 
                     size="40" 
                     value="${properties['sn']}"
                     title="your surname, cannot be empty">
            </td>
          </tr>
          <tr>
            <td>X500 DN</td>
            <td>
              <input type="text" 
                     name="seeAlso" 
                     size="40" 
                     value="${properties['seeAlso']!}"
                     title="certificate DN in RFC2253 format">
            </td>
          </tr>
          <tr>
            <td>Current Password (*)</td>
            <td>
              <input type="password" 
                     name="userPassword" 
                     size="40"
                     maxlength="20"
                     title="sequence of 8 to 20 printable characters">
            </td>
          </tr>
          <tr>
            <td>New Password</td>
            <td>
              <input type="password" 
                     name="newUserPassword" 
                     size="40"
                     maxlength="20"
                     title="new password (sequence of 8 to 20 printable characters)">
            </td>
          </tr>
          <tr>
            <td>New Password (again)</td>
            <td>
              <input type="password" 
                     name="newUserPasswordCheck" 
                     size="40"
                     maxlength="20"
                     title="password double check">
            </td>
          </tr>
          <tr>
            <td colspan="2"><input type="submit" value="update"></td>
          </tr>
        </tbody>
      </table>
    </form>
    
    <#include "tooltip-js.ftl">
    
  </body>
</html>
