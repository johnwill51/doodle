function createWebsocketHandler({
    hostname,
    port,
    path,
    log
}) {
    let ws;
    let listeners;

    function onOpen(event) {
        log.info("WS open", event);
        listeners.forEach(listener => 
            listener.onOpen && listener.onOpen()
        );
    }

    function onClose(event) {
        log.info("WS close", event);
        listeners.forEach(listener => 
            listener.onClose && listener.onClose()
        );
    }

    function onMessage(event) {
        log.info("WS receive message", event);
        listeners.forEach(listener => 
            listener.onMessage && listener.onMessage(event.data)
        );
    }

    return {
        open() {
            ws = new WebSocket(`ws://${hostname}:${port}${path}`);
            ws.onopen = onOpen;
            ws.onclose = onClose;
            ws.onmessage = onMessage;
            listeners = [];
        },
        close() {
            log.info("WS close");
            // The client is going away, 
            // perhaps the browser is navigating away from the page
            ws.close(1001);
        },
        listen({ onOpen, onClose, onMessage }) {
            listeners.push({ onOpen, onClose, onMessage });
        },
        send(message) {
            log.info("WS send message", message);
            ws.send(message);
        }
    };
}

export default createWebsocketHandler;
