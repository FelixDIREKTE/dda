

function validEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

//Login
//Login Proccess
function loginProcess(data) {
    DDA.Cookie.saveSessionUser(data);

    $('#stage').fadeOut(300, function () {
        window.location.href = '/parlamentauswahl.html';
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