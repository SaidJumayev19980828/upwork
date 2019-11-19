function upload(uploadProperties) {
    clearErrorMsgs();
    postFormData(createFormData(uploadProperties));
}

function postFormData(formData) {
    $.ajax({
        type: 'POST',
        url: "/product/image/bulk",
        headers: {
            "User-Token": token, 'Content-Type': undefined
        },
        cache: false,
        processData: false,
        contentType: false,
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

function createFormData(properties) {   
    var formData = new FormData();
    formData.append('properties'
                        , new Blob( 
                                [JSON.stringify( $("#" + properties).serializeJSON()) ]
                                , { type: "application/json" })
                    );
    formData.append('imgs_barcode_csv', document.getElementById('csv').files[0]);
    formData.append('imgs_zip' , document.getElementById('zip').files[0]);
   
    return formData;
}


function clearErrorMsgs() {
    $("#error-desc").text('');
    $("#error-msg").text('');
    $("#errors").text('');
}