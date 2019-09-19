function upload(event) {
    var uploadProperties = $("upload-form").serializeJSON();
    var colHeaders = $("headers-form").serializeJSON();
    uploadProperties.headers = colHeaders;


    var formData = new FormData();

    // formData.append("csv", document.forms["file-select-form"].file.files[0]);
    //formData.append('properties', new Blob([uploadProperties]), {type: "application/json"});
    formData.append('properties', new Blob([JSON.stringify(uploadProperties)]), {contentType: "application/json"});
    //formData.append("properties", document.forms["file-select-form"].file.files[0], {type: "application/json"});
    // formData.append('properties', "uploadProperties,");
    //formData.append('properties', JSON.stringify(uploadProperties));


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
            alert("Upload Successful!")
        },
        error: function (xhr, status, error) {
            var err = eval("(" + xhr.responseText + ")");
            console.log(err);
            var json = JSON.parse(xhr.responseText);
            var errors = json.error;
            $("#error-desc").text(json.description );
            $("#error-msg").text(json.message);
            $("#errors").text(json.error );
        }
    }); //End of Ajax

}