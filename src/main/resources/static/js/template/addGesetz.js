$('#header').load('template/header.html', function (){
    setTabTitleName("");

    if(DDA.Cookie.getSessionUser() == null){
    } else {

        [passedParliament, passedParliamentRole, passedBill] = getPassedStuff();

        parties = [];
        var selectType = document.getElementById("selectType")

        function showPartiesInParliament(data) {
            var x = document.getElementById("selectPartyPrev");
            for (var i = 0; i < data.length; i++) {
                var option = document.createElement("option");
                option.text = data[i].name;
                x.add(option);
            }
        }

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

        setPageTitle('Beitrag hinzufügen / bearbeiten');
//$('#backToDashboard').show();

        $('#footerBar').fadeOut();

//backToDashboardLink();

        role = "";
        if (passedParliamentRole == 0) {
            role = "Gesetzentwurf";
        }
        if (passedParliamentRole == 1) {
            role = "Initiative";
            $('#nurBeiGesetzentwurf').hide();

        }
        if (passedParliamentRole == 2) {
            role = "Diskussion";
            $('#nurBeiGesetzentwurf').hide();
        }

        document.getElementById("parlamentText").textContent = passedParliament.name;
        document.getElementById("beitragsartText").textContent = role;


        if (DDA.Cookie.getThemeSetting() != null && DDA.Cookie.getThemeSetting().theme === "dark") {
        } else {
        }
//Import Section
        $('#customFile2').on('change', function () {
            var fileName = $(this)[0].files[0].name;
            $(this).next('.custom-file-label').html(fileName);
        });

        $("#uploadImportData2").off().click(function () {
            if (passedBill == null) {
                showErrorToast("Der Beitrag muss angelegt werden bevor Du Dateien hochladen kannst. Klicke dafür auf 'Speichern'.");
            } else {
                logoutIfExpired();
                $("#uploadImportData2, #customFile2").attr("disabled", true);
                var data = new FormData();
                data.append('file', $("#customFile2")[0].files[0]);
                data.append('bill_id', passedBill.id);

                $("#WaitingModal2").modal("show");
                $.ajax({
                    url: "/billfiles/" + DDA.Cookie.getSessionUser().id + "/upload",
                    type: "POST",
                    enctype: 'multipart/form-data',
                    processData: false,  // Important!
                    contentType: false,
                    cache: false,
                    data: data,
                    error: function (xhr, ajaxOptions, thrownError) {
                        $("#WaitingModal2").modal("hide");
                        $("#uploadImportData2, #customFile2").attr("disabled", false);
                        showErrorToast("Datei konnte nicht verarbeitet werden, versuche es später bitte erneut.");
                    },
                    success: function (data) {
                        $("#WaitingModal2").modal("hide");
                        $("#uploadImportData2, #customFile2").attr("disabled", false);
                        if (data == "TOOBIG") {
                            showErrorToast("Datei zu groß. Bitte höchstens 2MB.");
                        } else {
                            updatebillfilesbody();
                            showSuccessToast("Datei erfolgreich eingelesen.")
                        }

                    }
                });
            }
        });


        $('#billdataAbbrechen').off().click(function () {
        });


        function updateBill() {
            logoutIfExpired();
            $.ajax({
                url: "/bills/" + DDA.Cookie.getSessionUser().id + "/updateData",
                method: "PUT",
                async: false,
                data: {
                    "title": $("#inputtitle").val(),
                    "abstract": $("#inputabstract").val(),
                    "bill_id": passedBill.id,
                    "inputParty": document.getElementById("selectPartyPrev").value,
                    "inputType": selectType.value,
                    "inputVorgang": $("#inputVorgang").val(),
                    "inputDatumVorgelegt": $("#inputDatumVorgelegt").val(),
                    "inputDatumAbstimm": $("#inputDatumAbstimm").val(),
                    "categoryBits": CategoriesToInt("kateg1", "kateg2", "kateg3", "kateg4", "kateg5", "kateg6", "kateg7", "kateg8", "kateg9", "kateg10", "kateg11", "kateg12", "kateg13", "kateg14", "kateg15", "kateg16")

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


        }


        $('#billdataSpeichern').off().click(function () {
            if (passedBill == null) {
                createBill();
            } else {
                updateBill();
            }
            savedaParty = 0;
            for (var k = 0; k < parties.length; k++) {
                if (saveReprBillVotes(parties[k])) {
                    savedaParty++;
                }
            }
            if (savedaParty == parties.length && savedaParty > 0) {
                sendResultNotification();
            }
        });

        function sendResultNotification() {
            logoutIfExpired();
            $.ajax({
                url: "/userBillVotes/" + DDA.Cookie.getSessionUser().id + "/sendResultNotification",
                method: "PUT",
                async: false,
                data: {
                    "bill_id": passedBill.id
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Etwas lief schief.");
                },
                success: function (data) {
                    showSuccessToast("Benachrichtigungen über Abstimmungsergebnis wurden versendet!");
                }
            });

        }


        function createBill() {
            if ($("#inputtitle").val().length > 200) {
                showErrorToast("Titel zu lang");
                return;
            }
            if ($("#inputtitle").val().length == 0) {
                showErrorToast("Titel erforderlich");
                return;
            }
            if ($("#inputabstract").val().length == 0) {
                showErrorToast("Zusammenfassung erforderlich");
                return;
            }

            logoutIfExpired();
            $.ajax({
                url: "/bills/" + DDA.Cookie.getSessionUser().id + "/create",
                method: "PUT",
                async: false,
                data: {
                    "title": $("#inputtitle").val(),
                    "abstract": $("#inputabstract").val(),
                    "parliament_id": passedParliament.id,
                    "parliament_role": passedParliamentRole,
                    "inputParty": document.getElementById("selectPartyPrev").value,
                    "inputType": selectType.value,
                    "inputVorgang": $("#inputVorgang").val(),
                    "inputDatumVorgelegt": $("#inputDatumVorgelegt").val(),
                    "inputDatumAbstimm": $("#inputDatumAbstimm").val(),
                    "categoryBits": CategoriesToInt("kateg1", "kateg2", "kateg3", "kateg4", "kateg5", "kateg6", "kateg7", "kateg8", "kateg9", "kateg10", "kateg11", "kateg12", "kateg13", "kateg14", "kateg15", "kateg16")

                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Etwas lief schief. Sind Titel und Zusammenfassung gegeben?");
                },
                success: function (data) {
                    passedBill = data;
                    if (passedParliamentRole == 1) {
                        showSuccessToast("Deine Initiative wurde erfolgreich angelegt.");
                    } else {
                        showSuccessToast("Beitrag angelegt");
                    }

                }
            });
        }

        $('.comboBoxSelect').combobox();

/////////////////


        function updateDisplay() {
            if (passedBill != null) {
                reloadBill();

                if (passedBill.name != null) {
                    document.getElementById("inputtitle").value = passedBill.name;
                }

                if (passedBill.abstr != null) {
                    document.getElementById("inputabstract").innerHTML = passedBill.abstr.replaceAll("<br>", "\n"); //TODO href entfernen
                    //$('#inputabstract').setAttribute("innerHTML", passedBill.abstr);
                    //document.getElementById("inputabstract").setAttribute("innerHTML", passedBill.abstr);
                }

                if (passedBill.party != null) {
                    document.getElementById("selectPartyPrev").value = passedBill.party.name;
                }

                if (passedBill.billtype != null) {
                    selectType.value = passedBill.billtype;
                }

                if (passedBill.procedurekey != null) {
                    document.getElementById("inputVorgang").value = passedBill.procedurekey;
                }

                if (passedBill.date_presented != null) {
                    s = passedBill.date_presented.toString();
                    s2 = s.substr(8, 2) + "." + s.substr(5, 2) + "." + s.substr(0, 4)
                    document.getElementById("inputDatumVorgelegt").value = s2;
                }

                if (passedBill.date_vote != null) {
                    s = passedBill.date_vote.toString();
                    s2 = s.substr(8, 2) + "." + s.substr(5, 2) + "." + s.substr(0, 4)
                    document.getElementById("inputDatumAbstimm").value = s2;
                }
            }
        }

        updateDisplay();

        function reloadBill() {
            logoutIfExpired();
            $.ajax({
                url: "/bills/getBill",
                method: "GET",
                async: false,
                data: {
                    "bill_id": passedBill.id,
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Seite konnte nicht aktualisiert werden.")
                },
                success: function (data) {
                    passedBill = data;
                }
            });

        }


        $("#deleteData").off().click(function () {
            logoutIfExpired();
            $.ajax({
                url: "/billfiles/" + DDA.Cookie.getSessionUser().id + "/deleteAll",
                method: "DELETE",
                async: false,
                data: {
                    "bill_id": passedBill.id
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Fehler beim Löschen der Identitätsnachweise");
                },
                success: function (data) {
                    updatebillfilesbody();
                    showSuccessToast("Dateien gelöscht");
                }
            });
        });
        updatebillfilesbody()

        function updatebillfilesbody() {
            if (passedBill != null) {
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
                        updateDisplay();

                    }
                });
            }

        }

        function showOwnFiles(data) {

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

                var pointparts = data[j].filename.split(".");
                var l = pointparts.length;
                fileending = pointparts[l - 1];

                var downloadtest = document.getElementById('downloadtest').cloneNode(true);
                downloadtest.id = "somerandomstring" + j;
                downloadtest.href = "data:file/" + fileending + ";base64," + str;
                downloadtest.download = data[j].filename;
                downloadtest.innerHTML = "<u>" + data[j].filename + "</u><br>";
                parent.appendChild(downloadtest);

            }
            $('#downloadtest').hide();

        }

        if (passedBill != null && passedBill.date_vote != null) {

            //getSeatsAtTime
            logoutIfExpired();
            $.ajax({
                url: "/seats/" + DDA.Cookie.getSessionUser().id + "/getSeatsAtTime",
                method: "GET",
                async: false,
                data: {
                    "parliament_id": passedParliament.id,
                    "date": passedBill.date_vote
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Fehler beim Laden der Sitze");
                },
                success: function (data) {
                    showReprVotesInput(data);

                }
            });

        } else {
            var template = document.getElementById('reprVotesTemplate');
            template.parentNode.removeChild(template);

        }

        function showReprVotesInput(data) {
            var container = document.getElementById('reprVotesContainer');
            var template = document.getElementById('reprVotesTemplate');
            for (var i = 0; i < data.length; i++) {
                party = getParty(data[i].party_id);
                parties.push(party);
                seats = data[i].seats;
                frontline = party.name + " (" + seats + ")";
                var clone = template.cloneNode(true);
                clone.children[0].textContent = frontline;
                clone.children[2].id = "input_y_" + party.id;
                clone.children[4].id = "input_n_" + party.id;
                clone.children[6].id = "input_a_" + party.id;
                container.appendChild(clone);

            }
            template.parentNode.removeChild(template);

        }

        function getParty(party_id) {
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


        function saveReprBillVotes(party) {

            votey = $("#input_y_" + party.id).val();
            voten = $("#input_n_" + party.id).val();
            votea = $("#input_a_" + party.id).val();
            result = false;
            if (votey != "" && voten != "" && votea != "") {
                logoutIfExpired();
                $.ajax({
                    url: "/reprBillVotes/" + DDA.Cookie.getSessionUser().id + "/create",
                    method: "PUT",
                    async: false,
                    data: {
                        "bill_id": passedBill.id,
                        "party_id": party.id,
                        "yesvotes": votey,
                        "novotes": voten,
                        "abstvotes": votea
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        showErrorToast("Fehler beim Speichern der Stimmen. Stimmt die Summe mit den Sitzen überein?");
                    },
                    success: function (data) {
                        result = true;
                        showSuccessToast("Stimmen gespeichert");
                    }
                });
            }
            return result;

        }


        $('#interestSaveChanges').off().click(function () {
            updateUserInterests()
        });


//////////////////
///Kategorien////
/////////////////
        if (passedBill != null) {
            intToCategories(passedBill.categories_bitstring, "kateg1", "kateg2", "kateg3", "kateg4", "kateg5", "kateg6", "kateg7", "kateg8", "kateg9", "kateg10", "kateg11", "kateg12", "kateg13", "kateg14", "kateg15", "kateg16");
        }

//////////////////////
///Datei nach URL/////
//////////////////////


        $("#urlBtn").off().click(function () {
            logoutIfExpired();
            $.ajax({
                url: "/billfiles/" + DDA.Cookie.getSessionUser().id + "/downloadByUrl",
                method: "POST",
                async: false,
                data: {
                    "url": $("#urlField").val(),
                    "bill_id": passedBill.id
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    showErrorToast("Ungültige Datei hinter URL");
                },
                success: function (data) {
                    updatebillfilesbody();
                    showSuccessToast("Dateien hochgeladen");
                }
            });
        });


        $(':input:not(textarea)').keypress(function (event) {
            return event.keyCode != 13;
        });

        $('#editBillContainer').show();
    }

});