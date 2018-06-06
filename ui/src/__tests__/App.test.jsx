import React from "react";
import { mount } from "enzyme";

import App from "../App";
import ChatPanel from "../ChatPanel";
import ServerDown from "../ServerDown";

jest.mock("../ChatPanel", () => () => <div />);
jest.mock("../ServerDown", () => () => <div />);

describe("<App />", () => {

    let wrapper;
    let context;
    let server;

    function createServer() {
        let listener;

        return {
            listen: jest.fn().mockImplementation(l => (listener = l)),
            send: jest.fn(),
            start: jest.fn(),
            stop: jest.fn(),
            receiveStart() { listener.onStart(); },
            receiveStop() { listener.onStop(); },
            receiveMessage(message) { listener.onMessage(message); }
        }
    }

    beforeEach(() => {
        server = createServer();
        context = {
            lang: "en",
            server,
            text: {
                placeholder: { en: "placeholder text" },
                send: { en: "send text" }
            },
            user: { name: "username" }
        };

        wrapper = mount(<App context={context} />);
    });

    it("renders ServerDown", () => {
        expect(wrapper.find(ServerDown).exists()).toBe(true);
        expect(wrapper.find(ChatPanel).exists()).toBe(false);
    });

    it("when mount start server", () => {
        expect(context.server.start).toHaveBeenCalled();
    });

    it("when mount listen to server", () => {
        expect(context.server.listen).toHaveBeenCalled();
    });

    it("when unmount stop server", () => {
        wrapper.unmount();
        expect(context.server.stop).toHaveBeenCalled();
    });

    it("when server receive start render ChatPanel", () => {
        server.receiveStart();
        wrapper.update();
        expect(wrapper.find(ServerDown).exists()).toBe(false);
        expect(wrapper.find(ChatPanel).exists()).toBe(true);
    });

    it("when server receive stop render ServerDown", () => {
        server.receiveStart();
        wrapper.update();
        server.receiveStop();
        wrapper.update();
        expect(wrapper.find(ServerDown).exists()).toBe(true);
        expect(wrapper.find(ChatPanel).exists()).toBe(false);
    });

    it("when server receive start pass props to ChatPanel", () => {
        server.receiveStart();
        wrapper.update();
        expect(wrapper.find(ChatPanel).props()).toMatchObject({
            lang: context.lang,
            messages: [],
            text: context.text,
            user: context.user
        });
    });


    it("when ChatPanel onSend send to server", () => {
        const message = "any message";
        server.receiveStart();
        wrapper.update();
        wrapper.find(ChatPanel).props().onSend(message);
        expect(context.server.send).toHaveBeenCalledWith(message);
    });

    it(
        "when server receive message ChatPanel is rendered with message and " +
        "previous messages", 
        () => {
            server.receiveStart();
            wrapper.update();

            const message1 = {
                author: "New",
                content: "The tide is high",
                datetime: 123
            };
            server.receiveMessage(message1);
            wrapper.update();
            expect(
                wrapper.find(ChatPanel).props().messages
            ).toEqual([ message1 ]);

            const message2 = {
                author: "Dubby",
                content: "but [Listen] I'm moving on",
                datetime: 456
            };
            server.receiveMessage(message2);
            wrapper.update();
            expect(
                wrapper.find(ChatPanel).props().messages
            ).toEqual([ message1, message2 ]);
        }
    );

});