$('#header').load('template/header.html', function (){
    [passedParliament, passedParliamentRole, passedBill]=getPassedStuff();
setTabTitleName(passedBill.name);


//Gesetz
if(passedBill.parliament_role == 0) {
    document.getElementById("communityVoteHeadline").innerHTML = "Abstimmung der Bevölkerung";
    document.getElementById("dokheadline").innerHTML = "Gesetzestext";
    document.getElementById("dokheadline").innerHTML = "Gesetzestext";
}

////Initiative : Unterstützen statt ja/nein
if(passedBill.parliament_role == 1){
    $('#btnNo').hide();
    document.getElementById("yesinactive").innerHTML = "<i class=\"far fa-thumbs-up\"></i> Unterstützen";
    document.getElementById("yesactive").innerHTML = "<i class=\"fas fa-thumbs-up\"></i></em><B> Unterstützt</B>";
    document.getElementById("btnYes").classList.remove("votebtn")
    document.getElementById("btnYes").classList.add("initiativevotebtn")
    document.getElementById("communityVoteHeadline").innerHTML = "Unterschriften";
    document.getElementById("dokheadline").innerHTML = "Gesetzestext";
}

//Diskussion
if(passedBill.parliament_role == 2) {
    document.getElementById("communityVoteHeadline").innerHTML = "Bewertung des Beitrags";
    document.getElementById("dokheadline").innerHTML = "Dokumente";
    document.getElementById("contraCommentSectionHalf").style = "display:none;";
    document.getElementById("proCommentSectionHalf").style = "width:100% !important;";
    document.getElementById("writeCommentProBtn").innerHTML = "Kommentar schreiben";



}



document.getElementById("billname").textContent = passedBill.name;
document.getElementById("abstract").innerHTML = passedBill.abstr;

var userRedbox = document.getElementById("userRedbox");

if(passedBill.parliament_role == 0){
    role = "Gesetzentwurf: ";
}
if(passedBill.parliament_role == 1){
    role = "Initiative: ";
    //$('#nurBeiGesetzentwurf').hide();

}
if(passedBill.parliament_role == 2){
    role = "Diskussion: ";
    //$('#nurBeiGesetzentwurf').hide();
}

setPageTitle(role + passedBill.name);
//$('#backToDashboard').show();

$('#footerBar').fadeOut();


////////////////////////////////7
////////FOLGEN///////////////////
////////////////////////////////

var followingIds = [];

function getFollowingIds(){
    $.ajax({
        url: "/follows/" + DDA.Cookie.getSessionUser().id + "/getFollowingIds",
        method: "GET",
        async: false,
        data: {
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Folgen");
        },
        success: function (data) {
            followingIds = data;
        }
    });
}

getFollowingIds();

function toggleFollow(idToFollow){

    $.ajax({
        url: "/follows/" + DDA.Cookie.getSessionUser().id + "/toggleFollow",
        method: "PUT",
        async: false,
        data: {
            "idToFollow": idToFollow,
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Folgen");
        },
        success: function (data) {
            getFollowingIds();
        }
    });
}

function updateCommentsFollowUser(user_id){
    userCommentsList = userCommentsMap.get(user_id);
    result = false;
    if(userCommentsList == null){
        alert("bug");
    } else {
        for(var i = 0; i < userCommentsList.length; i++){
            var comment_id = userCommentsList[i];
            var clone = commentHtmls.get(comment_id);
            if(clone.user_id != user_id){
                alert("bug");
            }
            //alert(followingIds.indexOf(user_id));
            if( followingIds.indexOf(user_id) >= 0) {
                clone.children[1].children[0].children[1].innerHTML = "(<i class=\"fas fa-user-minus\"></i> nicht mehr folgen)";
                result = true;
            } else {
                clone.children[1].children[0].children[1].innerHTML = "(<i class=\"fas fa-user-plus\"></i> folgen)";
            }
        }

    }
    return result;


}


//backToDashboardLink();

actionOnDelete = 0;
targetOnDelete = -1;
repliedCommentId = null;
var ownCommentVotes = new Map();
var allCommentVotes = new Map();
var commentHtmls = new Map();
var userCommentsMap = new Map();
var userPicMap = new Map();
var commentSequences = []; // hier wird je Kommentarsektion eine Liste der Kommentare von oben nach unten abgelegt um festzustellen welche gelesen sind
var readComments = new Set(); //enthält ids aller gelesener kommentare

function Sleep(milliseconds) {
    return new Promise(resolve => setTimeout(resolve, milliseconds));
}
async function waitshortly() {
    await Sleep(1000);
    window.location.href = '/gesetzauswahl.html?p='+passedBill.parliament.id+'&pr='+passedBill.parliament_role;
}

$('#modalContainer').append('<div id="holderForNextLoad" />');
$('#holderForNextLoad').load('template/areusure-modal.html', function (){

    $("#confirmdeletebtn").off().click(function () {
        $('#areusureModal').modal('hide');

        if(actionOnDelete == "DELETEBILL") {
            //Beitrag löschen
            logoutIfExpired();
            $.ajax({
                url: "/bills/" + DDA.Cookie.getSessionUser().id + "/delete",
                method: "DELETE",
                async: false,
                data: {
                    "bill_id": passedBill.id,
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Fehler beim Löschen des Beitrags");
                },
                success: function (data) {
                    showSuccessToast("Beitrag gelöscht");

                }
            });
            waitshortly();
        } else {
            if (actionOnDelete == "DELETECOMMENT"){
                if (targetOnDelete > 0) {
                    //onDelete ist Comment_id von zu löschendem
                    if (deleteComment(targetOnDelete)) {
                        closeReplySection(targetOnDelete);
                        var cc = commentHtmls.get(targetOnDelete);
                        cc.parentNode.removeChild(cc);
                    }
                }
            } else {
                if(actionOnDelete == "REPORTCOMMENT"){
                    reportComment(targetOnDelete);
                } else {
                    alert("Unkown action " + actionOnDelete);
                }
            }
        }

    });
});

function reportComment(comment_id){
    logoutIfExpired();
    $.ajax({
        url: "/comments/" + DDA.Cookie.getSessionUser().id + "/reportComment",
        method: "PUT",
        async: false,
        data: {
            "comment_id": comment_id,
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Kommentieren");
        },
        success: function (data) {
            showSuccessToast("Kommentar gemeldet. Unser Team wird prüfen ob der Inhalt verfassungswidrig ist.")
        }
    });
}


if(passedBill.created_by != null && (passedBill.created_by.id == DDA.Cookie.getSessionUser().id ||
    (DDA.Cookie.getSessionUser().admin &&  passedBill.parliament_role == 0 )
    )) {
    document.getElementById("modifyBtn").href = '/editgesetz.html?p='+passedBill.parliament.id+'&pr='+passedBill.parliament_role+'&b=' + passedBill.id;

    $("#deleteBtn").off().click(function () {
        actionOnDelete = "DELETEBILL";
        targetOnDelete = -2;
        document.getElementById("AreUSureLabel").textContent = "Beitrag wirklich löschen?";
        document.getElementById("confirmdeletebtn").innerHTML = "<i class=\"fas fa-trash-alt\"></i>Löschen";

    });



} else {
    $('#titlecard').hide();
}



////////////////////////////////////
////////////////////////////////////
////////////////////////////////////
////////////////////////////////////
////////////////////////////////////
////////INFOS///////////////////////
////////////////////////////////////
////////////////////////////////////
////////////////////////////////////


if(true) {
    $('#SachgebieteRow').hide();
} else {
    document.getElementById("Sachgebiete").textContent = "";
}

if(true) {
    $('#StandRow').hide();
} else {
    document.getElementById("Stand").textContent = "";
}

if(passedBill.billtype == null) {
    $('#TypRow').hide();
} else {
    document.getElementById("Typ").textContent = passedBill.billtype;
}

if(passedBill.procedurekey == null) {
    $('#VorgangRow').hide();
} else {
    document.getElementById("Vorgang").textContent = passedBill.procedurekey;
}

if(passedBill.party == null) {
    $('#ParteiRow').hide();
} else {
    document.getElementById("Partei").textContent = passedBill.party.name;
}

if(passedBill.categories_bitstring == 0) {
    $('#CategoryRow').hide();
} else {
    document.getElementById("Category").textContent = intToCategoryList(passedBill.categories_bitstring);
}


//if(passedBill.parliament_role == 0) {
//    $('#UserRow').hide();
//} else {
    document.getElementById("UserDisplay").textContent = passedBill.created_by.firstname + " " + passedBill.created_by.name;
//}




if(passedBill.date_presented == null) {
    $('#ErstelltDatumRow').hide();
} else {
    s = passedBill.date_presented.toString();
    s2 = s.substr(8,2) + "." + s.substr(5,2) + "." + s.substr(0,4)
    document.getElementById("ErstelltDatum").textContent = s2;
}

if(passedBill.date_vote == null) {
    $('#AbstimmDatumRow').hide();
} else {
    s = passedBill.date_vote.toString();
    s2 = s.substr(8,2) + "." + s.substr(5,2) + "." + s.substr(0,4)
    document.getElementById("AbstimmDatum").textContent = s2;
}

document.getElementById("Parlament").textContent = passedBill.parliament.name;

////////////////////////////////////
////////////////////////////////////
/////////Dokumente//////////////////
////////////////////////////////////
////////////////////////////////////

logoutIfExpired();
$.ajax({
    url: "/billfiles/get",
    method: "GET",
    async: false,
    data: {
        "bill_id": passedBill.id,
    },
    error: function (xhr, ajaxOptions, thrownError) {
        showErrorToast("Fehler beim Laden der Anhänge");
    },
    success: function (data) {
        showOwnFiles(data);

    }
});

function showOwnFiles(data){

    $('#downloadtest').show();

    var parent = document.getElementById('billfilesbody');
    while (parent.lastChild) {
        parent.removeChild(parent.lastChild);
    }
    //$('#downloadtest').show();

    for (var j = 0; j < data.length; j++) {
        testArray = data[j].bytes;
        str = "";
        for (var i = 0; i < testArray.length; i++) {
            str += testArray[i];
        }

        var pointparts=data[j].filename.split(".");
        var l = pointparts.length;
        fileending = pointparts[l-1];

        var downloadtest = document.getElementById('downloadtest').cloneNode(true);
        downloadtest.id="somerandomstring"+j;
        downloadtest.href="data:file/"+fileending+";base64," + str;
        downloadtest.download=data[j].filename;
        downloadtest.innerHTML="<u>"+ data[j].filename + "</u><br>";
        parent.appendChild(downloadtest);

    }
    $('#downloadtest').hide();

}

////////////////////////////////////
////////////////////////////////////
/////////ABSTIMMUNG////////////////
////////////////////////////////////
////////////////////////////////////




var yesvotes = 0;
var novotes = 0;
userVote = null;


$.ajax({
    url: "/userBillVotes/" + DDA.Cookie.getSessionUser().id + "/getVotes",
    method: "GET",
    async: false,
    data: {
        "bill_id": passedBill.id
    },
    error: function (xhr, ajaxOptions, thrownError) {
        showErrorToast("Etwas lief schief");
    },
    success: function (data) {
        yesvotes = data[0];
        novotes = data[1];
    }
});

setBars(userRedbox, yesvotes, novotes, 0, passedBill.parliament_role == 1);


$.ajax({
    url: "/userBillVotes/" + DDA.Cookie.getSessionUser().id + "/getUserVote",
    method: "GET",
    async: false,
    data: {
        "bill_id": passedBill.id
    },
    error: function (xhr, ajaxOptions, thrownError) {
        showErrorToast("Etwas lief schief");
    },
    success: function (data) {
        userVote = data;
        if(data == ""){
            userVote = null;
        }

        updateYesNoText()
    }
});

$('#btnYes').off().click(function () {

    pressVote(true);
});

$('#btnNo').off().click(function () {
    pressVote(false);
});


function pressVote(newvote) {
    if (userVote != null) {
        if (userVote.vote) {
            yesvotes = yesvotes - 1;
        } else {
            novotes = novotes - 1;
        }
    }

    if(userVote != null &&newvote == userVote.vote){
        deletevote();
    } else {
        castvote(newvote);
        if(newvote){
            yesvotes = yesvotes + 1;
        } else {
            novotes = novotes + 1;
        }
    }
    if ("" + DDA.Cookie.getSessionUser().verificationstatus == "VERIFIED") {
        setBars(userRedbox, yesvotes, novotes,0, passedBill.parliament_role == 1);
    } else {
        showLinkToKontoToast("Deine Stimme wurde abgegeben, wird aber erst gezählt sobald dein Account verifiziert ist. (<a href=\"/konto.html\" target=\"_blank\" rel=\"noopener noreferrer\">Klicke hier</a>)");

    }
}

function castvote(newvote){
    logoutIfExpired();
    $.ajax({
        url: "/userBillVotes/" + DDA.Cookie.getSessionUser().id + "/vote",
        method: "PUT",
        async: false,
        data: {
            "bill_id": passedBill.id,
            "vote": newvote
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            userVote = data;
            updateYesNoText()
        }
    });
}

function deletevote(){
    logoutIfExpired();
    $.ajax({
        url: "/userBillVotes/" + DDA.Cookie.getSessionUser().id + "/deleteVote",
        method: "DELETE",
        async: false,
        data: {
            "bill_id": passedBill.id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            userVote = null;
            updateYesNoText()
        }
    });

}




function updateYesNoText(){

    $('#yesinactive').hide();
    $('#yesactive').hide();
    $('#noinactive').hide();
    $('#noactive').hide();

    if(userVote == null || userVote == undefined) {
        $('#yesinactive').show();
        $('#noinactive').show();
    } else {
        if (userVote.vote){
            $('#yesactive').show();
            $('#noinactive').show();
        } else {
            $('#yesinactive').show();
            $('#noactive').show();
        }
    }

}


///////////////////////////////////
//////////PARLAMENT-ABSTIMMUNG///////
//////////////////////////////////
////////////////////////////////////
logoutIfExpired();
$.ajax({
    url: "/reprBillVotes/" + DDA.Cookie.getSessionUser().id + "/getVotes",
    method: "GET",
    async: false,
    data: {
        "bill_id": passedBill.id
    },
    error: function (xhr, ajaxOptions, thrownError) {
        showErrorToast("Fehler beim Laden der Stimmen");
        $('#parlamentabstimmung').hide();

    },
    success: function (data) {
        if(data.length > 0){
            showReprVotes(data);
        } else {
            $('#parlamentabstimmung').hide();
        }
    }
});

function showReprVotes(data){
    for(var i = 0; i < data.length; i++){
        showReprVote(getParty(data[i].party_id).name, data[i].yesvotes, data[i].novotes, data[i].abstinences);
    }
    var template = document.getElementById('reprVotesTemplate');
    template.parentNode.removeChild(template);
}

function showReprVote(partyName, reprVoteY, reprVoteN, reprVoteA){
    var container = document.getElementById('reprVotesContainer');
    var template = document.getElementById('reprVotesTemplate');

    var clone = template.cloneNode(true);
    clone.id = "redbox_" + partyName;
    clone.children[0].children[0].textContent = partyName;
    container.appendChild(clone);
    var thisredbox = clone.children[0].children[1];
    setBars(thisredbox, reprVoteY, reprVoteN, reprVoteA, false);

}

function getParty(party_id){
    result = null;
    logoutIfExpired();
    $.ajax({
        url: "/seats/getParty",
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



/////////////////////////////////////
////////////////////////////////////
////////////////////////////////////
////////////////////////////////
//KOMMENTARE ABGEBEN//////////////
////////////////////////////////////
////////////////////////////////////
////////////////////////////////////

//Kommentar abgeben
$('#commentProBtn').off().click(function () {

    createComment();
    if(repliedCommentId != null) {
        resetWriteCommentSection();
    }
})

/*$('#commentContraBtn').off().click(function () {
    createComment(false);
})*/

function createComment(){
    if($("#inputComment").val() == ""){
    } else {
        logoutIfExpired();
        $.ajax({
            url: "/comments/" + DDA.Cookie.getSessionUser().id + "/create",
            method: "PUT",
            async: false,
            data: {
                "text": $("#inputComment").val(),
                "bill_id": passedBill.id,
                "replied_comment_id": repliedCommentId
                //"pro": pro
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Fehler beim Kommentieren");
            },
            success: function (data) {

                document.getElementById("inputComment").value = "";

                if ("" + DDA.Cookie.getSessionUser().verificationstatus == "VERIFIED") {

                    showSuccessToast("Kommentar abgegeben");
                    $('#commenttemplate').show();
                    var commenttemplate = document.querySelector('#commenttemplate');


                    if(repliedCommentId == -1){
                        whereToPost = document.getElementById("proCommentSection");
                    } else if(repliedCommentId == -2){
                        whereToPost = document.getElementById("contraCommentSection");
                    } else {
                        var cc = commentHtmls.get(repliedCommentId);
                        whereToPost = cc.children[2].lastChild;
                        v1 = allCommentVotes.get(repliedCommentId).get("REPLIES");
                        allCommentVotes.get(repliedCommentId).set("REPLIES", v1+1);
                        //setCommentVotesText(repliedCommentId);
                    }

                    showComment(data, commenttemplate, whereToPost);
                    showOthersProfilepicBundle([data.id]);
                    updateCommentVoteButtons(data.id);



                    datamap = nothingToMap();
                    allCommentVotes.set(data.id, datamap);

                    $('#commenttemplate').hide();

                } else {
                    showLinkToKontoToast("Dein Kommentar wurde abgegeben, wird aber erst angezeigt sobald dein Account verifiziert ist. (<a href=\"/konto.html\" target=\"_blank\" rel=\"noopener noreferrer\">Klicke hier</a>)");
                }
            }
        });
    }

}


/////////////////////////////////
/////////COUNTDOWN/////////////
/////////////////////////////////

if (typeof lastInterval === 'undefined') {
    var lastInterval = undefined;
}

function initCountdown() {

    if (typeof lastInterval === 'undefined') {
    } else {
        clearInterval(lastInterval);
    }
    if(passedBill.parliament_role == 0) {
        var second = 1000,
            minute = second * 60,
            hour = minute * 60,
            day = hour * 24;
        var deadline = passedBill.date_vote;

        //let deadline = "Mar 23, 2021 11:35:00",
        var countDown = new Date(deadline).getTime();

        lastIntervalFun = function () {
            let now = new Date().getTime(),
                distance = countDown - now;

            (document.getElementById("days").innerText = Math.floor(distance / day)),
                (document.getElementById("hours").innerText = Math.floor(
                    (distance % day) / hour
                )),
                (document.getElementById("minutes").innerText = Math.floor(
                    (distance % hour) / minute
                )),
                (document.getElementById("seconds").innerText = Math.floor(
                    (distance % minute) / second
                ));

            //do something later when date is reached
            if (distance < 0) {
                let headline = document.getElementById("countdownheadline"),
                    countdown = document.getElementById("countdown");
                headline.innerText = "Die Abstimmung ist beendet. Ergebnis: " + passedBill.final_yes_votes + " Ja-Stimmen, " + passedBill.final_no_votes + " Nein-Stimmen";
                countdown.style.display = "none";
                clearInterval(x);
            }
        }
        lastInterval = setInterval(lastIntervalFun, 1000);
    } else {
        let headline = document.getElementById("countdownheadline"),
            countdown = document.getElementById("countdown");
        headline.style.display = "none";
        countdown.style.display = "none";
    }
}

initCountdown();



///////////////////////
//////SHOW/////////////
showOwnProfilepic();
document.getElementById("maincontainer").style.display = "block";


////////////////////////////////////
////////////////////////////////////
////////////////////////////////////
////////////////////////////////////
///////////////////////////////////
//Kommentare anzeigen///////////////
////////////////////////////////////
////////////////////////////////////
////////////////////////////////////
////////////////////////////////////


function loadReplies(comment_id) {
    logoutIfExpired();
    $.ajax({
        url: "/comments/" + DDA.Cookie.getSessionUser().id + "/getRankedComments",
        method: "GET",
        async: false,
        data: {
            "bill_id": passedBill.id,
            "reply_comment_id": comment_id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden der Kommentare");
        },
        success: function (data) {
            showComments(data, comment_id);
        }
    });
}

loadReplies(-1);
loadReplies(-2);




function showComments(data, comment_id) {
    $('#commenttemplate').show();
    var commenttemplate = document.querySelector('#commenttemplate');

    var readCommentSection = null;
    if(comment_id == -1){
        readCommentSection = document.getElementById("proCommentSection");
    } else if(comment_id == -2){
        readCommentSection = document.getElementById("contraCommentSection")
    } else if(comment_id > 0){
        var cc = commentHtmls.get(comment_id);
        readCommentSection = cc.children[2].lastChild;
    } else {
        alert("ungültige comment_id " + comment_id);
    }
    readCommentSection.style = "display:none;"

    //
    commentids = [];
    for (var i = 0; i < data.length ; i++) {
        commentids.push(data[i].id);
    }
    commentSequences.push(commentids);
    for (var i = data.length-1; i >= 0  ; i--) {
        showComment(data[i], commenttemplate, readCommentSection);
    }

    loadCommentVotesBundle(commentids);
    showOthersProfilepicBundle(commentids)
    $('#commenttemplate').hide();
    readCommentSection.style = "display:block;"

}

function loadCommentVotesBundle(commentids){

    if(commentids.length > 0) {

        $.ajax({
            url: "/commentrating/getVotesAsStringBundle",
            method: "GET",
            async: false,
            data: {
                "comment_ids": arrayToString(commentids)
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Etwas lief schief");
            },
            success: function (data) {
                for (var i = 0; i < data.length; i++) {
                    datamap = stringToMap(data[i]);
                    allCommentVotes.set(commentids[i], datamap);
                    setCommentVotesText(commentids[i]);
                }
            }
        });

        $.ajax({
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
        });
        //loadCommentVotes(commentids[i]);
    }

}

function strip(number) {
    return (parseFloat(number).toPrecision(5));
}


function showComment(commentdata, commenttemplate, readCommentSection){
    var clone = commenttemplate.cloneNode(true);
    readCommentSection.prepend(clone);
    clone.id = "clone" + commentdata.id;
    clone.user_id = commentdata.user.id;

    userCommentsList = userCommentsMap.get(commentdata.user.id);
    if(userCommentsList == null){
        userCommentsMap.set(commentdata.user.id, [commentdata.id]);
    } else {
        if(userCommentsList.indexOf(commentdata.id) < 0) {
            userCommentsList.push(commentdata.id);
            userCommentsMap.set(commentdata.user.id, userCommentsList);
        }
    }


    //TODO das hier muss geändert werden falls commenttemplate geändert wird
    //Autor-Name
    clone.children[1].children[0].children[0].textContent = commentdata.user.firstname + " " + commentdata.user.name;
    //..vor 4 Stunden
    clone.children[1].children[0].children[2].textContent = erstelltVor(commentdata.created_time);
    //Kommentar
    clone.children[1].children[1].innerHTML = commentdata.text;

    //Ranking-Info, zu löschen
    if((DDA.Cookie.getSessionUser().admin) && (DDA.Cookie.getSessionUser().id==1)) {
        clone.children[1].children[1].innerHTML = commentdata.text +  "<br>R" + commentdata.readCount + ";Rv" + strip(commentdata.relative_value) + ";Ra" + strip(commentdata.ranking) + ";cuRa" + strip(commentdata.customRanking);
    }

    //Profilbild
    commentHtmls.set(commentdata.id, clone);



    const pp = commentdata;
    const cc = clone;

    if(commentdata.user.id == DDA.Cookie.getSessionUser().id){
        //Eigenes Kommentar

        //Kein Cursor beim Liken
        clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_SOURCE].classList.remove("pointer")
        clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_PROP].classList.remove("pointer")
        clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_POINT].classList.remove("pointer")
        clone.children[1].children[2].children[RP_LIKE].children[0].classList.remove("pointer")



        //Folgen nicht möglich
        clone.children[1].children[0].children[1].style.display='none';
        //clone.children[1].children[2].children[1].style.visibility='hidden';
        rndid = "rndid_" + commentdata.id + "_5";
        //Delete button
        clone.children[1].children[2].children[RP_DELETE].id = rndid;
        $('#'+rndid).off().click(function () {
            actionOnDelete = "DELETECOMMENT";
            targetOnDelete = pp.id;
            document.getElementById("AreUSureLabel").textContent = "Kommentar wirklich löschen?";
            document.getElementById("confirmdeletebtn").innerHTML = "<i class=\"fas fa-trash-alt\"></i>Löschen";

        });

        rndid6 = "rndid_" + commentdata.id + "_6";
        //Antworten-btn
        clone.children[1].children[2].children[RP_REPLY].id = rndid6;
        $('#'+rndid6).off().click(function () {
            toggleShowReplies(pp.id);
        });

    } else {
        //Fremdes Kommentar
        updateCommentsFollowUser(commentdata.user.id);

        rndid52 = "rndid_" + commentdata.id + "_52";
        clone.children[1].children[0].children[1].id = rndid52;

        $('#'+rndid52).off().click(function () {
            toggleFollow(pp.user.id);
            if(updateCommentsFollowUser(pp.user.id)){
                showSuccessToast("Kommentare von "+clone.children[1].children[0].children[0].textContent+" werden nun für Dich weiter oben angezeigt.");
            }
        });

        //Löschen-Knopf wird Report-Knopf
        //clone.children[1].children[2].children[RP_DELETE].style.visibility='hidden';
        rndid = "rndid_" + commentdata.id + "_5";
        clone.children[1].children[2].children[RP_DELETE].id = rndid;
        clone.children[1].children[2].children[RP_DELETE].children[0].innerHTML = "Melden";

        $('#'+rndid).off().click(function () {
            heProbablyReadTillHere(pp.id);
            actionOnDelete = "REPORTCOMMENT";
            targetOnDelete = pp.id;
            document.getElementById("AreUSureLabel").textContent = "Verfassungswidriges Kommentar melden?";
            document.getElementById("confirmdeletebtn").innerHTML = "Melden";
        });


        ////////////Like-btn/////////////
        //Source
        rndid = "rndid_" + commentdata.id + "_1" + "_" + RP_DP_SOURCE;
        clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_SOURCE].id = rndid;
        $('#'+rndid).off().click(function () {
            heProbablyReadTillHere(pp.id);
            upvoteComment(pp.id, "GOODSOURCE", false);
        });

        //Point
        rndid17 = "rndid_" + commentdata.id + "_1" + "_" + RP_DP_POINT;
        clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_POINT].id = rndid17;
        $('#'+rndid17).off().click(function () {
            heProbablyReadTillHere(pp.id);
            upvoteComment(pp.id, "GOODPOINT", false);
        });

        //Prop
        rndid27 = "rndid_" + commentdata.id + "_1" + "_" + RP_DP_PROP;
        clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_PROP].id = rndid27;
        $('#'+rndid27).off().click(function () {
            heProbablyReadTillHere(pp.id);
            upvoteComment(pp.id, "GOODPROPOSAL", false);
        });

        //Default
        rndid37 = "rndid_" + commentdata.id + "_1" + "_DEFAULT";
        clone.children[1].children[2].children[RP_LIKE].children[0].id = rndid37;
        $('#'+rndid37).off().click(function () {
            //goodpoint oder delete
            heProbablyReadTillHere(pp.id);
            upvoteComment(pp.id, "GOODPOINT", true);
        });








        //////////////////////

        rndid6 = "rndid_" + commentdata.id + "_6";
        //Antworten-btn
        clone.children[1].children[2].children[RP_REPLY].id = rndid6;
        $('#'+rndid6).off().click(function () {
            heProbablyReadTillHere(pp.id);
            toggleShowReplies(pp.id);
        });
    }

    //clone.children[1].children[2].children[4].style.visibility='hidden'; //Editieren


}

function deleteComment(c_id){
    result = false;
    logoutIfExpired();
    $.ajax({
        url: "/comments/" + DDA.Cookie.getSessionUser().id + "/delete",
        method: "DELETE",
        async: false,
        data: {
            "comment_id":  c_id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Löschen des Kommentars");

        },
        success: function (data) {
            showSuccessToast("Kommentar gelöscht");
            result = true;

        }
    });
    return result;
}


/////////////////////////


function showOwnProfilepic(){
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
            if (data.length >= 1) {
                var img = document.getElementById('usercommentprofilepic');
                testArray = data[0].bytes;
                str = "";
                for (var i = 0; i < testArray.length; i++) {
                    str += testArray[i];
                }
                //img.style="max-width:100%";
                img.src =  "data:image/png;base64," + str;
            }
        }
    });
}

function showOthersProfilepicBundle(comment_ids){

    user_ids = [];
    for(var k = 0; k < comment_ids.length; k++){
        var u_id = commentHtmls.get(comment_ids[k]).user_id;
        if(userPicMap.get(u_id) == null && user_ids.indexOf(u_id) < 0){
            user_ids.push(u_id);
        }
    }
    if(user_ids.length > 0) {
        strids = arrayToString(user_ids);
        $.ajax({
            url: "/profilepicfiles/" + DDA.Cookie.getSessionUser().id + "/getForOthersBundle",
            method: "GET",
            async: false,
            data: {
                "othersids": strids
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Fehler beim Laden des Profilbilds");
            },
            success: function (data) {
                if(data.length != user_ids.length){
                    alert("bug");
                }
                for (var j = 0; j < data.length; j++) {
                    testArray = data[j].bytes;
                    str = "";
                    for (var i = 0; i < testArray.length; i++) {
                        str += testArray[i];
                    }
                    userPicMap.set(user_ids[j], str);
                }
            }
        });
    }

    for(var k = 0; k < comment_ids.length; k++){
        var clone = commentHtmls.get(comment_ids[k]);
        var img = clone.children[0].children[0];
        img.src = "data:image/png;base64," + userPicMap.get(clone.user_id);
    }


}

///////////////////////////////////////
///////////////////////////////////////
///////////////////////////////////////
/////////KOMMENTARE BEWERTEN///////////
///////////////////////////////////////
///////////////////////////////////////
///////////////////////////////////////

function nothingToMap(){
    var result = new Map();
    result.set("REPLIES", 0);
    result.set("GOODPOINT", 0);
    result.set("GOODSOURCE", 0);
    result.set("GOODPROPOSAL", 0);
    return result;
}






function upvoteComment(comment_id, newrating, deleteany) {
    userCommentVote = ownCommentVotes.get(comment_id);
    if (userCommentVote != null) {
        //Stimme von da wegnehmen wo sie vorher war
        v0 = allCommentVotes.get(comment_id).get(userCommentVote.rating);
        allCommentVotes.get(comment_id).set(userCommentVote.rating, v0-1);
    }

    if(userCommentVote != null && (deleteany || userCommentVote.rating == newrating)){
        deleteCommentvote(comment_id);
    } else {
        castCommentvote(comment_id, newrating);

        v1 = allCommentVotes.get(comment_id).get(newrating);
        allCommentVotes.get(comment_id).set(newrating, v1+1);

    }
    if ("" + DDA.Cookie.getSessionUser().verificationstatus == "VERIFIED") {
        setCommentVotesText(comment_id);
    } else {
        showLinkToKontoToast("Deine Stimme wurde abgegeben, wird aber erst gezählt sobald dein Account verifiziert ist. (<a href=\"/konto.html\" target=\"_blank\" rel=\"noopener noreferrer\">Klicke hier</a>)");
    }
}

function castCommentvote(comment_id, newvote){
    logoutIfExpired();
    $.ajax({
        url: "/commentrating/" + DDA.Cookie.getSessionUser().id + "/vote",
        method: "PUT",
        async: false,
        data: {
            "comment_id": comment_id,
            "vote": newvote
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            ownCommentVotes.set(comment_id, data);
            updateCommentVoteButtons(comment_id);
        }
    });
}

function deleteCommentvote(comment_id){
    logoutIfExpired();
    $.ajax({
        url: "/commentrating/" + DDA.Cookie.getSessionUser().id + "/deleteVote",
        method: "DELETE",
        async: false,
        data: {
            "comment_id": comment_id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            ownCommentVotes.set(comment_id, null);
            updateCommentVoteButtons(comment_id)
        }
    });
}


function updateCommentVoteButtons(comment_id){
    var clone = commentHtmls.get(comment_id);
    //Like-btn
    clone.children[1].children[2].children[RP_LIKE].children[0].innerHTML = "<i class=\"fas fa-thumbs-up\"></i>";
    clone.children[1].children[2].children[RP_LIKE].children[0].style="color:grey;";

    //clone.children[1].children[2].children[RP_LIKE].children[1]
    clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_POINT].style="color:grey;";
    clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_PROP].style="color:grey;";
    clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_SOURCE].style="color:grey;";

    var ownVote = ownCommentVotes.get(comment_id);
    if(ownVote != null){
            //Like-btn
        clone.children[1].children[2].children[RP_LIKE].children[0].style="color:black;";

        if(ownVote.rating == "GOODPOINT"){
            clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_POINT].style="color:black;";
        }
        if(ownVote.rating == "GOODPROPOSAL"){
            clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_PROP].style="color:black;";
        }
        if(ownVote.rating == "GOODSOURCE"){
            clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_SOURCE].style="color:black;";
        }
    }
}

function getRplMsg(comment_id){
    var voteCounts = allCommentVotes.get(comment_id);
    rpl = voteCounts.get("REPLIES");
    rplmsg = " " + rpl + " Antworten";
    if(rpl == 0){
        rplmsg = " Antworten";
    }
    if(rpl == 1) {
        rplmsg = " 1 Antwort";
    }
    return rplmsg;
}

function setCommentVotesText(comment_id){
    var clone = commentHtmls.get(comment_id);
    var voteCounts = allCommentVotes.get(comment_id);
    vGPT = voteCounts.get("GOODPOINT");
    vGS = voteCounts.get("GOODSOURCE");
    vGPP = voteCounts.get("GOODPROPOSAL");

    v_all = vGPP + vGPT + vGS;
    //Like-btn
    clone.children[1].children[2].children[RP_LIKE].children[1].innerHTML = " " + v_all;
    clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_POINT].innerHTML = "<i class=\"fas fa-thumbs-up\"></i> (" +vGPT+ ") Mag ich";
    clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_PROP].innerHTML = "<i class=\"fas fa-handshake\"></i> (" +vGPP+ ") Gutes Argument";
    clone.children[1].children[2].children[RP_LIKE].children[2].children[RP_DP_SOURCE].innerHTML = "<i class=\"fas fa-book-open\"></i> (" +vGS+ ") Gute Quelle";
    clone.children[1].children[2].children[RP_REPLY].children[0].innerHTML = getRplMsg(comment_id);
}

////////////////////////////////////////////
////////////ANTWORTEN///////////////////////
////////////////////////////////////////////

$('#writeCommentProBtn').off().click(function () {
    toggleShowReplies(-1);
});

$('#writeCommentContraBtn').off().click(function () {
    toggleShowReplies(-2);
});

function toggleShowReplies(comment_id){
    if(isReplysectionOpen(comment_id)){
    //if(repliedCommentId == comment_id){ //TODO if replycontainer leer
        //alle Antworten ausblenden
        closeReplySection(comment_id)
    } else {
        if(repliedCommentId != null){
            //closeReplySection(repliedCommentId);
        }
        closeReplySection(comment_id);
        openReplySection(comment_id);
    }
}

function resetWriteCommentSection(){
    var commentsectionscontainer = document.getElementById("commentsectionscontainer");
    var writeCommentSection = document.getElementById("writeCommentSection");
    writeCommentSection.style="display:none;";
    commentsectionscontainer.insertBefore(writeCommentSection, commentsectionscontainer.children[1]);
    //$('#commentContraBtn').show();
    document.getElementById("commentProBtn").innerText="Kommentieren";
    repliedCommentId = null;
}

function closeReplySection(comment_id){
    resetWriteCommentSection();
    if(comment_id > 0) {
        var clone = commentHtmls.get(comment_id);
        clone.children[1].children[2].children[RP_REPLY].children[0].innerHTML = getRplMsg(comment_id);
        var replycontainer = clone.children[2];
        //vorherige Antworten ausblenden
        while (replycontainer.lastChild) {
            replycontainer.removeChild(replycontainer.lastChild);
        }
    }
}

function isReplysectionOpen(comment_id){
    if(comment_id > 0) {
        var clone = commentHtmls.get(comment_id);
        var replycontainer = clone.children[2];
        if (replycontainer.lastChild) {
            return true;
        }
        return false;
    } else {
        return repliedCommentId == comment_id
    }
    return false;
}

function openReplySection(comment_id){
    repliedCommentId = comment_id;
    var writeCommentSection = document.getElementById("writeCommentSection");
    writeCommentSection.style="display:block;";

    if(comment_id < 0) {
        if (comment_id == -1) {
            document.getElementById("writeCommentProBtn").after(writeCommentSection)
        }
        if (comment_id == -2) {
            document.getElementById("writeCommentContraBtn").after(writeCommentSection)
        }
    } else {
        //Antwort-Text in Kommentar ändern
        var clone = commentHtmls.get(comment_id);
        clone.children[1].children[2].children[RP_REPLY].children[0].innerHTML = "Antworten einklappen";

        var replycontainer = clone.children[2];
        //repliedCommentId = comment_id;
        //Input-Feld verschieben
        replycontainer.appendChild(writeCommentSection);

        //Nur 1 Knopf
        //$('#commentContraBtn').hide();
        document.getElementById("commentProBtn").innerText = "Antworten";
        //Kommentarsektion erstellen
        var replyCommentSection = document.createElement('ul');
        replyCommentSection.className = "comment-section";
        replycontainer.appendChild(replyCommentSection);
        loadReplies(comment_id);
    }
}

/////////////////////////////////////////////////////////
/////////GELESENE KOMMENTARE IDENTIFIZIEREN//////////////
/////////////////////////////////////////////////////////

function heProbablyReadTillHere(comment_id){
    if ("" + DDA.Cookie.getSessionUser().verificationstatus == "VERIFIED") {
        //rausfinden in welcher Liste bis wo gelesen wurde
        var listNr;
        for (listNr = 0; listNr < commentSequences.length; listNr++) {
            ind = commentSequences[listNr].indexOf(comment_id);
            if (ind != -1) {
                break;
            }
        }
        if (ind == -1) {
            showErrorToast("Etwas lief schief. Bitte Seite neu laden.")
            return;
        }
        //alle Kommentare in dieser Liste vorher als gelesen markieren
        newlyReadComments = [];
        ind2 = Math.min(1.3 * ind, commentSequences[listNr].length - 1);
        for (var i = 0; i <= ind2; i++) {
            var c_id = commentSequences[listNr][i];
            if (readComments.has(c_id)) {
                continue;
            }
            readComments.add(c_id);
            newlyReadComments.push(c_id);
        }
        saveCommentsAsRead(newlyReadComments);
        //var readComments = new Set(); //enthält ids aller gelesener kommentare
    }

}

function saveCommentsAsRead(newlyReadComments){
    if(newlyReadComments.length > 0) {
        logoutIfExpired();
        $.ajax({
            url: "/comments/" + DDA.Cookie.getSessionUser().id + "/saveReadComments",
            method: "PUT",
            async: false,
            traditional: true,
            data: {
                "readCommentsIds": newlyReadComments
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Etwas lief schief");
            },
            success: function (data) {
            }
        });
    }
}


function loadReadComments(){
    logoutIfExpired();
    $.ajax({
        url: "/comments/" + DDA.Cookie.getSessionUser().id + "/loadReadCommentsIds",
        method: "GET",
        async: false,
        data: {
            "bill_id": passedBill.id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Etwas lief schief");
        },
        success: function (data) {
            for(var i = 0; i < data.length; i++) {
                readComments.add(data[i]);
            }
        }
    });
}

loadReadComments();





///////THE END///////////
/////////////////////////
});