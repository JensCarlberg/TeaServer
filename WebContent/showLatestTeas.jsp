<%@page import="se.liu.jenca01.teserver.BrewedTeas"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
BrewedTeas teas = BrewedTeas.instance();
String[][] latest = teas.latestTeasArray(4);
int noOfTeas = teas.noOfTeas();
%>
<html>
  <head>
    <title>Bryggda téer</title>
  </head>
  <body>
    <FORM action="Testart" method="post">
      <INPUT name="Te" type="text" size="30" accesskey="t" placeholder="Tesort" autofocus required/>
      <INPUT name="Volym" type="text" size="4" accesskey="v" placeholder="Antal liter" />
      <INPUT type="submit" value="OK"/>
    </FORM>
    <HR />
    <TABLE>
      <TR><th>Tid</th><th>Te</th></TR>
      <% for (String[] tea : latest) { %>
      <tr><td><%= tea[0] %></td><td><%= tea[1] %></td></tr>
      <% } %>
    </TABLE>
    <HR />
    <p>Totalt antal bryggda kannor te: <%= noOfTeas %></p>
    <HR />
    <FORM action="Testart" method="post">
      <INPUT name="Te" type="text" size="30" accesskey="t" placeholder="Tesort" autofocus required/>
      <INPUT name="Volym" type="text" size="4" accesskey="v" placeholder="Antal liter" />
      <INPUT type="submit" value="OK"/>
    </FORM>
    <HR />
    <p><a href="admin.jsp">Admin</a> <a href="stats.jsp">Statistiken</a></p>
  </body>
</html>
