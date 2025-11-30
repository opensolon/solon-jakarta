<%@ page import="java.util.Random" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${title}</title>
</head>
<body>
<div>
    page path: ${ctx.path()}
</div>
<div>
    properties: custom.user :${user}
</div>
<div>
    ${m.name} : ${message} （我想<a href="/jinjin.htm">静静</a>）
</div>
</body>
</html>