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
    let p1Paddle;
    let p2Paddle;
    let leftWallGridHelper;
    let rightWallGridHelper;
    let leftRightArrowState = "NONE";
    let upDownArrowState = "NONE";
    let playerPosition = "P1";
    let gameId = 0;
    let vx = 0;
    let vz = 0;
    let socket;

    function setCameraP1() {
        camera.up.set(0, 0, 1);
        camera.position.y = -100;
        camera.position.z = 65;
        camera.lookAt(new THREE.Vector3(0, 0, 35));
    }

    function setCameraP2() {
        camera.up.set(0, 0, 1);
        camera.position.y = 100;
        camera.position.z = 65;
        camera.lookAt(new THREE.Vector3(0, 0, 35));
    }

    function setWalls() {
        const wallGeometry = new THREE.BoxGeometry(1, 100, 100);
        const wallMaterial = new THREE.MeshLambertMaterial({color: 0x049ef4});
        const leftWall = new THREE.Mesh(wallGeometry, wallMaterial);
        const rightWall = new THREE.Mesh(wallGeometry, wallMaterial);
        leftWall.translateX(-50);
        leftWall.translateZ(50);
        rightWall.translateX(50);
        rightWall.translateZ(50);
        // leftWall.position = new THREE.Vector3(-50,-50,0);
        scene.add(leftWall);
        scene.add(rightWall);

        leftWallGridHelper = new THREE.GridHelper(100, 10);
        leftWallGridHelper.translateX(-49.4);
        leftWallGridHelper.translateZ(50);
        leftWallGridHelper.rotateZ(Math.PI / 2);
        scene.add(leftWallGridHelper);

        rightWallGridHelper = new THREE.GridHelper(100, 10);
        rightWallGridHelper.translateX(49.4);
        rightWallGridHelper.translateZ(50);
        rightWallGridHelper.rotateZ(Math.PI / 2);
        scene.add(rightWallGridHelper);
    }

    function setFloor() {
        const floorMaterial = new THREE.MeshLambertMaterial({color: 0x09702d});
        const floorGeometry = new THREE.PlaneGeometry(100, 100);
        const floor = new THREE.Mesh(floorGeometry, floorMaterial);
        scene.add(floor);
    }

    function setBall() {
        const ballMaterial = new THREE.MeshLambertMaterial({color: 0xA2294D});
        const ballGeometry = new THREE.SphereGeometry(4, 32, 32);
        ball = new THREE.Mesh(ballGeometry, ballMaterial);
        ball.translateZ(20);
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

    function getPaddleCoords() {
        console.log("p1Paddle x: " + p1Paddle.position.x);
        console.log("p1Paddle y: " + p1Paddle.position.y);
        console.log("p1Paddle z: " + p1Paddle.position.z);
    }

    function setKeyHandler() {
        window.addEventListener("keydown", keyDownHandler);
        window.addEventListener("keyup", keyUpHandler);
    }
    function keyDownHandler(keyboardEvent) {
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

    function setupWebSockets() {
        socket = new WebSocket("ws://localhost:8080/game_connect");
        socket.addEventListener("open", function (event) {
            console.log("socket open event:");
            console.log(event);
        });
        socket.addEventListener("message", webSocketMessage);
        socket.addEventListener("close", console.log);
    }
    function sendControlStateMessage() {
        const gameStateJson ={
            "gameId": gameId,
            "udState": upDownArrowState,
            "lrState": leftRightArrowState,
            "time": new Date(),
            "playerPosition": playerPosition
        };
        const fullMessage = {"type": "playerControlMessage", "data": gameStateJson };
        const fullMessageString = JSON.stringify(fullMessage);
        console.log("sending message: " +fullMessageString);
        socket.send(fullMessageString);
    }
    function webSocketMessage(event) {
        let sampleMessage = {"paused":false,"p1Score":0,"p2Score":0,"p1PaddleX":0.0,"p1PaddleY":0.0,"p1PaddleZ":0.0,"p1PaddleVx":0.0,"p1PaddleVy":0.0,"p1PaddleVz":0.0,"p2PaddleX":0.0,"p2PaddleY":0.0,"p2PaddleZ":0.0,"p2PaddleVx":0.0,"p2PaddleVy":0.0,"p2PaddleVz":0.0,"ballX":0.0,"ballY":0.0,"ballZ":0.0,"ballVx":0.0,"ballVy":0.0,"ballVz":0.0}
        console.log(event);
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

    function animate(timestamp) {
        ball.position.x = ((timestamp / 25 + 50) % 100) - 50;
        ball.position.y = ((timestamp / 50 + 50) % 100) - 50;
        vx = {
            "RIGHT": Math.min(vx + 0.1, 2),
            "LEFT": Math.max(vx - 0.1, -2),
            "BOTH": 0.90 * vx,
            "NONE": 0.90 * vx
        }[leftRightArrowState];
        if (p1Paddle.position.x > 50) vx = Math.min(vx, 0);
        if (p1Paddle.position.x < -50) vx = Math.max(vx, 0);
        p1Paddle.translateX(vx);
        vz = {
            "UP": Math.min(vz + 0.1, 2),
            "DOWN": Math.max(vz - 0.1, -2),
            "BOTH": 0.90 * vz,
            "NONE": 0.90 * vz
        }[upDownArrowState];
        if (p1Paddle.positionZ > 50) vz = Math.min(vz, 0);
        if (p1Paddle.positionZ < -50) vz = Math.max(vz, 0);
        p1Paddle.translateZ(vz);
        renderer.render(scene, camera);
        requestAnimationFrame(animate);
    }

    function resize() {
        console.log("window resized");
        const gameContainer = document.querySelector("#game-container");
        renderer.setSize(gameContainer.clientWidth, gameContainer.clientHeight);
        requestAnimationFrame(animate);
    }

    exports.addEventListener("load", start);
    exports.addEventListener("resize", resize);
    exports.setCameraP1 = setCameraP1;
    exports.setCameraP2 = setCameraP2;
})(window);
