function postOrgData(formName) {
    $.ajax({
        type: 'POST',
        url: "/upload/productlist",
        headers: {
            "User-Token": token, 'Content-Type': undefined
        },
        enctype: 'application/json',
        data: $("#"+formName).serializeJSON();,
        success: function (response) {
            console.log(response);
	    var org = JSON.parse(response);
            alert("Organization created!\nOrganization ID = " + org.organization_id);
        },
        error: function (xhr, status, error) {
            alert( "ERROR!!\n" + xhr.responseText );
            console.log(err);
        }
    }); //End of Ajax
}
