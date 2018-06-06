function assertAuthorized() {
    const userName = getUserName();
    if (!userName)
        location.replace("./login");
    return userName;
}

function getUserName() {
    const parts = ("; " + document.cookie).split("; username=");
    if (parts.length == 2) 
        return parts.pop().split(";").shift();
}

export default {
    assertAuthorized
};