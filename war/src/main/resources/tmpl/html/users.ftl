<html>

  <head>
    <meta http-equiv="CONTENT-TYPE" CONTENT="text/html; charset=utf-8">
    <title>Registration Form</title>
    <script src="http://cdn.jquerytools.org/1.2.5/jquery.tools.min.js"></script>
    
    <!-- standalone page styling (can be removed) --> 
    <link rel="stylesheet" type="text/css" href="../docs/stratuslab.css"/>   
    
    <!-- tab styling --> 
    <link rel="stylesheet" type="text/css" href="http://static.flowplayer.org/tools/css/tabs.css" /> 
    
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
