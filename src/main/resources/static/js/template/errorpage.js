
$('#header').load('template/header.html', function (){
    $('#footer').load('template/footer.html');
    setTabTitleName("");
    $('#footerBar').fadeIn();
    $('#pageTitle').html('');

});