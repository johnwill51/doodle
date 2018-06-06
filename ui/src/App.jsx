import * as React from "react";
import PropTypes from "prop-types";

import "./App.css";
import ChatPanel from "./ChatPanel";
import ServerDown from "./ServerDown";

const VIEW = {
    CHAT: 'CHAT',
    SERVER_DOWN: 'SERVER_DOWN'
};

class App extends React.Component {
    
    constructor(props) {
        super(props);
        this.state = {
            messages: [
                /*{
                    author: "Marzio",
                    datetime: new Date(2018, 2, 4).getTime(),
                    content: "slkjhfgalksfhg jhjlkdfh ljkh sdfh jklasjg "
                },
                {
                    author: "Giulia",
                    datetime: new Date(2018, 2, 5).getTime(),
                    content: "slkjhfgalksfhg jhjlkdfh ljkh sdfh jklasjg "
                },
                {
                    author: "Eliana",
                    datetime: new Date(2018, 2, 12).getTime(),
                    content: "slkjhfgalksfhg jhjlkdfh ljkh sdfh jklasjg "
                }*/
            ],
            view: VIEW.SERVER_DOWN
        };

        this._onServerStart = this._onServerStart.bind(this);
        this._onServerStop = this._onServerStop.bind(this);
        this._onServerMessage = this._onServerMessage.bind(this);
        this._sendMessage = this._sendMessage.bind(this);
    }

    _onServerStart() {
        this.setState({ view: VIEW.CHAT });
    }

    _onServerStop() {
        this.setState({ view: VIEW.SERVER_DOWN });
    }

    _onServerMessage(message) {
        const { messages } = this.state
        this.setState({ messages: [...messages, message] });
    }

    _sendMessage(message) {
        const { context } = this.props;
        const { server } = context;
        server.send(message);
    }

    componentDidMount() {
        const { context } = this.props;
        const { server } = context;
        server.start();
        server.listen({
            onStart: this._onServerStart,
            onStop: this._onServerStop,
            onMessage: this._onServerMessage
        });
    }

    componentWillUnmount() {
        const { context } = this.props;
        const { server } = context;
        server.stop();
    }

    render() {
        const { context } = this.props;
        const { lang, text, user } = context;
        const { messages, view } = this.state;

        if (view === VIEW.CHAT) 
            return (
                <ChatPanel 
                    lang={lang}
                    messages={messages}
                    onSend={this._sendMessage}
                    text={text}
                    user={user}
                />
            );
        
        return <ServerDown />;
    }
}

App.propTypes = {
    context: PropTypes.shape({
        lang: PropTypes.string.isRequired,
        server: PropTypes.shape({
            listen: PropTypes.func.isRequired,
            send: PropTypes.func.isRequired,
            start: PropTypes.func.isRequired,
            stop: PropTypes.func.isRequired
        }).isRequired,
        text: PropTypes.shape({
            placeholder: PropTypes.shape({
                en: PropTypes.string.isRequired
            }),
            send: PropTypes.shape({
                en: PropTypes.string.isRequired
            })
        }).isRequired,
        user: PropTypes.shape({
            name: PropTypes.string.isRequired
        }).isRequired
    }).isRequired,
};

export default App;
