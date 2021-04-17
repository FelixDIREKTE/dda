


$(document).ready(function () {
    if (DDA.Cookie.getThemeSetting() == null) {
        DDA.Cookie.saveThemeSetting({theme: "bright"});
    }

    if (DDA.Cookie.getThemeSetting().theme === 'dark') {
        // Dark UI theme
        $('#theme').attr('href', 'css/style-dark.css');
        $('#highcharts').attr('href', 'css/highcharts-dark.css');
    } else {
        // Bright UI theme
        $('#theme').attr('href', 'css/style-bright.css');
        $('#highcharts').attr('href', 'css/highcharts-bright.css');
    }
    $('#toastContainer').load("template/toast.html");

    //Auto Login
    function isLoggedIn(){
        result = false;
        $.ajax({
            url: "isLoggedIn",
            method: "GET",
            async: false,
            //headers: {"Authorization": "Basic " + DDA.Cookie.getLoginData().basicAuth},
            error: function (xhr, ajaxOptions, thrownError) {
            },
            success: function (data) {
                result = true;
            }
        });
        return result;

    }


    if (DDA.Cookie.getLoginData() != null && DDA.Cookie.getLoginData().basicAuth != null && (DDA.Cookie.getSessionUser() == null || !isLoggedIn())) {
        $.ajax({
            url: "login",
            method: "POST",
            async: false,
            headers: {"Authorization": "Basic " + DDA.Cookie.getLoginData().basicAuth},
            error: function (xhr, ajaxOptions, thrownError) {
                DDA.Cookie.resetLoginInfo();
                showErrorToast(xhr.responseJSON.message);
            },
            success: function (data) {
                DDA.Cookie.saveSessionUser(data);
            }
        });

    }
});


function randomString() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}


moment.locale("de", {
    months: "Januar_Februar_M\xe4rz_April_Mai_Juni_Juli_August_September_Oktober_November_Dezember".split("_"),
    monthsShort: "Jan._Feb._M\xe4rz_Apr._Mai_Juni_Juli_Aug._Sep._Okt._Nov._Dez.".split("_"),
    monthsParseExact: !0,
    weekdays: "Sonntag_Montag_Dienstag_Mittwoch_Donnerstag_Freitag_Samstag".split("_"),
    weekdaysShort: "So._Mo._Di._Mi._Do._Fr._Sa.".split("_"),
    weekdaysMin: "So_Mo_Di_Mi_Do_Fr_Sa".split("_"),
    weekdaysParseExact: !0,
    longDateFormat: {
        LT: "HH:mm",
        LTS: "HH:mm:ss",
        L: "DD.MM.YYYY",
        LL: "D. MMMM YYYY",
        LLL: "D. MMMM YYYY HH:mm",
        LLLL: "dddd, D. MMMM YYYY HH:mm"
    },
    calendar: {
        sameDay: "[heute um] LT [Uhr]",
        sameElse: "L",
        nextDay: "[morgen um] LT [Uhr]",
        nextWeek: "dddd [um] LT [Uhr]",
        lastDay: "[gestern um] LT [Uhr]",
        lastWeek: "[letzten] dddd [um] LT [Uhr]"
    },
    relativeTime: {
        future: "in %s",
        past: "vor %s",
        s: "ein paar Sekunden",
        ss: "%d Sekunden",
        m: "eine Minute",
        mm: "%d Minuten",
        h: "eine Stunde",
        hh: "%d Stunden",
        d: "ein Tag",
        dd: "%d Tage",
        M: "einen Monat",
        MM: "%d Monate",
        y: "ein Jahr",
        yy: "%d Jahre"
    },
    dayOfMonthOrdinalParse: /\d{1,2}\./,
    ordinal: "%d.",
    week: {dow: 1, doy: 4}
});


const RP_LIKE = 0;
const RP_DELETE = 1;
const RP_REPLY = 2;
const RP_DP_SOURCE = 2;
const RP_DP_POINT = 0;
const RP_DP_PROP = 1;
const tabTitleStr = "Demokratie DIREKT!";

var notif = 0;
var billname = "";

function setTabTitleNotif(notifCnt){
    notif = notifCnt;
    updateTabTitle();
}

function setTabTitleName(ttname){
    billname = ttname;
    updateTabTitle();
}

function updateTabTitle(){
    if(notif == 0){
        prefix = "";
    } else {
        prefix = "(" + notif + ")";
    }
    if(billname == ""){
        suffix = tabTitleStr;
    } else {
        suffix = billname;
    }
    document.getElementById("tabtitle").textContent = prefix + suffix;
}

function getCurrentTime(){
    var now = new Date();
    return new Date(
        now.getUTCFullYear(),
        now.getUTCMonth(),
        now.getUTCDate(),
        now.getUTCHours(),
        now.getUTCMinutes(),
        now.getUTCSeconds()
    );
}


function erstelltVor(time){
    if(time == null){
        return "vor Sekunden"
    }
    //2021-03-09T11:53:55.000+0000
    commentDate = new Date();

    y = time.substr(0,4);
    M = "" + (parseInt(time.substr(5,2)) - 1);
    d = time.substr(8,2);
    h = time.substr(11,2);
    m = time.substr(14,2);
    s = time.substr(17,2);

    commentDate.setFullYear(y, M, d);
    commentDate.setHours(h,m,s);

    let currentDate = getCurrentTime();
    var timeDiffMin = Math.floor((currentDate.getTime() - commentDate.getTime()) / 1000 /60);
    if(timeDiffMin <= 1){
        return "vor 1 Minute";
    }

    if(timeDiffMin < 60){
        return "vor " + timeDiffMin + " Minuten";
    } else {
        timeDiffH = Math.floor(timeDiffMin / 60);
        if(timeDiffH == 1){
            return "vor 1 Stunde";
        }
        if(timeDiffH < 24){
            return "vor " + timeDiffH + " Stunden";
        } else {

            timeDiffD = Math.floor(timeDiffH / 24);

            if(timeDiffD == 1){
                return "vor 1 Tag";
            }
            if(timeDiffD < 30){
                return "vor " + timeDiffD + " Tagen";
            } else {


                timeDiffM = Math.floor(timeDiffD / 30);
                if(timeDiffM == 1){
                    return "vor 1 Monat";
                }
                if(timeDiffM < 12){
                    return "vor " + timeDiffM + " Monaten";
                } else {


                    timeDiffY = Math.floor(timeDiffD / 365);
                    if (timeDiffY == 1) {
                        return "vor 1 Jahr";
                    }
                    return "vor " + timeDiffY + " Monaten";
                }
            }
        }
    }
}

function categorieIdToBitChar(catid){
    if($("#" + catid).prop("checked")){
        return "1";
    }
    return "0";
}

function BitCharToCategorieId(bit, catid){
    if(bit == "1"){
        document.getElementById(catid).checked = true;
        //$("#" + catid).prop("checked") = true;
    } else {
        document.getElementById(catid).checked = false;
    }
}


function CategoriesToInt(catid1, catid2, catid3, catid4, catid5, catid6, catid7, catid8, catid9, catid10, catid11, catid12, catid13, catid14, catid15, catid16){
    resultStr = categorieIdToBitChar(catid1) +    categorieIdToBitChar(catid2) + categorieIdToBitChar(catid3) + categorieIdToBitChar(catid4) + categorieIdToBitChar(catid5) + categorieIdToBitChar(catid6) + categorieIdToBitChar(catid7) + categorieIdToBitChar(catid8) + categorieIdToBitChar(catid9) + categorieIdToBitChar(catid10) + categorieIdToBitChar(catid11) + categorieIdToBitChar(catid12) + categorieIdToBitChar(catid13) + categorieIdToBitChar(catid14) + categorieIdToBitChar(catid15) + categorieIdToBitChar(catid16);
    resultInt = parseInt(resultStr, 2);
    return resultInt;
}

function intToCategories(resultInt, catid1, catid2, catid3, catid4, catid5, catid6, catid7, catid8, catid9, catid10, catid11, catid12, catid13, catid14, catid15, catid16){

    resultStr = resultInt.toString(2);
    while(resultStr.length < 16){ //TODO
        resultStr = "0" + resultStr;
    }
    BitCharToCategorieId(resultStr.charAt(0), catid1 );
    BitCharToCategorieId(resultStr.charAt(1), catid2 );
    BitCharToCategorieId(resultStr.charAt(2), catid3 );
    BitCharToCategorieId(resultStr.charAt(3), catid4 );
    BitCharToCategorieId(resultStr.charAt(4), catid5 );
    BitCharToCategorieId(resultStr.charAt(5), catid6 );
    BitCharToCategorieId(resultStr.charAt(6), catid7 );
    BitCharToCategorieId(resultStr.charAt(7), catid8 );
    BitCharToCategorieId(resultStr.charAt(8), catid9 );
    BitCharToCategorieId(resultStr.charAt(9), catid10 );
    BitCharToCategorieId(resultStr.charAt(10), catid11 );
    BitCharToCategorieId(resultStr.charAt(11), catid12 );
    BitCharToCategorieId(resultStr.charAt(12), catid13 );
    BitCharToCategorieId(resultStr.charAt(13), catid14 );
    BitCharToCategorieId(resultStr.charAt(14), catid15 );
    BitCharToCategorieId(resultStr.charAt(15), catid16 );
}

function intToCategoryList(resultInt){
    resultStr = resultInt.toString(2);
    while(resultStr.length < 16){
        resultStr = "0" + resultStr;
    }
    resultList = "";
    sep = "";
    if(resultStr.charAt(0) =="1"){
        resultList = resultList + sep + "Arbeit und Soziales";
        sep = ", ";
    }
    if(resultStr.charAt(1) =="1"){
        resultList = resultList + sep + "Außen- und Sicherheitspolitik";
        sep = ", ";
    }
    if(resultStr.charAt(2) =="1"){
        resultList = resultList + sep + "Bildung";
        sep = ", ";
    }
    if(resultStr.charAt(3) =="1"){
        resultList = resultList + sep + "Digitalisierung";
        sep = ", ";
    }
    if(resultStr.charAt(4) =="1"){
        resultList = resultList + sep + "Energie";
        sep = ", ";
    }
    if(resultStr.charAt(5) =="1"){
        resultList = resultList + sep + "Finanzen / Wirtschaft ";
        sep = ", ";
    }
    if(resultStr.charAt(6) =="1"){
        resultList = resultList + sep + "Forschung und Wissenschaft";
        sep = ", ";
    }
    if(resultStr.charAt(7) =="1"){
        resultList = resultList + sep + "Gesundheit";
        sep = ", ";
    }
    if(resultStr.charAt(8) =="1"){
        resultList = resultList + sep + "Grundrechte";
        sep = ", ";
    }
    if(resultStr.charAt(9) =="1"){
        resultList = resultList + sep + "Innere Sicherheit";
        sep = ", ";
    }
    if(resultStr.charAt(10) =="1"){
        resultList = resultList + sep + "Klima";
        sep = ", ";
    }
    if(resultStr.charAt(11) =="1"){
        resultList = resultList + sep + "Landwirtschaft";
        sep = ", ";
    }
    if(resultStr.charAt(12) =="1"){
        resultList = resultList + sep + "Tierschutz";
        sep = ", ";
    }
    if(resultStr.charAt(13) =="1"){
        resultList = resultList + sep + "Umwelt";
        sep = ", ";
    }
    if(resultStr.charAt(14) =="1"){
        resultList = resultList + sep + "Verkehr";
        sep = ", ";
    }
    if(resultStr.charAt(15) =="1"){
        resultList = resultList + sep + "Verteidigung / Rüstung";
        sep = ", ";
    }
    return resultList;
}

function arrayToString(arr){
    result = "";
    sep = "";
    for(var z = 0; z < arr.length; z++){
        result = result + sep + arr[z];
        sep = ";"
    }
    return result;
}

function updateSessionUser(){
    logoutIfExpired();
    $.ajax({
        url: "/users/" + DDA.Cookie.getSessionUser().id,
        method: "GET",
        async: false,
        data: {
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Seite konnte nicht aktualisiert werden.")
        },
        success: function (data) {
            DDA.Cookie.saveSessionUser(data);
        }
    });
}

function stringToMap(s){
    var result = new Map();
    var s1 = s.split(";")

    for(var i = 0; i < s1.length; i++){
        var s2 = s1[i];
        var s3 = s2.split(":");
        result.set(s3[0], parseInt(s3[1]));
    }
    return result;
}

$(':input:not(textarea)').keypress(function(event) {
    return event.keyCode != 13;
});

function getPassedStuff() {
    const urlParams = new URLSearchParams(window.location.search);
    const passedParliamentI = urlParams.get('p');
    passedParliamentRole = urlParams.get('pr');
    const passedBillI = urlParams.get('b');
    var passedParliament = null;
    var passedBill = null;
    if (passedBillI != null) {
        $.ajax({
            url: "/bills/" + DDA.Cookie.getSessionUser().id + "/getBill",
            method: "GET",
            async: false,
            data: {
                "bill_id": passedBillI
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Seite konnte nicht geladen werden.")
            },
            success: function (data) {
                passedBill = data;
            }
        });
    }
    if (passedParliamentI != null) {

        $.ajax({
            url: "/parliaments/" + DDA.Cookie.getSessionUser().id + "/getParliament",
            method: "GET",
            async: false,
            data: {
                "parl_id": passedParliamentI
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Seite konnte nicht geladen werden.")
            },
            success: function (data) {

                passedParliament = data;
            }
        });
    }
    return [passedParliament, passedParliamentRole, passedBill];
}



// Load Settings modal and About modal:
$('#modalContainer').load('template/settings-modal.html', function () {
    $('#modalContainer').append('<div id="holderForNextLoad" />');
    $('#holderForNextLoad').load('template/about-modal.html');

    $('#modalContainer').append('<div id="holderForNextLoad1" />');
    $('#holderForNextLoad1').load('template/help-modal.html');
});


function setBars(redbox, yesvotes, novotes, abstvotes, isInitiative){
    //Balken skalieren
    if(isInitiative){
        greenwidthpercent = 100;
        whitewidthpercent = 100;
    } else {
        if (yesvotes == 0 && novotes == 0 && abstvotes == 0) {
            greenwidthpercent = 0;
            whitewidthpercent = 100;
        } else {
            whitewidthpercent = 100.0 * (yesvotes + abstvotes) / (yesvotes + novotes + abstvotes);
            if(yesvotes == 0 && abstvotes == 0) {
                greenwidthpercent = 0.0;
            } else {
                greenwidthpercent = 100.0 * yesvotes / (yesvotes + abstvotes);
            }
        }
    }
    greenmsg = "width: " + greenwidthpercent + "%;";
    whitemsg = "width: " + whitewidthpercent + "%;";
    //Ecken rund bei 100%
    if (whitewidthpercent > 99){
        whitemsg = whitemsg + "border-radius:9px;";
        if (greenwidthpercent > 99){
            greenmsg = greenmsg + "border-radius:9px;";
        }
    }
    redbox.children[0].children[0].style = greenmsg;
    if(abstvotes > 0){
        redbox.children[0].children[1].innerHTML = '' + abstvotes.toString() + "  <i class=\"fas fa-hand-paper\"></i>";
        rightmargin = 0.5*(100 - greenwidthpercent);
        redbox.children[0].children[1].style = "right:"+rightmargin+"%;";
    }
    redbox.children[0].style = whitemsg;
    redbox.children[0].children[0].children[0].innerHTML = '' + yesvotes.toString() + "   <em class=\"fas fa-thumbs-up mr-2\"></em>";
    if(!isInitiative) {
        redbox.children[1].innerHTML = '' + novotes.toString() + "   <em class=\"fas fa-thumbs-down mr-2\"></em>";
    }
    if (DDA.Cookie.getColorblind() != null && DDA.Cookie.getColorblind()) {
        redbox.style="background-color:#2c7bb6;"
    }
}

