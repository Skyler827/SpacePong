// what is the purpose of this file?
// it's to let the server know that this user is online

let webSocketSession;
function windowOpen() {
    connect();
}
function reconnect() {
    connect();
}
function connect() {
    webSocketSession = new WebSocket("ws://localhost:8080/register_presence");
    webSocketSession.addEventListener("open", sessionOpen);
    webSocketSession.addEventListener("message", handleData);
    webSocketSession.addEventListener("close", websocketClosedByServer);
}

/**
 * When the WebSession begins, reveal the sidebar, show the connected elements,
 * and hide the elements indicating disconnection.
 */
async function sessionOpen () {
    console.log("we did it! we knocked em over! (session open)");
    document.querySelector(".sidebar").classList.remove("visibility-hidden");
    document.querySelectorAll(".connected").forEach(element =>
        element.classList.remove("display-none"));
    document.querySelectorAll(".connection-pending").forEach(element =>
        element.classList.add("display-none"));
    document.querySelectorAll(".disconnected").forEach(element =>
        element.classList.add("display-none"));
}
async function handleData(message) {
    console.log("wohoo! new message:");
    console.log(message);
}
async function websocketClosedByServer(event) {
    console.log("dude, websocket closed by the server with the following event:");
    console.log(event);
    webSocketSession = null;
    document.querySelectorAll(".disconnected").forEach(element =>
        element.classList.remove("display-none"));
    document.querySelectorAll(".connected").forEach(element =>
        element.classList.add("display-none"));
    document.querySelectorAll(".connection-pending").forEach(element =>
        element.classList.add("display-none"));
}
async function browserClose() {
    if (webSocketSession) {
        console.log("running browserClose(), websocket session exists, closing it");
        webSocketSession.close();
    } else {
        console.log("running browserClose, websocketSession does not exist");
    }
}

window.addEventListener("load", windowOpen);
window.addEventListener("beforeunload", browserClose);
