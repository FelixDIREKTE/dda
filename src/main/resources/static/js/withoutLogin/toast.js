// Toast messages

function showInfoToast(toastMessage) {
    showToast(toastMessage, 'info', null)
}

function showSuccessToast(toastMessage) {
    showToast(toastMessage, 'success', null)
}

function showErrorToast(toastMessage) {
    showToast(toastMessage, 'error', null)
}

function showLinkToKontoToast(toastMessage) {
    showToast(toastMessage, 'success', 'konto')
}


function showToast(toastMessage, type, linkto) {
    if (type === undefined || type === null) {
        type = 'success';
    }

    $('#successIcon, #errorIcon, #infoIcon').hide();

    switch (type.toLocaleLowerCase()) {
        case 'success':
            $('#successIcon').show();
            break;
        case 'error':
            $('#errorIcon').show();
            break;
        case 'info':
            $('#infoIcon').show();
            break;
        default:
            break;
    }

    $('#toastMessage').html(toastMessage);
    $('.toast').toast('show');

    // Toast verschwindet nicht mehr wenn Knopf verlinkt...?
    /*if(linkto != null) {
        $('.toast').off().click(function () {
            $('#stage').fadeOut(300, function () {
                $('#stage').load('template/' + linkto + '.html?uu=' + randomString()).fadeIn(300);
            });
        });
    } else {
        $('.toast').off().click(function () {
        });
    }*/
}

