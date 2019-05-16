<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Teserver Admin</title>
</head>
<body>
<p><a href="showLatestTeas.jsp">Till te-listan</a></p>
<p><a href="stats.jsp">Till statistiken</a></p>
<p><a href="edit.jsp">Editera téer</a></p>
<hr/>
<form action="Admin" method="post">
<input type="submit" name="clear" value="Rensa gamla téer">
<input type="submit" name="reload" value="Ladda om téer från fil">
<input type="submit" name="remove" value="Ta bort gammal te-fil">
<input type="submit" name="get" value="Hämta te-fil">
<hr/>
<input type="submit" name="reloadNewFormat" value="Ladda om téer från full-formats (full-tea-reload.log) fil">
<input type="submit" name="getNewFormat" value="Hämta full-formats te-fil">
</form>
<hr/>
<form action="Upload" method="post" enctype="multipart/form-data">
<input type="file" name="teas" size="60"><br>
<input type="submit" name="load" value="Ladda upp te-fil">
</form>
<hr/>
<% 
String msg = (String) session.getAttribute("admin_message");
if (msg != null) {
  out.println(msg);
  session.removeAttribute("admin_message");
}
%>
</body>
</html>