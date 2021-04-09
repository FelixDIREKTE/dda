setTabTitleName("");
$('#backToPrev').off().click(function () {
    $('#stage').fadeOut(300, function () {
        $('#stage').load('template/parlamentauswahl.html?uu=' + randomString()).fadeIn(300);
    });
})


title = "";
if(passedParliamentRole == 0){
    title = passedParliament.name + " - Gesetze";
}
if(passedParliamentRole == 1){
    title = passedParliament.name + " - Initiativen";
    document.getElementById("adminAddBtn").innerHTML = "Initivative einreichen";
}
if(passedParliamentRole == 2){
    title = passedParliament.name + " - Diskussionen";
}

var billSequences = []; // hier wird je Kommentarsektion eine Liste der Kommentare von oben nach unten abgelegt um festzustellen welche gelesen sind
var readbills = new Set(); //enthält ids aller gelesener kommentare


setPageTitle(title);
$('#footerBar').fadeIn();
//$('#backToDashboard').show();
$('#footerBar').fadeOut();


/*dateRangePicker(function (start, end, label) {
});*/

$('#viewRangeC').hide();
//$('#searchFieldC').hide();
//$('#searchBtn').hide();



//Beitrag Hinzufügen-Knopf
if((!DDA.Cookie.getSessionUser().admin && passedParliamentRole == 0) || DDA.Cookie.getSessionUser().verificationstatus != "VERIFIED") {
    $('#adminAddBtn').hide();
} else {
    $('#adminAddBtn').off().click(function () {

        $('#stage').fadeOut(300, function () {
            passedBill = null;
            $('#stage').load('template/addGesetz.html?uu=' + randomString()).fadeIn(300);
        });
    });
}
//Parlament editieren
if((!DDA.Cookie.getSessionUser().admin)) {
    $('#modifyParliamentBtn').hide();
} else {
    $('#modifyParliamentBtn').off().click(function () {

        $('#stage').fadeOut(300, function () {
            passedBill = null;
            $('#stage').load('template/modifyParlament.html?uu=' + randomString()).fadeIn(300);
        });
    });
}


// Load Settings modal and About modal:
$('#modalContainer').load('template/settings-modal.html', function () {
    $('#modalContainer').append('<div id="holderForNextLoad" />');
    $('#holderForNextLoad').load('template/about-modal.html');
});

logoutIfExpired();


function getRankedBills() {
    $.ajax({
        url: "/bills/" + DDA.Cookie.getSessionUser().id + "/getRankedBills",
        method: "GET",
        async: false,
        data: {
            "parliament_id": passedParliament.id,
            "parliament_role": passedParliamentRole
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Gesetze");
        },
        success: function (data) {

            showBills(data);

        }
    });
}
getRankedBills();

var passedBill = null;



function faellig(time){
    if(time == null){
        return "unbekannt"
    }
    billDate = new Date();
    y = time.substr(0,4);
    M = "" + (parseInt(time.substr(5,2)) - 1);
    d = time.substr(8,2);
    h = time.substr(11,2);
    m = time.substr(14,2);
    s = time.substr(17,2);
    billDate.setFullYear(y, M, d);
    billDate.setHours(h,m,s);

    let currentDate = new Date();

    var timeDiffDays = Math.floor((currentDate.getTime() - billDate.getTime()) /86400000);

    if (timeDiffDays > 100){
        s = time;
        s2 = s.substr(8,2) + "." + s.substr(5,2) + "." + s.substr(0,4);
        return "Abgestimmt am " + s2;
    }

    if(timeDiffDays == -1){
        return "Abstimmung endet in einem Tag";
    }
    if(timeDiffDays == 1){
        return "Abgestimmt vor einem Tag";
    }
    if(timeDiffDays == 0){
        return "Heute abgestimmt";
    }



    if(timeDiffDays < 0){
        return "Abstimmung endet in " + (-timeDiffDays) + " Tagen";
    }
    return "Abgestimmt vor " + timeDiffDays + " Tagen";


}

function showBills(data) {


    billids = [];
    for (var i = 0; i < data.length ; i++) {
        billids.push(data[i].id);
    }
    billSequences.push(billids);

    $('#billTileTemplate').show();

    var billContainer = document.querySelector('#billContainer');
    billContainer.style = "display:none;"
    while (billContainer.lastChild) {
        billContainer.removeChild(billContainer.lastChild);
    }

    //TODO billTileTemplate sichtbar, unsichtbar
    var billTileTemplate = document.querySelector('#billTileTemplate');
    for (var i = 0; i < data.length; i++) {
        var clone = billTileTemplate.cloneNode(true);
        //TODO das hier muss geändert werden falls billTileTemplate geändert wird

        clone.children[0].children[0].textContent = data[i].name;
        if(data[i].date_vote != null) {
            clone.children[0].children[1].textContent = faellig(data[i].date_vote);
        }
        if((DDA.Cookie.getSessionUser().admin) && (DDA.Cookie.getSessionUser().id==1)){
            clone.children[0].children[2].textContent = "" + data[i].readCount + ";" + data[i].read_detail_count + ";" + data[i].relative_value + ";" + data[i].ranking;
        }
        bid0 = "btn" + i;
        clone.id = bid0;

        billContainer.append(clone);

        const pp = data[i];

        $("#" + bid0).off().click(function () {
            $('#stage').fadeOut(300, function () {
                heProbablyReadTillHere(pp.id);
                passedBill = pp;
                $('#stage').load('template/gesetzdetail.html?uu=' + randomString()).fadeIn(300);
            });
        });

    }
    $('#billTileTemplate').hide();
    //billTileTemplate.parentNode.removeChild(billTileTemplate);
    billContainer.style = "display:block;"

}

//billSequences readbills

function heProbablyReadTillHere(bill_id){
    if ("" + DDA.Cookie.getSessionUser().verificationstatus == "VERIFIED") {
        //rausfinden in welcher Liste bis wo gelesen wurde
        var listNr;
        for (listNr = 0; listNr < billSequences.length; listNr++) {
            ind = billSequences[listNr].indexOf(bill_id);
            if (ind != -1) {
                break;
            }
        }
        if (ind == -1) {
            logoutIfExpired();
            // showErrorToast("Etwas lief schief. Bitte Seite neu laden.")
            return;
        }
        //alle Kommentare in dieser Liste vorher als gelesen markieren
        newlyReadBills = [];
        ind2 = Math.min(1.3 * ind, billSequences[listNr].length - 1);
        for (var i = 0; i <= ind2; i++) {
            var c_id = billSequences[listNr][i];
            if (readbills.has(c_id)) {
                continue;
            }
            readbills.add(c_id);
            newlyReadBills.push(c_id);
        }
        savebillsAsRead(newlyReadBills, bill_id);
        //var readbills = new Set(); //enthält ids aller gelesener kommentare
    }

}

function savebillsAsRead(newlyReadBills, bill_id){
    if(newlyReadBills.length > 0) {
        logoutIfExpired();
        $.ajax({
            url: "/bills/" + DDA.Cookie.getSessionUser().id + "/saveReadBills",
            method: "PUT",
            async: false,
            traditional: true,
            data: {
                "readBillsIds": newlyReadBills,
                "readBillDetailId" : bill_id
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Etwas lief schief");
            },
            success: function (data) {
            }
        });
    }
}


function loadReadbills(){
    logoutIfExpired();
    $.ajax({
        url: "/bills/" + DDA.Cookie.getSessionUser().id + "/loadReadBillsIds",
        method: "GET",
        async: false,
        data: {
            "parliament_id": passedParliament.id,
            "parliament_role": passedParliamentRole
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            for(var i = 0; i < data.length; i++) {
                readbills.add(data[i]);
            }
        }
    });
}


loadReadbills();

$("#searchBtn").off().click(function () {
    searchterm = $("#searchField").val();
    if(searchterm == "") {
        getRankedBills();
    } else {
        getBillSearch(searchterm);
    }
});

function getBillSearch(searchterm){
    $.ajax({
        url: "/bills/" + DDA.Cookie.getSessionUser().id + "/getBillSearch",
        method: "GET",
        async: false,
        data: {
            "parliament_id": passedParliament.id,
            "parliament_role": passedParliamentRole,
            "searchterm":searchterm
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Gesetze");
        },
        success: function (data) {
            showBills(data);
        }
    });
}

///////////////////////
//////SHOW/////////////
document.getElementById("maincontainer").style.display = "block";
///////THE END///////////
/////////////////////////