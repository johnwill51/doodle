module.exports = {
    moduleNameMapper: {
        "\\.(css|less)$": "<rootDir>/__mocks__/styleMocks.js"
    },
    rootDir: "src",
    setupTestFrameworkScriptFile: "<rootDir>/setupTests.js"
};