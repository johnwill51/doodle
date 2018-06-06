import createWebsocketHandler from "./websocket";

function createServer({
    wsHostname,
    wsPort,
    wsPath,
    log,
    fromServer = JSON.parse,
    toServer = JSON.stringify
}) {
    const ws = createWebsocketHandler({
        hostname: wsHostname,
        port: wsPort,
        path: wsPath,
        log
    });

    return {
        listen: ({ onStart, onStop, onMessage }) => 
            ws.listen({ 
                onOpen: onStart, 
                onClose: onStop, 
                onMessage: onMessage
                    ? data => onMessage(fromServer(data))
                    : onMessage
            }),
        send: data => ws.send(toServer(data)),
        start: () => ws.open(),
        stop: () => ws.close()
    };
}

export default createServer;
