<html>

  <#include "/html/common/header.ftl">

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
        </tbody>
      </table>
    </form>
    
  </body>
</html>
