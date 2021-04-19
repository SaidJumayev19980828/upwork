


function postOrgData(formName) {
    $.ajax({
        type: 'POST',
        url: "/admin/organization",
        headers: {
            "User-Token": token,
            'Content-Type': "application/json; charset=utf-8"
        },
        enctype: 'application/json',
        data: JSON.stringify($("#"+formName).serializeJSON()),
        success: function (response) {
            console.log(response);
	        alert("Organization created!\nOrganization ID = " + response.organization_id);
        },
        error: function (xhr) {
            alert(xhr.responseJSON.message +"\n" +xhr.responseJSON.error);
        }
    }); //End of Ajax
}
