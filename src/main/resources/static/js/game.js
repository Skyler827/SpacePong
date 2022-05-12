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
    let leftRightArrowState = "none";
    let upDownArrowState = "none";
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
                    "left": "left",
                    "right": "both",
                    "both": "both",
                    "none": "left"
                }[leftRightArrowState];
                break;
            case "ArrowRight":
            case "KeyD":
                leftRightArrowState = {
                    "left": "both",
                    "right": "right",
                    "both": "both",
                    "none": "right"
                }[leftRightArrowState];
                break;
            case "ArrowUp":
            case "KeyW":
                upDownArrowState = {
                    "up": "up",
                    "down": "both",
                    "both": "both",
                    "none": "up"
                }[upDownArrowState];
                break;
            case "ArrowDown":
            case "KeyS":
                upDownArrowState = {
                    "up": "both",
                    "down": "down",
                    "both": "both",
                    "none": "down"
                }[upDownArrowState];
                break;
            default:
                break; // no action required
        }
    }
    function keyUpHandler(keyboardEvent) {
        switch (keyboardEvent.code) {
            case "ArrowLeft":
            case "KeyA":
                leftRightArrowState = {
                    "left": "none",
                    "right": "right",
                    "both": "right",
                    "none": "none"
                }[leftRightArrowState];
                break;
            case "ArrowRight":
            case "KeyD":
                leftRightArrowState = {
                    "left": "left",
                    "right": "none",
                    "both": "left",
                    "none": "none"
                }[leftRightArrowState];
                break;
            case "ArrowUp":
            case "KeyW":
                upDownArrowState = {
                    "up": "none",
                    "down": "down",
                    "both": "down",
                    "none": "none"
                }[upDownArrowState];
                break;
            case "ArrowDown":
            case "KeyS":
                upDownArrowState = {
                    "up": "up",
                    "down": "none",
                    "both": "up",
                    "none": "none"
                }[upDownArrowState];
                break;
            default:
                break; //no action required
        }
    }

    function setupWebSockets() {
        socket = new WebSocket("ws://localhost:8080/gameConnect");
        socket.addEventListener("open", async function (event) {
            console.log("socket opened, sent 'hello server' to server.");
            console.log("event:");
            console.log(event);
            socket.send("hello server!");
        });
        socket.addEventListener("message", webSocketMessage);

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
            "right": Math.min(vx + 0.1, 2),
            "left": Math.max(vx - 0.1, -2),
            "both": 0.90 * vx,
            "none": 0.90 * vx
        }[leftRightArrowState];
        if (p1Paddle.position.x > 50) vx = Math.min(vx, 0);
        if (p1Paddle.position.x < -50) vx = Math.max(vx, 0);
        p1Paddle.translateX(vx);
        vz = {
            "up": Math.min(vz + 0.1, 2),
            "down": Math.max(vz - 0.1, -2),
            "both": 0.90 * vz,
            "none": 0.90 * vz
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
})(window);
