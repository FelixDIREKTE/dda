
// Date Ranger Picker
// https://www.daterangepicker.com/

//German translation for Date Range Picker:
var dateRangePickerLocale = {
    "format": "DD.MM.YYYY",
    "separator": " - ",
    "applyLabel": "Übernehmen",
    "cancelLabel": "Abbrechen",
    "fromLabel": "Von",
    "toLabel": "Bis",
    "customRangeLabel": "Benutzerdefiniert",
    "weekLabel": "W",
    "daysOfWeek": [
        "So",
        "Mo",
        "Di",
        "Mi",
        "Do",
        "Fr",
        "Sa"
    ],
    "monthNames": [
        "Januar",
        "Februar",
        "März",
        "April",
        "Mai",
        "Juni",
        "Juli",
        "August",
        "September",
        "Oktober",
        "November",
        "Dezember"
    ],
    "firstDay": 1
};
var aktuellesGwjStart, aktuellesGwjEnde;
var letztesGwjStart, letztesGwjEnde;

function berechneAktuellesGwj() {
    // Aktuelles Datum ist vor dem 01.10 des aktuellen Jahres (8 = September)    
    if (moment().month() <= 8) {
        //Vom 01.10 des vorherigen Jahres:
        aktuellesGwjStart = moment().subtract(1, 'years').month(9).date(1);
        // bis 30.9 des aktuelles Jahres:
        aktuellesGwjEnde = moment().month(8).endOf('month');
    }
    // Aktuelles Datum ist nach dem 01.10 des aktuellen Jahres
    else {
        aktuellesGwjStart = moment().month(9).date(1);
        aktuellesGwjEnde = moment().add(1, 'years').month(8).endOf('month');
    }
}

function berechneLetzesGwj() {
    // Aktuelles Datum ist vor dem 01.10
    if (moment().month() <= 8) {
        letztesGwjStart = moment().subtract(2, 'years').month(9).date(1);
        letztesGwjEnde = moment().subtract(1, 'years').month(8).endOf('month');
    }
    // Aktuelles Datum ist nach dem 01.10
    else {
        letztesGwjStart = moment().subtract(1, 'years').month(9).date(1);
        letztesGwjEnde = moment().month(8).endOf('month');
    }
}

function dateRangePicker(callbackFunction) {
    dateRangePicker(callbackFunction, 0);
}

function dateRangePicker(callbackFunction, addDays) {
    berechneAktuellesGwj();
    berechneLetzesGwj();
    var lastDate = moment().add(addDays, 'days');
    var maxYear = lastDate.year();

    $('#viewRange').daterangepicker({
        'showWeekNumbers': false,
        'showDropdowns': true,
        ranges: {
            'Dieses GWJ': [aktuellesGwjStart, aktuellesGwjEnde],
            'Letztes GWJ': [letztesGwjStart, letztesGwjEnde],
            'Dieser Monat': [moment().startOf('month'), lastDate],
            'Letzter Monat': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
        },
        'locale': dateRangePickerLocale,
        'startDate': moment().subtract(12, 'days').startOf('month'), //moment().startOf('month'),
        'endDate': moment().endOf('month'),
        'autoUpdateInput': true,
        'minYear': 2010,
        'maxYear': maxYear,
        'linkedCalendars': false,
        'maxDate': lastDate
    }, callbackFunction)
}
