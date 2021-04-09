setTabTitleName("");
setPageTitle('Accounts verifizieren');
//$('#backToDashboard').show();

$('#footerBar').fadeOut();

//backToDashboardLink();

nrOfWaitingUsers = -1;
waitingUser = null;
duplicates = null;
logoutIfExpired();
$.ajax({
    url: "/verification/" + DDA.Cookie.getSessionUser().id + "/getWatingUser",
    method: "GET",
    async: false,
    data: {
    },
    error: function (xhr, ajaxOptions, thrownError) {
    },
    success: function (data) {
        nrOfWaitingUsers = data.waitingUsers;
        document.getElementById("accountswaiting").textContent = "Accounts in Warteschlange: " + nrOfWaitingUsers;
        waitingUser = data.userToVerify;
        duplicates = data.duplicats;
        if(waitingUser != null) {
            updatePage();
        } else {
            document.getElementById("dataCol").style="display:none";
        }
    }
});

function getParliamentsFor(user_id){
    result = "";
    logoutIfExpired();
    $.ajax({
        url: "/parliaments/" + DDA.Cookie.getSessionUser().id + "/getEligibleParliamentsFor",
        method: "GET",
        async: false,
        data: {
            "user_id" : user_id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Parlamente");
        },
        success: function (data) {
            comma = "";
            for(var i = 0; i < data.length; i++){
                result = result + comma +  data[i].name;
                comma = ", ";
            }
        }
    });
    return result;

}

function updatePage() {

    //Kürzlich verifiziert
    if(waitingUser.verifiedDate != null){
        txt = "Dieser Nutzer wurde " + erstelltVor(waitingUser.verifiedDate)  + " verifiziert. Bitte Parlamente überprüfen!";
        document.getElementById("recentlyVerifiedText").innerHTML = txt;
    }

    //Duplikate
    if(duplicates.length > 0){
        msg = "";
        for(var i = 0; i < duplicates.length; i++){
            msg = msg + "MÖGLICHES DUPLIKAT! <br>";

            msg = msg + "id: " + duplicates[i].id + "<br>";
            msg = msg + "Email: " + duplicates[i].email + "<br>";
            msg = msg + "firstname: " + duplicates[i].firstname + "<br>";
            msg = msg + "name: " + duplicates[i].name + "<br>";
            msg = msg + "street: " + duplicates[i].street + "<br>";
            msg = msg + "housenr: " + duplicates[i].housenr + "<br>";
            msg = msg + "zipcode: " + duplicates[i].zipcode + "<br>";
            msg = msg + "birthdate: " + duplicates[i].birthdate + "<br>";
            msg = msg + "parlamente: " + getParliamentsFor(duplicates[i].id) + "<br>";
            msg = msg + "erstellt am: " + duplicates[i].created_time + "<br>";
            msg = msg + "<br>";
        }
        document.getElementById("duplicatesText").innerHTML = msg;

    } else {
        $('#duplicateBtn').hide();
    }

    //Daten
    //document.getElementById("accountswaiting").textContent = "Accounts in Warteschlange: " + nrOfWaitingUsers;
    document.getElementById("Vorname").textContent = "" + waitingUser.firstname;
    document.getElementById("Nachname").textContent = "" + waitingUser.name;
    s = waitingUser.birthdate.toString();
    s2 = s.substr(8,2) + "." + s.substr(5,2) + "." + s.substr(0,4);
    document.getElementById("Geburtsdatum").textContent = s2;
    document.getElementById("PLZ").textContent = "" + waitingUser.zipcode;
    document.getElementById("stra").textContent = "" + waitingUser.street;
    document.getElementById("hausnr").textContent = "" + waitingUser.housenr;
    document.getElementById("Emailadresse").textContent = "" + waitingUser.email;
    document.getElementById("Parlamente").textContent = "" +getParliamentsFor(waitingUser.id);
    document.getElementById("Erstelltan").textContent = "" + waitingUser.created_time;

    //Bild
    loadVerificationPicture();

    //Nicht-Verifizier-Nachricht
    document.getElementById("msgtounverified").value = "Hallo "+ waitingUser.firstname +"! \nEs gibt ein Problem bei der Verifizierung deines Accounts: \n\nLiebe Grüße\nTeam DIREKTE";


}

function loadVerificationPicture(){
    logoutIfExpired();
    $.ajax({
        url: "/verificationfiles/" + DDA.Cookie.getSessionUser().id + "/getForVerification",
        method: "GET",
        async: false,
        data: {
            "verifiedId": waitingUser.id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Identitätsnachweise für diesen Nutzer");
        },
        success: function (data) {
            showImages(data);

        }
    });
}



function showImages(data){
    for (var j = 0; j < data.length; j++) {


        testArray = data[j].bytes;
        str = "";
        for (var i = 0; i < testArray.length; i++) {
            str += testArray[i];
        }

        var img = document.createElement('img');
        img.style="max-width:100%";
        img.src =  "data:image/png;base64," + str;
        document.getElementById('imagebody').appendChild(img);
        //down.innerHTML = "Image Element Added.";
    }
}




//

$('#verifizNein').off().click(function () {
    logoutIfExpired();
    $.ajax({
        url: "/verification/" + DDA.Cookie.getSessionUser().id + "/verify",
        method: "PUT",
        async: false,
        data: {
            "verifiedId": waitingUser.id,
            "verify": false,
            "msg": $("#msgtounverified").val()
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("etwas lief schief")
        },
        success: function (data) {
            $('#stage').fadeOut(300, function () {
                $('#stage').load('template/adminverifiz.html?uu=' + randomString()).fadeIn(300);
            });

        }
    });
});



$('#verifizJa').off().click(function () {
    logoutIfExpired();
    $.ajax({
        url: "/verification/" + DDA.Cookie.getSessionUser().id + "/verify",
        method: "PUT",
        async: false,
        data: {
            "verifiedId": waitingUser.id,
            "verify": true,
            msg: null
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("etwas lief schief")
        },
        success: function (data) {
            $('#stage').fadeOut(300, function () {
                $('#stage').load('template/adminverifiz.html?uu=' + randomString()).fadeIn(300);
            });

        }
    });
});



$('#duplicateBtn').off().click(function () {
    logoutIfExpired();
    $.ajax({
        url: "/verification/" + DDA.Cookie.getSessionUser().id + "/reportDuplicate",
        method: "PUT",
        async: false,
        data: {
            "verifiedId": waitingUser.id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("etwas lief schief")
        },
        success: function (data) {
            $('#stage').fadeOut(300, function () {
                $('#stage').load('template/adminverifiz.html?uu=' + randomString()).fadeIn(300);
            });

        }
    });
});