<html>

  <#include "header.ftl">

  <body>
    
    <h1>Registration Form</h1>
    
    <#include "breadcrumbs.ftl">

    <p>
      Please complete the following form to create a new account 
      on the StratusLab reference cloud infrastructures.
    </p>
    <p>
      <strong>All fields are mandatory.</strong> 
    </p>

    <form id="form_with_tooltips" 
          action="../users/" 
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
                     title="sequence of 3 to 20 letters, digits, or underscores">
            </td>
          </tr>
          <tr>
            <td>Email Address</td>
            <td>
              <input type="text" 
                     name="mail" 
                     size="40" 
                     title="valid email address with domain">
            </td>
          </tr>
          <tr>
            <td>Given Name(s)</td>
            <td>
              <input type="text" 
                     name="givenName" 
                     size="40" 
                     title="your given name(s), cannot be empty">
            </td>
          </tr>
          <tr>
            <td>Family Name</td>
            <td>
              <input type="text" 
                     name="sn" 
                     size="40" 
                     title="your surname, cannot be empty">
            </td>
          </tr>
          <tr>
            <td>Password</td>
            <td>
              <input type="password" 
                     name="newUserPassword" 
                     size="40"
                     maxlength="20"
                     title="sequence of 8 to 20 printable characters">
            </td>
          </tr>
          <tr>
            <td>Password (again)</td>
            <td>
              <input type="password" 
                     name="newUserPasswordCheck" 
                     size="40"
                     maxlength="20"
                     title="password double check">
            </td>
          </tr>
          <tr>
            <td>Message</td>
            <td>
              <textarea name="message" 
                        cols="40"
                        rows="5"
                        title="tell us why you'd like to use the cloud and how you heard about StratusLab">
              </textarea>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <label>I agree to the defined 
                     <a href="policies/">terms, conditions, and policies</a>
                <input type="checkbox" 
                       name="agreement" 
                       title="you must agree to create an account">
              </label>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <input type="submit" value="create">
            </td>
          </tr>
        </tbody>
      </table>
    </form>

    <#include "tooltip-js.ftl">    

  </body>
</html>
