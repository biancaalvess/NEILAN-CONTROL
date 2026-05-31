const NeilanConfig = {
    /** Backend Spring Boot — mesma máquina, porta do monólito */
    apiBase() {
        const port = window.location.port;
        const host = window.location.hostname;
        const devPorts = ['5500', '3000', '5173', '4173'];
        if (devPorts.includes(port)) {
            return `${window.location.protocol}//${host}:8090`;
        }
        return '';
    },

    api(path) {
        return this.apiBase() + path;
    }
};
