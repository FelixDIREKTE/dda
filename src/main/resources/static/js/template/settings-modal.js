// "EINSTELLUNGEN" Modal

// "Darstellung" section
$('#themeToggle input:radio').change(function () {
    if ($(this).val() === 'bright') {
        DDA.Cookie.saveThemeSetting({theme: "bright"});
        $('#theme').attr('href', 'css/style-bright.css');
        $('#highcharts').attr('href', 'css/highcharts-bright.css');
    } else if ($(this).val() === 'dark') {
        DDA.Cookie.saveThemeSetting({theme: "dark"});
        $('#theme').attr('href', 'css/style-dark.css');
        $('#highcharts').attr('href', 'css/highcharts-dark.css');
    }
});

if (DDA.Cookie.getThemeSetting() != null && DDA.Cookie.getThemeSetting().theme === "dark") {
    $("#brightTheme").prop("checked", false);
    $("#darkTheme").prop("checked", true);
} else {
    $("#darkTheme").prop("checked", false);
    $("#brightTheme").prop("checked", true);
}

//Import Section
$('#customFile').on('change', function () {
    var fileName = $(this)[0].files[0].name;
    $(this).next('.custom-file-label').html(fileName);
});

$("#uploadImportData").off().click(function () {
    logoutIfExpired();
    $("#uploadImportData, #customFile").attr("disabled", true);
    var data = new FormData();
    data.append('file', $("#customFile")[0].files[0]);

    $("#WaitingModal").modal("show");
    $.ajax({
        url: "/imports/all",
        type: "POST",
        enctype: 'multipart/form-data',
        processData: false,  // Important!
        contentType: false,
        cache: false,
        data: data,
        error: function (xhr, ajaxOptions, thrownError) {
            $("#WaitingModal").modal("hide");
            $("#uploadImportData, #customFile").attr("disabled", false);

            showErrorToast("Datensatz konnte nicht verarbeitet werden, versuchen Sie es später bitte erneut.");
        },
        success: function () {
            $("#WaitingModal").modal("hide");
            $("#uploadImportData, #customFile").attr("disabled", false);
            showSuccessToast("Datensatz wurde erfolgreich eingelesen.")
        }
    });
});
$('#v-pills-theme').hide();

$('#btnAbbrechen').off().click(function () {
    $('#btnEditieren').show();
    $('#btnHinzufuegen, #editModeBottomControls, .deleteBtn').hide();
    $('.inputDisabled').prop('disabled', true);
});

$('#btnSpeichern').off().click(function () {
    $('#btnEditieren').show();
    $('#btnHinzufuegen, #editModeBottomControls, .deleteBtn').hide();
    $('.inputDisabled').prop('disabled', true);
});

$('#btnHinzufuegen').off().click(function () {
    $('#wetterStationHinzufuegenModal').modal({backdrop: false});
});

$('.comboBoxSelect').combobox();




// "Benutzerkonto" section
$('#loginEmailAdress').text(DDA.Cookie.getLoginData().login);

$('#changePassword').click(function () {
    $('#changePasswordSection').show();
    $('#changePassword').hide();
    $('#currentPasswordLabel').text('Aktuelles Kennwort');
    $("#currentPasswordInput").prop("disabled", false);
    $("#currentPasswordInput").val("");
    $("#currentPasswordInput").focus();
    $('#changePasswordForm').removeClass('was-validated');
});


// "Kennwort ädndern"

// "Abbrechen" Button
$('#newPasswordCancelBtn').off().click(function () {
    clearPasswordInputs()
});

function clearPasswordInputs() {
    $('#changePasswordForm').removeClass('was-validated');
    $("#currentPasswordInput, #newPasswordInput, #repeatNewPasswordInput").removeClass("is-invalid").removeClass("is-valid");
    $('#changePasswordSection').hide();
    $('#changePassword').show();
    $('#currentPasswordLabel').text('Kennwort');
    $("#currentPasswordInput").prop("disabled", true);
    $("#currentPasswordInput").val("******");
    $("#newPasswordInput, #repeatNewPasswordInput").val('');
}

$("#currentPasswordInput").on("focusout", function () {
    $.ajax({
        url: "auth",
        method: "POST",
        async: false,
        contentType: "application/json",
        data: JSON.stringify({
            "email": DDA.Cookie.getSessionUser().email,
            "password": $("#currentPasswordInput").val()
        }),
        error: function (xhr, ajaxOptions, thrownError) {
            $("#currentPasswordInput").addClass("is-invalid").removeClass("is-valid");
        },
        success: function () {
            $("#currentPasswordInput").addClass("is-valid").removeClass("is-invalid");
        }
    });
});


// "Speichern" Button mit Form Field Validation:
$("#saveNewPasswordBtn").off().click(function () {
    if ($("#currentPasswordInput").val().trim().length != 0) {
        if ($("#newPasswordInput").val() == $("#repeatNewPasswordInput").val()) {
            $("#repeatNewPasswordInput").removeClass("is-invalid");
            if ($("#newPasswordInput").val().trim().length >= 6) {
                $("#newPasswordInput").removeClass("is-invalid");
                if (DDA.Cookie.getSessionUser() != null) {
                    $.ajax({
                        url: "/users/" + DDA.Cookie.getSessionUser().id + "/password",
                        method: "PUT",
                        async: false,
                        data: {
                            "oldPassword": $("#currentPasswordInput").val(),
                            "newPassword": $("#newPasswordInput").val()
                        },
                        error: function (xhr, ajaxOptions, thrownError) {
                            $("#currentPasswordInput").addClass("is-invalid").removeClass("is-valid");
                        },
                        success: function () {
                            $("#currentPasswordInput").addClass("is-valid").removeClass("is-invalid");
                            showSuccessToast("Passwort wurde erfolgreich geändert!");
                            $("#settingsModal").modal("hide");
                            clearPasswordInputs();
                        }
                    });
                }
            } else {
                $("#newPasswordInput").addClass("is-invalid");
            }
        } else {
            $("#repeatNewPasswordInput").addClass("is-invalid");
        }
    } else {
        $("#currentPasswordInput").addClass("is-invalid");
    }
});