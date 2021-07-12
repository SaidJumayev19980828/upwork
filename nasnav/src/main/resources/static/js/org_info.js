
function postOrgData(formData) {
    $.ajax({
        type: 'POST',
        url: "/organization/info",
        headers: {
            "User-Token": token,
            'Content-Type': undefined
        },
        cache: false,
        processData: false,
        contentType: false,
        enctype: 'multipart/form-data',
        data: formData,
        success: function (response) {
	        alert("Organization Updated!");
        },
        error: function (xhr) {
            alert(xhr.responseJSON.message +"\n" +xhr.responseJSON.error);
        }
    }); //End of Ajax
}

function createFormData(org_info) {
    var properties = $("#" + org_info).serializeJSON();
    properties.org_id = org_id;
    properties = removeEmpty(properties);

    var data = {
        properties: JSON.stringify(properties),
        logo: document.getElementById('logo').files[0]
    }

    var formData = new FormData();
    $.each(data, function(key, value){
        formData.append(key, value);
    })
    postOrgData(formData);
}

function  removeEmpty(obj) {
    for (var propName in obj)
        if (obj[propName] == "")
            delete obj[propName];

    return obj;
}