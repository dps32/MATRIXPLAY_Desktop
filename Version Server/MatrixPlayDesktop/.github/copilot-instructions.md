# Copilot / AI agent instructions — MatrixPlay Desktop (client)

Short, actionable notes to help an AI editing or navigating this repo.

- Project type: Java desktop application using JavaFX (FXML). Built with Maven.
- Main UI entry: `com.client.Main` (JavaFX Stage & view wiring in `UtilsViews`).
- Views: `src/main/resources/assets/*.fxml` (examples: `logView.fxml`, `waitView.fxml`, `countdownView.fxml`, `gameView.fxml`).
- Controllers: `src/main/java/com/client/*` — look at `LogCtrl`, `GameCtrl`, `CountdCtrl`, `CtrlWait` for view logic and app flow.

Quick architecture summary
- Single-process JavaFX client. UI is orchestrated by `UtilsViews` which loads FXML and returns controllers.
- Networking: WebSocket client is a singleton wrapper `UtilsWS` used via `Main.wsClient`. JSON messages flow between server and UI. Message handling is centralized in `Main.wsMessage(String)` and routed to controller methods (e.g., `gameCtrl.updateGameState(...)`).
- Configuration persists to disk at runtime in `data/clientConfig.json` via `Config` and model class `com.shared.ClientData`.

Key patterns and files to reference
- UI/View router: `UtilsViews` — use `UtilsViews.setView("ViewLog")` or `setViewAnimating(...)` to change screens.
- WebSocket usage: `UtilsWS.getSharedInstance(url)` is used; the app resets the singleton with reflection in `Main.createNewWebSocketInstance(...)` before connecting. Callbacks: `onMessage`, `onOpen`, `onError`, `onClose` are used in `Main.connectToServer()`.
- Message format: server <-> client JSON; typical message `type` values seen in code: `welcome`, `playerAssigned`, `countdown`, `gameState`, `waiting`, `playerNames`.

Build / run developer workflows (concrete)
- Run the app (recommended):
  - Ensure JavaFX jars exist in local Maven repo (~/.m2). The `run.sh` script locates JavaFX jars and sets `MAVEN_OPTS` with `--module-path`.
  - Start the client UI:
    - Example: `./run.sh com.client.Main` (this invokes `mvn exec:java -PrunMain -Dexec.mainClass=com.client.Main -Djavafx.platform=<os>`).
  - To build a shaded JAR (script supports this): `./run.sh <main-class> build` — produces `target/server-package.jar` per `run.sh` (verify `pom.xml` settings if this is needed).

Important pom notes / caveats
- `pom.xml` contains a `maven-shade-plugin` configured to create `server-package` and sets a manifest `mainClass` to `com.server.Main` — this looks inconsistent with the client main (`com.client.Main`). Treat these as potentially outdated; verify before changing packaging.
- `javafx-maven-plugin` config shows `mainClass` `com.clientFX.Main` (also likely stale). When changing run or packaging behavior, prefer updating `pom.xml` to point to the real start class used by developers.

What to change cautiously
- Avoid renaming or moving FXML files without updating `UtilsViews.addView(...)` calls in `Main.start(...)`.
- `UtilsWS` is a singleton manipulated via reflection in `Main` to reset it; refactors around the websocket lifecycle must preserve that reset behavior or replace it with a safer lifecycle API.

Examples to copy when editing
- Switch UI view (safe, repeatable):
  - `UtilsViews.setView("ViewLog");`
  - `UtilsViews.setViewAnimating("ViewCountD");`
- Send JSON on open (see `Main.connectToServer()`):
  - `JSONObject confirmation = new JSONObject(); confirmation.put("type","clientConfirmation"); confirmation.put("name", namePlayerDesktop); wsClient.safeSend(confirmation.toString());`

Where to look for more context
- UI flow and callbacks: `src/main/java/com/client/Main.java`
- WebSocket impl: `src/main/java/com/client/UtilsWS.java`
- FXML and images: `src/main/resources/assets/*`, `src/main/resources/icons/*`.
- Local configuration: `data/clientConfig.json` and loader `Config.java`.

If anything is ambiguous for a change (packaging mainClass, JavaFX launch options, or server message formats), ask the human maintainer and point to these files as the source of truth.

— End of auto-generated agent hints —

Please review and tell me any extra conventions or workflows you'd like included (for example: exact Java/JDK versions, CI commands, or developer build flags).
