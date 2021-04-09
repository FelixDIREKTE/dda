setTabTitleName("");

setPageTitle('Benutzerkonto');
$('#footerBar').fadeOut();


logoutIfExpired();


$('#backToPrev').off().click(function () {
    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/parlamentauswahl.html?uu=' + randomString()).fadeIn(300);
    });
})



var isNameMissing = true;
var isAddressMissing = true;
var isBirthdateMissing = true;
var isParliamentMissing = true;
var isIdentityProofMissing = true;


function updateDisplay() {
    updateSessionUser();
    zipcode = DDA.Cookie.getSessionUser().zipcode;
    street = DDA.Cookie.getSessionUser().street;
    housenr = DDA.Cookie.getSessionUser().housenr;
    uname = DDA.Cookie.getSessionUser().name;
    firstname = DDA.Cookie.getSessionUser().firstname;
    birthdate = DDA.Cookie.getSessionUser().birthdate;
    email = DDA.Cookie.getSessionUser().email;
    verifstatus = DDA.Cookie.getSessionUser().verificationstatus;
    phonenr = DDA.Cookie.getSessionUser().phonenr;

    isNameMissing = (firstname == null || uname == null);
    isAddressMissing = (zipcode == null || street == null || housenr == null);
    isBirthdateMissing = (birthdate == null);


    if(isNameMissing){
        $('#missingName').show();
    } else {
        $('#missingName').hide();
    }

    if(isAddressMissing){
        $('#missingAddress').show();
    } else {
        $('#missingAddress').hide();
    }

    if(isBirthdateMissing){
        $('#missingBirthdate').show();
    } else {
        $('#missingBirthdate').hide();
    }
    if(isParliamentMissing){
        $('#missingParliaments').show();
    } else {
        $('#missingParliaments').hide();
    }
    if(isIdentityProofMissing){
        $('#missingIdentityProof').show();
    } else {
        $('#missingIdentityProof').hide();
    }

    if(isNameMissing || isAddressMissing || isBirthdateMissing || isParliamentMissing || isIdentityProofMissing ){
        $('#missingDataHeadline').show();
    } else {
        $('#missingDataHeadline').hide();
    }


    vss = "" + verifstatus;
    if(vss == "DATANEEDED"){
        //TODO bright mode
        document.getElementById("sli0").style="color:white;"
        document.getElementById("sli1").style="color:grey;"
        document.getElementById("sli2").style="color:grey;"
        //$('#verproofexplanation').show();

    } else {
        //$('#verproofexplanation').hide();
    }

    if(vss == "WAITINGFORVERIF" || vss == "LOCKEDBYADMIN"){
        document.getElementById("sli0").style="color:grey;"
        document.getElementById("sli1").style="color:white;"
        document.getElementById("sli2").style="color:grey;"
    }


    if(vss == "VERIFIED"){

        document.getElementById("sli0").style="color:grey;"
        document.getElementById("sli1").style="color:grey;"
        document.getElementById("sli2").style="color:white;"
        $('#verproofcard').hide();
    }  else {
        $('#verproofcard').show();

    }

    if (phonenr != null) {
        document.getElementById("inputphonenr").value = phonenr;
    }
    if (zipcode != null) {
        document.getElementById("inputplz").value = zipcode;
    }
    if(uname != null) {
        document.getElementById("inputname").value = uname;
    }
    if(firstname != null) {
        document.getElementById("inputvorname").value = firstname;
    }
    if(street != null) {
        document.getElementById("inputstr").value = street;
    }
    if(housenr != null) {
        document.getElementById("inputhausnr").value = housenr;
    }
    if(birthdate != null) {
        s = birthdate.toString();
        s2 = s.substr(8,2) + "." + s.substr(5,2) + "." + s.substr(0,4)
        document.getElementById("geburtsdatum").value = s2;
        $('#missingBirthdate').hide();
    } else {
        document.getElementById("missingBirthdate").display = 'block';
    }
    if(email != null) {
        document.getElementById("loginEmailInput2").value = email;
    }



}


if (DDA.Cookie.getThemeSetting() != null && DDA.Cookie.getThemeSetting().theme === "dark") {
} else {
}

//Import Section
$('#customFile2').on('change', function () {
    var fileName = $(this)[0].files[0].name;
    $(this).next('.custom-file-label').html(fileName);
});

$("#uploadImportData2").off().click(function () {

    $("#uploadImportData2, #customFile2").attr("disabled", true);
    var data = new FormData();

    data.append('file', $("#customFile2")[0].files[0]);

    $("#WaitingModal2").modal("show");
    logoutIfExpired();
    $.ajax({
        url: "/verificationfiles/" + DDA.Cookie.getSessionUser().id + "/upload",
        type: "POST",
        enctype: 'multipart/form-data',
        processData: false,  // Important!
        contentType: false,
        cache: false,
        data: data,
        error: function (xhr, ajaxOptions, thrownError) {
            $("#WaitingModal2").modal("hide");
            $("#uploadImportData2, #customFile2").attr("disabled", false);
            showErrorToast("Datensatz konnte nicht verarbeitet werden");
        },
        success: function (data) {
            $("#WaitingModal2").modal("hide");
            $("#uploadImportData2, #customFile2").attr("disabled", false);


            if(data == "TOOBIG"){
                showErrorToast("Bild zu groß. Bitte höchstens 2MB.");
            } else {
                showSuccessToast("Datensatz wurde erfolgreich eingelesen.")
                updateDisplay();
                updateverproofbody();
            }
        }
    });
});




$('.comboBoxSelect').combobox();





// "Speichern" Button mit Form Field Validation:
$("#Kennwortandern").off().click(function () {
        changePassword();
});

$("#Emailandern").off().click(function () {
    changeEmail();
});


function validEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}


function changeEmail(){
    if (validEmail($("#loginEmailInput2").val().trim())) {
        logoutIfExpired();
        $.ajax({
            url: "/users/" + DDA.Cookie.getSessionUser().id + "/changeEmail",
            method: "PUT",
            async: false,
            data: {
                "email": $("#loginEmailInput2").val().trim()
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Etwas lief schief");
            },
            success: function (data) {
                if (data == "ok") {
                    showSuccessToast("Änderungen gespeichert!");
                    updateDisplay();
                } else {
                    showErrorToast(data);
                }
            }
        });
    }else {
        showErrorToast("Diese E-Mail-Adresse ist nicht gültig!")
    }

}

function changePassword(){
    if ($("#currentPasswordInput").val().trim().length != 0) {
        if ($("#newPasswordInput").val() == $("#repeatNewPasswordInput").val()) {
            $("#repeatNewPasswordInput").removeClass("is-invalid");
            if ($("#newPasswordInput").val().trim().length >= 6) {
                $("#newPasswordInput").removeClass("is-invalid");
                if (DDA.Cookie.getSessionUser() != null) {
                    logoutIfExpired();
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
}


$('#andergspeichern').off().click(function () {
    updateUser()
});


function updateUser(){
    logoutIfExpired();
    $.ajax({
        url: "/users/" + DDA.Cookie.getSessionUser().id + "/updateData",
        method: "PUT",
        async: false,
        data: {
            "name": $("#inputname").val(),
            "firstname": $("#inputvorname").val(),
            "street": $("#inputstr").val(),
            "housenr": $("#inputhausnr").val(),
            "zipcode": $("#inputplz").val(),
            "birthdate": $("#geburtsdatum").val(),
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            if(data == "ok") {
                showSuccessToast("Änderungen gespeichert!");
                updateDisplay();
            } else {
                showErrorToast(data);
            }
        }
    });

}

function updateverproofbody(){
    logoutIfExpired();
    $.ajax({
        url: "/verificationfiles/" + DDA.Cookie.getSessionUser().id + "/getForSelf",
        method: "GET",
        async: false,
        data: {
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Identitätsnachweise");
        },
        success: function (data) {
            showOwnImages(data);
            updateDisplay();

        }
    });
}

function showOwnImages(data){
    var parent = document.getElementById('verproofbody');
    while (parent.lastChild) {
        parent.removeChild(parent.lastChild);
    }

    isIdentityProofMissing = (data.length == 0 && DDA.Cookie.getSessionUser().verificationstatus != "VERIFIED" );

    for (var j = 0; j < data.length; j++) {
        testArray = data[j].bytes;
        str = "";
        for (var i = 0; i < testArray.length; i++) {
            str += testArray[i];
        }
        var img = document.createElement('img');
        img.style="max-width:100%";
        img.src =  "data:image/png;base64," + str;
        parent.appendChild(img);
    }
}



$("#deleteData").off().click(function () {
    logoutIfExpired();
    $.ajax({
        url: "/verificationfiles/" + DDA.Cookie.getSessionUser().id + "/deleteAll",
        method: "DELETE",
        async: false,
        data: {

        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Löschen der Identitätsnachweise");
        },
        success: function (data) {
            updateverproofbody();
            showSuccessToast("Bilder gelöscht");
        }
    });
});



$('#modalContainer').append('<div id="holderForNextLoad" />');
$('#holderForNextLoad').load('template/areusure-modal.html', function (){
    $("#confirmdeletebtn").off().click(function () {
        logoutIfExpired();
        $.ajax({
            url: "/users/" + DDA.Cookie.getSessionUser().id + "/deleteSelf",
            method: "DELETE",
            async: false,
            data: {

            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Fehler beim Löschen des Accounts");
            },
            success: function (data) {
                showSuccessToast("Account gelöscht");
                logout();
            }
        });
    });

});









/////////////////////////////
///////Parlamente//////////
//////////////////////////

eligibleParliaments = null;

$.ajax({
    url: "/parliaments/" + DDA.Cookie.getSessionUser().id + "/getEligibleParliamentsComplete",
    method: "GET",
    async: false,
    data: {
    },
    error: function (xhr, ajaxOptions, thrownError) {
        showErrorToast("Fehler beim Laden der Parlamente");
    },
    success: function (data) {
        eligibleParliaments = data;
        if(eligibleParliaments.length == 6){
            isParliamentMissing = false;
        } else {
            isParliamentMissing = true;
        }
    }
});



parliamentChoices = [-1, 1,2,-1,-1,-1,-1];

fetchSubParliaments(2, 3)

function fetchSubParliaments(parliament_id, parliamentLvl) {
    logoutIfExpired();
    $.ajax({
        url: "/parliaments/" + DDA.Cookie.getSessionUser().id + "/getSubParliaments",
        method: "GET",
        async: false,
        data: {
            "parliament_id": parliament_id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Parlamente");
        },
        success: function (data) {
            fillParliamentsSelect(data, parliamentLvl);
        }
    });
}

function fillParliamentsSelect(data, parliamentLvl){
    if(data.length > 0) {
        for(var i = parliamentLvl; i <= 6; i++) {
            emptySelect(i);
        }

        var selectParliamentLandtag = document.getElementById("selectParliament" + parliamentLvl)
        var div = document.createElement('option');  //creating element
        div.textContent = "wählen";         //adding text on the element
        selectParliamentLandtag.appendChild(div);           //appending the element
        var correctI = -1;
        for(var i = 0; i < data.length; i++) {
            var div = document.createElement('option');  //creating element
            div.textContent = data[i].name;         //adding text on the element
            //if data[i].name in eligibleParliaments.name -> selected
            for(var j = 0; j < eligibleParliaments.length; j++){
                if (eligibleParliaments[j].id == data[i].id){
                    correctI = i;
                    break;
                }
            }
            selectParliamentLandtag.appendChild(div);           //appending the element
        }
        if(correctI >= 0){
            selectParliamentLandtag.value = data[correctI].name;
            requestParliamentAccess(data[correctI].id , parliamentLvl);
        }

        selectParliamentLandtag.onchange=function(){
            var selectedParliament=null;
            for(var i = 0; i < data.length; i++) {
                if(data[i].name == selectParliamentLandtag.value){
                    selectedParliament = data[i];
                    break;
                }
            }
            if(selectedParliament == null) {
                //alert("bug");
            }
            requestParliamentAccess(selectedParliament.id , parliamentLvl);
        };

    } else {

    }
}

function emptySelect(parliamentLvl){
    parliamentChoices[parliamentLvl] = -1;
    var selectParliamentLandtag = document.getElementById("selectParliament" + parliamentLvl);
    while (selectParliamentLandtag.lastChild) {
        selectParliamentLandtag.removeChild(selectParliamentLandtag.lastChild);
    }
}

function requestParliamentAccess(parliament_id, parliamentLvl){
    parliamentChoices[parliamentLvl] = parliament_id
    fetchSubParliaments(parliament_id, parliamentLvl + 1)
}


$('#parliamentsSaveChanges').off().click(function () {
    logoutIfExpired();
    $.ajax({
        url: "/parliaments/" + DDA.Cookie.getSessionUser().id + "/requestAccess",
        method: "PUT",
        async: false,
        data: {
            "parliament_id3": parliamentChoices[3],
            "parliament_id4": parliamentChoices[4],
            "parliament_id5": parliamentChoices[5],
            "parliament_id6": parliamentChoices[6]
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Speichern der Parlamente");
        },
        success: function (data) {
            if(data == "ok") {
                if(parliamentChoices[3] > 0 && parliamentChoices[4] > 0 && parliamentChoices[5] > 0 && parliamentChoices[6] > 0 ){
                    isParliamentMissing = false;
                } else {
                    isParliamentMissing = true;
                }
                updateDisplay();
                showSuccessToast("Parlamente gespeichert");
            } else {
                showErrorToast(data);
            }

        }
    });

});



///////////////////////////
///PROFILBILD////////////
//////////////////


$("#uploadImportData3").off().click(function () {

    $("#uploadImportData3, #customFile3").attr("disabled", true);
    var data = new FormData();

    data.append('file', $("#customFile3")[0].files[0]);

    $("#WaitingModal3").modal("show");
    logoutIfExpired();
    $.ajax({
        url: "/profilepicfiles/" + DDA.Cookie.getSessionUser().id + "/upload",
        type: "POST",
        enctype: 'multipart/form-data',
        processData: false,  // Important!
        contentType: false,
        cache: false,
        data: data,
        error: function (xhr, ajaxOptions, thrownError) {
            $("#WaitingModal3").modal("hide");
            $("#uploadImportData3, #customFile3").attr("disabled", false);
            showErrorToast("Profilbild konnte nicht eingelesen werden");
        },
        success: function (data) {
            $("#WaitingModal3").modal("hide");
            $("#uploadImportData3, #customFile3").attr("disabled", false);
            if(data == "TOOBIG"){
                showErrorToast("Bild zu groß. Bitte höchstens 500kB.");
            } else {
                showSuccessToast("Profilbild aktualisiert")
                updateprofilepicbody();
                showHeaderProfilePic();
            }
        }
    });

});

function updateprofilepicbody(){
    logoutIfExpired();
    $.ajax({
        url: "/profilepicfiles/" + DDA.Cookie.getSessionUser().id + "/getForSelf",
        method: "GET",
        async: false,
        data: {
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden des Profilbilds");
        },
        success: function (data) {
            showOwnProfilepic(data);
        }
    });
}

function showOwnProfilepic(data){

    var parent = document.getElementById('profilepicbody');
    while (parent.lastChild) {
        parent.removeChild(parent.lastChild);
    }
    for (var j = 0; j < data.length; j++) {

        testArray = data[j].bytes;
        str = "";
        for (var i = 0; i < testArray.length; i++) {
            str += testArray[i];
        }
        var img = document.createElement('img');
        img.style="width:20%";
        img.src =  "data:image/png;base64," + str;
        parent.appendChild(img);
    }

}

updateprofilepicbody();


        ////////////////////////////////////
        /////////OPTIONALE DATEN////////////
        ////////////////////////////////////


$('#optandergspeichern').off().click(function () {
    updateOptUser()
});


function updateOptUser(){
    logoutIfExpired();
    $.ajax({
        url: "/users/" + DDA.Cookie.getSessionUser().id + "/updateOptionalData",
        method: "PUT",
        async: false,
        data: {
            "phonenr": $("#inputphonenr").val()
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            if(data == "ok") {
                showSuccessToast("Änderungen gespeichert!");
                updateDisplay();
            } else {
                showErrorToast(data);
            }
        }
    });

}


/////////////////////////
///////KATEGORIEN////////
/////////////////////////

$('#interestSaveChanges').off().click(function () {
    updateUserInterests()
});

function initInterests(){
    $.ajax({
        url: "/users/" + DDA.Cookie.getSessionUser().id + "/getCategories",
        method: "GET",
        async: false,
        data: {
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            intToCategories(data, "kateg1", "kateg2", "kateg3", "kateg4", "kateg5", "kateg6", "kateg7", "kateg8", "kateg9", "kateg10", "kateg11", "kateg12", "kateg13", "kateg14", "kateg15", "kateg16");
        }
    });
}
initInterests()

function updateUserInterests(){

    catInt = CategoriesToInt("kateg1", "kateg2", "kateg3", "kateg4", "kateg5", "kateg6", "kateg7", "kateg8", "kateg9", "kateg10", "kateg11", "kateg12", "kateg13", "kateg14", "kateg15", "kateg16");



    $.ajax({
        url: "/users/" + DDA.Cookie.getSessionUser().id + "/updateCategories",
        method: "POST",
        async: false,
        data: {
            "categoryBits":catInt
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            if(data == "ok") {
                showSuccessToast("Änderungen gespeichert!");
            } else {
                showErrorToast(data);
            }
        }
    });

}




updateverproofbody();
