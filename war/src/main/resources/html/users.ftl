<html>

<head>
  <meta http-equiv="CONTENT-TYPE" CONTENT="text/html; charset=UTF-8">
  <title>StratusLab Persistent Disk Storage</title>
</head>
  
  <body>

    <table>  
      <thead>
        <tr>
          <td>name</td>
          <td>uid</td>
          <td>cn</td>
        </tr>
      </thead>
      <tbody>
        <#list users as user>
        <tr>
          <td>${user.name}</td>
          <td><a href="${user.uid}">${user.uid}</a></td>
          <td>${user.cn}</td>
        </tr>
        </#list>
      </tbody>
    </table>

   </body>
</html>
