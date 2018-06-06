const path = require("path");
const webpack = require("webpack");
const HtmlWebpackPlugin = require("html-webpack-plugin");

const config = {
    entry: {
        application: "./src/application.js",
        login: "./src/login.js"
    },
    output: {
        path: path.resolve(__dirname, "dist"),
        filename: "[name].bundle.js",
        sourceMapFilename: "[file].map"
    },
    module: {
        rules: [
            { 
                test: /\.css$/, 
                use: ["style-loader", "css-loader"] 
            },
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: ["babel-loader"]
            }
        ]
    },
    resolve: {
        extensions: [".js", ".jsx"]
    },
    plugins: [
        new HtmlWebpackPlugin({
            filename: "application.html",
            template: "src/application.html",
            chunks: ["application"]
        }),
        new HtmlWebpackPlugin({
            filename: "login.html",
            template: "src/login.html",
            chunks: ["login"]
        }),
        new HtmlWebpackPlugin({
            filename: "not_found.html",
            template: "src/not_found.html",
            chunks: []
        })
    ],
    devtool: "source-map",
    devServer: {
        contentBase: path.join(__dirname, "dist"),
        compress: true,
        port: 9000,
        proxy: {
            "*": "http://localhost:9001"
        }
    }
};

module.exports = config;