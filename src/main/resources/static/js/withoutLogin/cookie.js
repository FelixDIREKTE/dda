const DDA_LOGIN_DATA = "DDALoginData";
const DDA_USER_DATA = "DDAUserData";
const DDA_THEME_SETTING = "DDAThemeSetting";
const DDA_COLORBLIND = "DDAColorblind";
const DDA_ACCEPTEDCOOKIES = "DDAAcceptedCookies"

if (typeof DDA === 'undefined') {
    var DDA = {};
}

DDA.Cookie = new function () {
    return {

        getLoginData: function () {
            return Cookies.getJSON(DDA_LOGIN_DATA);
        },

        saveLoginData: function (data) {
            Cookies.set(DDA_LOGIN_DATA, data, {expires: 1825});
        },

        getThemeSetting: function () {
            return Cookies.getJSON(DDA_THEME_SETTING);
        },

        saveThemeSetting: function (data) {
            Cookies.set(DDA_THEME_SETTING, data, {expires: 1825});
        },

        getColorblind: function () {
            return Cookies.getJSON(DDA_COLORBLIND);
        },

        saveColorblind: function (data) {
            Cookies.set(DDA_COLORBLIND, data, {expires: 1825});
        },

        getAcceptedCookies: function () {
            return Cookies.getJSON(DDA_ACCEPTEDCOOKIES);
        },

        saveAcceptedCookies: function (data) {
            Cookies.set(DDA_ACCEPTEDCOOKIES, data, {expires: 1825});
        },





        resetLoginData: function () {
            Cookies.remove(DDA_LOGIN_DATA);
        },

        getSessionUser: function () {
            return Cookies.getJSON(DDA_USER_DATA);
        },

        saveSessionUser: function (data) {
            Cookies.set(DDA_USER_DATA, data);
        },

        resetSessionUser: function () {
            Cookies.remove(DDA_USER_DATA);
        },

        resetLoginInfo: function () {
            this.resetLoginData();
            this.resetSessionUser();
        },

        reloadSessionUser: function (id) {
            return $.ajax({
                type: "GET",
                url: "/users/" + id,
                success: function (data) {
                    this.saveSessionUser(data);
                },
                fail: function () {
                    showErrorToast("Benutzter konnte nicht abgerufen werden, versuchen Sie es sp??ter erneut.");
                }
            });
        },
    }
};