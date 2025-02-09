var stompClient = null;
var username = null;
function connect() {
    username = document.getElementById('name').value.trim();
    if (username) {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    } else {
        alert('Please enter a username.');
    }
}

function onConnected() {
    console.log('Connected to WebSocket');
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )

    document.querySelector('.chat-container').style.display = 'block';
    document.querySelector('.join-container').style.display = 'none';
}

function onError(error) {
    console.error('Could not connect to WebSocket server. Please refresh this page to try again!', error);
}

// 페이지 로드 시 연결 시도
document.addEventListener('DOMContentLoaded', function() {
    var connectButton = document.querySelector('#connectButton');
    if (connectButton) {
        connectButton.addEventListener('click', connect);
    }
});

function sendMessage() {
    var messageInput = document.getElementById('message');
    var messageContent = messageInput.value.trim();
    if(messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');
        var usernameElement = document.createElement('strong');
        usernameElement.textContent = message.sender;
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('span');
    textElement.textContent = message.content;
    messageElement.appendChild(textElement);

    document.getElementById('messageArea').appendChild(messageElement);
}

// Event listeners
document.querySelector('#joinForm').addEventListener('submit', function(event) {
    event.preventDefault();
    connect();
});

document.querySelector('#chatForm').addEventListener('submit', function(event) {
    event.preventDefault();
    sendMessage();
});