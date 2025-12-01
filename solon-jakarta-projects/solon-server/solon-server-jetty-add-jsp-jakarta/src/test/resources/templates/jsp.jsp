<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ct" uri="/tags" %>
<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
</head>
<body>
jsp::${msg}；i18n::${i18n.get("login.title")}

<ct:footer />
</body>
</html>