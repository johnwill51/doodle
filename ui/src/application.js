import * as React from "react";
import * as ReactDOM from "react-dom";

import "./reset.css";

import App from "./App";
import authenticate from "./authenticate";
import createServer from "./server";
import createLogger from "./log";

const userName = authenticate.assertAuthorized();

const context = { 
    lang: "en",
    server: createServer({
        wsHostname: location.hostname,
        wsPort: 9001,
        wsPath: "/messages",
        log: createLogger(),
    }),
    text: {
        placeholder: {
            en: "Please, be nice!"
        },
        send: {
            en: "Send"
        }
    },
    user: {
        name: userName
    }
};

const appNode = document.createElement("div");
document.body.appendChild(appNode);

ReactDOM.render(<App context={context} />, appNode);
