<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>Fehler</title>
</head>
<body>
<h2>Es ist ein Fehler aufgetreten</h2>
<p>${errorMessage}</p>
<p><a href="${pageContext.request.contextPath}/">Zur Startseite</a></p>
</body>
</html>
