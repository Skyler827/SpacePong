// the purpose of this file:
// is to let the server know that this user is online
// update the list of online users on the sidebar
// and respond to requests for games by creating the notifications on screen

(function(exports) {
    let webSocketSession;
    const header = document.querySelector("meta[name=\"_csrf_header\"]").getAttribute("content");
    const token = document.querySelector("meta[name=\"_csrf\"]").getAttribute("content");

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
        const requestedGames = document.querySelector(".requested-games");
        const notificationDiv = document.createElement("div");
        requestedGames.append(notificationDiv);
        requestedGames.classList.remove("display-none");

        const p1 = document.createElement("p");
        notificationDiv.append(p1);
        p1.textContent = "New Game Request:";

        const p2 = document.createElement("p");
        notificationDiv.append(p2);
        p2.textContent = "User: "+newGameData.proposer;

        const ul = document.createElement("ul");
        notificationDiv.append(ul);
        const li1 = document.createElement("li");
        ul.append(li1);
        if (newGameData.isTimeLimited) {
            li1.textContent = "Time Limit: "+ newGameData.timeLimit+" minutes";
        } else {
            li1.textContent = "Time Limit: none";
        }
        const li2 = document.createElement("li");
        ul.append(li2);
        li2.textContent = "User: "+newGameData.proposer;

        const buttonContainer = document.createElement("div");
        notificationDiv.append(buttonContainer);
        buttonContainer.classList.add("accept-reject-buttons");

        const acceptForm = document.createElement("form");
        buttonContainer.append(acceptForm);
        const acceptId = "accept-form-"+encodeURIComponent(newGameData.proposer);
        acceptForm.id = acceptId;

        const acceptButton = document.createElement("button");
        acceptForm.append(acceptButton);
        acceptButton.textContent = "Accept";
        acceptButton.form = acceptId;
        acceptForm.addEventListener("submit", function(ev) {
            ev.preventDefault();
            const request = new XMLHttpRequest();
            request.open("post","/handle_proposal/accept?username="+encodeURIComponent(newGameData.proposer));
            request.setRequestHeader(header,token);
            request.send();
            request.addEventListener("load", function(e){
                exports.location = "/game";
            });
        });

        const rejectForm = document.createElement("form");
        buttonContainer.append(rejectForm);
        const rejectId = "reject-form-"+newGameData.proposer;
        rejectForm.id = rejectId;

        const rejectButton = document.createElement("button");
        rejectForm.append(rejectButton);
        rejectButton.textContent = "Reject";
        rejectButton.form = rejectId;

        rejectForm.addEventListener("submit",function(event) {
            event.preventDefault();
            const url = "/handle_proposal/reject?username="+encodeURIComponent(newGameData.proposer);
            const request = new XMLHttpRequest();
            request.open("POST", url, true);
            rejectButton.remove();
            const newSpan = document.createElement("span");
            newSpan.innerText = "Rejecting request...";
            buttonContainer.append(newSpan);
            request.addEventListener("load", ()=>notificationDiv.remove());
            console.log("_csrf: "+ header);
            console.log("_csrf_header: "+ token);
            request.setRequestHeader(header,token);
            request.send();
        });
    }
    function websocketClosedByServer(event) {
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
    function browserClose() {
        if (webSocketSession) {
            console.log("running browserClose(), websocket session exists, closing it");
            webSocketSession.close();
        } else {
            console.log("running browserClose, websocketSession does not exist");
        }
    }
    exports.addEventListener("load", connect);
    exports.addEventListener("beforeunload", browserClose);
    exports.connect = connect;
    exports.reconnect = reconnect;
})(window);
