function upload() {
    postBrandData(createFormData());
}

function postBrandData(formData) {
    $.ajax({
        type: 'POST',
        url: "/organization/brand",
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
            console.log(response);
	        alert("Brand created!\nBrand ID = " + response.brand_id);
        },
        error: function (xhr) {
            alert(xhr.responseJSON.message +"\n" +xhr.responseJSON.error);
        }
    }); //End of Ajax
}

function createFormData(brand) {
    var data = {
        properties: JSON.stringify($("#" + brand).serializeJSON()),
        logo: document.getElementById('logo').files[0],
        banner: document.getElementById('banner').files[0]
    }
    var formData = new FormData();
    $.each(data, function(key, value){
        formData.append(key, value);
    })
    postBrandData(formData);
}