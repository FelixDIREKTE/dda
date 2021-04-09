//Delete JSessionID
$.ajax({
    url: "logout",
    method: "GET",
    async: false,
    error: function (xhr, ajaxOptions, thrownError) {
        console.log("Fehler beim Pre Logout: " + xhr);
    }
});

function validEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

//Login
//Login Proccess
function loginProcess(data) {
    DDA.Cookie.saveSessionUser(data);

    $('#stage').fadeOut(300, function () {
        $('#header').load('template/header.html?uu=' + randomString());
        $('#stage').load('template/parlamentauswahl.html?uu=' + randomString());
        $('#footer').load('template/footer.html?uu=' + randomString());
        $('#header, #stage, #footer').fadeIn(300);
    });
}

//Auto Login
if (DDA.Cookie.getLoginData() != null && DDA.Cookie.getLoginData().basicAuth != null) {
    $.ajax({
        url: "login",
        method: "POST",
        async: false,
        headers: {"Authorization": "Basic " + DDA.Cookie.getLoginData().basicAuth},
        error: function (xhr, ajaxOptions, thrownError) {
            DDA.Cookie.resetLoginInfo();

            showErrorToast(xhr.responseJSON.message);
        },
        success: function (data) {
            loginProcess(data)
        }
    });
}

//Login Button
$('#loginBtn').off().click(function () {
    if (validEmail($("#loginEmailInput").val().trim())) {
        $.ajax({
            url: "login",
            method: "POST",
            async: false,
            headers: {"Authorization": "Basic " + btoa($("#loginEmailInput").val().trim() + ":" + $("#loginPasswordInput").val())},
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast(xhr.responseJSON.message);
            },
            success: function (data) {
                if ($("#stayLoggedInCheck").prop("checked") && $("#loginEmailInput").val().trim().length !== 0) {
                    DDA.Cookie.saveLoginData({
                        login: $("#loginEmailInput").val().trim(),
                        basicAuth: btoa($("#loginEmailInput").val().trim() + ":" + $("#loginPasswordInput").val())
                    });
                }

                loginProcess(data);
            }
        });
    } else {
        showErrorToast("Diese E-Mail-Adresse ist nicht gültig!")
    }
});


$('#registerBtn').off().click(function () {
    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/register.html?uu=' + randomString()).fadeIn(300);
    });
});

// KENNWORT VERGESSEN

// "Kennwort vergessen" Link
$('#forgetPasswordLink').off().click(function () {
    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/forgotpassword.html?uu=' + randomString()).fadeIn(300);
    });

});

if(typeof tabTitleStr == 'undefined'){
    document.getElementById("tabtitle").textContent = "Demokratie DIREKT!";
} else {
    document.getElementById("tabtitle").textContent = tabTitleStr;
}