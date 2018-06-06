import * as React from "react";

const serverDownLink = 
    "http://classicprogrammerpaintings.com" +
    "/post/144953638470/github-major-service-outage-georges-seurat";

function ServerDown() {
    return (
        <div className="server-down">
            <div>500</div>
            <div><a href={serverDownLink}>SERVER DOWN</a>
            </div>
        </div>
    )
}

export default ServerDown;