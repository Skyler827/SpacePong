// the purpose of this file:
// is to inform the server that the user is waiting for a requested game to start
// and to respond appropriately when the server affirms or denies the request

(function(exports) {
    let webSocketSession;
    function connect() {
        webSocketSession = new WebSocket("ws://localhost:8080/register_waiting");
        webSocketSession.addEventListener("open", sessionOpen);
        webSocketSession.addEventListener("message", handleData);
        webSocketSession.addEventListener("close", sessionClosed);
    }
    function sessionOpen() {}
    function handleData(message) {
        const parsedMessage = JSON.parse(message.data);
        switch (parsedMessage.type) {
            case "game_start":
                handleGameStart();
                break;
            case "game_reject":
                handleGameRejected();
                break;
            default:
                console.log("unexpected message:");
                console.log(message);
                break;
        }
    }
    function sessionClosed() {}
    function browserClose() {}
    function handleGameRejected() {
        const msg = "game rejected, redirecting back to home...";
        console.log(msg);
        exports.location.href = "/";
    }
    function handleGameStart() {
        const msg = "game accepted, starting...";
        console.log(msg);
        exports.location.href = "/game";
    }
    exports.addEventListener("load", connect);
    exports.addEventListener("beforeunload", browserClose);
})(window);