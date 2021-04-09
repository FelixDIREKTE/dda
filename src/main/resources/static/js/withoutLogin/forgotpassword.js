function validEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

$('#sendBtn').off().click(function () {


        if (validEmail($("#passwordResetEmail").val().trim())) {
            $.ajax({
                url: "/users/forgotPassword",
                method: "POST",
                async: false,
                data: {
                    "email": $("#passwordResetEmail").val().trim()
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast('E-Mail konnte nicht versendet werden');
                },
                success: function () {
                    showSuccessToast('E-Mail mit Kennwort wurde versendet');

                    $('#stage').fadeOut(300, function () {
                        $('#stage').load('template/login.html?uu=' + randomString()).fadeIn(300);
                    });

                }
            });
        } else {
            showErrorToast("Diese E-Mail-Adresse ist nicht gültig!")
        }





});


$('#cancelBtn').off().click(function () {
    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/login.html?uu=' + randomString()).fadeIn(300);
    });

});