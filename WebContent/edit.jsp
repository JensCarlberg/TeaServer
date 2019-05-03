<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="se.liu.jenca01.teserver.BrewedTeas"%>
<%@page import="se.liu.jenca01.teserver.servlets.AddTea"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Teserver Te-editor</title>
</head>
<body>
<p><a href="showLatestTeas.jsp">Till te-listan</a></p>
<p><a href="stats.jsp">Till statistiken</a></p>
<hr/>
<form action="Admin" method="post" accept-charset="UTF-8">
  <input type="submit" name="save" value="Spara">
  <textarea name="all-teas" rows="<%= BrewedTeas.instance().noOfTeas() %>" cols="150"><%= AddTea.getAllTeas() %></textarea>
  <input type="submit" name="save" value="Spara">
</form>
</body>
</html>