function upload() {
    postData(createFormData());
}

function postData(formData) {
    $.ajax({
        type: 'POST',
        url: "/files",
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
	        alert("file uploaded!\nfile url = " + response);
        },
        error: function (xhr) {
            alert(xhr.responseJSON.message +"\n" +xhr.responseJSON.error);
        }
    }); //End of Ajax
}

function createFormData() {
    var org_id = isNasnavAdmin ? document.getElementById('nasnav_org_id').value : organization_id;
    var data = {
        org_id: org_id,
        file: document.getElementById('file').files[0]
    }
    var formData = new FormData();
    $.each(data, function(key, value){
        formData.append(key, value);
    })
    postData(formData);
}