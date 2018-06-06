function createLogger() {
    
    function info(main, ...others) {
        console.log(main, new Date().toISOString(), ...others);
    }

    return {
        info
    };
}

export default createLogger;