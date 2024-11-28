<!DOCTYPE html>
<html>
    <head>
        <title>FLYS - Login</title>
        <link href="FLYS.css" type="text/css" rel="stylesheet">
    </head>

    <body>
        <form method="POST" action="<%= request.getContextPath() + "/flys/login" %>" id="authentication">
            <h1>FLYS Anmeldung</h1>
            <% String error = request.getParameter("error"); %>
            <% if (error != null) { %>
                <div class="error">
                    <h2>Authentifizierung fehlgeschlagen</h2>
                    <div class="details">
                        <%= error %>
                    </div>
                </div>
            <% } %>
            <div>Bitte geben Sie eine Benutzerkennung und ein Passwort ein.</div>
            <table>
                <tr>
                    <td><label for="username">Benutzername: </label></td>
                    <td><input type="text" name="username" /></td>
                </tr>
                <tr>
                    <td><label for="password">Passwort: </label></td>
                    <td><input type="password" name="password" /></td>
                </tr>
            </table>
            <input type="submit" class="sendButton" value="Anmelden"/>
        </form>
    </body>
</html>
