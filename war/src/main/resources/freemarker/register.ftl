<html>

  <#include "header.ftl">

  <body>
    
    <p>
      Please complete the following form to create an account for the 
      StratusLab reference infrastructure.
    </p>
    <p>
      <strong>All fields are mandatory.</strong> 
    </p>

    <form id="myform" action="../users/" enctype="application/x-www-form-urlencoded" method="POST">
      <table>
        <tbody>
          <tr>
            <td>Username</td>
            <td><input type="text" name="uid" size="40" title="sequence of 3 to 20 letters and underscores characters"></td>
          </tr>
          <tr>
            <td>Email Address</td>
            <td><input type="text" name="mail" size="40" title="valid email address with domain"></td>
          </tr>
          <tr>
            <td>Given Name(s)</td>
            <td><input type="text" name="givenName" size="40" title="cannot be empty"></td>
          </tr>
          <tr>
            <td>Family Name</td>
            <td><input type="text" name="sn" size="40" title="cannot be empty"></td>
          </tr>
          <tr>
            <td>Password</td>
            <td><input type="password" name="userPassword" size="40" title="sequence of 8 to 20 printable characters"></td>
          </tr>
          <tr>
            <td>Password (again)</td>
            <td><input type="password" name="userPasswordCheck" size="40" title="double check the password"></td>
          </tr>
          <tr>
            <td colspan="2"><label>I agree to the terms and conditions<input type="checkbox" name="termsAgreement" title="you must agree to proceed"></label></td>
          </tr>
          <tr>
            <td colspan="2"><input type="submit" value="create"></td><td></td>
          </tr>
        </tbody>
      </table>
    </form>
    
<!-- This JavaScript snippet activates those tabs --> 
<script> 
 
// perform JavaScript after the document is scriptable.
$(function() {

$("#myform :input").tooltip({

    // place tooltip on the right edge
    position: "center right",

    // a little tweaking of the position
    offset: [-2, 10],

    // use the built-in fadeIn/fadeOut effect
    effect: "fade",

    // custom opacity setting
    opacity: 0.7

});

});
</script> 

  </body>
</html>
