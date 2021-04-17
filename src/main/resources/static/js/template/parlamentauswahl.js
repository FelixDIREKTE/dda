$('#header').load('template/header.html', function (){
    $('#footer').load('template/footer.html');


setTabTitleName("");
setTabTitleName("");


$('#footerBar').fadeIn();





//alert("1");

if( typeof logoutIfExpired === 'undefined') {
} else {
    logoutIfExpired();
}
//alert("2");


// Back to Dashboard Link (Home Icon)


///////////////////
$.ajax({
    url: "/parliaments/" + DDA.Cookie.getSessionUser().id + "/getEligibleParliaments",
    method: "GET",
    async: false,
    data: {
    },
    error: function (xhr, ajaxOptions, thrownError) {
        showErrorToast("Fehler beim Laden der Parlamente");
    },
    success: function (data) {
        showParliaments(data);

    }
});

function showParliaments(data) {
    var parliamentCardTemplate = document.querySelector('#parliamentCardTemplate');
    var lastCard = parliamentCardTemplate;
    for (var i = 0; i < data.length; i++) {
        var clone = parliamentCardTemplate.cloneNode(true);
        //TODO das hier muss geändert werden falls parliamentCardTemplate geändert wird
        clone.children[0].children[0].textContent = data[i].name;
        bid0 = "btn" + i + "_a";
        bid1 = "btn" + i + "_i";
        bid2 = "btn" + i + "_d";
        clone.children[0].children[1].children[0].children[0].id = bid0;
        clone.children[0].children[1].children[0].children[1].id = bid1;
        clone.children[0].children[1].children[0].children[2].id = bid2;
        if(data[i].name == "Europäisches Parlament"){
            btnbad = clone.children[0].children[1].children[0].children[1];
            btnbad.style.visibility='hidden';
            //btnbad.parentNode.removeChild(btnbad);
        }
        lastCard.after(clone);
        lastCard = clone;
        const pp = data[i];
        $("#" + bid0).off().click(function () {
            $('#stage').fadeOut(300, function () {
                window.location.href = '/gesetzauswahl.html?p='+pp.id+'&pr=0';
            });
        });

        $("#" + bid1).off().click(function () {
            $('#stage').fadeOut(300, function () {
                window.location.href = '/gesetzauswahl.html?p='+pp.id+'&pr=1';
            });
        });

        $("#" + bid2).off().click(function () {
            $('#stage').fadeOut(300, function () {
                window.location.href = '/gesetzauswahl.html?p='+pp.id+'&pr=2';

            });
        });

        showParliamentPicture(clone, pp);


    }
    parliamentCardTemplate.parentNode.removeChild(parliamentCardTemplate);

    var parlContainer = document.querySelector('#parlContainer');
    parlContainer.style = "display:block;"
}

function showParliamentPicture(clone, pp){
    $.ajax({
        url: "/parliamentpicfiles/" + DDA.Cookie.getSessionUser().id + "/getForOthers",
        method: "GET",
        async: false,
        data: {
            "parliament_id": pp.id
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Fehler beim Laden des Profilbilds");
        },
        success: function (data) {
            if(data.length > 0){
                testArray = data[0].bytes;
                url = "data:image/png;base64,";
                for (var i = 0; i < testArray.length; i++) {
                    url += testArray[i];
                }


                clone.style="background: url(\""+ url.replace(/(\r\n|\n|\r)/gm, "") +"\") no-repeat center;background-size: 100% auto;"
                     //+"  width: 100%; height: auto%;";


            }
        }
    });
}

if (typeof setPageTitle === 'undefined') {
} else {
    setPageTitle('Parlament wählen');
}
//Willkommen
if (DDA.Cookie.getSessionUser().verificationstatus == "VERIFIED") {
    $('#welcomeRow').hide();
}




//////Email verifizieren
const urlPars = new URLSearchParams(window.location.search);
const ve = urlPars.get('ve');
if(ve != null && ve != ""){
    $.ajax({
        url: "/users/" + DDA.Cookie.getSessionUser().id + "/verifymail",
        method: "PUT",
        async: false,
        data: {
            "ve":ve
        },
        error: function (xhr, ajaxOptions, thrownError) {
            showErrorToast("Verifizierung fehlgeschlagen. Bist Du eingeloggt?");
        },
        success: function (data) {
            if(data){
                showSuccessToast("Emailadresse wurde verifiziert.");
            } else {
                showErrorToast("Verifizierung fehlgeschlagen. Bist Du eingeloggt?");
            }
        }
    });
}



});