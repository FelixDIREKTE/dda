//Delete JSessionID
$.ajax({
    url: "logout",
    method: "GET",
    async: false,
    error: function (xhr, ajaxOptions, thrownError) {
        console.log("Fehler beim Pre Logout: " + xhr);
    }
});


$('#registerBtn').off().click(function () {
    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/register.html?uu=' + randomString()).fadeIn(300);
    });
});

$('#loginBtn').off().click(function () {
    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/login.html?uu=' + randomString()).fadeIn(300);
    });
});


if(typeof tabTitleStr == 'undefined'){
    document.getElementById("tabtitle").textContent = "Demokratie DIREKT!";
} else {
    document.getElementById("tabtitle").textContent = tabTitleStr;
}