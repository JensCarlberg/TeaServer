<%@page import="se.liu.jenca01.teserver.BrewedTeas"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Teserver Statistik</title>
</head>
<body>
<p><a href="showLatestTeas.jsp">Till te-listan</a></p>
<p><a href="admin.jsp">Till adminstration</a></p>
<hr/>
Antal bryggda kannor: <%= BrewedTeas.instance().noOfTeas() %>
<table>
<%
String[][] teaStats = BrewedTeas.instance().sumTeaDay();
String[] bgColours = new String[] { "#eee", "#ccc" };
for (int i=0; i<teaStats.length; i++) {
    out.print("<tr bgcolor='" + bgColours[i % bgColours.length] + "'>");
    for (int j=0; j<teaStats[i].length; j++) {
        out.print("<td>");
        out.print(teaStats[i][j]);
        out.print("</td>");
    }
    out.print("</tr>");
}
%>
</table>
</body>
</html>