function upload(event) {
    clearErrorMsgs();

    var uploadProperties = getUploadProperties();
    var formData = createFormData(uploadProperties);

    postFormData(formData); 
}





function postFormData(formData) {
    $.ajax({
        type: 'POST',
        url: "/upload/productlist",
        headers: {
            "User-Token": token, 'Content-Type': undefined
        },
        cache: false,
        processData: false,
        contentType: false,
        enctype: 'multipart/form-data',
        data: formData,
        success: function (response) {
            console.log(response);
            alert("Upload Successful!");
        },
        error: function (xhr, status, error) {
            var err = eval("(" + xhr.responseText + ")");
            console.log(err);
            var json = JSON.parse(xhr.responseText);
            var errors = json.error;
            $("#error-desc").text(json.description);
            $("#error-msg").text(json.message);
            $("#errors").text(json.error);
        }
    }); //End of Ajax
}



function createFormData(uploadProperties) {
    var formData = new FormData();
    formData.append("csv", document.forms["file-select-form"].file.files[0]);
    formData.append('properties', new Blob([JSON.stringify(uploadProperties)], { type: "application/json" }));
    return formData;
}





function getUploadProperties() {
    var uploadProperties = $("#upload-form").serializeJSON();
    var colHeaders = $("#headers-form").serializeJSON();
    uploadProperties.dryrun = document.getElementById('dryrun').checked;
    uploadProperties.update_product = document.getElementById('update-product').checked;
    uploadProperties.update_stocks = document.getElementById('update-stocks').checked;
    uploadProperties.headers = colHeaders;
    return uploadProperties;
}





function clearErrorMsgs() {
    $("#error-desc").text('');
    $("#error-msg").text('');
    $("#errors").text('');
}