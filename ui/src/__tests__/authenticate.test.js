import authenticate from "../authenticate";

describe("authenticate", () => {

    let locationReplaceSpy;

    beforeEach(() => {
        locationReplaceSpy = jest.spyOn(global.location, "replace")
            .mockImplementation(() => {});
    });

    afterEach(() => {
        jest.clearAllMocks();
        global.document.cookie = "";
    });

    it("when no username cookie redirects to login", () => {
        authenticate.assertAuthorized();
        expect(locationReplaceSpy).toHaveBeenCalledWith("./login");
    });

    it("when username cookie does not redirect to login", () => {
        global.document.cookie = "; username=any";
        authenticate.assertAuthorized();
        expect(locationReplaceSpy).not.toHaveBeenCalled();
    });

    function createCookieMock() {
        
    };
});