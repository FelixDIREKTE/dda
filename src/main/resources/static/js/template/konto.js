

$('#header').load('template/header.html?uu=' + randomString(), function (){

    setTabTitleName("");
    setPageTitle('Benutzerkonto');





$('#footerBar').fadeOut();


logoutIfExpired();


    if(DDA.Cookie.getSessionUser() == null){
    } else {


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
            emailverif = DDA.Cookie.getSessionUser().emailverif;

            isNameMissing = (firstname == null || uname == null);
            isAddressMissing = (zipcode == null || street == null || housenr == null);
            isBirthdateMissing = (birthdate == null);
            isemailverifmissing = (emailverif != null) && (emailverif != "");


            //missingEmailVerif
            if (isemailverifmissing) {
                $('#missingEmailVerif').show();
                $('#resendEV').show();

            } else {
                $('#missingEmailVerif').hide();
                $('#resendEV').hide();

            }


            if (isNameMissing) {
                $('#missingName').show();
            } else {
                $('#missingName').hide();
            }

            if (isAddressMissing) {
                $('#missingAddress').show();
            } else {
                $('#missingAddress').hide();
            }

            if (isBirthdateMissing) {
                $('#missingBirthdate').show();
            } else {
                $('#missingBirthdate').hide();
            }
            if (isParliamentMissing) {
                $('#missingParliaments').show();
            } else {
                $('#missingParliaments').hide();
            }
            if(verifstatus == "VERIFIED" || verifstatus == "REVERIFYEMAIL"){
                isIdentityProofMissing = false;
            }
            if (isIdentityProofMissing) {
                $('#missingIdentityProof').show();
            } else {
                $('#missingIdentityProof').hide();
            }

            if (isNameMissing || isAddressMissing || isBirthdateMissing || isParliamentMissing || isIdentityProofMissing || isemailverifmissing) {
                $('#missingDataHeadline').show();
            } else {
                $('#missingDataHeadline').hide();
            }

            vss = "" + verifstatus;
            document.getElementById("sli0").style = "color:grey;";
            document.getElementById("sli1").style = "color:grey;"
            document.getElementById("sli2").style = "color:grey;"

            if (vss == "DATANEEDED") {
                document.getElementById("sli0").style = "color:inherit;";
                $('#verproofcard').show();
                //$('#verproofexplanation').show();

            } else {
                //$('#verproofexplanation').hide();
            }

            if (vss == "WAITINGFORVERIF" || vss == "LOCKEDBYADMIN") {
                document.getElementById("sli1").style = "color:inherit;";
                $('#verproofcard').show();
            }


            if (vss == "VERIFIED") {
                document.getElementById("sli2").style = "color:inherit;";
                $('#verproofcard').hide();
            }

            if (vss == "REVERIFYEMAIL") {
                $('#verproofcard').hide();
            }

            if (phonenr != null) {
                document.getElementById("inputphonenr").value = phonenr;
            }
            if (zipcode != null) {
                document.getElementById("inputplz").value = zipcode;
            }
            if (uname != null) {
                document.getElementById("inputname").value = uname;
            }
            if (firstname != null) {
                document.getElementById("inputvorname").value = firstname;
            }
            if (street != null) {
                document.getElementById("inputstr").value = street;
            }
            if (housenr != null) {
                document.getElementById("inputhausnr").value = housenr;
            }
            if (birthdate != null) {
                s = birthdate.toString();
                s2 = s.substr(8, 2) + "." + s.substr(5, 2) + "." + s.substr(0, 4)
                document.getElementById("geburtsdatum").value = s2;
                $('#missingBirthdate').hide();
            } else {
                document.getElementById("missingBirthdate").display = 'block';
            }
            if (email != null) {
                document.getElementById("loginEmailInput2").value = email;
            }


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
                    //showErrorToast(xhr.responseJSON.message);
                },
                success: function (data) {
                    $("#WaitingModal2").modal("hide");
                    $("#uploadImportData2, #customFile2").attr("disabled", false);


                    if (data == "TOOBIG") {
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


        function changeEmail() {
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
                            showSuccessToast("Email geändert. Bitte verifiziere deine Emailadresse über den dir zugesandten Link.");
                            updateDisplay();
                        } else {
                            showErrorToast(data);
                        }
                    }
                });
            } else {
                showErrorToast("Diese E-Mail-Adresse ist nicht gültig!")
            }

        }

        function changePassword() {
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


        function updateUser() {
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
                    if (data == "ok") {
                        showSuccessToast("Änderungen gespeichert");
                        updateDisplay();
                        saveParliaments();
                    } else {
                        showErrorToast(data);
                    }
                }
            });

        }

        function updateverproofbody() {
            logoutIfExpired();
            $.ajax({
                url: "/verificationfiles/" + DDA.Cookie.getSessionUser().id + "/getForSelf",
                method: "GET",
                async: false,
                data: {},
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Fehler beim Laden der Identitätsnachweise");
                },
                success: function (data) {
                    showOwnImages(data);
                    updateDisplay();

                }
            });
        }

        function showOwnImages(data) {
            var parent = document.getElementById('verproofbody');
            while (parent.lastChild) {
                parent.removeChild(parent.lastChild);
            }

            isIdentityProofMissing = (data.length == 0 && DDA.Cookie.getSessionUser().verificationstatus != "VERIFIED" && DDA.Cookie.getSessionUser().verificationstatus != "REVERIFYEMAIL");

            for (var j = 0; j < data.length; j++) {
                testArray = data[j].bytes;
                str = "";
                for (var i = 0; i < testArray.length; i++) {
                    str += testArray[i];
                }
                var img = document.createElement('img');
                img.style = "max-width:100%";
                img.src = "data:image/png;base64," + str;
                parent.appendChild(img);
            }
        }


        $("#deleteData").off().click(function () {
            logoutIfExpired();
            $.ajax({
                url: "/verificationfiles/" + DDA.Cookie.getSessionUser().id + "/deleteAll",
                method: "DELETE",
                async: false,
                data: {},
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
        $('#holderForNextLoad').load('template/areusure-modal.html', function () {
            $("#confirmdeletebtn").off().click(function () {
                logoutIfExpired();

                if (ondelete == "RESENDVE") {
                    resendVE();
                } else {
                    if (ondelete == "DELETEUSER") {
                        deleteAccount();
                    } else {
                        if (ondelete == "") {
                            alert("ondelete empty");
                        } else {
                            alert("ondelete weird: " + ondelete);

                        }
                    }
                }


            });

        });

        function resendVE() {
            $.ajax({
                url: "/users/" + DDA.Cookie.getSessionUser().id + "/resendVE",
                method: "PUT",
                async: false,
                data: {},
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Fehler");
                },
                success: function (data) {
                    showSuccessToast("Email wurde geschickt");
                }
            });
        }


        function deleteAccount() {
            $.ajax({
                url: "/users/" + DDA.Cookie.getSessionUser().id + "/deleteSelf",
                method: "DELETE",
                async: false,
                data: {},
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Fehler beim Löschen des Accounts");
                },
                success: function (data) {
                    showSuccessToast("Account gelöscht");
                    logout();
                }
            });
        }


/////////////////////////////
///////Parlamente//////////
//////////////////////////

        eligibleParliaments = null;

        $.ajax({
            url: "/parliaments/" + DDA.Cookie.getSessionUser().id + "/getEligibleParliamentsComplete",
            method: "GET",
            async: false,
            data: {},
            error: function (xhr, ajaxOptions, thrownError) {
                showErrorToast("Fehler beim Laden der Parlamente");
            },
            success: function (data) {
                eligibleParliaments = data;
                if (eligibleParliaments.length == 6) {
                    isParliamentMissing = false;
                } else {
                    isParliamentMissing = true;
                }
            }
        });


        parliamentChoices = [-1, 1, 2, -1, -1, -1, -1];

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

        function fillParliamentsSelect(data, parliamentLvl) {
            if (data.length > 0) {
                for (var i = parliamentLvl; i <= 6; i++) {
                    emptySelect(i);
                }

                var selectParliamentLandtag = document.getElementById("selectParliament" + parliamentLvl)
                var div = document.createElement('option');  //creating element
                div.textContent = "";         //adding text on the element
                selectParliamentLandtag.appendChild(div);           //appending the element
                var correctI = -1;
                for (var i = 0; i < data.length; i++) {
                    var div = document.createElement('option');  //creating element
                    div.textContent = data[i].name;         //adding text on the element
                    //if data[i].name in eligibleParliaments.name -> selected
                    for (var j = 0; j < eligibleParliaments.length; j++) {
                        if (eligibleParliaments[j].id == data[i].id) {
                            correctI = i;
                            break;
                        }
                    }
                    selectParliamentLandtag.appendChild(div);           //appending the element
                }
                if (correctI >= 0) {
                    selectParliamentLandtag.value = data[correctI].name;
                    requestParliamentAccess(data[correctI].id, parliamentLvl);
                }

                selectParliamentLandtag.onchange = function () {
                    var selectedParliament = null;
                    for (var i = 0; i < data.length; i++) {
                        if (data[i].name == selectParliamentLandtag.value) {
                            selectedParliament = data[i];
                            break;
                        }
                    }
                    if (selectedParliament == null) {
                        //alert("bug");
                    }
                    requestParliamentAccess(selectedParliament.id, parliamentLvl);
                };

            } else {

            }
        }

        function emptySelect(parliamentLvl) {
            parliamentChoices[parliamentLvl] = -1;
            var selectParliamentLandtag = document.getElementById("selectParliament" + parliamentLvl);
            while (selectParliamentLandtag.lastChild) {
                selectParliamentLandtag.removeChild(selectParliamentLandtag.lastChild);
            }
        }

        function requestParliamentAccess(parliament_id, parliamentLvl) {
            parliamentChoices[parliamentLvl] = parliament_id
            fetchSubParliaments(parliament_id, parliamentLvl + 1)
        }


        $('#parliamentsSaveChanges').off().click(function () {
            logoutIfExpired();
            updateUser();


        });

        var ondelete = "";
        $('#deleteUser').off().click(function () {
            ondelete = "DELETEUSER";
            document.getElementById("AreUSureLabel").textContent = "Account wirklich löschen?";
            document.getElementById("confirmdeletebtn").innerHTML = "<i class=\"fas fa-trash-alt\"></i>Löschen";
        });

        $('#resendEV').off().click(function () {
            ondelete = "RESENDVE";
            document.getElementById("AreUSureLabel").textContent = "Email mit Verifizierungs-Link erneut senden?";
            document.getElementById("confirmdeletebtn").innerHTML = "Senden";
        });


        function saveParliaments() {
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
                    if (data == "ok") {
                        if (parliamentChoices[3] > 0 && parliamentChoices[4] > 0 && parliamentChoices[5] > 0 && parliamentChoices[6] > 0) {
                            isParliamentMissing = false;
                        } else {
                            isParliamentMissing = true;
                        }
                        updateDisplay();
                        showSuccessToast("Änderungen gespeichert");
                    } else {
                        showErrorToast(data);
                    }

                }
            });
        }


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
                    //showErrorToast(xhr.responseJSON.message);
                },
                success: function (data) {
                    $("#WaitingModal3").modal("hide");
                    $("#uploadImportData3, #customFile3").attr("disabled", false);
                    if (data == "TOOBIG") {
                        showErrorToast("Bild zu groß. Bitte höchstens 500kB.");
                    } else {
                        showSuccessToast("Profilbild aktualisiert")
                        updateprofilepicbody();
                        showHeaderProfilePic();
                    }
                }
            });

        });

        function updateprofilepicbody() {
            logoutIfExpired();
            $.ajax({
                url: "/profilepicfiles/" + DDA.Cookie.getSessionUser().id + "/getForSelf",
                method: "GET",
                async: false,
                data: {},
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Fehler beim Laden des Profilbilds");
                },
                success: function (data) {
                    showOwnProfilepic(data);
                }
            });
        }

        function showOwnProfilepic(data) {

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
                img.style = "width:20%";
                img.src = "data:image/png;base64," + str;
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


        function updateOptUser() {
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
                    if (data == "ok") {
                        showSuccessToast("Änderungen gespeichert");
                        updateDisplay();
                    } else {
                        showErrorToast(data);
                    }
                }
            });
        }

        updateverproofbody();

        $('#kontoContainer').show();
    }

});

