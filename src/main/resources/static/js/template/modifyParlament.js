setTabTitleName("");
setPageTitle(passedParliament.name);
//$('#backToDashboard').show();

$('#footerBar').fadeOut();

//backToDashboardLink();

$('#backToPrev').off().click(function () {
    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/gesetzauswahl.html?uu=' + randomString()).fadeIn(300);
    });
})
///////////////////////////////////
//// Parteien vorschlagen////////
////////////////////////////////

logoutIfExpired();
$.ajax({
    url: "/seats/" + DDA.Cookie.getSessionUser().id + "/getPartiesInParliament",
    method: "GET",
    async: false,
    data: {
        "parliament_id": passedParliament.id
    },
    error: function (xhr, ajaxOptions, thrownError) {
        showErrorToast("Fehler beim Laden der Parteien");
    },
    success: function (data) {
        showPartiesInParliament(data);
    }
});

function showPartiesInParliament(data){
    var x = document.getElementById("selectPartyPrev");
    for(var i = 0; i < data.length; i++) {
        var option = document.createElement("option");
        option.text = data[i].name;
        x.add(option);
    }
}
logoutIfExpired();
$.ajax({
    url: "/seats/" + DDA.Cookie.getSessionUser().id + "/getParties",
    method: "GET",
    async: false,
    data: {
    },
    error: function (xhr, ajaxOptions, thrownError) {
        showErrorToast("Fehler beim Laden der Parteien");
    },
    success: function (data) {
        showAllParties(data);
    }
});

function showAllParties(data){
    var x = document.getElementById("selectPartyAll");
    for(var i = 0; i < data.length; i++) {
        var option = document.createElement("option");
        option.text = data[i].name;
        x.add(option);
    }
}



/////////////////////////
///Sitz-Tabelle////////
////////////////////

$("#btnAddRow").off().click(function () {

    validPartySelection=[];

    if( $("#inputNewparty").val() != ""){
        validPartySelection.push( $("#inputNewparty").val());
    }
    if(document.getElementById("selectPartyPrev").value != ""){
        validPartySelection.push(document.getElementById("selectPartyPrev").value);
    }
    if(document.getElementById("selectPartyAll").value != ""){
        validPartySelection.push(document.getElementById("selectPartyAll").value);
    }


    if(validPartySelection.length == 1) {
        party = validPartySelection[0];
        logoutIfExpired();
        $.ajax({
            url: "/seats/" + DDA.Cookie.getSessionUser().id + "/create",
            method: "PUT",
            async: false,
            data: {
                "from_date": $("#inputSince").val(),
                "seats": $("#inputSeats").val(),
                "party": party,
                "parliament_id": passedParliament.id
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Fehler beim Erstellen der Zeile");
            },
            success: function (data) {
                loadTable();
            }
        });
    } else {
        showErrorToast("Genau 1 Partei auswählen");
    }


});

function loadTable(){
    logoutIfExpired();
    $.ajax({
        url: "/seats/" + DDA.Cookie.getSessionUser().id + "/getSeats",
        method: "GET",
        async: false,
        data: {
            "parliament_id": passedParliament.id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Anhänge");
        },
        success: function (data) {
            showTable(data);

        }
    });
}
loadTable();

function showTable(data){

    // Find a <table> element with id="myTable":
    var table = document.getElementById("seatstable");
    while (table.lastChild) {
        table.removeChild(table.lastChild);
    }

    var row = table.insertRow(0);
    row.insertCell(0).innerHTML = "Partei";
    row.insertCell(1).innerHTML = "seit";
    row.insertCell(2).innerHTML = "Sitze";
    row.insertCell(3).innerHTML = "";

    for(var i = 0; i < data.length; i++){
        var row = table.insertRow(i+1);
        s = data[i].from_date.toString();
        const s2 = s.substr(8,2) + "." + s.substr(5,2) + "." + s.substr(0,4);
        var party = getParty(data[i].party_id);
        row.insertCell(0).innerHTML = "" + party.name;
        row.insertCell(1).innerHTML = "" + s2;
        row.insertCell(2).innerHTML = "" + data[i].seats;
        row.insertCell(3).innerHTML = "<i class=\"fas fa-trash-alt\" id=\"deletebtn_"+i+"\"></i>";

        const party_id = party.id;

        $("#deletebtn_" + i).off().click(function () {
            deleteRow(party_id, s2);
        });
    }
}

function deleteRow(party_id, from_date){
    logoutIfExpired();
    $.ajax({
        url: "/seats/" + DDA.Cookie.getSessionUser().id + "/delete",
        method: "DELETE",
        async: false,
        data: {
            "party_id": party_id,
            "from_date": from_date,
            "parliament_id": passedParliament.id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Partei");
        },
        success: function (data) {
            loadTable();
        }
    });
}

function getParty(party_id){
    result = null;
    logoutIfExpired();
    $.ajax({
        url: "/seats/" + DDA.Cookie.getSessionUser().id + "/getParty",
        method: "GET",
        async: false,
        data: {
            "party_id": party_id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Partei");
        },
        success: function (data) {
            result = data;
        }
    });
    return result;
}


////////////////////////////////////
///////////Parlamentbilder//////////
////////////////////////////////////

$("#uploadImportData3").off().click(function () {

    $("#uploadImportData3, #customFile3").attr("disabled", true);
    var data = new FormData();
    data.append('file', $("#customFile3")[0].files[0]);
    data.append('parliament_id', passedParliament.id);
    $("#WaitingModal3").modal("show");
    logoutIfExpired();
    $.ajax({
        url: "/parliamentpicfiles/" + DDA.Cookie.getSessionUser().id + "/upload",
        type: "POST",
        enctype: 'multipart/form-data',
        processData: false,  // Important!
        contentType: false,
        cache: false,
        data: data,
        error: function (xhr, ajaxOptions, thrownError) {
            $("#WaitingModal3").modal("hide");
            $("#uploadImportData3, #customFile3").attr("disabled", false);
            showErrorToast("Datei konnte nicht verarbeitet werden, versuche es später bitte erneut.");
        },
        success: function (data) {
            $("#WaitingModal3").modal("hide");
            $("#uploadImportData3, #customFile3").attr("disabled", false);
            if(data == "TOOBIG"){
                showErrorToast("Datei zu groß. Bitte höchstens 2MB.");
            } else {
                showSuccessToast("Datei erfolgreich eingelesen.");
                updateprofilepicbody();
            }

        }
    });
});

function updateprofilepicbody(){
    logoutIfExpired();
    $.ajax({
        url: "/parliamentpicfiles/" + DDA.Cookie.getSessionUser().id + "/getForOthers",
        method: "GET",
        async: false,
        data: {
            "parliament_id": passedParliament.id
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
        img.style="max-width:100%";
        img.src =  "data:image/png;base64," + str;
        parent.appendChild(img);
    }

}
updateprofilepicbody();

$("#deleteData").off().click(function () {
    logoutIfExpired();
    $.ajax({
        url: "/parliamentpicfiles/" + DDA.Cookie.getSessionUser().id + "/deleteAll",
        method: "DELETE",
        async: false,
        data: {
            "parliament_id": passedParliament.id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Löschen der Bilder");
        },
        success: function (data) {
            updateprofilepicbody();
            showSuccessToast("Bilder gelöscht");
        }
    });
});
