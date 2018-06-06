import React from "react";
import { mount } from "enzyme";

import ChatPanel from "../ChatPanel";

describe("<ChatPanel />", () => {

    let wrapper;
    let props;

    beforeEach(() => {
        props = {
            lang: "en",
            messages: [
                {
                    author: "New",
                    content: "The tide is high",
                    datetime: 123
                },
                {
                    author: "Dubby",
                    content: "but [Listen] I'm moving on",
                    datetime: 456
                }
            ],
            onSend: jest.fn(),
            text: {
                placeholder: { en: "placeholder text" },
                send: { en: "send text" }
            },
            user: { name: "Dubby" }
        };

        wrapper = mount(<ChatPanel {...props} />)
    });

    it("render messages", () => {
        expect(wrapper.find(".messages").html()).toBe(
            `<div class="messages">` +
                `<ul>` +
                    `<li class="them">` +
                        `<div class="author">New</div>` +
                        `<div class="content">The tide is high</div>` +
                        `<div class="datetime">1970-01-01T00:00:00.123Z</div>` +
                    `</li>` +
                    `<li class="me">` +
                        `<div class="author">Dubby</div>` +
                        `<div class="content">but [Listen] I'm moving on</div>` +
                        `<div class="datetime">1970-01-01T00:00:00.456Z</div>` +
                    `</li>` +
                `</ul>` +
            `</div>`
        );
    });

    it("when textarea empty button send disabled", () => {
        expect(wrapper.find("textarea").value).toBe(undefined);
        expect(wrapper.find("button").props().disabled).toBe(true);
    });

    it("when textarea not empty button send not disabled", () => {
        wrapper.find("textarea").simulate("change", { target: { value: "a" }});
        expect(wrapper.find("button").props().disabled).toBe(false);
    });

    it("when click send button call onSend prop", () => {
        const dateTimeBeforeChange = new Date().getTime();

        wrapper.find("textarea").simulate("change", { target: { value: "ab" }});
        wrapper.find("button").simulate("click");

        const arg = props.onSend.mock.calls[0][0];
        expect(arg).toMatchObject({
            author: "Dubby",
            content: "ab",
        });
        expect(arg.datetime).toBeGreaterThanOrEqual(dateTimeBeforeChange);
    });

    it("when click send button call empty textarea", () => {
        wrapper.find("textarea").simulate("change", { target: { value: "ab" }});
        wrapper.find("button").simulate("click");
        wrapper.update();
        expect(wrapper.find("textarea").value).toBe(undefined);
    });

});