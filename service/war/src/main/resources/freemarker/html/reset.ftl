<html>

  <#include "header.ftl">

  <body>
 
    <h1>Reset Password</h1>
    
    <#include "breadcrumbs.ftl">
     
    <p>
      Provide your username to reset your password.  
    </p>

    <form id="form_with_tooltips" 
          action="reset/" 
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
                     maxlength="20">
            </td>
            <td><input type="submit" value="reset password"></td>
          </tr>
        </tbody>
      </table>
    </form>
    
    <#include "tooltip-js.ftl">
    
  </body>
</html>
