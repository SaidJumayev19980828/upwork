
function postProductFeatureData(formName) {
    $.ajax({
        type: 'POST',
        url: "/organization/products_feature",
        headers: {
            "User-Token": token,
            'Content-Type': "application/json; charset=utf-8"
        },
        enctype: 'application/json',
        data: JSON.stringify($("#"+formName).serializeJSON()),
        success: function (response) {
            console.log(response);
	        alert("Feature created!\nFeature ID = " + response.feature_id);
        },
        error: function (xhr) {
            alert(xhr.responseJSON.message +"\n" +xhr.responseJSON.error);
        }
    }); //End of Ajax
}
