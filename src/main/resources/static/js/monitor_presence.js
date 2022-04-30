// what is the purpose of this file?
// it's to let the server know that this user is online

let webSocketSession;
function windowOpen() {
    connect();
}
function reconnect() {
    if (webSocketSession) {
        webSocketSession.removeEventListener("open", sessionOpen);
        webSocketSession.removeEventListener("message", handleData);
        webSocketSession.removeEventListener("close", websocketClosedByServer);
        webSocketSession = null;
    }
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
    // console.log("just received the following data:");
    // console.log(message);
    let parsedMessage = JSON.parse(message.data);
    switch(parsedMessage.type) {
        case "username_list":
            handleUpdatedUserList(parsedMessage.data);
            break;
        case "notify_game_request":
            handleNewGameRequest(parsedMessage.data)
            console.log("new game requested:");
            console.log(parsedMessage.data);
            break;
        case "requested_game_start":
            handleGameStartSignal(parsedMessage.data);
            break;
        case undefined:
            console.log("message should have a type attribute");
            break;
        default:
            console.log("unexpected message type attribute: "+parsedMessage.type);
    }
}
function handleUpdatedUserList(updatedUserList) {
    const usersList = document.querySelector("ul.online-users-list");
    usersList.innerHTML = "";
    for (let onlineUser of updatedUserList) {
        const newLi = document.createElement("li")
        const newA = document.createElement("a");
        newA.textContent = onlineUser;
        newLi.appendChild(newA);
        usersList.appendChild(newLi);
    }
}
function handleNewGameRequest(newGameData) {
    const newDiv = document.createElement("div");

    const p1 = document.createElement("p");
    p1.textContent = "New Game Request:";
    newDiv.append(p1);

    const p2 = document.createElement("p");
    p2.textContent = "User: "+newGameData.proposer;
    newDiv.append(p2);

    const ul = document.createElement("ul");
    const li1 = document.createElement("li");
    if (newGameData.isTimeLimited) {
        li1.textContent = "Time Limit: "+ newGameData.timeLimit;
    } else {
        li1.textContent = "Time Limit: none";
    }
    ul.append(li1);
    const li2 = document.createElement("li");
    li2.textContent = "User: "+newGameData.proposer;
    ul.append(li2);
    newDiv.append(ul);

    const buttonContainer = document.createElement("div");
    buttonContainer.classList.add("accept-reject-buttons");
    const acceptButton = document.createElement("button");
    acceptButton.textContent = "Accept";
    acceptButton.id = "accept-form-"+newGameData.proposer;
    const rejectButton = document.createElement("button");
    rejectButton.textContent = "Reject";
    rejectButton.form = "reject-form-"+newGameData.proposer;
    buttonContainer.append(acceptButton);
    buttonContainer.append(rejectButton);
    newDiv.append(buttonContainer);

    const acceptForm = document.createElement("form");
    acceptForm.id = "accept-form-"+newGameData.proposer;
    acceptForm.action = "/handle_proposal/accept?username="+encodeURIComponent(newGameData.proposer);
    acceptForm.method = "POST";
    newDiv.append(acceptForm);

    const rejectForm = document.createElement("form");
    rejectForm.id = "reject-form-"+newGameData.proposer;
    rejectForm.addEventListener("click",function(event) {
        event.preventDefault();
        const url = "/handle_proposal/reject?username="+encodeURIComponent(newGameData.proposer);
        const request = new XMLHttpRequest();
        request.open("POST", url, true);
        rejectButton.remove();
        const newSpan = document.createElement("span");
        newSpan.innerText = "Rejecting request...";
        buttonContainer.append(newSpan);
        request.addEventListener("load", ()=>newDiv.remove());
    });
    newDiv.append(rejectForm);

    const requestedGames = document.querySelector(".requested-games");
    requestedGames.append(newDiv);
    requestedGames.classList.remove("display-none");

}
function handleGameStartSignal() {
    location.href = "/game";
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
