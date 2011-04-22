<html>

  <body>
  
    <p>
      Please complete the following form to create an account for the 
      StratusLab reference infrastructure.  All fields are mandatory. 
    </p>

    <hr/>

    <form action="../users/" enctype="application/x-www-form-urlencoded" method="POST">
      <table>
        <tbody>
          <tr>
            <td>Username</td>
            <td><input type="text" name="uid" size="40"></td>
          </tr>
          <tr>
            <td>Email Address</td>
            <td><input type="text" name="mail" size="40"></td>
          </tr>
          <tr>
            <td>Given Name(s)</td>
            <td><input type="text" name="givenName" size="40"></td>
          </tr>
          <tr>
            <td>Family Name</td>
            <td><input type="text" name="sn" size="40"></td>
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
            <td><input type="submit" value="create"></td><td></td>
          </tr>
        </tbody>
      </table>
    </form>
    
    <hr/>

  </body>
</html>
