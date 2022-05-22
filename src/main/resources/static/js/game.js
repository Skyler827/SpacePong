// The purpose of this file is to handle the game logic
// send control state messages out to the server
// and receive game state updates in from the server
(function(exports) {
    let scene;
    let camera;
    let renderer;
    let geometry;
    let material;
    let ball;
    let gameState = initialGameState();
    let lastTick;
    let p1Paddle;
    let p2Paddle;
    let floor;
    let leftRightArrowState = "NONE";
    let upDownArrowState = "NONE";
    let playerPosition = "P1";
    let gameId = 0;
    let scoreThreshold;
    let timeLimitMinutes;
    let isTimeLimited;
    let bounds = {};
    let socket;
    let paused = true;
    let gameAnimationFrameId;
    let pressedKeys;
    let gameOverFunctionCalled = false;
    const userAgent = navigator.userAgent;
    const firefoxX11 = userAgent.includes("Firefox") && userAgent.includes("Linux");
    console.log("firefoxX11: "+firefoxX11);
    pressedKeys = new Set();
    /*
    Positive X is P1's right, P2's left
    Negative X is P1's left, P2's right
    Positive Y is up for both players
    Negative Y is down for both players
    Positive Z is towards P1, away from P2
    Negative Z is away from P1, towards P2
     */

    function setCameraP1() {
        camera.up.set(0, 1, 0);
        camera.position.y = 50;
        camera.position.z = 130;
        camera.lookAt(new THREE.Vector3(0, 50, 0));
    }

    function setCameraP2() {
        camera.up.set(0, 1, 0);
        camera.position.y = 50;
        camera.position.z = -130;
        camera.lookAt(new THREE.Vector3(0, 50, 0));
    }

    function setWalls() {
        const wallGeometry = new THREE.BoxGeometry(1, 100, 100);
        const wallMaterial = new THREE.MeshLambertMaterial({color: 0x049ef4});
        const leftWall = new THREE.Mesh(wallGeometry, wallMaterial);
        const rightWall = new THREE.Mesh(wallGeometry, wallMaterial);
        leftWall.translateX(-50);
        leftWall.translateY(50);
        rightWall.translateX(50);
        rightWall.translateY(50);
        // leftWall.position = new THREE.Vector3(-50,-50,0);
        scene.add(leftWall);
        scene.add(rightWall);

        const leftWallGridHelper = new THREE.GridHelper(100, 10);
        leftWallGridHelper.translateX(-49.4);
        leftWallGridHelper.translateY(50);
        leftWallGridHelper.rotateZ(Math.PI / 2);
        scene.add(leftWallGridHelper);

        const rightWallGridHelper = new THREE.GridHelper(100, 10);
        rightWallGridHelper.translateX(49.4);
        rightWallGridHelper.translateY(50);
        rightWallGridHelper.rotateZ(Math.PI / 2);
        scene.add(rightWallGridHelper);
    }

    function setFloor() {
        const floorMaterial = new THREE.MeshLambertMaterial({color: 0x09702d});
        const floorGeometry = new THREE.PlaneGeometry(100, 100);
        floor = new THREE.Mesh(floorGeometry, floorMaterial);
        floor.rotateX(Math.PI/2);
        floor.translateY(-25);
        scene.add(floor);
    }

    function setBall() {
        const ballMaterial = new THREE.MeshLambertMaterial({color: 0xA2294D});
        const ballGeometry = new THREE.SphereGeometry(4, 32, 32);
        ball = new THREE.Mesh(ballGeometry, ballMaterial);
        scene.add(ball);
    }

    function setPaddles() {
        const paddleMaterial = new THREE.MeshLambertMaterial({color: 0x42f5f5});
        const paddleGeometry = new THREE.BoxGeometry(20, 1, 10);
        p1Paddle = new THREE.Mesh(paddleGeometry, paddleMaterial);
        p1Paddle.translateZ(50);
        p1Paddle.translateY(-50);
        p1Paddle.translateX(5);
        scene.add(p1Paddle);

        p2Paddle = new THREE.Mesh(paddleGeometry, paddleMaterial);
        p2Paddle.translateZ(50);
        p2Paddle.translateY(50);
        p2Paddle.translateX(-30);
        scene.add(p2Paddle);
    }

    function setScene() {
        setWalls();
        setFloor();
        setBall();
        setPaddles();
    }

    function setKeyHandler() {
        window.addEventListener("keydown", keyDownHandler);
        window.addEventListener("keyup", keyUpHandler);
    }
    function keyDownHandler(keyboardEvent) {
        // there is a bug affecting firefox on linux that requiring the following workaround:
        // https://bugzilla.mozilla.org/show_bug.cgi?id=1594003
        if (firefoxX11) {
            if (pressedKeys.has(keyboardEvent.code)) return;
            pressedKeys.add(keyboardEvent.code);
        } else {
            if (keyboardEvent.repeat) return;
        }
        switch (keyboardEvent.code) {
            case "ArrowLeft":
            case "KeyA":
                leftRightArrowState = {
                    "LEFT": "LEFT",
                    "RIGHT": "BOTH",
                    "BOTH": "BOTH",
                    "NONE": "LEFT"
                }[leftRightArrowState];
                break;
            case "ArrowRight":
            case "KeyD":
                leftRightArrowState = {
                    "LEFT": "BOTH",
                    "RIGHT": "RIGHT",
                    "BOTH": "BOTH",
                    "NONE": "RIGHT"
                }[leftRightArrowState];
                break;
            case "ArrowUp":
            case "KeyW":
                upDownArrowState = {
                    "UP": "UP",
                    "DOWN": "BOTH",
                    "BOTH": "BOTH",
                    "NONE": "UP"
                }[upDownArrowState];
                break;
            case "ArrowDown":
            case "KeyS":
                upDownArrowState = {
                    "UP": "BOTH",
                    "DOWN": "DOWN",
                    "BOTH": "BOTH",
                    "NONE": "DOWN"
                }[upDownArrowState];
                break;
            default:
                return; // no action required
        }
        sendControlStateMessage();
    }
    function keyUpHandler(keyboardEvent) {
        if (firefoxX11) {
            pressedKeys.delete(keyboardEvent.code);
        }
        switch (keyboardEvent.code) {
            case "ArrowLeft":
            case "KeyA":
                leftRightArrowState = {
                    "LEFT": "NONE",
                    "RIGHT": "RIGHT",
                    "BOTH": "RIGHT",
                    "NONE": "NONE"
                }[leftRightArrowState];
                break;
            case "ArrowRight":
            case "KeyD":
                leftRightArrowState = {
                    "LEFT": "LEFT",
                    "RIGHT": "NONE",
                    "BOTH": "LEFT",
                    "NONE": "NONE"
                }[leftRightArrowState];
                break;
            case "ArrowUp":
            case "KeyW":
                upDownArrowState = {
                    "UP": "NONE",
                    "DOWN": "DOWN",
                    "BOTH": "DOWN",
                    "NONE": "NONE"
                }[upDownArrowState];
                break;
            case "ArrowDown":
            case "KeyS":
                upDownArrowState = {
                    "UP": "UP",
                    "DOWN": "NONE",
                    "BOTH": "UP",
                    "NONE": "NONE"
                }[upDownArrowState];
                break;
            default:
                return; //no action required
        }
        sendControlStateMessage();
    }

    function initialGameState() {
        return {
            "paused":true,
            "p1Score":0,
            "p2Score":0,
            "tickInstant":Date.now(),
            "p1PaddleX":0.0,
            "p1PaddleY":0.0,
            "p1PaddleZ":0.0,
            "p1PaddleVx":0.0,
            "p1PaddleVy":0.0,
            "p1PaddleVz":0.0,
            "p2PaddleX":0.0,
            "p2PaddleY":0.0,
            "p2PaddleZ":0.0,
            "p2PaddleVx":0.0,
            "p2PaddleVy":0.0,
            "p2PaddleVz":0.0,
            "ballX":0.0,
            "ballY":0.0,
            "ballZ":0.0,
            "ballVx":0.0,
            "ballVy":0.0,
            "ballVz":0.0
        };
    }
    function setupWebSockets() {
        socket = new WebSocket("ws://localhost:8080/game_connect");
        socket.addEventListener("open", function (event) {
            console.log("socket open event:");
            console.log(event);
        });
        socket.addEventListener("message", webSocketMessage);
        socket.addEventListener("close", handleClose);
    }
    function handleClose(event) {
        console.log(event);
        if (!paused) {
            cancelAnimationFrame(gameAnimationFrameId);
            gameAnimationFrameId = null;
            paused = true;
        }
    }
    function sendControlStateMessage() {
        const controlStateJson ={
            "gameId": gameId,
            "udState": upDownArrowState,
            "lrState": leftRightArrowState,
            "time": new Date(),
            "playerPosition": playerPosition
        };
        const fullMessage = {"type": "playerControlMessage", "data": controlStateJson };
        const fullMessageString = JSON.stringify(fullMessage);
        // console.log("sending message: " +fullMessageString);
        socket.send(fullMessageString);
    }
    function webSocketMessage(event) {
        let messageData = JSON.parse(event.data);
        switch(messageData["type"]) {
            case "initialization":
                handleInitializationMessage(messageData);
                break;
            case "gameState":
                handleGameStateMessage(messageData);
                break;
            case "gameOver":
                handleGameOver(messageData);
                break;
        }
    }
    function handleGameStateMessage(messageData) {
        gameState = messageData;
        lastTick = new Date(gameState["tickInstant"]);
        paused = gameState["paused"];
        document.querySelector("#p1-score").textContent = gameState.p1Score;
        document.querySelector("#p2-score").textContent = gameState.p2Score;
        if (paused) {
            cancelAnimationFrame(gameAnimationFrameId);
            gameAnimationFrameId = null;
        } else {
            gameAnimationFrameId = requestAnimationFrame(animate);
        }
    }
    function handleInitializationMessage(data) {
        console.log("initialization event:");
        console.log(data);
        playerPosition = data["playerPosition"];
        gameId = data["gameId"];
        scoreThreshold = data["scoreThreshold"];
        timeLimitMinutes = data["timeLimitMinutes"];
        isTimeLimited = data["isTimeLimited"];
        bounds = data["bounds"];
        switch(playerPosition) {
            case "P1":
                setCameraP1();
                break;
            case "P2":
                setCameraP2();
                break;
        }
    }
    function handleGameOver(data) {
        if (gameOverFunctionCalled) return;
        gameOverFunctionCalled = true;
        let msg;
        switch (data["reason"]) {
            case "SCORE":
            case "TIME":
                if (playerPosition === data["winner"]) {
                    msg = "You won the game! Congratulations!";
                } else {
                    msg = "You lost the game!";
                }
                break;
            case "P1DISCONNECT":
            case "P2DISCONNECT":
                msg = "game ended due to disconnect by "+data["reason"].substr(0,2);
                break;
        }
        alert(msg);
        window.location = "/";
    }

    async function start() {
        let loadingMessage = document.querySelector("#loading-message");
        loadingMessage.remove();
        const gameContainer = document.querySelector("#game-container");
        scene = new THREE.Scene();
        const light = new THREE.AmbientLight(0x808080); // soft white light
        scene.add(light);
        camera = new THREE.PerspectiveCamera(75, gameContainer.clientWidth / gameContainer.clientHeight, 0.1, 1000);
        camera.up.set(0, 0, 1);

        renderer = new THREE.WebGLRenderer();
        renderer.setSize(gameContainer.clientWidth, gameContainer.clientHeight);
        gameContainer.append(renderer.domElement);
        gameContainer.appendChild(renderer.domElement);
        geometry = new THREE.BoxGeometry();
        material = new THREE.MeshBasicMaterial({color: 0x00ff00});
        setCameraP1();
        setScene();
        setKeyHandler();
        setupWebSockets();
        animate(0);
    }

    function moveObjects() {
        /** number of seconds since previous update */
        const dt = (new Date() - lastTick)/100;

        // I know it would be possible to replace these statements with a double loop
        // but its more readable this way
        ball.position.x = gameState.ballX + dt * gameState.ballVx;
        ball.position.y = gameState.ballY + dt * gameState.ballVy;
        ball.position.z = gameState.ballZ + dt * gameState.ballVz;

        p1Paddle.position.x = gameState.p1PaddleX + dt * gameState.p1PaddleVx;
        p1Paddle.position.y = gameState.p1PaddleY + dt * gameState.p1PaddleVy;
        p1Paddle.position.z = gameState.p1PaddleZ + dt * gameState.p1PaddleVz;

        p2Paddle.position.x = gameState.p2PaddleX + dt * gameState.p2PaddleVx;
        p2Paddle.position.y = gameState.p2PaddleY + dt * gameState.p2PaddleVy;
        p2Paddle.position.z = gameState.p2PaddleZ + dt * gameState.p2PaddleVz;

        floor.position.y = Math.sin(10*dt)
    }
    function animate() {
        moveObjects();
        renderer.render(scene, camera);
        if (paused) return;
        gameAnimationFrameId = requestAnimationFrame(animate);
    }

    function resize() {
        console.log("window resized");
        const gameContainer = document.querySelector("#game-container");
        renderer.setSize(gameContainer.clientWidth, gameContainer.clientHeight);
        gameAnimationFrameId = requestAnimationFrame(animate);
    }
    function pause() {
        socket.send(JSON.stringify({type:"pause"}));
    }
    function unpause() {
        socket.send(JSON.stringify({type:"unpause"}));
    }

    exports.addEventListener("load", start);
    exports.addEventListener("resize", resize);
    exports.setCameraP1 = setCameraP1;
    exports.setCameraP2 = setCameraP2;
    exports.pause = pause;
    exports.unpause = unpause;
})(window);
