<%@ page import="password.pwm.error.PwmException" %>
<%@ page import="password.pwm.http.JspUtility" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%--
  ~ Password Management Servlets (PWM)
  ~ http://code.google.com/p/pwm/
  ~
  ~ Copyright (c) 2006-2009 Novell, Inc.
  ~ Copyright (c) 2009-2014 The PWM Project
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  --%>

<!DOCTYPE html>
<%@ page language="java" session="true" isThreadSafe="true" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="pwm" prefix="pwm" %>
<html dir="<pwm:LocaleOrientation/>">
<%@ include file="/WEB-INF/jsp/fragment/header.jsp" %>
<%
    List<Locale> localeList = Collections.emptyList();
    PwmApplication localeselect_pwmApplication = null;
    try {
        localeselect_pwmApplication = PwmRequest.forRequest(request, response).getPwmApplication();
        localeList = localeselect_pwmApplication.getConfig().getKnownLocales();
    } catch (PwmException e) {
        /* noop */
    }
%>
<body class="nihilo">
<div id="wrapper">
    <jsp:include page="/WEB-INF/jsp/fragment/header-body.jsp">
        <jsp:param name="pwm.PageName" value="Title_LocaleSelect"/>
    </jsp:include>
    <div id="centerbody">
        <br/>
        <% for (final Locale locale : localeList) { %>
        <% final String flagCode = localeselect_pwmApplication.getConfig().getKnownLocaleFlagMap().get(locale); %>
        <div style="text-align: center; width: 100%">
            <img alt="flag" src="<%=request.getContextPath()%><pwm:url url='/public/resources/flags/png/'/><%=flagCode%>.png"/>
            <a href="<%=request.getContextPath()%>?<%=localeselect_pwmApplication.getConfig().readAppProperty(password.pwm.AppProperty.HTTP_PARAM_NAME_LOCALE)%>=<%=locale.toString()%>">
                <%=locale.getDisplayName()%> - <%=locale.getDisplayName(locale)%>
            </a>
        </div>
        <br/>
        <% } %>
    </div>
    <div class="push"></div>
</div>
<pwm:script>
    <script type="text/javascript">
        PWM_GLOBAL['startupFunctions'].push(function(){
        });
    </script>
</pwm:script>
<% JspUtility.setFlag(pageContext, PwmRequest.Flag.HIDE_LOCALE); %>
<%@ include file="/WEB-INF/jsp/fragment/footer.jsp" %>
</body>
</html>
