import * as React from "react";
import PropTypes from "prop-types";

class ChatPanel extends React.Component {
    
    constructor(props) {
        super(props);
        this.state = {
            contentToSend: undefined
        };

        this._canSend = this._canSend.bind(this);
        this._sendAction = this._sendAction.bind(this);
        this._writeAction = this._writeAction.bind(this);
    }

    _canSend() {
        return !!this.state.contentToSend;
    }

    _sendAction() {
        const { onSend, user } = this.props;
        const { contentToSend } = this.state;
        if (this._canSend()) {
            onSend({
                author: user.name,
                datetime: new Date().getTime(),
                content: contentToSend
            });
            this.setState({ contentToSend: undefined })
        }    
    }

    _writeAction(contentToSend) {
        this.setState({ contentToSend });
    }

    render() {
        const { lang, messages, text, user } = this.props;
        const { contentToSend = "" } = this.state;
        return (
            <section className="app">
                <div className="messages">
                    <ul>
                    {
                        messages.map(message => {
                            const liClassName = user.name === message.author
                                ? "me"
                                : "them";
                            const { author, content, datetime } = message;
                            const displayDateTime = formatDateTime(datetime);
                            return (
                                <li 
                                    key={displayDateTime}
                                    className={liClassName}
                                >
                                    <div className="author">{author}</div>
                                    <div className="content">{content}</div>
                                    <div className="datetime">{displayDateTime}</div>
                                </li>
                            );
                        })
                    }
                    </ul>
                </div>
                <div className="controls">
                    <textarea 
                        rows="4" 
                        cols="20" 
                        value={contentToSend}
                        onChange={e => this._writeAction(e.target.value)}
                        placeholder={text.placeholder[lang]}
                    />
                    <button
                        type="button"
                        onClick={this._sendAction}
                        onKeyDown={e => {
                            if (e.charCode === 13)
                                this._sendAction();
                        }}
                        disabled={!this._canSend()}
                    >
                        {text.send[lang]}
                    </button>
                </div>
            </section>
        );
    }
}

ChatPanel.propTypes = {
    lang: PropTypes.string.isRequired,
    messages: PropTypes.arrayOf(PropTypes.shape({
        author: PropTypes.string.isRequired, 
        content: PropTypes.string.isRequired,
        datetime: PropTypes.number.isRequired
    })),
    onSend: PropTypes.func.isRequired,
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
};

function formatDateTime(datetime) {
    return new Date(datetime).toISOString();
}

export default ChatPanel;
