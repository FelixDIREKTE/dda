
$('#header').load('template/header.html', function (){

    if(DDA.Cookie.getSessionUser()){
        myid = DDA.Cookie.getSessionUser().id;
    } else {
        myid = null;
        $('#categoriesHeadline').hide();
    }

setTabTitleName("");

title = "";

    [passedParliament, passedParliamentRole, passedBill]=getPassedStuff();

if(passedParliamentRole == 0){
    title = passedParliament.name + " - Gesetze";
    document.getElementById("parlroledescription").innerHTML = "Hier findest Du Gesetze, die im Parlament zur Abstimmung vorliegen. Eure Abstimmung entscheidet, wie die Abgeordneten der DIREKTEn abstimmen werden. ";
}
if(passedParliamentRole == 1){
    title = passedParliament.name + " - Initiativen";
    document.getElementById("adminAddBtn").innerHTML = "Initivative einreichen";
    document.getElementById("parlroledescription").innerHTML = "Hier kannst du Initiativen einreichen oder unterzeichnen. Die Initiativen mit den meisten Unterschriften werden von den Abgeordneten der DIREKTEn im Parlament zur Abstimmung vorgelegt. ";
}
if(passedParliamentRole == 2){
    title = passedParliament.name + " - Diskussionen";
    document.getElementById("parlroledescription").innerHTML = "Hier werden Diskussionen angezeigt. Du kannst deine Ansichten mit anderen Wählern austauschen, Unterstützung für deine Anliegen im Parlament und bei der Formulierung von Initiativen finden, oder Fragen stellen.";
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
if(!DDA.Cookie.getSessionUser() || (!DDA.Cookie.getSessionUser().admin && passedParliamentRole == 0) || DDA.Cookie.getSessionUser().verificationstatus != "VERIFIED") {
    $('#adminAddLink').hide();
} else {
    document.getElementById("adminAddLink").href='/editgesetz.html?p='+passedParliament.id+'&pr='+passedParliamentRole;
}

//Parlament editieren
if(!DDA.Cookie.getSessionUser() || !DDA.Cookie.getSessionUser().admin) {
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
/*$('#modalContainer').load('template/settings-modal.html', function () {
    $('#modalContainer').append('<div id="holderForNextLoad" />');
    $('#holderForNextLoad').load('template/about-modal.html');
});*/

logoutIfExpired();


function getRankedBills() {
    $.ajax({
        url: "/bills/getRankedBills",
        method: "GET",
        async: false,
        data: {
            "user_id": myid,
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
    allBillVotes = loadBillVotesBundle(billids);

    $('#billTileTemplate').show();

    var billContainer = document.querySelector('#billContainer');
    billContainer.style = "display:none;"
    while (billContainer.lastChild) {
        billContainer.removeChild(billContainer.lastChild);
    }

    var billTileTemplate = document.querySelector('#billTileTemplate');
    for (var i = 0; i < data.length; i++) {
        var clone = billTileTemplate.cloneNode(true);
        //TODO das hier muss geändert werden falls billTileTemplate geändert wird

        clone.children[0].children[0].textContent = data[i].name;

        //Votes
        datamap = stringToMap(allBillVotes[i]);
        yv = datamap.get("true");
        nv = datamap.get("false");

        if(passedParliamentRole == 0){
            setBars(clone.children[0].children[1].children[1].children[1], yv, nv, 0);
        } else {
            clone.children[0].children[1].children[1].children[1].style="display:none;";
            votestring = "";
            if(yv > 0){
                votestring = votestring + "  <i class=\"far fa-thumbs-up\"></i> " + yv + " ";
            }
            if(nv > 0){
                votestring = votestring + " &nbsp; &nbsp; &nbsp;  <i class=\"far fa-thumbs-down\"></i> " + nv;
            }
            clone.children[0].children[1].children[1].children[0].innerHTML = votestring;
        }

        if(data[i].date_vote != null) {
            clone.children[0].children[1].children[0].children[0].textContent = faellig(data[i].date_vote);
        }
        if((DDA.Cookie.getSessionUser()) && (myid==1)){
            clone.children[0].children[1].children[0].children[1].textContent = "R" + data[i].readCount + ";RD" + data[i].read_detail_count + ";rV" + data[i].relative_value + ";Ra" + data[i].ranking +";cuRa" + data[i].customRanking;
        }
        bid0 = "btn" + i;
        clone.id = bid0;
        bid1 = "btn_" + i;
        clone.children[0].id = bid1;
        billContainer.append(clone);
        const pp = data[i];
        clone.children[0].href='/gesetz.html?b=' + pp.id;
        clone.children[0].onclick=function (e){
            return heProbablyReadTillHere(pp.id);
        }
        //TODO nur bei linker Maustauste, middle mouse?

        /*$("#" + bid0).off().click(function () {
            heProbablyReadTillHere(pp.id);
            window.location.href = '/parlamentauswahl.html';
            //return false;
        });*/

    }

    $('#billTileTemplate').hide();
    billContainer.style = "display:block;"

}
//billSequences readbills

function heProbablyReadTillHere(bill_id){
    if (DDA.Cookie.getSessionUser() && "" + DDA.Cookie.getSessionUser().verificationstatus == "VERIFIED") {
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
    //return false;

}

function savebillsAsRead(newlyReadBills, bill_id){
    if(newlyReadBills.length > 0) {
        logoutIfExpired();
        $.ajax({
            url: "/bills/" + myid + "/saveReadBills",
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
        url: "/bills/" + myid + "/loadReadBillsIds",
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

if(DDA.Cookie.getSessionUser()) {
    loadReadbills();
}

$("#searchBtn").off().click(function () {
    updateSearch();
});
function updateSearch(){
    searchterm = $("#searchField").val();
    if(searchterm == "") {
        getRankedBills();
    } else {
        getBillSearch(searchterm);
    }
}

function getBillSearch(searchterm){



    $.ajax({
        url: "/bills/getBillSearch",
        method: "GET",
        async: false,
        data: {
            "user_id":myid,
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

/////////////////////////////////////
////////VOTES LADEN///////////////////
////////////////////////////////////////






function loadBillVotesBundle(billids){
    result = [];
    if(billids.length > 0) {

        $.ajax({
            url: "/userBillVotes/getVotesAsStringBundle",
            method: "GET",
            async: false,
            data: {
                "bill_ids": arrayToString(billids)
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Etwas lief schief");
            },
            success: function (data) {

                //for (var i = 0; i < data.length; i++) {
                    //datamap = stringToMap(data[i]);
                    //allCommentVotes.set(commentids[i], datamap);
                    //setCommentVotesText(commentids[i]);
                //}
                result = data;
            }
        });

        /*$.ajax({
            url: "/commentrating/" + DDA.Cookie.getSessionUser().id + "/getUserVoteBundle",
            method: "GET",
            async: false,
            data: {
                "comment_ids": arrayToString(commentids)
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Etwas lief schief");
            },
            success: function (data) {
                for (var i = 0; i < commentids.length; i++) {
                    ownCommentVotes.set(commentids[i], data[i]);
                    if (data[i] == "") {
                        ownCommentVotes.set(commentids[i], null);
                    }
                    updateCommentVoteButtons(commentids[i]);
                }
            }
        });*/
        //loadCommentVotes(commentids[i]);
    }
    return result;

}









/////////////////////////
///////KATEGORIEN////////
/////////////////////////

$('#interestSaveChanges').off().click(function () {
    updateUserInterests();
    updateSearch();
});

function initInterests(){
    $.ajax({
        url: "/users/" + myid + "/getCategories",
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

    if(DDA.Cookie.getSessionUser())
    {
        initInterests()
    }

function updateUserInterests(){

    catInt = CategoriesToInt("kateg1", "kateg2", "kateg3", "kateg4", "kateg5", "kateg6", "kateg7", "kateg8", "kateg9", "kateg10", "kateg11", "kateg12", "kateg13", "kateg14", "kateg15", "kateg16");



    $.ajax({
        url: "/users/" + myid + "/updateCategories",
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
                //showSuccessToast("Änderungen gespeichert!");
            } else {
                showErrorToast(data);
            }
        }
    });

}
var categories = document.getElementById("categories");
var categoriesHeadline = document.getElementById("categoriesHeadline");
var showCateg = false;

$('#categoriesHeadline').off().click(function () {
    toggleShowCategories();
});
function toggleShowCategories(){
    if(showCateg){
        showCateg=false;
        categories.style="display:none;"
        categoriesHeadline.innerText="Themengebiete (anzeigen)"
    } else {
        showCateg=true;
        categories.style="display:block;"
        categoriesHeadline.innerText="Themengebiete (einklappen)"

    }
}




///////////////////////
//////SHOW/////////////



$(':input:not(textarea)').keypress(function(event) {
    if(event.keyCode == 13){
        updateSearch();
        return false;
    }
    return true;
});


document.getElementById("maincontainer").style.display = "block";
///////THE END///////////
/////////////////////////

});