
function validEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}




//Login Button
$('#loginBtn').off().click(function () {
    if($("#licencereadcheckbox").prop("checked")) {
        createUser()
    } else {
        showErrorToast("Bitte akzeptiere zuerst die Datenschutzerklärung");
    }
});


$('#registerBtn').off().click(function () {

    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/login.html?uu=' + randomString()).fadeIn(300);
    });
});



function createUser(){

    if (validEmail($("#loginEmailInput2").val().trim())) {

        if ($("#loginEmailInput2").val().trim().length != 0) {
            if ($("#newPasswordInput").val() == $("#repeatNewPasswordInput").val()) {
                $("#repeatNewPasswordInput").removeClass("is-invalid");
                if ($("#newPasswordInput").val().trim().length >= 6) {
                    $("#newPasswordInput").removeClass("is-invalid");


                    $.ajax({
                        url: "/users/create",
                        method: "PUT",
                        async: false,
                        data: {
                            "email": $("#loginEmailInput2").val(),
                            "password": $("#newPasswordInput").val()
                        },
                        error: function (xhr, ajaxOptions, thrownError) {
                            $("#loginEmailInput2").addClass("is-invalid").removeClass("is-valid");
                            showErrorToast("Emailadresse ist bereits registriert.");
                        },
                        success: function () {
                            $("#currentPasswordInput").addClass("is-valid").removeClass("is-invalid");
                            showSuccessToast("Account wurde erfolgreich erstellt!");

                            $("#settingsModal").modal("hide");
                            login();

                        }
                    });


                } else {
                    $("#newPasswordInput").addClass("is-invalid");
                    showErrorToast("Passwort soll aus mindestens 6 Zeichen bestehen.");
                }
            } else {
                $("#repeatNewPasswordInput").addClass("is-invalid");
                showErrorToast("Passworte stimmen nicht überein.");
            }
        } else {
            $("#loginEmailInput2").addClass("is-invalid");
            showErrorToast("Bitte Emailadresse eingeben");
        }
    }else {
        showErrorToast("Diese E-Mail-Adresse ist nicht gültig!")
    }
}


function login(){
    $.ajax({
        url: "login",
        method: "POST",
        async: false,
        headers: {"Authorization": "Basic " + btoa($("#loginEmailInput2").val().trim() + ":" + $("#newPasswordInput").val())},
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast(xhr.responseJSON.message);
        },
        success: function (data) {
            if ($("#stayLoggedInCheck").prop("checked") && $("#loginEmailInput2").val().trim().length !== 0) {
                DDA.Cookie.saveLoginData({
                    login: $("#loginEmailInput2").val().trim(),
                    basicAuth: btoa($("#loginEmailInput2").val().trim() + ":" + $("#newPasswordInput").val())
                });
            }

            loginProcess(data);
        }
    });
}

function loginProcess(data) {
    DDA.Cookie.saveSessionUser(data);

    $('#stage').fadeOut(300, function () {
        window.location.href = '/parlamentauswahl.html';
    });
}