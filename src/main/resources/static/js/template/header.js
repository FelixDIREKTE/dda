// Main Nav Link "Dashboard"
function setPageTitle(title){

    if(title.length < 50) {
        $('#pageTitle').html(title);
        $('#pageTitleLong').html('');
    } else {
        $('#pageTitle').html('');
        $('#pageTitleLong').html(title);

    }
}

function logoutIfExpired(){
    /*
    if(DDA.Cookie.getSessionUser() == null || typeof DDA.Cookie.getSessionUser() === 'undefined'){
        showErrorToast("Deine Sitzung wurde beendet. Bitte logge dich erneut ein.");
        if (typeof lastIntervalC === 'undefined') {
        } else {
            clearInterval(lastIntervalC);
        }
        logout();
    }*/
}

/*$('#appTitle').off().click(function () {
    $(this).addClass('active');
    $('#linkLogbuch').removeClass('active');
    $('#stage').fadeOut(300, function () {
        window.location.href = '/parlamentauswahl.html';
    });
});*/

// Main Nav Link "Ausloggen"

function logout()
{
    $.ajax({
        url: "logout",
        method: "GET",
        async: false,
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Logout konnte nicht ausgeführt werden");
        },
        success: function () {
            DDA.Cookie.resetLoginInfo();
            $('#header, #footer').fadeOut(300);

            $('#stage').fadeOut(300, function () {
                window.location.href = '/login.html';
            });
        }
    });
}

$('#linkLogout').off().click(function () {
    logout();
});

$('#kontoButton').off().click(function () {
    $('#stage').fadeOut(300, function () {
        window.location.href = '/konto.html';
    });

});


//$('#settings').hide();

/*if(DDA.Cookie.getSessionUser().firstname != null &&  DDA.Cookie.getSessionUser().name != null) {
    $('#userName').html(DDA.Cookie.getSessionUser().firstname + " " + DDA.Cookie.getSessionUser().name);
}*/

function showHeaderProfilePic() {
    if(DDA.Cookie.getSessionUser()) {
        $.ajax({
            url: "/profilepicfiles/" + DDA.Cookie.getSessionUser().id + "/getForSelf",
            method: "GET",
            async: false,
            data: {},
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Fehler beim Laden des Profilbilds");
            },
            success: function (data) {
                if (data.length >= 1) {
                    var img = document.getElementById('userimg');
                    testArray = data[0].bytes;
                    str = "";
                    for (var i = 0; i < testArray.length; i++) {
                        str += testArray[i];
                    }
                    //img.style="max-width:100%";
                    img.src = "data:image/png;base64," + str;
                }
            }
        });
    }
}
showHeaderProfilePic();

///////////////////////////////////
/////////Notifications/////////////
///////////////////////////////////
var shownNotifications = ""
var notificationcount = document.getElementById("notificationcount");

function loadNotifications(){
    logoutIfExpired();
    $.ajax({
            url: "/notifications/" + DDA.Cookie.getSessionUser().id + "/getNotifications",
            method: "GET",
            async: false,
            data: {
        },
        error: function (xhr, ajaxOptions, thrownError) {

            showErrorToast("Fehler beim Laden der Benachrichtigungen");
            logoutIfExpired();
        },
        success: function (data) {
                showNotifications(data);

        }
    });
}

if(DDA.Cookie.getSessionUser() == null || typeof DDA.Cookie.getSessionUser() === 'undefined') {
    $('#notificationdropdown').hide();
    $('#linkVerif').hide();
    $('#kontoButton').hide();
    document.getElementById("linkLogout").innerHTML="<i class=\"fas fa-sign-in-alt mr-2\"></i> Einloggen"; //TODO Symbol
} else {
    if(!DDA.Cookie.getSessionUser().admin) {
        $('#linkVerif').hide();
    } else {
        $('#linkVerif').off().click(function () {
            $('#stage').fadeOut(300, function () {
                $('#stage').load('template/adminverifiz.html?uu=' + randomString()).fadeIn(300);
            });
        });
    }

    loadNotifications();
}


function showNotifications(data){

    shownNotifications = "";

    $('#notificationtemplate').show();
    var notificationtemplate = document.querySelector('#notificationtemplate');

    notificationcontainer = document.getElementById("notificationcontainer");
    while (notificationcontainer.lastChild) {
        notificationcontainer.removeChild(notificationcontainer.lastChild);
    }

    unreadCnt = 0;
    for (var i = data.length-1; i >= 0  ; i--) {
        showNotification(data[i], notificationtemplate, notificationcontainer);
        const n_id = data[i].id;
        shownNotifications = shownNotifications + ";" + n_id;
        if(! data[i].noted){
            unreadCnt++;
            if(data[i].message == "Dein Account wurde verifiziert!"){
                updateSessionUser();
            }
        }
    }

    if(data.length == 0){
        var clone = notificationtemplate.cloneNode(true);
        var atag = document.createElement("a");
        atag.appendChild(clone);
        notificationcontainer.prepend(atag);
        clone.id = "clone__";
        clone.children[1].textContent = "Keine Benachrichtigungen";
    }

    $('#notificationtemplate').hide();
    setUnreadMessages(unreadCnt);

}


function showNotification(notificationdata, notificationtemplate, notificationcontainer){
    var clone = notificationtemplate.cloneNode(true);

    var atag = document.createElement("a");
    atag.appendChild(clone);
    notificationcontainer.prepend(atag);


    clone.id = "clone" + notificationdata.id;
    //TODO das hier muss geändert werden falls notificationtemplate geändert wird
    //..vor 4 Stunden
    clone.children[0].textContent = erstelltVor(notificationdata.created_time);
    //Text
    clone.children[1].textContent = notificationdata.message;
    //link
    if(notificationdata.link != "" && notificationdata.link != null) {
        atag.href = notificationdata.link;
    }
}








$('#notificationBell').off().click(function () {

    loadNotifications();

    if( shownNotifications != "") {
        logoutIfExpired();
        $.ajax({
            url: "/notifications/" + DDA.Cookie.getSessionUser().id + "/markReadNotifications",
            method: "PUT",
            async: false,
            data: {
                "readNotificationsIds": shownNotifications
            },
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Fehler beim Markieren der Benachrichtigungen");
            },
            success: function (data) {
                setUnreadMessages(0);

            }
        });
    }

});

function setUnreadMessages(cnt){
    setTabTitleNotif(cnt);
    if(cnt == 0){
        notificationcount.classList.remove("active");
    } else {
        notificationcount.classList.add("active");
        notificationcount.textContent = "" + cnt;
    }

}





if (typeof lastIntervalC === 'undefined') {
} else {
    clearInterval(lastIntervalC);
}
lastIntervalC = setInterval(loadNotifications, 180000);




var theheader = document.querySelector('#theheader');
theheader.style = "display:block;"


/*function backToDashboardLink() {
    $('#backToDashboard').off().click(function () {
        $('#stage').fadeOut(300, function () {
            window.location.href = '/parlamentauswahl.html';
        });
    })
}

backToDashboardLink();*/

