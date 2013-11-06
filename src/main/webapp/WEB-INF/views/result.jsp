<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>My HTML View</title>
	<%-- <link href="<c:url value="/resources/form.css" />" rel="stylesheet"  type="text/css" />	 --%>	
</head>
<body>
<div class="success">
	response:
	<h3>result: "${result}"</h3>
</div>
</body>
</html>